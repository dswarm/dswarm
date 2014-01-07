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
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.service.schema.AttributePathService;

/**
 * A resource (controller service) for {@link AttributePath}s.
 * 
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/attributepaths", description = "Operations about attribute paths.")
@Path("attributepaths")
public class AttributePathsResource extends BasicResource<AttributePathService, AttributePath, Long> {

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
	public AttributePathsResource(final Provider<AttributePathService> attributePathServiceProviderArg, final ObjectMapper objectMapper,
			final DMPStatus dmpStatus) {

		super(AttributePath.class, attributePathServiceProviderArg, objectMapper, dmpStatus);
	}

	/**
	 * This endpoint returns an attribute path as JSON representation for the provided attribute paths identifier.
	 * 
	 * @param id an attribute path identifier
	 * @return a JSON representation of an attribute path
	 */
	@ApiOperation(value = "get the attribute path that matches the given id", notes = "Returns the AttributePath object that matches the given id.")
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
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
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
