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
package org.dswarm.controller.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.types.Tuple;
import org.dswarm.persistence.service.InternalModelService;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.resource.ResourceService;
import org.dswarm.persistence.service.schema.SchemaService;

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
	private final ObjectMapper                          objectMapper;
	private final Provider<ResourceService>             resourceServiceProvider;
	private final Provider<InternalModelServiceFactory> internalServiceFactoryProvider;
	private final Provider<SchemaService>               schemaServiceProvider;
	private final Provider<DataModelService>            dataModelServiceProvider;

	@Inject
	public DataModelUtil(final ObjectMapper objectMapper, final Provider<ResourceService> resourceServiceProvider,
			final Provider<InternalModelServiceFactory> internalServiceFactoryProvider, final Provider<SchemaService> schemaServiceProvider,
			final Provider<DataModelService> dataModelServiceProvider) {
		this.objectMapper = objectMapper;
		this.resourceServiceProvider = resourceServiceProvider;
		this.internalServiceFactoryProvider = internalServiceFactoryProvider;
		this.schemaServiceProvider = schemaServiceProvider;
		this.dataModelServiceProvider = dataModelServiceProvider;
	}

	/**
	 * Gets the data of the given data model.
	 *
	 * @param dataModelUuid the identifier of the data model.
	 * @return the data of the given data model
	 */
	public Optional<Iterator<Tuple<String, JsonNode>>> getData(final String dataModelUuid) {
		return getData(dataModelUuid, Optional.<Integer>absent());
	}

	/**
	 * Gets the data of the given data model and maximum in the given amount.
	 *
	 * @param dataModelUuid the identifer of the data model
	 * @param atMost        the number of records that should be retrieved
	 * @return the data of the given data model
	 */
	public Optional<Iterator<Tuple<String, JsonNode>>> getData(final String dataModelUuid, final Optional<Integer> atMost) {

		DataModelUtil.LOG.debug(String.format("try to get data for data model with id [%s]", dataModelUuid));

		final Optional<Configuration> configurationOptional = fetchConfiguration(dataModelUuid);

		if (!configurationOptional.isPresent()) {

			return Optional.absent();
		}

		final InternalModelService internalService;
		try {
			internalService = determineInternalService(configurationOptional.get());
		} catch (final DMPControllerException e) {
			return Optional.absent();
		}

		final Optional<Map<String, Model>> maybeTriples;

		try {

			maybeTriples = internalService.getObjects(dataModelUuid, atMost);
		} catch (final DMPPersistenceException e1) {

			DataModelUtil.LOG.debug("couldn't find data", e1);
			return Optional.absent();
		}

		if (!maybeTriples.isPresent()) {

			DataModelUtil.LOG.debug("couldn't find data");
			return Optional.absent();
		}

		final Iterator<Map.Entry<String, Model>> iterator = maybeTriples.get().entrySet().iterator();

		return Optional.of(dataIterator(iterator));
	}

	public Optional<ObjectNode> getSchema(final String dataModelUuid) {

		final Optional<Configuration> configurationOptional = fetchConfiguration(dataModelUuid);

		if (!configurationOptional.isPresent()) {

			return Optional.absent();
		}

		final InternalModelService internalService;
		try {
			internalService = determineInternalService(configurationOptional.get());
		} catch (final DMPControllerException e) {

			return Optional.absent();
		}

		Optional<Schema> schemaOptional = null;
		try {

			schemaOptional = internalService.getSchema(dataModelUuid);
		} catch (final DMPPersistenceException e) {

			DataModelUtil.LOG.error("something went wrong while schema retrieval", e);

			return Optional.absent();
		}

		if (!schemaOptional.isPresent()) {

			DataModelUtil.LOG.debug("couldn't find schema");
			return Optional.absent();
		}

		String schemaJSONString = null;
		try {

			schemaJSONString = objectMapper.writeValueAsString(schemaOptional.get());
		} catch (final JsonProcessingException e) {

			DataModelUtil.LOG.error("something went wrong while schema serialization", e);

			return Optional.absent();
		}

		ObjectNode node;
		try {
			node = objectMapper.readValue(schemaJSONString, ObjectNode.class);
		} catch (final IOException e) {

			DataModelUtil.LOG.error("something went wrong while schema deserialization", e);

			return Optional.absent();
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

		return Optional.fromNullable(resource);
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

		return Optional.fromNullable(dataModel);
	}

	/**
	 * Gets the configuration for the given resource identifier and configuration identifier
	 *
	 * @param resourceUuid      a resource identifier
	 * @param configurationUuid a configuration identifier
	 * @return (optional) the matched configuration
	 */
	public Optional<Configuration> fetchConfiguration(final String resourceUuid, final String configurationUuid) {
		final Optional<Resource> resourceOptional = fetchResource(resourceUuid);

		if (!resourceOptional.isPresent()) {

			DataModelUtil.LOG.debug("couldn't find  resource '" + resourceUuid);
			return Optional.absent();
		}

		final Configuration configuration = resourceOptional.get().getConfiguration(configurationUuid);

		return Optional.fromNullable(configuration);
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
			return Optional.absent();
		}

		final Configuration configuration = dataModelOptional.get().getConfiguration();

		return Optional.fromNullable(configuration);
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

	private InternalModelService determineInternalService(final Configuration configuration) throws DMPControllerException {

		final JsonNode storageType = configuration.getParameters().get(ConfigurationStatics.STORAGE_TYPE);

		if (storageType != null) {

			switch (storageType.asText()) {

				case ConfigurationStatics.SCHEMA_STORAGE_TYPE:

					// TODO: fix this as needed

					return null;
				case ConfigurationStatics.CSV_STORAGE_TYPE:

					return internalServiceFactoryProvider.get().getInternalGDMGraphService();

				case ConfigurationStatics.XML_STORAGE_TYPE:
				case ConfigurationStatics.MABXML_STORAGE_TYPE:
				case ConfigurationStatics.MARCXML_STORAGE_TYPE:
				case ConfigurationStatics.PNX_STORAGE_TYPE:
				case ConfigurationStatics.OAI_PMH_DC_ELEMENTS_STORAGE_TYPE:
				case ConfigurationStatics.OAIPMH_DC_TERMS_STORAGE_TYPE:
				case ConfigurationStatics.OAIPMH_MARCXML_STORAGE_TYPE:

					return internalServiceFactoryProvider.get().getInternalGDMGraphService();
				default:

					throw new DMPControllerException("couldn't determine internal service type from storage type = '" + storageType.asText() + "'");
			}
		} else {

			throw new DMPControllerException("couldn't determine storage type from configuration");
		}
	}

	private Iterator<Tuple<String, JsonNode>> dataIterator(final Iterator<Map.Entry<String, Model>> triples) {
		return new AbstractIterator<Tuple<String, JsonNode>>() {

			// TODO: where to to this? => [@tgaengler]: In my opinion, this needs to be done, when the input data model will
			// created, i.e., that you will only have valid data models here
			// private JsonNode injectDataType(final JsonNode jsonNode) {
			// final UnmodifiableIterator<String> typeKeys = Iterators.filter(jsonNode.fieldNames(), new Predicate<String>() {
			// @Override
			// public boolean apply(@Nullable final String input) {
			// return input != null && input.endsWith("#type");
			// }
			// });
			// final String typeKey;
			// try {
			// typeKey = Iterators.getOnlyElement(typeKeys);
			// } catch (final IllegalArgumentException | NoSuchElementException e) {
			// return jsonNode;
			// }
			//
			// final JsonNode typeNode = jsonNode.get(typeKey);
			// final String longTypeName = typeNode.textValue();
			// final String typeName = longTypeName.substring(longTypeName.lastIndexOf('#') + 1,
			// longTypeName.lastIndexOf("Type"));
			//
			// final ObjectNode objectNode = objectMapper.createObjectNode();
			// objectNode.put(typeName, jsonNode);
			//
			// return objectNode;
			// }

			@Override
			protected Tuple<String, JsonNode> computeNext() {
				if (triples.hasNext()) {
					final Map.Entry<String, Model> nextTriple = triples.next();
					final String recordId = nextTriple.getKey();
					final JsonNode jsonNode = nextTriple.getValue().toRawJSON();
					// return Tuple.tuple(recordId, injectDataType(jsonNode));
					return Tuple.tuple(recordId, jsonNode);
				}
				return endOfData();
			}
		};
	}
}
