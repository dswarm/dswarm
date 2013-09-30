package de.avgl.dmp.controller.resources;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;
import org.apache.commons.lang3.builder.ToStringBuilder;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.services.PersistenceServices;
import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.services.ConfigurationService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

@Path("configurations")
public class ConfigurationsResource {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ConfigurationsResource.class);

	@Context
	UriInfo											uri;

	private Response buildResponse(String responseContent) {
		return Response.ok(responseContent).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfigurations() throws DMPControllerException {

		LOG.debug("try to get configurations");

		final ConfigurationService configurationService = PersistenceServices.getInstance().getConfigurationService();

		final List<Configuration> configurations = configurationService.getObjects();

		if (configurations == null || configurations.isEmpty()) {

			LOG.debug("couldn't find configurations or there are no configurations");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		LOG.debug("got configurations = '" + ToStringBuilder.reflectionToString(configurations) + "'");

		Set<Configuration> configurationsSet = Sets.newHashSet();
		configurationsSet.addAll(configurations);

		String configurationsJSON = null;

		try {

			configurationsJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(configurationsSet);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform configurations set to JSON string.\n" + e.getMessage());
		}

		LOG.debug("return configurations = '" + configurationsJSON + "'");

		return buildResponse(configurationsJSON);
	}

	/**
	 * this endpoint consumes a configuration as JSON representation and writes this configuration persistent to the database
	 *
	 * @param jsonObjectString a JSON representation of one configuration
	 * @return
	 * @throws IOException
	 * @throws DMPConverterException
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createConfiguration(final String jsonObjectString) throws DMPControllerException {

		LOG.debug("try to create new configuration");

		final Configuration configuration = addConfiguration(jsonObjectString);

		if (configuration == null) {

			LOG.debug("couldn't add configuration ");

			throw new DMPControllerException("couldn't add configuration");
		}

		LOG.debug("added new configuration = '" + ToStringBuilder.reflectionToString(configuration) + "'");

		String configurationJSON = null;

		try {

			configurationJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(configuration);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform resource configuration to JSON string.\n" + e.getMessage());
		}

		URI baseURI = uri.getRequestUri();
		URI configurationURI = URI.create(baseURI.toString() + "/" + configuration.getId());

		LOG.debug("return new configuration at '" + configurationURI.toString() + "' with content '" + configurationJSON + "'");

		return Response.created(configurationURI).entity(configurationJSON).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfiguration(@PathParam("id") Long id) throws DMPControllerException {

		LOG.debug("try to get configuration with id '" + id + "'");

		final ConfigurationService configurationService = PersistenceServices.getInstance().getConfigurationService();

		final Configuration configuration = configurationService.getObject(id);

		if (configuration == null) {

			LOG.debug("couldn't find configuration '" + id + "'");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		LOG.debug("got configuration with id '" + id + "' = '" + ToStringBuilder.reflectionToString(configuration) + "'");

		String configurationJSON = null;

		try {

			configurationJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(configuration);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform configuration to JSON string.\n" + e.getMessage());
		}

		LOG.debug("return configuration with id '" + id + "' = '" + configurationJSON + "'");

		return buildResponse(configurationJSON);
	}

	@OPTIONS
	public Response getOptions() {

		return Response.ok().header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, OPTIONS, HEAD")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Origin, X-Requested-With, Content-Type").build();
	}

	@Path("/{id}")
	@OPTIONS
	public Response getConfigurationOptions() {

		return Response.ok().header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, OPTIONS, HEAD")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Origin, X-Requested-With, Content-Type").build();
	}

	private Configuration addConfiguration(final String configurationJSONString) throws DMPControllerException {

		Configuration configurationFromJSON = null;

		try {

			configurationFromJSON = DMPPersistenceUtil.getJSONObjectMapper().readValue(configurationJSONString, Configuration.class);
		} catch (final JsonParseException e) {

			LOG.debug("something went wrong while deserializing the configuration JSON string");

			throw new DMPControllerException("something went wrong while deserializing the configuration JSON string.\n" + e.getMessage());
		} catch (final JsonMappingException e) {

			LOG.debug("something went wrong while deserializing the configuration JSON string");

			throw new DMPControllerException("something went wrong while deserializing the configuration JSON string.\n" + e.getMessage());
		} catch (final IOException e) {

			LOG.debug("something went wrong while deserializing the configuration JSON string");

			throw new DMPControllerException("something went wrong while deserializing the configuration JSON string.\n" + e.getMessage());
		}

		if (configurationFromJSON == null) {

			throw new DMPControllerException("deserialized configuration is null");
		}

		final ConfigurationService configurationService = PersistenceServices.getInstance().getConfigurationService();

		Configuration configuration = null;

		try {

			configuration = configurationService.createObject();
		} catch (final DMPPersistenceException e) {

			LOG.debug("something went wrong while configuration creation");

			throw new DMPControllerException("something went wrong while configuration creation\n" + e.getMessage());
		}

		if (configuration == null) {

			throw new DMPControllerException("fresh configuration shouldn't be null");
		}

		final String name = configurationFromJSON.getName();

		if (name != null) {

			configuration.setName(name);
		}

		final String description = configurationFromJSON.getDescription();

		if (description != null) {

			configuration.setDescription(description);
		}

		final ObjectNode parameters = configurationFromJSON.getParameters();

		if (parameters != null && parameters.size() > 0) {

			configuration.setParameters(parameters);
		}

		try {

			configurationService.updateObjectTransactional(configuration);
		} catch (final DMPPersistenceException e) {

			LOG.debug("something went wrong while configuration updating");

			throw new DMPControllerException("something went wrong while configuration updating\n" + e.getMessage());
		}

		return configuration;
	}
}
