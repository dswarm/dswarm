/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
import java.util.Set;

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

import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.io.LineProcessor;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.tika.Tika;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.eventbus.XMLSchemaEventRecorder;
import org.dswarm.controller.status.DMPStatus;
import org.dswarm.controller.utils.DMPControllerUtils;
import org.dswarm.controller.utils.DataModelUtil;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.flow.CSVResourceFlowFactory;
import org.dswarm.converter.flow.CSVSourceResourceCSVJSONPreviewFlow;
import org.dswarm.converter.flow.CSVSourceResourceCSVPreviewFlow;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.model.resource.proxy.ProxyConfiguration;
import org.dswarm.persistence.model.resource.proxy.ProxyResource;
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
public class ResourcesResource {

	private static final Logger						LOG	= LoggerFactory.getLogger(ResourcesResource.class);

	@Context
	UriInfo											uri;

	private final DMPControllerUtils controllerUtils;
	private final Provider<ResourceService>			resourceServiceProvider;

	private final Provider<ConfigurationService>	configurationServiceProvider;

	private final DMPStatus							dmpStatus;

	private final ObjectMapper						objectMapper;
	private final DataModelUtil						dataModelUtil;

	/**
	 * Creates a new resource (controller service) for {@link Resource}s with the provider of the resource persistence service,
	 * the provider of configuration persistence service, the provider of data model persistence service, the object mapper,
	 * metrics registry, event bus provider and data model util.
	 * 
	 * @param dmpStatusArg a metrics registry
	 * @param objectMapperArg an object mapper
	 * @param resourceServiceProviderArg the provider for the resource persistence service
	 * @param configurationServiceProviderArg the provider for the configuration persistence service
	 * @param xmlSchemaEventRecorderProviderArg an xml schema event recorder provider
	 * @param dataModelUtilArg the data model util
	 */
	@Inject
	public ResourcesResource(final DMPStatus dmpStatusArg, final ObjectMapper objectMapperArg, final DMPControllerUtils controllerUtilsArg,
			final Provider<ResourceService> resourceServiceProviderArg, final Provider<ConfigurationService> configurationServiceProviderArg,
			final Provider<XMLSchemaEventRecorder> xmlSchemaEventRecorderProviderArg, final DataModelUtil dataModelUtilArg) {

		controllerUtils = controllerUtilsArg;
		resourceServiceProvider = resourceServiceProviderArg;
		configurationServiceProvider = configurationServiceProviderArg;
		dmpStatus = dmpStatusArg;
		objectMapper = objectMapperArg;
		dataModelUtil = dataModelUtilArg;
	}

	/**
	 * Builds a positive response with the given content.
	 * 
	 * @param responseContent a response message
	 * @return the response
	 */
	private Response buildResponse(final String responseContent) {

		return Response.ok(responseContent).build();
	}

	/**
	 * Builds a positive "created" response with the given content at the given response URI.
	 * 
	 * @param responseContent a response message
	 * @param responseURI a URI
	 * @return the response
	 * @throws DMPControllerException
	 */
	private Response buildResponseCreated(final String responseContent, final URI responseURI, final RetrievalType type, final String objectType)
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

				ResourcesResource.LOG.debug("something went wrong, while evaluating the retrieval type of the " + objectType);

