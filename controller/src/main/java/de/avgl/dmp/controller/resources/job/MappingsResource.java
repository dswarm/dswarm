package de.avgl.dmp.controller.resources.job;

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
import de.avgl.dmp.controller.resources.BasicDMPResource;
import de.avgl.dmp.controller.resources.job.utils.MappingsResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.model.job.proxy.ProxyMapping;
import de.avgl.dmp.persistence.service.job.MappingService;

/**
 * A resource (controller service) for {@link Mapping}s.
 * 
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/mappings", description = "Operations about mappings.")
@Path("mappings")
public class MappingsResource extends BasicDMPResource<MappingsResourceUtils, MappingService, ProxyMapping, Mapping> {

	/**
	 * Creates a new resource (controller service) for {@link Mapping}s with the provider of the mapping persistence service, the
	 * object mapper and metrics registry.
	 * 
	 * @param mappingServiceProviderArg the mapping persistence service provider
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	@Inject
	public MappingsResource(final ResourceUtilsFactory utilsFactory, final DMPStatus dmpStatusArg) throws DMPControllerException {

		super(utilsFactory.reset().get(MappingsResourceUtils.class), dmpStatusArg);
	}

	/**
	 * This endpoint returns a mapping as JSON representation for the provided mapping identifier.
	 * 
	 * @param id a mapping identifier
	 * @return a JSON representation of a mapping
	 */
	@ApiOperation(value = "get the mapping that matches the given id", notes = "Returns the Mapping object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the mapping (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find a mapping for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
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
	@ApiResponses(value = { @ApiResponse(code = 201, message = "mapping was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
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
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all available mappings (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any mapping, i.e., there are no mappings available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * This endpoint deletes a mapping that matches the given id.
	 * 
	 * @param id a mapping identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 *         went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "delete mapping that matches the given id", notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "mapping was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a mapping for the given id"),
			@ApiResponse(code = 409, message = "mapping couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "mapping identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}

	/**
	 * This endpoint consumes a mapping as JSON representation and updates this mapping in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one mapping
	 * @param id a mapping identifier
	 * @return the updated mapping as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "update mapping with given id ", notes = "Returns an updated Mapping object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "mapping was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a mapping for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "mapping (as JSON)", required = true) final String jsonObjectString,
			@ApiParam(value = "mapping identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.updateObject(jsonObjectString, id);
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
