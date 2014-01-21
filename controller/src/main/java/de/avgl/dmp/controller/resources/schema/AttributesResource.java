package de.avgl.dmp.controller.resources.schema;

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

import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.AdvancedDMPResource;
import de.avgl.dmp.controller.resources.schema.utils.AttributesResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.service.schema.AttributeService;

/**
 * A resource (controller service) for {@link Attribute}s.
 * 
 * @author tgaengler
 *
 */
@RequestScoped
@Api(value = "/attributes", description = "Operations about attributes.")
@Path("attributes")
public class AttributesResource extends AdvancedDMPResource<AttributesResourceUtils, AttributeService, Attribute> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributesResource.class);

	/**
	 * Creates a new resource (controller service) for {@link Attribute}s with the provider of the attribute persistence
	 * service, the object mapper and metrics registry.
	 * 
	 * @param attributeServiceProviderArg the attribute persistence service provider
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	@Inject
	public AttributesResource(final ResourceUtilsFactory utilsFactory, final DMPStatus dmpStatusArg) throws DMPControllerException {

		super(utilsFactory.reset().get(AttributesResourceUtils.class), dmpStatusArg);
	}

	/**
	 * This endpoint returns an attribute as JSON representation for the provided attribute identifier.<br/>
	 * 
	 * @param id an attribute identifier
	 * @return a JSON representation of an attribute
	 */
	@ApiOperation(value = "get the attribute that matches the given id", notes = "Returns the Attribute object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "attribute identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes an attribute as JSON representation and persists this attribute in the
	 * database.
	 * 
	 * @param jsonObjectString a JSON representation of one attribute
	 * @return the persisted attribute as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new attribute", notes = "Returns a new Attribute object.", response = Attribute.class)
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
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}
}
