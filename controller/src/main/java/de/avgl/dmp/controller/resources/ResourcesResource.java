package de.avgl.dmp.controller.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
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

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.HttpHeaders;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.services.PersistenceServices;
import de.avgl.dmp.controller.utils.DMPControllerUtils;
import de.avgl.dmp.init.util.DMPUtil;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.services.ConfigurationService;
import de.avgl.dmp.persistence.services.ResourceService;

@Path("resources")
public class ResourcesResource {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ResourcesResource.class);

	@Context
	UriInfo											uri;

	private Response buildResponse(final String responseContent) {

		return Response.ok(responseContent).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadResource(@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("name") String name, @FormDataParam("description") String description) throws DMPControllerException {
		
		final Resource resource = createResource(uploadedInputStream, fileDetail, name, description);

		String resourceJSON = null;

		try {

			resourceJSON = DMPUtil.getJSONObjectMapper().writeValueAsString(resource);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform resource object to JSON string");
		}

		URI baseURI = uri.getRequestUri();
		URI resourceURI = URI.create(baseURI.toString() + "/" + resource.getId());

		return Response.created(resourceURI).entity(resourceJSON).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResources() throws DMPControllerException {

		final ResourceService resourceService = PersistenceServices.getInstance().getResourceService();

		final List<Resource> resources = resourceService.getObjects();

		if (resources == null) {

			LOG.debug("couldn't find resource");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		if (resources.isEmpty()) {

			LOG.debug("there are no resources");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		String resourcesJSON = null;

		try {

			resourcesJSON = DMPUtil.getJSONObjectMapper().writeValueAsString(resources);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform resources list object to JSON string.\n" + e.getMessage());
		}

		return buildResponse(resourcesJSON);
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResource(@PathParam("id") Long id) throws DMPControllerException {

		final Resource resource = getResourceInternal(id);

		if (resource == null) {

			LOG.debug("couldn't find resource");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		String resourceJSON = null;

		try {

			resourceJSON = DMPUtil.getJSONObjectMapper().writeValueAsString(resource);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform resource object to JSON string.\n" + e.getMessage());
		}

		return buildResponse(resourceJSON);
	}

	@GET
	@Path("/{id}/configurations")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfigurations(@PathParam("id") Long id) throws DMPControllerException {

		final ResourceService resourceService = PersistenceServices.getInstance().getResourceService();

		final Resource resource = resourceService.getObject(id);

		if (resource == null) {

			LOG.debug("couldn't find resource");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		final Set<Configuration> configurations = resource.getConfigurations();

		if (configurations == null || configurations.isEmpty()) {

			LOG.debug("couldn't find configurations for resource '" + id + "'; or there are no configurations for this resource");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		String configurationsJSON = null;

		try {

			configurationsJSON = DMPUtil.getJSONObjectMapper().writeValueAsString(resource.getConfigurations());
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform resource configurations set to JSON string.\n" + e.getMessage());
		}

		return buildResponse(configurationsJSON);
	}

	@POST
	@Path("/{id}/configurations")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response run(@PathParam("id") Long id, final String jsonObjectString) throws DMPControllerException {

		final Resource resource = getResourceInternal(id);

		if (resource == null) {

			LOG.debug("couldn't find resource");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		final Configuration configuration = addConfiguration(resource, jsonObjectString);

		String configurationsJSON = null;

		try {

			configurationsJSON = DMPUtil.getJSONObjectMapper().writeValueAsString(configuration);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform resource configuration to JSON string.\n" + e.getMessage());
		}

		return buildResponse(configurationsJSON);
	}
	
	@GET
	@Path("/{id}/configurations/{configurationid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfiguration(@PathParam("id") Long id, @PathParam("configurationid") Long configurationId) throws DMPControllerException {

		final ResourceService resourceService = PersistenceServices.getInstance().getResourceService();

		final Resource resource = resourceService.getObject(id);

		if (resource == null) {

			LOG.debug("couldn't find resource");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		final Set<Configuration> configurations = resource.getConfigurations();

		if (configurations == null || configurations.isEmpty()) {

			LOG.debug("couldn't find configurations for resource '" + id + "'; or there are no configurations for this resource");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}
		
		final Configuration configuration = resource.getConfiguration(configurationId);
		
		if(configuration == null) {
			
			LOG.debug("couldn't find configuration '" + configurationId + "' for resource '" + id + "'");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		String configurationJSON = null;

		try {

			configurationJSON = DMPUtil.getJSONObjectMapper().writeValueAsString(configuration);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform resource configuration to JSON string.\n" + e.getMessage());
		}

		return buildResponse(configurationJSON);
	}

	@OPTIONS
	public Response getOptions() {

		return Response.ok().header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, OPTIONS, HEAD")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Origin, X-Requested-With, Content-Type").build();
	}

	@OPTIONS
	@Path("/{id}")
	public Response getResourceOptions() {

		return Response.ok().header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, OPTIONS, HEAD")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Origin, X-Requested-With, Content-Type").build();
	}

	@OPTIONS
	@Path("/{id}/configurations")
	public Response getConfigurationsOptions() {

		return Response.ok().header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, OPTIONS, HEAD")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Origin, X-Requested-With, Content-Type").build();
	}
	
	@OPTIONS
	@Path("/{id}/configurations/{configurationid}")
	public Response getConfigurationOptions() {

		return Response.ok().header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, OPTIONS, HEAD")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Origin, X-Requested-With, Content-Type").build();
	}

	private Resource createResource(final InputStream uploadInputedStream, final FormDataContentDisposition fileDetail, final String name, final String description) throws DMPControllerException {

		final File file = DMPControllerUtils.writeToFile(uploadInputedStream, fileDetail.getFileName(), "resources");

		final ResourceService resourceService = PersistenceServices.getInstance().getResourceService();

		Resource resource = null;

		try {

			resource = resourceService.createObject();
		} catch (final DMPPersistenceException e) {

			LOG.debug("something went wrong while resource creation");

			throw new DMPControllerException("something went wrong while resource creation\n" + e.getMessage());
		}

		if (resource == null) {

			throw new DMPControllerException("fresh resource shouldn't be null");
		}

		resource.setName(name);

		if (description != null) {

			resource.setDescription(description);
		}

		resource.setType(ResourceType.FILE);

		String fileType = null;

		try {

			fileType = Files.probeContentType(file.toPath());
		} catch (IOException e1) {

			LOG.debug("couldn't determine file type from file '" + file.getAbsolutePath() + "'");
		}

		final ObjectNode attributes = new ObjectNode(DMPUtil.getJSONFactory());
		attributes.put("path", file.getAbsolutePath());

		if (fileType != null) {

			attributes.put("filetype", fileType);
		}

		attributes.put("filesize", fileDetail.getSize());

		resource.setAttributes(attributes);

		try {

			resourceService.updateObjectTransactional(resource);
		} catch (final DMPPersistenceException e) {

			LOG.debug("something went wrong while resource updating");

			throw new DMPControllerException("something went wrong while resource updating\n" + e.getMessage());
		}

		return resource;
	}

	private Configuration addConfiguration(final Resource resource, final String configurationJSONString) throws DMPControllerException {

		Configuration configurationFromJSON = null;

		try {

			configurationFromJSON = DMPUtil.getJSONObjectMapper().readValue(configurationJSONString, Configuration.class);
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

		configuration.setParameters(configurationFromJSON.getParameters());
		configuration.setResource(resource);

		try {

			configurationService.updateObjectTransactional(configuration);
		} catch (final DMPPersistenceException e) {

			LOG.debug("something went wrong while configuration updating");

			throw new DMPControllerException("something went wrong while configuration updating\n" + e.getMessage());
		}

		return configuration;
	}

	private Resource getResourceInternal(final Long id) {

		final ResourceService resourceService = PersistenceServices.getInstance().getResourceService();

		final Resource resource = resourceService.getObject(id);

		return resource;
	}
}
