/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.persistence.service.internal.graph;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.net.HttpHeaders;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.rx.RxWebTarget;
import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.AsyncSubject;
import rx.subjects.PublishSubject;

import org.dswarm.common.DMPStatics;
import org.dswarm.common.model.util.AttributePathUtil;
import org.dswarm.common.types.Tuple;
import org.dswarm.graph.json.Resource;
import org.dswarm.graph.json.stream.ModelBuilder;
import org.dswarm.graph.json.stream.ModelParser;
import org.dswarm.graph.json.util.Util;
import org.dswarm.persistence.DMPPersistenceError;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.internal.helper.AttributePathHelper;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.UpdateFormat;
import org.dswarm.persistence.model.resource.proxy.ProxyDataModel;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.InternalModelService;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.dswarm.persistence.util.GDMUtil;

/**
 * A internal model service implementation for GDM statements processing (read, write, search, ...).<br/>
 * Currently, the Neo4j database is utilised.
 *
 * @author tgaengler
 */
@Singleton
public class InternalGDMGraphService implements InternalModelService {

	private static final Logger LOG = LoggerFactory.getLogger(InternalGDMGraphService.class);

	private static final String GDM_RESOURCE_IDENTIFIER      = "gdm";
	private static final String MAINTAIN_RESOURCE_IDENTIFIER = "maintain";
	private static final String MULTIPART_MIXED              = "multipart/mixed";
	private static final String CHUNKED                      = "CHUNKED";

	private static final String SEARCH_RESULT    = "search result";
	private static final String OBJECT_RETRIEVAL = "object retrieval";
	private static final String WRITE_GDM        = "write to graph database";

	private static final int CHUNK_SIZE      = 1024;
	private static final int REQUEST_TIMEOUT = 20000000;

	private static final String          DSWARM_MODEL_STREAMER_THREAD_NAMING_PATTERN = "dswarm-model-streamer-%d";
	private static final ExecutorService EXECUTOR_SERVICE                            = Executors.newCachedThreadPool(
			new BasicThreadFactory.Builder().daemon(false).namingPattern(DSWARM_MODEL_STREAMER_THREAD_NAMING_PATTERN).build());

	private static final ClientBuilder BUILDER                   = ClientBuilder.newBuilder().register(MultiPartFeature.class)
			.property(ClientProperties.CHUNKED_ENCODING_SIZE, CHUNK_SIZE)
			.property(ClientProperties.REQUEST_ENTITY_PROCESSING, CHUNKED)
			.property(ClientProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, CHUNK_SIZE)
			.property(ClientProperties.CONNECT_TIMEOUT, REQUEST_TIMEOUT)
			.property(ClientProperties.READ_TIMEOUT, REQUEST_TIMEOUT);
	public static final  String        METADATA_TYPE             = "metadata";
	public static final  String        DEPRECATE_DATA_MODEL_TYPE = "deprecate data model";
	public static final  String        DEPRECATE_RECORDS_TYPE = "deprecate records";

	static {

		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
	}

	private static final String READ_GDM_ENDPOINT             = "/get";
	private static final String WRITE_GDM_ENDPOINT            = "/put";
	private static final String DEPRECATE_DATA_MODEL_ENDPOINT = "/deprecate/datamodel";
	private static final String DEPRECATE_RECORDS_ENDPOINT = "/deprecate/records";
	private static final String SEARCH_GDM_RECORDS_ENDPOINT   = "/searchrecords";
	private static final String GET_GDM_RECORD_ENDPOINT       = "/getrecord";
	public static final  String CHUNKED_TRANSFER_ENCODING     = "chunked";

	/**
	 * The data model persistence service.
	 */
	private final Provider<DataModelService> dataModelService;

	/**
	 * The schema persistence service.
	 */
	private final Provider<SchemaService> schemaService;

	/**
	 * The class persistence service.
	 */
	private final Provider<ClaszService> classService;

	private final Provider<AttributePathService> attributePathService;

	private final Provider<SchemaAttributePathInstanceService> schemaAttributePathInstanceService;

	private final Provider<AttributeService> attributeService;

	private final String graphEndpoint;

	private final Provider<ObjectMapper> objectMapperProvider;

