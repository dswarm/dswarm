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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.BasicResource;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePath;
import org.dswarm.persistence.service.schema.AttributePathService;

/**
 * A resource (controller service) for {@link AttributePath}s.
 *
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/attributepaths", description = "Operations about attribute paths.")
@Path("attributepaths")
public class AttributePathsResource extends BasicResource<AttributePathService, ProxyAttributePath, AttributePath> {

	private static final Logger LOG = LoggerFactory.getLogger(AttributePathsResource.class);

	/**
	 * Creates a new resource (controller service) for {@link AttributePath}s with the provider of the attribute path persistence
	 * service, the object mapper and metrics registry.
	 *
	 * @param persistenceServiceProviderArg
	 * @param objectMapperProviderArg
	 */
	@Inject
	public AttributePathsResource(final Provider<AttributePathService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg) throws DMPControllerException {

		super(AttributePath.class, persistenceServiceProviderArg, objectMapperProviderArg);
	}

	/**
	 * This endpoint returns an attribute path as JSON representation for the provided attribute paths identifier.
	 *
	 * @param id an attribute path identifier
	 * @return a JSON representation of an attribute path
	 */
	@ApiOperation(value = "get the attribute path that matches the given id", notes = "Returns the AttributePath object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the attribute path (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find an attribute path for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "attribute path identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes an attribute path as JSON representation and persists this attribute path in the database.
	 *
	 * @param jsonObjectString a JSON representation of one attribute path
	 * @return the persisted attribute path as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new attribute path", notes = "Returns a new AttributePath object.", response = AttributePath.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "attribute path does already exist; returns the existing one"),
			@ApiResponse(code = 201, message = "attribute path was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "attribute path (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all attribute paths as JSON representation.
	 *
	 * @return a list of all attribute paths as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all attribute paths ", notes = "Returns a list of AttributePath objects.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all available attribute paths (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any attribute path, i.e., there are no attribute paths available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * This endpoint consumes a attribute path as JSON representation and updates this attribute path in the database.
	 *
	 * @param jsonObjectString a JSON representation of one attribute path
	 * @param uuid             a attribute path identifier
	 * @return the updated attribute path as JSON representation
	 * @throws DMPControllerException
	 */
	@Override
	@ApiOperation(value = "update attribute path with given id ", notes = "Returns an updated AttributePath object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "attribute path was successfully updated"),
			@ApiResponse(code = 404, message = "could not find an attribute path for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "attribute path (as JSON)", required = true) final String jsonObjectString,
			@ApiParam(value = "attribute path identifier", required = true) @PathParam("id") final String uuid) throws DMPControllerException {

		return super.updateObject(jsonObjectString, uuid);
	}

	/**
	 * This endpoint deletes a attribute path that matches the given id.
	 *
	 * @param id an attribute path identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 * went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "delete attribute path that matches the given id",
			notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "attribute path was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find an attribute path for the given id"),
			@ApiResponse(code = 409, message = "attribute path couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "attribute path identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name and list of attributes of the attribute path.
	 */
	@Override
	protected AttributePath prepareObjectForUpdate(final AttributePath objectFromJSON, final AttributePath object) {

		// if (!object.getId().equals(objectFromJSON.getId())) {
		//
		// object.setAttributePath(objectFromJSON.getAttributePath());
		// } else {

		// attribute path was already retrieved by attribute path string (maybe + attributes where created, because they
		// didn't exist before)
		// => replace dummy id'ed attributes with real ids by attribute uri

		final List<Attribute> attributePath = objectFromJSON.getAttributePath();

		if (attributePath != null) {

			final Set<Attribute> persistentAttributes = object.getAttributes();

			if (persistentAttributes != null) {

				final Set<String> attributeURIsFromDummyIdsFromObjectFromJSON = Sets.newHashSet();
				final Map<String, Attribute> attributeFromRealIdsFromObject = Maps.newHashMap();

				// collect uris of attributes with dummy id

				for (final Attribute attribute : attributePath) {

					// note: one could even collect all attribute ids and replace them by their actual ones
					// => ok, let's do this

					//if (attribute.getId() < 0) {

					attributeURIsFromDummyIdsFromObjectFromJSON.add(attribute.getUri());
					//}
				}

				// collect attributes that match the uris of the attribute with dummy id

				for (final Attribute attribute : persistentAttributes) {

					if (attributeURIsFromDummyIdsFromObjectFromJSON.contains(attribute.getUri())) {

						attributeFromRealIdsFromObject.put(attribute.getUri(), attribute);
					}
				}

				final LinkedList<Attribute> newAttributePath = Lists.newLinkedList();

				// construct new attribute path

				for (final Attribute attribute : attributePath) {

					final Attribute newAttribute = attributeFromRealIdsFromObject.get(attribute.getUri());

					if (newAttribute == null) {

						newAttributePath.add(attribute);
					} else {

						newAttributePath.add(newAttribute);
					}
				}

				object.setAttributePath(newAttributePath);
			}
		}
		// }

		return object;
	}

	@Override
	protected AttributePath retrieveObject(final String uuid, final String jsonObjectString) throws DMPControllerException {

		if (jsonObjectString == null) {

			return super.retrieveObject(uuid, jsonObjectString);
		}

		// what should we do if the attribute path is a different one? => check whether an entity
		// with this attribute path exists and manipulate this one instead
		// note: we could also throw an exception instead

		final AttributePath objectFromJSON = deserializeObjectJSONString(jsonObjectString);

		// get persistent object per attribute path

		final AttributePathService persistenceService = persistenceServiceProvider.get();

		AttributePath object = null;

		try {

			object = persistenceService.getObjectViaAttributePathJSON(objectFromJSON.getAttributePathAsJSONObjectString());
		} catch (final DMPPersistenceException e) {

			AttributePathsResource.LOG.debug("couldn't retrieve " + pojoClassName + " for attribute path '"
					+ objectFromJSON.toAttributePath() + "'");

			return null;
		}

		if (object == null) {

			// retrieve object by id and manipulate attribute path

			return super.retrieveObject(uuid, jsonObjectString);
		}

		AttributePathsResource.LOG.debug("got " + pojoClassName + " with attribute path '" + objectFromJSON.toAttributePath()
				+ "' ");
		AttributePathsResource.LOG.trace("= '" + ToStringBuilder.reflectionToString(object) + "'");

		return object;
	}

	@Override
	protected ProxyAttributePath createObject(final AttributePath objectFromJSON, final AttributePathService persistenceService)
			throws DMPPersistenceException {

		return persistenceService.createOrGetObjectTransactional(objectFromJSON.getAttributePath());
	}
}
