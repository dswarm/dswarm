package de.avgl.dmp.controller.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.jsonschema.JSRoot;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.types.Tuple;
import de.avgl.dmp.persistence.service.InternalModelService;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.resource.ResourceService;
import de.avgl.dmp.persistence.service.schema.SchemaService;

@Singleton
public class InternalSchemaDataUtil {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(InternalSchemaDataUtil.class);
	private final ObjectMapper						objectMapper;
	private final Provider<ResourceService>			resourceServiceProvider;
	private final Provider<InternalModelServiceFactory>	internalServiceFactoryProvider;
	private final Provider<SchemaService>			schemaServiceProvider;
	private final Provider<DataModelService>		dataModelServiceProvider;

	@Inject
	public InternalSchemaDataUtil(final ObjectMapper objectMapper, final Provider<ResourceService> resourceServiceProvider,
			final Provider<InternalModelServiceFactory> internalServiceFactoryProvider, final Provider<SchemaService> schemaServiceProvider,
			final Provider<DataModelService> dataModelServiceProvider) {
		this.objectMapper = objectMapper;
		this.resourceServiceProvider = resourceServiceProvider;
		this.internalServiceFactoryProvider = internalServiceFactoryProvider;
		this.schemaServiceProvider = schemaServiceProvider;
		this.dataModelServiceProvider = dataModelServiceProvider;
	}

	/**
	 * This method is deprecated. Please utilise {@link InternalSchemaDataUtil#getData(long)} instead.
	 * 
	 * @param resourceId
	 * @param configurationId
	 * @return
	 */
	@Deprecated
	public Optional<Iterator<Tuple<String, JsonNode>>> getData(final long resourceId, final long configurationId) {
		return getData(resourceId, configurationId, Optional.<Integer> absent());
	}

	public Optional<Iterator<Tuple<String, JsonNode>>> getData(final long dataModelId) {
		return getData(dataModelId, Optional.<Integer> absent());
	}

	/**
	 * This method is deprecated. Please utilise {@link InternalSchemaDataUtil#getData(long, Optional)} instead.
	 * 
	 * @param resourceId
	 * @param configurationId
	 * @param atMost
	 * @return
	 */
	@Deprecated
	public Optional<Iterator<Tuple<String, JsonNode>>> getData(final long resourceId, final long configurationId, final Optional<Integer> atMost) {

		InternalSchemaDataUtil.LOG.debug(String.format("try to get data for configuration with id [%d] for resource with id [%d]", configurationId,
				resourceId));

		final Optional<Configuration> configurationOptional = fetchConfiguration(resourceId, configurationId);

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

			maybeTriples = internalService.getObjects(resourceId, configurationId, atMost);
		} catch (final DMPPersistenceException e1) {

			InternalSchemaDataUtil.LOG.debug(e1);
			return Optional.absent();
		}

		if (!maybeTriples.isPresent()) {

			InternalSchemaDataUtil.LOG.debug("couldn't find data");
			return Optional.absent();
		}

		final Iterator<Map.Entry<String, Model>> iterator = maybeTriples.get().entrySet().iterator();