	/**
	 * Creates a new internal triple service with the given persistence services and the endpoint to access the graph database.
	 *
	 * @param dataModelService     the data model persistence service
	 * @param schemaService        the schema persistence service
	 * @param classService         the class persistence service
	 * @param attributePathService the attribute path persistence service
	 * @param attributeService     the attribute persistence service
	 * @param graphEndpointArg     the endpoint to access the graph database
	 */
	@Inject
	public InternalGDMGraphService(
			final Provider<DataModelService> dataModelService,
			final Provider<SchemaService> schemaService,
			final Provider<ClaszService> classService,
			final Provider<SchemaAttributePathInstanceService> schemaAttributePathInstanceService,
			final Provider<AttributePathService> attributePathService,
			final Provider<AttributeService> attributeService,
			@Named("dswarm.db.graph.endpoint") final String graphEndpointArg,
			final Provider<ObjectMapper> objectMapperProviderArg) {

		this.dataModelService = dataModelService;
		this.schemaService = schemaService;
		this.classService = classService;
		this.attributePathService = attributePathService;
		this.schemaAttributePathInstanceService = schemaAttributePathInstanceService;
		this.attributeService = attributeService;

		graphEndpoint = graphEndpointArg;
		objectMapperProvider = objectMapperProviderArg;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Observable<Response> createObject(final String dataModelUuid, final Observable<Model> model) throws DMPPersistenceException {

		LOG.debug("try to create data model '{}' in data hub", dataModelUuid);

		// always full at creation time, i.e., all existing records will be deprecated (however, there shouldn't be any)
		// versioning is disabled at data model creation, since there should be any data for this data model in the data hub
		final Observable<Response> result = createOrUpdateObject(dataModelUuid, model, UpdateFormat.FULL, false);
		result.doOnCompleted(() -> LOG.debug("created data model '{}' in data hub", dataModelUuid));

		return result;
	}

	@Override public Observable<Response> updateObject(final String dataModelUuid, final Observable<Model> model, final UpdateFormat updateFormat,
			final boolean enableVersioning)
			throws DMPPersistenceException {

		LOG.debug("try to update data model '{}' in data hub", dataModelUuid);

		final Observable<Response> result = createOrUpdateObject(dataModelUuid, model, updateFormat, enableVersioning);

		result.doOnCompleted(
				() -> LOG.debug("updated data model '{}' in data hub", dataModelUuid));

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Observable<Tuple<String, Model>> getObjects(final String dataModelUuid, final Optional<Integer> optionalAtMost)
			throws DMPPersistenceException {

		if (dataModelUuid == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final String dataModelURI = GDMUtil.getDataModelGraphURI(dataModelUuid);

		// retrieve record class uri from data model schema
		final DataModel dataModel = dataModelService.get().getObject(dataModelUuid);

		if (dataModel == null) {

			InternalGDMGraphService.LOG.debug("couldn't find data model '{}' to retrieve record class from", dataModelUuid);

			throw new DMPPersistenceException(String.format("couldn't find data model '%s' to retrieve record class from", dataModelUuid));
		}

		if(dataModel.isDeprecated()) {

			InternalGDMGraphService.LOG.debug("cannot retrieve data from data model '{}', because the data model is deprecated", dataModelUuid);

			return Observable.empty();
		}

		final Schema schema = dataModel.getSchema();

		if (schema == null) {

			InternalGDMGraphService.LOG.debug("couldn't find schema in data model '{}'", dataModelUuid);

			throw new DMPPersistenceException(String.format("couldn't find schema in data model '%s'", dataModelUuid));
		}

		final Clasz recordClass = schema.getRecordClass();

		if (recordClass == null) {

			InternalGDMGraphService.LOG
					.debug("couldn't find record class in schema '{}' of data model '{}'", schema.getUuid(), dataModelUuid);

			throw new DMPPersistenceException(String.format(
					"couldn't find record class in schema '%s' of data model '%s'", schema.getUuid(), dataModelUuid));
		}

		final String recordClassUri = recordClass.getUri();

		final Tuple<Observable<Resource>, InputStream> readResult = readGDMFromDB(recordClassUri, dataModelURI, optionalAtMost);
		final Observable<Resource> recordResourcesObservable = readResult.v1();
		final InputStream inputStream = readResult.v2();

		// TODO: this won'T be done right now, but is maybe also not really necessary any more
		//		final Set<Resource> recordResources = GDMUtil.getRecordResources(recordClassUri, model);
		//
		//		if (recordResources == null || recordResources.isEmpty()) {
		//
		//			InternalGDMGraphService.LOG.debug("couldn't find records for record class'{}' in data model '{}'", recordClassUri, dataModelUuid);
		//
		//			throw new DMPPersistenceException(
		//					String.format("couldn't find records for record class '%s' in data model '%s'", recordClassUri, dataModelUuid));
		//		}

		final AtomicLong counter = new AtomicLong(0);
		final AtomicLong bigCounter = new AtomicLong(1);

		return recordResourcesObservable
				.map(recordResource -> {
					final org.dswarm.graph.json.Model recordModel = new org.dswarm.graph.json.Model();
					recordModel.addResource(recordResource);
					return new GDMModel(recordModel, recordResource.getUri());
				}).doOnNext(gdmModel -> {

					final long current = counter.incrementAndGet();

					if (current / 10000 == bigCounter.get()) {

						bigCounter.incrementAndGet();

						LOG.debug("retrieved and processed '{}' records", current);
					}
				})
				.map(gdm -> Tuple.tuple(gdm.getRecordURIs().iterator().next(), (Model) gdm))
				.doOnCompleted(DMPPersistenceError.wrapped(() -> closeResource(inputStream, OBJECT_RETRIEVAL)))
				.doOnCompleted(() -> LOG.debug("finally, retrieved and processed '{}' records", counter.get()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteObject(final String dataModelUuid) throws DMPPersistenceException {

		if (dataModelUuid == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final String dataModelURI = GDMUtil.getDataModelGraphURI(dataModelUuid);

		// TODO: delete DataModel object from DB here as well?

		// dataset.begin(ReadWrite.WRITE);
		// dataset.removeNamedModel(dataModelURI);
		// dataset.commit();
		// dataset.end();

		// TODO

	}

	@Override public Observable<Response> deprecateDataModel(final String dataModelUuid) throws DMPPersistenceException {

		if (dataModelUuid == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final DataModel dataModel = getDataModel(dataModelUuid);

		if (dataModel == null) {

			final String message = String.format("data model '%s' is not available", dataModelUuid);

			LOG.debug(message);

			return Observable.empty();
		}

		final String dataModelURI = GDMUtil.getDataModelGraphURI(dataModelUuid);

		final Observable<Response> result = deprecateDataModelInternal(dataModelURI);

		return result.doOnCompleted(
				() -> {

					LOG.debug("deprecated data model '{}' in data hub", dataModelUuid);

					dataModel.setDeprecated(true);

					final DataModelService dataModelService = this.dataModelService.get();

					try {

						dataModelService.updateObjectTransactional(dataModel);
					} catch (final DMPPersistenceException e) {

						throw DMPPersistenceError.wrap(e);
					}
				});
	}

	@Override public Observable<Response> deprecateRecords(final Collection<String> recordURIs, final String dataModelUuid)
			throws DMPPersistenceException {

		if (dataModelUuid == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final DataModel dataModel = getDataModel(dataModelUuid);

		if (dataModel == null) {

			final String message = String.format("data model '%s' is not available", dataModelUuid);

			LOG.debug(message);

			return Observable.empty();
		}

		final String dataModelURI = GDMUtil.getDataModelGraphURI(dataModelUuid);

		return deprecateRecordsInternal(recordURIs, dataModelURI);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<Schema> getSchema(final String dataModelUuid) throws DMPPersistenceException {

		if (dataModelUuid == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final DataModel dataModel = dataModelService.get().getObject(dataModelUuid);

		if (dataModel == null) {

			InternalGDMGraphService.LOG.debug("couldn't find data model '{}' to retrieve it's schema", dataModelUuid);

			throw new DMPPersistenceException(String.format("couldn't find data model '%s' to retrieve it's schema", dataModelUuid));
		}

		final Schema schema = dataModel.getSchema();

		if (schema == null) {

			InternalGDMGraphService.LOG.debug("couldn't find schema in data model '{}'", dataModelUuid);

			return Optional.absent();
		}

		return Optional.of(schema);
	}

	@Override public Observable<Tuple<String, Model>> searchObjects(final String dataModelUuid, final String keyAttributePathString,
			final String searchValue, final Optional<Integer> optionalAtMost) throws DMPPersistenceException {

		if (dataModelUuid == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final String dataModelURI = GDMUtil.getDataModelGraphURI(dataModelUuid);

		final DataModel dataModel = dataModelService.get().getObject(dataModelUuid);

		if (dataModel == null) {

			InternalGDMGraphService.LOG.debug("couldn't find data model '{}' to search records", dataModelUuid);

			throw new DMPPersistenceException(String.format("couldn't find data model '%s' to search", dataModelUuid));
		}

		final Schema schema = dataModel.getSchema();

		if (schema == null) {

			InternalGDMGraphService.LOG.debug("couldn't find schema in data model '{}'", dataModelUuid);

			throw new DMPPersistenceException(String.format("couldn't find schema in data model '%s'", dataModelUuid));
		}

		final Clasz recordClass = schema.getRecordClass();

		if (recordClass == null) {

			InternalGDMGraphService.LOG
					.debug("couldn't find record class in schema '{}' of data model '{}'", schema.getUuid(), dataModelUuid);

			throw new DMPPersistenceException(String.format(
					"couldn't find record class in schema '%s' of data model '%s'", schema.getUuid(), dataModelUuid));
		}

		final String recordClassUri = recordClass.getUri();

		final Observable<org.dswarm.graph.json.Model> modelObservable =
				searchGDMRecordsInDB(keyAttributePathString, searchValue, dataModelURI, optionalAtMost);

		// TODO:
		//		if (recordResources == null || recordResources.isEmpty()) {
		//
		//			InternalGDMGraphService.LOG
		//					.debug("couldn't find records for record class '{}' in search result of key attribute path = '{}' and search value = '{}' in data model '{}'",
		//							recordClassUri, keyAttributePathString, searchValue, dataModelUuid);
		//
		//			throw new DMPPersistenceException(String.format(
		//					"couldn't find records for record class '%s' in search result of key attribute path = '%s' and search value = '%s' in data model '%s'",
		//					recordClassUri, keyAttributePathString, searchValue, dataModelUuid));
		//		}

		return modelObservable
				.filter(model -> {

					if (model.size() <= 0) {

						InternalGDMGraphService.LOG
								.debug("model is empty for key attribute path '{}' and search value '{}  in data model '{}' in database",
										keyAttributePathString, searchValue, dataModelUuid);

						return false;
					}

					return true;
				})
				.flatMapIterable(model -> GDMUtil.getRecordResources(recordClassUri, model))
				.map(resource -> {
					final org.dswarm.graph.json.Model recordModel = new org.dswarm.graph.json.Model();
					recordModel.addResource(resource);
					final GDMModel gdmModel = new GDMModel(recordModel, resource.getUri());
					return Tuple.tuple(resource.getUri(), gdmModel);
				});

	}

	@Override public Observable<Model> getRecord(final String recordIdentifier, final String dataModelUuid) throws DMPPersistenceException {

		if (recordIdentifier == null) {

			throw new DMPPersistenceException("record identifier shouldn't be null");
		}

		if (dataModelUuid == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final String dataModelURI = GDMUtil.getDataModelGraphURI(dataModelUuid);

		final Observable<Resource> resourceObservable = readGDMRecordFromDB(recordIdentifier, dataModelURI);

		return resourceObservable.filter(resource -> {
			if (resource == null) {

				InternalGDMGraphService.LOG
						.debug("couldn't find record data for record identifier '{}' in data model '{}' in database", recordIdentifier,
								dataModelUuid);

				return false;
			}

			if (resource.size() <= 0) {

				InternalGDMGraphService.LOG.debug("resource is empty for record identifier '{}' in data model '{}' in database", recordIdentifier,
						dataModelUuid);

				return false;
			}

			return true;
		}).map(resource -> {

			final org.dswarm.graph.json.Model model = new org.dswarm.graph.json.Model();
			model.addResource(resource);

			return new GDMModel(model, recordIdentifier);
		});
	}

	@Override public Observable<Tuple<String, Model>> getRecords(final Set<String> recordIdentifiers, final String dataModelUuid)
			throws DMPPersistenceException {

		if (recordIdentifiers == null) {

			throw new DMPPersistenceException("record identifiers shouldn't be null");
		}

		if (recordIdentifiers.isEmpty()) {

			throw new DMPPersistenceException("there are no record identifiers");
		}

		if (dataModelUuid == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final String dataModelURI = GDMUtil.getDataModelGraphURI(dataModelUuid);

		return Observable.from(recordIdentifiers).flatMap(recordIdentifier ->
						readGDMRecordFromDB(recordIdentifier, dataModelURI).filter(resource -> {
							if (resource == null) {

								InternalGDMGraphService.LOG
										.debug("couldn't find record data for record identifier '{}' in data model '{}' in database",
												recordIdentifier,
												dataModelUuid);
								return false;
							}

							if (resource.size() <= 0) {

								InternalGDMGraphService.LOG
										.debug("resource is empty for record identifier '{}' in data model '{}' in database", recordIdentifier,
												dataModelUuid);

								return false;

							}
							return true;
						}).map(resource -> {
							final org.dswarm.graph.json.Model model = new org.dswarm.graph.json.Model();
							model.addResource(resource);
							final Model gdmModel = new GDMModel(model, recordIdentifier);

							return Tuple.tuple(recordIdentifier, gdmModel);
						})
		);
	}

	private Observable<Response> createOrUpdateObject(final String dataModelUuid, final Observable<Model> model, final UpdateFormat updateFormat,
			final boolean enableVersioning) throws DMPPersistenceException {

		if (dataModelUuid == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		if (model == null) {

			throw new DMPPersistenceException("model that should be added to DB shouldn't be null");
		}

		final Optional<Boolean> optionalDeprecateMissingRecords = determineMissingRecordsFlag(updateFormat);
		final String dataModelURI = GDMUtil.getDataModelGraphURI(dataModelUuid);
		final DataModel dataModel = getSchemaInternal(dataModelUuid);

		final boolean isSchemaAnInBuiltSchema = isSchemaAnInbuiltSchema(dataModel);

		final Observable<GDMModel> modelObservable = model.cast(GDMModel.class);

		final GDMWriteRequestOperator operator = new GDMWriteRequestOperator(dataModel, isSchemaAnInBuiltSchema, dataModelURI,
				optionalDeprecateMissingRecords,
				enableVersioning);
		final Observable<Resource> resourceObservable = modelObservable.lift(operator).flatMapIterable(gdm -> {

			try {

				final org.dswarm.graph.json.Model model1 = getRealModel(gdm);

				if (model1 == null) {

					LOG.debug("no model available");

					return Collections.emptyList();
				}

				final Collection<Resource> resources = model1.getResources();

				if (resources == null || resources.isEmpty()) {

					LOG.debug("no resources available in model");

					return Collections.emptyList();
				}

				optionallyEnhancedDataModel(dataModel, gdm, model1, isSchemaAnInBuiltSchema);

				// note the model should always consist of one resource only
				return resources;
			} catch (final DMPPersistenceException e) {

				throw DMPPersistenceError.wrap(e);
			}
		});

		resourceObservable.subscribe(operator.resourceObserver());

		final Observable<Response> responseObservable = operator.responseObservable();

		return responseObservable.doOnCompleted(() -> {

			if(dataModel.isDeprecated()) {

				// reincarnate data model
				dataModel.setDeprecated(false);

				final DataModelService dataModelService = this.dataModelService.get();

				try {

					dataModelService.updateObjectTransactional(dataModel);
				} catch (final DMPPersistenceException e) {

					throw DMPPersistenceError.wrap(e);
				}
			}
		});
	}

	private DataModel optionallyEnhancedDataModel(final DataModel dataModel, final GDMModel gdmModel, final org.dswarm.graph.json.Model realModel,
			final boolean isSchemaAnInBuildSchema)
			throws DMPPersistenceException {

		if (!isSchemaAnInBuildSchema) {

			return determineSchema(dataModel, gdmModel, realModel);
		} else {

			return dataModel;
		}
	}

	private org.dswarm.graph.json.Model getRealModel(final GDMModel gdmModel) throws DMPPersistenceException {

		final org.dswarm.graph.json.Model realModel = gdmModel.getModel();

		if (realModel == null) {

			throw new DMPPersistenceException("real model that should be added to DB shouldn't be null");
		}

		return realModel;
	}

	private DataModel determineSchema(final DataModel dataModel, final GDMModel gdmModel, final org.dswarm.graph.json.Model realModel)
			throws DMPPersistenceException {

		LOG.debug("determine schema for data model '{}'", dataModel.getUuid());

		final DataModel updatedDataModel = addRecordClass(dataModel, gdmModel.getRecordClassURI());

		if (updatedDataModel == null) {

			throw new DMPPersistenceException("Could not get the actual data model to use");
		}

		determineRecordResources(gdmModel, realModel, updatedDataModel);

		determineAttributePaths(updatedDataModel, gdmModel);

		LOG.debug("determined schema for data model '{}'", dataModel.getUuid());

		return updatedDataModel;
	}

	private void determineRecordResources(final GDMModel gdmModel, final org.dswarm.graph.json.Model realModel, final DataModel finalDataModel) {

		LOG.debug("determine record resources for data model '{}'", finalDataModel.getUuid());

		if (finalDataModel.getSchema() != null) {

			if (finalDataModel.getSchema().getRecordClass() != null) {

				gdmModel.setRecordURIs(realModel.getResourceURIs());
			}
		}

		LOG.debug("determined record resources for data model '{}'", finalDataModel.getUuid());
	}

	private Optional<Boolean> determineMissingRecordsFlag(final UpdateFormat updateFormat) throws DMPPersistenceException {

		switch (updateFormat) {

			case FULL:

				return Optional.of(Boolean.TRUE);
			case DELTA:

				return Optional.of(Boolean.FALSE);
			default:

				throw new DMPPersistenceException(String.format("unkown update format '%s'", updateFormat));
		}
	}

	private boolean isSchemaAnInbuiltSchema(final DataModel dataModel) throws DMPPersistenceException {

		final Schema schema = dataModel.getSchema();

		if (schema != null) {

			final String schemaUUID = schema.getUuid();

			if (schemaUUID != null) {

				switch (schemaUUID) {

					case SchemaUtils.MABXML_SCHEMA_UUID:
					case SchemaUtils.MARC21_SCHEMA_UUID:
					case SchemaUtils.PNX_SCHEMA_UUID:
					case SchemaUtils.FINC_SOLR_SCHEMA_UUID:
					case SchemaUtils.OAI_PMH_DC_ELEMENTS_SCHEMA_UUID:
					case SchemaUtils.OAI_PMH_DC_TERMS_SCHEMA_UUID:
					case SchemaUtils.OAI_PMH_MARCXML_SCHEMA_UUID:

						// those schemas are already there and shouldn't be manipulated by data that differs from those schemas
						LOG.debug("schema for data model '{}' is a preset schema, so everything is already set", dataModel.getUuid());

						return true;
				}
			}
		}

		return false;
	}

	/**
	 * Adds the record class to the schema of the data model.
	 *
	 * @param dataModel the data model
	 * @param recordClassUri the identifier of the record class
	 * @throws DMPPersistenceException
	 */
	private DataModel addRecordClass(final DataModel dataModel, final String recordClassUri) throws DMPPersistenceException {

		LOG.debug("add record class '{}' to schema for data model '{}'", recordClassUri, dataModel.getUuid());

		// (try) add record class uri to schema
		final Schema schema = dataModel.getSchema();

		if (schema != null) {

			final boolean result = SchemaUtils.addRecordClass(schema, recordClassUri, classService);

			if (!result) {

				return dataModel;
			}

			LOG.debug("added record class to schema for data model '{}'", dataModel.getUuid());

			return updateDataModel(dataModel);
		}

		return dataModel;
	}

	private DataModel determineAttributePaths(final DataModel dataModel, final Model model)
			throws DMPPersistenceException {

		LOG.debug("determine attribute paths of schema for data model '{}'", dataModel.getUuid());

		final Schema schema = dataModel.getSchema();

		if (schema != null) {

			// note: model.getAttributePaths is expensive atm
			final Set<AttributePathHelper> attributePathHelpers = model.getAttributePaths();

			final boolean result = SchemaUtils.addAttributePaths(schema, attributePathHelpers,
					attributePathService, schemaAttributePathInstanceService, attributeService);

			if (!result) {

				return dataModel;
			}

			LOG.debug("determined attribute paths of schema for data model '{}'", dataModel.getUuid());

			return updateDataModel(dataModel);
		}

		return dataModel;
	}

	private DataModel updateDataModel(final DataModel dataModel) throws DMPPersistenceException {
		final ProxyDataModel proxyUpdatedDataModel = dataModelService.get().updateObjectTransactional(dataModel);

		if (proxyUpdatedDataModel == null) {

			throw new DMPPersistenceException("couldn't update data model");
		}

		return proxyUpdatedDataModel.getObject();
	}

	private DataModel getSchemaInternal(final String dataModelUuid) throws DMPPersistenceException {

		final DataModel dataModel = getDataModel(dataModelUuid);

		final Schema schema;

		if (dataModel != null && dataModel.getSchema() == null) {

			final Configuration configuration = dataModel.getConfiguration();

			Optional<String> optionalPresetSchema = null;

			if (configuration != null) {

				final JsonNode storageTypeJsonNode = configuration.getParameter(ConfigurationStatics.STORAGE_TYPE);

				if (storageTypeJsonNode != null) {

					final String storageType = storageTypeJsonNode.asText();

					if (storageType != null) {

						switch (storageType) {

							case ConfigurationStatics.MABXML_STORAGE_TYPE:
							case ConfigurationStatics.MARCXML_STORAGE_TYPE:
							case ConfigurationStatics.PNX_STORAGE_TYPE:
							case ConfigurationStatics.OAI_PMH_DC_ELEMENTS_STORAGE_TYPE:
							case ConfigurationStatics.OAIPMH_DC_TERMS_STORAGE_TYPE:
							case ConfigurationStatics.OAIPMH_MARCXML_STORAGE_TYPE:

								optionalPresetSchema = Optional.of(storageType);

								LOG.debug("found storage type '{}' for preset schema", storageType);

								break;
						}

					}
				}
			}

			if (optionalPresetSchema == null || !optionalPresetSchema.isPresent()) {

				// create new schema
				final ProxySchema proxySchema = schemaService.get().createObjectTransactional();

				if (proxySchema != null) {

					schema = proxySchema.getObject();
				} else {

					schema = null;
				}
			} else {

				switch (optionalPresetSchema.get()) {

					case ConfigurationStatics.MABXML_STORAGE_TYPE:

						// assign existing mabxml schema to data resource

						schema = schemaService.get().getObject(SchemaUtils.MABXML_SCHEMA_UUID);

						break;
					case ConfigurationStatics.MARCXML_STORAGE_TYPE:

						// assign existing marc21 schema to data resource

						schema = schemaService.get().getObject(SchemaUtils.MARC21_SCHEMA_UUID);

						break;
					case ConfigurationStatics.PNX_STORAGE_TYPE:

						// assign existing pnx schema to data resource

						schema = schemaService.get().getObject(SchemaUtils.PNX_SCHEMA_UUID);

						break;
					case ConfigurationStatics.OAI_PMH_DC_ELEMENTS_STORAGE_TYPE:

						// assign existing OAI-PMH + DC Elements schema to data resource

						schema = schemaService.get().getObject(SchemaUtils.OAI_PMH_DC_ELEMENTS_SCHEMA_UUID);

						break;
					case ConfigurationStatics.OAIPMH_DC_TERMS_STORAGE_TYPE:

						// assign existing OAI-PMH + DC Terms schema to data resource

						schema = schemaService.get().getObject(SchemaUtils.OAI_PMH_DC_TERMS_SCHEMA_UUID);

						break;
					case ConfigurationStatics.OAIPMH_MARCXML_STORAGE_TYPE:

						// assign existing OAI-PMH + MARCXML schema to data resource

						schema = schemaService.get().getObject(SchemaUtils.OAI_PMH_MARCXML_SCHEMA_UUID);

						break;
					default:

						LOG.debug("could not determine and set preset schema for identifier '{}'", optionalPresetSchema.get());

						schema = null;
				}
			}

			LOG.debug("set preset schema for data model '{}'", dataModel.getUuid());

			dataModel.setSchema(schema);
		}

		return updateDataModel(dataModel);
	}

	private DataModel getDataModel(final String dataModelUuid) {

		final DataModel dataModel = dataModelService.get().getObject(dataModelUuid);

		if (dataModel == null) {

			InternalGDMGraphService.LOG.error("couldn't find data model '{}'", dataModelUuid);

			return null;
		}

		return dataModel;
	}

	private Observable<Response> deprecateDataModelInternal(final String dataModelURI) throws DMPPersistenceException {

		LOG.debug("try to deprecate data model '{}' in data hub", dataModelURI);

		final WebTarget target = maintainTarget(DEPRECATE_DATA_MODEL_ENDPOINT);

		final RxWebTarget<RxObservableInvoker> rxWebTarget = RxObservable.from(target);

		final ObjectNode requestJSON = objectMapperProvider.get().createObjectNode();
		requestJSON.put(DMPStatics.DATA_MODEL_URI_IDENTIFIER, dataModelURI);

		final String requestJSONString = serializeObject(requestJSON, DEPRECATE_DATA_MODEL_TYPE);

		// POST the request
		final RxObservableInvoker rx = rxWebTarget.request(MediaType.APPLICATION_JSON).rx();

		final Entity<String> entity = Entity.entity(requestJSONString, MediaType.APPLICATION_JSON);

		final Observable<Response> post = rx.post(entity).subscribeOn(Schedulers.from(EXECUTOR_SERVICE));

		final PublishSubject<Response> asyncPost = PublishSubject.create();
		asyncPost.subscribe(response -> {

			//TODO maybe check status code here, i.e., should be 200

			LOG.debug("deprecated data model '{}' in data hub", dataModelURI);
		}, throwable -> {

			throw DMPPersistenceError.wrap(new DMPPersistenceException(
					String.format("Couldn't deprecate data model in database. Received status code '%s' from database endpoint.",
							throwable.getMessage())));

		}, () -> LOG.debug("completely deprecated data model '{}' in data hub", dataModelURI));

		post.subscribe(asyncPost);

		return asyncPost;
	}

	private Observable<Response> deprecateRecordsInternal(final Collection<String> recordURIs, final String dataModelURI) throws DMPPersistenceException {

		LOG.debug("try to deprecate '{}' records in data model '{}' in data hub", recordURIs.size(), dataModelURI);

		final WebTarget target = maintainTarget(DEPRECATE_RECORDS_ENDPOINT);

		final RxWebTarget<RxObservableInvoker> rxWebTarget = RxObservable.from(target);

		final ObjectNode requestJSON = objectMapperProvider.get().createObjectNode();
		requestJSON.put(DMPStatics.DATA_MODEL_URI_IDENTIFIER, dataModelURI);

		final ArrayNode recordURIsArray = createRecordURIsArray(recordURIs);
		requestJSON.set(DMPStatics.RECORDS_IDENTIFIER, recordURIsArray);

		final String requestJSONString = serializeObject(requestJSON, DEPRECATE_RECORDS_TYPE);

		// POST the request
		final RxObservableInvoker rx = rxWebTarget.request(MediaType.APPLICATION_JSON).rx();

		final Entity<String> entity = Entity.entity(requestJSONString, MediaType.APPLICATION_JSON);

		final Observable<Response> post = rx.post(entity).subscribeOn(Schedulers.from(EXECUTOR_SERVICE));

		final PublishSubject<Response> asyncPost = PublishSubject.create();
		asyncPost.subscribe(response -> {

			//TODO maybe check status code here, i.e., should be 200

			LOG.debug("deprecated some records in data model '{}' in data hub", dataModelURI);
		}, throwable -> {

			throw DMPPersistenceError.wrap(new DMPPersistenceException(
					String.format("Couldn't deprecate some records in data model '%s' in database. Received status code '%s' from database endpoint.", dataModelURI,
							throwable.getMessage())));

		}, () -> LOG.debug("completely deprecated data model '{}' in data hub", dataModelURI));

		post.subscribe(asyncPost);

		return asyncPost;
	}

	private ArrayNode createRecordURIsArray(final Collection<String> recordURIs) {

		final ArrayNode recordURIsArray = objectMapperProvider.get().createArrayNode();

		for(final String recordURI : recordURIs) {

			recordURIsArray.add(recordURI);
		}

		return recordURIsArray;
	}

	private Tuple<Observer<org.dswarm.graph.json.Resource>, Observable<Response>> writeGDMToDB(final String dataModelUri, final String metadata)
			throws DMPPersistenceException {

		LOG.debug("try to write GDM data for data model '{}' into data hub", dataModelUri);

		final WebTarget target = gdmTarget(WRITE_GDM_ENDPOINT);

		final RxWebTarget<RxObservableInvoker> rxWebTarget = RxObservable.from(target);

		final PipedInputStream input = new PipedInputStream();
		final PipedOutputStream output = new PipedOutputStream();

		try {

			final Observer<Resource> modelConsumer = EXECUTOR_SERVICE.submit(() -> {

				output.connect(input);

				return getBytes(output); // turns a Runnable into a Callable which handles exceptions
			}).get();

			final MultiPart multiPart = new MultiPart();
			final BufferedInputStream entity1 = new BufferedInputStream(input, CHUNK_SIZE);

			multiPart
					.bodyPart(metadata, MediaType.APPLICATION_JSON_TYPE)
					.bodyPart(entity1, MediaType.APPLICATION_OCTET_STREAM_TYPE);

			// POST the request
			final RxObservableInvoker rx = rxWebTarget.request(MULTIPART_MIXED).header(HttpHeaders.TRANSFER_ENCODING, CHUNKED_TRANSFER_ENCODING).rx();

			final Entity<MultiPart> entity = Entity.entity(multiPart, MULTIPART_MIXED);

			final Observable<Response> post = rx.post(entity).subscribeOn(Schedulers.from(EXECUTOR_SERVICE));

			final PublishSubject<Response> asyncPost = PublishSubject.create();
			asyncPost.subscribe(response -> {

				try {

					closeResource(multiPart, WRITE_GDM);
					closeResource(output, WRITE_GDM);
					closeResource(input, WRITE_GDM);

					//TODO maybe check status code here, i.e., should be 200

					LOG.debug("wrote GDM data for data model '{}' into data hub", dataModelUri);
				} catch (final DMPPersistenceException e) {

					throw DMPPersistenceError.wrap(e);
				}
			}, throwable -> {

				throw DMPPersistenceError.wrap(new DMPPersistenceException(
						String.format("Couldn't store GDM data into database. Received status code '%s' from database endpoint.",
								throwable.getMessage())));
			}, () -> LOG.debug("completely wrote GDM data for data model '{}' into data hub", dataModelUri));

			post.subscribe(asyncPost);

			return Tuple.tuple(modelConsumer, asyncPost);
		} catch (final InterruptedException | ExecutionException e) {

			throw new DMPPersistenceException("couldn't store GDM data into database successfully");
		}
	}

	private JsonNode generateContentSchemaJSON(final ContentSchema contentSchema) throws DMPPersistenceException {

		final org.dswarm.common.model.AttributePath recordIdentifierSAP;

		if (contentSchema.getRecordIdentifierAttributePath() != null) {

			final String recordIdentifierAP = contentSchema.getRecordIdentifierAttributePath().toAttributePath();
			recordIdentifierSAP = AttributePathUtil.parseAttributePathString(recordIdentifierAP);
		} else {

			recordIdentifierSAP = null;
		}

		final LinkedList<org.dswarm.common.model.AttributePath> keyAttributePaths;

		if (contentSchema.getKeyAttributePaths() != null) {

			keyAttributePaths = new LinkedList<>();

			for (final AttributePath keyAttributePath : contentSchema.getKeyAttributePaths()) {

				final String keyAP = keyAttributePath.toAttributePath();
				final org.dswarm.common.model.AttributePath keySAP = AttributePathUtil.parseAttributePathString(keyAP);
				keyAttributePaths.add(keySAP);
			}
		} else {

			keyAttributePaths = null;
		}

		final org.dswarm.common.model.AttributePath valueSAP;

		if (contentSchema.getValueAttributePath() != null) {

			final String valueAttributePath = contentSchema.getValueAttributePath().toAttributePath();
			valueSAP = AttributePathUtil.parseAttributePathString(valueAttributePath);
		} else {

			valueSAP = null;
		}

		final org.dswarm.common.model.ContentSchema simpleContentSchema = new org.dswarm.common.model.ContentSchema(recordIdentifierSAP,
				keyAttributePaths, valueSAP);

		try {

			final String simpleContentSchemaString = objectMapperProvider.get().writeValueAsString(simpleContentSchema);

			return objectMapperProvider.get().readValue(simpleContentSchemaString, ObjectNode.class);
		} catch (final IOException e) {

			final String message = "couldn't serialize/deserialize content schema for dmp graph endpoint";

			InternalGDMGraphService.LOG.error(message, e);

			throw new DMPPersistenceException(message, e);
		}
	}

	private Tuple<Observable<Resource>, InputStream> readGDMFromDB(final String recordClassUri, final String dataModelUri,
			final Optional<Integer> optionalAtMost)
			throws DMPPersistenceException {

		LOG.debug("try to read GDM data for data model '{}' and record class '{}' from data hub", dataModelUri, recordClassUri);

		final WebTarget target = gdmTarget(READ_GDM_ENDPOINT);

		final ObjectMapper objectMapper = DMPPersistenceUtil.getJSONObjectMapper();
		final ObjectNode requestJson = objectMapper.createObjectNode();

		requestJson.put(DMPStatics.RECORD_CLASS_URI_IDENTIFIER, recordClassUri);
		requestJson.put(DMPStatics.DATA_MODEL_URI_IDENTIFIER, dataModelUri);

		if (optionalAtMost.isPresent()) {

			requestJson.put(DMPStatics.AT_MOST_IDENTIFIER, optionalAtMost.get());
		}

		final String requestJsonString;

		try {

			requestJsonString = objectMapper.writeValueAsString(requestJson);
		} catch (final JsonProcessingException e) {

			throw new DMPPersistenceException("something went wrong, while creating the request JSON string for the read-gdm-from-db request", e);
		}

		// POST the request
		// TODO: async ?
		final Response response = target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(requestJsonString, MediaType.APPLICATION_JSON));

		if (response.getStatus() != 200) {

			throw new DMPPersistenceException(
					String.format("Couldn't read GDM data from database. Received status code '%s' from database endpoint.", response.getStatus()));
		}

		final InputStream body = response.readEntity(InputStream.class);

		LOG.debug("read GDM data for data model '{}' and record class '{}' from data hub", dataModelUri, recordClassUri);

		return deserializeModel(body);
	}

	private Observable<Resource> readGDMRecordFromDB(final String recordUri, final String dataModelUri) {

		final WebTarget target = gdmTarget(GET_GDM_RECORD_ENDPOINT);

		final ObjectMapper objectMapper = DMPPersistenceUtil.getJSONObjectMapper();
		final ObjectNode requestJson = objectMapper.createObjectNode();

		requestJson.put(DMPStatics.RECORD_URI_IDENTIFIER, recordUri);
		requestJson.put(DMPStatics.DATA_MODEL_URI_IDENTIFIER, dataModelUri);

		final String requestJsonString;

		try {

			requestJsonString = objectMapper.writeValueAsString(requestJson);
		} catch (final JsonProcessingException e) {

			return Observable.error(new DMPPersistenceException(
					"something went wrong, while creating the request JSON string for the read-gdm-record-from-db request",
					e));
		}

		// POST the request
		// TODO: re-work to rxjava jersey client
		final Future<Response> responseFuture = target.request(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON)
				.async()
				.post(Entity.entity(requestJsonString, MediaType.APPLICATION_JSON));

		return Observable.from(responseFuture)
				.flatMap(response -> {
					if (response.getStatus() != 200) {
						return Observable.error(new DMPPersistenceException(
								String.format("Couldn't read GDM record data from database. Received status code '%s' from database endpoint.",
										response.getStatus())));
					}
					return Observable.just(response.readEntity(String.class));
				})
				.map(DMPPersistenceError.wrapped(this::deserializeResource));
	}

	private Observable<org.dswarm.graph.json.Model> searchGDMRecordsInDB(final String keyAttributePathString, final String searchValue,
			final String dataModelUri,
			final Optional<Integer> optionalAtMost)
			throws DMPPersistenceException {

		final WebTarget target = gdmTarget(SEARCH_GDM_RECORDS_ENDPOINT);

		final ObjectMapper objectMapper = DMPPersistenceUtil.getJSONObjectMapper();
		final ObjectNode requestJson = objectMapper.createObjectNode();

		requestJson.put(DMPStatics.KEY_ATTRIBUTE_PATH_IDENTIFIER, keyAttributePathString);
		requestJson.put(DMPStatics.SEARCH_VALUE_IDENTIFIER, searchValue);
		requestJson.put(DMPStatics.DATA_MODEL_URI_IDENTIFIER, dataModelUri);

		if (optionalAtMost.isPresent()) {

			requestJson.put(DMPStatics.AT_MOST_IDENTIFIER, optionalAtMost.get());
		}

		final String requestJsonString;

		try {

			requestJsonString = objectMapper.writeValueAsString(requestJson);
		} catch (final JsonProcessingException e) {

			throw new DMPPersistenceException("something went wrong, while creating the request JSON string for the search-gdm-records-in-db request",
					e);
		}

		// POST the request
		// TODO: re-work to rxjava jersey client
		final Future<Response> responseFuture = target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON)
				.async()
				.post(Entity.entity(requestJsonString, MediaType.APPLICATION_JSON));

		// TODO:
		//		InternalGDMGraphService.LOG
		//				.debug("couldn't find results for key attribute path '{}' and search value '{}' in data model '{}'",
		//						keyAttributePathString, searchValue, dataModelUri);

		return Observable.from(responseFuture)
				.flatMap(response -> {
					if (response.getStatus() != 200) {
						return Observable.error(new DMPPersistenceException(
								String.format("Couldn't find GDM records in database. Received status code '%s' from database endpoint.",
										response.getStatus())));
					}

					final InputStream body = response.readEntity(InputStream.class);

					final Tuple<Observable<Resource>, InputStream> searchResultTuple = deserializeModel(body);
					final Observable<Resource> searchResult = searchResultTuple.v1();
					final InputStream is = searchResultTuple.v2();

					return searchResult.reduce(
							new org.dswarm.graph.json.Model(),
							org.dswarm.graph.json.Model::addResource)
							.doOnCompleted(DMPPersistenceError.wrapped(() -> closeResource(is, SEARCH_RESULT)));
				});
	}

	private Tuple<Observable<Resource>, InputStream> deserializeModel(final InputStream modelStream) {

		final InputStream bis = new BufferedInputStream(modelStream, CHUNK_SIZE);
		final ModelParser modelParser = new ModelParser(bis);

		return Tuple.tuple(modelParser.parse(), bis);
	}

	private Resource deserializeResource(final String modelString) throws DMPPersistenceException {

		final ObjectMapper gdmObjectMapper = Util.getJSONObjectMapper();

		final Resource resource;

		try {

			resource = gdmObjectMapper.readValue(modelString, Resource.class);
		} catch (final JsonParseException e) {

			throw new DMPPersistenceException("something went wrong, while parsing the JSON string");
		} catch (final JsonMappingException e) {

			throw new DMPPersistenceException("something went wrong, while mapping the JSON string");
		} catch (final IOException e) {

			throw new DMPPersistenceException("something went wrong, while processing the JSON string");
		}

		return resource;
	}

	private Client client() {

		return BUILDER.build();
	}

	private WebTarget gdmTarget() {

		return client().target(graphEndpoint).path(InternalGDMGraphService.GDM_RESOURCE_IDENTIFIER);
	}

	private WebTarget maintainTarget() {

		return client().target(graphEndpoint).path(InternalGDMGraphService.MAINTAIN_RESOURCE_IDENTIFIER);
	}

	private WebTarget gdmTarget(final String... path) {

		WebTarget target = gdmTarget();

		for (final String p : path) {

			target = target.path(p);
		}

		return target;
	}

	private WebTarget maintainTarget(final String... path) {

		WebTarget target = maintainTarget();

		for (final String p : path) {

			target = target.path(p);
		}

		return target;
	}

	private Observer<Resource> getBytes(final OutputStream output) throws DMPPersistenceException {

		try {

			final ModelBuilder modelBuilder = new ModelBuilder(output);

			return new Observer<org.dswarm.graph.json.Resource>() {

				//final AtomicInteger counter = new AtomicInteger(0);

				@Override public void onCompleted() {

					try {

						//LOG.debug("in model builder onCompleted with '{}' resources", counter.get());

						modelBuilder.build();
					} catch (final IOException e) {

						throw new RuntimeException(e);
					}
				}

				@Override public void onError(final Throwable e) {

					// TODO
					LOG.error("couldn't serialize GDM model", e);
				}

				@Override public void onNext(final Resource resource) {

					try {

						//						LOG.debug("add resource number '{}' with '{}' to output stream", counter.incrementAndGet(), resource.getUri());
						//
						//						if(counter.get() == 1) {
						//
						//							LOG.debug("added first resource to output stream '{}'", resource.getUri());
						//						}
						//
						//						System.out.println("resource in model builder = " + objectMapperProvider.get().writeValueAsString(resource));

						modelBuilder.addResource(resource);
						output.flush();
					} catch (final IOException e) {

						throw new RuntimeException(e);
					}
				}
			};

		} catch (final RuntimeException | IOException e) {

			throw new DMPPersistenceException("couldn't serialize model", e);
		}
	}

	private String getMetadata(final String dataModelUri, final Optional<ContentSchema> optionalContentSchema,
			final Optional<Boolean> optionalDeprecateMissingRecords, final Optional<String> optionalRecordClassUri, final boolean enableVersioning)
			throws DMPPersistenceException {

		final ObjectNode metadata = objectMapperProvider.get().createObjectNode();
		metadata.put(DMPStatics.DATA_MODEL_URI_IDENTIFIER, dataModelUri);

		if (optionalContentSchema.isPresent()) {

			final JsonNode contentSchemaJSON = generateContentSchemaJSON(optionalContentSchema.get());

			metadata.set(DMPStatics.CONTENT_SCHEMA_IDENTIFIER, contentSchemaJSON);
		}

		if (optionalDeprecateMissingRecords.isPresent() && optionalRecordClassUri.isPresent()) {

			metadata.put(DMPStatics.DEPRECATE_MISSING_RECORDS_IDENTIFIER, optionalDeprecateMissingRecords.get().toString());
			metadata.put(DMPStatics.RECORD_CLASS_URI_IDENTIFIER, optionalRecordClassUri.get());
		}

		metadata.put(DMPStatics.ENABLE_VERSIONING_IDENTIFIER, String.valueOf(enableVersioning));

		return serializeObject(metadata, METADATA_TYPE);
	}

	private String serializeObject(final ObjectNode object, final String type) throws DMPPersistenceException {

		try {

			return objectMapperProvider.get().writeValueAsString(object);
		} catch (final JsonProcessingException e) {

			final String message = String.format("couldn't serialize %s", type);

			InternalGDMGraphService.LOG.error(message, e);

			throw new DMPPersistenceException(message, e);
		}
	}

	private static void closeResource(final Closeable closeable, final String type) throws DMPPersistenceException {

		if (closeable != null) {

			try {

				closeable.close();
			} catch (final IOException e) {

				throw new DMPPersistenceException(String.format("couldn't finish %s processing", type), e);
			}
		}
	}

	private class GDMWriteRequestOperator implements Observable.Operator<GDMModel, GDMModel> {

		private final DataModel         dataModel;
		private final boolean           isSchemaAnInBuiltSchema;
		private final String            dataModelURI;
		private final Optional<Boolean> optionalDeprecateMissingRecords;
		private final boolean           enableVersioning;

		private final AsyncSubject<Response>   responseAsyncSubject   = AsyncSubject.create();
		private final PublishSubject<Resource> resourcePublishSubject = PublishSubject.create();

		private GDMWriteRequestOperator(final DataModel dataModel, final boolean isSchemaAnInBuiltSchema, final String dataModelURI,
				final Optional<Boolean> optionalDeprecateMissingRecords, final boolean enableVersioning) {
			this.dataModel = dataModel;
			this.isSchemaAnInBuiltSchema = isSchemaAnInBuiltSchema;
			this.dataModelURI = dataModelURI;
			this.optionalDeprecateMissingRecords = optionalDeprecateMissingRecords;
			this.enableVersioning = enableVersioning;
		}

		Observer<Resource> resourceObserver() {
			return resourcePublishSubject;
		}

		Observable<Response> responseObservable() {
			return responseAsyncSubject;
		}

		@Override public Subscriber<? super GDMModel> call(final Subscriber<? super GDMModel> subscriber) {

			final AtomicBoolean seenFirstModel = new AtomicBoolean();

			return new Subscriber<GDMModel>() {

				@Override public void onCompleted() {
					subscriber.onCompleted();
				}

				@Override public void onError(final Throwable e) {

					subscriber.onError(e);
				}

				@Override public void onNext(final GDMModel gdm) {

					if (seenFirstModel.compareAndSet(false, true)) {

						try {

							final org.dswarm.graph.json.Model realModel = getRealModel(gdm);
							final DataModel finalDataModel = optionallyEnhancedDataModel(dataModel, gdm, realModel, isSchemaAnInBuiltSchema);
							final Optional<ContentSchema> optionalContentSchema = Optional
									.fromNullable(finalDataModel.getSchema().getContentSchema());
							final Optional<String> optionalRecordClassUri = Optional
									.fromNullable(finalDataModel.getSchema().getRecordClass().getUri());

							final String metadata = getMetadata(dataModelURI, optionalContentSchema, optionalDeprecateMissingRecords,
									optionalRecordClassUri,
									enableVersioning);

							final Tuple<Observer<Resource>, Observable<Response>> observerObservableTuple = writeGDMToDB(dataModelURI, metadata);
							final Observer<Resource> resourceObserver = observerObservableTuple.v1();
							resourcePublishSubject.subscribe(resourceObserver);

							final Observable<Response> responseObservable = observerObservableTuple.v2();
							responseObservable.subscribe(responseAsyncSubject);
						} catch (final DMPPersistenceException e) {

							throw DMPPersistenceError.wrap(e);
						}
					}
					subscriber.onNext(gdm);
				}
			};

		}

	}
}
