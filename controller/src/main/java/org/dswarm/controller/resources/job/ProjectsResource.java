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
package org.dswarm.controller.resources.job;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.ExtendedBasicDMPResource;
import org.dswarm.controller.resources.POJOFormat;
import org.dswarm.init.DMPException;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.proxy.ProxyProject;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.job.ProjectService;
import org.dswarm.persistence.service.resource.DataModelService;
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

	public static final String INPUT_DATA_MODEL  = "input_data_model";
	public static final String REFERENCE_PROJECT = "reference_project";

	private final Provider<DataModelService> dataModelPersistenceServiceProvider;

	/**
	 * Creates a new resource (controller service) for {@link Project}s with the provider of the project persistence service, the
	 * object mapper and metrics registry.
	 *
	 * @param persistenceServiceProviderArg
	 * @param objectMapperProviderArg
	 */
	@Inject
	public ProjectsResource(final Provider<ProjectService> persistenceServiceProviderArg,
			final Provider<DataModelService> dataModelPersistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg)
			throws DMPControllerException {

		super(Project.class, persistenceServiceProviderArg, objectMapperProviderArg);

		dataModelPersistenceServiceProvider = dataModelPersistenceServiceProviderArg;
	}

	/**
	 * This endpoint returns a project as JSON representation for the provided project identifier.
	 *
	 * @param id a project identifier
	 * @return a JSON representation of a project
	 */
	@ApiOperation(value = "get the project that matches the given id", notes = "Returns the Project object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the project (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find a project for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
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
	@ApiResponses(value = { @ApiResponse(code = 201, message = "project was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "project (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all projects as JSON representation.
	 *
	 * @return a list of all projects as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all projects ", notes = "Returns a list of Project objects.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all available projects (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any project, i.e., there are no projects available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })

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
	@ApiResponses(value = { @ApiResponse(code = 200, message = "project was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a project for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
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
	@ApiResponses(value = { @ApiResponse(code = 204, message = "project was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a project for the given id"),
			@ApiResponse(code = 409, message = "project couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "project identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}

	/**
	 * This endpoint consumes a project as JSON representation and persists this project (incl. all its parts, i.e., new sub
	 * elements, e.g., mappings will be persisted as well) in the database. <br/>
	 * Note: please utilise generated uuids for all entity identifier.
	 *
	 * @param requestJsonObjectString a JSON representation of the request parameters
	 * @return the persisted project as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new project by copying parts from existing project and utilising other existing entities", notes = "Returns a new Project object. Persists this project (incl. all its parts, i.e., new sub elements, e.g., mappings will be persisted as well). Note: please utilise generated uuids for all entity identifier.", response = Project.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "project was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Path("/createprojectwithhelpofexistingentities")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createProjectWithHelpOfExistingEntities(
			@ApiParam(value = "project (as JSON)", required = true) final String requestJsonObjectString)
			throws DMPControllerException {

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

		final Optional<String> optionalInputDataModelId = getStringValue(INPUT_DATA_MODEL, requestJSON);
		final Optional<String> optionalReferenceProjectId = getStringValue(REFERENCE_PROJECT, requestJSON);

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

		// create new project
		final String newProjectId = UUIDService.getUUID(Project.class.getSimpleName());

		final Project newProject = new Project(newProjectId);
		newProject.setName("copy of '" + referenceProject.getName() + "'");
		newProject.setDescription("copy of '" + referenceProject.getDescription() + "'");
		newProject.setInputDataModel(inputDataModel);
		// TODO: maybe check output data model?
		newProject.setOutputDataModel(referenceProject.getOutputDataModel());
		newProject.setFunctions(referenceProject.getFunctions());

		final Set<Mapping> referenceMappings = referenceProject.getMappings();

		if (referenceMappings != null) {

			final Set<Mapping> newMappings = new LinkedHashSet<>();

			for (final Mapping referenceMapping : referenceMappings) {

				final String newMappingId = UUIDService.getUUID(Mapping.class.getSimpleName());

				final Mapping newMapping = new Mapping(newMappingId);

				newMapping.setName(referenceMapping.getName());
				newMapping.setInputAttributePaths(referenceMapping.getInputAttributePaths());
				newMapping.setOutputAttributePath(referenceMapping.getOutputAttributePath());
				newMapping.setTransformation(referenceMapping.getTransformation());

				newMappings.add(newMapping);
			}

			newProject.setMappings(newMappings);
		}

		// persist project
		final ProjectService projectService = persistenceServiceProvider.get();

		final ProxyProject newPersistentProxyProject;

		try {

			newPersistentProxyProject = projectService.createObject(newProject);
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

		return createCreateObjectResponse(newPersistentProxyProject, newPersistentProject);
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

	private Optional<String> getStringValue(final String key, final JsonNode json) {

		final JsonNode node = json.get(key);
		final Optional<String> optionalValue;

		if (node != null) {

			optionalValue = Optional.ofNullable(node.asText());
		} else {

			optionalValue = Optional.empty();
		}

		return optionalValue;
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
}
