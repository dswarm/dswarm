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
import java.util.Collection;
import java.util.Set;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import org.dswarm.common.types.Tuple;
import org.dswarm.controller.DMPControllerException;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.schema.Schema;
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
	private final ObjectMapper                          objectMapper;
	private final Provider<ResourceService>             resourceServiceProvider;
	private final Provider<InternalModelServiceFactory> internalServiceFactoryProvider;
	private final Provider<DataModelService>            dataModelServiceProvider;

	@Inject
	public DataModelUtil(final ObjectMapper objectMapper, final Provider<ResourceService> resourceServiceProvider,
			final Provider<InternalModelServiceFactory> internalServiceFactoryProvider, final Provider<DataModelService> dataModelServiceProvider) {
		this.objectMapper = objectMapper;
		this.resourceServiceProvider = resourceServiceProvider;
		this.internalServiceFactoryProvider = internalServiceFactoryProvider;
		this.dataModelServiceProvider = dataModelServiceProvider;
	}

	/**
	 * Gets the data of the given data model.
	 *
	 * @param dataModelUuid the identifier of the data model.
	 * @return the data of the given data model
	 */
	public Observable<Tuple<String, JsonNode>> getData(final String dataModelUuid) {
		return getData(dataModelUuid, Optional.<Integer>absent());
	}

	/**
	 * Gets the data of the given data model and maximum in the given amount.
	 *
	 * @param dataModelUuid the identifier of the data model
	 * @param atMost        the number of records that should be retrieved
	 * @return the data of the given data model
	 */
	public Observable<Tuple<String, JsonNode>> getData(final String dataModelUuid, final Optional<Integer> atMost) {

		DataModelUtil.LOG.debug(String.format("try to get data for data model with id [%s]", dataModelUuid));

		final InternalModelService internalService = internalServiceFactoryProvider.get().getInternalGDMGraphService();

		final Observable<Tuple<String, Model>> modelObservable;

		try {

			modelObservable = internalService.getObjects(dataModelUuid, atMost);
		} catch (final DMPPersistenceException e1) {

			DataModelUtil.LOG.debug("couldn't find data", e1);
			return Observable.empty();
		}

		return modelObservable.map(this::transformDataNode);
	}

	/**
	 * Gets the data of the records with the given record identifier in the given data model..
	 *
	 * @param recordIdentifiers the record identifiers
	 * @param dataModelUuid the identifier of the data model
	 * @return the data of the given data model
	 */
	public Observable<Tuple<String, JsonNode>> getRecordsData(final Set<String> recordIdentifiers, final String dataModelUuid) {

		DataModelUtil.LOG.debug(String.format("try to get record's data for some records in data model with id [%s]", dataModelUuid));

		final InternalModelService internalService = internalServiceFactoryProvider.get().getInternalGDMGraphService();

		final Observable<Tuple<String, Model>> maybeTriples;

		try {

			maybeTriples = internalService.getRecords(recordIdentifiers, dataModelUuid);
		} catch (final DMPPersistenceException e1) {

			DataModelUtil.LOG.debug("couldn't find record's data data in data model with id [{}]", dataModelUuid, e1);
			return Observable.empty();
		}

		return maybeTriples.map(this::transformDataNode);
	}

	/**
	 * Gets the data of the search result of the given data model and maximum in the given amount.
	 *
	 * @param keyAttributePathString the key attribute path
	 * @param searchValue the search value
	 * @param dataModelUuid the identifer of the data model
	 * @param atMost        the number of records that should be retrieved
	 * @return the data of the given data model
	 */
	public Observable<Tuple<String, JsonNode>> searchRecords(final String keyAttributePathString, final String searchValue,
			final String dataModelUuid, final Optional<Integer> atMost) {

		DataModelUtil.LOG.debug(String.format("try to get data for data model with id [%s]", dataModelUuid));

		final InternalModelService internalService = internalServiceFactoryProvider.get().getInternalGDMGraphService();

		final Observable<Tuple<String, Model>> maybeTriples;

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

	public Observable<Response> deprecateRecords(final Collection<String> recordURIs, final String dataModelUuid) throws DMPControllerException {

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

			return Optional.absent();
		}

		if (!schemaOptional.isPresent()) {

			DataModelUtil.LOG.debug("couldn't find schema");
			return Optional.absent();
		}

		final String schemaJSONString;
		try {

			schemaJSONString = objectMapper.writeValueAsString(schemaOptional.get());
		} catch (final JsonProcessingException e) {

			DataModelUtil.LOG.error("something went wrong while schema serialization", e);

			return Optional.absent();
		}

		final ObjectNode node;
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

	private Tuple<String, JsonNode> transformDataNode(final Tuple<String, Model> input) {
		final String recordId = input.v1();
		final JsonNode jsonNode = input.v2().toRawJSON();
		return Tuple.tuple(recordId, jsonNode);
	}
}
