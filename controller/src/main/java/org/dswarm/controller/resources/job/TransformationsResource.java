/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.controller.resources.job;

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
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.job.proxy.ProxyTransformation;
import org.dswarm.persistence.service.job.TransformationService;

/**
 * A resource (controller service) for {@link Transformation}s.
 *
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/transformations", description = "Operations about transformations.")
@Path("transformations")
public class TransformationsResource extends
		BasicFunctionsResource<TransformationService, ProxyTransformation, Transformation> {

	/**
	 * Creates a new resource (controller service) for {@link Transformation}s with the provider of the transformation persistence
	 * service, the object mapper and metrics registry.
	 *
	 * @param persistenceServiceProviderArg
	 * @param objectMapperProviderArg
	 */
	@Inject
	public TransformationsResource(final Provider<TransformationService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg) throws DMPControllerException {

		super(Transformation.class, persistenceServiceProviderArg, objectMapperProviderArg);
	}

	/**
	 * This endpoint returns a transformation as JSON representation for the provided transformation identifier.
	 *
	 * @param id a transformation identifier
	 * @return a JSON representation of a transformation
	 */
	@ApiOperation(value = "get the transformation that matches the given id", notes = "Returns the Transformation object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the transformation (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find a transformation for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "transformation identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a transformation as JSON representation and persists this transformation in the database.
	 *
	 * @param jsonObjectString a JSON representation of one transformation
	 * @return the persisted transformation as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new transformation", notes = "Returns a new Transformation object.", response = Transformation.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "transformation was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "transformation (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all transformations as JSON representation.
	 *
	 * @return a list of all transformations as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all transformations ", notes = "Returns a list of Transformation objects.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all available transformations (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any transformation, i.e., there are no transformations available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * This endpoint consumes a transformation as JSON representation and updates this transformation in the database.
	 *
	 * @param jsonObjectString a JSON representation of one transformation
	 * @param uuid             a transformation identifier
	 * @return the updated transformation as JSON representation
	 * @throws DMPControllerException
	 */
	@Override
	@ApiOperation(value = "update transformation with given id ", notes = "Returns an updated Transformation object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "transformation was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a transformation for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "transformation (as JSON)", required = true) final String jsonObjectString,
			@ApiParam(value = "transformation identifier", required = true) @PathParam("id") final String uuid) throws DMPControllerException {

		return super.updateObject(jsonObjectString, uuid);
	}

	/**
	 * This endpoint deletes a transformation that matches the given id.
	 *
	 * @param id a transformation identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 * went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "delete transformation that matches the given id", notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "transformation was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a transformation for the given id"),
			@ApiResponse(code = 409, message = "transformation couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "transformation identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, description, parameters, machine processable function description and components of the transformation.
	 */
	@Override
	protected Transformation prepareObjectForUpdate(final Transformation objectFromJSON, final Transformation object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setComponents(objectFromJSON.getComponents());

		return object;
	}
}
