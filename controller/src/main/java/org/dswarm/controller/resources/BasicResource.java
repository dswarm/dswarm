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
package org.dswarm.controller.resources;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

import javax.inject.Provider;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.DMPJsonException;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.proxy.ProxyDMPObject;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.service.BasicJPAService;

/**
 * A generic resource (controller service), whose concrete implementations can be derived with a given implementation of
 * {@link DMPObject} and the related identifier type. This service delivers basic controller layer functionality to create a new
 * object or retrieve existing ones.<br/>
 *
 * @param <POJOCLASSPERSISTENCESERVICE> the concrete persistence service of the resource that is related to the concrete POJO
 *                                      class
 * @param <POJOCLASS>                   the concrete POJO class of the resource
 * @author tgaengler
 * @author fniederlein
 */
public abstract class BasicResource<POJOCLASSPERSISTENCESERVICE extends BasicJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyDMPObject<POJOCLASS>, POJOCLASS extends DMPObject> {

	private static final Logger LOG = LoggerFactory.getLogger(BasicResource.class);

	protected final Class<POJOCLASS> pojoClass;

	protected final String pojoClassName;

	protected final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProvider;

	protected final Provider<ObjectMapper> objectMapperProvider;

	/**
	 * The base URI of this resource.
	 */
	@Context
	UriInfo uri;

	/**
	 * Creates a new resource (controller service) for the given concrete POJO class with the provider of the concrete persistence
	 * service and metrics registry.
	 *
	 * @param pojoClassArg
	 * @param persistenceServiceProviderArg
	 * @param objectMapperProviderArg
	 */
	protected BasicResource(final Class<POJOCLASS> pojoClassArg, final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg) {

		pojoClass = pojoClassArg;
		pojoClassName = pojoClass.getSimpleName();
		persistenceServiceProvider = persistenceServiceProviderArg;
		objectMapperProvider = objectMapperProviderArg;
	}

	/**
	 * Gets the concrete POJO class of this resource (controller service).
	 *
	 * @return the concrete POJO class
	 */
	public Class<POJOCLASS> getClasz() {

		return pojoClass;
	}

	/**
	 * Builds a positive response with the given content.
	 *
	 * @param responseContent a response message
	 * @return the response
	 */
	protected Response buildResponse(final String responseContent) {

		return Response.ok(responseContent).build();
	}

	/**
	 * This endpoint returns an object of the type of the POJO class as JSON representation for the provided object uuid.
	 *
	 * @param uuid an object uuid
	 * @return a JSON representation of an object of the type of the POJO class
	 */
	// @GET
	// @Path("/{uuid}")
	// @Produces(MediaType.APPLICATION_JSON)
	@Timed
	public Response getObject(/* @PathParam("uuid") */final String uuid) throws DMPControllerException {

		BasicResource.LOG.debug("try to get " + pojoClassName + " with uuid '" + uuid + "'");

		final POJOCLASSPERSISTENCESERVICE persistenceService = persistenceServiceProvider.get();
		final POJOCLASS object = persistenceService.getObject(uuid);

		if (object == null) {

			BasicResource.LOG.debug("couldn't find " + pojoClassName + " '" + uuid + "'");

			return Response.status(Status.NOT_FOUND).build();
		}
		BasicResource.LOG.debug("got " + pojoClassName + " with uuid '" + uuid + "'");
		BasicResource.LOG.trace(" = '" + ToStringBuilder.reflectionToString(object) + "'");

		final String objectJSON = serializeObject(object);

		BasicResource.LOG.debug("return " + pojoClassName + " with uuid '" + uuid + "'");
		BasicResource.LOG.trace(" = '" + objectJSON + "'");

		return buildResponse(objectJSON);
	}

