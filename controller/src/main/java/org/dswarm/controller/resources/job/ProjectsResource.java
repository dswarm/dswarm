/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.controller.resources.job;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.common.types.Tuple;
import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.ExtendedBasicDMPResource;
import org.dswarm.controller.resources.POJOFormat;
import org.dswarm.controller.utils.JsonUtils;
import org.dswarm.init.DMPException;
import org.dswarm.persistence.DMPPersistenceError;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.FunctionType;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.job.proxy.ProxyProject;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.AttributePathInstance;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePath;
import org.dswarm.persistence.model.schema.proxy.ProxyClasz;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.job.ProjectService;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * A resource (controller service) for {@link Project}s.
 *
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/projects", description = "Operations about projects.")
@Path("projects")
public class ProjectsResource extends ExtendedBasicDMPResource<ProjectService, ProxyProject, Project> {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectsResource.class);

	public static final String INPUT_DATA_MODEL = "input_data_model";
	public static final String REFERENCE_PROJECT = "reference_project";

	private static final Comparator<String> STRING_LENGTH_COMPARATOR;
	public static final String COPY = "copy";
	public static final String MIGRATION = "migration";

	static {

		STRING_LENGTH_COMPARATOR = (o1, o2) -> {

			if (o1.length() < o2.length()) {

				return -1;
			}

			if (o1.length() == o2.length()) {

				return 0;
			}

			//if(o1.length() > o2.length())

			return 1;
		};
	}

	private final Provider<DataModelService> dataModelPersistenceServiceProvider;
	private final Provider<AttributeService> attributePersistenceServiceProvider;
	private final Provider<AttributePathService> attributePathPersistenceServiceProvider;
	private final Provider<ClaszService> classPersistenceServiceProvider;
	private final Provider<SchemaService> schemaPersistenceServiceProvider;

	/**
	 * Creates a new resource (controller service) for {@link Project}s with the provider of the project persistence service, the
	 * object mapper and metrics registry.
	 *
	 * @param persistenceServiceProviderArg
	 * @param objectMapperProviderArg
	 */
	@Inject
	public ProjectsResource(final Provider<ProjectService> persistenceServiceProviderArg,
	                        final Provider<DataModelService> dataModelPersistenceServiceProviderArg,
	                        final Provider<AttributeService> attributePersistenceServiceProviderArg,
	                        final Provider<AttributePathService> attributePathPersistenceServiceProviderArg,
	                        final Provider<ClaszService> classPersistenceServiceProviderArg,
	                        final Provider<SchemaService> schemaPersistenceServiceProviderArg,
	                        final Provider<ObjectMapper> objectMapperProviderArg) throws DMPControllerException {

		super(Project.class, persistenceServiceProviderArg, objectMapperProviderArg);

		dataModelPersistenceServiceProvider = dataModelPersistenceServiceProviderArg;
		attributePersistenceServiceProvider = attributePersistenceServiceProviderArg;
		attributePathPersistenceServiceProvider = attributePathPersistenceServiceProviderArg;
		classPersistenceServiceProvider = classPersistenceServiceProviderArg;
		schemaPersistenceServiceProvider = schemaPersistenceServiceProviderArg;
	}

	/**
	 * This endpoint returns a project as JSON representation for the provided project identifier.
	 *
	 * @param id a project identifier
	 * @return a JSON representation of a project
	 */
	@ApiOperation(value = "get the project that matches the given id", notes = "Returns the Project object that matches the given id.")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "returns the project (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find a project for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)")})
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(
			@ApiParam(value = "project identifier", required = true) @PathParam("id") final String id,
			@ApiParam(value = "'short' for only uuid,name,description, 'full' for the complete entity")
			@QueryParam("format") @DefaultValue("full") final POJOFormat format)
			throws DMPControllerException {

		return super.getObject(id, format);
	}

	/**
	 * This endpoint consumes a project as JSON representation and persists this project (incl. all its parts, i.e., new sub
	 * elements, e.g., mappings will be persisted as well) in the database. <br/>
	 * Note: please utilise generated uuids for all entity identifier.
	 *
	 * @param jsonObjectString a JSON representation of one project
	 * @return the persisted project as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new project", notes = "Returns a new Project object. Persists this project (incl. all its parts, i.e., new sub elements, e.g., mappings will be persisted as well). Note: please utilise generated uuids for all entity identifier.", response = Project.class)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "project was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)")})
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "project (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint consumes a project as JSON representation and persists this project (incl. all its parts, i.e., new sub
	 * elements, e.g., mappings will be persisted as well) robust in the database, i.e., attributes and attribute paths will be written beforehand to guarantee entity identifier compatibility.<br/>
	 * Note: please utilise generated uuids for all entity identifier.
	 *
	 * @param jsonObjectString a JSON representation of one project
	 * @return the persisted project as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new project robust", notes = "Returns a new Project object. Persists this project (incl. all its parts, i.e., new sub elements, e.g., mappings will be persisted as well) robust, i.e., attributes and attribute paths will be written beforehand to guarantee entity identifier compatibility. Note: please utilise generated uuids for all entity identifier.", response = Project.class)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "project was successfully persisted robust"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)")})
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/robust")
	public Response createObjectRobust(@ApiParam(value = "project (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		final Project projectFromJSON = deserializeObjectJSONString(jsonObjectString);

		final Optional<Schema> optionalPersistentInputSchema = persistInputSchema(projectFromJSON);
		final Optional<Schema> optionalPersistentOutputSchema = persistOutputSchema(projectFromJSON);

		if (optionalPersistentInputSchema.isPresent()) {

			final Schema inputSchema = optionalPersistentInputSchema.get();

			projectFromJSON.getInputDataModel().setSchema(inputSchema);

			replaceMappingInputs(projectFromJSON, inputSchema);
		}

		if (optionalPersistentOutputSchema.isPresent()) {

			final Schema outputSchema = optionalPersistentOutputSchema.get();

			projectFromJSON.getOutputDataModel().setSchema(outputSchema);

			replaceMappingOutputs(projectFromJSON, outputSchema);
		}

		return super.createObject(projectFromJSON);
	}

	/**
	 * This endpoint returns a list of all projects as JSON representation.
	 *
	 * @return a list of all projects as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all projects ", notes = "Returns a list of Project objects.")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "returns all available projects (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any project, i.e., there are no projects available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)")})

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects(
			@ApiParam(value = "'short' for only uuid,name,description, 'full' for the complete entity")
			@QueryParam("format") @DefaultValue("full") final POJOFormat format) throws DMPControllerException {

		return super.getObjects(format);
	}

	/**
	 * This endpoint consumes a project as JSON representation and updates this project in the database.
	 *
	 * @param jsonObjectString a JSON representation of one project
	 * @param uuid             a project identifier
	 * @return the updated project as JSON representation
	 * @throws DMPControllerException
	 */
	@Override
	@ApiOperation(value = "update project with given id ", notes = "Returns an updated Project object.")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "project was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a project for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)")})
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "project (as JSON)", required = true) final String jsonObjectString,
	                             @ApiParam(value = "project identifier", required = true) @PathParam("id") final String uuid) throws DMPControllerException {

		return super.updateObject(jsonObjectString, uuid);
	}

	/**
	 * This endpoint deletes a project that matches the given id.
	 *
	 * @param id a project identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 * went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "delete project that matches the given id", notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = {@ApiResponse(code = 204, message = "project was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a project for the given id"),
			@ApiResponse(code = 409, message = "project couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)")})
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "project identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}

	/**
	 * This endpoint consumes a request JSON to create a new project with help of an existing project and a new input data model, whose schema is very similar to the schema of the existing input data model e.g. OAI-PMH+DCE+Europeana schema generated by example to inbuilt OAI-PMH+DCE+Europeana schema.<br/>
	 * The request parameters can be:<br/>
	 * - "input_data_model": the new input data model (incl. an input schema that is very similar to the one utilised in the existing project)<br/>
	 * - "reference_project": the reference project (where the mappings and the output data model will be taken from)<br/>
	 * The result should be a new project with copies of the mappings (from the existing project) that are aligned to the new input schema.<br/>
	 * This endpoint can only be utilised, when the input attribute paths also exist in the new input schema, i.e., it's not suitable for migrating to an input schema with different (but similar) attribute paths (e.g. from OAI-PMH+MARCXML to MARCXML). Please utilise the "migrateprojecttonewinputschema" endpoint for such migration tasks.
	 *
	 * @param requestJsonObjectString a JSON representation of the request parameters
	 * @return the persisted project as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new project by copying parts from existing project and utilising other existing entities", notes = "Returns a new Project object. Persists this project (incl. all its parts, i.e., new sub elements, e.g., mappings will be persisted as well).", response = Project.class)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "project was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)")})
	@POST
	@Path("/createprojectwithhelpofexistingentities")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createProjectWithHelpOfExistingEntities(
			@ApiParam(value = "project (as JSON)", required = true) final String requestJsonObjectString) throws DMPControllerException {

		LOG.debug("try to create new project (copy mappings) with help of existing entities");

		final Tuple<DataModel, Project> migrationInput = getMigrationInput(requestJsonObjectString);
		final DataModel inputDataModel = migrationInput.v1();
		final Project referenceProject = migrationInput.v2();

		LOG.debug("try to create new project (copy mappings) with help of input data model '{}' and reference project '{}", inputDataModel.getUuid(), referenceProject.getUuid());

		final String newProjectId = UUIDService.getUUID(Project.class.getSimpleName());
		final Project newProject = createNewProjectForMigration(inputDataModel, referenceProject, newProjectId, COPY);

		migrateMappingsToVerySimilarInputAttributePaths(referenceProject, newProject);

		LOG.debug("successfully created new project '{}' ('{}') incl. copied mappings with help of input data model '{}' and reference project '{}", newProject.getUuid(), newProject.getName(), inputDataModel.getUuid(), referenceProject.getUuid());

		return persistProjectForMigration(newProjectId, newProject);
	}

	/**
	 * This endpoint consumes a request JSON to create a new project with help of an existing project and a new input data model, whose schema is somehow similar to the schema of the existing input data model.<br/>
	 * The request parameters can be:<br/>
	 * - "input_data_model": the new input data model (incl. an input schema that is somehow similar to the one utilised in the existing project)<br/>
	 * - "reference_project": the reference project (where the mappings and the output data model will be taken from)<br/>
	 * The result should be a new project with copies of the mappings (from the existing project) that are aligned to the new input schema.<br/>
	 * This endpoint is suitable for migrating to an input schema with different (but similar) attribute paths (e.g. from OAI-PMH+MARCXML to MARCXML). Please utilise the "createprojectwithhelpofexistingentities" endpoint for migrating projects to an input schema that is very similar to the input schema of the existing project (e.g. OAI-PMH+DCE+Europeana schema generated by example to inbuilt OAI-PMH+DCE+Europeana schema).
	 *
	 * @param requestJsonObjectString a JSON representation of the request parameters
	 * @return the persisted project as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "migrates a project by applying a similar input schema to the mappings", notes = "Returns a new Project object. Persists this project (incl. all its parts, i.e., new sub elements, e.g., mappings will be persisted as well).", response = Project.class)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "project was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)")})
	@POST
	@Path("migrateprojecttonewinputschema")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response migrateProjectToNewInputSchema(
			@ApiParam(value = "project (as JSON)", required = true) final String requestJsonObjectString) throws DMPControllerException {

		LOG.debug("try to create new project (migrate mappings) with help of existing entities");

		final Tuple<DataModel, Project> migrationInput = getMigrationInput(requestJsonObjectString);
		final DataModel inputDataModel = migrationInput.v1();
		final Project referenceProject = migrationInput.v2();

		LOG.debug("try to create new project (migrate mappings) with help of input data model '{}' and reference project '{}", inputDataModel.getUuid(), referenceProject.getUuid());

		final String newProjectId = UUIDService.getUUID(Project.class.getSimpleName());
		final Project newProject = createNewProjectForMigration(inputDataModel, referenceProject, newProjectId, MIGRATION);

		final Schema referenceInputSchema = referenceProject.getInputDataModel().getSchema();
		final Schema newInputSchema = inputDataModel.getSchema();

		final Map<AttributePath, AttributePath> attributePathMap = mapAttributePaths(referenceInputSchema, newInputSchema);

		migrateMappingsToSomehowSimilarInputAttributePaths(referenceProject, newProject, attributePathMap);

		LOG.debug("successfully created new project '{}' ('{}') incl. migrated mappings with help of input data model '{}' and reference project '{}", newProject.getUuid(), newProject.getName(), inputDataModel.getUuid(), referenceProject.getUuid());

		return persistProjectForMigration(newProjectId, newProject);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, description, (sample) input data model, output data model, mappings and functions of the project.
	 */
	@Override
	protected Project prepareObjectForUpdate(final Project objectFromJSON, final Project object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setFunctions(objectFromJSON.getFunctions());
		object.setInputDataModel(objectFromJSON.getInputDataModel());
		object.setOutputDataModel(objectFromJSON.getOutputDataModel());
		object.setSkipFilter(objectFromJSON.getSkipFilter());
		object.setMappings(objectFromJSON.getMappings());
		object.setSelectedRecords(objectFromJSON.getSelectedRecords());

		return object;
	}

	private Optional<DataModel> getDataModel(final String dataModelId) {

		final DataModelService dataModelService = dataModelPersistenceServiceProvider.get();

		final DataModel dataModel = dataModelService.getObject(dataModelId);

		return Optional.ofNullable(dataModel);
	}

	private Optional<Project> getProject(final String projectId) {

		final ProjectService projectService = persistenceServiceProvider.get();

		final Project project = projectService.getObject(projectId);

		return Optional.ofNullable(project);
	}

	private Tuple<DataModel, Project> getMigrationInput(final String requestJsonObjectString) throws DMPControllerException {

		if (requestJsonObjectString == null) {

			throw new DMPControllerException(
					"Cannot create project with help of existing entities. The request JSON string does not exist. Please define one");
		}

		final ObjectNode requestJSON;

		try {

			requestJSON = DMPPersistenceUtil.getJSON(requestJsonObjectString);
		} catch (final DMPException e) {

			throw new DMPControllerException(
					"Cannot create project with help of existing entities. Could not deserialize request JSON string. Please define a valid one.", e);
		}

		if (requestJSON == null) {

			throw new DMPControllerException(
					"Cannot create project with help of existing entities. The request JSON object does not exist. Please define one");
		}

		final Optional<String> optionalInputDataModelId = JsonUtils.getStringValue(INPUT_DATA_MODEL, requestJSON);
		final Optional<String> optionalReferenceProjectId = JsonUtils.getStringValue(REFERENCE_PROJECT, requestJSON);

		final Optional<DataModel> optionalInputDataModel = optionalInputDataModelId.flatMap(this::getDataModel);
		final Optional<Project> optionalReferenceProject = optionalReferenceProjectId.flatMap(this::getProject);

		if (!optionalReferenceProject.isPresent()) {

			final String startMessage = "Cannot create project with help of existing entities.";
			final String finalMessage;

			if (optionalReferenceProjectId.isPresent()) {

				final String referenceProjectId = optionalReferenceProjectId.get();

				final String message = String
						.format("Could not retrieve reference project with id '%s' from metadata repository.", referenceProjectId);

				finalMessage = startMessage + message;
			} else {

				final String message2 = "Could not retrieve reference project, because no project id was given in the request JSON";

				finalMessage = startMessage + message2;
			}

			LOG.error(finalMessage);

			throw new DMPControllerException(finalMessage);
		}

		final Project referenceProject = optionalReferenceProject.get();

		final Optional<DataModel> optionalFinalInputDataModel = Optional
				.ofNullable(optionalInputDataModel.orElseGet(referenceProject::getInputDataModel));

		if (!optionalFinalInputDataModel.isPresent()) {

			final String message = "Cannot create project with help of existing entities. Could not determine an input data model that should be utilised for project creation";

			LOG.error(message);

			throw new DMPControllerException(message);
		}

		final DataModel inputDataModel = optionalFinalInputDataModel.get();

		return Tuple.tuple(inputDataModel, referenceProject);
	}

	private static Project createNewProjectForMigration(final DataModel inputDataModel, final Project referenceProject, final String newProjectId, final String type) {

		// create new project
		final Project newProject = new Project(newProjectId);
		newProject.setName(type + " of '" + referenceProject.getName() + "'");
		newProject.setDescription(type + " of '" + referenceProject.getDescription() + "'");
		newProject.setInputDataModel(inputDataModel);
		// TODO: maybe check output data model?
		newProject.setOutputDataModel(referenceProject.getOutputDataModel());
		newProject.setFunctions(referenceProject.getFunctions());

		return newProject;
	}

	/**
	 * Migrates mappings to new ones with very similar input attribute paths (copy).
	 *
	 * @param referenceProject
	 * @param newProject
	 */
	private static void migrateMappingsToVerySimilarInputAttributePaths(final Project referenceProject, final Project newProject) {

		final Set<Mapping> referenceMappings = referenceProject.getMappings();

		if (referenceMappings == null) {

			return;
		}

		final Set<Mapping> newMappings = new LinkedHashSet<>();

		for (final Mapping referenceMapping : referenceMappings) {

			final String newMappingId = UUIDService.getUUID(Mapping.class.getSimpleName());

			final Mapping newMapping = new Mapping(newMappingId);

			newMapping.setName(referenceMapping.getName());
			// TODO: do we need to check attribute paths by their string representation?
			newMapping.setInputAttributePaths(referenceMapping.getInputAttributePaths());
			newMapping.setOutputAttributePath(referenceMapping.getOutputAttributePath());

			final Component referenceMappingTransformationComponent = referenceMapping.getTransformation();
			createCopyOfComponent(referenceMappingTransformationComponent).ifPresent(referenceAndNewComponentTuple -> newMapping.setTransformation(referenceAndNewComponentTuple.v2()));

			newMappings.add(newMapping);
		}

		newProject.setMappings(newMappings);
	}

	/**
	 * Maps attribute paths from a reference schema to matching attribute paths from a new schema. Returns a map of attribute paths from the reference schema to matching attribute paths from the new schema.
	 *
	 * @param referenceSchema the reference schema
	 * @param newSchema       the new schema
	 * @return a map of attribute paths from the reference schema to matching attribute paths from the new schema
	 */
	private static Map<AttributePath, AttributePath> mapAttributePaths(final Schema referenceSchema, final Schema newSchema)
			throws DMPControllerException {

		if (referenceSchema == null) {

			final String message = "Reference schema is not available; cannot map attribute paths from reference schema to new schema";

			LOG.error(message);

			throw new DMPControllerException(message);
		}

		if (newSchema == null) {

			final String message = "New schema is not available; cannot map attribute paths from reference schema to new schema";

			LOG.error(message);

			throw new DMPControllerException(message);
		}

		final String referenceSchemaUuid = referenceSchema.getUuid();
		final String newSchemaUuid = newSchema.getUuid();

		final Set<SchemaAttributePathInstance> uniqueReferenceSAPIs = referenceSchema.getUniqueAttributePaths();
		final Set<SchemaAttributePathInstance> uniqueNewSAPIs = newSchema.getUniqueAttributePaths();

		if (uniqueReferenceSAPIs == null || uniqueReferenceSAPIs.isEmpty()) {

			final String message = String
					.format("There are no reference schema attribute path instances; cannot map attribute paths from reference schema '%s' to new schema '%s'.",
							referenceSchemaUuid, newSchemaUuid);

			LOG.error(message);

			throw new DMPControllerException(message);
		}

		if (uniqueNewSAPIs == null || uniqueNewSAPIs.isEmpty()) {

			final String message = String
					.format("There are no new schema attribute path instances; cannot map attribute paths from reference schema '%s' to new schema '%s'.",
							referenceSchemaUuid, newSchemaUuid);

			LOG.error(message);

			throw new DMPControllerException(message);
		}

		final Map<String, AttributePath> referenceAPs = uniqueReferenceSAPIs.parallelStream()
				.collect(Collectors.toMap(uniqueReferenceSAPI -> uniqueReferenceSAPI.getAttributePath().toAttributePath(),
						AttributePathInstance::getAttributePath));

		final Map<String, AttributePath> newAPs = uniqueNewSAPIs.parallelStream()
				.collect(Collectors.toMap(uniqueNewSAPI -> uniqueNewSAPI.getAttributePath().toAttributePath(),
						AttributePathInstance::getAttributePath));

		final Set<String> newAPStrings = newAPs.keySet();

		final Map<AttributePath, AttributePath> attributePathMap = new ConcurrentHashMap<>();

		referenceAPs.forEach((referenceAPString, referenceAP) -> {

			final Collection<String> matchedNewAttributePaths = matchAttributePaths(referenceAPString, newAPStrings);

			final String matchedNewAPString;

			if (matchedNewAttributePaths.size() == 1) {

				// should be one match exactly
				matchedNewAPString = matchedNewAttributePaths.iterator().next();
			} else if (matchedNewAttributePaths.isEmpty()) {

				// try it the other way around, i.e., scan reference APs with most similar match from new APs (requires "most-similar-match" determination first)
				// might be the case, when reference AP is longer than new APs

				// 1. determine candidates for "most-similar-match" from new APs
				final Collection<String> mostSimilarMatchCandidates = matchAttributePaths2(referenceAPString, newAPStrings);

				// 2. determine longest match
				final Optional<String> optionalMostSimilarAP = determineLongestMatchedAttributePath(mostSimilarMatchCandidates);

				if (!optionalMostSimilarAP.isPresent()) {

					// TODO: no match - > what should we here???

					LOG.debug(
							"couldn't determine the most similar new attribute path from candidates (site = '{}') for reference attribute path '{}'",
							mostSimilarMatchCandidates.size(), referenceAPString);

					return;
				}

				matchedNewAPString = optionalMostSimilarAP.get();
			} else {

				// matchedNewAttributePaths.size() > 1

				LOG.debug("found multiple matches ('{}') in new attribute paths for reference attribute path '{}'; try to determine shortest match",
						matchedNewAttributePaths.size(), referenceAPString);

				final Optional<String> optionalMostSimilarAP = determineShortestMatchedAttributePath(matchedNewAttributePaths);

				if (!optionalMostSimilarAP.isPresent()) {

					LOG.debug("couldn't determine shortest match for in matched new attribute paths (size = '{}') for reference attribute path '{}'",
							matchedNewAttributePaths.size(), referenceAPString);
				}

				matchedNewAPString = optionalMostSimilarAP.get();
			}

			final AttributePath matchedNewAttributePath = newAPs.get(matchedNewAPString);

			attributePathMap.put(referenceAP, matchedNewAttributePath);
		});

		// TODO: align result set to smaller collection (this could be the reference attribute paths collection or the new attribute paths collection), otherwise the relationship is not bi-unique (1:1)

		return attributePathMap;
	}

	private static Collection<String> matchAttributePaths(final String inputAttributePath, final Collection<String> haystack) {

		return haystack.parallelStream().filter(hayStackAP -> hayStackAP.endsWith(inputAttributePath)).collect(Collectors.toList());
	}

	private static Collection<String> matchAttributePaths2(final String inputAttributePath, final Collection<String> haystack) {

		return haystack.parallelStream().filter(inputAttributePath::endsWith).collect(Collectors.toList());
	}

	private static Optional<String> determineLongestMatchedAttributePath(final Collection<String> inputCollection) {

		return inputCollection.parallelStream().max(STRING_LENGTH_COMPARATOR);
	}

	private static Optional<String> determineShortestMatchedAttributePath(final Collection<String> inputCollection) {

		return inputCollection.parallelStream().min(STRING_LENGTH_COMPARATOR);
	}

	/**
	 * Migrates mappings to new ones with somehow similar input attribute paths.
	 *
	 * @param referenceProject
	 * @param newProject
	 */
	private void migrateMappingsToSomehowSimilarInputAttributePaths(final Project referenceProject, final Project newProject,
	                                                                final Map<AttributePath, AttributePath> attributePathMap)
			throws DMPControllerException {

		final Set<Mapping> referenceMappings = referenceProject.getMappings();

		if (referenceMappings == null) {

			return;
		}

		final Map<String, String> attributePathStringsMap = generateAttributePathStringsMap(attributePathMap);

		final Set<Mapping> newMappings = new LinkedHashSet<>();

		for (final Mapping referenceMapping : referenceMappings) {

			final String newMappingId = UUIDService.getUUID(Mapping.class.getSimpleName());

			final Mapping newMapping = new Mapping(newMappingId);

			newMapping.setName(referenceMapping.getName());

			final Set<MappingAttributePathInstance> inputMAPIs = referenceMapping.getInputAttributePaths();

			final Set<MappingAttributePathInstance> newInputMAPIs = migrateMappingInputs(inputMAPIs, attributePathMap, attributePathStringsMap);

			newMapping.setInputAttributePaths(newInputMAPIs);
			newMapping.setOutputAttributePath(referenceMapping.getOutputAttributePath());

			final Component referenceMappingTransformationComponent = referenceMapping.getTransformation();
			createCopyOfComponent(referenceMappingTransformationComponent).ifPresent(referenceAndNewComponentTuple -> newMapping.setTransformation(referenceAndNewComponentTuple.v2()));

			newMappings.add(newMapping);
		}

		newProject.setMappings(newMappings);
	}

	private static Map<String, String> generateAttributePathStringsMap(final Map<AttributePath, AttributePath> attributePathMap) {

		if (attributePathMap == null) {

			return null;
		}

		return attributePathMap.entrySet().parallelStream()
				.collect(Collectors.toMap(attributePathEntry -> attributePathEntry.getKey().toAttributePath(),
						attributePathEntry1 -> attributePathEntry1.getValue().toAttributePath()));
	}

	private Set<MappingAttributePathInstance> migrateMappingInputs(final Set<MappingAttributePathInstance> referenceInputMAPIs,
	                                                               final Map<AttributePath, AttributePath> attributePathAttributePathMap, final Map<String, String> attributePathStringsMap)
			throws DMPControllerException {

		if (referenceInputMAPIs == null) {

			return null;
		}

		final Set<MappingAttributePathInstance> newInputMAPIs = new LinkedHashSet<>();

		for (final MappingAttributePathInstance referenceInputMAPI : referenceInputMAPIs) {

			final AttributePath referenceAP = referenceInputMAPI.getAttributePath();

			final AttributePath newAttributePath = attributePathAttributePathMap.get(referenceAP);

			if (newAttributePath == null) {

				final String message = String.format("couldn't find new attribute path for reference attribute path '%s'", referenceAP.getUuid());

				LOG.error(message);

				throw new DMPControllerException(message);
			}

			final String newInputMAPIUuid = UUIDService.getUUID(MappingAttributePathInstance.class.getSimpleName());

			final MappingAttributePathInstance newInputMAPI = new MappingAttributePathInstance(newInputMAPIUuid);
			newInputMAPI.setAttributePath(newAttributePath);
			newInputMAPI.setOrdinal(referenceInputMAPI.getOrdinal());
			newInputMAPI.setName(referenceInputMAPI.getName());

			migrateMappingInputFilter(referenceInputMAPI, newInputMAPI, attributePathStringsMap);

			newInputMAPIs.add(newInputMAPI);
		}

		return newInputMAPIs;
	}

	private void migrateMappingInputFilter(final MappingAttributePathInstance referenceInputMAPI, final MappingAttributePathInstance newInputMAPI,
	                                       final Map<String, String> attributePathStringsMap)
			throws DMPControllerException {

		final Filter referenceIMAPIFilter = referenceInputMAPI.getFilter();

		if (referenceIMAPIFilter != null) {

			final String newFilterUuid = UUIDService.getUUID(Filter.class.getSimpleName());

			final Filter newFilter = new Filter(newFilterUuid);
			newFilter.setName(referenceIMAPIFilter.getName());

			final String referenceIMAPIFilterExpression = referenceIMAPIFilter.getExpression();

			if (referenceIMAPIFilterExpression != null) {

				try {

					final ArrayNode referenceIMAPIFilterExpressionArray = objectMapperProvider.get()
							.readValue(referenceIMAPIFilterExpression, ArrayNode.class);

					final ArrayNode newIMAPIFilterExpressionArray = objectMapperProvider.get().createArrayNode();

					for (final JsonNode referenceFilterExpressionNode : referenceIMAPIFilterExpressionArray) {

						final Iterator<Map.Entry<String, JsonNode>> referenceFilterExpressionIter = referenceFilterExpressionNode.fields();

						final ObjectNode newFilterExpressionNode = objectMapperProvider.get().createObjectNode();

						while (referenceFilterExpressionIter.hasNext()) {

							final Map.Entry<String, JsonNode> referenceFilterExpressionEntry = referenceFilterExpressionIter.next();
							final String referenceFilterExpressionKey = referenceFilterExpressionEntry.getKey();
							final JsonNode referenceFilterExpressionValue = referenceFilterExpressionEntry.getValue();

							final String newFilterExpressionKey = attributePathStringsMap.get(referenceFilterExpressionKey);

							if (newFilterExpressionKey == null) {

								final String message = String
										.format("couldn't find new filter expression key for reference filter expression key '%s'",
												referenceFilterExpressionKey);

								LOG.error(message);

								throw new DMPControllerException(message);
							}

							newFilterExpressionNode.set(newFilterExpressionKey, referenceFilterExpressionValue);
						}

						newIMAPIFilterExpressionArray.add(newFilterExpressionNode);
					}

					final String newIMAPIFilterExpression = objectMapperProvider.get().writeValueAsString(newIMAPIFilterExpressionArray);

					newFilter.setExpression(newIMAPIFilterExpression);
				} catch (final IOException e) {

					final String message = String
							.format("for filter '%s' couldn't deserialize filter expression '%s'", referenceIMAPIFilter.getUuid(),
									referenceIMAPIFilterExpression);

					LOG.error(message, e);

					throw new DMPControllerException(message);
				}
			}

			newInputMAPI.setFilter(newFilter);
		}
	}

	/**
	 * Creates a copy of a component without wiring them together
	 *
	 * @param referenceComponent the references component where the copy should be created from
	 * @return a tuple with the reference component as first part and the new component as second part
	 */
	private static Optional<Tuple<Component, Component>> createCopyOfComponent(final Component referenceComponent) {

		if (referenceComponent == null) {

			return Optional.empty();
		}

		final String newComponentUuid = UUIDService.getUUID(Component.class.getSimpleName());

		final Component newComponent = new Component(newComponentUuid);
		newComponent.setName(referenceComponent.getName());
		newComponent.setDescription(referenceComponent.getDescription());
		newComponent.setParameterMappings(referenceComponent.getParameterMappings());

		final Function referenceFunction = referenceComponent.getFunction();

		final Function newFunction;

		final FunctionType referenceFunctionType = referenceFunction.getFunctionType();

		switch (referenceFunctionType) {

			case Transformation:

				newFunction = createCopyOfTransformation((Transformation) referenceFunction);

				break;
			default:

				// function type = Function

				newFunction = referenceFunction;

		}

		newComponent.setFunction(newFunction);

		return Optional.of(Tuple.tuple(referenceComponent, newComponent));
	}

	private static Optional<Component> wireNewComponents(final Component referenceComponent, final Map<String, String> componentUUIDs, final Map<String, Component> newComponentsMaps) {

		if (referenceComponent == null || referenceComponent.getUuid() == null) {

			return Optional.empty();
		}

		final String newComponentUUID = componentUUIDs.get(referenceComponent.getUuid());

		if (newComponentUUID == null) {

			LOG.error("couldn't find a new component UUID for reference component UUID {}", referenceComponent.getUuid());

			return Optional.empty();
		}

		return Optional.ofNullable(newComponentsMaps.get(newComponentUUID));
	}

	private static Transformation createCopyOfTransformation(final Transformation referenceTransformation) {

		final String newTransformationUuid = UUIDService.getUUID(Transformation.class.getSimpleName());

		final Transformation newTransformation = new Transformation(newTransformationUuid);
		newTransformation.setName(referenceTransformation.getName());
		newTransformation.setDescription(referenceTransformation.getDescription());
		newTransformation.setParameters(referenceTransformation.getParameters());
		newTransformation.setFunctionDescription(referenceTransformation.getFunctionDescription());

		final Set<Component> referenceComponents = referenceTransformation.getComponents();

		if (referenceComponents != null) {

			final List<Tuple<Component, Component>> referenceAndNewComponents = referenceComponents.parallelStream()
					.map(ProjectsResource::createCopyOfComponent)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.collect(Collectors.toList());

			final Map<String, String> componentUUIDs = referenceAndNewComponents.parallelStream()
					.collect(Collectors.toMap(referenceAndNewComponentTuple -> referenceAndNewComponentTuple.v1().getUuid(),
							referenceAndNewComponentTuple -> referenceAndNewComponentTuple.v2().getUuid()));

			final Map<String, Component> newComponentsMap = referenceAndNewComponents.parallelStream()
					.map(Tuple::v2)
					.collect(Collectors.toMap(DMPObject::getUuid, newComponent -> newComponent));

			final Set<Component> newComponents = referenceAndNewComponents.parallelStream().map(referenceAndNewComponentTuple -> {

				final Component referenceComponent = referenceAndNewComponentTuple.v1();
				final Component newComponent = referenceAndNewComponentTuple.v2();

				final Set<Component> referenceInputComponents = referenceComponent.getInputComponents();

				if (referenceInputComponents != null) {

					final Set<Component> newInputComponents = referenceInputComponents.parallelStream()
							.map(referenceInputComponent -> wireNewComponents(referenceInputComponent, componentUUIDs, newComponentsMap))
							.filter(Optional::isPresent)
							.map(Optional::get)
							.collect(Collectors.toSet());

					newComponent.setInputComponents(newInputComponents);
				}

				final Set<Component> referenceOutputComponents = referenceComponent.getOutputComponents();

				if (referenceOutputComponents != null) {

					final Set<Component> newOutputComponents = referenceOutputComponents.parallelStream()
							.map(referenceOutputComponent -> wireNewComponents(referenceOutputComponent, componentUUIDs, newComponentsMap))
							.filter(Optional::isPresent)
							.map(Optional::get)
							.collect(Collectors.toSet());

					newComponent.setOutputComponents(newOutputComponents);
				}

				return newComponent;
			}).collect(Collectors.toSet());

			newTransformation.setComponents(newComponents);
		}

		return newTransformation;
	}

	private Response persistProjectForMigration(final String newProjectId, final Project newProject) throws DMPControllerException {

		LOG.debug("try to persist new project '{}' from mappings copying/migration", newProjectId);

		// persist project
		final ProjectService projectService = persistenceServiceProvider.get();

		final ProxyProject newPersistentProxyProject;

		try {

			newPersistentProxyProject = projectService.createObjectTransactional(newProject);
		} catch (final DMPPersistenceException e) {

			final String message = String
					.format("Cannot create project with help of existing entities. Could not persist new project '%s' into metadata repository successfully.",
							newProjectId);

			LOG.error(message, e);

			throw new DMPControllerException(message);
		}

		if (newPersistentProxyProject == null || newPersistentProxyProject.getObject() == null) {

			final String message = String
					.format("Cannot create project with help of existing entities. Could not persist new project '%s' into metadata repository successfully.",
							newProjectId);

			LOG.error(message);

			throw new DMPControllerException(message);
		}

		final Project newPersistentProject = newPersistentProxyProject.getObject();

		LOG.debug("successfully persisted new project '{}' from mappings copying/migration", newProjectId);

		return createCreateObjectResponse(newPersistentProxyProject, newPersistentProject);
	}

	private Optional<Schema> persistInputSchema(final Project project) {

		final DataModel inputDataModel = project.getInputDataModel();

		if (inputDataModel == null) {

			return Optional.empty();
		}

		final Schema schema = inputDataModel.getSchema();

		if (schema == null) {

			return Optional.empty();
		}

		return persistSchema(schema);
	}

	private Optional<Schema> persistOutputSchema(final Project project) {

		final DataModel outputDataModel = project.getOutputDataModel();

		if (outputDataModel == null) {

			return Optional.empty();
		}

		final Schema schema = outputDataModel.getSchema();

		if (schema == null) {

			return Optional.empty();
		}

		return persistSchema(schema);
	}

	private Optional<Schema> persistSchema(final Schema schema) {

		final Optional<Schema> optionalPersistentInbuiltSchema = checkForInbuiltSchema(schema);

		if (optionalPersistentInbuiltSchema.isPresent()) {

			// directly take fresh inbuilt schema from metadata repository

			return optionalPersistentInbuiltSchema;
		}

		final Collection<SchemaAttributePathInstance> sapis = schema.getAttributePaths();

		if (sapis == null || sapis.isEmpty()) {

			return Optional.empty();
		}

		sapis.forEach(this::persistAttributePath);

		final Optional<Clasz> optionalPersistRecordClass = persistRecordClass(schema);

		if (optionalPersistRecordClass.isPresent()) {

			final Clasz persistentRecordClass = optionalPersistRecordClass.get();

			schema.setRecordClass(persistentRecordClass);
		}

		return Optional.of(schema);
	}

	private void persistAttributePath(final SchemaAttributePathInstance sapi) {

		final AttributePath attributePath = sapi.getAttributePath();

		if (attributePath == null) {

			return;
		}

		final Optional<List<Attribute>> optionalPersistentAttributePathList = persistAttributes(attributePath);

		if (!optionalPersistentAttributePathList.isPresent()) {

			return;
		}

		final List<Attribute> persistentAttributePathList = optionalPersistentAttributePathList.get();

		try {

			final ProxyAttributePath proxyAttributePath = attributePathPersistenceServiceProvider.get().createOrGetObjectTransactional(persistentAttributePathList);

			if (proxyAttributePath == null) {

				return;
			}

			final AttributePath persistentAttributePath = proxyAttributePath.getObject();

			sapi.setAttributePath(persistentAttributePath);
		} catch (final DMPPersistenceException e) {

			throw DMPPersistenceError.wrap(e);
		}

		final Schema subSchema = sapi.getSubSchema();

		if (subSchema != null) {

			persistSchema(subSchema);
		}
	}

	private Optional<List<Attribute>> persistAttributes(final AttributePath attributePath) {

		final Set<Attribute> attributes = attributePath.getAttributes();

		if (attributes == null || attributes.isEmpty()) {

			return Optional.empty();
		}

		final Map<String, Attribute> persistentAttributes = attributes.stream()
				.map(this::persistAttribute)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toMap(Attribute::getUri, attribute -> attribute));

		final List<Attribute> attributePathList = attributePath.getAttributePath();

		final List<Attribute> persistentAttributePathList = attributePathList.stream()
				.map(attribute -> Optional.ofNullable(persistentAttributes.get(attribute.getUri())))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());

		return Optional.ofNullable(persistentAttributePathList);
	}

	private Optional<Attribute> persistAttribute(final Attribute attribute) {

		try {

			final ProxyAttribute proxyAttribute = attributePersistenceServiceProvider.get().createOrGetObjectTransactional(attribute);

			if (proxyAttribute == null) {

				return Optional.empty();
			}

			return Optional.ofNullable(proxyAttribute.getObject());
		} catch (final DMPPersistenceException e) {

			throw DMPPersistenceError.wrap(e);
		}
	}

	private Optional<Clasz> persistRecordClass(final Schema schema) {

		final Clasz recordClass = schema.getRecordClass();

		if (recordClass == null) {

			return Optional.empty();
		}

		try {

			final ProxyClasz proxyClasz = classPersistenceServiceProvider.get().createOrGetObjectTransactional(recordClass);

			if (proxyClasz == null) {

				return Optional.empty();
			}

			return Optional.ofNullable(proxyClasz.getObject());
		} catch (final DMPPersistenceException e) {

			throw DMPPersistenceError.wrap(e);
		}
	}

	private static void replaceMappingInputs(final Project project,
	                                         final Schema inputSchema) {

		final Set<Mapping> mappings = project.getMappings();

		if (mappings == null || mappings.isEmpty()) {

			return;
		}

		final Map<String, AttributePath> inputAttributePathMap = SchemaUtils.generateAttributePathMap(inputSchema);

		mappings.stream()
				.filter(mapping -> {

					final Set<MappingAttributePathInstance> inputAttributePaths = mapping.getInputAttributePaths();

					return inputAttributePaths != null && !inputAttributePaths.isEmpty();
				})
				.forEach(mapping -> {

					final Set<MappingAttributePathInstance> inputAttributePaths = mapping.getInputAttributePaths();

					replaceAttributePaths(inputAttributePaths, inputAttributePathMap);
				});
	}

	private static void replaceMappingOutputs(final Project project,
	                                          final Schema outputSchema) {

		final Set<Mapping> mappings = project.getMappings();

		if (mappings == null || mappings.isEmpty()) {

			return;
		}

		final Map<String, AttributePath> outputAttributePathMap = SchemaUtils.generateAttributePathMap(outputSchema);

		mappings.stream()
				.filter(mapping -> {

					final MappingAttributePathInstance outputAttributePath = mapping.getOutputAttributePath();

					return outputAttributePath != null;
				})
				.forEach(mapping -> {

					final MappingAttributePathInstance outputAttributePath = mapping.getOutputAttributePath();

					replaceAttributePath(outputAttributePath, outputAttributePathMap);
				});
	}

	private static void replaceAttributePaths(final Set<MappingAttributePathInstance> attributePaths,
	                                          final Map<String, AttributePath> attributePathMap) {

		attributePaths.stream()
				.filter(ProjectsResource::checkMAPI)
				.forEach(mapi -> replaceAttributePath2(attributePathMap, mapi));
	}

	private static void replaceAttributePath(final MappingAttributePathInstance mapi,
	                                         final Map<String, AttributePath> attributePathMap) {

		if (!checkMAPI(mapi)) {

			return;
		}

		replaceAttributePath2(attributePathMap, mapi);
	}

	private static boolean checkMAPI(final MappingAttributePathInstance mapi) {

		final AttributePath attributePath = mapi.getAttributePath();

		return attributePath != null;
	}

	private static void replaceAttributePath2(final Map<String, AttributePath> attributePathMap,
	                                          final MappingAttributePathInstance mapi) {

		final AttributePath attributePath = mapi.getAttributePath();

		final String attributePathString = attributePath.toAttributePath();

		if (!attributePathMap.containsKey(attributePathString)) {

			return;
		}

		final AttributePath persistentAttributePath = attributePathMap.get(attributePathString);

		mapi.setAttributePath(persistentAttributePath);
	}

	private Optional<Schema> checkForInbuiltSchema(final Schema schema) {

		final String schemaUuid = schema.getUuid();

		final Optional<Schema> optionalPersistentInbuiltSchema;

		switch (schemaUuid) {

			case SchemaUtils.MABXML_SCHEMA_UUID:
			case SchemaUtils.MARCXML_SCHEMA_UUID:
			case SchemaUtils.PICAPLUSXML_SCHEMA_UUID:
			case SchemaUtils.PICAPLUSXML_GLOBAL_SCHEMA_UUID:
			case SchemaUtils.PNX_SCHEMA_UUID:
			case SchemaUtils.FINC_SOLR_SCHEMA_UUID:
			case SchemaUtils.OAI_PMH_DC_ELEMENTS_SCHEMA_UUID:
			case SchemaUtils.OAI_PMH_DC_ELEMENTS_AND_EDM_SCHEMA_UUID:
			case SchemaUtils.OAI_PMH_DC_TERMS_SCHEMA_UUID:
			case SchemaUtils.OAI_PMH_MARCXML_SCHEMA_UUID:
			case SchemaUtils.SRU_11_PICAPLUSXML_GLOBAL_SCHEMA_UUID:
			case SchemaUtils.UBL_INTERMEDIATE_FORMAT_SCHEMA_UUID:
			case SchemaUtils.SPRINGER_JOURNALS_SCHEMA_UUID:

				optionalPersistentInbuiltSchema = fetchInbuiltSchema(schemaUuid);

				break;
			default:

				optionalPersistentInbuiltSchema = Optional.empty();
		}

		return optionalPersistentInbuiltSchema;
	}

	private Optional<Schema> fetchInbuiltSchema(final String schemaUuid) {

		return Optional.ofNullable(schemaPersistenceServiceProvider.get().getObject(schemaUuid));
	}
}
