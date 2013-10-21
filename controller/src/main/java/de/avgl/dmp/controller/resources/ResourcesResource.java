package de.avgl.dmp.controller.resources;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.common.net.HttpHeaders;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.eventbus.CSVConverterEvent;
import de.avgl.dmp.controller.eventbus.XMLConverterEvent;
import de.avgl.dmp.controller.eventbus.XMLSchemaEvent;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.controller.utils.DMPControllerUtils;
import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.CSVResourceFlowFactory;
import de.avgl.dmp.converter.flow.CSVSourceResourceCSVJSONPreviewFlow;
import de.avgl.dmp.converter.flow.CSVSourceResourceCSVPreviewFlow;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.jsonschema.JSRoot;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.services.ConfigurationService;
import de.avgl.dmp.persistence.services.InternalService;
import de.avgl.dmp.persistence.services.ResourceService;
import de.avgl.dmp.persistence.services.SchemaService;


@RequestScoped
@Path("resources")
public class ResourcesResource {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ResourcesResource.class);


	@Context
	UriInfo											uri;

	private final Provider<EventBus>				eventBusProvider;

	private final Provider<ResourceService>			resourceServiceProvider;

	private final Provider<ConfigurationService>	configurationServiceProvider;

	private final Provider<InternalService<Model>>			internalServiceProvider;

	private final Provider<SchemaService>			schemaServiceProvider;

	private final DMPStatus							dmpStatus;

	private final ObjectMapper						objectMapper;


	@Inject
	public ResourcesResource(final DMPStatus dmpStatus,
							 final ObjectMapper objectMapper,
							 final Provider<ResourceService> resourceServiceProvider,
							 final Provider<ConfigurationService> configurationServiceProvider,
							 final Provider<InternalService<Model>> internalServiceProvider,
							 final Provider<SchemaService> schemaServiceProvider,
							 final Provider<EventBus> eventBusProvider) {

		this.eventBusProvider = eventBusProvider;
		this.resourceServiceProvider = resourceServiceProvider;
		this.configurationServiceProvider = configurationServiceProvider;
		this.internalServiceProvider = internalServiceProvider;
		this.schemaServiceProvider = schemaServiceProvider;
		this.dmpStatus = dmpStatus;
		this.objectMapper = objectMapper;
	}

	private Response buildResponse(final String responseContent) {

		return Response.ok(responseContent).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}

	private Response buildResponseCreated(final String responseContent, final URI responseURI) {

		return Response.created(responseURI).entity(responseContent).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadResource(@FormDataParam("file") final InputStream uploadedInputStream,
			@FormDataParam("file") final FormDataContentDisposition fileDetail, @FormDataParam("name") final String name,
			@FormDataParam("description") final String description) throws DMPControllerException {
		final Timer.Context context = dmpStatus.createNewResource();

		LOG.debug("try to create new resource '" + name + "' for file '" + fileDetail.getFileName() + "'");

		final Resource resource = createResource(uploadedInputStream, fileDetail, name, description);

		if (resource == null) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't create new resource");
		}

		LOG.debug("created new resource '" + name + "' for file '" + fileDetail.getFileName() + "' = '"
				+ ToStringBuilder.reflectionToString(resource) + "'");

		String resourceJSON = null;

		try {

			resourceJSON = objectMapper.writeValueAsString(resource);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform resource object to JSON string");
		}

		final URI baseURI = uri.getRequestUri();
		final URI resourceURI = URI.create(baseURI.toString() + "/" + resource.getId());

		LOG.debug("created new resource at '" + resourceURI.toString() + "' with content '" + resourceJSON + "'");

		dmpStatus.stop(context);
		return buildResponseCreated(resourceJSON, resourceURI);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResources() throws DMPControllerException {
		final Timer.Context context = dmpStatus.getAllResources();

		LOG.debug("try to get all resources");

		final ResourceService resourceService = resourceServiceProvider.get();

		final List<Resource> resources = resourceService.getObjects();

		if (resources == null) {

			LOG.debug("couldn't find resources");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		if (resources.isEmpty()) {

			LOG.debug("there are no resources");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		LOG.debug("got all resources = ' = '" + ToStringBuilder.reflectionToString(resources) + "'");

		String resourcesJSON = null;

		try {

			resourcesJSON = objectMapper.writeValueAsString(resources);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform resources list object to JSON string.\n" + e.getMessage());
		}

		LOG.debug("return all resources '" + resourcesJSON + "'");

		dmpStatus.stop(context);
		return buildResponse(resourcesJSON);
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResource(@PathParam("id") final Long id) throws DMPControllerException {
		final Timer.Context context = dmpStatus.getSingleResource();

		final Optional<Resource> resourceOptional = fetchResource(id);

		if (!resourceOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		final Resource resource = resourceOptional.get();

		String resourceJSON = null;

		try {

			resourceJSON = objectMapper.writeValueAsString(resource);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform resource object to JSON string.\n" + e.getMessage());
		}

		LOG.debug("return resource with id '" + id + "' and content '" + resourceJSON + "'");

		dmpStatus.stop(context);
		return buildResponse(resourceJSON);
	}


	@GET
	@Path("/{id}/lines")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourcePlain(@PathParam("id") final Long id,
									 @DefaultValue("50") @QueryParam("atMost") final int atMost,
									 @DefaultValue("UTF-8") @QueryParam("encoding") final String encoding)
			throws DMPControllerException {
		final Timer.Context context = dmpStatus.getSingleResource();

		final Optional<Resource> resourceOptional = fetchResource(id);

		if (!resourceOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		final Resource resource = resourceOptional.get();

		final JsonNode path = resource.getAttributes().get("path");

		if (path == null) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		final String filePath = path.asText();

		final List<String> lines;
		try {
			lines = Files.readLines(new File(filePath), Charset.forName(encoding), new LineProcessor<List<String>>() {
				private final ImmutableList.Builder<String> lines = ImmutableList.builder();
				private int linesProcessed = 1;

				@Override
				public boolean processLine(String line) throws IOException {
					if (linesProcessed ++> atMost) {

						return false;
					}

					lines.add(line);
					return true;
				}

				@Override
				public List<String> getResult() {
					return lines.build();
				}
			});
		} catch (IOException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't read file contents.\n" + e.getMessage());
		}

		Map<String, Object> jsonMap = new HashMap<String, Object>(1);
		jsonMap.put("lines", lines);
		jsonMap.put("name", resource.getName());
		jsonMap.put("description", resource.getDescription());

		final String plainJson;
		try {

			plainJson = objectMapper.writeValueAsString(jsonMap);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform resource contents to JSON array.\n" + e.getMessage());
		}



		dmpStatus.stop(context);
		return buildResponse(plainJson);
	}

	@GET
	@Path("/{id}/configurations")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfigurations(@PathParam("id") final Long id) throws DMPControllerException {
		final Timer.Context context = dmpStatus.getAllConfigurations();

		LOG.debug("try to get resource configurations for resource with id '" + id.toString() + "'");

		final Optional<Resource> resourceOptional = fetchResource(id);

		if (!resourceOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		final Resource resource = resourceOptional.get();

		LOG.debug("got resource with id '" + id.toString() + "' for resource configurations retrieval = '"
				+ ToStringBuilder.reflectionToString(resource) + "'");

		final Set<Configuration> configurations = resource.getConfigurations();

		if (configurations == null || configurations.isEmpty()) {

			LOG.debug("couldn't find configurations for resource '" + id + "'; or there are no configurations for this resource");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		LOG.debug("got resource configurations for resource with id '" + id.toString() + "' = '" + ToStringBuilder.reflectionToString(configurations)
				+ "'");

		String configurationsJSON = null;

		try {

			configurationsJSON = objectMapper.writeValueAsString(resource.getConfigurations());
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform resource configurations set to JSON string.\n" + e.getMessage());
		}

		LOG.debug("return resource configurations for resource with id '" + id.toString() + "' and content '" + configurationsJSON + "'");

		dmpStatus.stop(context);
		return buildResponse(configurationsJSON);
	}

	@POST
	@Path("/{id}/configurations")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addConfiguration(@PathParam("id") final Long id, final String jsonObjectString) throws DMPControllerException {
		final Timer.Context context = dmpStatus.createNewConfiguration();

		LOG.debug("try to create new configuration for resource with id '" + id + "'");

		final Optional<Resource> resourceOptional = fetchResource(id);

		if (!resourceOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		LOG.debug("try to add new configuration to resource with id '" + id + "'");

		final Resource resource = resourceOptional.get();

		final Configuration configuration = addConfiguration(resource, jsonObjectString);

		if (configuration == null) {

			LOG.debug("couldn't add configuration to resource with id '" + id + "'");

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't add configuration to resource with id '" + id + "'");
		}

		LOG.debug("added new configuration to resource with id '" + id + "' = '" + ToStringBuilder.reflectionToString(configuration) + "'");

		final JsonNode storageType = configuration.getParameters().get("storage_type");
		if (storageType != null) {
			if ("schema".equals(storageType.asText())) {

				eventBusProvider.get().post(new XMLSchemaEvent(configuration, resource));
			} else if ("csv".equals(storageType.asText())) {

				eventBusProvider.get().post(new CSVConverterEvent(configuration, resource));
			} else if ("xml".equals(storageType.asText())) {

				eventBusProvider.get().post(new XMLConverterEvent(configuration, resource));
			}
		}

		String configurationJSON = null;

		try {

			configurationJSON = objectMapper.writeValueAsString(configuration);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform resource configuration to JSON string.\n" + e.getMessage());
		}

		final URI baseURI = uri.getRequestUri();
		final URI configurationURI = URI.create(baseURI.toString() + "/" + configuration.getId());

		LOG.debug("return new configuration at '" + configurationURI.toString() + "' with content '" + configurationJSON + "'");

		dmpStatus.stop(context);
		return buildResponseCreated(configurationJSON, configurationURI);
	}

	@GET
	@Path("/{id}/configurations/{configurationid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfiguration(@PathParam("id") final Long id, @PathParam("configurationid") final Long configurationId)
			throws DMPControllerException {
		final Timer.Context context = dmpStatus.getSingleConfiguration();

		final Optional<Configuration> configurationOptional = fetchConfiguration(id, configurationId);

		if (!configurationOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		String configurationJSON = null;

		try {

			configurationJSON = objectMapper.writeValueAsString(configurationOptional.get());
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform resource configuration to JSON string.\n" + e.getMessage());
		}

		LOG.debug("return configuration with id '" + configurationId + "' for resource with id '" + id + "' and content '" + configurationJSON + "'");

		dmpStatus.stop(context);
		return buildResponse(configurationJSON);
	}

	@GET
	@Path("/{id}/configurations/{configurationid}/schema")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfigurationSchema(@PathParam("id") final Long id, @PathParam("configurationid") final Long configurationId)
			throws DMPControllerException {
		final Timer.Context context = dmpStatus.getConfigurationSchema();

		LOG.debug("try to get schema for configuration with id '" + configurationId + "' for resource with id '" + id + "'");

		final Optional<Configuration> configurationOptional = fetchConfiguration(id, configurationId);

		if (!configurationOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		String schemaJSON;

		final Optional<JSRoot> rootOptional = schemaServiceProvider.get().getSchema(id, configurationId);
		if (rootOptional.isPresent()) {

			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			try {

				rootOptional.get().render(objectMapper, outputStream);
				schemaJSON = outputStream.toString("UTF-8");

			} catch (IOException e) {

				dmpStatus.stop(context);
				throw new DMPControllerException("couldn't transform resource configuration schema to JSON string.\n" + e.getMessage());
			}

		} else {

			final Optional<Set<String>> schemaOptional = internalServiceProvider.get().getSchema(id, configurationId);

			if (!schemaOptional.isPresent()) {

				LOG.debug("couldn't find schema");

				dmpStatus.stop(context);
				return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
			}

			//TODO: wouldn't work with XML
			final Map<String, Map<String, String>> schema = Maps.newHashMap();
			final Map jsonMap = Maps.newHashMap();

			for (final String schemaProp : schemaOptional.get()) {
				final Map<String, String> schemaPropMap = Maps.newHashMap();
				schemaPropMap.put("type", "string");
				schema.put(schemaProp, schemaPropMap);
			}

			jsonMap.put("title", configurationOptional.get().getName());
			jsonMap.put("type", "object");
			jsonMap.put("properties", schema);

			try {

				schemaJSON = objectMapper.writeValueAsString(jsonMap);
			} catch (final JsonProcessingException e) {

				dmpStatus.stop(context);
				throw new DMPControllerException("couldn't transform resource configuration to JSON string.\n" + e.getMessage());
			}
		}

		LOG.debug("return schema for configuration with id '" + configurationId + "' for resource with id '" + id + "' and content '" + schemaJSON + "'");

		dmpStatus.stop(context);
		return buildResponse(schemaJSON);
	}

	@GET
	@Path("/{id}/configurations/{configurationid}/data")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfigurationData(@PathParam("id") final Long id, @PathParam("configurationid") final Long configurationId,
		@QueryParam("atMost") final Integer atMost)
			throws DMPControllerException {
		final Timer.Context context = dmpStatus.getConfigurationData();

		LOG.debug("try to get schema for configuration with id '" + configurationId + "' for resource with id '" + id + "'");

		final Optional<Configuration> configurationOptional = fetchConfiguration(id, configurationId);

		if (!configurationOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		final Optional<Map<String, Model>> maybeTriples = internalServiceProvider.get().getObjects(id, configurationId, Optional.fromNullable(atMost));

		if (!maybeTriples.isPresent()) {

			LOG.debug("couldn't find data");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		final Map<String, Model> triples = maybeTriples.get();
		final ObjectNode jsonList = objectMapper.createObjectNode();

		for (final Map.Entry<String, Model> record : triples.entrySet()) {
			final Model model = record.getValue();
			jsonList.put(record.getKey(), model.toJSON());
		}

		String configurationJSON = null;

		try {

			configurationJSON = objectMapper.writeValueAsString(jsonList);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform resource configuration to JSON string.\n" + e.getMessage());
		}

		LOG.debug("return data for configuration with id '" + configurationId + "' for resource with id '" + id + "' and content '" + configurationJSON + "'");

		dmpStatus.stop(context);
		return buildResponse(configurationJSON);
	}

	@POST
	@Path("/{id}/configurationpreview")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response csvPreviewConfiguration(@PathParam("id") final Long id, final String jsonObjectString) throws DMPControllerException {
		final Timer.Context context = dmpStatus.configurationsPreview();

		LOG.debug("try to apply configuration for resource with id '" + id + "'");
		LOG.debug("try to recieve resource with id '" + id + "' for csv configuration preview");

		final Optional<Resource> resourceOptional = fetchResource(id);

		if (!resourceOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		final Resource resource = resourceOptional.get();

		LOG.debug("found resource with id '" + id + "' for csv configuration preview = '" + ToStringBuilder.reflectionToString(resource) + "'");
		LOG.debug("try to apply configuration to resource with id '" + id + "'");

		final String result = applyConfigurationForCSVPreview(resource, jsonObjectString);

		if (result == null) {

			LOG.debug("couldn't apply configuration to resource with id '" + id + "'");

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't apply configuration to resource with id '" + id + "'");
		}

		LOG.debug("applied configuration to resource with id '" + id + "'");

		dmpStatus.stop(context);
		return buildResponse(result);
	}

	@POST
	@Path("/{id}/configurationpreview")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response csvJSONPreviewConfiguration(@PathParam("id") final Long id, final String jsonObjectString) throws DMPControllerException {
		final Timer.Context context = dmpStatus.configurationsPreview();

		LOG.debug("try to apply configuration for resource with id '" + id + "'");
		LOG.debug("try to recieve resource with id '" + id + "' for csv json configuration preview");

		final Optional<Resource> resourceOptional = fetchResource(id);

		if (!resourceOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}

		final Resource resource = resourceOptional.get();

		LOG.debug("found resource with id '" + id + "' for csv json configuration preview = '" + ToStringBuilder.reflectionToString(resource) + "'");
		LOG.debug("try to apply configuration to resource with id '" + id + "'");

		final String result = applyConfigurationForCSVJSONPreview(resource, jsonObjectString);

		if (result == null) {

			LOG.debug("couldn't apply configuration to resource with id '" + id + "'");

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't apply configuration to resource with id '" + id + "'");
		}

		LOG.debug("applied configuration to resource with id '" + id + "'");

		dmpStatus.stop(context);
		return buildResponse(result);
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

	private Optional<Resource> fetchResource(final long resourceId) {

		LOG.debug("try to recieve resource with id '" + resourceId + "'");

		final ResourceService resourceService = resourceServiceProvider.get();

		final Resource resource = resourceService.getObject(resourceId);

		if (resource == null) {

			LOG.debug("couldn't find resource");

			return Optional.absent();
		}

		LOG.debug("found resource with id '" + resourceId + "' = '" + ToStringBuilder.reflectionToString(resource) + "'");

		return Optional.of(resource);
	}

	private Optional<Configuration> fetchConfiguration(final long resourceId, final long configurationId) {
		LOG.debug("try to get configuration with id '" + configurationId + "' for resource with id '" + resourceId + "'");

		final Optional<Resource> resourceOptional = fetchResource(resourceId);

		if (!resourceOptional.isPresent()) {

			return Optional.absent();
		}

		final Resource resource = resourceOptional.get();

		final Set<Configuration> configurations = resource.getConfigurations();

		if (configurations == null || configurations.isEmpty()) {

			LOG.debug("couldn't find configurations for resource '" + resourceId + "'; or there are no configurations for this resource");

			return Optional.absent();
		}

		final Configuration configuration = resource.getConfiguration(configurationId);

		if (configuration == null) {

			LOG.debug("couldn't find configuration '" + configurationId + "' for resource '" + resourceId + "'");

			return Optional.absent();
		}

		LOG.debug("got configuration with id '" + configurationId + "' for resource with id '" + resourceId + "' = '"
				+ ToStringBuilder.reflectionToString(configuration) + "'");

		return Optional.of(configuration);
	}

	private Resource createResource(final InputStream uploadInputedStream, final FormDataContentDisposition fileDetail, final String name,
			final String description) throws DMPControllerException {

		final File file = DMPControllerUtils.writeToFile(uploadInputedStream, fileDetail.getFileName(), "resources");

		final ResourceService resourceService = resourceServiceProvider.get();

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

		final ObjectNode attributes = new ObjectNode(objectMapper.getNodeFactory());
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

		final ConfigurationService configurationService = configurationServiceProvider.get();

		final Configuration configuration;
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

		configuration.addResource(resource);

		try {

			configurationService.updateObjectTransactional(configuration);
		} catch (final DMPPersistenceException e) {

			LOG.debug("something went wrong while configuration updating");

			throw new DMPControllerException("something went wrong while configuration updating\n" + e.getMessage());
		}

		final ResourceService resourceService = resourceServiceProvider.get();

		try {

			resourceService.updateObjectTransactional(resource);
		} catch (final DMPPersistenceException e) {

			LOG.debug("something went wrong while resource updating for configuration");

			throw new DMPControllerException("something went wrong while resource updating for configuration\n" + e.getMessage());
		}

		return configuration;
	}

	private String applyConfigurationForCSVPreview(final Resource resource, final String configurationJSONString) throws DMPControllerException {

		final Configuration configurationFromJSON = getConfiguration(configurationJSONString);

		if (resource.getAttributes() == null) {

			throw new DMPControllerException("there are no attributes available at resource '" + resource.getId() + "'");
		}

		final JsonNode filePathNode = resource.getAttribute("path");

		if (filePathNode == null) {

			throw new DMPControllerException("couldn't determine file path");
		}

		CSVSourceResourceCSVPreviewFlow flow;

		try {
			flow = CSVResourceFlowFactory.fromConfiguration(configurationFromJSON, CSVSourceResourceCSVPreviewFlow.class);
		} catch (DMPConverterException e) {

			throw new DMPControllerException(e.getMessage());
		}

		try {
			return flow.applyFile(filePathNode.asText());
		} catch (DMPConverterException e) {
			throw new DMPControllerException(e.getMessage());
		}
	}

	private String applyConfigurationForCSVJSONPreview(final Resource resource, final String configurationJSONString) throws DMPControllerException {

		final Configuration configurationFromJSON = getConfiguration(configurationJSONString);

		if (resource.getAttributes() == null) {

			throw new DMPControllerException("there are no attributes available at resource '" + resource.getId() + "'");
		}

		final JsonNode filePathNode = resource.getAttribute("path");

		if (filePathNode == null) {

			throw new DMPControllerException("couldn't determine file path");
		}

		CSVSourceResourceCSVJSONPreviewFlow flow;

		try {
			flow = CSVResourceFlowFactory.fromConfiguration(configurationFromJSON, CSVSourceResourceCSVJSONPreviewFlow.class);
		} catch (DMPConverterException e) {

			throw new DMPControllerException(e.getMessage());
		}

		flow.withLimit(50);

		try {
			return flow.applyFile(filePathNode.asText());
		} catch (DMPConverterException e) {

			throw new DMPControllerException(e.getMessage());
		}
	}

	private Configuration getConfiguration(final String configurationJSONString) throws DMPControllerException {

		Configuration configurationFromJSON = null;

		try {

			configurationFromJSON = objectMapper.readValue(configurationJSONString, Configuration.class);
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
}
