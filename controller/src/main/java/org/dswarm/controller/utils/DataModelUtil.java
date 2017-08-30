/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.controller.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import javaslang.Tuple;
import javaslang.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;

import org.dswarm.common.DMPStatics;
import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.eventbus.CSVConverterEventRecorder;
import org.dswarm.controller.eventbus.JSONConverterEventRecorder;
import org.dswarm.controller.eventbus.SchemaEvent;
import org.dswarm.controller.eventbus.SchemaEventRecorder;
import org.dswarm.controller.eventbus.XMLConverterEventRecorder;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.model.resource.utils.ResourceStatics;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.utils.ClaszUtils;
import org.dswarm.persistence.service.InternalModelService;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.resource.ResourceService;

/**
 * A utility class for working with data ({@link Model}) and {@link Schema} of a {@link DataModel} (and other related parts of a
 * data model, .e.g, {@link Resource}s).
 *
 * @author phorn
 * @author tgaengler
 */
@Singleton
public class DataModelUtil {

	private static final Logger LOG = LoggerFactory.getLogger(DataModelUtil.class);
	private final ObjectMapper objectMapper;
	private final Provider<ResourceService> resourceServiceProvider;
	private final Provider<InternalModelServiceFactory> internalServiceFactoryProvider;
	private final Provider<DataModelService> dataModelServiceProvider;
	private final Provider<SchemaEventRecorder> schemaEventRecorderProvider;
	private final Provider<CSVConverterEventRecorder> csvConverterEventRecorderProvider;
	private final Provider<XMLConverterEventRecorder> xmlConvertEventRecorderProvider;
	private final Provider<JSONConverterEventRecorder> jsonConvertEventRecorderProvider;

	@Inject
	public DataModelUtil(final ObjectMapper objectMapper,
	                     final Provider<ResourceService> resourceServiceProvider,
	                     final Provider<InternalModelServiceFactory> internalServiceFactoryProvider,
	                     final Provider<DataModelService> dataModelServiceProvider,
	                     final Provider<SchemaEventRecorder> schemaEventRecorderProviderArg,
	                     final Provider<CSVConverterEventRecorder> csvConverterEventRecorderProviderArg,
	                     final Provider<XMLConverterEventRecorder> xmlConverterEventRecorderProviderArg,
	                     final Provider<JSONConverterEventRecorder> jsonConverterEventRecorderProviderArg) {

		this.objectMapper = objectMapper;
		this.resourceServiceProvider = resourceServiceProvider;
		this.internalServiceFactoryProvider = internalServiceFactoryProvider;
		this.dataModelServiceProvider = dataModelServiceProvider;
		schemaEventRecorderProvider = schemaEventRecorderProviderArg;
		csvConverterEventRecorderProvider = csvConverterEventRecorderProviderArg;
		xmlConvertEventRecorderProvider = xmlConverterEventRecorderProviderArg;
		jsonConvertEventRecorderProvider = jsonConverterEventRecorderProviderArg;
	}

	/**
	 * Gets the data of the given data model.
	 *
	 * @param dataModelUuid the identifier of the data model.
	 * @return the data of the given data model
	 */
	public Observable<Tuple2<String, JsonNode>> getDataAndMapToMappingInputFormat(final String dataModelUuid) {

		return getDataAndMapToMappingInputFormat(dataModelUuid, Optional.empty());
	}

	public Observable<Tuple2<String, JsonNode>> getDataAndMapToMappingInputFormat(final String dataModelUuid,
	                                                                              final Optional<Integer> atMost) {

		return getData(dataModelUuid, atMost)
				.map(this::transformDataNode);
	}

	public Observable<GDMModel> getDataAsGDMModel(final String dataModelUuid,
	                                              final Optional<Integer> atMost) {

		return getData(dataModelUuid, atMost)
				.cast(org.dswarm.persistence.model.internal.gdm.GDMModel.class);
	}

	/**
	 * Gets the data of the given data model and maximum in the given amount.
	 *
	 * @param dataModelUuid the identifier of the data model
	 * @param atMost        the number of records that should be retrieved
	 * @return the data of the given data model
	 */
	public Observable<Tuple2<String, Model>> getData(final String dataModelUuid,
	                                                 final Optional<Integer> atMost) {

		DataModelUtil.LOG.debug(String.format("try to get data for data model with id [%s]", dataModelUuid));

		final InternalModelService internalService = internalServiceFactoryProvider.get().getInternalGDMGraphService();

		final Observable<Tuple2<String, Model>> modelObservable;

		try {

			modelObservable = internalService.getObjects(dataModelUuid, atMost);
		} catch (final DMPPersistenceException e1) {

			DataModelUtil.LOG.debug("couldn't find data", e1);
			return Observable.empty();
		}

		return modelObservable;
	}