		return Optional.of(dataIterator(iterator));
	}

	public Optional<Iterator<Tuple<String, JsonNode>>> getData(final long dataModelId, final Optional<Integer> atMost) {

		InternalSchemaDataUtil.LOG.debug(String.format("try to get data for data model with id [%d]", dataModelId));

		final Optional<Configuration> configurationOptional = fetchConfiguration(dataModelId);

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

			maybeTriples = internalService.getObjects(dataModelId, atMost);
		} catch (final DMPPersistenceException e1) {

			InternalSchemaDataUtil.LOG.debug(e1);
			return Optional.absent();
		}

		if (!maybeTriples.isPresent()) {

			InternalSchemaDataUtil.LOG.debug("couldn't find data");
			return Optional.absent();
		}

		final Iterator<Map.Entry<String, Model>> iterator = maybeTriples.get().entrySet().iterator();

		return Optional.of(dataIterator(iterator));
	}

	/**
	 * This method is deprecated. Please utilise {@link InternalSchemaDataUtil#getSchema(long)} instead.
	 * 
	 * @param resourceId
	 * @param configurationId
	 * @return
	 */
	@Deprecated
	public Optional<ObjectNode> getSchema(final long resourceId, final long configurationId) {

		final Optional<Configuration> configurationOptional = fetchConfiguration(resourceId, configurationId);

		if (!configurationOptional.isPresent()) {

			return Optional.absent();
		}

		final Configuration configuration = configurationOptional.get();

		// TODO: fixme

		final Optional<JSRoot> rootOptional = null;

		// schemaServiceProvider.get().getSchema(resourceId, configurationId)
		// .or(getConfiguredSchema(configuration));

		// if (rootOptional.isPresent()) {
		//
		// final JSRoot jsElements = rootOptional.get();
		//
		// try {
		// return Optional.of(jsElements.toJson(objectMapper));
		// } catch (final IOException e) {
		// LOG.warn(e.getMessage(), e);
		// return Optional.absent();
		// }
		// }

		final InternalModelService internalService;
		try {
			internalService = determineInternalService(configurationOptional.get());
		} catch (final DMPControllerException e) {

			return Optional.absent();
		}

		final Optional<Set<String>> schemaOptional = internalService.getSchema(resourceId, configurationId);

		if (!schemaOptional.isPresent()) {

			InternalSchemaDataUtil.LOG.debug("couldn't find schema");
			return Optional.absent();
		}

		final ObjectNode node = objectMapper.createObjectNode();
		node.put("title", configuration.getName());
		node.put("type", "object");
		final ObjectNode properties = node.putObject("properties");

		for (final String schemaProp : schemaOptional.get()) {
			properties.putObject(schemaProp).put("type", "string");
		}

		return Optional.of(node);
	}

	public Optional<ObjectNode> getSchema(final long dataModelId) {

		final Optional<Configuration> configurationOptional = fetchConfiguration(dataModelId);

		if (!configurationOptional.isPresent()) {

			return Optional.absent();
		}

		final Configuration configuration = configurationOptional.get();

		// TODO: fixme

		final Optional<JSRoot> rootOptional = null;

		// schemaServiceProvider.get().getSchema(resourceId, configurationId)
		// .or(getConfiguredSchema(configuration));

		// if (rootOptional.isPresent()) {
		//
		// final JSRoot jsElements = rootOptional.get();
		//
		// try {
		// return Optional.of(jsElements.toJson(objectMapper));
		// } catch (final IOException e) {
		// LOG.warn(e.getMessage(), e);
		// return Optional.absent();
		// }
		// }

		final InternalModelService internalService;
		try {
			internalService = determineInternalService(configurationOptional.get());
		} catch (final DMPControllerException e) {

			return Optional.absent();
		}

		Optional<Schema> schemaOptional = null;
		try {

			schemaOptional = internalService.getSchema(dataModelId);
		} catch (final DMPPersistenceException e) {

			InternalSchemaDataUtil.LOG.error("something went wrong while schema retrieval", e);

			return Optional.absent();
		}

		if (!schemaOptional.isPresent()) {

			InternalSchemaDataUtil.LOG.debug("couldn't find schema");
			return Optional.absent();
		}

		String schemaJSONString = null;;
		try {

			schemaJSONString = objectMapper.writeValueAsString(schemaOptional.get());
		} catch (final JsonProcessingException e) {

			InternalSchemaDataUtil.LOG.error("something went wrong while schema serialization", e);

			return Optional.absent();
		}

		ObjectNode node;
		try {
			node = objectMapper.readValue(schemaJSONString, ObjectNode.class);
		} catch (final IOException e) {

			InternalSchemaDataUtil.LOG.error("something went wrong while schema deserialization", e);

			return Optional.absent();
		}

		return Optional.of(node);
	}

	public Optional<Resource> fetchResource(final long resourceId) {

		final ResourceService resourceService = resourceServiceProvider.get();
		final Resource resource = resourceService.getObject(resourceId);

		return Optional.fromNullable(resource);
	}

	public Optional<DataModel> fetchDataModel(final long dataModelId) {

		final DataModelService dataModelService = dataModelServiceProvider.get();
		final DataModel dataModel = dataModelService.getObject(dataModelId);

		return Optional.fromNullable(dataModel);
	}

	public Optional<Configuration> fetchConfiguration(final long resourceId, final long configurationId) {
		final Optional<Resource> resourceOptional = fetchResource(resourceId);

		if (!resourceOptional.isPresent()) {

			InternalSchemaDataUtil.LOG.debug("couldn't find  resource '" + resourceId);
			return Optional.absent();
		}

		final Configuration configuration = resourceOptional.get().getConfiguration(configurationId);

		return Optional.fromNullable(configuration);
	}

	public Optional<Configuration> fetchConfiguration(final long dataModelId) {
		final Optional<DataModel> dataModelOptional = fetchDataModel(dataModelId);

		if (!dataModelOptional.isPresent()) {

			InternalSchemaDataUtil.LOG.debug("couldn't find data model '" + dataModelId + "'");
			return Optional.absent();
		}

		final Configuration configuration = dataModelOptional.get().getConfiguration();

		return Optional.fromNullable(configuration);
	}

	private InternalModelService determineInternalService(final Configuration configuration) throws DMPControllerException {

		final JsonNode storageType = configuration.getParameters().get("storage_type");

		if (storageType != null) {

			if ("schema".equals(storageType.asText())) {

				return internalServiceFactoryProvider.get().getMemoryDbInternalService();
			} else if ("csv".equals(storageType.asText())) {

				return internalServiceFactoryProvider.get().getMemoryDbInternalService();
			} else if ("xml".equals(storageType.asText())) {

				return internalServiceFactoryProvider.get().getInternalTripleService();
			} else {

				throw new DMPControllerException("couldn't determine internal service type from storage type = '" + storageType.asText() + "'");
			}
		} else {

			throw new DMPControllerException("couldn't determine storage type from configuration");
		}
	}

	private Optional<JSRoot> getConfiguredSchema(final Configuration configuration) {
		final ObjectNode parameters = configuration.getParameters();
		final JsonNode storageType = parameters.get("storage_type");
		if ("xml".equals(storageType.asText())) {

			final JsonNode schemaFile = parameters.get("schema_file");
			if (schemaFile != null) {
				final long schemaId = Long.valueOf(schemaFile.get("id").asText());
				final Optional<Resource> schemaOptional = fetchResource(schemaId);
				if (schemaOptional.isPresent()) {

					long latestConfigId = Integer.MIN_VALUE;

					for (final Configuration schemaConfiguration : schemaOptional.get().getConfigurations()) {

						if (schemaConfiguration.getId() > latestConfigId) {
							latestConfigId = schemaConfiguration.getId();
						}
					}

					if (latestConfigId != Integer.MIN_VALUE) {

						// TODO: fixme

						// return schemaServiceProvider.get().getSchema(schemaId, latestConfigId);
					}
				}
			}
		}

		return Optional.absent();
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
					final JsonNode jsonNode = nextTriple.getValue().toJSON();
					// return Tuple.tuple(recordId, injectDataType(jsonNode));
					return Tuple.tuple(recordId, jsonNode);
				}
				return endOfData();
			}
		};
	}
}
