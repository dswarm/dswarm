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
import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.service.job.FilterService;


@RequestScoped
@Api(value = "/filters", description = "Operations about filters.")
@Path("filters")
public class FiltersResource extends BasicResource<FilterService, Filter, Long> {

	@Context
	UriInfo											uri;

	@Inject
	public FiltersResource(final Provider<FilterService> filterServiceProviderArg, final ObjectMapper objectMapper, final DMPStatus dmpStatus) {

		super(Filter.class, filterServiceProviderArg, objectMapper, dmpStatus);
	}

	@ApiOperation(value = "get the filter that matches the given id", notes = "Returns the Filter object that matches the given id.")
	@Override
	public Response getObject(@ApiParam(value = "filter identifier", required = true) final Long id) throws DMPControllerException {

		return super.getObject(id);
	}

	@ApiOperation(value = "create a new filter", notes = "Returns a new Filter object.", response = Filter.class)
	@Override
	public Response createObject(@ApiParam(value = "filter (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	@ApiOperation(value = "get all filters ", notes = "Returns a list of Filter objects.")
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	@Override
	protected Filter prepareObjectForUpdate(final Filter objectFromJSON, final Filter object) {

		object.setName(objectFromJSON.getName());

		return object;
	}
}
