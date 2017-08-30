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
package org.dswarm.persistence.service.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.persist.PersistService;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.common.types.Tuple;
import org.dswarm.init.ConfigModule;
import org.dswarm.init.ExecutionScope;
import org.dswarm.init.LoggingConfigurator;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.JacksonObjectMapperModule;
import org.dswarm.persistence.JpaHibernateModule;
import org.dswarm.persistence.PersistenceModule;
import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.FunctionType;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.proxy.ProxyDMPObject;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.utils.AttributePathUtils;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.BasicJPAService;
import org.dswarm.persistence.service.MaintainDBService;
import org.dswarm.persistence.service.PersistenceType;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.job.FunctionService;
import org.dswarm.persistence.service.job.ProjectService;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.resource.ResourceService;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.dswarm.persistence.service.schema.SchemaService;

/**
 * a script that migrates existing data in the metadata repository into a new (initial) state of the metadata repository
 *
 * @author tgaengler
 */
public class MetadataRepositoryMigrator {

	private static final Logger LOG = LoggerFactory.getLogger(MetadataRepositoryMigrator.class);

	private static final String RESOURCES_FILE_NAME      = "resources.json";
	private static final String CONFIGURATIONS_FILE_NAME = "configurations.json";
	private static final String SCHEMAS_FILE_NAME        = "schemas.json";
	private static final String DATA_MODELS_FILE_NAME    = "data_models.json";
	private static final String PROJECTS_FILE_NAME       = "projects.json";

	private final Provider<ResourceService>                    resourcePersistenceServiceProvider;
	private final Provider<ConfigurationService>               configurationPersistenceServiceProvider;
	private final Provider<SchemaService>                      schemaPersistenceServiceProvider;
	private final Provider<SchemaAttributePathInstanceService> schemaAttributePathInstancePersistenceServiceProvider;
	private final Provider<AttributePathService>               attributePathPersistenceServiceProvider;
	private final Provider<AttributeService>                   attributePersistenceServiceProvider;
	private final Provider<ClaszService>                       classPersistenceServiceProvider;
	private final Provider<DataModelService>                   dataModelPersistenceServiceProvider;
	private final Provider<ProjectService>                     projectPersistenceServiceProvider;
	private final Provider<FunctionService>                    functionPersistenceServiceProvider;
	private final Provider<MaintainDBService>                  maintainDBServiceProvider;

	private final Map<String, Function>  persistentFunctionsCache  = new HashMap<>();
	private final Map<String, Attribute> persistentAttributesCache = new HashMap<>();

	private static final JaxbAnnotationModule module = new JaxbAnnotationModule();
	private final ObjectMapper mapper;
	private static final ObjectMapper MAPPER = new ObjectMapper()
			.registerModule(module)
			.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
			.setSerializationInclusion(JsonInclude.Include.NON_NULL)
			.configure(SerializationFeature.INDENT_OUTPUT, true);

