package de.avgl.dmp.controller.resources.schema;

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
import de.avgl.dmp.controller.resources.BasicIDResource;
import de.avgl.dmp.controller.resources.schema.utils.AttributePathsResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttributePath;
import de.avgl.dmp.persistence.service.schema.AttributePathService;

/**
 * A resource (controller service) for {@link AttributePath}s.
 * 
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/attributepaths", description = "Operations about attribute paths.")
@Path("attributepaths")
public class AttributePathsResource extends BasicIDResource<AttributePathsResourceUtils, AttributePathService, ProxyAttributePath, AttributePath> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributePathsResource.class);

	/**
	 * Creates a new resource (controller service) for {@link AttributePath}s with the provider of the attribute path persistence
	 * service, the object mapper and metrics registry.
	 * 
	 * @param attributePathServiceProviderArg the attribute path persistence service provider
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	@Inject
	public AttributePathsResource(final ResourceUtilsFactory utilsFactory, final DMPStatus dmpStatusArg) throws DMPControllerException {

		super(utilsFactory.reset().get(AttributePathsResourceUtils.class), dmpStatusArg);
	}

	/**
	 * This endpoint returns an attribute path as JSON representation for the provided attribute paths identifier.
	 * 
	 * @param id an attribute path identifier
	 * @return a JSON representation of an attribute path
	 */
	@ApiOperation(value = "get the attribute path that matches the given id", notes = "Returns the AttributePath object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the attribute path (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find an attribute path for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "attribute path identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes an attribute path as JSON representation and persists this attribute path in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one attribute path
	 * @return the persisted attribute path as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new attribute path", notes = "Returns a new AttributePath object.", response = AttributePath.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "attribute path does already exist; returns the existing one"),
			@ApiResponse(code = 201, message = "attribute path was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "attribute path (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all attribute paths as JSON representation.
	 * 
	 * @return a list of all attribute paths as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all attribute paths ", notes = "Returns a list of AttributePath objects.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all available attribute paths (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any attribute path, i.e., there are no attribute paths available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * This endpoint consumes a attribute path as JSON representation and updates this attribute path in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one attribute path
	 * @param id a attribute path identifier
	 * @return the updated attribute path as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "update attribute path with given id ", notes = "Returns an updated AttributePath object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "attribute path was successfully updated"),
			@ApiResponse(code = 404, message = "could not find an attribute path for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "attribute path (as JSON)", required = true) final String jsonObjectString,
			@ApiParam(value = "attribute path identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.updateObject(jsonObjectString, id);
	}

	/**
	 * This endpoint deletes a attribute path that matches the given id.
	 * 
	 * @param id an attribute path identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 *         went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "delete attribute path that matches the given id", notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "attribute path was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find an attribute path for the given id"),
			@ApiResponse(code = 409, message = "attribute path couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "attribute path identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name and list of attributes of the attribute path.
	 */
	@Override
	protected AttributePath prepareObjectForUpdate(final AttributePath objectFromJSON, final AttributePath object) {

		object.setAttributePath(objectFromJSON.getAttributePath());

		return object;
	}
}
