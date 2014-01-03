package de.avgl.dmp.controller.resources.schema;

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
import de.avgl.dmp.controller.resources.BasicResource;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.service.schema.ClaszService;

/**
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/classes", description = "Operations about classes.")
@Path("classes")
public class ClaszesResource extends BasicResource<ClaszService, Clasz, String> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ClaszesResource.class);

	@Inject
	public ClaszesResource(final Provider<ClaszService> claszServiceProviderArg, final ObjectMapper objectMapper, final DMPStatus dmpStatus) {

		super(Clasz.class, claszServiceProviderArg, objectMapper, dmpStatus);
	}

	@ApiOperation(value = "get the class that matches the given id", notes = "Returns the Clasz object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "class identifier", required = true) @PathParam("id") final String id) throws DMPControllerException {

		// return super.getObject(id);

		return Response.status(505).build();
	}

	@ApiOperation(value = "create a new class", notes = "Returns a new Clasz object.", response = Clasz.class)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "class (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	@ApiOperation(value = "get all classes", notes = "Returns a list of Clasz objects.")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	@Override
	protected Clasz prepareObjectForUpdate(final Clasz objectFromJSON, final Clasz object) {

		object.setName(objectFromJSON.getName());

		return object;
	}

	@Override
	protected Clasz createObject(final Clasz objectFromJSON, final ClaszService persistenceService) throws DMPPersistenceException {

		return persistenceService.createObject(objectFromJSON.getId());
	}

}
