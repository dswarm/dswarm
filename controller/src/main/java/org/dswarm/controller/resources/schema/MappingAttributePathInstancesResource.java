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
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyMappingAttributePathInstance;
import org.dswarm.persistence.service.schema.MappingAttributePathInstanceService;

/**
 * A resource (controller service) for {@link MappingAttributePathInstance}s.
 *
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/mappingattributepathinstances", description = "Operations about mapping attribute path instances.")
@Path("mappingattributepathinstances")
public class MappingAttributePathInstancesResource
		extends
		AttributePathInstancesResource<MappingAttributePathInstanceService, ProxyMappingAttributePathInstance, MappingAttributePathInstance> {

	/**
	 * Creates a new resource (controller service) for {@link MappingAttributePathInstance}s with the provider of the mapping
	 * attribute path instance persistence service, the object mapper and metrics registry.
	 *
	 * @param persistenceServiceProviderArg
	 * @param objectMapperProviderArg
	 * @throws DMPControllerException
	 */
	@Inject
	public MappingAttributePathInstancesResource(final Provider<MappingAttributePathInstanceService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg) throws DMPControllerException {

		super(MappingAttributePathInstance.class, persistenceServiceProviderArg, objectMapperProviderArg);
	}

	/**
	 * This endpoint returns a mapping attribute path instance as JSON representation for the provided mapping attribute path
	 * instance identifier.
	 *
	 * @param id a mapping attribute path instance identifier
	 * @return a JSON representation of a mapping attribute path instance
	 */
	@ApiOperation(value = "get the mapping attribute path instance that matches the given id",
			notes = "Returns the MappingAttributePathInstance object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the mapping attribute path instance (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find a mapping attribute path instance for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "mapping attribute path instance identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a mapping attribute path instance as JSON representation and persists this mapping attribute path instance in the database.
	 *
	 * @param jsonObjectString a JSON representation of one mapping attribute path instance
	 * @return the persisted mapping attribute path instance as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new mapping attribute path instance", notes = "Returns a new MappingAttributePathInstance object.",
			response = MappingAttributePathInstance.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "mapping attribute path instance was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "mapping attribute path instance (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all functions as JSON representation.
	 *
	 * @return a list of all functions as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all functions ", notes = "Returns a list of MappingAttributePathInstance objects.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all available functions (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any mapping attribute path instance, i.e., there are no functions available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * This endpoint consumes a mapping attribute path instance as JSON representation and updates this mapping attribute path
	 * instance in the database.
	 *
	 * @param jsonObjectString a JSON representation of one mapping attribute path instance
	 * @param uuid             a mapping attribute path instance identifier
	 * @return the updated mapping attribute path instance as JSON representation
	 * @throws DMPControllerException
	 */
	@Override
	@ApiOperation(value = "update mapping attribute path instance with given id ", notes = "Returns an updated MappingAttributePathInstance object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "mapping attribute path instance was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a mapping attribute path instance for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "mapping attribute path instance (as JSON)", required = true) final String jsonObjectString,
			@ApiParam(value = "mapping attribute path instance identifier", required = true) @PathParam("id") final String uuid)
			throws DMPControllerException {

		return super.updateObject(jsonObjectString, uuid);
	}

	/**
	 * This endpoint deletes a mapping attribute path instance that matches the given id.
	 *
	 * @param id a mapping attribute path instance identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 * went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "delete mapping attribute path instance that matches the given id",
			notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "mapping attribute path instance was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a mapping attribute path instance for the given id"),
			@ApiResponse(code = 409,
					message = "mapping attribute path instance couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "mapping attribute path instance identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}

	/**
	 * {@inheritDoc}<br/>
	 */
	@Override
	protected MappingAttributePathInstance prepareObjectForUpdate(final MappingAttributePathInstance objectFromJSON,
			final MappingAttributePathInstance object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setFilter(objectFromJSON.getFilter());
		object.setOrdinal(objectFromJSON.getOrdinal());

		return object;
	}
}