	public Observable<GDMModel> getRecordsDataAsGDMModel(final Set<String> recordIdentifiers,
	                                                     final String dataModelUuid) {

		return getRecordsData(recordIdentifiers, dataModelUuid)
				.cast(org.dswarm.persistence.model.internal.gdm.GDMModel.class);
	}

	public Observable<Tuple2<String, JsonNode>> getRecordsDataAndMapToMappingInputFormat(final Set<String> recordIdentifiers,
	                                                                                     final String dataModelUuid) {

		return getRecordsData(recordIdentifiers, dataModelUuid)
				.map(this::transformDataNode);
	}

	/**
	 * Gets the data of the records with the given record identifier in the given data model..
	 *
	 * @param recordIdentifiers the record identifiers
	 * @param dataModelUuid     the identifier of the data model
	 * @return the data of the given data model
	 */
	public Observable<Tuple2<String, Model>> getRecordsData(final Set<String> recordIdentifiers,
	                                                        final String dataModelUuid) {

		DataModelUtil.LOG.debug(String.format("try to get record's data for some records in data model with id [%s]", dataModelUuid));

		final InternalModelService internalService = internalServiceFactoryProvider.get().getInternalGDMGraphService();

		final Observable<Tuple2<String, Model>> maybeTriples;

		try {

			maybeTriples = internalService.getRecords(recordIdentifiers, dataModelUuid);
		} catch (final DMPPersistenceException e1) {

			DataModelUtil.LOG.debug("couldn't find record's data data in data model with id [{}]", dataModelUuid, e1);
			return Observable.empty();
		}

		return maybeTriples;
	}

	/**
	 * Gets the data of the search result of the given data model and maximum in the given amount.
	 *
	 * @param keyAttributePathString the key attribute path
	 * @param searchValue            the search value
	 * @param dataModelUuid          the identifer of the data model
	 * @param atMost                 the number of records that should be retrieved
	 * @return the data of the given data model
	 */
	public Observable<Tuple2<String, JsonNode>> searchRecords(final String keyAttributePathString,
	                                                          final String searchValue,
	                                                          final String dataModelUuid,
	                                                          final Optional<Integer> atMost) {

		DataModelUtil.LOG.debug(String.format("try to get data for data model with id [%s]", dataModelUuid));

		final InternalModelService internalService = internalServiceFactoryProvider.get().getInternalGDMGraphService();

		final Observable<Tuple2<String, Model>> maybeTriples;

		try {

			maybeTriples = internalService.searchObjects(dataModelUuid, keyAttributePathString, searchValue, atMost);
		} catch (final DMPPersistenceException e1) {

			DataModelUtil.LOG.debug("couldn't find data for key attribute path '{}' and search value '{}' in data model '{}'", keyAttributePathString,
					searchValue, dataModelUuid, e1);
			return Observable.empty();
		}

		return maybeTriples.map(this::transformDataNode);
	}

	public Observable<Response> deprecateDataModel(final String dataModelUuid) throws DMPControllerException {

		DataModelUtil.LOG.debug(String.format("try to deprecated data model with id [%s]", dataModelUuid));

		final InternalModelService internalService = internalServiceFactoryProvider.get().getInternalGDMGraphService();

		try {

			return internalService.deprecateDataModel(dataModelUuid);
		} catch (final DMPPersistenceException e1) {

			final String message = String.format("couldn't deprecate data model '%s'", dataModelUuid);

			DataModelUtil.LOG.error(message, dataModelUuid, e1);

			throw new DMPControllerException(message, e1);
		}
	}

	public Observable<Response> deprecateRecords(final Collection<String> recordURIs,
	                                             final String dataModelUuid) throws DMPControllerException {

		DataModelUtil.LOG.debug(String.format("try to deprecated '%d' records data model with id [%s]", recordURIs.size(), dataModelUuid));

		final InternalModelService internalService = internalServiceFactoryProvider.get().getInternalGDMGraphService();

		try {

			return internalService.deprecateRecords(recordURIs, dataModelUuid);
		} catch (final DMPPersistenceException e1) {

			final String message = String.format("couldn't deprecate some records in data model '%s'", dataModelUuid);

			DataModelUtil.LOG.error(message, dataModelUuid, e1);

			throw new DMPControllerException(message, e1);
		}
	}

