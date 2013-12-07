package de.avgl.dmp.controller.resources;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

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
import de.avgl.dmp.persistence.model.job.AttributePath;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.services.MappingService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

@RequestScoped
@Api(value = "/mappings", description = "Operations about mappings.")
@Path("mappings")
public class MappingsResource {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(MappingsResource.class);

	private final Provider<MappingService> mappingServiceProvider;
	
	@Context
	UriInfo uri;

	@Inject
	public MappingsResource(
			final Provider<MappingService> mappingServiceProvider,
			final ObjectMapper objectMapper) {
		this.mappingServiceProvider = mappingServiceProvider;
	}

	private Response buildResponse(final String responseContent) {
		return Response.ok(responseContent).build();
	}
	
	@ApiOperation(value = "get mapping with id", notes = "returns json of the mapping")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMapping(@PathParam("id") final Long id) throws DMPControllerException {
		
		LOG.debug("try to get mapping with id '" + id + "'");

		final MappingService mappingService = mappingServiceProvider.get();

		final Mapping mapping = mappingService.getObject(id);

		if (mapping == null) {

			LOG.debug("couldn't find mapping '" + id + "'");

			return Response.status(Status.NOT_FOUND).build();
		}

		LOG.debug("got mapping with id '" + id + "' = '" + ToStringBuilder.reflectionToString(mapping) + "'");

		String mappingJSON = null;

		try {

			mappingJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(mapping);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform mapping to JSON string.\n" + e.getMessage());
		}

		LOG.debug("return mapping with id '" + id + "' = '" + mappingJSON + "'");

		return buildResponse(mappingJSON);
	}
	
	@ApiOperation(value = "writes mapping given by json to db", notes = "returns the stored mapping in json")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createMapping(final String jsonObjectString)
			throws DMPControllerException {
		
		final Mapping mapping = addMapping(jsonObjectString);
		
		if (mapping == null) {
			throw new DMPControllerException("couldn't add mapping");
		}
		
		LOG.debug("added new mapping = '" + ToStringBuilder.reflectionToString(mapping) + "'");

		String mappingJSON = null;
		
		try {
			mappingJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(mapping);
		} catch (JsonProcessingException e) {
			throw new DMPControllerException("couldn't transform mapping to JSON string.\n" + e.getMessage());
		}

		final URI baseURI = uri.getRequestUri();
		final URI mappingURI = URI.create(baseURI.toString() + "/" + mapping.getId());

		LOG.debug("return new mapping at '" + mappingURI.toString() + "' with content = '" + mappingJSON + "'");

		return Response.created(mappingURI).entity(mappingJSON).build();
	}

	private Mapping addMapping(final String mappingJSONString) throws DMPControllerException {

		Mapping mappingFromJSON = null;

		try {
			mappingFromJSON = DMPPersistenceUtil.getJSONObjectMapper()
					.readValue(mappingJSONString, Mapping.class);
		} catch (final JsonParseException e) {
			LOG.debug("something went wrong while deserializing the mapping JSON string.\n");
			throw new DMPControllerException("something went wrong while deserializing the mapping JSON string.\n" + e.getMessage());
		} catch (final JsonMappingException e) {
			LOG.debug("something went wrong while deserializing the mapping JSON string.\n");
			throw new DMPControllerException("something went wrong while deserializing the mapping JSON string.\n" + e.getMessage());
		} catch (IOException e) {
			LOG.debug("something went wrong while deserializing the mapping JSON string.\n");
			throw new DMPControllerException("something went wrong while deserializing the mapping JSON string.\n" + e.getMessage());
		}
		if (mappingFromJSON == null) {

			throw new DMPControllerException("deserialized mapping is null");
		}

		final MappingService mappingService = mappingServiceProvider.get();

		Mapping mapping = null;

		try {
			mapping = mappingService.createObject();
		} catch (DMPPersistenceException e) {
			LOG.debug("something went wrong while mapping creation");
			throw new DMPControllerException("something went wrong while mapping creation\n" + e.getMessage());
		}

		if (mapping == null) {
			throw new DMPControllerException("mapping is null");
		}

		final String name = mappingFromJSON.getName();
		if (name != null) {
			mapping.setName(name);
		}
		
		final Component component = mappingFromJSON.getTransformation();
		if (component != null) {
			mapping.setTransformation(component);
		}
		
		final Set<AttributePath> inputAttributePaths = mappingFromJSON.getInputAttributePaths();
		if (inputAttributePaths != null) {
			mapping.setInputAttributePaths(inputAttributePaths);
		}
		
		final AttributePath outputAttributePath = mapping.getOutputAttributePath();
		if (outputAttributePath != null) {
			mapping.setOutputAttributePath(outputAttributePath);
		}
		
		final Filter inputFilter = mappingFromJSON.getInputFilter();
		if (inputFilter != null) {
			mapping.setInputFilter(inputFilter);
		}
		
		final Filter outputFilter = mappingFromJSON.getOutputFilter();
		if (outputFilter != null) {
			mapping.setOutputFilter(outputFilter);
		}
		
		try {
			mapping = mappingService.updateObjectTransactional(mapping);
		} catch(DMPPersistenceException e) {
			LOG.debug("something went wrong while mapping update");
			throw new DMPControllerException("something went wrong while mapping update\n" + e.getMessage());
		}
		
		return mapping;
	}
}
