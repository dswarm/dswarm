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

import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.schema.utils.SchemaAttributePathInstancesResourceUtils;
import org.dswarm.controller.resources.utils.ResourceUtilsFactory;
import org.dswarm.controller.status.DMPStatus;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxySchemaAttributePathInstance;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;

/**
 * A resource (controller service) for {@link org.dswarm.persistence.model.schema.SchemaAttributePathInstance}s.
 *
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/schemaattributepathinstances", description = "Operations about schema attribute path instances.")
@Path("schemaattributepathinstances")
public class SchemaAttributePathInstancesResource
		extends
		AttributePathInstancesResource<SchemaAttributePathInstancesResourceUtils, SchemaAttributePathInstanceService, ProxySchemaAttributePathInstance, SchemaAttributePathInstance> {

	/**
	 * Creates a new resource (controller service) for {@link org.dswarm.persistence.model.schema.SchemaAttributePathInstance}s with the provider of the schema
	 * attribute path instance persistence service, the object mapper and metrics registry.
	 *
	 * @param utilsFactory
	 * @param dmpStatusArg
	 * @throws org.dswarm.controller.DMPControllerException
	 */
	@Inject
	public SchemaAttributePathInstancesResource(final ResourceUtilsFactory utilsFactory, final DMPStatus dmpStatusArg) throws DMPControllerException {

		super(utilsFactory.reset().get(SchemaAttributePathInstancesResourceUtils.class), dmpStatusArg);
	}

	/**
	 * This endpoint returns a schema attribute path instance as JSON representation for the provided schema attribute path
	 * instance identifier.
	 *
	 * @param id a schema attribute path instance identifier
	 * @return a JSON representation of a schema attribute path instance
	 */
	@ApiOperation(value = "get the schema attribute path instance that matches the given id",
			notes = "Returns the SchemaAttributePathInstance object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the schema attribute path instance (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find a schema attribute path instance for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "schema attribute path instance identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a schema attribute path instance as JSON representation and persists this schema attribute path instance in the database.
	 *
	 * @param jsonObjectString a JSON representation of one schema attribute path instance
	 * @return the persisted schema attribute path instance as JSON representation
	 * @throws org.dswarm.controller.DMPControllerException
	 */
	@ApiOperation(value = "create a new schema attribute path instance", notes = "Returns a new MappingAttributePathInstance object.",
			response = MappingAttributePathInstance.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "schema attribute path instance was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "schema attribute path instance (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all functions as JSON representation.
	 *
	 * @return a list of all functions as JSON representation
	 * @throws org.dswarm.controller.DMPControllerException
	 */
	@ApiOperation(value = "get all functions ", notes = "Returns a list of MappingAttributePathInstance objects.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all available functions (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any schema attribute path instance, i.e., there are no functions available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * This endpoint consumes a schema attribute path instance as JSON representation and updates this mapping attribute path
	 * instance in the database.
	 *
	 * @param jsonObjectString a JSON representation of one schema attribute path instance
	 * @param id               a schema attribute path instance identifier
	 * @return the updated schema attribute path instance as JSON representation
	 * @throws org.dswarm.controller.DMPControllerException
	 */
	@Override
	@ApiOperation(value = "update schema attribute path instance with given id ", notes = "Returns an updated MappingAttributePathInstance object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "schema attribute path instance was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a schema attribute path instance for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "schema attribute path instance (as JSON)", required = true) final String jsonObjectString,
			@ApiParam(value = "schema attribute path instance identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.updateObject(jsonObjectString, id);
	}

	/**
	 * This endpoint deletes a schema attribute path instance that matches the given id.
	 *
	 * @param id a schema attribute path instance identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 * went wrong
	 * @throws org.dswarm.controller.DMPControllerException
	 */
	@ApiOperation(value = "delete schema attribute path instance that matches the given id",
			notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "schema attribute path instance was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a schema attribute path instance for the given id"),
			@ApiResponse(code = 409,
					message = "schema attribute path instance couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "schema attribute path instance identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}

	/**
	 * {@inheritDoc}<br/>
	 */
	@Override
	protected SchemaAttributePathInstance prepareObjectForUpdate(final SchemaAttributePathInstance objectFromJSON,
			final SchemaAttributePathInstance object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setSubSchema(objectFromJSON.getSubSchema());

		return object;
	}
}