	public Optional<ObjectNode> getSchema(final String dataModelUuid) {

		final InternalModelService internalService = internalServiceFactoryProvider.get().getInternalGDMGraphService();

		Optional<Schema> schemaOptional = null;
		try {

			schemaOptional = internalService.getSchema(dataModelUuid);
		} catch (final DMPPersistenceException e) {

			DataModelUtil.LOG.error("something went wrong while schema retrieval", e);

			return Optional.empty();
		}

		if (!schemaOptional.isPresent()) {

			DataModelUtil.LOG.debug("couldn't find schema");
			return Optional.empty();
		}

		final String schemaJSONString;
		try {

			schemaJSONString = objectMapper.writeValueAsString(schemaOptional.get());
		} catch (final JsonProcessingException e) {

			DataModelUtil.LOG.error("something went wrong while schema serialization", e);

			return Optional.empty();
		}

		final ObjectNode node;
		try {
			node = objectMapper.readValue(schemaJSONString, ObjectNode.class);
		} catch (final IOException e) {

			DataModelUtil.LOG.error("something went wrong while schema deserialization", e);

			return Optional.empty();
		}

		return Optional.of(node);
	}

	/**
	 * Gets the resource for the given resource identifier.
	 *
	 * @param resourceUuid a resource identifier
	 * @return (optional) the matched resource
	 */
	public Optional<Resource> fetchResource(final String resourceUuid) {

		final ResourceService resourceService = resourceServiceProvider.get();
		final Resource resource = resourceService.getObject(resourceUuid);

		return Optional.ofNullable(resource);
	}

	/**
	 * Gets the data model for the given data model identifier.
	 *
	 * @param dataModelUuid a data model identifier
	 * @return (optional) the matched data model
	 */
	public Optional<DataModel> fetchDataModel(final String dataModelUuid) {

		final DataModelService dataModelService = dataModelServiceProvider.get();
		final DataModel dataModel = dataModelService.getObject(dataModelUuid);

		return Optional.ofNullable(dataModel);
	}

	/**
	 * Gets the configuration for the given resource identifier and configuration identifier
	 *
	 * @param resourceUuid      a resource identifier
	 * @param configurationUuid a configuration identifier
	 * @return (optional) the matched configuration
	 */
	public Optional<Configuration> fetchConfiguration(final String resourceUuid,
	                                                  final String configurationUuid) {

		final Optional<Resource> resourceOptional = fetchResource(resourceUuid);

		if (!resourceOptional.isPresent()) {

			DataModelUtil.LOG.debug("couldn't find  resource '" + resourceUuid);
			return Optional.empty();
		}

		final Configuration configuration = resourceOptional.get().getConfiguration(configurationUuid);

		return Optional.ofNullable(configuration);
	}

	/**
	 * Gets the related configuration for the given data model identifier.
	 *
	 * @param dataModelUuid a data model identifier
	 * @return (optional) the matched configuration
	 */
	public Optional<Configuration> fetchConfiguration(final String dataModelUuid) {

		final Optional<DataModel> dataModelOptional = fetchDataModel(dataModelUuid);

		if (!dataModelOptional.isPresent()) {

			DataModelUtil.LOG.debug("couldn't find data model '" + dataModelUuid + "'");
			return Optional.empty();
		}

		final Configuration configuration = dataModelOptional.get().getConfiguration();

		return Optional.ofNullable(configuration);
	}

	/**
	 * Deletes the resource for the given resource identifier.
	 *
	 * @param resourceUuid a resource identifier
	 */
	public void deleteResource(final String resourceUuid) {

		final ResourceService resourceService = resourceServiceProvider.get();
		resourceService.deleteObject(resourceUuid);

	}

	public Observable<Tuple2<String, JsonNode>> doIngestAndMapToMappingInputFormat(final DataModel dataModel,
	                                                                               final boolean utiliseExistingInputSchema,
	                                                                               final Scheduler scheduler) throws DMPControllerException {

		return mapToMappingInput(doIngest(dataModel, utiliseExistingInputSchema, scheduler));
	}

