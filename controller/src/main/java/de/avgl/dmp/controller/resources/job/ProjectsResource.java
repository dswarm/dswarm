package de.avgl.dmp.controller.resources.job;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.ExtendedBasicDMPResource;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.job.Project;
import de.avgl.dmp.persistence.service.job.ProjectService;

/**
 * A resource (controller service) for {@link Project}s.
 * 
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/projects", description = "Operations about projects.")
@Path("projects")
public class ProjectsResource extends ExtendedBasicDMPResource<ProjectService, Project> {

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
	public ProjectsResource(final Provider<ProjectService> projectServiceProviderArg, final ObjectMapper objectMapper, final DMPStatus dmpStatus) {

		super(Project.class, projectServiceProviderArg, objectMapper, dmpStatus);
	}

	/**
	 * This endpoint returns a project as JSON representation for the provided project identifier.
	 * 
	 * @param id a project identifier
	 * @return a JSON representation of a project
	 */
	@ApiOperation(value = "get the project that matches the given id", notes = "Returns the Project object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "project identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a project as JSON representation and persists this project in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one project
	 * @return the persisted project as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new project", notes = "Returns a new Project object.", response = Project.class)
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
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}
	
	/**
	 * This endpoint consumes a project as JSON representation and update this project in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one project
	 * @param id a project identifier
	 * @return the updated project as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "update project with given id ", notes = "Returns a new Project object.")
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "project (as JSON)", required = true) final String jsonObjectString, 
			@ApiParam(value = "project identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.updateObject(jsonObjectString, id);
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
