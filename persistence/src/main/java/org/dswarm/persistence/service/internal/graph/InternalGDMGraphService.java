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
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func1;

import org.dswarm.common.DMPStatics;
import org.dswarm.common.model.util.AttributePathUtil;
import org.dswarm.common.types.Tuple;
import org.dswarm.graph.json.Resource;
import org.dswarm.graph.json.stream.ModelBuilder;
import org.dswarm.graph.json.stream.ModelParser;
import org.dswarm.graph.json.util.Util;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.internal.helper.AttributePathHelper;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
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
 * A internal model service implementation for RDF triples.<br/>
 * Currently, the Neo4j database is utilised.
 *
 * @author tgaengler
 */
@Singleton
public class InternalGDMGraphService implements InternalModelService {

	private static final Logger LOG = LoggerFactory.getLogger(InternalGDMGraphService.class);

	private static final String resourceIdentifier = "gdm";
	private static final String MULTIPART_MIXED    = "multipart/mixed";

	private static final String SEARCH_RESULT    = "search result";
	private static final String OBJECT_RETRIEVAL = "object retrieval";
	private static final String OBJECT_ADDITION  = "object addition";

	private static final ClientBuilder builder = ClientBuilder.newBuilder();

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
	public void createObject(final String dataModelUuid, final Object model) throws DMPPersistenceException {

		if (dataModelUuid == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		if (model == null) {

			throw new DMPPersistenceException("model that should be added to DB shouldn't be null");
		}

		if (!GDMModel.class.isInstance(model)) {

			throw new DMPPersistenceException("this service can only process GDM models");
		}

		final GDMModel gdmModel = (GDMModel) model;

		final org.dswarm.graph.json.Model realModel = gdmModel.getModel();

		if (realModel == null) {

			throw new DMPPersistenceException("real model that should be added to DB shouldn't be null");
		}

		final String dataModelURI = GDMUtil.getDataModelGraphURI(dataModelUuid);

		final DataModel dataModel = addRecordClass(dataModelUuid, gdmModel.getRecordClassURI());

		final DataModel finalDataModel;

		if (dataModel != null) {

			finalDataModel = dataModel;
		} else {

			finalDataModel = getDataModel(dataModelUuid);
		}

		if (finalDataModel == null) {
			throw new DMPPersistenceException("Could not get the actual data model to use");
		}

		if (finalDataModel.getSchema() != null) {

			if (finalDataModel.getSchema().getRecordClass() != null) {

				final String recordClassURI = finalDataModel.getSchema().getRecordClass().getUri();

				final Set<Resource> recordResources = GDMUtil.getRecordResources(recordClassURI, realModel);

				if (recordResources != null && !recordResources.isEmpty()) {

					final LinkedHashSet<String> recordURIs = Sets.newLinkedHashSet();

					recordURIs.addAll(recordResources.stream().map(Resource::getUri).collect(Collectors.toList()));

					gdmModel.setRecordURIs(recordURIs);
				}
			}
		}

		addAttributePaths(finalDataModel, gdmModel.getAttributePaths());

		final Optional<ContentSchema> optionalContentSchema = Optional.fromNullable(finalDataModel.getSchema().getContentSchema());

		// TODO: true for now
		final Optional<Boolean> optionalDeprecateMissingRecords = Optional.of(Boolean.TRUE);
		final Optional<String> optionalRecordClassUri = Optional.fromNullable(finalDataModel.getSchema().getRecordClass().getUri());

		writeGDMToDB(realModel, dataModelURI, optionalContentSchema, optionalDeprecateMissingRecords, optionalRecordClassUri);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<Map<String, Model>> getObjects(final String dataModelUuid, final Optional<Integer> optionalAtMost)
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

		final Map<String, Model> modelMap = Maps.newLinkedHashMap();

		final Observable<Void> modelObservable = recordResourcesObservable.map(new Func1<Resource, Void>() {

			@Override public Void call(final Resource recordResource) {

				final org.dswarm.graph.json.Model recordModel = new org.dswarm.graph.json.Model();
				recordModel.addResource(recordResource);

				final Model gdmModel = new GDMModel(recordModel, recordResource.getUri());

				modelMap.put(recordResource.getUri(), gdmModel);

				return null;
			}
		});

		try {

			// TODO: move this, if needed
			final Iterator<Void> iterator = modelObservable.toBlocking().getIterator();

			if (!iterator.hasNext()) {

				InternalGDMGraphService.LOG.debug("model is empty for data model '{}' in database", dataModelUuid);

				return Optional.absent();
			}

			while (iterator.hasNext()) {

				iterator.next();
			}
		} catch (final RuntimeException e) {

			throw new DMPPersistenceException(e.getMessage(), e.getCause());
		}

		closeInputStream(inputStream, OBJECT_RETRIEVAL);

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

		return Optional.of(modelMap);
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

			throw new DMPPersistenceException("couldn't find data model '" + dataModelUuid + "' to retrieve it's schema");
		}

		final Schema schema = dataModel.getSchema();

		if (schema == null) {

			InternalGDMGraphService.LOG.debug("couldn't find schema in data model '" + dataModelUuid + "'");

			return Optional.absent();
		}

		return Optional.of(schema);
	}

