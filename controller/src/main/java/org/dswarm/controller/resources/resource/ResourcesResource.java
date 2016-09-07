/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.controller.resources.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.AbstractBaseResource;
import org.dswarm.controller.resources.POJOFormat;
import org.dswarm.controller.utils.DMPControllerUtils;
import org.dswarm.controller.utils.DataModelUtil;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.flow.CSVResourceFlowFactory;
import org.dswarm.converter.flow.CSVSourceResourceCSVJSONPreviewFlow;
import org.dswarm.converter.flow.CSVSourceResourceCSVPreviewFlow;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.dto.ShortExtendendBasicDMPDTO;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.model.resource.proxy.ProxyConfiguration;
import org.dswarm.persistence.model.resource.proxy.ProxyResource;
import org.dswarm.persistence.model.resource.utils.ResourceStatics;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.resource.ResourceService;

/**
 * A resource (controller service) for {@link Resource}s.
 *
 * @author tgaengler
 * @author phorn
 */
@RequestScoped
@Api(value = "/resources", description = "Operations about data resources.")
@Path("/resources")
public class ResourcesResource extends AbstractBaseResource {

	private static final Logger LOG = LoggerFactory.getLogger(ResourcesResource.class);

	private static final String RESOURCES_DIRECTORY_POSTFIX = "resources";
	private static final String LINES_IDENTIFIER            = "lines";
	private static final String NAME_IDENTIFIER             = "name";
	private static final String DESCRIPTION_IDENTIFIER      = "description";
	private static final int    DEFAULT_PREVIEW_LIMIT       = 50;
	public static final String RESOURCE_OBJECT_TYPE = "resource";

	@Context
	UriInfo uri;

	private final DMPControllerUtils        controllerUtils;
	private final Provider<ResourceService> resourceServiceProvider;

	private final Provider<ConfigurationService> configurationServiceProvider;

	private final ObjectMapper                     objectMapper;
	private final DataModelUtil                    dataModelUtil;
	private final Provider<CSVResourceFlowFactory> flowFactory2;

	/**
	 * Creates a new resource (controller service) for {@link Resource}s with the provider of the resource persistence service,
	 * the provider of configuration persistence service, the provider of data model persistence service, the object mapper,
	 * metrics registry, event bus provider and data model util.
	 *
	 * @param objectMapperArg                 an object mapper
	 * @param resourceServiceProviderArg      the provider for the resource persistence service
	 * @param configurationServiceProviderArg the provider for the configuration persistence service
	 * @param dataModelUtilArg                the data model util
	 */
	@Inject
	public ResourcesResource(
			final ObjectMapper objectMapperArg,
			final DMPControllerUtils controllerUtilsArg,
			final Provider<ResourceService> resourceServiceProviderArg,
			final Provider<ConfigurationService> configurationServiceProviderArg,
			final DataModelUtil dataModelUtilArg,
			final Provider<CSVResourceFlowFactory> flowFactory2) {

		controllerUtils = controllerUtilsArg;
		resourceServiceProvider = resourceServiceProviderArg;
		configurationServiceProvider = configurationServiceProviderArg;
		objectMapper = objectMapperArg;
		dataModelUtil = dataModelUtilArg;
		this.flowFactory2 = flowFactory2;
	}

	/**
	 * Builds a positive "created" response with the given content at the given response URI.
	 *
	 * @param responseContent a response message
	 * @param responseURI     a URI
	 * @return the response
	 * @throws DMPControllerException
	 */
	private static Response buildResponseCreated(final String responseContent, final URI responseURI, final RetrievalType type,
			final String objectType)
			throws DMPControllerException {

		final ResponseBuilder responseBuilder;

		switch (type) {

			case CREATED:

				responseBuilder = Response.created(responseURI);

				break;
			case RETRIEVED:

				responseBuilder = Response.ok().contentLocation(responseURI);

				break;
			default:

				ResourcesResource.LOG.error("something went wrong, while evaluating the retrieval type of the {}", objectType);

				throw new DMPControllerException(String.format("something went wrong, while evaluating the retrieval type of the %s", objectType));
		}

		return responseBuilder.entity(responseContent).build();
	}

