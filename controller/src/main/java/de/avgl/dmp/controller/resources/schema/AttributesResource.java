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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.AdvancedDMPResource;
import de.avgl.dmp.controller.resources.schema.utils.AttributesResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttribute;
import de.avgl.dmp.persistence.service.schema.AttributeService;

/**
 * A resource (controller service) for {@link Attribute}s.
 * 
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/attributes", description = "Operations about attributes.")
@Path("attributes")
public class AttributesResource extends AdvancedDMPResource<AttributesResourceUtils, AttributeService, ProxyAttribute, Attribute> {

	/**
	 * Creates a new resource (controller service) for {@link Attribute}s with the provider of the attribute persistence service,
	 * the object mapper and metrics registry.
	 * 
	 * @param attributeServiceProviderArg the attribute persistence service provider
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	@Inject
	public AttributesResource(final ResourceUtilsFactory utilsFactory, final DMPStatus dmpStatusArg, final ObjectMapper objectMapperArg)
			throws DMPControllerException {

		super(utilsFactory.reset().get(AttributesResourceUtils.class), dmpStatusArg);
	}

	/**
	 * This endpoint returns an attribute as JSON representation for the provided attribute identifier.<br/>
	 * 
	 * @param id an attribute identifier
	 * @return a JSON representation of an attribute
	 */
	@ApiOperation(value = "get the attribute that matches the given id", notes = "Returns the Attribute object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the attribute (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find an attribute for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "attribute identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes an attribute as JSON representation and persists this attribute in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one attribute
	 * @return the persisted attribute as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new attribute", notes = "Returns a new Attribute object.", response = Attribute.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "attribute does already exist; returns the existing one"),
			@ApiResponse(code = 201, message = "attribute was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "attribute (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all attributes as JSON representation.
	 * 
	 * @return a list of all attributes as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all attributes ", notes = "Returns a list of Attribute objects.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all available attributes (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any attribute, i.e., there are no attribute available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * This endpoint consumes an attribute as JSON representation and updates this attribute in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one attribute
	 * @param id an attribute identifier
	 * @return the updated attribute as JSON representation
	 * @throws DMPControllerException
	 */
	@Override
	@ApiOperation(value = "update attribute with given id ", notes = "Returns an updated Attribute object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "attribut was successfully updated"),
			@ApiResponse(code = 201, message = "attribut was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "attribute (as JSON)", required = true) final String jsonObjectString,
			@ApiParam(value = "attribute identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.updateObject(jsonObjectString, id);
	}

	/**
	 * This endpoint deletes a attribute that matches the given id.
	 * 
	 * @param id an attribute identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 *         went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "delete attribute that matches the given id", notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "attribute was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find an attribute for the given id"),
			@ApiResponse(code = 409, message = "attribute couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "attribute identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}
}
