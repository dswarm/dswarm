package de.avgl.dmp.controller.resources.schema;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import de.avgl.dmp.controller.resources.AdvancedDMPResource;
import de.avgl.dmp.controller.resources.schema.utils.ClaszesResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyClasz;
import de.avgl.dmp.persistence.service.schema.ClaszService;

/**
 * A resource (controller service) for {@link Clasz}es.
 * 
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/classes", description = "Operations about classes.")
@Path("classes")
public class ClaszesResource extends AdvancedDMPResource<ClaszesResourceUtils, ClaszService, ProxyClasz, Clasz> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ClaszesResource.class);

	/**
	 * Creates a new resource (controller service) for {@link Clasz}s with the provider of the class persistence service, the
	 * object mapper and metrics registry.
	 * 
	 * @param claszServiceProviderArg the class persistence service provider
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	@Inject
	public ClaszesResource(final ResourceUtilsFactory utilsFactory, final DMPStatus dmpStatusArg) throws DMPControllerException {

		super(utilsFactory.reset().get(ClaszesResourceUtils.class), dmpStatusArg);
	}

	/**
	 * This endpoint returns a class as JSON representation for the provided class identifier.<br/>
	 * 
	 * @param id a class identifier
	 * @return a JSON representation of a class
	 */
	@ApiOperation(value = "get the class that matches the given id", notes = "Returns the Clasz object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the class (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find a class for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "class identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a class as JSON representation and persists this class in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one class
	 * @return the persisted class as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new class", notes = "Returns a new Clasz object.", response = Clasz.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "class was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "class (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all classes as JSON representation.
	 * 
	 * @return a list of all classes as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all classes", notes = "Returns a list of Clasz objects.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all available classes (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any class, i.e., there are no classes available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	// TODO: add put

	/**
	 * This endpoint deletes a class that matches the given id.
	 * 
	 * @param id a class identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 *         went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "delete class that matches the given id", notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "class was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a class for the given id"),
			@ApiResponse(code = 409, message = "class couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "class identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.deleteObject(id);
	}
}
