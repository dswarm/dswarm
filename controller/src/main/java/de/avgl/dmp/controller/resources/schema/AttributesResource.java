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
public class AttributesResource extends BasicResource<AttributeService, Attribute, String> {

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
	public AttributesResource(final Provider<AttributeService> attributeServiceProviderArg, final ObjectMapper objectMapper, final DMPStatus dmpStatus) {

		super(Attribute.class, attributeServiceProviderArg, objectMapper, dmpStatus);
	}

	/**
	 * This endpoint returns an attribute as JSON representation for the provided attribute identifier.<br/>
	 * note: currently, this method is not implemented
	 * 
	 * @param id an attribute identifier
	 * @return a JSON representation of an attribute
	 */
	@ApiOperation(value = "get the attribute that matches the given id", notes = "Returns the Attribute object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "attribute identifier", required = true) @PathParam("id") final String id) throws DMPControllerException {

		//return super.getObject(id);

		return Response.status(505).build();
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

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name of the attribute.
	 */
	@Override
	protected Attribute prepareObjectForUpdate(final Attribute objectFromJSON, final Attribute object) {

		object.setName(objectFromJSON.getName());

		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Attribute createObject(final Attribute objectFromJSON, final AttributeService persistenceService) throws DMPPersistenceException {

		return persistenceService.createObject(objectFromJSON.getId());
	}

}
