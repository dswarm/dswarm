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
import org.dswarm.controller.resources.BasicDMPResource;
import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.job.proxy.ProxyFilter;
import org.dswarm.persistence.service.job.FilterService;

/**
 * A resource (controller service) for {@link Filter}s.
 *
 * @author tgaengler
 * @author fniederlein
 */
@RequestScoped
@Api(value = "/filters", description = "Operations about filters.")
@Path("filters")
public class FiltersResource extends BasicDMPResource<FilterService, ProxyFilter, Filter> {

	/**
	 * Creates a new resource (controller service) for {@link Filter}s with the provider of the filter persistence service, the
	 * object mapper and metrics registry.
	 *
	 * @param persistenceServiceProviderArg
	 * @param objectMapperProviderArg
	 */
	@Inject
	public FiltersResource(final Provider<FilterService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg) throws DMPControllerException {

		super(Filter.class, persistenceServiceProviderArg, objectMapperProviderArg);
	}

	/**
	 * This endpoint returns a filter as JSON representation for the provided filter identifier.
	 *
	 * @param id a filter identifier
	 * @return a JSON representation of a filter
	 */
	@ApiOperation(value = "get the filter that matches the given id", notes = "Returns the Filter object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the filter (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find a filter for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "filter identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a filter as JSON representation and persists this filter in the database.
	 *
	 * @param jsonObjectString a JSON representation of one filter
	 * @return the persisted filter as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new filter", notes = "Returns a new Filter object.", response = Filter.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "filter was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "filter (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all filters as JSON representation.
	 *
	 * @return a list of all filters as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all filters ", notes = "Returns a list of Filter objects.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all available filters (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any filter, i.e., there are no filters available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * This endpoint consumes a filter as JSON representation and updates this filter in the database.
	 *
	 * @param jsonObjectString a JSON representation of one filter
	 * @param uuid             a filter identifier
	 * @return the updated filter as JSON representation
	 * @throws DMPControllerException
	 */
	@Override
	@ApiOperation(value = "update filter with given id ", notes = "Returns an updated Filter object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "filter was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a filter for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "filter (as JSON)", required = true) final String jsonObjectString,
			@ApiParam(value = "filter identifier", required = true) @PathParam("id") final String uuid) throws DMPControllerException {

		return super.updateObject(jsonObjectString, uuid);
	}

	/**
	 * This endpoint deletes a filter that matches the given id.
	 *
	 * @param id a filter identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 * went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "delete filter that matches the given id", notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "filter was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a filter for the given id"),
			@ApiResponse(code = 409, message = "filter couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "filter identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name and expression of the filter.
	 */
	@Override
	protected Filter prepareObjectForUpdate(final Filter objectFromJSON, final Filter object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setExpression(objectFromJSON.getExpression());

		return object;
	}
}