	@Override public Optional<Map<String, Model>> searchObjects(final String dataModelUuid, final String keyAttributePathString,
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

		final org.dswarm.graph.json.Model model = searchGDMRecordsInDB(keyAttributePathString, searchValue, dataModelURI, optionalAtMost);

		if (model == null) {

			InternalGDMGraphService.LOG
					.debug("couldn't find records for key attribute path '{}' and search value '{}  in data model '{}' in database",
							keyAttributePathString, searchValue, dataModelUuid);

			return Optional.absent();
		}

		if (model.size() <= 0) {

			InternalGDMGraphService.LOG
					.debug("model is empty for key attribute path '{}' and search value '{}  in data model '{}' in database", keyAttributePathString,
							searchValue, dataModelUuid);

			return Optional.absent();
		}

		final Set<Resource> recordResources = GDMUtil.getRecordResources(recordClassUri, model);

		if (recordResources == null || recordResources.isEmpty()) {

			InternalGDMGraphService.LOG
					.debug("couldn't find records for record class '{}' in search result of key attribute path = '{}' and search value = '{}' in data model '{}'",
							recordClassUri, keyAttributePathString, searchValue, dataModelUuid);

			throw new DMPPersistenceException(String.format(
					"couldn't find records for record class '%s' in search result of key attribute path = '%s' and search value = '%s' in data model '%s'",
					recordClassUri, keyAttributePathString, searchValue, dataModelUuid));
		}

		final Map<String, Model> modelMap = Maps.newLinkedHashMap();

		for (final Resource recordResource : recordResources) {

			final org.dswarm.graph.json.Model recordModel = new org.dswarm.graph.json.Model();
			recordModel.addResource(recordResource);

			final Model gdmModel = new GDMModel(recordModel, recordResource.getUri());

			modelMap.put(recordResource.getUri(), gdmModel);
		}

		return Optional.of(modelMap);
	}

	@Override public Optional<Model> getRecord(final String recordIdentifier, final String dataModelUuid) throws DMPPersistenceException {

		if (recordIdentifier == null) {

			throw new DMPPersistenceException("record identifier shouldn't be null");
		}

		if (dataModelUuid == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final String dataModelURI = GDMUtil.getDataModelGraphURI(dataModelUuid);

		final org.dswarm.graph.json.Resource resource = readGDMRecordFromDB(recordIdentifier, dataModelURI);

		if (resource == null) {

			InternalGDMGraphService.LOG
					.debug("couldn't find record data for record identifier '{}' in data model '{}' in database", recordIdentifier, dataModelUuid);

			return Optional.absent();
		}

		if (resource.size() <= 0) {

			InternalGDMGraphService.LOG.debug("resource is empty for record identifier '{}' in data model '{}' in database", recordIdentifier,
					dataModelUuid);

			return Optional.absent();
		}

		final org.dswarm.graph.json.Model model = new org.dswarm.graph.json.Model();
		model.addResource(resource);

		final Model gdmModel = new GDMModel(model, recordIdentifier);

		return Optional.of(gdmModel);
	}

	@Override public Optional<Map<String, Model>> getRecords(final Set<String> recordIdentifiers, final String dataModelUuid)
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

		final Map<String, Model> modelMap = Maps.newLinkedHashMap();

		for (final String recordIdentifier : recordIdentifiers) {

			final org.dswarm.graph.json.Resource resource = readGDMRecordFromDB(recordIdentifier, dataModelURI);

			if (resource == null) {

				InternalGDMGraphService.LOG
						.debug("couldn't find record data for record identifier '{}' in data model '{}' in database", recordIdentifier,
								dataModelUuid);

				continue;
			}

			if (resource.size() <= 0) {

				InternalGDMGraphService.LOG
						.debug("resource is empty for record identifier '{}' in data model '{}' in database", recordIdentifier, dataModelUuid);

				continue;
			}

			final org.dswarm.graph.json.Model model = new org.dswarm.graph.json.Model();
			model.addResource(resource);

			final Model gdmModel = new GDMModel(model, recordIdentifier);

			modelMap.put(recordIdentifier, gdmModel);
		}

		if (modelMap.isEmpty()) {

			InternalGDMGraphService.LOG.debug("couldn't find any record in data model '{}' in database", dataModelUuid);

			return Optional.absent();
		}

		return Optional.of(modelMap);
	}