				throw new DMPControllerException("something went wrong, while evaluating the retrieval type of the " + objectType);
		}

		return responseBuilder.entity(responseContent).build();
	}

	/**
	 * This endpoint processes (uploades) the input stream and creates a new resource object with related metadata that will be
	 * returned as JSON representation.
	 * 
	 * @param uploadedInputStream the input stream that should be uploaded
	 * @param fileDetail file metadata
	 * @param name the name of the resource
	 * @param description an description of the resource
	 * @return a JSON representation of the created resource
	 * @throws DMPControllerException
	 */
	@POST
	@ApiOperation(value = "upload new data resource", notes = "Returns a new Resource object, when upload was successfull.", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "data resource was successfully uploaded and stored"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadResource(
			@ApiParam(value = "file input stream", required = true) @FormDataParam("file") final InputStream uploadedInputStream,
			@ApiParam("file metadata") @FormDataParam("file") final FormDataContentDisposition fileDetail,
			@ApiParam(value = "resource name", required = true) @FormDataParam("name") final String name,
			@ApiParam("resource description") @FormDataParam("description") final String description) throws DMPControllerException {
		final Timer.Context context = dmpStatus.createNewResource();

		ResourcesResource.LOG.debug("try to create new resource '" + name + "' for file '" + fileDetail.getFileName() + "'");

		final ProxyResource proxyResource = createResource(uploadedInputStream, fileDetail, name, description);

		if (proxyResource == null) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't create new resource");
		}

		final Resource resource = proxyResource.getObject();

		if (resource == null) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't create new resource");
		}

		ResourcesResource.LOG.debug("created new resource '" + name + "' for file '" + fileDetail.getFileName() + "' ");
		ResourcesResource.LOG.trace("= '" + ToStringBuilder.reflectionToString(resource) + "'");

		final String resourceJSON;

		try {

			resourceJSON = objectMapper.writeValueAsString(resource);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform resource object to JSON string");
		}

		final URI baseURI = uri.getRequestUri();
		final URI resourceURI = URI.create(baseURI.toString() + "/" + resource.getId());

		ResourcesResource.LOG.debug("created new resource at '" + resourceURI.toString() + "' with content ");
		ResourcesResource.LOG.trace("'" + resourceJSON + "'");

		dmpStatus.stop(context);
		return buildResponseCreated(resourceJSON, resourceURI, proxyResource.getType(), "resource");
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
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResources() throws DMPControllerException {
		final Timer.Context context = dmpStatus.getAllResources();

		ResourcesResource.LOG.debug("try to get all resources");

		final ResourceService resourceService = resourceServiceProvider.get();

		final List<Resource> resources = resourceService.getObjects();

		if (resources == null) {

			ResourcesResource.LOG.debug("couldn't find resources");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		if (resources.isEmpty()) {

			ResourcesResource.LOG.debug("there are no resources");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		ResourcesResource.LOG.debug("got all resources ");
		ResourcesResource.LOG.trace("= '" + ToStringBuilder.reflectionToString(resources) + "'");

		final String resourcesJSON;

		try {

			resourcesJSON = objectMapper.writeValueAsString(resources);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform resources list object to JSON string.\n" + e.getMessage());
		}

		ResourcesResource.LOG.debug("return all resources ");
		ResourcesResource.LOG.trace("'" + resourcesJSON + "'");

		dmpStatus.stop(context);
		return buildResponse(resourcesJSON);
	}

	/**
	 * This endpoint returns a resource as JSON representation for the provided resource identifier.
	 * 
	 * @param id a resource identifier
	 * @return a JSON representation of a resource
	 */
	@ApiOperation(value = "get the data resource that matches the given id", notes = "Returns the Resource object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the resource (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find a resource for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResource(@ApiParam(value = "data resource identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {
		final Timer.Context context = dmpStatus.getSingleResource();

		final Optional<Resource> resourceOptional = dataModelUtil.fetchResource(id);

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

		ResourcesResource.LOG.debug("return resource with id '" + id + "' and content ");
		ResourcesResource.LOG.trace("'" + resourceJSON + "'");

		dmpStatus.stop(context);
		return buildResponse(resourceJSON);
	}

	/**
	 * This endpoint processes (uploades) the input stream and update an existing resource object with related metadata that will
	 * be returned as JSON representation.
	 * 
	 * @param uploadedInputStream the input stream that should be uploaded
	 * @param fileDetail file metadata
	 * @param name the name of the resource
	 * @param description an description of the resource
	 * @return a JSON representation of the created resource
	 * @throws DMPControllerException
	 */
	@PUT
	@Path("/{id}")
	@ApiOperation(value = "update data resource", notes = "Returns a Resource object, when update was successfully.", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "data resource was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a resource for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateResource(@ApiParam(value = "resource identifier", required = true) @PathParam("id") final Long id,
			@ApiParam(value = "file input stream", required = true) @FormDataParam("file") final InputStream uploadedInputStream,
			@ApiParam("file metadata") @FormDataParam("file") final FormDataContentDisposition fileDetail,
			@ApiParam(value = "resource name", required = true) @FormDataParam("name") final String name,
			@ApiParam("resource description") @FormDataParam("description") final String description) throws DMPControllerException {

		final Timer.Context context = dmpStatus.updateResource();

		final Optional<Resource> resourceOptional = dataModelUtil.fetchResource(id);

		if (!resourceOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		final Resource resource = resourceOptional.get();

		ResourcesResource.LOG.debug("try to update resource '" + name + "' for file '" + fileDetail.getFileName() + "'");

		final ProxyResource proxyResource = refreshResource(resource, uploadedInputStream, fileDetail, name, description);

		final Resource updateResource = proxyResource.getObject();

		final String resourceJSON;

		try {

			resourceJSON = objectMapper.writeValueAsString(updateResource);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform resource object to JSON string");
		}

		ResourcesResource.LOG.debug("updated resource with id '" + id + "' ");
		ResourcesResource.LOG.trace("and JSON content '" + resourceJSON + "'");

		dmpStatus.stop(context);
		return buildResponse(resourceJSON);
	}

	/**
	 * This endpoint deletes a resource that matches the given id.
	 * 
	 * @param id a resource identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 *         went wrong
	 */
	@ApiOperation(value = "delete the data resource that matches the given id", notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "resource was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a resource for the given id"),
			@ApiResponse(code = 409, message = "resource couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	public Response deleteResource(@ApiParam(value = "data resource identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {
		final Timer.Context context = dmpStatus.deleteResource();

		ResourcesResource.LOG.debug("try to delete resource with id '" + id + "'");

		Optional<Resource> resourceOptional = dataModelUtil.fetchResource(id);

		if (!resourceOptional.isPresent()) {

			ResourcesResource.LOG.debug("couldn't find resource '" + id + "'");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		dataModelUtil.deleteResource(id);

		resourceOptional = dataModelUtil.fetchResource(id);

		if (resourceOptional.isPresent()) {

			ResourcesResource.LOG.debug("couldn't delete resource '" + id + "'");

			dmpStatus.stop(context);
			return Response.status(Status.CONFLICT).build();
		}

		ResourcesResource.LOG.debug("deletion of resource with id '" + id + "' was successful");

		dmpStatus.stop(context);
		return Response.status(Status.NO_CONTENT).build();
	}

	/**
	 * Returns the content of the uploaded resource line-wise.
	 * 
	 * @param id a resource identifier
	 * @param atMost the number of lines that should be returned at most
	 * @param encoding the encoding of the uploaded resource
	 * @return a JSON representation of the content
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get the lines of the data resource that matches the given id", notes = "Returns the lines of the data resource that matches the given id. The number of lines can be limited via the 'atMost' parameter. The encoding can be set via the 'encoding' parameter.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "raw data of data resource could be retrieved"),
			@ApiResponse(code = 404, message = "could not find a resource for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/{id}/lines")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourcePlain(@ApiParam(value = "data resource identifier", required = true) @PathParam("id") final Long id,
			@ApiParam(value = "number of lines limit", defaultValue = "50") @DefaultValue("50") @QueryParam("atMost") final int atMost,
			@ApiParam(value = "data resource encoding", defaultValue = "UTF-8") @DefaultValue("UTF-8") @QueryParam("encoding") final String encoding)
			throws DMPControllerException {
		final Timer.Context context = dmpStatus.getSingleResource();

		final Optional<Resource> resourceOptional = dataModelUtil.fetchResource(id);

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
			lines = com.google.common.io.Files.readLines(new File(filePath), Charset.forName(encoding), new LineProcessor<List<String>>() {

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

	/**
	 * This endpoint delivers all configurations that are related to this resource.
	 * 
	 * @param id a resource identifier
	 * @return a JSON representation of a list of configurations
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all configurations of the data resource that matches the given id", notes = "Returns the configurations of the data resource that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all configurations (as JSON) of the resource that matches the given id"),
			@ApiResponse(code = 404, message = "could not find a resource for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/{id}/configurations")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfigurations(@ApiParam(value = "data resource identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {
		final Timer.Context context = dmpStatus.getAllConfigurations();

		ResourcesResource.LOG.debug("try to get resource configurations for resource with id '" + id.toString() + "'");

		final Optional<Resource> resourceOptional = dataModelUtil.fetchResource(id);

		if (!resourceOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		final Resource resource = resourceOptional.get();

		ResourcesResource.LOG.debug("got resource with id '" + id.toString() + "' for resource configurations retrieval ");
		ResourcesResource.LOG.trace("= '" + ToStringBuilder.reflectionToString(resource) + "'");

		final Set<Configuration> configurations = resource.getConfigurations();

		if (configurations == null || configurations.isEmpty()) {

			ResourcesResource.LOG.debug("couldn't find configurations for resource '" + id + "'; or there are no configurations for this resource");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		ResourcesResource.LOG.debug("got resource configurations for resource with id '" + id.toString() + "' ");
		ResourcesResource.LOG.trace("= '" + ToStringBuilder.reflectionToString(configurations) + "'");

		final String configurationsJSON;

		try {

			configurationsJSON = objectMapper.writeValueAsString(resource.getConfigurations());
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform resource configurations set to JSON string.\n" + e.getMessage());
		}

		ResourcesResource.LOG.debug("return resource configurations for resource with id '" + id.toString() + "' ");
		ResourcesResource.LOG.trace("and content '" + configurationsJSON + "'");

		dmpStatus.stop(context);
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
	 * @param id a resource identifier
	 * @param jsonObjectString a JSON representation of a configuration.
	 * @return a JSON representation of the added configuration
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "add a new configuration to the data resource that matches the given id", notes = "Returns the new configuration that was added to the data resource that matches the given id. Note: The data processing, which was a step of this operation is deprecated, i.e., only the given configuration will be added to this resource. For data processing of a given resource with a given configuration, please utilise POST [base uri]/datamodels instead")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "configuration was successfully persisted and added to the resource for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Path("/{id}/configurations")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addConfiguration(@ApiParam(value = "data resource identifier", required = true) @PathParam("id") final Long id,
			@ApiParam(value = "configuration (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {
		final Timer.Context context = dmpStatus.createNewConfiguration();

		ResourcesResource.LOG.debug("try to create new configuration for resource with id '" + id + "'");

		final Optional<Resource> resourceOptional = dataModelUtil.fetchResource(id);

		if (!resourceOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		ResourcesResource.LOG.debug("try to add new configuration to resource with id '" + id + "'");

		final Resource resource = resourceOptional.get();

		final ProxyConfiguration proxyConfiguration = addConfiguration(resource, jsonObjectString);

		if (proxyConfiguration == null) {

			ResourcesResource.LOG.debug("couldn't add configuration to resource with id '" + id + "'");

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't add configuration to resource with id '" + id + "'");
		}

		final Configuration configuration = proxyConfiguration.getObject();

		if (configuration == null) {

			ResourcesResource.LOG.debug("couldn't add configuration to resource with id '" + id + "'");

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't add configuration to resource with id '" + id + "'");
		}

		ResourcesResource.LOG.debug("added new configuration to resource with id '" + id + "' ");
		ResourcesResource.LOG.trace("= '" + ToStringBuilder.reflectionToString(configuration) + "'");

		final String configurationJSON;

		try {

			configurationJSON = objectMapper.writeValueAsString(configuration);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform resource configuration to JSON string.\n" + e.getMessage());
		}

		final URI baseURI = uri.getRequestUri();
		final URI configurationURI = URI.create(baseURI.toString() + "/" + configuration.getId());

		ResourcesResource.LOG.debug("return new configuration at '" + configurationURI.toString() + "' ");
		ResourcesResource.LOG.trace("with content '" + configurationJSON + "'");

		dmpStatus.stop(context);
		return buildResponseCreated(configurationJSON, configurationURI, proxyConfiguration.getType(), "configuration");
	}

	/**
	 * This endpoint delivers a configuration for the given resource identifier and configuration identifier.
	 * 
	 * @param id a resource identifier
	 * @param configurationId a configuration identifier
	 * @return a JSON representation of the matched configuration
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get the configuration of the data resource that matches the given data resource id and the given configuration id", notes = "Returns the configuration of the data resource that matches the given data resource id and the given configuration id.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "returns a configuration (as JSON) that matches the given configuration id and is related to the resource that matches the given resource id"),
			@ApiResponse(code = 404, message = "could not find a configuration for the given configuration id and/or resource id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/{id}/configurations/{configurationid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceConfiguration(@ApiParam(value = "data resource identifier", required = true) @PathParam("id") final Long id,
			@ApiParam(value = "configuration identifier", required = true) @PathParam("configurationid") final Long configurationId)
			throws DMPControllerException {
		final Timer.Context context = dmpStatus.getSingleConfiguration();

		final Optional<Configuration> configurationOptional = dataModelUtil.fetchConfiguration(id, configurationId);

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

		ResourcesResource.LOG.debug("return configuration with id '" + configurationId + "' for resource with id '" + id + "' ");
		ResourcesResource.LOG.trace("and content '" + configurationJSON + "'");

		dmpStatus.stop(context);
		return buildResponse(configurationJSON);
	}

	/**
	 * note: [@tgaengler] this operation should be moved to {@link DataModelsResource} and there should be a generic preview
	 * operation
	 * 
	 * @param id
	 * @param jsonObjectString
	 * @return
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get a CSV data preview of the data resource that matches the given id and where the given configuration will be applied to ( = data model)", notes = "Returns a CSV data preview of the data resource that matches the given id and where the given configuration will be applied to ( = data model).")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "a preview of the CSV data of the data resource, where the given configuration was applied, could be retrieved"),
			@ApiResponse(code = 404, message = "could not find a resource for the given id or data for this resource"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Path("/{id}/configurationpreview")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response csvPreviewConfiguration(@ApiParam(value = "data resource identifier", required = true) @PathParam("id") final Long id,
			@ApiParam(value = "configuration (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {
		final Timer.Context context = dmpStatus.configurationsPreview();

		ResourcesResource.LOG.debug("try to apply configuration for resource with id '" + id + "'");
		ResourcesResource.LOG.debug("try to receive resource with id '" + id + "' for csv configuration preview");

		final Optional<Resource> resourceOptional = dataModelUtil.fetchResource(id);

		if (!resourceOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		final Resource resource = resourceOptional.get();

		ResourcesResource.LOG.debug("found resource with id '" + id + "' for csv configuration preview ");
		ResourcesResource.LOG.trace("= '" + ToStringBuilder.reflectionToString(resource) + "'");
		ResourcesResource.LOG.debug("try to apply configuration to resource with id '" + id + "'");

		final String result = applyConfigurationForCSVPreview(resource, jsonObjectString);

		if (result == null) {

			ResourcesResource.LOG.debug("couldn't apply configuration to resource with id '" + id + "'");

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't apply configuration to resource with id '" + id + "'");
		}

		ResourcesResource.LOG.debug("applied configuration to resource with id '" + id + "'");

		dmpStatus.stop(context);
		return buildResponse(result);
	}

	/**
	 * note: [@tgaengler] this operation should be moved to {@link DataModelsResource} and there should be a generic preview
	 * operation
	 * 
	 * @param id
	 * @param jsonObjectString
	 * @return
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get a CSV JSON data preview of the data resource that matches the given id and where the given configuration will be applied to ( = data model)", notes = "Returns a CSV JSON data preview of the data resource that matches the given id and where the given configuration will be applied to ( = data model).")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "a preview of the CSV JSON data of the data resource, where the given configuration was applied, could be retrieved"),
			@ApiResponse(code = 404, message = "could not find a resource for the given id or data for this resource"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Path("/{id}/configurationpreview")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response csvJSONPreviewConfiguration(@ApiParam(value = "data resource identifier", required = true) @PathParam("id") final Long id,
			@ApiParam(value = "configuration (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {
		final Timer.Context context = dmpStatus.configurationsPreview();

		ResourcesResource.LOG.debug("try to apply configuration for resource with id '" + id + "'");
		ResourcesResource.LOG.debug("try to recieve resource with id '" + id + "' for csv json configuration preview");

		final Optional<Resource> resourceOptional = dataModelUtil.fetchResource(id);

		if (!resourceOptional.isPresent()) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		final Resource resource = resourceOptional.get();

		ResourcesResource.LOG.debug("found resource with id '" + id + "' for csv json configuration preview ");
		ResourcesResource.LOG.trace("= '" + ToStringBuilder.reflectionToString(resource) + "'");
		ResourcesResource.LOG.debug("try to apply configuration to resource with id '" + id + "'");

		final String result = applyConfigurationForCSVJSONPreview(resource, jsonObjectString);

		if (result == null) {

			ResourcesResource.LOG.debug("couldn't apply configuration to resource with id '" + id + "'");

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't apply configuration to resource with id '" + id + "'");
		}

		ResourcesResource.LOG.debug("applied configuration to resource with id '" + id + "'");

		dmpStatus.stop(context);
		return buildResponse(result);
	}

	/**
	 * Process stores the input stream and creates and persists a new resource with the related metadata.
	 * 
	 * @param uploadInputedStream an input stream that should be uploaded
	 * @param fileDetail metadata of the given input stream
	 * @param name the name of the resource
	 * @param description an description of the resource
	 * @return a JSON representation of the new resource
	 * @throws DMPControllerException
	 */
	private ProxyResource createResource(final InputStream uploadInputedStream, final FormDataContentDisposition fileDetail, final String name,
			final String description) throws DMPControllerException {

		final ResourceService resourceService = resourceServiceProvider.get();

		ProxyResource proxyResource;

		try {

			proxyResource = resourceService.createObjectTransactional();
		} catch (final DMPPersistenceException e) {

			ResourcesResource.LOG.debug("something went wrong while resource creation");

			throw new DMPControllerException("something went wrong while resource creation\n" + e.getMessage());
		}

		if (proxyResource == null) {

			throw new DMPControllerException("fresh resource shouldn't be null");
		}

		final Resource resource = proxyResource.getObject();

		if (resource == null) {

			throw new DMPControllerException("fresh resource shouldn't be null");
		}

		final ProxyResource refreshedResource = refreshResource(resource, uploadInputedStream, fileDetail, name, description);

		// re-wrap with correct type (created)
		final ProxyResource refreshedProxyResource = new ProxyResource(refreshedResource.getObject(), proxyResource.getType());

		return refreshedProxyResource;
	}

	/**
	 * Process stores the input stream and update a resource with the related metadata.
	 * 
	 * @param uploadInputedStream an input stream that should be uploaded
	 * @param fileDetail metadata of the given input stream
	 * @param name the name of the resource
	 * @param description an description of the resource
	 * @return a JSON representation of the updated resource
	 * @throws DMPControllerException
	 */
	private ProxyResource refreshResource(final Resource resource, final InputStream uploadInputedStream,
			final FormDataContentDisposition fileDetail, final String name, final String description) throws DMPControllerException {

		final File file = controllerUtils.writeToFile(uploadInputedStream, fileDetail.getFileName(), "resources");

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

		attributes.put("path", file.getAbsolutePath());
		attributes.put("filesize", fileDetail.getSize());

		String fileType = null;
		final Tika tika = new Tika();
		try {
			fileType = tika.detect(file);
			// fileType = Files.probeContentType(file.toPath());
		} catch (final IOException e1) {
			ResourcesResource.LOG.debug("couldn't determine file type from file '{}'", file.getAbsolutePath());
		}
		if (fileType != null) {
			attributes.put("filetype", fileType);
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

			ResourcesResource.LOG.debug("something went wrong while resource updating");

			throw new DMPControllerException("something went wrong while resource updating\n" + e.getMessage());
		}

		return proxyResource;
	}

	/**
	 * Adds and persists a configuration to the given resource.
	 * 
	 * @param resource a resource
	 * @param configurationJSONString a JSON representation of a new configuration
	 * @return the new configuration
	 * @throws DMPControllerException
	 */
	private ProxyConfiguration addConfiguration(final Resource resource, final String configurationJSONString) throws DMPControllerException {

		final Configuration configurationFromJSON = getConfiguration(configurationJSONString);

		final ConfigurationService configurationService = configurationServiceProvider.get();

		ProxyConfiguration proxyConfiguration = null;

		if (configurationFromJSON.getId() == null) {

			// create new configuration, since it has no id

			proxyConfiguration = createNewConfiguration(configurationService);
		} else {

			// try to retrieve configuration via id from "configuration from JSON"

			final Configuration retrievedConfiguration = configurationService.getObject(configurationFromJSON.getId());

			if (retrievedConfiguration == null) {

				// if the id is not in the DB, also create a new object

				proxyConfiguration = createNewConfiguration(configurationService);
			} else {

				RetrievalType type = null;

				final Set<Configuration> configurations = resource.getConfigurations();

				if (configurations != null) {

					for (final Configuration configuration : configurations) {

						if (configuration.getId().equals(retrievedConfiguration.getId())) {

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

			ResourcesResource.LOG.debug("something went wrong while configuration updating");

			throw new DMPControllerException("something went wrong while configuration updating\n" + e.getMessage());
		}

		final ResourceService resourceService = resourceServiceProvider.get();

		try {

			resourceService.updateObjectTransactional(resource);
		} catch (final DMPPersistenceException e) {

			ResourcesResource.LOG.debug("something went wrong while resource updating for configuration");

			throw new DMPControllerException("something went wrong while resource updating for configuration\n" + e.getMessage());
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

			ResourcesResource.LOG.debug("something went wrong while configuration creation");

			throw new DMPControllerException("something went wrong while configuration creation\n" + e.getMessage());
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

			ResourcesResource.LOG.debug("something went wrong while deserializing the configuration JSON string");

			throw new DMPControllerException("something went wrong while deserializing the configuration JSON string.\n" + e.getMessage());
		}

		if (configurationFromJSON == null) {

			throw new DMPControllerException("deserialized configuration is null");
		}

		return configurationFromJSON;
	}

}
