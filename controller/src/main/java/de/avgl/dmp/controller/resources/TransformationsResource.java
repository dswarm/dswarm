package de.avgl.dmp.controller.resources;

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
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.service.job.TransformationService;

@RequestScoped
@Api(value = "/transformations", description = "Operations about transformations.")
@Path("transformations")
public class TransformationsResource extends BasicFunctionsResource<TransformationService, Transformation> {

	@Inject
	public TransformationsResource(final Provider<TransformationService> transformationServiceProviderArg, final ObjectMapper objectMapper,
			final DMPStatus dmpStatus) {

		super(Transformation.class, transformationServiceProviderArg, objectMapper, dmpStatus);
	}

	@ApiOperation(value = "get the transformation that matches the given id", notes = "Returns the Transformation object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "transformation identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	@ApiOperation(value = "create a new transformation", notes = "Returns a new Transformation object.", response = Transformation.class)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "transformation (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	@ApiOperation(value = "get all transformations ", notes = "Returns a list of Transformation objects.")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	@Override
	protected Transformation prepareObjectForUpdate(final Transformation objectFromJSON, final Transformation object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setComponents(objectFromJSON.getComponents());

		return object;
	}
}
