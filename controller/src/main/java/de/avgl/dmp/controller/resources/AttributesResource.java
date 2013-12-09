package de.avgl.dmp.controller.resources;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.service.schema.AttributeService;

/**
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/attributes", description = "Operations about attributes.")
@Path("attributes")
public class AttributesResource extends BasicResource<AttributeService, Attribute, String> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributesResource.class);

	@Context
	UriInfo											uri;

	@Inject
	public AttributesResource(final Provider<AttributeService> attributeServiceProviderArg, final ObjectMapper objectMapper, final DMPStatus dmpStatus) {

		super(Attribute.class, attributeServiceProviderArg, objectMapper, dmpStatus);
	}

	@ApiOperation(value = "get the attribute that matches the given id", notes = "Returns the Attribute object that matches the given id.")
	@Override
	public Response getObject(@ApiParam(value = "attribute identifier", required = true) final String id) throws DMPControllerException {

		return super.getObject(id);
	}

	@ApiOperation(value = "create a new attribute", notes = "Returns a new Attribute object.", response = Attribute.class)
	@Override
	public Response createObject(@ApiParam(value = "attribute (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	@ApiOperation(value = "get all attributes ", notes = "Returns a list of Attribute objects.")
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	@Override
	protected Attribute prepareObjectForUpdate(final Attribute objectFromJSON, final Attribute object) {

		object.setName(objectFromJSON.getName());

		return object;
	}
}