	@Inject
	public MetadataRepositoryMigrator(final Provider<ResourceService> resourcePersistenceServiceProviderArg,
			final Provider<ConfigurationService> configurationPersistenceServiceProviderArg,
			final Provider<SchemaService> schemaPersistenceServiceProviderArg,
			final Provider<SchemaAttributePathInstanceService> schemaAttributePathInstancePersistenceServiceProviderArg,
			final Provider<AttributePathService> attributePathPersistenceServiceProviderArg,
			final Provider<AttributeService> attributePersistenceServiceProviderArg,
			final Provider<ClaszService> classPersistenceServiceProviderArg,
			final Provider<DataModelService> dataModelPersistenceServiceProviderArg,
			final Provider<ProjectService> projectPersistenceServiceProviderArg,
			final Provider<FunctionService> functionPersistenceServiceProviderArg,
			final Provider<MaintainDBService> maintainDBServiceProvierArg,
			final ObjectMapper mapperArg) {

		resourcePersistenceServiceProvider = resourcePersistenceServiceProviderArg;
		configurationPersistenceServiceProvider = configurationPersistenceServiceProviderArg;
		schemaPersistenceServiceProvider = schemaPersistenceServiceProviderArg;
		schemaAttributePathInstancePersistenceServiceProvider = schemaAttributePathInstancePersistenceServiceProviderArg;
		attributePathPersistenceServiceProvider = attributePathPersistenceServiceProviderArg;
		attributePersistenceServiceProvider = attributePersistenceServiceProviderArg;
		classPersistenceServiceProvider = classPersistenceServiceProviderArg;
		dataModelPersistenceServiceProvider = dataModelPersistenceServiceProviderArg;
		projectPersistenceServiceProvider = projectPersistenceServiceProviderArg;
		functionPersistenceServiceProvider = functionPersistenceServiceProviderArg;
		maintainDBServiceProvider = maintainDBServiceProvierArg;
		mapper = mapperArg
				.registerModule(module)
				.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	public void migrateData() throws IOException, DMPPersistenceException {

		final Map<String, String> dumps = dumpData();
		upgradeMetadataRepository();
		recreateExistingEntities(dumps);
	}

	private Map<String, String> dumpData() throws IOException {

		LOG.debug("try to dump data from Metadata Repository");

		final Tuple<String, String> resourcesDumpTuple = dumpResources();
		final Tuple<String, String> configurationsDumpTuple = dumpConfigurations();
		final Tuple<String, String> schemasDumpTuple = dumpSchemas();
		final Tuple<String, String> dataModelsDumpTuple = dumpDataModels();
		final Tuple<String, String> projectsDumpTuple = dumpProjects();

		final Map<String, String> dumps = new HashMap<>();

		dumps.put(resourcesDumpTuple.v1(), resourcesDumpTuple.v2());
		dumps.put(configurationsDumpTuple.v1(), configurationsDumpTuple.v2());
		dumps.put(schemasDumpTuple.v1(), schemasDumpTuple.v2());
		dumps.put(dataModelsDumpTuple.v1(), dataModelsDumpTuple.v2());
		dumps.put(projectsDumpTuple.v1(), projectsDumpTuple.v2());

		LOG.debug("dumped data from Metadata Repository");

		return dumps;
	}

	private Tuple<String, String> dumpResources() throws IOException {

		return dumpEntities(resourcePersistenceServiceProvider, RESOURCES_FILE_NAME, MAPPER);
	}

	private Tuple<String, String> dumpConfigurations() throws IOException {

		return dumpEntities(configurationPersistenceServiceProvider, CONFIGURATIONS_FILE_NAME, MAPPER);
	}

	private Tuple<String, String> dumpSchemas() throws IOException {

		return dumpEntities(schemaPersistenceServiceProvider, SCHEMAS_FILE_NAME);
	}

	private Tuple<String, String> dumpDataModels() throws IOException {

		return dumpEntities(dataModelPersistenceServiceProvider, DATA_MODELS_FILE_NAME);
	}

	private Tuple<String, String> dumpProjects() throws IOException {

		return dumpEntities(projectPersistenceServiceProvider, PROJECTS_FILE_NAME);
	}

	private void upgradeMetadataRepository() throws DMPPersistenceException {

		LOG.debug("upgrade metadata repository");

		final MaintainDBService maintainDBService = maintainDBServiceProvider.get();

		maintainDBService.initDB();
	}

	/**
	 * replay dumped data (incl. necessary replacements)
	 */
	private void recreateExistingEntities(final Map<String, String> dumps) throws IOException, DMPPersistenceException {

		final String resourcesDumpFileName = dumps.get(RESOURCES_FILE_NAME);
		final String configurationsDumpFileName = dumps.get(CONFIGURATIONS_FILE_NAME);
		final String schemasDumpFileName = dumps.get(SCHEMAS_FILE_NAME);
		final String dataModelsDumpFileName = dumps.get(DATA_MODELS_FILE_NAME);
		final String projectsDumpFileName = dumps.get(PROJECTS_FILE_NAME);

		final Map<String, Configuration> persistentConfigurations = recreateExistingConfigurations(configurationsDumpFileName);
		final Map<String, Resource> persistentResources = recreateExistingResources(resourcesDumpFileName, persistentConfigurations);
		final SchemaRecreationResultSet schemaRecreationResultSet = recreateExistingSchemas(schemasDumpFileName);

		final Map<String, Schema> persistentSchemata = schemaRecreationResultSet.getOldUuidNewPersistentSchemaMap();
		final Map<String, AttributePath> persistentAttributePaths = schemaRecreationResultSet.getOldUuidNewPersistentAttributePathMap();

		final Map<String, DataModel> persistentDataModels = recreateExistingDataModels(dataModelsDumpFileName, persistentSchemata,
				persistentConfigurations, persistentResources);
		recreateExistingProjects(projectsDumpFileName, persistentDataModels, persistentAttributePaths);
	}

	/**
	 * note: with the new settings that are available at entity creation, we may don't need the separate update step here, i.e, we could simply update the configurations at the resources and persist them
	 *
	 * 0. remove related configurations from resource
	 * 1. create resources (without related configurations)
	 * 2. add related configurations to persistent resources
	 * 3. update resources with related configurations
	 *
	 * @param filePath
	 * @throws IOException
	 * @throws DMPPersistenceException
	 */
	private Map<String, Resource> recreateExistingResources(final String filePath, final Map<String, Configuration> persistentConfigurations)
			throws IOException, DMPPersistenceException {

		final Collection<Resource> existingResources = deserializeEntities(filePath, new TypeReference<ArrayList<Resource>>() {

		}, Resource.class.getName(), MAPPER);

		final Collection<Resource> modifiedResources = new ArrayList<>();
		final Map<String, Collection<Configuration>> resourcesConfigurations = new LinkedHashMap<>();

		// remove related configurations from resources
		for (final Resource existingResource : existingResources) {

			final Set<Configuration> configurations = existingResource.getConfigurations();

			if (configurations != null) {

				final Set<Configuration> configurationsCopy = new LinkedHashSet<>();
				configurationsCopy.addAll(configurations);

				existingResource.setConfigurations(null);

				final String existingResourceUuid = existingResource.getUuid();
				resourcesConfigurations.put(existingResourceUuid, new ArrayList<>());

				for (final Configuration configuration : configurationsCopy) {

					final String configurationUuid = configuration.getUuid();
					final Configuration persistentConfiguration = persistentConfigurations.get(configurationUuid);

					if (persistentConfiguration != null) {

						resourcesConfigurations.get(existingResourceUuid).add(persistentConfiguration);
					} else {

						LOG.debug("couldn't find configuration '{}' in the collection of persistent configurations", configurationUuid);
					}
				}
			}

			modifiedResources.add(existingResource);
		}

		// create resources without related configurations
		final Map<String, Resource> persistentResources = recreateEntities(resourcePersistenceServiceProvider, modifiedResources);

		final Collection<Resource> updatedResources = new ArrayList<>();

		// add related configurations to persistent resources
		for (final Map.Entry<String, Resource> persistentResourceEnty : persistentResources.entrySet()) {

			final String persistentResourceUuid = persistentResourceEnty.getKey();
			final Resource persistentResource = persistentResourceEnty.getValue();

			final Collection<Configuration> resourceConfigurations = resourcesConfigurations.get(persistentResourceUuid);

			if (resourceConfigurations == null) {

				continue;
			}

			for (final Configuration resourceConfiguration : resourceConfigurations) {

				persistentResource.addConfiguration(resourceConfiguration);
			}

			updatedResources.add(persistentResource);
		}

		// updated resources with related configurations
		return updateEntities(resourcePersistenceServiceProvider, updatedResources);
	}

	/**
	 * 1. remove related resources
	 * 2. create configurations
	 *
	 * @param filePath
	 * @return
	 * @throws IOException
	 * @throws DMPPersistenceException
	 */
	private Map<String, Configuration> recreateExistingConfigurations(final String filePath) throws IOException, DMPPersistenceException {

		final Collection<Configuration> existingConfigurations = deserializeEntities(filePath, new TypeReference<ArrayList<Configuration>>() {

		}, Configuration.class.getName(), MAPPER);

		final Collection<Configuration> modifiedConfigurations = new ArrayList<>();

		for (final Configuration existingConfiguration : existingConfigurations) {

			existingConfiguration.setResources(null);

			modifiedConfigurations.add(existingConfiguration);
		}

		return recreateEntities(configurationPersistenceServiceProvider, modifiedConfigurations);
	}

	/**
	 * recreates existing schemata that are no inbuilt schema
	 * note: currently, we do not handle schemata with sub schemata
	 * note: currently, we create new schema uuids for those schemata
	 *
	 * 0. fetch all existing attributes and attribute paths from schemata
	 * 2. recreate attributes and attribute paths (with new, persistent attributes)
	 * 3. recreate schemata with new, persistent attribute paths
	 *
	 * @param filePath
	 * @return a map with the old schema uuids as keys and the new, persistent schemas as values
	 * @throws IOException
	 * @throws DMPPersistenceException
	 */
	private SchemaRecreationResultSet recreateExistingSchemas(final String filePath) throws IOException, DMPPersistenceException {

		final Collection<Schema> existingSchemas = deserializeEntities(filePath, new TypeReference<ArrayList<Schema>>() {

		}, Schema.class.getName());

		final Collection<String> inbuiltSchemaUuids = SchemaUtils.getInbuiltSchemaUuids();
		final Map<String, Schema> otherSchemata = new HashMap<>();

		for (final Schema existingSchema : existingSchemas) {

			final String existingSchemaUuid = existingSchema.getUuid();

			if (inbuiltSchemaUuids.contains(existingSchemaUuid)) {

				// we only need to handle other schemas here

				continue;
			}

			otherSchemata.put(existingSchemaUuid, existingSchema);
		}

		// note:
		// - we (probably) need a mapping between old and new attribute paths (identifiers)
		// - we (probably) need a mapping between old and new attributes (identifiers)
		// - we (probably) need a mapping between old and new classes (identifiers)
		// - we (probably) need a mapping between old and new schema (identifiers)

		final Map<String, Schema> oldUuidNewSchemaMap = new LinkedHashMap<>();
		final Map<String, AttributePath> oldUuidNewPersistentAttributePathMap = new LinkedHashMap<>();
		final Map<String, AttributePath> persistentAttributePaths = new LinkedHashMap<>();

		final AttributePathService attributePathService = attributePathPersistenceServiceProvider.get();
		final SchemaAttributePathInstanceService schemaAttributePathInstanceService = schemaAttributePathInstancePersistenceServiceProvider
				.get();

		for (final Map.Entry<String, Schema> otherSchemaEntry : otherSchemata.entrySet()) {

			final Schema otherSchema = otherSchemaEntry.getValue();

			// TODO: or shall we simply take the uuid from the old, existing schema?
			final String schemaUUID = UUIDService.getUUID(Schema.class.getSimpleName());
			final Schema newSchema = new Schema(schemaUUID);

			final Map<String, Attribute> otherSchemaAttributeMap = SchemaUtils.generateAttributeMap2(otherSchema);

			// handle attribute paths
			if (otherSchemaAttributeMap != null) {

				// re-create all attributes
				final Map<String, Attribute> persistentAttributes = recreateEntities(attributePersistenceServiceProvider,
						otherSchemaAttributeMap.values());

				final Map<String, Attribute> attributeUriPersistentAttributeMap = generateAttributeMap(persistentAttributes.values());

				final Map<String, AttributePath> otherSchemaAttributePathMap = SchemaUtils.generateAttributePathMap(otherSchema);

				// re-create all attribute paths with re-created attributes (instead of old ones)
				final Map<String, LinkedList<Attribute>> newAttributePaths = generateAttributesLists(otherSchemaAttributePathMap.values(),
						attributeUriPersistentAttributeMap);

				for (final Map.Entry<String, LinkedList<Attribute>> newAttributePathEntry : newAttributePaths.entrySet()) {

					final String newAttributePathString = newAttributePathEntry.getKey();
					final LinkedList<Attribute> newAttributePath = newAttributePathEntry.getValue();

					final AttributePath persistentAttributePath;

					if (!persistentAttributePaths.containsKey(newAttributePathString)) {

						final Boolean required = null;
						final Boolean multivalue = null;

						final AttributePath recreatedAttributePath = SchemaUtils
								.addAttributePaths(newSchema, newAttributePath, required, multivalue, attributePathService, schemaAttributePathInstanceService);

						final String recreatedAttributePathString = recreatedAttributePath.toAttributePath();

						persistentAttributePaths.put(recreatedAttributePathString, recreatedAttributePath);

						final AttributePath otherSchemaAttributePath = otherSchemaAttributePathMap.get(recreatedAttributePathString);
						final String oldAttributePathUuid = otherSchemaAttributePath.getUuid();

						oldUuidNewPersistentAttributePathMap.put(oldAttributePathUuid, recreatedAttributePath);

						persistentAttributePath = recreatedAttributePath;
					} else {

						// re-utilise already persistent attribute paths (i.e. this is a cache)

						persistentAttributePath = persistentAttributePaths.get(newAttributePathString);
					}

					final String sapiUUID = UUIDService.getUUID(SchemaAttributePathInstance.class.getSimpleName());
					final SchemaAttributePathInstance sapi = new SchemaAttributePathInstance(sapiUUID);
					sapi.setAttributePath(persistentAttributePath);

					newSchema.addAttributePath(sapi);
				}
			}

			final Clasz recordClass = otherSchema.getRecordClass();

			// handle record class
			if (recordClass != null) {

				SchemaUtils.addRecordClass(newSchema, recordClass.getUri(), classPersistenceServiceProvider);
			}

			oldUuidNewSchemaMap.put(otherSchema.getUuid(), newSchema);
		}

		final Map<String, Schema> recreateEntities = recreateEntities(schemaPersistenceServiceProvider, oldUuidNewSchemaMap.values(),
				PersistenceType.Merge);

		// create map with old schema uuids and new, persistent schemas
		final Map<String, Schema> oldUuidNewPersistentSchemaMap = new LinkedHashMap<>();

		for (final Map.Entry<String, Schema> oldUuidNewSchemaEntry : oldUuidNewSchemaMap.entrySet()) {

			final String oldSchemaUuid = oldUuidNewSchemaEntry.getKey();
			final String newSchemaUuid = oldUuidNewSchemaEntry.getValue().getUuid();

			final Schema newPersistentSchema = recreateEntities.get(newSchemaUuid);

			oldUuidNewPersistentSchemaMap.put(oldSchemaUuid, newPersistentSchema);
		}

		return new SchemaRecreationResultSet(oldUuidNewPersistentSchemaMap, oldUuidNewPersistentAttributePathMap);
	}

	/**
	 * tries to re-create the existing data models.
	 *
	 * 1. replace data resource, configuration and schema with persistent entities
	 * 2. re-create data models (with same uuid as existing ones)
	 *
	 * @param filePath
	 * @param oldUuidNewPersistentSchemaMap
	 * @param persistentConfigurations
	 * @param persistentResources
	 * @return
	 * @throws IOException
	 * @throws DMPPersistenceException
	 */
	private Map<String, DataModel> recreateExistingDataModels(final String filePath, final Map<String, Schema> oldUuidNewPersistentSchemaMap,
			final Map<String, Configuration> persistentConfigurations, final Map<String, Resource> persistentResources)
			throws IOException, DMPPersistenceException {

		final Collection<DataModel> existingDataModels = deserializeEntities(filePath, new TypeReference<ArrayList<DataModel>>() {

		}, DataModel.class.getName());

		final Collection<String> dataModelsToInbuiltSchemata = DataModelUtils.getDataModelsToInbuiltSchemata();
		final Map<String, DataModel> otherDataModels = new HashMap<>();

		for (final DataModel existingDataModel : existingDataModels) {

			final String existingDataModelUuid = existingDataModel.getUuid();

			if (dataModelsToInbuiltSchemata.contains(existingDataModelUuid)) {

				// we only need to handle other data models here

				continue;
			}

			otherDataModels.put(existingDataModelUuid, existingDataModel);
		}

		for (final DataModel otherDataModel : otherDataModels.values()) {

			replaceDataResource(persistentResources, otherDataModel);
			replaceConfiguration(persistentConfigurations, otherDataModel);
			replaceSchema(oldUuidNewPersistentSchemaMap, otherDataModel);
		}

		return recreateEntities(dataModelPersistenceServiceProvider, otherDataModels.values(),
				PersistenceType.Merge);
	}

	/**
	 *
	 *
	 * 1. replace input and output data models with persistent ones (or inbuilt ones)
	 * 2. replace attribute paths of MAPIs with persistent ones (we also fetch APs from inbuilt schemata from DB)
	 * 3. replace functions with persistent ones (i.e. those that are at a project and those that are at a mapping)
	 * 4. re-create projects
	 *
	 * @param filePath
	 * @param persistentDataModels
	 * @param persistentAttributePaths
	 * @throws IOException
	 */
	private Map<String, Project> recreateExistingProjects(final String filePath, final Map<String, DataModel> persistentDataModels,
			final Map<String, AttributePath> persistentAttributePaths) throws IOException, DMPPersistenceException {

		final Collection<Project> existingProjects = deserializeEntities(filePath, new TypeReference<ArrayList<Project>>() {

		}, Project.class.getName());

		for (final Project existingProject : existingProjects) {

			replaceDataModels(persistentDataModels, existingProject);
			replaceAttributePathsInMappings(persistentAttributePaths, existingProject);
			replaceFunctions(existingProject);
		}

		return recreateEntities(projectPersistenceServiceProvider, existingProjects, PersistenceType.Merge);
	}

	private <PERSISTENCE_SERVICE extends BasicJPAService> Tuple<String, String> dumpEntities(
			final Provider<PERSISTENCE_SERVICE> persistenceServiceProvider, final String fileName)
			throws IOException {

		return dumpEntities(persistenceServiceProvider, fileName, mapper);
	}

	private <PERSISTENCE_SERVICE extends BasicJPAService> Tuple<String, String> dumpEntities(
			final Provider<PERSISTENCE_SERVICE> persistenceServiceProvider, final String fileName, final ObjectMapper mapper) throws IOException {

		final PERSISTENCE_SERVICE persistenceService = persistenceServiceProvider.get();

		final Class clasz = persistenceService.getClasz();
		final String claszName = clasz.getName();

		LOG.debug("try to dump {}s", claszName);

		final List entities = persistenceService.getObjects();

		LOG.debug("retrieved '{}' {}s", entities.size(), claszName);

		final String entitiesJSONString = mapper.writeValueAsString(entities);
		final byte[] entitiesJSONBytes = entitiesJSONString.getBytes();

		final File file = createFile(fileName);

		Files.write(entitiesJSONBytes, file);

		final String absolutePath = file.getAbsolutePath();

		LOG.debug("wrote '{}s' to '{}'", claszName, absolutePath);

		return Tuple.tuple(fileName, absolutePath);
	}

	private static File createFile(final String fileName) throws IOException {

		final String[] fileNameParts = fileName.split("\\.");

		return File.createTempFile(fileNameParts[0], "." + fileNameParts[1]);
	}

	private <POJOCLASS extends DMPObject, T extends ArrayList<POJOCLASS>> Collection<POJOCLASS> deserializeEntities(final String filePath,
			final TypeReference<T> typeReference, final String entityName) throws IOException {

		return deserializeEntities(filePath, typeReference, entityName, mapper);
	}

	private <POJOCLASS extends DMPObject, T extends ArrayList<POJOCLASS>> Collection<POJOCLASS> deserializeEntities(final String filePath,
			final TypeReference<T> typeReference, final String entityName, final ObjectMapper mapper) throws IOException {

		LOG.debug("try to deserialize some {}s", entityName);

		final Path path = Paths.get(filePath);
		final URI uri = path.toUri();
		final File file = new File(uri);

		final Collection<POJOCLASS> deserializedEntities = mapper.readValue(file, typeReference);

		LOG.debug("deserialized '{}' {}s", deserializedEntities.size(), entityName);

		return deserializedEntities;
	}

	private <PERSISTENCE_SERVICE extends BasicJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyDMPObject<POJOCLASS>, POJOCLASS extends DMPObject> Map<String, POJOCLASS> recreateEntities(
			final Provider<PERSISTENCE_SERVICE> persistenceServiceProvider, final Collection<POJOCLASS> entities) throws DMPPersistenceException {

		return recreateEntities(persistenceServiceProvider, entities, PersistenceType.Persist);
	}

	private <PERSISTENCE_SERVICE extends BasicJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyDMPObject<POJOCLASS>, POJOCLASS extends DMPObject> Map<String, POJOCLASS> recreateEntities(
			final Provider<PERSISTENCE_SERVICE> persistenceServiceProvider, final Collection<POJOCLASS> entities,
			final PersistenceType persistentType) throws DMPPersistenceException {

		final PERSISTENCE_SERVICE persistenceService = persistenceServiceProvider.get();
		final Class<POJOCLASS> clasz = persistenceService.getClasz();
		final String claszName = clasz.getName();

		LOG.debug("try to re-create '{}' {}s", entities.size(), claszName);

		final Map<String, POJOCLASS> persistentEntities = new LinkedHashMap<>();

		for (final POJOCLASS entity : entities) {

			final PROXYPOJOCLASS proxyPersistentEntity = persistenceService.createObjectTransactional(entity, persistentType);
			final POJOCLASS persistentEntity = proxyPersistentEntity.getObject();

			persistentEntities.put(persistentEntity.getUuid(), persistentEntity);
		}

		LOG.debug("re-created '{}' {}s", persistentEntities.size(), claszName);

		return persistentEntities;
	}

	private <PERSISTENCE_SERVICE extends BasicJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyDMPObject<POJOCLASS>, POJOCLASS extends DMPObject> Map<String, POJOCLASS> updateEntities(
			final Provider<PERSISTENCE_SERVICE> persistenceServiceProvider, final Collection<POJOCLASS> entities) throws DMPPersistenceException {

		final PERSISTENCE_SERVICE persistenceService = persistenceServiceProvider.get();
		final Class<POJOCLASS> clasz = persistenceService.getClasz();
		final String claszName = clasz.getName();

		LOG.debug("try to update '{}' {}s", entities.size(), claszName);

		final Map<String, POJOCLASS> persistentEntities = new LinkedHashMap<>();

		for (final POJOCLASS entity : entities) {

			final PROXYPOJOCLASS proxyEntity = persistenceService.updateObjectTransactional(entity);
			final POJOCLASS persistentEntity = proxyEntity.getObject();

			persistentEntities.put(persistentEntity.getUuid(), persistentEntity);
		}

		LOG.debug("updated '{}' {}s", persistentEntities.size(), claszName);

		return persistentEntities;
	}

	private static Map<String, Attribute> generateAttributeMap(final Collection<Attribute> attributes) {

		final Map<String, Attribute> attributeMap = new LinkedHashMap<>();

		for (final Attribute attribute : attributes) {

			attributeMap.put(attribute.getUri(), attribute);
		}

		return attributeMap;
	}

	private static Map<String, LinkedList<Attribute>> generateAttributesLists(final Collection<AttributePath> attributePaths,
			final Map<String, Attribute> attributeMap) {

		final Map<String, LinkedList<Attribute>> actualAttributesLists = new LinkedHashMap<>();

		for (final AttributePath attributePath : attributePaths) {

			final List<Attribute> attributePathAttributesList = attributePath.getAttributePath();

			final LinkedList<Attribute> newAttributes = generateAttributeList(attributePathAttributesList, attributeMap);

			final String newAttributePathString = AttributePathUtils.generateAttributePath(newAttributes);

			actualAttributesLists.put(newAttributePathString, newAttributes);
		}

		return actualAttributesLists;
	}

	private static LinkedList<Attribute> generateAttributeList(final List<Attribute> existingAttributes,
			final Map<String, Attribute> recreatedAttributes) {

		final LinkedList<Attribute> newAttributes = new LinkedList<>();

		for (final Attribute existingAttribute : existingAttributes) {

			final Attribute newAttribute = recreatedAttributes.get(existingAttribute.getUri());

			newAttributes.add(newAttribute);
		}

		return newAttributes;
	}

	private static void replaceDataResource(final Map<String, Resource> persistentResources, final DataModel otherDataModel)
			throws DMPPersistenceException {

		final Resource existingDataResource = otherDataModel.getDataResource();

		if (existingDataResource == null) {

			return;
		}

		final String existingDataResourceUuid = existingDataResource.getUuid();

		final Resource persistentResource = persistentResources.get(existingDataResourceUuid);

		if (persistentResource == null) {

			final String message = String.format("could not find resource '%s' in the persistent resources map", existingDataResourceUuid);

			LOG.error(message);

			throw new DMPPersistenceException(message);
		}

		otherDataModel.setDataResource(persistentResource);
	}

	private static void replaceConfiguration(final Map<String, Configuration> persistentConfigurations, final DataModel otherDataModel)
			throws DMPPersistenceException {

		final Configuration existingConfiguration = otherDataModel.getConfiguration();

		if (existingConfiguration == null) {

			return;
		}

		final String existingConfigurationUuid = existingConfiguration.getUuid();

		final Configuration persistentConfiguration = persistentConfigurations.get(existingConfigurationUuid);

		if (persistentConfiguration == null) {

			final String message = String
					.format("could not find configuration '%s' in the persistent configurations map", existingConfigurationUuid);

			LOG.error(message);

			throw new DMPPersistenceException(message);
		}

		otherDataModel.setConfiguration(persistentConfiguration);
	}

	/**
	 * replace existing (maybe out-dated (inbuilt)) schemata with persistent ones
	 *
	 * @param oldUuidNewPersistentSchemaMap
	 * @param otherDataModel
	 * @throws DMPPersistenceException if it couldn't find a persistent schema for the schema uuid of the existing data model
	 */
	private void replaceSchema(final Map<String, Schema> oldUuidNewPersistentSchemaMap, final DataModel otherDataModel)
			throws DMPPersistenceException {

		final Schema existingSchema = otherDataModel.getSchema();

		if (existingSchema == null) {

			return;
		}

		final String existingSchemaUuid = existingSchema.getUuid();

		final Collection<String> inbuiltSchemaUuids = SchemaUtils.getInbuiltSchemaUuids();

		final Schema persistentSchema;

		if (inbuiltSchemaUuids.contains(existingSchemaUuid)) {

			// schema is an inbuilt schema

			final SchemaService schemaService = schemaPersistenceServiceProvider.get();

			LOG.debug("schema '{}' is an inbuilt schema, will try re-retrieve it from DB");

			persistentSchema = schemaService.getObject(existingSchemaUuid);
		} else {

			// schema is not an inbuilt schema

			persistentSchema = oldUuidNewPersistentSchemaMap.get(existingSchemaUuid);

			if (persistentSchema == null) {

				final String message = String
						.format("could not find schema '%s' in the persistent schemas map", existingSchemaUuid);

				LOG.error(message);

				throw new DMPPersistenceException(message);
			}
		}

		otherDataModel.setSchema(persistentSchema);
	}

	/**
	 * replaces input and output data models with persistent ones (or inbuilt ones)
	 *
	 * @param persistentDataModels
	 * @param project
	 * @throws DMPPersistenceException
	 */
	private void replaceDataModels(final Map<String, DataModel> persistentDataModels, final Project project)
			throws DMPPersistenceException {

		final DataModel existingInputDataModel = project.getInputDataModel();
		final DataModel persistentInputDataModel = replaceDataModel(persistentDataModels, existingInputDataModel);
		project.setInputDataModel(persistentInputDataModel);

		final DataModel existingOutputDataModel = project.getOutputDataModel();
		final DataModel persistentOutputDataModel = replaceDataModel(persistentDataModels, existingOutputDataModel);
		project.setOutputDataModel(persistentOutputDataModel);
	}

	private DataModel replaceDataModel(final Map<String, DataModel> persistentDataModels, final DataModel existingDataModel)
			throws DMPPersistenceException {

		if (existingDataModel == null) {

			return null;
		}

		final String existingDataModelUuid = existingDataModel.getUuid();

		final DataModel persistentDataModel;

		if (persistentDataModels.containsKey(existingDataModelUuid)) {

			// take data model from persistent data models cache

			persistentDataModel = persistentDataModels.get(existingDataModelUuid);
		} else {

			// try to retrieve (inbuilt) data model from metadata repository

			final DataModelService dataModelService = dataModelPersistenceServiceProvider.get();

			persistentDataModel = dataModelService.getObject(existingDataModelUuid);

			if (persistentDataModel == null) {

				// TODO: what should we do with non-existing data models in projects?

				final String message = String
						.format("could not find data model '%s' in the persistent data models map nor in the metadata repository",
								existingDataModelUuid);

				LOG.error(message);

				throw new DMPPersistenceException(message);
			}
		}

		return persistentDataModel;
	}

	/**
	 * replace attribute paths of MAPIs with persistent ones (we also fetch APs from inbuilt schemata from DB)
	 *
	 * @param oldUuidNewPersistentAttributePathMap
	 * @param project
	 * @throws DMPPersistenceException
	 */
	private void replaceAttributePathsInMappings(final Map<String, AttributePath> oldUuidNewPersistentAttributePathMap, final Project project)
			throws DMPPersistenceException, JsonProcessingException {

		if (project == null) {

			return;
		}

		final Set<Mapping> existingMappings = project.getMappings();

		if (existingMappings == null) {

			return;
		}

		for (final Mapping existingMapping : existingMappings) {

			final Set<MappingAttributePathInstance> existingMappingInputAttributePaths = existingMapping.getInputAttributePaths();

			if (existingMappingInputAttributePaths != null) {

				for (final MappingAttributePathInstance existingMAPI : existingMappingInputAttributePaths) {

					replaceAttributePathInMAPI(oldUuidNewPersistentAttributePathMap, existingMAPI);
				}
			}

			final MappingAttributePathInstance existingMappingOutputAttributePath = existingMapping.getOutputAttributePath();

			replaceAttributePathInMAPI(oldUuidNewPersistentAttributePathMap, existingMappingOutputAttributePath);
		}
	}

	private void replaceAttributePathInMAPI(final Map<String, AttributePath> oldUuidNewPersistentAttributePathMap,
			final MappingAttributePathInstance mapi) throws DMPPersistenceException, JsonProcessingException {

		final AttributePath existingAttributePath = mapi.getAttributePath();
		final String existingAttributePathUuid = existingAttributePath.getUuid();

		final AttributePath persistentAttributePath;

		if (oldUuidNewPersistentAttributePathMap.containsKey(existingAttributePathUuid)) {

			// existing attribute path was replaced by a new, persistent attribute path

			persistentAttributePath = oldUuidNewPersistentAttributePathMap.get(existingAttributePathUuid);
		} else {

			// check, whether other attribute paths does really exists in the DB

			final AttributePathService attributePathService = attributePathPersistenceServiceProvider.get();

			final AttributePath attributePathFromDB = attributePathService.getObject(existingAttributePathUuid);

			if (attributePathFromDB == null) {

				// try to retrieve attribute path (e.g. from an inbuilt schema) via attribute path string from DB

				// 1. retrieve all attributes of the attribute path via URI from DB
				// 2. compose a new JSON array of the attribute path with the attribute UUIDs of the containing attributes

				final List<Attribute> attributePathAttributesList = existingAttributePath.getAttributePath();
				final String attributePathAttributesJSONArray = buildActualAttributePathJSONArray(attributePathAttributesList);

				// 3. do the request at the attribute path persistence service with the string representation of this JSON array
				final List<AttributePath> attributePathsWithPathFromDB = attributePathService.getAttributePathsWithPath(
						attributePathAttributesJSONArray);

				if (attributePathsWithPathFromDB == null || attributePathsWithPathFromDB.isEmpty()) {

					// TODO: how should we deal with attribute paths that do not really exist anymore in their schemata?

					final String message = String
							.format("could not find attribute path '%s' ('%s') in the persistent attribute paths map nor in the metadata repository",
									existingAttributePathUuid, existingAttributePath.toAttributePath());

					LOG.error(message);

					throw new DMPPersistenceException(message);
				}

				// result list should always be of size 1
				final AttributePath attributePathWithPathFromDB = attributePathsWithPathFromDB.get(0);

				oldUuidNewPersistentAttributePathMap.put(existingAttributePathUuid, attributePathWithPathFromDB);

				persistentAttributePath = attributePathWithPathFromDB;
			} else {

				persistentAttributePath = attributePathFromDB;
			}
		}

		mapi.setAttributePath(persistentAttributePath);
	}

	/**
	 * replace functions with persistent ones (i.e. those that are at a project and those that are at a mapping)
	 *
	 * @param project
	 */
	private void replaceFunctions(final Project project) throws DMPPersistenceException {

		final Set<Function> existingProjectFunctions = project.getFunctions();

		// replace project functions
		if (existingProjectFunctions != null) {

			final Set<Function> persistentProjectFunctions = new LinkedHashSet<>();

			for (final Function existingProjectFunction : existingProjectFunctions) {

				final Function persistentProjectFunction = replaceFunction(existingProjectFunction);

				persistentProjectFunctions.add(persistentProjectFunction);
			}

			project.setFunctions(persistentProjectFunctions);
		}

		final Set<Mapping> existingProjectMappings = project.getMappings();

		// replace mapping functions
		if (existingProjectMappings != null) {

			for (final Mapping existingProjectMapping : existingProjectMappings) {

				final Component existingProjectMappingTransformationComponent = existingProjectMapping.getTransformation();

				if (existingProjectMappingTransformationComponent != null) {

					replaceFunctionsInComponent(existingProjectMappingTransformationComponent);
				}
			}
		}
	}

	private Function replaceFunction(final Function existingFunction) throws DMPPersistenceException {

		if (existingFunction == null) {

			return null;
		}

		final String existingFunctionUuid = existingFunction.getUuid();

		if (!persistentFunctionsCache.containsKey(existingFunctionUuid)) {

			final FunctionService functionService = functionPersistenceServiceProvider.get();

			final Function persistentFunction = functionService.getObject(existingFunctionUuid);

			if (persistentFunction == null) {

				final String message = String.format("could not find function '%s' in the persistent functions cache nor in the metadata repository",
						existingFunctionUuid);

				LOG.error(message);

				throw new DMPPersistenceException(message);
			}

			persistentFunctionsCache.put(existingFunctionUuid, persistentFunction);
		}

		return persistentFunctionsCache.get(existingFunctionUuid);
	}

	private void replaceFunctionsInComponent(final Component component) throws DMPPersistenceException {

		if (component == null) {

			return;
		}

		final Function existingComponentFunction = component.getFunction();

		final FunctionType componentFunctionFunctionType = existingComponentFunction.getFunctionType();

		switch (componentFunctionFunctionType) {

			case Function:

				final Function persistentFunction = replaceFunction(existingComponentFunction);

				component.setFunction(persistentFunction);

				break;
			case Transformation:

				replaceFunctionsInTransformation((Transformation) existingComponentFunction);

				break;
		}
	}

	private void replaceFunctionsInTransformation(final Transformation transformation) throws DMPPersistenceException {

		if (transformation == null) {

			return;
		}

		final Set<Component> transformationComponents = transformation.getComponents();

		if (transformationComponents == null) {

			return;
		}

		for (final Component transformationComponent : transformationComponents) {

			replaceFunctionsInComponent(transformationComponent);
		}
	}

	private String buildActualAttributePathJSONArray(final List<Attribute> attributePathAttributesList)
			throws DMPPersistenceException, JsonProcessingException {

		final AttributeService attributeService = attributePersistenceServiceProvider.get();
		final ArrayNode attributePathAttributesJSONArray = mapper.createArrayNode();

		for (final Attribute attribute : attributePathAttributesList) {

			final String attributeUri = attribute.getUri();

			if (!persistentAttributesCache.containsKey(attributeUri)) {

				final Attribute persistentAttribute = attributeService.getObjectByUri(attributeUri);

				if (persistentAttribute == null) {

					final String message = String.format("could not find an attribute for uri '%s' in the metadata repository", attributeUri);

					LOG.error(message);

					throw new DMPPersistenceException(message);
				}

				persistentAttributesCache.put(attributeUri, persistentAttribute);
			}

			final Attribute persistentAttribute = persistentAttributesCache.get(attributeUri);

			final String persistentAttributeUuid = persistentAttribute.getUuid();

			attributePathAttributesJSONArray.add(persistentAttributeUuid);
		}

		return attributePathAttributesJSONArray.toString();
	}

	private class SchemaRecreationResultSet {

		private final Map<String, Schema> oldUuidNewPersistentSchemaMap;

		private final Map<String, AttributePath> oldUuidNewPersistentAttributePathMap;

		private SchemaRecreationResultSet(final Map<String, Schema> oldUuidNewPersistentSchemaMap,
				final Map<String, AttributePath> oldUuidNewPersistentAttributePathMap) {

			this.oldUuidNewPersistentSchemaMap = oldUuidNewPersistentSchemaMap;
			this.oldUuidNewPersistentAttributePathMap = oldUuidNewPersistentAttributePathMap;
		}

		public Map<String, Schema> getOldUuidNewPersistentSchemaMap() {

			return oldUuidNewPersistentSchemaMap;
		}

		public Map<String, AttributePath> getOldUuidNewPersistentAttributePathMap() {

			return oldUuidNewPersistentAttributePathMap;
		}
	}

	public static void main(final String[] args) {

		final Injector injector = getInjector();

		injector.getInstance(PersistService.class).start();
		injector.getInstance(ExecutionScope.class).enter();

		final MetadataRepositoryMigrator metadataRepositoryMigrator = injector.getInstance(MetadataRepositoryMigrator.class);

		try {

			metadataRepositoryMigrator.migrateData();
		} catch (final IOException | DMPPersistenceException e) {

			final String message = "could not migrate metadata repository state successfully";

			LOG.error(message, e);

			throw new RuntimeException(message, e);
		}
	}

	private static Injector getInjector() {

		final ConfigModule configModule = new ConfigModule();
		final Config config = configModule.getConfig();
		LoggingConfigurator.configureFrom(config);

		return Guice.createInjector(
				configModule,
				new PersistenceModule(),
				new JacksonObjectMapperModule(),
				new JpaHibernateModule(config)
		);
	}
}