	/**
	 * This endpoint consumes an object of the type of the POJO class as JSON representation and persists this object in the
	 * database.
	 *
	 * @param jsonObjectString a JSON representation of one object of the type of the POJO class
	 * @return the persisted object as JSON representation
	 * @throws DMPControllerException
	 */
	// @POST
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	@Timed
	public Response createObject(final String jsonObjectString) throws DMPControllerException {

		BasicResource.LOG.debug("try to create new " + pojoClassName);

		final PROXYPOJOCLASS proxyObject = addObject(jsonObjectString);

		if (proxyObject == null) {

			BasicResource.LOG.debug("couldn't add " + pojoClassName);
			throw new DMPControllerException("couldn't add " + pojoClassName);
		}

		final POJOCLASS object = proxyObject.getObject();

		if (object == null) {

			BasicResource.LOG.debug("couldn't add " + pojoClassName);
			throw new DMPControllerException("couldn't add " + pojoClassName);
		}

		BasicResource.LOG.debug("added new " + pojoClassName + " with id = '" + object.getUuid() + "' ");
		BasicResource.LOG.trace(" = '" + ToStringBuilder.reflectionToString(object) + "'");

		final String objectJSON = serializeObject(object);

		final URI objectURI = createObjectURI(object);

		if (objectURI == null) {

			BasicResource.LOG.debug("something went wrong, while minting the URL of the new " + pojoClassName);
			throw new DMPControllerException("something went wrong, while minting the URL of the new " + pojoClassName);
		}

		BasicResource.LOG.debug("return new " + pojoClassName + " at '" + objectURI.toString() + "'");
		BasicResource.LOG.trace("with content '" + objectJSON + "'");

		final ResponseBuilder responseBuilder;
		final RetrievalType type = proxyObject.getType();

		switch (type) {

			case CREATED:

				responseBuilder = Response.created(objectURI);

				break;
			case RETRIEVED:

				responseBuilder = Response.ok().contentLocation(objectURI);

				break;
			default:

				BasicResource.LOG.debug("something went wrong, while evaluating the retrieval type of the " + pojoClassName);
				throw new DMPControllerException("something went wrong, while evaluating the retrieval type of the "
						+ pojoClassName);
		}

		return responseBuilder.entity(objectJSON).build();
	}

	/**
	 * This endpoint consumes an object of the type of the POJO class as JSON representation and update this object in the
	 * database.
	 *
	 * @param jsonObjectString a JSON representation of one object of the type of the POJO class
	 * @param uuid             an object uuid
	 * @return the persisted object as JSON representation
	 * @throws DMPControllerException
	 */
	// @PUT
	// @Path("/{uuid}")
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	@Timed
	public Response updateObject(final String jsonObjectString, /* @PathParam("uuid") */final String uuid) throws DMPControllerException {

		BasicResource.LOG.debug("try to update " + pojoClassName + " '" + uuid + "'");

		final POJOCLASS objectFromDB = retrieveObject(uuid, jsonObjectString);

		if (objectFromDB == null) {

			return Response.status(Status.NOT_FOUND).build();
		}

		final PROXYPOJOCLASS proxyObject = refreshObject(jsonObjectString, objectFromDB, uuid);

		if (proxyObject == null) {

			BasicResource.LOG.debug("couldn't update " + pojoClassName + " '" + uuid + "'");

			throw new DMPControllerException("couldn't update " + pojoClassName + " '" + uuid + "'");
		}

		final POJOCLASS object = proxyObject.getObject();

		if (object == null) {

			BasicResource.LOG.debug("couldn't update " + pojoClassName + " '" + uuid + "'");

			throw new DMPControllerException("couldn't update " + pojoClassName + " '" + uuid + "'");
		}

		BasicResource.LOG.debug("updated " + pojoClassName + " '" + uuid + "'");
		BasicResource.LOG.trace(" = '" + ToStringBuilder.reflectionToString(object) + "'");

		final String objectJSON = serializeObject(object);

		BasicResource.LOG.debug("return updated " + pojoClassName + " with uuid '" + object.getUuid() + "'");
		BasicResource.LOG.trace(" = '" + objectJSON + "'");

		final URI objectURI = createObjectURI(object);
		final ResponseBuilder responseBuilder;
		final RetrievalType type = proxyObject.getType();

		switch (type) {

			case CREATED:

				responseBuilder = Response.created(objectURI);

				break;
			case UPDATED:
			case RETRIEVED:

				responseBuilder = Response.ok().contentLocation(objectURI);

				break;
			default:

				BasicResource.LOG.debug("something went wrong, while evaluating the retrieval type of the " + pojoClassName);

				throw new DMPControllerException("something went wrong, while evaluating the retrieval type of the "
						+ pojoClassName);
		}

		return responseBuilder.entity(objectJSON).build();
	}

