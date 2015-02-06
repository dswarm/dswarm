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

import java.util.LinkedList;
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

import com.codahale.metrics.annotation.Timed;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.BasicDMPResource;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.proxy.ProxyContentSchema;
import org.dswarm.persistence.service.schema.ContentSchemaService;

/**
 * A resource (controller service) for {@link org.dswarm.persistence.model.schema.ContentSchema}s.
 *
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/contentschemas", description = "Operations about content schemas")
@Path("contentschemas")
public class ContentSchemasResource extends BasicDMPResource<ContentSchemaService, ProxyContentSchema, ContentSchema> {

	private static final Logger LOG = LoggerFactory.getLogger(ContentSchemasResource.class);

	/**
	 * Creates a new resource (controller service) for {@link org.dswarm.persistence.model.schema.ContentSchema}s with the
	 * provider of the content schema persistence service, the object mapper and metrics registry.
	 *
	 * @param persistenceServiceProviderArg
	 * @param objectMapperProviderArg
	 */
	@Inject
	public ContentSchemasResource(final Provider<ContentSchemaService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg) throws DMPControllerException {

		super(ContentSchema.class, persistenceServiceProviderArg, objectMapperProviderArg);
	}

	/**
	 * This endpoint returns a content schema as JSON representation for the provided content schema identifier.
	 *
	 * @param id a schema identifier
	 * @return a JSON representation of a content schema
	 */
	@ApiOperation(value = "get the content schema that matches the given id", notes = "Returns the ContentSchema object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the content schema (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find a content schema for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "schema identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a content schema as JSON representation and persists this content schema in the database.
	 *
	 * @param jsonObjectString a JSON representation of one content schema
	 * @return the persisted content schema as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new content schema", notes = "Returns a new ContentSchema object.", response = ContentSchema.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "content schema was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "content schema (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all content schemas as JSON representation.
	 *
	 * @return a list of all content schemas as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all content schemas ", notes = "Returns a list of ContentSchema objects.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all available content schemas (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any content schema, i.e., there are no content schemas available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * This endpoint consumes a content schema as JSON representation and updates this content schema in the database.
	 *
	 * @param jsonObjectString a JSON representation of one content schema
	 * @param uuid             a content schema identifier
	 * @return the updated content schema as JSON representation
	 * @throws DMPControllerException
	 */
	@Override
	@ApiOperation(value = "update content schema with given id ", notes = "Returns an updated ContentSchema object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "content schema was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a content schema for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "content schema (as JSON)", required = true) final String jsonObjectString,
			@ApiParam(value = "content schema identifier", required = true) @PathParam("id") final String uuid) throws DMPControllerException {

		return super.updateObject(jsonObjectString, uuid);
	}

	/**
	 * This endpoint deletes a content schema that matches the given id.
	 *
	 * @param id a content schema identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 * went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "delete content schema that matches the given id", notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "content schema was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a content schema for the given id"),
			@ApiResponse(code = 409, message = "content schema couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "content schema identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, record identifier attribute path, key attribute paths and value attribute path of the content schema.
	 */
	@Override
	protected ContentSchema prepareObjectForUpdate(final ContentSchema objectFromJSON, final ContentSchema object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		final AttributePath recordIdentifierAttributePath = objectFromJSON.getRecordIdentifierAttributePath();

		object.setRecordIdentifierAttributePath(recordIdentifierAttributePath);

		final LinkedList<AttributePath> keyAttributePaths = objectFromJSON.getKeyAttributePaths();

		if (keyAttributePaths != null) {

			final Set<AttributePath> persistentUtilisedKeyAttributePaths = object.getUtilisedKeyAttributePaths();

			if (persistentUtilisedKeyAttributePaths != null) {

				final Set<String> keyAttributePathStringsFromDummyIdsFromObjectFromJSON = Sets.newHashSet();
				final Map<String, AttributePath> keyAttributePathFromRealIdsFromObject = Maps.newHashMap();

				// collect attribute path strings of key attribute paths with dummy id

				for (final AttributePath keyAttributePath : keyAttributePaths) {

					// note: one could even collect all key attribute path ids and replace them by their actual ones
					// => ok, let's do this

					//if (keyAttributePath.getId() < 0) {

					keyAttributePathStringsFromDummyIdsFromObjectFromJSON.add(keyAttributePath.toAttributePath());
					//}
				}

				// collect key attribute paths that match the attribute paths of the key attribute path with dummy id

				for (final AttributePath persistentUtilisedKeyAttributePath : persistentUtilisedKeyAttributePaths) {

					if (keyAttributePathStringsFromDummyIdsFromObjectFromJSON.contains(persistentUtilisedKeyAttributePath.toAttributePath())) {

						keyAttributePathFromRealIdsFromObject.put(persistentUtilisedKeyAttributePath.toAttributePath(),
								persistentUtilisedKeyAttributePath);
					}
				}

				final LinkedList<AttributePath> newKeyAttributePaths = Lists.newLinkedList();

				// construct new key attribute paths list

				for (final AttributePath keyAttributePath : keyAttributePaths) {

					final AttributePath newKeyAttributePath = keyAttributePathFromRealIdsFromObject.get(keyAttributePath.toAttributePath());

					if (newKeyAttributePath == null) {

						newKeyAttributePaths.add(keyAttributePath);
					} else {

						newKeyAttributePaths.add(newKeyAttributePath);
					}
				}

				object.setKeyAttributePaths(newKeyAttributePaths);
			} else {

				object.setKeyAttributePaths(objectFromJSON.getKeyAttributePaths());
			}
		}

		final AttributePath valueAttributePath = objectFromJSON.getValueAttributePath();

		object.setValueAttributePath(valueAttributePath);

		return object;
	}
}