	public Observable<GDMModel> doIngest(final DataModel dataModel,
	                                     final boolean utiliseExistingInputSchema,
	                                     final Scheduler scheduler) throws DMPControllerException {

		DataModelUtil.LOG.debug("try to process data for data model with id '{}'", dataModel.getUuid());

		final Configuration configuration = dataModel.getConfiguration();

		if (configuration == null) {

			DataModelUtil.LOG
					.debug("The data model '{}' has no configuration. Hence, the data of the data model cannot be processed.", dataModel.getUuid());

			return Observable.empty();
		}

		final JsonNode jsStorageType = configuration.getParameters().get(ConfigurationStatics.STORAGE_TYPE);

		if (jsStorageType == null) {

			DataModelUtil.LOG
					.debug("The configuration of the data model '{}' has no storage type. Hence, the data of the data model cannot be processed.",
							dataModel.getUuid());

			return Observable.empty();
		}

		final String storageType = jsStorageType.asText();

		if (!utiliseExistingInputSchema) {

			try {

				final SchemaEvent.SchemaType type = SchemaEvent.SchemaType.fromString(storageType);
				final SchemaEvent schemaEvent = new SchemaEvent(dataModel, type, null, false);
				schemaEventRecorderProvider.get().convertSchema(schemaEvent);
			} catch (final IllegalArgumentException e) {

				DataModelUtil.LOG.warn("could not determine schema type", e);
			}
		}

		final Observable<Model> modelObservable;

		switch (storageType) {

			case ConfigurationStatics.CSV_STORAGE_TYPE:

				modelObservable = csvConverterEventRecorderProvider.get().doIngest(dataModel, utiliseExistingInputSchema, scheduler);

				break;
			case ConfigurationStatics.XML_STORAGE_TYPE:
			case ConfigurationStatics.MABXML_STORAGE_TYPE:
			case ConfigurationStatics.MARCXML_STORAGE_TYPE:
			case ConfigurationStatics.PICAPLUSXML_STORAGE_TYPE:
			case ConfigurationStatics.PICAPLUSXML_GLOBAL_STORAGE_TYPE:
			case ConfigurationStatics.PNX_STORAGE_TYPE:
			case ConfigurationStatics.OAI_PMH_DC_ELEMENTS_STORAGE_TYPE:
			case ConfigurationStatics.OAI_PMH_DCE_AND_EDM_ELEMENTS_STORAGE_TYPE:
			case ConfigurationStatics.OAIPMH_DC_TERMS_STORAGE_TYPE:
			case ConfigurationStatics.OAIPMH_MARCXML_STORAGE_TYPE:
			case ConfigurationStatics.SRU_11_PICAPLUSXML_GLOBAL_STORAGE_TYPE:
			case ConfigurationStatics.SPRINGER_JOURNALS_STORAGE_TYPE:

				modelObservable = xmlConvertEventRecorderProvider.get().doIngest(dataModel, utiliseExistingInputSchema, scheduler);

				break;
			case ConfigurationStatics.JSON_STORAGE_TYPE:

				modelObservable = jsonConvertEventRecorderProvider.get().doIngest(dataModel, utiliseExistingInputSchema, scheduler);

				break;
			default:

				DataModelUtil.LOG
						.error("couldn't match storage type of configuration of data model '{}'. Hence, the data of the data model cannot be processed.",
								dataModel.getUuid());

				modelObservable = Observable.empty();
		}

		return modelObservable.cast(org.dswarm.persistence.model.internal.gdm.GDMModel.class);
	}

	public Observable<Tuple2<String, JsonNode>> mapToMappingInput(final Observable<GDMModel> modelObservable) {

		return modelObservable.map(gdm -> Tuple.of(gdm.getRecordURIs().iterator().next(), (Model) gdm))
				.map(this::transformDataNode);
	}

