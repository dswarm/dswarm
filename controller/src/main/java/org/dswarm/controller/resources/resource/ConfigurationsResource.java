/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.ExtendedBasicDMPResource;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.proxy.ProxyConfiguration;
import org.dswarm.persistence.service.resource.ConfigurationService;

/**
 * A resource (controller service) for {@link Configuration}s.
 *
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/configurations", description = "Operations about configurations")
@Path("configurations")
public class ConfigurationsResource extends
		ExtendedBasicDMPResource<ConfigurationService, ProxyConfiguration, Configuration> {

	/**
	 * Creates a new resource (controller service) for {@link Configuration}s with the provider of the component persistence
	 * service, the object mapper and metrics registry.
	 *
	 * @param persistenceServiceProviderArg
	 * @param objectMapperProviderArg
	 * @throws DMPControllerException
	 */
	@Inject
	public ConfigurationsResource(final Provider<ConfigurationService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg) throws DMPControllerException {

		super(Configuration.class, persistenceServiceProviderArg, objectMapperProviderArg);
	}

	/**
	 * This endpoint returns a configuration as JSON representation for the provided configuration identifier.
	 *
	 * @param id a configuration identifier
	 * @return a JSON representation of a configuration
	 */
	@ApiOperation(value = "get the configuration that matches the given id", notes = "Returns the Configuration object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the configuration (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find a configuration for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "configuration identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a configuration as JSON representation and persists this configuration in the database.
	 *
	 * @param jsonObjectString a JSON representation of one configuration
	 * @return the persisted configuration as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new configuration", notes = "Returns a new Configuration object.", response = Configuration.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "configuration was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "configuration identifier", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all configurations as JSON representation.
	 *
	 * @return a list of all configurations as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all configurations ", notes = "Returns a list of Configuration objects.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all available configurations (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any configuration, i.e., there are no configurations available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * This endpoint deletes a configuration that matches the given id.
	 *
	 * @param id a configuration identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 * went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "delete configuration that matches the given id", notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "configuration was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a configuration for the given id"),
			@ApiResponse(code = 409, message = "configuration couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "configuration identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}

	/**
	 * This endpoint consumes a configuration as JSON representation and updates this configuration in the database.
	 *
	 * @param jsonObjectString a JSON representation of one configuration
	 * @param uuid             a configuration identifier
	 * @return the updated configuration as JSON representation
	 * @throws DMPControllerException
	 */
	@Override
	@ApiOperation(value = "update configuration with given id ", notes = "Returns an updated Configuration object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "configuration was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a configuration for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "configuration (as JSON)", required = true) final String jsonObjectString,
			@ApiParam(value = "configuration identifier", required = true) @PathParam("id") final String uuid) throws DMPControllerException {

		return super.updateObject(jsonObjectString, uuid);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, description, resources and parameters of the configuration.
	 */
	@Override
	protected Configuration prepareObjectForUpdate(final Configuration objectFromJSON, final Configuration object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		final ObjectNode parameters = objectFromJSON.getParameters();

		object.setParameters(parameters);

		// TODO: disable resource updating for now (until resource id ref resolution is implemented)

		// object.setResources(objectFromJSON.getResources());

		return object;
	}
}
