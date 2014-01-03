package de.avgl.dmp.controller.resources.job;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/projects", description = "Operations about projects.")
@Path("projects")
public class ProjectsResource extends ExtendedBasicDMPResource<ProjectService, Project> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ProjectsResource.class);

	@Inject
	public ProjectsResource(final Provider<ProjectService> projectServiceProviderArg, final ObjectMapper objectMapper, final DMPStatus dmpStatus) {

		super(Project.class, projectServiceProviderArg, objectMapper, dmpStatus);
	}

	@ApiOperation(value = "get the project that matches the given id", notes = "Returns the Project object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "project identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.getObject(id);
	}

	@ApiOperation(value = "create a new project", notes = "Returns a new Project object.", response = Project.class)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "project (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	@ApiOperation(value = "get all projects ", notes = "Returns a list of Project objects.")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

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