	public static Optional<String> determineOriginalDataModelType(final DataModel dataModel,
	                                                              final Optional<Configuration> optionalConfiguration) {

		// original data model type
		final Optional<String> optionalOriginalDataModelType;

		switch (dataModel.getUuid()) {

			case DataModelUtils.MABXML_DATA_MODEL_UUID:
			case DataModelUtils.MARCXML_DATA_MODEL_UUID:
			case DataModelUtils.PICAPLUSXML_DATA_MODEL_UUID:
			case DataModelUtils.PICAPLUSXML_GLOBAL_DATA_MODEL_UUID:
			case DataModelUtils.PNX_DATA_MODEL_UUID:
			case DataModelUtils.OAI_PMH_DC_ELEMENTS_DATA_MODEL_UUID:
			case DataModelUtils.OAI_PMH_DC_ELEMENTS_AND_EDM_DATA_MODEL_UUID:
			case DataModelUtils.OAI_PMH_DC_TERMS_DATA_MODEL_UUID:
			case DataModelUtils.OAI_PMH_MARCXML_DATA_MODEL_UUID:
			case DataModelUtils.SRU_11_PICAPLUSXML_GLOBAL_DATA_MODEL_UUID:
			case DataModelUtils.SPRINGER_JOURNALS_DATA_MODEL_UUID:

				optionalOriginalDataModelType = Optional.of(DMPStatics.XML_DATA_TYPE);

				break;
			default:

				if (optionalConfiguration.isPresent()) {

					final Configuration configuration = optionalConfiguration.get();

					final Optional<JsonNode> optionalStorageTypeNode = Optional
							.ofNullable(configuration.getParameter(ConfigurationStatics.STORAGE_TYPE));

					if (optionalStorageTypeNode.isPresent()) {

						final JsonNode storageTypeNode = optionalStorageTypeNode.get();

						final Optional<String> optionalStorageType = Optional.ofNullable(storageTypeNode.asText());

						if (optionalStorageType.isPresent()) {

							final String storageType = optionalStorageType.get();

							switch (storageType) {

								case ConfigurationStatics.XML_STORAGE_TYPE:
								case ConfigurationStatics.PNX_STORAGE_TYPE:
								case ConfigurationStatics.MABXML_STORAGE_TYPE:
								case ConfigurationStatics.MARCXML_STORAGE_TYPE:
								case ConfigurationStatics.PICAPLUSXML_STORAGE_TYPE:
								case ConfigurationStatics.PICAPLUSXML_GLOBAL_STORAGE_TYPE:
								case ConfigurationStatics.OAI_PMH_DC_ELEMENTS_STORAGE_TYPE:
								case ConfigurationStatics.OAI_PMH_DCE_AND_EDM_ELEMENTS_STORAGE_TYPE:
								case ConfigurationStatics.OAIPMH_DC_TERMS_STORAGE_TYPE:
								case ConfigurationStatics.OAIPMH_MARCXML_STORAGE_TYPE:
								case ConfigurationStatics.SRU_11_PICAPLUSXML_GLOBAL_STORAGE_TYPE:
								case ConfigurationStatics.SPRINGER_JOURNALS_STORAGE_TYPE:

									optionalOriginalDataModelType = Optional.of(DMPStatics.XML_DATA_TYPE);

									break;
								default:

									optionalOriginalDataModelType = Optional.empty();
							}
						} else {

							optionalOriginalDataModelType = Optional.empty();
						}
					} else {

						optionalOriginalDataModelType = Optional.empty();
					}
				} else {

					optionalOriginalDataModelType = Optional.empty();
				}
		}

		return optionalOriginalDataModelType;
	}

	public static Optional<String> determineRecordTag(final Optional<Configuration> optionalConfiguration) {

		if (!optionalConfiguration.isPresent()) {

			return Optional.empty();
		}

		final Optional<JsonNode> optionalRecordTagNode = Optional.ofNullable(
				optionalConfiguration.get().getParameter(ConfigurationStatics.RECORD_TAG));

		if (!optionalRecordTagNode.isPresent()) {

			return Optional.empty();
		}

		return Optional.of(optionalRecordTagNode.get().asText());
	}

	public static Optional<String> determineRecordClassURI(final DataModel dataModel) {

		final Optional<Schema> optionalSchema = Optional.ofNullable(dataModel.getSchema());

		if (!optionalSchema.isPresent()) {

			Optional.empty();
		}

		final Optional<Clasz> optionalRecordClass = Optional.ofNullable(optionalSchema.get().getRecordClass());

		final String recordClassURI;

		if (optionalRecordClass.isPresent()) {

			recordClassURI = optionalRecordClass.get().getUri();
		} else {

			// fallback: bibo:Document as default record class
			recordClassURI = ClaszUtils.BIBO_DOCUMENT_URI;
		}

		return Optional.of(recordClassURI);
	}

	public static Resource checkDataResource(final DataModel dataModel) throws DMPControllerException {

		final Resource dataResource = dataModel.getDataResource();
		final JsonNode resourcePathJSONNode = dataResource.getAttribute(ResourceStatics.PATH);

		if (resourcePathJSONNode == null) {

			final String message = String
					.format("The data resource '%s' of data model '%s' contains not path attribute. Hence, the data of the data model cannot be processed.",
							dataResource.getUuid(), dataModel.getUuid());

			DataModelUtil.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final String resourcePathString = resourcePathJSONNode.asText();
		final java.nio.file.Path resourcePath = Paths.get(resourcePathString);
		final boolean exists = Files.exists(resourcePath);

		if (!exists) {

			final String message = String
					.format("The data resource '%s' at path '%s' of data model '%s' does not exist. Hence, the data of the data model cannot be processed.",
							dataResource.getUuid(), resourcePathString, dataModel.getUuid());

			DataModelUtil.LOG.error(message);

			throw new DMPControllerException(message);
		}
		return dataResource;
	}

	private Tuple2<String, JsonNode> transformDataNode(final Tuple2<String, Model> input) {

		final String recordId = input._1;
		final JsonNode jsonNode = input._2.toRawJSON();

		return Tuple.of(recordId, jsonNode);
	}
}
