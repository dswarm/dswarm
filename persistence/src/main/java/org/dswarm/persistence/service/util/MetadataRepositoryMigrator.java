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
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.common.types.Tuple;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.proxy.ProxyDMPObject;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.BasicJPAService;
import org.dswarm.persistence.service.MaintainDBService;
import org.dswarm.persistence.service.PersistenceType;
import org.dswarm.persistence.service.job.ProjectService;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.resource.ResourceService;
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

	private final Provider<ResourceService>      resourcePersistenceServiceProvider;
	private final Provider<ConfigurationService> configurationPersistenceServiceProvider;
	private final Provider<SchemaService>        schemaPersistenceServiceProvider;
	private final Provider<DataModelService>     dataModelPersistenceServiceProvider;
	private final Provider<ProjectService>       projectPersistenceServiceProvider;
	private final Provider<MaintainDBService>    maintainDBServiceProvider;

	private static final JaxbAnnotationModule module = new JaxbAnnotationModule();
	private static final ObjectMapper         MAPPER = new ObjectMapper()
			.registerModule(module)
			.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
			.setSerializationInclusion(JsonInclude.Include.NON_NULL)
			.configure(SerializationFeature.INDENT_OUTPUT, true);

	@Inject
	public MetadataRepositoryMigrator(final Provider<ResourceService> resourcePersistenceServiceProviderArg,
			final Provider<ConfigurationService> configurationPersistenceServiceProviderArg,
			final Provider<SchemaService> schemaPersistenceServiceProviderArg,
			final Provider<DataModelService> dataModelPersistenceServiceProviderArg,
			final Provider<ProjectService> projectPersistenceServiceProviderArg,
			final Provider<MaintainDBService> maintainDBServiceProvierArg) {

		resourcePersistenceServiceProvider = resourcePersistenceServiceProviderArg;
		configurationPersistenceServiceProvider = configurationPersistenceServiceProviderArg;
		schemaPersistenceServiceProvider = schemaPersistenceServiceProviderArg;
		dataModelPersistenceServiceProvider = dataModelPersistenceServiceProviderArg;
		projectPersistenceServiceProvider = projectPersistenceServiceProviderArg;
		maintainDBServiceProvider = maintainDBServiceProvierArg;
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

		return dumpEntities(resourcePersistenceServiceProvider, RESOURCES_FILE_NAME);
	}

	private Tuple<String, String> dumpConfigurations() throws IOException {

		return dumpEntities(configurationPersistenceServiceProvider, CONFIGURATIONS_FILE_NAME);
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
		recreateExistingSchemas(schemasDumpFileName);
		recreateExistingDataModels(dataModelsDumpFileName);
		recreateExistingProjects(projectsDumpFileName);
	}

	/**
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

		}, Resource.class.getName());

		final Collection<Resource> modifiedResources = new ArrayList<>();
		final Map<String, Collection<Configuration>> resourcesConfigurations = new LinkedHashMap<>();

		// remove related configurations from resources
		for (final Resource existingResource : existingResources) {

			final Set<Configuration> configurations = existingResource.getConfigurations();

			if(configurations != null) {

				final Set<Configuration> configurationsCopy = new LinkedHashSet<>();
				configurationsCopy.addAll(configurations);

				existingResource.setConfigurations(null);

				final String existingResourceUuid = existingResource.getUuid();
				resourcesConfigurations.put(existingResourceUuid, new ArrayList<>());

				for (final Configuration configuration : configurationsCopy) {

					final String configurationUuid = configuration.getUuid();
					final Configuration persistentConfiguration = persistentConfigurations.get(configurationUuid);

					if(persistentConfiguration != null) {

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

		System.out.println("here I am");

		final Collection<Resource> updatedResources = new ArrayList<>();

		// add related configurations to persistent resources
		for(final Map.Entry<String, Resource> persistentResourceEnty : persistentResources.entrySet()) {

			final String persistentResourceUuid = persistentResourceEnty.getKey();
			final Resource persistentResource = persistentResourceEnty.getValue();

			final Collection<Configuration> resourceConfigurations = resourcesConfigurations.get(persistentResourceUuid);

			if(resourceConfigurations == null) {

				continue;
			}

			for(final Configuration resourceConfiguration : resourceConfigurations) {

				persistentResource.addConfiguration(resourceConfiguration);
			}

			updatedResources.add(persistentResource);
		}

		// updated resources with related configurations
		final Map<String, Resource> updatedPersistentResources = updateEntities(resourcePersistenceServiceProvider, updatedResources);

		System.out.println("here I am");

		return updatedPersistentResources;
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

		}, Configuration.class.getName());

		final Collection<Configuration> modifiedConfigurations = new ArrayList<>();

		for (final Configuration existingConfiguration : existingConfigurations) {

			existingConfiguration.setResources(null);

			modifiedConfigurations.add(existingConfiguration);
		}

		final Map<String, Configuration> persistentConfigurations = recreateEntities(configurationPersistenceServiceProvider, modifiedConfigurations);

		System.out.println("here I am");

		return persistentConfigurations;
	}

	private void recreateExistingSchemas(final String filePath) throws IOException {

		final Collection<Schema> existingSchemas = deserializeEntities(filePath, new TypeReference<ArrayList<Schema>>() {

		}, Schema.class.getName());

		System.out.println("here I am");
	}

	private void recreateExistingDataModels(final String filePath) throws IOException {

		final Collection<DataModel> existingDataModels = deserializeEntities(filePath, new TypeReference<ArrayList<DataModel>>() {

		}, DataModel.class.getName());

		System.out.println("here I am");
	}

	private void recreateExistingProjects(final String filePath) throws IOException {

		final Collection<Project> existingProjects = deserializeEntities(filePath, new TypeReference<ArrayList<Project>>() {

		}, Project.class.getName());

		System.out.println("here I am");
	}

	private <PERSISTENCE_SERVICE extends BasicJPAService> Tuple<String, String> dumpEntities(
			final Provider<PERSISTENCE_SERVICE> persistenceServiceProvider, final String fileName)
			throws IOException {

		final PERSISTENCE_SERVICE persistenceService = persistenceServiceProvider.get();

		final Class clasz = persistenceService.getClasz();
		final String claszName = clasz.getName();

		LOG.debug("try to dump {}s", claszName);

		final List entities = persistenceService.getObjects();

		LOG.debug("retrieved '{}' {}s", entities.size(), claszName);

		final String entitiesJSONString = MAPPER.writeValueAsString(entities);
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

	private static <POJOCLASS extends DMPObject, T extends ArrayList<POJOCLASS>> Collection<POJOCLASS> deserializeEntities(final String filePath,
			final TypeReference<T> typeReference, final String entityName) throws IOException {

		LOG.debug("try to deserialize some {}s", entityName);

		final Path path = Paths.get(filePath);
		final URI uri = path.toUri();
		final File file = new File(uri);

		final Collection<POJOCLASS> deserializedEntities = MAPPER.readValue(file, typeReference);

		LOG.debug("deserialized '{}' {}s", deserializedEntities.size(), entityName);

		return deserializedEntities;
	}

	private <PERSISTENCE_SERVICE extends BasicJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyDMPObject<POJOCLASS>, POJOCLASS extends DMPObject> Map<String, POJOCLASS> recreateEntities(
			final Provider<PERSISTENCE_SERVICE> persistenceServiceProvider, final Collection<POJOCLASS> entities) throws DMPPersistenceException {

		final PERSISTENCE_SERVICE persistenceService = persistenceServiceProvider.get();
		final Class<POJOCLASS> clasz = persistenceService.getClasz();
		final String claszName = clasz.getName();

		LOG.debug("try to re-create '{}' {}s", entities.size(), claszName);

		final Map<String, POJOCLASS> persistentEntities = new LinkedHashMap<>();

		for (final POJOCLASS entity : entities) {

			final PROXYPOJOCLASS proxyPersistentEntity = persistenceService.createObjectTransactional(entity, PersistenceType.Persist);
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
}
