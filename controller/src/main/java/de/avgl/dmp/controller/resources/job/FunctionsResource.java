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
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.service.job.FunctionService;

/**
 * A resource (controller service) for {@link Function}s.
 * 
 * @author tgaengler
 * @author fniederlein
 */
@RequestScoped
@Api(value = "/functions", description = "Operations about functions.")
@Path("functions")
public class FunctionsResource extends BasicFunctionsResource<FunctionService, Function> {

	/**
	 * Creates a new resource (controller service) for {@link Function}s with the provider of the function persistence service,
	 * the object mapper and metrics registry.
	 * 
	 * @param functionServiceProviderArg the function persistence service provider
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	@Inject
	public FunctionsResource(final Provider<FunctionService> functionServiceProviderArg, final ObjectMapper objectMapper, final DMPStatus dmpStatus) {

		super(Function.class, functionServiceProviderArg, objectMapper, dmpStatus);
	}

	/**
	 * This endpoint returns a function as JSON representation for the provided function identifier.
	 * 
	 * @param id a function identifier
	 * @return a JSON representation of a function
	 */
	@ApiOperation(value = "get the function that matches the given id", notes = "Returns the Function object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "function identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a filter as JSON representation and persists this filter in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one filter
	 * @return the persisted filter as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new function", notes = "Returns a new Function object.", response = Function.class)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "function (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all functions as JSON representation.
	 * 
	 * @return a list of all functions as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all functions ", notes = "Returns a list of Function objects.")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}
	
	/**
	 * This endpoint consumes a function as JSON representation and update this function in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one function
	 * @param id a function identifier
	 * @return the updated function as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "update function with given id ", notes = "Returns a new Function object.")
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "function (as JSON)", required = true) final String jsonObjectString, 
			@ApiParam(value = "function identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.updateObject(jsonObjectString, id);
	}
}
