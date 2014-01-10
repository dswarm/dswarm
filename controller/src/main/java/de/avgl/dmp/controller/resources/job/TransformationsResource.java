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
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.service.job.TransformationService;

/**
 * A resource (controller service) for {@link Transformation}s.
 * 
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/transformations", description = "Operations about transformations.")
@Path("transformations")
public class TransformationsResource extends BasicFunctionsResource<TransformationService, Transformation> {

	/**
	 * Creates a new resource (controller service) for {@link Transformation}s with the provider of the transformation persistence
	 * service, the object mapper and metrics registry.
	 * 
	 * @param transformationServiceProviderArg the transformation persistence service provider
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	@Inject
	public TransformationsResource(final Provider<TransformationService> transformationServiceProviderArg, final ObjectMapper objectMapper,
			final DMPStatus dmpStatus) {

		super(Transformation.class, transformationServiceProviderArg, objectMapper, dmpStatus);
	}

	/**
	 * This endpoint returns a transformation as JSON representation for the provided transformation identifier.
	 * 
	 * @param id a transformation identifier
	 * @return a JSON representation of a transformation
	 */
	@ApiOperation(value = "get the transformation that matches the given id", notes = "Returns the Transformation object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "transformation identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a transformation as JSON representation and persists this transformation in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one transformation
	 * @return the persisted transformation as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new transformation", notes = "Returns a new Transformation object.", response = Transformation.class)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "transformation (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all transformations as JSON representation.
	 * 
	 * @return a list of all transformations as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all transformations ", notes = "Returns a list of Transformation objects.")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}
	
	/**
	 * This endpoint consumes a transformation as JSON representation and update this transformation in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one transformation
	 * @param id a transformation identifier
	 * @return the updated transformation as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "update transformation with given id ", notes = "Returns a new Transformation object.")
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "transformation (as JSON)", required = true) final String jsonObjectString, 
			@ApiParam(value = "transformation identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.updateObject(jsonObjectString, id);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, description, parameters, machine processable function description and components of the transformation.
	 */
	@Override
	protected Transformation prepareObjectForUpdate(final Transformation objectFromJSON, final Transformation object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setComponents(objectFromJSON.getComponents());

		return object;
	}
}
