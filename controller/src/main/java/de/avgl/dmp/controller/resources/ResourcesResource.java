package de.avgl.dmp.controller.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.HttpHeaders;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.services.PersistenceServices;
import de.avgl.dmp.controller.utils.DMPControllerUtils;
import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.CSVSourceResourcePreviewFlow;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.services.ConfigurationService;
import de.avgl.dmp.persistence.services.ResourceService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

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
			@FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("name") String name,
			@FormDataParam("description") String description) throws DMPControllerException {
		
		LOG.debug("try to create new resource '" + name + "' for file '" + fileDetail.getFileName() + "'");

		final Resource resource = createResource(uploadedInputStream, fileDetail, name, description);
		
		if(resource == null) {
			
			throw new DMPControllerException("couldn't create new resource");
		}
		
		LOG.debug("created new resource '" + name + "' for file '" + fileDetail.getFileName() + "' = '" + ToStringBuilder.reflectionToString(resource) + "'");

		String resourceJSON = null;

		try {

			resourceJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(resource);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform resource object to JSON string");
		}

		URI baseURI = uri.getRequestUri();
		URI resourceURI = URI.create(baseURI.toString() + "/" + resource.getId());
		
		LOG.debug("created new resource at '" + resourceURI.toString() + "' with content '" + resourceJSON + "'");

		return Response.created(resourceURI).entity(resourceJSON).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResources() throws DMPControllerException {
		
		LOG.debug("try to get all resources");

		final ResourceService resourceService = PersistenceServices.getInstance().getResourceService();

		final List<Resource> resources = resourceService.getObjects();

		if (resources == null) {

			LOG.debug("couldn't find resources");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		if (resources.isEmpty()) {

			LOG.debug("there are no resources");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}
		
		LOG.debug("got all resources = ' = '" + ToStringBuilder.reflectionToString(resources) + "'");

		String resourcesJSON = null;

		try {

			resourcesJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(resources);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform resources list object to JSON string.\n" + e.getMessage());
		}
		
		LOG.debug("return all resources '" + resourcesJSON + "'");

		return buildResponse(resourcesJSON);
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResource(@PathParam("id") Long id) throws DMPControllerException {
		
		LOG.debug("try to get resource with id '" + id.toString() + "'");

		final Resource resource = getResourceInternal(id);

		if (resource == null) {

			LOG.debug("couldn't find resource");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}
		
		LOG.debug("got resource with id '" + id.toString() + "' = '" + ToStringBuilder.reflectionToString(resource) + "'");

		String resourceJSON = null;

		try {

			resourceJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(resource);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform resource object to JSON string.\n" + e.getMessage());
		}
		
		LOG.debug("return resource with id '" + id + "' and content '" + resourceJSON + "'");

		return buildResponse(resourceJSON);
	}

	@GET
	@Path("/{id}/configurations")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfigurations(@PathParam("id") Long id) throws DMPControllerException {
		
		LOG.debug("try to get resource configurations for resource with id '" + id.toString() + "'");

		final ResourceService resourceService = PersistenceServices.getInstance().getResourceService();

		final Resource resource = resourceService.getObject(id);

		if (resource == null) {

			LOG.debug("couldn't find resource");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}
		
		LOG.debug("got resource with id '" + id.toString() + "' for resource configurations retrieval = '" + ToStringBuilder.reflectionToString(resource) + "'");

		final Set<Configuration> configurations = resource.getConfigurations();

		if (configurations == null || configurations.isEmpty()) {

			LOG.debug("couldn't find configurations for resource '" + id + "'; or there are no configurations for this resource");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}
		
		LOG.debug("got resource configurations for resource with id '" + id.toString() + "' = '" + ToStringBuilder.reflectionToString(configurations) + "'");

		String configurationsJSON = null;

		try {

			configurationsJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(resource.getConfigurations());
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform resource configurations set to JSON string.\n" + e.getMessage());
		}
		
		LOG.debug("return resource configurations for resource with id '" + id.toString() + "' and content '" + configurationsJSON + "'");

		return buildResponse(configurationsJSON);
	}

	@POST
	@Path("/{id}/configurations")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addConfiguration(@PathParam("id") Long id, final String jsonObjectString) throws DMPControllerException {
		
		LOG.debug("try to create new configuration for resource with id '" + id + "'");
		LOG.debug("try to recieve resource with id '" + id + "' for configuration creation");

		final Resource resource = getResourceInternal(id);

		if (resource == null) {

			LOG.debug("couldn't find resource");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}
		
		LOG.debug("found resource with id '" + id + "' for configuration creation = '" + ToStringBuilder.reflectionToString(resource) + "'");
		LOG.debug("try to add new configuration to resource with id '" + id + "'");

		final Configuration configuration = addConfiguration(resource, jsonObjectString);
		
		if(configuration == null) {
			
			LOG.debug("couldn't add configuration to resource with id '" + id + "'");
			
			throw new DMPControllerException("couldn't add configuration to resource with id '" + id + "'");
		}
		
		LOG.debug("added new configuration to resource with id '" + id + "' = '" + ToStringBuilder.reflectionToString(configuration) + "'");

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
	@Path("/{id}/configurations/{configurationid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfiguration(@PathParam("id") Long id, @PathParam("configurationid") Long configurationId)
			throws DMPControllerException {
		
		LOG.debug("try to get configuration with id '" + configurationId + "' for resource with id '" + id + "'");

		final ResourceService resourceService = PersistenceServices.getInstance().getResourceService();

		final Resource resource = resourceService.getObject(id);

		if (resource == null) {

			LOG.debug("couldn't find resource");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}
		
		LOG.debug("got resource with id '" + id + "' for configuration with id '" + configurationId + "' = '" + ToStringBuilder.reflectionToString(resource) + "'");

		final Set<Configuration> configurations = resource.getConfigurations();

		if (configurations == null || configurations.isEmpty()) {

			LOG.debug("couldn't find configurations for resource '" + id + "'; or there are no configurations for this resource");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		final Configuration configuration = resource.getConfiguration(configurationId);

		if (configuration == null) {

			LOG.debug("couldn't find configuration '" + configurationId + "' for resource '" + id + "'");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}
		
		LOG.debug("got configuration with id '" + configurationId + "' for resource with id '" + id + "' = '" + ToStringBuilder.reflectionToString(configuration) + "'");

		String configurationJSON = null;

		try {

			configurationJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(configuration);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform resource configuration to JSON string.\n" + e.getMessage());
		}
		
		LOG.debug("return configuration with id '" + configurationId + "' for resource with id '" + id + "' and content '" + configurationJSON + "'");

		return buildResponse(configurationJSON);
	}
	
	@POST
	@Path("/{id}/configurationpreview")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response previewConfiguration(@PathParam("id") Long id, final String jsonObjectString) throws DMPControllerException {
		
		LOG.debug("try to apply configuration for resource with id '" + id + "'");
		LOG.debug("try to recieve resource with id '" + id + "' for configuration preview");

		final Resource resource = getResourceInternal(id);

		if (resource == null) {

			LOG.debug("couldn't find resource");

			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}
		
		LOG.debug("found resource with id '" + id + "' for configuration preview = '" + ToStringBuilder.reflectionToString(resource) + "'");
		LOG.debug("try to apply configuration to resource with id '" + id + "'");

		final String result = applyConfiguration(resource, jsonObjectString);
		
		if(result == null) {
			
			LOG.debug("couldn't apply configuration to resource with id '" + id + "'");
			
			throw new DMPControllerException("couldn't apply configuration to resource with id '" + id + "'");
		}
		
		LOG.debug("applied configuration to resource with id '" + id + "'");

		return Response.ok().entity(result).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	
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
	
	@Path("/{id}/configurationpreview")
	@OPTIONS
	public Response getConfigurationPreviewOptions() {

		return Response.ok().header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, OPTIONS, HEAD")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Origin, X-Requested-With, Content-Type").build();
	}

	private Resource createResource(final InputStream uploadInputedStream, final FormDataContentDisposition fileDetail, final String name,
			final String description) throws DMPControllerException {

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

		// String fileType = null;

		// TODO: Files.probeContentType is JDK 1.7 only -> will be re-enabled when JDK 1.7 is support again
		// try {
		//
		// fileType = Files.probeContentType(file.toPath());
		// } catch (IOException e1) {
		//
		// LOG.debug("couldn't determine file type from file '" + file.getAbsolutePath() + "'");
		// }

		final ObjectNode attributes = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
		attributes.put("path", file.getAbsolutePath());

		// if (fileType != null) {
		//
		// attributes.put("filetype", fileType);
		// }

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

		final Configuration configurationFromJSON = getConfiguration(configurationJSONString);
		
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
		
		if(name != null) {
			
			configuration.setName(name);
		}
		
		final String description = configurationFromJSON.getDescription();
		
		if(description != null) {
			
			configuration.setDescription(description);
		}

		final ObjectNode parameters = configurationFromJSON.getParameters();
		
		if(parameters != null && parameters.size() > 0) {
			
			configuration.setParameters(parameters);
		}
		
		configuration.addResource(resource);

		try {

			configurationService.updateObjectTransactional(configuration);
		} catch (final DMPPersistenceException e) {

			LOG.debug("something went wrong while configuration updating");

			throw new DMPControllerException("something went wrong while configuration updating\n" + e.getMessage());
		}
		
		final ResourceService resourceService = PersistenceServices.getInstance().getResourceService();
		
		try {

			resourceService.updateObjectTransactional(resource);
		} catch (final DMPPersistenceException e) {

			LOG.debug("something went wrong while resource updating for configuration");

			throw new DMPControllerException("something went wrong while resource updating for configuration\n" + e.getMessage());
		}

		return configuration;
	}
	
	private String applyConfiguration(final Resource resource, final String configurationJSONString) throws DMPControllerException {
		
		final Configuration configurationFromJSON = getConfiguration(configurationJSONString);
		
		CSVSourceResourcePreviewFlow flow = null;
		
		try {
			flow = CSVSourceResourcePreviewFlow.fromConfiguration(configurationFromJSON);
		} catch (DMPConverterException e) {
			
			throw new DMPControllerException("something went wrong while apply configuration to resource");
		} catch (IOException e) {

			throw new DMPControllerException("something went wrong while apply configuration to resource");
		}
		
		if(flow == null) {

			throw new DMPControllerException("something went wrong while apply configuration to resource");
		}

		if(resource.getAttributes() == null) {
			
			throw new DMPControllerException("there are no attributes available at resource '" + resource.getId() + "'");
		}
		
		final JsonNode filePathNode = resource.getAttribute("path");
		
		if(filePathNode == null) {
			
			throw new DMPControllerException("couldn't determine file path");
		}
		
		final String result = flow.applyFile(filePathNode.asText());
		
		return result;
	}
	
	private Configuration getConfiguration(final String configurationJSONString) throws DMPControllerException {
		
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
		
		return configurationFromJSON;
	}

	private Resource getResourceInternal(final Long id) {

		final ResourceService resourceService = PersistenceServices.getInstance().getResourceService();

		final Resource resource = resourceService.getObject(id);

		return resource;
	}
}
