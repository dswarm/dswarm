package de.avgl.dmp.controller.resources;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.services.FilterService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

@RequestScoped
@Api(value = "/filters", description = "Operations about filters.")
@Path("filters")
public class FiltersResource {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(FiltersResource.class);

	private final Provider<FilterService> filterServiceProvider;
	
	@Context
	UriInfo uri;

	@Inject
	public FiltersResource(
			final Provider<FilterService> filterServiceProvider,
			final ObjectMapper objectMapper) {
		this.filterServiceProvider = filterServiceProvider;
	}

	private Response buildResponse(final String responseContent) {
		return Response.ok(responseContent).build();
	}
	
	@ApiOperation(value = "get filter with id", notes = "returns json of the filter")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMapping(@PathParam("id") final Long id) throws DMPControllerException {
		
		LOG.debug("try to get mapping with id '" + id + "'");

		final FilterService filterService = filterServiceProvider.get();

		final Filter filter = filterService.getObject(id);

		if (filter == null) {

			LOG.debug("couldn't find filter '" + id + "'");

			return Response.status(Status.NOT_FOUND).build();
		}

		LOG.debug("got filter with id '" + id + "' = '" + ToStringBuilder.reflectionToString(filter) + "'");

		String filterJSON = null;

		try {

			filterJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(filter);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform filter to JSON string.\n" + e.getMessage());
		}

		LOG.debug("return mapping with id '" + id + "' = '" + filterJSON + "'");

		return buildResponse(filterJSON);
	}
	
	@ApiOperation(value = "writes filter given by json to db", notes = "returns the stored filter in json")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createMapping(final String jsonObjectString)
			throws DMPControllerException {
		
		final Filter filter = addFilter(jsonObjectString);
		
		if (filter == null) {
			throw new DMPControllerException("couldn't add filter");
		}
		
		LOG.debug("added new filter = '" + ToStringBuilder.reflectionToString(filter) + "'");

		String filterJSON = null;
		
		try {
			filterJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(filter);
		} catch (JsonProcessingException e) {
			throw new DMPControllerException("couldn't transform filter to JSON string.\n" + e.getMessage());
		}

		final URI baseURI = uri.getRequestUri();
		final URI filterURI = URI.create(baseURI.toString() + "/" + filter.getId());

		LOG.debug("return new mapping at '" + filterURI.toString() + "' with content = '" + filterJSON + "'");

		return Response.created(filterURI).entity(filterJSON).build();
	}

	private Filter addFilter(final String filterJSONString) throws DMPControllerException {

		Filter filterFromJSON = null;

		try {
			filterFromJSON = DMPPersistenceUtil.getJSONObjectMapper()
					.readValue(filterJSONString, Filter.class);
		} catch (final JsonParseException e) {
			LOG.debug("something went wrong while deserializing the filter JSON string.\n");
			throw new DMPControllerException("something went wrong while deserializing the filter JSON string.\n" + e.getMessage());
		} catch (final JsonMappingException e) {
			LOG.debug("something went wrong while deserializing the filter JSON string.\n");
			throw new DMPControllerException("something went wrong while deserializing the filter JSON string.\n" + e.getMessage());
		} catch (IOException e) {
			LOG.debug("something went wrong while deserializing the filter JSON string.\n");
			throw new DMPControllerException("something went wrong while deserializing the filter JSON string.\n" + e.getMessage());
		}
		if (filterFromJSON == null) {

			throw new DMPControllerException("deserialized filter is null");
		}

		final FilterService filterService = filterServiceProvider.get();

		Filter filter = null;

		try {
			filter = filterService.createObject();
		} catch (DMPPersistenceException e) {
			LOG.debug("something went wrong while filter creation");
			throw new DMPControllerException("something went wrong while filter creation\n" + e.getMessage());
		}

		if (filter == null) {
			throw new DMPControllerException("filter is null");
		}

		final String name = filterFromJSON.getName();
		if (name != null) {
			filter.setName(name);
		}
		
		final String expression = filterFromJSON.getExpression();
		if (expression != null) {
			filter.setExpression(expression);
		}
		
		try {
			filter = filterService.updateObjectTransactional(filter);
		} catch(DMPPersistenceException e) {
			LOG.debug("something went wrong while filter update");
			throw new DMPControllerException("something went wrong while filter update\n" + e.getMessage());
		}
		
		return filter;
	}
}
