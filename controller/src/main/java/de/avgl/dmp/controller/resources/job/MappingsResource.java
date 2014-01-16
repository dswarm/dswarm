package de.avgl.dmp.controller.resources.job;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
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

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.BasicDMPResource;
import de.avgl.dmp.controller.resources.job.utils.MappingsResourceUtils;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.service.job.MappingService;

/**
 * A resource (controller service) for {@link Mapping}s.
 * 
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/mappings", description = "Operations about mappings.")
@Path("mappings")
public class MappingsResource extends BasicDMPResource<MappingsResourceUtils, MappingService, Mapping> {

	/**
	 * Creates a new resource (controller service) for {@link Mapping}s with the provider of the mapping persistence service, the
	 * object mapper and metrics registry.
	 * 
	 * @param mappingServiceProviderArg the mapping persistence service provider
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	@Inject
	public MappingsResource(final MappingsResourceUtils pojoClassResourceUtilsArg, final DMPStatus dmpStatusArg) {

		super(pojoClassResourceUtilsArg, dmpStatusArg);
	}

	/**
	 * This endpoint returns a mapping as JSON representation for the provided mapping identifier.
	 * 
	 * @param id a mapping identifier
	 * @return a JSON representation of a mapping
	 */
	@ApiOperation(value = "get the mapping that matches the given id", notes = "Returns the Mapping object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "mapping identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a mapping as JSON representation and persists this mapping in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one mapping
	 * @return the persisted mapping as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new mapping", notes = "Returns a new Mapping object.", response = Mapping.class)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "mapping (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all mappings as JSON representation.
	 * 
	 * @return a list of all mappings as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all mappings ", notes = "Returns a list of Mapping objects.")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, transformation (component), input filter, output filter, input attribute paths and output attribute path
	 * of the mapping.
	 */
	@Override
	protected Mapping prepareObjectForUpdate(final Mapping objectFromJSON, final Mapping object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setTransformation(objectFromJSON.getTransformation());
		object.setInputFilter(objectFromJSON.getInputFilter());
		object.setOutputFilter(objectFromJSON.getOutputFilter());
		object.setInputAttributePaths(objectFromJSON.getInputAttributePaths());
		object.setOutputAttributePath(objectFromJSON.getOutputAttributePath());

		return object;
	}
}