	/**
	 * Adds the record class to the schema of the data model.
	 *
	 * @param dataModelUuid  the identifier of the data model
	 * @param recordClassUri the identifier of the record class
	 * @throws DMPPersistenceException
	 */
	private DataModel addRecordClass(final String dataModelUuid, final String recordClassUri) throws DMPPersistenceException {

		// (try) add record class uri to schema
		final DataModel dataModel = getSchemaInternal(dataModelUuid);
		final Schema schema = dataModel.getSchema();

		final boolean result = SchemaUtils.addRecordClass(schema, recordClassUri, classService);

		if (!result) {

			return dataModel;
		}

		return updateDataModel(dataModel);
	}

	private DataModel addAttributePaths(final DataModel dataModel, final Set<AttributePathHelper> attributePathHelpers)
			throws DMPPersistenceException {

		final Schema schema = dataModel.getSchema();
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

					return dataModel;
			}
		}

		final boolean result = SchemaUtils.addAttributePaths(schema, attributePathHelpers,
				attributePathService, schemaAttributePathInstanceService, attributeService);

		if (!result) {

			return dataModel;
		}

		return updateDataModel(dataModel);
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

		if (dataModel.getSchema() == null) {

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

			dataModel.setSchema(schema);
		}

		return updateDataModel(dataModel);
	}

	private DataModel getDataModel(final String dataModelUuid) {

		final DataModel dataModel = dataModelService.get().getObject(dataModelUuid);

		if (dataModel == null) {

			InternalGDMGraphService.LOG.debug("couldn't find data model '{}'", dataModelUuid);

			return null;
		}

		return dataModel;
	}

	private void writeGDMToDB(final org.dswarm.graph.json.Model model, final String dataModelUri, final Optional<ContentSchema> optionalContentSchema,
			final Optional<Boolean> optionalDeprecateMissingRecords, final Optional<String> optionalRecordClassUri) throws DMPPersistenceException {

		final WebTarget target = target("/put");

		final ByteArrayOutputStream content = getBytes(model);

		if (content == null) {

			return;
		}

		final String metadata = getMetadata(dataModelUri, optionalContentSchema, optionalDeprecateMissingRecords, optionalRecordClassUri);

		try {

			// convert pipe output stream into input stream
			final PipedInputStream in = new PipedInputStream();
			final PipedOutputStream out = new PipedOutputStream(in);
			final Thread writeThread = new Thread(
					() -> {
						try {
							// write the original OutputStream to the PipedOutputStream
							content.writeTo(out);

							// Construct a MultiPart with two body parts
							final MultiPart multiPart = new MultiPart();
							multiPart.bodyPart(in, MediaType.APPLICATION_OCTET_STREAM_TYPE)
									.bodyPart(metadata, MediaType.APPLICATION_JSON_TYPE);

							// POST the request
							final Response response = target.request(MULTIPART_MIXED).post(Entity.entity(multiPart, MULTIPART_MIXED));

							multiPart.close();
							in.close();
							out.close();
							content.close();

							if (response.getStatus() != 200) {

								throw new DMPPersistenceException(
										String.format("Couldn't store GDM data into database. Received status code '%s' from database endpoint.",
												response.getStatus()));
							}
						} catch (IOException e) {

							// logging and exception handling should go here
							LOG.error("couldn't store GDM data into database. something with i/o went wrong", e);
						} catch(DMPPersistenceException e) {

							LOG.error(e.getMessage(), e);
						}
					}
			);
			writeThread.start();

		} catch (IOException e) {

			final String message = "couldn't store GDM data into database. something with i/o went wrong";

			LOG.error(message, e);

			throw new DMPPersistenceException(message, e);
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

		final WebTarget target = target("/get");

		final ObjectMapper objectMapper = DMPPersistenceUtil.getJSONObjectMapper();
		final ObjectNode requestJson = objectMapper.createObjectNode();

		requestJson.put(DMPStatics.RECORD_CLASS_URI_IDENTIFIER, recordClassUri);
		requestJson.put(DMPStatics.DATA_MODEL_URI_IDENTIFIER, dataModelUri);

		if (optionalAtMost.isPresent()) {

			requestJson.put(DMPStatics.AT_MOST_IDENTIFIER, optionalAtMost.get());
		}

		String requestJsonString;

		try {

			requestJsonString = objectMapper.writeValueAsString(requestJson);
		} catch (final JsonProcessingException e) {

			throw new DMPPersistenceException("something went wrong, while creating the request JSON string for the read-gdm-from-db request", e);
		}

		// POST the request
		final Response response = target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(requestJsonString, MediaType.APPLICATION_JSON));

		if (response.getStatus() != 200) {

			throw new DMPPersistenceException(
					String.format("Couldn't read GDM data from database. Received status code '%s' from database endpoint.", response.getStatus()));
		}

		final InputStream body = response.readEntity(InputStream.class);

		return deserializeModel(body);
	}

	private org.dswarm.graph.json.Resource readGDMRecordFromDB(final String recordUri, final String dataModelUri)
			throws DMPPersistenceException {

		final WebTarget target = target("/getrecord");

		final ObjectMapper objectMapper = DMPPersistenceUtil.getJSONObjectMapper();
		final ObjectNode requestJson = objectMapper.createObjectNode();

		requestJson.put(DMPStatics.RECORD_URI_IDENTIFIER, recordUri);
		requestJson.put(DMPStatics.DATA_MODEL_URI_IDENTIFIER, dataModelUri);

		String requestJsonString;

		try {

			requestJsonString = objectMapper.writeValueAsString(requestJson);
		} catch (final JsonProcessingException e) {

			throw new DMPPersistenceException("something went wrong, while creating the request JSON string for the read-gdm-record-from-db request",
					e);
		}

		// POST the request
		final Response response = target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(requestJsonString, MediaType.APPLICATION_JSON));

		if (response.getStatus() != 200) {

			throw new DMPPersistenceException(
					String.format("Couldn't read GDM record data from database. Received status code '%s' from database endpoint.",
							response.getStatus()));
		}

		final String body = response.readEntity(String.class);

		return deserializeResource(body);
	}

	private org.dswarm.graph.json.Model searchGDMRecordsInDB(final String keyAttributePathString, final String searchValue, final String dataModelUri,
			final Optional<Integer> optionalAtMost)
			throws DMPPersistenceException {

		final WebTarget target = target("/searchrecords");

		final ObjectMapper objectMapper = DMPPersistenceUtil.getJSONObjectMapper();
		final ObjectNode requestJson = objectMapper.createObjectNode();

		requestJson.put(DMPStatics.KEY_ATTRIBUTE_PATH_IDENTIFIER, keyAttributePathString);
		requestJson.put(DMPStatics.SEARCH_VALUE_IDENTIFIER, searchValue);
		requestJson.put(DMPStatics.DATA_MODEL_URI_IDENTIFIER, dataModelUri);

		if (optionalAtMost.isPresent()) {

			requestJson.put(DMPStatics.AT_MOST_IDENTIFIER, optionalAtMost.get());
		}

		String requestJsonString;

		try {

			requestJsonString = objectMapper.writeValueAsString(requestJson);
		} catch (final JsonProcessingException e) {

			throw new DMPPersistenceException("something went wrong, while creating the request JSON string for the search-gdm-records-in-db request",
					e);
		}

		// POST the request
		final Response response = target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(requestJsonString, MediaType.APPLICATION_JSON));

		if (response.getStatus() != 200) {

			throw new DMPPersistenceException(
					String.format("Couldn't find GDM records in database. Received status code '%s' from database endpoint.", response.getStatus()));
		}

		final InputStream body = response.readEntity(InputStream.class);

		final org.dswarm.graph.json.Model model = new org.dswarm.graph.json.Model();

		final Tuple<Observable<Resource>, InputStream> searchResultTuple = deserializeModel(body);
		final Observable<Resource> searchResult = searchResultTuple.v1();
		final InputStream is = searchResultTuple.v2();

		final Observable<Void> resultObservable = searchResult.map(new Func1<Resource, Void>() {

			@Override public Void call(final Resource resource) {

				model.addResource(resource);

				return null;
			}
		});

		try {

			// TODO: move this, if needed
			final Iterator<Void> iterator = resultObservable.toBlocking().getIterator();

			if (!iterator.hasNext()) {

				InternalGDMGraphService.LOG
						.debug("couldn't find results for key attribute path '{}' and search value '}' in data model '{}'", keyAttributePathString,
								searchValue, dataModelUri);

				closeInputStream(is, SEARCH_RESULT);

				return null;
			}

			while (iterator.hasNext()) {

				iterator.next();
			}
		} catch (final RuntimeException e) {

			throw new DMPPersistenceException(e.getMessage(), e.getCause());
		}

		closeInputStream(is, SEARCH_RESULT);

		return model;
	}

	private Tuple<Observable<Resource>, InputStream> deserializeModel(final InputStream modelStream) {

		final InputStream bis = new BufferedInputStream(modelStream, 1024);
		final ModelParser modelParser = new ModelParser(bis);

		return Tuple.tuple(modelParser.parse(), bis);
	}

	private org.dswarm.graph.json.Resource deserializeResource(final String modelString) throws DMPPersistenceException {

		final ObjectMapper gdmObjectMapper = Util.getJSONObjectMapper();

		final org.dswarm.graph.json.Resource resource;

		try {

			resource = gdmObjectMapper.readValue(modelString, org.dswarm.graph.json.Resource.class);
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

		return builder.register(MultiPartFeature.class).property(ClientProperties.CHUNKED_ENCODING_SIZE, 1024).build();
	}

	private WebTarget target() {

		return client().target(graphEndpoint).path(InternalGDMGraphService.resourceIdentifier);
	}

	private WebTarget target(final String... path) {

		WebTarget target = target();

		for (final String p : path) {

			target = target.path(p);
		}

		return target;
	}

	private ByteArrayOutputStream getBytes(final org.dswarm.graph.json.Model model) throws DMPPersistenceException {

		if (model == null || model.getResources() == null) {

			LOG.debug("model or resources are not available");

			return null;
		}

		try {

			final ByteArrayOutputStream modelStream = new ByteArrayOutputStream(1024);
			final ModelBuilder modelBuilder = new ModelBuilder(modelStream);

			for (final Resource resource : model.getResources()) {

				modelBuilder.addResource(resource);
			}

			modelBuilder.build();

			return modelStream;
		} catch (final IOException e) {

			throw new DMPPersistenceException("couldn't serialize model", e);
		}
	}

	private String getMetadata(final String dataModelUri, final Optional<ContentSchema> optionalContentSchema,
			final Optional<Boolean> optionalDeprecateMissingRecords, final Optional<String> optionalRecordClassUri) throws DMPPersistenceException {

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

		try {
			return objectMapperProvider.get().writeValueAsString(metadata);
		} catch (JsonProcessingException e) {

			final String message = "couldn't serialize metadata";

			InternalGDMGraphService.LOG.error(message, e);

			throw new DMPPersistenceException(message, e);
		}
	}

	private void closeInputStream(final InputStream inputStream, final String type) throws DMPPersistenceException {

		if (inputStream != null) {

			try {

				inputStream.close();
			} catch (final IOException e) {

				throw new DMPPersistenceException(String.format("couldn't finish %s processing", type), e);
			}
		}
	}

	private void closeOutputStream(final OutputStream outputStream, final String type) throws DMPPersistenceException {

		if (outputStream != null) {

			try {

				outputStream.close();
			} catch (final IOException e) {

				throw new DMPPersistenceException(String.format("couldn't finish %s processing", type), e);
			}
		}
	}

	private void closeMultiPart(final MultiPart multiPart, final String type) throws DMPPersistenceException {

		if (multiPart != null) {

			try {

				multiPart.close();
			} catch (final IOException e) {

				throw new DMPPersistenceException(String.format("couldn't finish %s processing", type), e);
			}
		}
	}
}
