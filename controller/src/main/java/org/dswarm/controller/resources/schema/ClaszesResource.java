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
package org.dswarm.controller.resources.schema;

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
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.AdvancedDMPResource;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.proxy.ProxyClasz;
import org.dswarm.persistence.service.schema.ClaszService;

/**
 * A resource (controller service) for {@link Clasz}es.
 *
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/classes", description = "Operations about classes.")
@Path("classes")
public class ClaszesResource extends AdvancedDMPResource<ClaszService, ProxyClasz, Clasz> {

	/**
	 * Creates a new resource (controller service) for {@link Clasz}s with the provider of the class persistence service, the
	 * object mapper and metrics registry.
	 *
	 * @param persistenceServiceProviderArg
	 * @param objectMapperProviderArg
	 */
	@Inject
	public ClaszesResource(final Provider<ClaszService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg) throws DMPControllerException {

		super(Clasz.class, persistenceServiceProviderArg, objectMapperProviderArg);
	}

	/**
	 * This endpoint returns a class as JSON representation for the provided class identifier.<br/>
	 *
	 * @param id a class identifier
	 * @return a JSON representation of a class
	 */
	@ApiOperation(value = "get the class that matches the given id", notes = "Returns the Clasz object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the class (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find a class for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "class identifier", required = true) @PathParam("id") final String id) throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a class as JSON representation and persists this class in the database.
	 *
	 * @param jsonObjectString a JSON representation of one class
	 * @return the persisted class as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new class", notes = "Returns a new Clasz object.", response = Clasz.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "class does already exist; returns the existing one"),
			@ApiResponse(code = 201, message = "class was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "class (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all classes as JSON representation.
	 *
	 * @return a list of all classes as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all classes", notes = "Returns a list of Clasz objects.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all available classes (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any class, i.e., there are no classes available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * This endpoint consumes a class as JSON representation and updates this class in the database.
	 *
	 * @param jsonObjectString a JSON representation of one class
	 * @param uuid             a class identifier
	 * @return the updated class as JSON representation
	 * @throws DMPControllerException
	 */
	@Override
	@ApiOperation(value = "update class with given id ", notes = "Returns an updated Clasz object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "class was successfully updated"),
			@ApiResponse(code = 201, message = "class was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "class (as JSON)", required = true) final String jsonObjectString,
			@ApiParam(value = "class identifier", required = true) @PathParam("id") final String uuid) throws DMPControllerException {

		return super.updateObject(jsonObjectString, uuid);
	}

	/**
	 * This endpoint deletes a class that matches the given id.
	 *
	 * @param id a class identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 * went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "delete class that matches the given id", notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "class was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a class for the given id"),
			@ApiResponse(code = 409, message = "class couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "class identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}
}