	/**
	 * This endpoint returns a list of all objects of the type of the POJO class as JSON representation.
	 *
	 * @return a list of all objects of the type of the POJO class as JSON representation
	 * @throws DMPControllerException
	 */
	// @ApiOperation(value = "get all objects ", notes = "Returns a list objects.")
	// @GET
	// @Produces(MediaType.APPLICATION_JSON)
	@Timed
	public Response getObjects() throws DMPControllerException {

		BasicResource.LOG.debug("try to get all " + pojoClassName + "s");

		final POJOCLASSPERSISTENCESERVICE persistenceService = persistenceServiceProvider.get();

		final List<POJOCLASS> objects = persistenceService.getObjects();

		if (objects == null) {

			BasicResource.LOG.debug("couldn't find " + pojoClassName + "s");
			return Response.status(Status.NOT_FOUND).build();
		}

		if (objects.isEmpty()) {

			BasicResource.LOG.debug("there are no " + pojoClassName + "s");
			return Response.status(Status.NOT_FOUND).build();
		}

		BasicResource.LOG.debug("got all " + pojoClassName + "s ");
		BasicResource.LOG.trace(" = '" + ToStringBuilder.reflectionToString(objects) + "'");

		final String objectsJSON = serializeObject(objects);

		BasicResource.LOG.debug("return all " + pojoClassName + "s ");
		BasicResource.LOG.trace("'" + objectsJSON + "'");
		return buildResponse(objectsJSON);
	}

	/**
	 * This endpoint deletes an object identified by the uuid.
	 *
	 * @param uuid an object uuid
	 * @return status 204 if removal was successful, 404 if uuid not found, 409 if it couldn't be removed, or 500 if something else
	 * went wrong
	 * @throws DMPControllerException
	 */
	// @ApiOperation(value = "delete an object by uuid ", notes = "Returns a status.")
	// @DELETE
	// @Path("/{uuid}")
	@Timed
	public Response deleteObject(/* @PathParam("uuid") */final String uuid) throws DMPControllerException {

		BasicResource.LOG.debug("try to delete " + pojoClassName + " with uuid '" + uuid + "'");

		final POJOCLASSPERSISTENCESERVICE persistenceService = persistenceServiceProvider.get();
		POJOCLASS object = persistenceService.getObject(uuid);

		if (object == null) {

			BasicResource.LOG.debug("couldn't find " + pojoClassName + " '" + uuid + "'");
			return Response.status(Status.NOT_FOUND).build();
		}
		BasicResource.LOG.debug("got " + pojoClassName + " with uuid '" + uuid + "' ");
		BasicResource.LOG.trace(" = '" + ToStringBuilder.reflectionToString(object) + "'");

		persistenceService.deleteObject(uuid);

		object = persistenceService.getObject(uuid);

		if (object != null) {

			BasicResource.LOG.debug("couldn't delete " + pojoClassName + " '" + uuid + "'");
			return Response.status(Status.CONFLICT).build();
		}

		BasicResource.LOG.debug("deletion of " + pojoClassName + " with uuid '" + uuid + " was successful");
		return Response.status(Status.NO_CONTENT).build();
	}

	/**
	 * Creates the resource URI for the given object.
	 *
	 * @param object an object
	 * @return the resource URI for the given object
	 */
	protected URI createObjectURI(final POJOCLASS object) {

		final URI baseURI = uri.getRequestUri();

		final String idEncoded;

		try {

			idEncoded = URLEncoder.encode(object.getUuid(), "UTF-8");
		} catch (final UnsupportedEncodingException e) {

			BasicResource.LOG.debug("couldn't encode id", e);

			return null;
		}

		return URI.create(baseURI.toString() + "/" + idEncoded);
	}

	/**
	 * Prepares a given object with information from an object that was received via an API request.
	 *
	 * @param objectFromJSON an object that was received via an API request
	 * @param object         the given object
	 * @return the updated object
	 */
	protected abstract POJOCLASS prepareObjectForUpdate(final POJOCLASS objectFromJSON, final POJOCLASS object);

	/**
	 * Persists a new object that was received via an API request into the database.
	 *
	 * @param objectJSONString
	 * @return
	 * @throws DMPControllerException
	 */
	protected PROXYPOJOCLASS addObject(final String objectJSONString) throws DMPControllerException {

		// get the deserialisised object from the object JSON string

		final POJOCLASS objectFromJSON = deserializeObjectJSONString(objectJSONString);

		// create a new persistent object

		final POJOCLASSPERSISTENCESERVICE persistenceService = persistenceServiceProvider.get();
		final PROXYPOJOCLASS proxyObject;

		try {

			proxyObject = createObject(objectFromJSON, persistenceService);
		} catch (final DMPPersistenceException e) {

			BasicResource.LOG.debug("something went wrong while " + pojoClassName + " creation");

			throw new DMPControllerException("something went wrong while " + pojoClassName + " creation\n" + e.getMessage());
		}

		if (proxyObject == null) {

			throw new DMPControllerException("fresh " + pojoClassName + " shouldn't be null");
		}

		final POJOCLASS object = proxyObject.getObject();

		if (object == null) {

			throw new DMPControllerException("fresh " + pojoClassName + " shouldn't be null");
		}

		return proxyObject;
	}