	/**
	 * This endpoint processes (uploades) the input stream and creates a new resource object with related metadata that will be
	 * returned as JSON representation.
	 *
	 * @param fileDetail          file metadata
	 * @param name                the name of the resource
	 * @param description         an description of the resource
	 * @param uuid                a preset resource uuid
	 * @param uploadedInputStream the input stream that should be uploaded
	 * @return a JSON representation of the created resource
	 * @throws DMPControllerException
	 */
	@POST
	@ApiOperation(value = "upload new data resource", notes = "Returns a new Resource object, when upload was successful.", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "data resource was successfully uploaded and stored"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadResource(@ApiParam("file metadata") @FormDataParam("file") final FormDataContentDisposition fileDetail,
			@ApiParam(value = "resource name", required = true) @FormDataParam("name") final String name,
			@ApiParam("resource description") @FormDataParam("description") final String description,
			@ApiParam("resource uuid") @FormDataParam("uuid") final String uuid,
			@ApiParam(value = "file input stream", required = true) @FormDataParam("file") final InputStream uploadedInputStream)
			throws DMPControllerException {

		ResourcesResource.LOG.debug("try to create new resource '{}' for file '{}'", name, fileDetail.getFileName());

		final ProxyResource proxyResource = createResource(fileDetail, name, description, uuid, uploadedInputStream);

		if (proxyResource == null) {

			throw new DMPControllerException("couldn't create new resource");
		}

		final Resource resource = proxyResource.getObject();

		if (resource == null) {

			throw new DMPControllerException("couldn't create new resource");
		}

		ResourcesResource.LOG.debug("created new resource '{}' for file '{}' ", name, fileDetail.getFileName());

		if (ResourcesResource.LOG.isTraceEnabled()) {

			ResourcesResource.LOG.trace("= '{}'", ToStringBuilder.reflectionToString(resource));
		}

		final String resourceJSON = serialiseObject(resource);
		final URI resourceURI = createObjectURI(resource.getUuid());

		ResourcesResource.LOG.debug("created new resource at '{}' with content ", resourceURI);

		if (ResourcesResource.LOG.isTraceEnabled()) {

			ResourcesResource.LOG.trace("'{}'", resourceJSON);
		}

		return buildResponseCreated(resourceJSON, resourceURI, proxyResource.getType(), RESOURCE_OBJECT_TYPE);
	}

	/**
	 * This endpoint returns a list of all resources as JSON representation.
	 *
	 * @return a list of all resources as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all data resources", notes = "Returns a list of Resource objects.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all resources (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any resource, i.e., there are no resources available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResources(
			@ApiParam(value = "'short' for only uuid,name,description, 'full' for the complete entity")
			@QueryParam("format") @DefaultValue("full") final POJOFormat format) throws DMPControllerException {
		ResourcesResource.LOG.debug("try to get all resources");

		final ResourceService resourceService = resourceServiceProvider.get();

		final List<Resource> resources = resourceService.getObjects();

		if (resources == null) {

			ResourcesResource.LOG.debug("couldn't find resources");
			return Response.status(Status.NOT_FOUND).build();
		}

		if (resources.isEmpty()) {

			ResourcesResource.LOG.debug("there are no resources");
			return Response.status(Status.NOT_FOUND).build();
		}

		ResourcesResource.LOG.debug("got all resources ");

		if (ResourcesResource.LOG.isTraceEnabled()) {

			ResourcesResource.LOG.trace("= '{}'", ToStringBuilder.reflectionToString(resources));
		}

		final String resourcesJSON;

		switch (format) {
			case SHORT:
				resourcesJSON = serializeShortObjects(resources);
				break;
			default:
				resourcesJSON = serializeFullObjects(resources);
				break;
		}

		ResourcesResource.LOG.debug("return all resources ");

		if (ResourcesResource.LOG.isTraceEnabled()) {

			ResourcesResource.LOG.trace("'{}'", resourcesJSON);
		}

		return buildResponse(resourcesJSON);
	}

	private String serializeShortObjects(final List<Resource> resources) throws DMPControllerException {
		final List<ShortExtendendBasicDMPDTO> shortResources = resources.stream()
				.map(r -> ShortExtendendBasicDMPDTO.of(r, createObjectURI(r.getUuid())))
				.collect(Collectors.toList());

		return serialiseObject(shortResources);
	}

	private String serializeFullObjects(final List<Resource> resources) throws DMPControllerException {
		return serialiseObject(resources);
	}

	/**
	 * This endpoint returns a resource as JSON representation for the provided resource identifier.
	 *
	 * @param uuid a resource identifier
	 * @return a JSON representation of a resource
	 */
	@ApiOperation(value = "get the data resource that matches the given uuid", notes = "Returns the Resource object that matches the given uuid.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the resource (as JSON) that matches the given uuid"),
			@ApiResponse(code = 404, message = "could not find a resource for the given uuid"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@GET
	@Path("/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResource(
			@ApiParam(value = "data resource identifier", required = true) @PathParam("uuid") final String uuid,
			@ApiParam(value = "'short' for only uuid,name,description, 'full' for the complete entity")
			@QueryParam("format") @DefaultValue("full") final POJOFormat format)
			throws DMPControllerException {
		final Optional<Resource> resourceOptional = dataModelUtil.fetchResource(uuid);

		if (!resourceOptional.isPresent()) {
			return Response.status(Status.NOT_FOUND).build();
		}

		final Resource resource = resourceOptional.get();

		final String resourceJSON;
		switch (format) {
			case SHORT:
				resourceJSON = serializeShortResource(resource);
				break;
			default:
				resourceJSON = serializeFullResource(resource);
				break;
		}

		ResourcesResource.LOG.debug("return resource with uuid '{}' and content ", uuid);

		if (ResourcesResource.LOG.isTraceEnabled()) {

			ResourcesResource.LOG.trace("'{}'", resourceJSON);
		}

		return buildResponse(resourceJSON);
	}

	private String serializeShortResource(final Resource resource) throws DMPControllerException {
		final ShortExtendendBasicDMPDTO shortVersion =
				ShortExtendendBasicDMPDTO.of(resource, createObjectURI(resource.getUuid()));

		return serialiseObject(shortVersion);
	}

	private String serializeFullResource(final Resource resource) throws DMPControllerException {

		return serialiseObject(resource);
	}

	private String serialiseObject(final Object object) throws DMPControllerException {

		try {

			return objectMapper.writeValueAsString(object);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform resource object to JSON string.", e);
		}
	}

	/**
	 * This endpoint processes (uploades) the input stream and update an existing resource object with related metadata that will
	 * be returned as JSON representation.
	 *
	 * @param uuid
	 * @param fileDetail          file metadata
	 * @param name                the name of the resource
	 * @param description         an description of the resource
	 * @param uploadedInputStream the input stream that should be uploaded
	 * @return a JSON representation of the created resource
	 * @throws DMPControllerException
	 */
	@PUT
	@Path("/{uuid}")
	@ApiOperation(value = "update data resource", notes = "Returns a Resource object, when update was successfully.", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "data resource was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a resource for the given uuid"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateResource(@ApiParam(value = "resource identifier", required = true) @PathParam("uuid") final String uuid,
			@ApiParam("file metadata") @FormDataParam("file") final FormDataContentDisposition fileDetail,
			@ApiParam(value = "resource name", required = true) @FormDataParam("name") final String name,
			@ApiParam("resource description") @FormDataParam("description") final String description,
			@ApiParam(value = "file input stream", required = true) @FormDataParam("file") final InputStream uploadedInputStream)
			throws DMPControllerException {

		final Optional<Resource> resourceOptional = dataModelUtil.fetchResource(uuid);

		if (!resourceOptional.isPresent()) {

			return Response.status(Status.NOT_FOUND).build();
		}

		final Resource resource = resourceOptional.get();

		ResourcesResource.LOG.debug("try to update resource '{}' for file '{}'", name, fileDetail.getFileName());

		final ProxyResource proxyResource = refreshResource(resource, fileDetail, name, description, uploadedInputStream);

		final Resource updateResource = proxyResource.getObject();

		final String resourceJSON;

		try {

			resourceJSON = objectMapper.writeValueAsString(updateResource);
		} catch (final JsonProcessingException e) {
			throw new DMPControllerException("couldn't transform resource object to JSON string");
		}

		ResourcesResource.LOG.debug("updated resource with uuid '{}' ", uuid);

		if (ResourcesResource.LOG.isTraceEnabled()) {

			ResourcesResource.LOG.trace("and JSON content '{}'", resourceJSON);
		}

		return buildResponse(resourceJSON);
	}

	/**
	 * This endpoint deletes a resource that matches the given uuid.
	 *
	 * @param uuid a resource identifier
	 * @return status 204 if removal was successful, 404 if uuid not found, 409 if it couldn't be removed, or 500 if something else
	 * went wrong
	 */
	@ApiOperation(value = "delete the data resource that matches the given uuid", notes = "Returns status 204 if removal was successful, 404 if uuid not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "resource was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a resource for the given uuid"),
			@ApiResponse(code = 409, message = "resource couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@DELETE
	@Path("/{uuid}")
	public Response deleteResource(@ApiParam(value = "data resource identifier", required = true) @PathParam("uuid") final String uuid)
			throws DMPControllerException {

		ResourcesResource.LOG.debug("try to delete resource with uuid '{}'", uuid);

		Optional<Resource> resourceOptional = dataModelUtil.fetchResource(uuid);

		if (!resourceOptional.isPresent()) {

			ResourcesResource.LOG.error("couldn't find resource '{}'", uuid);

			return Response.status(Status.NOT_FOUND).build();
		}

		dataModelUtil.deleteResource(uuid);

		resourceOptional = dataModelUtil.fetchResource(uuid);

		if (resourceOptional.isPresent()) {

			ResourcesResource.LOG.error("couldn't delete resource '{}'", uuid);

			return Response.status(Status.CONFLICT).build();
		}

		ResourcesResource.LOG.debug("deletion of resource with uuid '{}' was successful", uuid);

		return Response.status(Status.NO_CONTENT).build();
	}

	/**
	 * Returns the content of the uploaded resource line-wise.
	 *
	 * @param uuid     a resource identifier
	 * @param atMost   the number of lines that should be returned at most
	 * @param encoding the encoding of the uploaded resource
	 * @return a JSON representation of the content
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get the lines of the data resource that matches the given uuid", notes = "Returns the lines of the data resource that matches the given uuid. The number of lines can be limited via the 'atMost' parameter. The encoding can be set via the 'encoding' parameter.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "raw data of data resource could be retrieved"),
			@ApiResponse(code = 404, message = "could not find a resource for the given uuid"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@GET
	@Path("/{uuid}/lines")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourcePlain(@ApiParam(value = "data resource identifier", required = true) @PathParam("uuid") final String uuid,
			@ApiParam(value = "number of lines limit", defaultValue = "50") @DefaultValue("50") @QueryParam("atMost") final int atMost,
			@ApiParam(value = "data resource encoding", defaultValue = "UTF-8") @DefaultValue("UTF-8") @QueryParam("encoding") final String encoding)
			throws DMPControllerException {
		final Optional<Resource> resourceOptional = dataModelUtil.fetchResource(uuid);

		if (!resourceOptional.isPresent()) {

			return Response.status(Status.NOT_FOUND).build();
		}

		final Resource resource = resourceOptional.get();

		final JsonNode path;

		final JsonNode originalPath = resource.getAttributes().get(ResourceStatics.ORIGINAL_PATH);

		if(originalPath != null) {

			// take original path

			path = originalPath;
		} else {

			path= resource.getAttributes().get(ResourceStatics.PATH);
		}

		if (path == null) {

			return Response.status(Status.NOT_FOUND).build();
		}

		final String filePath = path.asText();

		final List<String> lines;

		try {

			lines = Files.readLines(new File(filePath), Charset.forName(encoding), new LineProcessor<List<String>>() {

				private final ImmutableList.Builder<String> lines = ImmutableList.builder();
				private int linesProcessed = 1;

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
			throw new DMPControllerException("couldn't read file contents", e);
		}

		final Map<String, Object> jsonMap = new HashMap<>(3);
		jsonMap.put(LINES_IDENTIFIER, lines);
		jsonMap.put(NAME_IDENTIFIER, resource.getName());
		jsonMap.put(DESCRIPTION_IDENTIFIER, resource.getDescription());

		try {

			final String plainJson = objectMapper.writeValueAsString(jsonMap);

			return buildResponse(plainJson);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException(String.format("couldn't transform resource contents to JSON array.\n%s", e.getMessage()));
		}
	}

	/**
	 * This endpoint delivers all configurations that are related to this resource.
	 *
	 * @param uuid a resource identifier
	 * @return a JSON representation of a list of configurations
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all configurations of the data resource that matches the given uuid", notes = "Returns the configurations of the data resource that matches the given uuid.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all configurations (as JSON) of the resource that matches the given uuid"),
			@ApiResponse(code = 404, message = "could not find a resource for the given uuid"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@GET
	@Path("/{uuid}/configurations")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfigurations(@ApiParam(value = "data resource identifier", required = true) @PathParam("uuid") final String uuid)
			throws DMPControllerException {
		ResourcesResource.LOG.debug("try to get resource configurations for resource with uuid '{}'", uuid);

		final Optional<Resource> resourceOptional = dataModelUtil.fetchResource(uuid);

		if (!resourceOptional.isPresent()) {
			return Response.status(Status.NOT_FOUND).build();
		}

		final Resource resource = resourceOptional.get();

		ResourcesResource.LOG.debug("got resource with uuid '{}' for resource configurations retrieval ", uuid);

		if (ResourcesResource.LOG.isTraceEnabled()) {

			ResourcesResource.LOG.trace("= '{}'", ToStringBuilder.reflectionToString(resource));
		}

		final Set<Configuration> configurations = resource.getConfigurations();

		if (configurations == null || configurations.isEmpty()) {

			ResourcesResource.LOG.debug("couldn't find configurations for resource '{}'; or there are no configurations for this resource", uuid);
			return Response.status(Status.NOT_FOUND).build();
		}

		ResourcesResource.LOG.debug("got resource configurations for resource with uuid '{}' ", uuid);

		if (ResourcesResource.LOG.isTraceEnabled()) {

			ResourcesResource.LOG.trace("= '{}'", ToStringBuilder.reflectionToString(configurations));
		}

		final String configurationsJSON;

		try {

			configurationsJSON = objectMapper.writeValueAsString(resource.getConfigurations());
		} catch (final JsonProcessingException e) {
			throw new DMPControllerException(String.format("couldn't transform resource configurations set to JSON string.\n%s", e.getMessage()));
		}

		ResourcesResource.LOG.debug("return resource configurations for resource with uuid '{}' ", uuid);

		if (ResourcesResource.LOG.isTraceEnabled()) {

			ResourcesResource.LOG.trace("and content '{}'", configurationsJSON);
		}

		return buildResponse(configurationsJSON);
	}

	/**
	 * This endpoint adds a configuration to the given resource.<br/>
	 * The data processing, which was a step of this operation is deprecated, i.e., only the given configuration will be added to
	 * this resource. For data processing of a given resource with a given configuration, please utilise
	 * DataModelsResource#createObject instead.<br/>
	 * <br/>
	 * note: [@tgaengler] the processing of a given data resource with a given configuration has been moved to
	 * {@link DataModelsResource}, i.e., the result of this operation should only be the addition of the given configuration,
	 * however, not the processing of this combination.
	 *
	 * @param uuid             a resource identifier
	 * @param jsonObjectString a JSON representation of a configuration.
	 * @return a JSON representation of the added configuration
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "add a new configuration to the data resource that matches the given uuid", notes = "Returns the new configuration that was added to the data resource that matches the given uuid. Note: The data processing, which was a step of this operation is deprecated, i.e., only the given configuration will be added to this resource. For data processing of a given resource with a given configuration, please utilise POST [base uri]/datamodels instead")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "configuration was successfully persisted and added to the resource for the given uuid"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@POST
	@Path("/{uuid}/configurations")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addConfiguration(@ApiParam(value = "data resource identifier", required = true) @PathParam("uuid") final String uuid,
			@ApiParam(value = "configuration (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {
		ResourcesResource.LOG.debug("try to create new configuration for resource with uuid '{}'", uuid);

		final Optional<Resource> resourceOptional = dataModelUtil.fetchResource(uuid);

		if (!resourceOptional.isPresent()) {
			return Response.status(Status.NOT_FOUND).build();
		}

		ResourcesResource.LOG.debug("try to add new configuration to resource with uuid '{}'", uuid);

		final Resource resource = resourceOptional.get();

		final ProxyConfiguration proxyConfiguration = addConfiguration(resource, jsonObjectString);

		if (proxyConfiguration == null) {

			ResourcesResource.LOG.debug("couldn't add configuration to resource with uuid '{}'", uuid);
			throw new DMPControllerException(String.format("couldn't add configuration to resource with uuid '%s'", uuid));
		}

		final Configuration configuration = proxyConfiguration.getObject();

		if (configuration == null) {

			ResourcesResource.LOG.debug("couldn't add configuration to resource with uuid '{}'", uuid);
			throw new DMPControllerException(String.format("couldn't add configuration to resource with uuid '%s'", uuid));
		}

		ResourcesResource.LOG.debug("added new configuration to resource with uuid '{}' ", uuid);

		if (ResourcesResource.LOG.isTraceEnabled()) {

			ResourcesResource.LOG.trace("= '{}'", ToStringBuilder.reflectionToString(configuration));
		}

		final String configurationJSON;

		try {

			configurationJSON = objectMapper.writeValueAsString(configuration);
		} catch (final JsonProcessingException e) {
			throw new DMPControllerException(String.format("couldn't transform resource configuration to JSON string.\n%s", e.getMessage()));
		}

		final URI baseURI = uri.getRequestUri();
		final URI configurationURI = URI.create(baseURI + "/" + configuration.getUuid());

		ResourcesResource.LOG.debug("return new configuration at '{}' ", configurationURI);

		if (ResourcesResource.LOG.isTraceEnabled()) {

			ResourcesResource.LOG.trace("with content '{}'", configurationJSON);
		}

		return buildResponseCreated(configurationJSON, configurationURI, proxyConfiguration.getType(), "configuration");
	}

	/**
	 * This endpoint delivers a configuration for the given resource identifier and configuration identifier.
	 *
	 * @param uuid              a resource identifier
	 * @param configurationUuid a configuration identifier
	 * @return a JSON representation of the matched configuration
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get the configuration of the data resource that matches the given data resource uuid and the given configuration uuid", notes = "Returns the configuration of the data resource that matches the given data resource uuid and the given configuration uuid.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "returns a configuration (as JSON) that matches the given configuration uuid and is related to the resource that matches the given resource uuid"),
			@ApiResponse(code = 404, message = "could not find a configuration for the given configuration uuid and/or resource uuid"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@GET
	@Path("/{uuid}/configurations/{configurationuuid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfiguration(@ApiParam(value = "data resource identifier", required = true) @PathParam("uuid") final String uuid,
			@ApiParam(value = "configuration identifier", required = true) @PathParam("configurationuuid") final String configurationUuid)
			throws DMPControllerException {
		final Optional<Configuration> configurationOptional = dataModelUtil.fetchConfiguration(uuid, configurationUuid);

		if (!configurationOptional.isPresent()) {
			return Response.status(Status.NOT_FOUND).build();
		}

		final String configurationJSON;

		try {

			configurationJSON = objectMapper.writeValueAsString(configurationOptional.get());
		} catch (final JsonProcessingException e) {
			throw new DMPControllerException("couldn't transform resource configuration to JSON string", e);
		}

		ResourcesResource.LOG.debug("return configuration with uuid '{}' for resource with uuid '{}' ", configurationUuid, uuid);

		if (ResourcesResource.LOG.isTraceEnabled()) {

			ResourcesResource.LOG.trace("and content '{}'", configurationJSON);
		}

		return buildResponse(configurationJSON);
	}

	/**
	 * note: [@tgaengler] this operation should be moved to {@link DataModelsResource} and there should be a generic preview
	 * operation
	 *
	 * @param uuid
	 * @param jsonObjectString
	 * @return
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get a CSV data preview of the data resource that matches the given uuid and where the given configuration will be applied to ( = data model)", notes = "Returns a CSV data preview of the data resource that matches the given uuid and where the given configuration will be applied to ( = data model).")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "a preview of the CSV data of the data resource, where the given configuration was applied, could be retrieved"),
			@ApiResponse(code = 404, message = "could not find a resource for the given uuid or data for this resource"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@POST
	@Path("/{uuid}/configurationpreview")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response csvPreviewConfiguration(@ApiParam(value = "data resource identifier", required = true) @PathParam("uuid") final String uuid,
			@ApiParam(value = "configuration (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {
		ResourcesResource.LOG.debug("try to apply configuration for resource with uuid '{}'", uuid);
		ResourcesResource.LOG.debug("try to receive resource with uuid '{}' for csv configuration preview", uuid);

		final Optional<Resource> resourceOptional = dataModelUtil.fetchResource(uuid);

		if (!resourceOptional.isPresent()) {
			return Response.status(Status.NOT_FOUND).build();
		}

		final Resource resource = resourceOptional.get();

		ResourcesResource.LOG.debug("found resource with uuid '{}' for csv configuration preview ", uuid);

		if (ResourcesResource.LOG.isTraceEnabled()) {

			ResourcesResource.LOG.trace("= '{}'", ToStringBuilder.reflectionToString(resource));
		}

		ResourcesResource.LOG.debug("try to apply configuration to resource with uuid '{}'", uuid);

		final String result = applyConfigurationForCSVPreview(resource, jsonObjectString);

		if (result == null) {

			ResourcesResource.LOG.error("couldn't apply configuration to resource with uuid '{}'", uuid);

			throw new DMPControllerException(String.format("couldn't apply configuration to resource with uuid '%s'", uuid));
		}

		ResourcesResource.LOG.debug("applied configuration to resource with uuid '{}'", uuid);

		return buildResponse(result);
	}

	/**
	 * note: [@tgaengler] this operation should be moved to {@link DataModelsResource} and there should be a generic preview
	 * operation
	 *
	 * @param uuid
	 * @param jsonObjectString
	 * @return
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get a CSV JSON data preview of the data resource that matches the given uuid and where the given configuration will be applied to ( = data model)", notes = "Returns a CSV JSON data preview of the data resource that matches the given uuid and where the given configuration will be applied to ( = data model).")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "a preview of the CSV JSON data of the data resource, where the given configuration was applied, could be retrieved"),
			@ApiResponse(code = 404, message = "could not find a resource for the given uuid or data for this resource"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@POST
	@Path("/{uuid}/configurationpreview")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response csvJSONPreviewConfiguration(@ApiParam(value = "data resource identifier", required = true) @PathParam("uuid") final String uuid,
			@ApiParam(value = "configuration (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {
		ResourcesResource.LOG.debug("try to apply configuration for resource with uuid '{}'", uuid);
		ResourcesResource.LOG.debug("try to recieve resource with uuid '{}' for csv json configuration preview", uuid);

		final Optional<Resource> resourceOptional = dataModelUtil.fetchResource(uuid);

		if (!resourceOptional.isPresent()) {
			return Response.status(Status.NOT_FOUND).build();
		}

		final Resource resource = resourceOptional.get();

		ResourcesResource.LOG.debug("found resource with uuid '{}' for csv json configuration preview ", uuid);

		if (ResourcesResource.LOG.isTraceEnabled()) {

			ResourcesResource.LOG.trace("= '{}'", ToStringBuilder.reflectionToString(resource));
		}

		ResourcesResource.LOG.debug("try to apply configuration to resource with uuid '{}'", uuid);

		final String result = applyConfigurationForCSVJSONPreview(resource, jsonObjectString);

		if (result == null) {

			ResourcesResource.LOG.error("couldn't apply configuration to resource with uuid '{}'", uuid);

			throw new DMPControllerException(String.format("couldn't apply configuration to resource with uuid '%s'", uuid));
		}

		ResourcesResource.LOG.debug("applied configuration to resource with uuid '{}'", uuid);

		return buildResponse(result);
	}

	/**
	 * Process stores the input stream and creates and persists a new resource with the related metadata.
	 *
	 * TODO: reduce transactions (one instead of two)
	 *
	 * @param fileDetail          metadata of the given input stream
	 * @param name                the name of the resource
	 * @param description         an description of the resource
	 * @param uploadInputedStream an input stream that should be uploaded
	 * @return a JSON representation of the new resource
	 * @throws DMPControllerException
	 */
	private ProxyResource createResource(final FormDataContentDisposition fileDetail, final String name,
			final String description, final String uuid, final InputStream uploadInputedStream) throws DMPControllerException {

		final ResourceService resourceService = resourceServiceProvider.get();

		final Resource newResource = new Resource(uuid);

		ProxyResource proxyResource;

		try {

			proxyResource = resourceService.createObjectTransactional(newResource);
		} catch (final DMPPersistenceException e) {

			ResourcesResource.LOG.debug("something went wrong while resource creation");

			throw new DMPControllerException(String.format("something went wrong while resource creation\n%s", e.getMessage()));
		}

		if (proxyResource == null) {

			throw new DMPControllerException("fresh resource shouldn't be null");
		}

		final Resource resource = proxyResource.getObject();

		if (resource == null) {

			throw new DMPControllerException("fresh resource shouldn't be null");
		}

		final ProxyResource refreshedResource = refreshResource(resource, fileDetail, name, description, uploadInputedStream);

		// re-wrap with correct type (created)

		return new ProxyResource(refreshedResource.getObject(), proxyResource.getType());
	}

	/**
	 * Process stores the input stream and update a resource with the related metadata.
	 *
	 * @param fileDetail          metadata of the given input stream
	 * @param name                the name of the resource
	 * @param description         an description of the resource
	 * @param uploadInputedStream an input stream that should be uploaded
	 * @return a JSON representation of the updated resource
	 * @throws DMPControllerException
	 */
	private ProxyResource refreshResource(final Resource resource, final FormDataContentDisposition fileDetail, final String name,
			final String description, final InputStream uploadInputedStream) throws DMPControllerException {

		final File file = controllerUtils.writeToFile(uploadInputedStream, fileDetail.getFileName(), RESOURCES_DIRECTORY_POSTFIX);

		final ResourceService resourceService = resourceServiceProvider.get();

		final ProxyResource proxyResource;

		resource.setName(name);

		if (description != null) {

			resource.setDescription(description);
		}

		resource.setType(ResourceType.FILE);

		// update attributes
		ObjectNode attributes = resource.getAttributes();

		if (attributes == null) {

			attributes = new ObjectNode(objectMapper.getNodeFactory());
		}

		final String fileAbsolutePath = file.getAbsolutePath();

		attributes.put(ResourceStatics.PATH, fileAbsolutePath);
		attributes.put(ResourceStatics.FILE_SIZE, fileDetail.getSize());

		try {

			final String fileType = java.nio.file.Files.probeContentType(file.toPath());

			if (fileType != null) {

				attributes.put(ResourceStatics.FILE_TYPE, fileType);
			}
		} catch (final IOException e1) {

			ResourcesResource.LOG.debug("couldn't determine file type from file '{}'", fileAbsolutePath);
		}

		resource.setAttributes(attributes);

		// update resource
		final ProxyResource updatedResource;

		try {

			updatedResource = resourceService.updateObjectTransactional(resource);

			if (updatedResource == null) {

				throw new DMPControllerException("something went wrong while resource updating");
			}

			proxyResource = new ProxyResource(updatedResource.getObject(), updatedResource.getType());
		} catch (final DMPPersistenceException e) {

			ResourcesResource.LOG.error("something went wrong while resource updating");

			throw new DMPControllerException(String.format("something went wrong while resource updating\n%s", e.getMessage()));
		}

		return proxyResource;
	}

	/**
	 * Adds and persists a configuration to the given resource.
	 *
	 * @param resource                a resource
	 * @param configurationJSONString a JSON representation of a new configuration
	 * @return the new configuration
	 * @throws DMPControllerException
	 */
	private ProxyConfiguration addConfiguration(final Resource resource, final String configurationJSONString) throws DMPControllerException {

		final Configuration configurationFromJSON = getConfiguration(configurationJSONString);

		final ConfigurationService configurationService = configurationServiceProvider.get();

		ProxyConfiguration proxyConfiguration;

		if (configurationFromJSON.getUuid() == null) {

			// create new configuration, since it has no id

			proxyConfiguration = createNewConfiguration(configurationService);
		} else {

			// try to retrieve configuration via id from "configuration from JSON"

			final Configuration retrievedConfiguration = configurationService.getObject(configurationFromJSON.getUuid());

			if (retrievedConfiguration == null) {

				// if the id is not in the DB, also create a new object

				proxyConfiguration = createNewConfiguration(configurationService);
			} else {

				RetrievalType type = null;

				final Set<Configuration> configurations = resource.getConfigurations();

				if (configurations != null) {

					for (final Configuration configuration : configurations) {

						if (configuration.getUuid().equals(retrievedConfiguration.getUuid())) {

							type = RetrievalType.RETRIEVED;

							break;
						}
					}
				}

				if (type == null) {

					type = RetrievalType.CREATED;
				}

				proxyConfiguration = new ProxyConfiguration(retrievedConfiguration, type);
			}
		}

		if (proxyConfiguration == null) {

			throw new DMPControllerException("couldn't create or retrieve configuration");
		}

		final Configuration configuration = proxyConfiguration.getObject();

		if (configuration == null) {

			throw new DMPControllerException("couldn't create or retrieve configuration");
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

			final ProxyConfiguration proxyUpdatedConfiguration = configurationService.updateObjectTransactional(configuration);

			if (proxyUpdatedConfiguration == null) {

				throw new DMPControllerException("something went wrong while configuration updating");
			}

			final RetrievalType type = proxyConfiguration.getType();

			proxyConfiguration = new ProxyConfiguration(proxyUpdatedConfiguration.getObject(), type);
		} catch (final DMPPersistenceException e) {

			ResourcesResource.LOG.error("something went wrong while configuration updating");

			throw new DMPControllerException(String.format("something went wrong while configuration updating\n%s", e.getMessage()));
		}

		final ResourceService resourceService = resourceServiceProvider.get();

		try {

			resourceService.updateObjectTransactional(resource);
		} catch (final DMPPersistenceException e) {

			ResourcesResource.LOG.error("something went wrong while resource updating for configuration");

			throw new DMPControllerException(String.format("something went wrong while resource updating for configuration\n%s", e.getMessage()));
		}

		return proxyConfiguration;
	}

	/**
	 * Persists a new configuration in the database.
	 *
	 * @param configurationService the configuration persistence service
	 * @return the new persisted configuration
	 * @throws DMPControllerException
	 */
	private ProxyConfiguration createNewConfiguration(final ConfigurationService configurationService) throws DMPControllerException {

		final ProxyConfiguration proxyConfiguration;

		try {

			proxyConfiguration = configurationService.createObjectTransactional();
		} catch (final DMPPersistenceException e) {

			ResourcesResource.LOG.error("something went wrong while configuration creation");

			throw new DMPControllerException(String.format("something went wrong while configuration creation\n%s", e.getMessage()));
		}

		if (proxyConfiguration == null) {

			throw new DMPControllerException("fresh configuration shouldn't be null");
		}

		final Configuration configuration = proxyConfiguration.getObject();

		if (configuration == null) {

			throw new DMPControllerException("fresh configuration shouldn't be null");
		}

		return proxyConfiguration;
	}

	private String applyConfigurationForCSVPreview(final Resource resource, final String configurationJSONString) throws DMPControllerException {

		final Configuration configurationFromJSON = getConfiguration(configurationJSONString);

		if (resource.getAttributes() == null) {

			throw new DMPControllerException(String.format("there are no attributes available at resource '%s'", resource.getUuid()));
		}

		final JsonNode filePathNode = resource.getAttribute(ResourceStatics.PATH);

		if (filePathNode == null) {

			throw new DMPControllerException("couldn't determine file path");
		}

		final CSVSourceResourceCSVPreviewFlow flow = flowFactory2.get().csvPreview(configurationFromJSON);

		try {
			return flow.applyFile(filePathNode.asText());
		} catch (final DMPConverterException e) {
			throw new DMPControllerException(e.getMessage());
		}
	}

	private String applyConfigurationForCSVJSONPreview(final Resource resource, final String configurationJSONString) throws DMPControllerException {

		final Configuration configurationFromJSON = getConfiguration(configurationJSONString);

		if (resource.getAttributes() == null) {

			throw new DMPControllerException(String.format("there are no attributes available at resource '%s'", resource.getUuid()));
		}

		final JsonNode filePathNode = resource.getAttribute(ResourceStatics.PATH);

		if (filePathNode == null) {

			throw new DMPControllerException("couldn't determine file path");
		}

		final CSVSourceResourceCSVJSONPreviewFlow flow = flowFactory2.get()
				.jsonPreview(configurationFromJSON)
				.withLimit(DEFAULT_PREVIEW_LIMIT);

		try {
			return flow.applyFile(filePathNode.asText());
		} catch (final DMPConverterException e) {

			throw new DMPControllerException(e.getMessage());
		}
	}

	/**
	 * Deserializes the given string that holds a JSON object of a configuration.
	 *
	 * @param configurationJSONString a string that holds a JSON object of a configuration
	 * @return the deserialized configuration
	 * @throws DMPControllerException
	 */
	private Configuration getConfiguration(final String configurationJSONString) throws DMPControllerException {

		final Configuration configurationFromJSON;

		try {

			configurationFromJSON = objectMapper.readValue(configurationJSONString, Configuration.class);
		} catch (final IOException e) {

			ResourcesResource.LOG.error("something went wrong while deserializing the configuration JSON string");

			throw new DMPControllerException(
					String.format("something went wrong while deserializing the configuration JSON string.\n%s", e.getMessage()));
		}

		if (configurationFromJSON == null) {

			throw new DMPControllerException("deserialized configuration is null");
		}

		return configurationFromJSON;
	}

}
