package de.avgl.dmp.controller.resources.job;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.ExtendedBasicDMPResource;
import de.avgl.dmp.controller.resources.job.utils.ProjectsResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.job.Project;
import de.avgl.dmp.persistence.model.job.proxy.ProxyProject;
import de.avgl.dmp.persistence.service.job.ProjectService;

/**
 * A resource (controller service) for {@link Project}s.
 * 
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/projects", description = "Operations about projects.")
@Path("projects")
public class ProjectsResource extends ExtendedBasicDMPResource<ProjectsResourceUtils, ProjectService, ProxyProject, Project> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ProjectsResource.class);

	/**
	 * Creates a new resource (controller service) for {@link Project}s with the provider of the project persistence service, the
	 * object mapper and metrics registry.
	 * 
	 * @param projectServiceProviderArg the project persistence service provider
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	@Inject
	public ProjectsResource(final ResourceUtilsFactory utilsFactory, final DMPStatus dmpStatusArg) throws DMPControllerException {

		super(utilsFactory.reset().get(ProjectsResourceUtils.class), dmpStatusArg);
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
	public Response getObject(@ApiParam(value = "project identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a project as JSON representation and persists this project (incl. all its parts, i.e., new sub
	 * elements, e.g., mappings will be persisted as well) in the database. <br/>
	 * Note: please utilise negative 'long' values for assigning a dummy id to an object. The same dummy id addresses the same
	 * object for a certain domain model class.
	 * 
	 * @param jsonObjectString a JSON representation of one project
	 * @return the persisted project as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new project", notes = "Returns a new Project object. Persists this project (incl. all its parts, i.e., new sub elements, e.g., mappings will be persisted as well). Note: please utilise negative 'long' values for assigning a dummy id to an object. The same dummy id means the same object for a certain domain model class.", response = Project.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "project was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "project (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {

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
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * This endpoint consumes a project as JSON representation and updates this project in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one project
	 * @param id a project identifier
	 * @return the updated project as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "update project with given id ", notes = "Returns an updated Project object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "project was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a project for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "project (as JSON)", required = true) final String jsonObjectString,
			@ApiParam(value = "project identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.updateObject(jsonObjectString, id);
	}

	/**
	 * This endpoint deletes a project that matches the given id.
	 * 
	 * @param id a project identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 *         went wrong
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
	public Response deleteObject(@ApiParam(value = "project identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.deleteObject(id);
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
		object.setMappings(objectFromJSON.getMappings());

		return object;
	}
}
