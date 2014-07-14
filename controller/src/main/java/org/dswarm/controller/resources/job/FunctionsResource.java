package org.dswarm.controller.resources.job;

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

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.job.utils.FunctionsResourceUtils;
import org.dswarm.controller.resources.utils.ResourceUtilsFactory;
import org.dswarm.controller.status.DMPStatus;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.proxy.ProxyFunction;
import org.dswarm.persistence.service.job.FunctionService;

import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * A resource (controller service) for {@link Function}s.
 * 
 * @author tgaengler
 * @author fniederlein
 */
@RequestScoped
@Api(value = "/functions", description = "Operations about functions.")
@Path("functions")
public class FunctionsResource extends BasicFunctionsResource<FunctionsResourceUtils, FunctionService, ProxyFunction, Function> {

	/**
	 * Creates a new resource (controller service) for {@link Function}s with the provider of the function persistence service,
	 * the object mapper and metrics registry.
	 * 
	 * @param functionServiceProviderArg the function persistence service provider
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	@Inject
	public FunctionsResource(final ResourceUtilsFactory utilsFactory, final DMPStatus dmpStatusArg) throws DMPControllerException {

		super(utilsFactory.reset().get(FunctionsResourceUtils.class), dmpStatusArg);
	}

	/**
	 * This endpoint returns a function as JSON representation for the provided function identifier.
	 * 
	 * @param id a function identifier
	 * @return a JSON representation of a function
	 */
	@ApiOperation(value = "get the function that matches the given id", notes = "Returns the Function object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the function (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find a function for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
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
	@ApiResponses(value = { @ApiResponse(code = 201, message = "function was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
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
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all available functions (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any function, i.e., there are no functions available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * This endpoint consumes a function as JSON representation and updates this function in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one function
	 * @param id a function identifier
	 * @return the updated function as JSON representation
	 * @throws DMPControllerException
	 */
	@Override
	@ApiOperation(value = "update function with given id ", notes = "Returns an updated Function object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "function was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a function for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "function (as JSON)", required = true) final String jsonObjectString,
			@ApiParam(value = "function identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.updateObject(jsonObjectString, id);
	}

	/**
	 * This endpoint deletes a function that matches the given id.
	 * 
	 * @param id a function identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 *         went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "delete function that matches the given id", notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "function was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a function for the given id"),
			@ApiResponse(code = 409, message = "function couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "function identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}
}
