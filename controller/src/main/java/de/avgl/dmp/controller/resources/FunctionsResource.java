package de.avgl.dmp.controller.resources;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.spi.LinkedKeyBinding;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.services.FunctionService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

@RequestScoped
@Path("functions")
public class FunctionsResource {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(FunctionsResource.class);

	@Context
	UriInfo uri;
	
	private final Provider<FunctionService> functionServiceProvider;

	private final ObjectMapper objectMapper;

	@Inject
	public FunctionsResource(
			final Provider<FunctionService> functionServiceProvider,
			final ObjectMapper objectMapper) {
		this.functionServiceProvider = functionServiceProvider;
		this.objectMapper = objectMapper;
	}

	private Response buildResponse(final String responseContent) {
		return Response.ok(responseContent).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllFunctions() throws DMPControllerException {

		LOG.debug("try to get functions");

		final FunctionService functionService = functionServiceProvider.get();

		final List<Function> functions = functionService.getObjects();

		if (functions == null) {

			LOG.debug("couldn't find functions");
			return Response.status(Status.NOT_FOUND).build();
		}

		if (functions.isEmpty()) {

			LOG.debug("there are no functions");
			return Response.status(Status.NOT_FOUND).build();
		}

		LOG.debug("got all functions = ' = '"
				+ ToStringBuilder.reflectionToString(functions) + "'");

		String resourcesJSON;

		try {

			resourcesJSON = objectMapper.writeValueAsString(functions);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException(
					"couldn't transform resources list object to JSON string.\n"
							+ e.getMessage());
		}

		LOG.debug("return all resources '" + resourcesJSON + "'");

		return buildResponse(resourcesJSON);
	}

	/**
	 * this endpoint consumes a function as JSON and writes this to the database
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createFunction(final String jsonObjectString)
			throws DMPControllerException {
		
		final Function function = addFunction(jsonObjectString);
		
		if (function == null) {
			throw new DMPControllerException("couldn't add function");
		}
		
		LOG.debug("added new function = '" + ToStringBuilder.reflectionToString(function) + "'");

		String functionJSON = null;
		
		try {
			functionJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(function);
		} catch (JsonProcessingException e) {
			throw new DMPControllerException("couldn't transform function to JSON string.\n" + e.getMessage());
		}

		final URI baseURI = uri.getRequestUri();
		final URI functionURI = URI.create(baseURI.toString() + "/" + function.getId());

		LOG.debug("return new function at '" + functionURI.toString() + "' with content = '" + functionJSON + "'");

		return Response.created(functionURI).entity(functionJSON).build();
	}

	private Function addFunction(final String functionJSONString) throws DMPControllerException {

		Function functionFromJSON = null;

		try {
			functionFromJSON = DMPPersistenceUtil.getJSONObjectMapper()
					.readValue(functionJSONString, Function.class);
		} catch (final JsonParseException e) {
			LOG.debug("something went wrong while deserializing the function JSON string.\n");
			throw new DMPControllerException("something went wrong while deserializing the function JSON string.\n" + e.getMessage());
		} catch (final JsonMappingException e) {
			LOG.debug("something went wrong while deserializing the function JSON string.\n");
			throw new DMPControllerException("something went wrong while deserializing the function JSON string.\n" + e.getMessage());
		} catch (IOException e) {
			LOG.debug("something went wrong while deserializing the function JSON string.\n");
			throw new DMPControllerException("something went wrong while deserializing the function JSON string.\n" + e.getMessage());
		}
		if (functionFromJSON == null) {

			throw new DMPControllerException("deserialized function is null");
		}

		final FunctionService functionService = functionServiceProvider.get();

		Function function = null;

		try {
			function = functionService.createObject();
		} catch (DMPPersistenceException e) {
			LOG.debug("something went wrong while function creation");
			throw new DMPControllerException("something went wrong while function creation\n" + e.getMessage());
		}

		if (function == null) {
			throw new DMPControllerException("function is null");
		}

		final String name = functionFromJSON.getName();
		if (name != null) {
			function.setName(name);
		} 
		
		final String description = functionFromJSON.getDescription();
		if (description != null) {
			function.setDescription(description);
		}
		
		final LinkedList<String> parameters = functionFromJSON.getParameters();
		if (parameters != null && parameters.size() > 0) {
			function.setParameters(parameters);
		}
		
		try {
			function = functionService.updateObject(function);
		} catch(DMPPersistenceException e) {
			LOG.debug("something went wrong while function update");
			throw new DMPControllerException("something went wrong while function update\n" + e.getMessage());
		}
		
		return function;
	}

}
