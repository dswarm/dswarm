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

import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.BasicDMPResource;
import de.avgl.dmp.controller.resources.job.utils.FiltersResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.service.job.FilterService;

/**
 * A resource (controller service) for {@link Filter}s.
 *
 * @author tgaengler
 * @author fniederlein
 */
@RequestScoped
@Api(value = "/filters", description = "Operations about filters.")
@Path("filters")
public class FiltersResource extends BasicDMPResource<FiltersResourceUtils, FilterService, Filter> {

	/**
	 * Creates a new resource (controller service) for {@link Filter}s with the provider of the filter persistence service, the
	 * object mapper and metrics registry.
	 *
	 * @param filterServiceProviderArg the filter persistence service provider
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	@Inject
	public FiltersResource(final ResourceUtilsFactory utilsFactory, final DMPStatus dmpStatusArg) throws DMPControllerException {

		super(utilsFactory.reset().get(FiltersResourceUtils.class), dmpStatusArg);
	}

	/**
	 * This endpoint returns a filter as JSON representation for the provided filter identifier.
	 *
	 * @param id a filter identifier
	 * @return a JSON representation of a filter
	 */
	@ApiOperation(value = "get the filter that matches the given id", notes = "Returns the Filter object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "filter identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a filter as JSON representation and persists this filter in the database.
	 *
	 * @param jsonObjectString a JSON representation of one filter
	 * @return the persisted filter as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new filter", notes = "Returns a new Filter object.", response = Filter.class)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "filter (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all filters as JSON representation.
	 *
	 * @return a list of all filters as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all filters ", notes = "Returns a list of Filter objects.")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * This endpoint consumes a filter as JSON representation and update this filter in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one filter
	 * @param id a filter identifier
	 * @return the updated filter as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "update filter with given id ", notes = "Returns a new Filter object.")
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "filter (as JSON)", required = true) final String jsonObjectString, 
			@ApiParam(value = "filter identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.updateObject(jsonObjectString, id);
	}
	
	/**
	 * {@inheritDoc}<br/>
	 * Updates the name and expression of the filter.
	 */
	@Override
	protected Filter prepareObjectForUpdate(final Filter objectFromJSON, final Filter object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setExpression(objectFromJSON.getExpression());

		return object;
	}
}
