package de.avgl.dmp.controller.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.eventbus.CSVConverterEvent;
import de.avgl.dmp.controller.eventbus.SchemaEvent;
import de.avgl.dmp.controller.eventbus.XMLConverterEvent;
import de.avgl.dmp.controller.eventbus.XMLSchemaEvent;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.controller.utils.DMPControllerUtils;
import de.avgl.dmp.controller.utils.InternalSchemaDataUtil;
import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.CSVResourceFlowFactory;
import de.avgl.dmp.converter.flow.CSVSourceResourceCSVJSONPreviewFlow;
import de.avgl.dmp.converter.flow.CSVSourceResourceCSVPreviewFlow;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.types.Tuple;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.resource.ResourceService;

@RequestScoped
@Api(value = "/resources", description = "Operations about data resources.")
@Path("/resources")
public class ResourcesResource {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ResourcesResource.class);

	@Context
	UriInfo											uri;

	private final Provider<EventBus>				eventBusProvider;

	private final Provider<ResourceService>			resourceServiceProvider;

	private final Provider<ConfigurationService>	configurationServiceProvider;

	private final DMPStatus							dmpStatus;

	private final ObjectMapper						objectMapper;
	private final DataModelService                  modelService;
	private final InternalSchemaDataUtil			schemaDataUtil;

	@Inject
	public ResourcesResource(final DMPStatus dmpStatus, final ObjectMapper objectMapper, final Provider<ResourceService> resourceServiceProvider,
			final Provider<ConfigurationService> configurationServiceProvider, final Provider<EventBus> eventBusProvider,
			final DataModelService modelService, final InternalSchemaDataUtil schemaDataUtil) {

		this.eventBusProvider = eventBusProvider;
		this.resourceServiceProvider = resourceServiceProvider;
		this.configurationServiceProvider = configurationServiceProvider;
		this.dmpStatus = dmpStatus;
		this.objectMapper = objectMapper;
		this.modelService = modelService;
		this.schemaDataUtil = schemaDataUtil;
	}

	private Response buildResponse(final String responseContent) {

		return Response.ok(responseContent).build();
	}

	private Response buildResponseCreated(final String responseContent, final URI responseURI) {

		return Response.created(responseURI).entity(responseContent).build();
	}

	@POST
	@ApiOperation(value = "upload new data resource", notes = "Returns a new Resource object, when upload was successfully.", response = Resource.class)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadResource(
			@ApiParam(value = "file input stream", required = true) @FormDataParam("file") final InputStream uploadedInputStream,
			@ApiParam("file metadata") @FormDataParam("file") final FormDataContentDisposition fileDetail,
			@ApiParam(value = "resource name", required = true) @FormDataParam("name") final String name,
			@ApiParam("resource description") @FormDataParam("description") final String description) throws DMPControllerException {
		final Timer.Context context = dmpStatus.createNewResource();

		LOG.debug("try to create new resource '" + name + "' for file '" + fileDetail.getFileName() + "'");

		final Resource resource = createResource(uploadedInputStream, fileDetail, name, description);

		if (resource == null) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't create new resource");
		}

		LOG.debug("created new resource '" + name + "' for file '" + fileDetail.getFileName() + "' = '"
				+ ToStringBuilder.reflectionToString(resource) + "'");

		final String resourceJSON;

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

	@ApiOperation(value = "get all data resources", notes = "Returns a list of Resource objects.")
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
			return Response.status(Status.NOT_FOUND).build();
		}

		if (resources.isEmpty()) {

			LOG.debug("there are no resources");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		LOG.debug("got all resources = ' = '" + ToStringBuilder.reflectionToString(resources) + "'");

		final String resourcesJSON;

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

	@ApiOperation(value = "get the data resource that matches the given id", notes = "Returns the Resource object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResource(@ApiParam(value = "data resource identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {
		final Timer.Context context = dmpStatus.getSingleResource();

		final Optional<Resource> resourceOptional = schemaDataUtil.fetchResource(id);

		if (!resourceOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		final Resource resource = resourceOptional.get();

		final String resourceJSON;

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

	@ApiOperation(value = "get the lines of the data resource that matches the given id", notes = "Returns the lines of the data resource that matches the given id. The number of lines can be limited via the 'atMost' parameter. The encoding can be set via the 'encoding' parameter.")
	@GET
	@Path("/{id}/lines")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourcePlain(@ApiParam(value = "data resource identifier", required = true) @PathParam("id") final Long id,
			@ApiParam(value = "number of lines limit", defaultValue = "50") @DefaultValue("50") @QueryParam("atMost") final int atMost,
			@ApiParam(value = "data resource encoding", defaultValue = "50") @DefaultValue("UTF-8") @QueryParam("encoding") final String encoding)
			throws DMPControllerException {
		final Timer.Context context = dmpStatus.getSingleResource();

		final Optional<Resource> resourceOptional = schemaDataUtil.fetchResource(id);

		if (!resourceOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		final Resource resource = resourceOptional.get();

		final JsonNode path = resource.getAttributes().get("path");

		if (path == null) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		final String filePath = path.asText();

		final List<String> lines;
		try {
			lines = Files.readLines(new File(filePath), Charset.forName(encoding), new LineProcessor<List<String>>() {

				private final ImmutableList.Builder<String>	lines			= ImmutableList.builder();
				private int									linesProcessed	= 1;

				@Override
				public boolean processLine(final String line) throws IOException {
					if (linesProcessed++ > atMost) {

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
		} catch (final IOException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't read file contents.\n" + e.getMessage());
		}

		final Map<String, Object> jsonMap = new HashMap<>(1);
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

	@ApiOperation(value = "get all configurations of the data resource that matches the given id", notes = "Returns the configurations of the data resource that matches the given id.")
	@GET
	@Path("/{id}/configurations")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfigurations(@ApiParam(value = "data resource identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {
		final Timer.Context context = dmpStatus.getAllConfigurations();

		LOG.debug("try to get resource configurations for resource with id '" + id.toString() + "'");

		final Optional<Resource> resourceOptional = schemaDataUtil.fetchResource(id);

		if (!resourceOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		final Resource resource = resourceOptional.get();

		LOG.debug("got resource with id '" + id.toString() + "' for resource configurations retrieval = '"
				+ ToStringBuilder.reflectionToString(resource) + "'");

		final Set<Configuration> configurations = resource.getConfigurations();

		if (configurations == null || configurations.isEmpty()) {

			LOG.debug("couldn't find configurations for resource '" + id + "'; or there are no configurations for this resource");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		LOG.debug("got resource configurations for resource with id '" + id.toString() + "' = '" + ToStringBuilder.reflectionToString(configurations)
				+ "'");

		final String configurationsJSON;

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

	@ApiOperation(value = "add a new configuration to the data resource that matches the given id", notes = "Returns the new configuration that was added to the data resource that matches the given id.")
	@POST
	@Path("/{id}/configurations")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addConfiguration(@ApiParam(value = "data resource identifier", required = true) @PathParam("id") final Long id,
			@ApiParam(value = "configuration (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {
		final Timer.Context context = dmpStatus.createNewConfiguration();

		LOG.debug("try to create new configuration for resource with id '" + id + "'");

		final Optional<Resource> resourceOptional = schemaDataUtil.fetchResource(id);

		if (!resourceOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
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

		final JsonNode jsStorageType = configuration.getParameters().get("storage_type");
		if (jsStorageType != null) {
			final String storageType = jsStorageType.asText();
			try {
				final SchemaEvent.SchemaType type = SchemaEvent.SchemaType.fromString(storageType);
				eventBusProvider.get().post(new SchemaEvent(resource, configuration, type));
			} catch (final IllegalArgumentException e) {
				LOG.warn("could not determine schema type", e);
			}

			switch (storageType) {
				case "schema":

					eventBusProvider.get().post(new XMLSchemaEvent(configuration, resource));
					break;
				case "csv":

					eventBusProvider.get().post(new CSVConverterEvent(configuration, resource));
					break;
				case "xml":

					eventBusProvider.get().post(new XMLConverterEvent(configuration, resource));
					break;
			}
		}

		final String configurationJSON;

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

	@ApiOperation(value = "get the configuration of the data resource that matches the given data resource id and the given configuration id", notes = "Returns the configuration of the data resource that matches the given data resource id and the given configuration id.")
	@GET
	@Path("/{id}/configurations/{configurationid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfiguration(@ApiParam(value = "data resource identifier", required = true) @PathParam("id") final Long id,
			@ApiParam(value = "configuration identifier", required = true) @PathParam("configurationid") final Long configurationId)
			throws DMPControllerException {
		final Timer.Context context = dmpStatus.getSingleConfiguration();

		final Optional<Configuration> configurationOptional = schemaDataUtil.fetchConfiguration(id, configurationId);

		if (!configurationOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		final String configurationJSON;

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

	@ApiOperation(value = "get the schema of the data resource and configuration (= data model) that matches the given data resource id and the given configuration id", notes = "Returns the schema of the data resource and configuration (= data model) that matches the given data resource id and the given configuration id.")
	@GET
	@Path("/{id}/configurations/{configurationid}/schema")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfigurationSchema(@ApiParam(value = "data resource identifier", required = true) @PathParam("id") final Long id,
			@ApiParam(value = "configuration identifier", required = true) @PathParam("configurationid") final Long configurationId)
			throws DMPControllerException {
		final Timer.Context context = dmpStatus.getConfigurationSchema();

		LOG.debug("try to get schema for configuration with id '" + configurationId + "' for resource with id '" + id + "'");

		final List<DataModel> models = modelService.getObjects();

		Schema schema = null;
		for (final DataModel model : models) {
			if (model.getDataResource().getId().equals(id) && model.getConfiguration().getId().equals(configurationId)) {

				schema = model.getSchema();
				break;
			}
		}

		if (schema == null) {
			LOG.info("could not find schema for resource=" + id + " and configuration=" + configurationId);
			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		final String jsonString;
		try {
			jsonString = objectMapper.writeValueAsString(schema);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform resource configuration to JSON string.\n" + e.getMessage());
		}

		LOG.debug(String.format("return schema for configuration with id [%d] for resource with id [%d] and content [%s...]", configurationId, id,
				jsonString.substring(0, 30)));

		dmpStatus.stop(context);
		return buildResponse(jsonString);
	}

	@ApiOperation(value = "get the data of the data resource and configuration (= data model) that matches the given data resource id and the given configuration id", notes = "Returns the data of the data resource and configuration (= data model) that matches the given data resource id and the given configuration id.")
	@GET
	@Path("/{id}/configurations/{configurationid}/data")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfigurationData(@ApiParam(value = "data resource identifier", required = true) @PathParam("id") final Long id,
			@ApiParam(value = "configuration identifier", required = true) @PathParam("configurationid") final Long configurationId,
			@ApiParam("number of records limit") @QueryParam("atMost") final Integer atMost) throws DMPControllerException {
		final Timer.Context context = dmpStatus.getConfigurationData();

		LOG.debug("try to get schema for configuration with id '" + configurationId + "' for resource with id '" + id + "'");

		final Optional<Iterator<Tuple<String, JsonNode>>> data = schemaDataUtil.getData(id, configurationId, Optional.fromNullable(atMost));

		if (!data.isPresent()) {

			LOG.debug("couldn't find data");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		// temp
		final Iterator<Tuple<String, JsonNode>> tupleIterator;
		if (atMost != null) {
			tupleIterator = Iterators.limit(data.get(), atMost);
		} else {
			tupleIterator = data.get();
		}

		final ObjectNode json = objectMapper.createObjectNode();
		while (tupleIterator.hasNext()) {
			final Tuple<String, JsonNode> tuple = data.get().next();
			json.put(tuple.v1(), tuple.v2());
		}

		final String jsonString;

		try {

			jsonString = objectMapper.writeValueAsString(json);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform resource configuration to JSON string.\n" + e.getMessage());
		}

		LOG.debug("return data for configuration with id '" + configurationId + "' for resource with id '" + id + "' and content '" + jsonString
				+ "'");

		dmpStatus.stop(context);
		return buildResponse(jsonString);
	}

	@ApiOperation(value = "get a CSV data preview of the data resource that matches the given id and where the given configuration will be applied to ( = data model)", notes = "Returns a CSV data preview of the data resource that matches the given id and where the given configuration will be applied to ( = data model).")
	@POST
	@Path("/{id}/configurationpreview")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response csvPreviewConfiguration(@ApiParam(value = "data resource identifier", required = true) @PathParam("id") final Long id,
			@ApiParam(value = "configuration (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {
		final Timer.Context context = dmpStatus.configurationsPreview();

		LOG.debug("try to apply configuration for resource with id '" + id + "'");
		LOG.debug("try to recieve resource with id '" + id + "' for csv configuration preview");

		final Optional<Resource> resourceOptional = schemaDataUtil.fetchResource(id);

		if (!resourceOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
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

	@ApiOperation(value = "get a CSV JSON data preview of the data resource that matches the given id and where the given configuration will be applied to ( = data model)", notes = "Returns a CSV JSON data preview of the data resource that matches the given id and where the given configuration will be applied to ( = data model).")
	@POST
	@Path("/{id}/configurationpreview")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response csvJSONPreviewConfiguration(@ApiParam(value = "data resource identifier", required = true) @PathParam("id") final Long id,
			@ApiParam(value = "configuration (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {
		final Timer.Context context = dmpStatus.configurationsPreview();

		LOG.debug("try to apply configuration for resource with id '" + id + "'");
		LOG.debug("try to recieve resource with id '" + id + "' for csv json configuration preview");

		final Optional<Resource> resourceOptional = schemaDataUtil.fetchResource(id);

		if (!resourceOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
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

	private Resource createResource(final InputStream uploadInputedStream, final FormDataContentDisposition fileDetail, final String name,
			final String description) throws DMPControllerException {

		final File file = DMPControllerUtils.writeToFile(uploadInputedStream, fileDetail.getFileName(), "resources");

		final ResourceService resourceService = resourceServiceProvider.get();

		final Resource resource;

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

		Configuration configuration = null;

		if (configurationFromJSON.getId() == null) {

			// create new configuration, since it has no id

			configuration = createNewConfiguration(configurationService);
		} else {

			// try to retrieve configuration via id from "configuration from JSON"

			configuration = configurationService.getObject(configurationFromJSON.getId());

			if(configuration == null) {

				// if the id is not in the DB, also create a new object

				configuration = createNewConfiguration(configurationService);
			}
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

	private Configuration createNewConfiguration(final ConfigurationService configurationService) throws DMPControllerException {

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

		final CSVSourceResourceCSVPreviewFlow flow;

		try {
			flow = CSVResourceFlowFactory.fromConfiguration(configurationFromJSON, CSVSourceResourceCSVPreviewFlow.class);
		} catch (final DMPConverterException e) {

			throw new DMPControllerException(e.getMessage());
		}

		try {
			return flow.applyFile(filePathNode.asText());
		} catch (final DMPConverterException e) {
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

		final CSVSourceResourceCSVJSONPreviewFlow flow;

		try {
			flow = CSVResourceFlowFactory.fromConfiguration(configurationFromJSON, CSVSourceResourceCSVJSONPreviewFlow.class);
		} catch (final DMPConverterException e) {

			throw new DMPControllerException(e.getMessage());
		}

		flow.withLimit(50);

		try {
			return flow.applyFile(filePathNode.asText());
		} catch (final DMPConverterException e) {

			throw new DMPControllerException(e.getMessage());
		}
	}

	private Configuration getConfiguration(final String configurationJSONString) throws DMPControllerException {

		final Configuration configurationFromJSON;

		try {

			configurationFromJSON = objectMapper.readValue(configurationJSONString, Configuration.class);
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