	/**
	 * Persists an existing object that was received via an API request into the database.
	 *
	 * @param objectJSONString
	 * @param uuid
	 * @return
	 * @throws DMPControllerException
	 */
	protected PROXYPOJOCLASS refreshObject(final String objectJSONString, final POJOCLASS object, final String uuid)
			throws DMPControllerException {

		// get the deserialisised object from the object JSON string

		final POJOCLASS objectFromJSON = deserializeObjectJSONString(objectJSONString);

		final POJOCLASS preparedObject = prepareObjectForUpdate(objectFromJSON, object);

		return updateObject(preparedObject, uuid);
	}

	protected POJOCLASS retrieveObject(final String uuid, final String jsonObjectString) throws DMPControllerException {

		// get persistent object per uuid

		final POJOCLASSPERSISTENCESERVICE persistenceService = persistenceServiceProvider.get();
		final POJOCLASS object = persistenceService.getObject(uuid);

		if (object == null) {

			BasicResource.LOG.debug(pojoClassName + " for uuid '" + uuid + "' does not exist, i.e., it cannot be updated");

			return null;
		}

		BasicResource.LOG.debug("got " + pojoClassName + " with uuid '" + uuid + "' ");
		BasicResource.LOG.trace("= '" + ToStringBuilder.reflectionToString(object) + "'");

		return object;

	}

	protected PROXYPOJOCLASS updateObject(final POJOCLASS preparedObject, final String uuid) throws DMPControllerException {

		// update the persistent object in the DB

		final POJOCLASSPERSISTENCESERVICE persistenceService = persistenceServiceProvider.get();

		try {

			return persistenceService.updateObjectTransactional(preparedObject);
		} catch (final DMPPersistenceException e) {

			BasicResource.LOG.debug("something went wrong while updating " + pojoClassName + "  for uuid '" + uuid + "'");

			throw new DMPControllerException("something went wrong while updating" + pojoClassName + "  for uuid '" + uuid
					+ "'\n" + e.getMessage());
		}
	}

	protected POJOCLASS deserializeObjectJSONString(final String objectJSONString) throws DMPControllerException {

		POJOCLASS objectFromJSON = null;

		try {

			objectFromJSON = objectMapperProvider.get().readValue(objectJSONString, pojoClass);
		} catch (final JsonMappingException je) {

			throw new DMPJsonException("something went wrong while deserializing the " + pojoClassName + " JSON string", je);
		} catch (final IOException e) {

			BasicResource.LOG.debug("something went wrong while deserializing the " + pojoClassName + " JSON string");

			throw new DMPControllerException("something went wrong while deserializing the " + pojoClassName + " JSON string.\n" + e.getMessage());
		}

		if (objectFromJSON == null) {

			throw new DMPControllerException("deserialized " + pojoClassName + " is null");
		}

		return objectFromJSON;
	}

	protected String serializeObject(final Object object) throws DMPControllerException {

		String objectJSONString = null;

		try {

			objectJSONString = objectMapperProvider.get().writeValueAsString(object);
		} catch (final JsonProcessingException e) {

			BasicResource.LOG.debug("couldn't serialize enhanced " + pojoClassName + " JSON.");

			throw new DMPControllerException("couldn't serialize enhanced " + pojoClassName + " JSON.\n" + e.getMessage());
		}

		if (objectJSONString == null) {

			BasicResource.LOG.debug("couldn't serialize enhanced " + pojoClassName + " JSON correctly.");

			throw new DMPControllerException("couldn't serialize enhanced " + pojoClassName + " JSON correctly.\n");
		}

		return objectJSONString;
	}

	/**
	 * Creates and persists a new object into the database.
	 *
	 * @param objectFromJSON     the new object
	 * @param persistenceService the related persistence service
	 * @return the persisted object
	 * @throws DMPPersistenceException
	 */
	protected PROXYPOJOCLASS createObject(final POJOCLASS objectFromJSON, final POJOCLASSPERSISTENCESERVICE persistenceService)
			throws DMPPersistenceException {

		return persistenceService.createObjectTransactional(objectFromJSON);
	}
}
