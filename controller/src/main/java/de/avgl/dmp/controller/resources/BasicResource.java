package de.avgl.dmp.controller.resources;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.DMPJsonException;
import de.avgl.dmp.controller.resources.utils.BasicResourceUtils;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.DMPObject;
import de.avgl.dmp.persistence.service.BasicJPAService;

/**
 * A generic resource (controller service), whose concrete implementations can be derived with a given implementation of
 * {@link DMPObject} and the related identifier type. This service delivers basic controller layer functionality to create a new
 * object or retrieve existing ones.<br/>
 * TODO: implement update an existing object and delete existing objects
 * 
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE> the concrete persistence service of the resource that is related to the concrete POJO
 *            class
 * @param <POJOCLASS> the concrete POJO class of the resource
 * @param <POJOCLASSIDTYPE> the related identifier type of the concrete POJO class of the resource
 */
public abstract class BasicResource<POJOCLASSRESOURCEUTILS extends BasicResourceUtils<POJOCLASSPERSISTENCESERVICE, POJOCLASS, POJOCLASSIDTYPE>, POJOCLASSPERSISTENCESERVICE extends BasicJPAService<POJOCLASS, POJOCLASSIDTYPE>, POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, POJOCLASSIDTYPE> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(BasicResource.class);

	protected final POJOCLASSRESOURCEUTILS			pojoClassResourceUtils;

	/**
	 * The metrics registry.
	 */
	protected final DMPStatus						dmpStatus;

	/**
	 * The base URI of this resource.
	 */
	@Context
	UriInfo											uri;

	/**
	 * Creates a new resource (controller service) for the given concrete POJO class with the provider of the concrete persistence
	 * service, the object mapper and metrics registry.
	 * 
	 * @param clasz a concrete POJO class
	 * @param persistenceServiceProviderArg the concrete persistence service that is related to the concrete POJO class
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	protected BasicResource(final POJOCLASSRESOURCEUTILS pojoClassResourceUtilsArg, final DMPStatus dmpStatusArg) {

		pojoClassResourceUtils = pojoClassResourceUtilsArg;
		dmpStatus = dmpStatusArg;
	}

	/**
	 * Gets the concrete POJO class of this resource (controller service).
	 * 
	 * @return the concrete POJO class
	 */
	public Class<POJOCLASS> getClasz() {

		return pojoClassResourceUtils.getClasz();
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
	 * This endpoint returns an object of the type of the POJO class as JSON representation for the provided object id.
	 * 
	 * @param id an object id
	 * @return a JSON representation of an object of the type of the POJO class
	 */
	// @GET
	// @Path("/{id}")
	// @Produces(MediaType.APPLICATION_JSON)
	public Response getObject(/* @PathParam("id") */final POJOCLASSIDTYPE id) throws DMPControllerException {

		final Timer.Context context = dmpStatus.getSingleObject(pojoClassResourceUtils.getClaszName(), this.getClass());

		BasicResource.LOG.debug("try to get " + pojoClassResourceUtils.getClaszName() + " with id '" + id + "'");

		final POJOCLASSPERSISTENCESERVICE persistenceService = pojoClassResourceUtils.getPersistenceService();
		final POJOCLASS object = persistenceService.getObject(id);

		if (object == null) {

			BasicResource.LOG.debug("couldn't find " + pojoClassResourceUtils.getClaszName() + " '" + id + "'");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}
		BasicResource.LOG.debug("got " + pojoClassResourceUtils.getClaszName() + " with id '" + id + "' = '"
				+ ToStringBuilder.reflectionToString(object) + "'");

		final String objectJSON;
		try {

			objectJSON = pojoClassResourceUtils.getObjectMapper().writeValueAsString(object);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform " + pojoClassResourceUtils.getClaszName() + " to JSON string.\n" + e.getMessage());
		}

		BasicResource.LOG.debug("return " + pojoClassResourceUtils.getClaszName() + " with id '" + id + "' = '" + objectJSON + "'");

		dmpStatus.stop(context);
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
	public Response createObject(final String jsonObjectString) throws DMPControllerException {

		final Timer.Context context = dmpStatus.createNewObject(pojoClassResourceUtils.getClaszName(), this.getClass());

		BasicResource.LOG.debug("try to create new " + pojoClassResourceUtils.getClaszName());

		final POJOCLASS object = addObject(jsonObjectString);

		if (object == null) {

			BasicResource.LOG.debug("couldn't add " + pojoClassResourceUtils.getClaszName());

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't add " + pojoClassResourceUtils.getClaszName());
		}

		BasicResource.LOG.debug("added new " + pojoClassResourceUtils.getClaszName() + " = '" + ToStringBuilder.reflectionToString(object) + "'");

		final String objectJSON;

		try {

			objectJSON = pojoClassResourceUtils.getObjectMapper().writeValueAsString(object);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform " + pojoClassResourceUtils.getClaszName() + " to JSON string.\n" + e.getMessage());
		}

		final URI objectURI = createObjectURI(object);

		if (objectURI == null) {

			BasicResource.LOG.debug("something went wrong, while minting the URL of the new " + pojoClassResourceUtils.getClaszName());

			dmpStatus.stop(context);
			throw new DMPControllerException("something went wrong, while minting the URL of the new " + pojoClassResourceUtils.getClaszName());
		}

		BasicResource.LOG.debug("return new " + pojoClassResourceUtils.getClaszName() + " at '" + objectURI.toString() + "' with content '"
				+ objectJSON + "'");

		dmpStatus.stop(context);
		return Response.created(objectURI).entity(objectJSON).build();
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
	public Response getObjects() throws DMPControllerException {

		final Timer.Context context = dmpStatus.getAllObjects(pojoClassResourceUtils.getClaszName(), this.getClass());

		BasicResource.LOG.debug("try to get all " + pojoClassResourceUtils.getClaszName() + "s");

		final POJOCLASSPERSISTENCESERVICE persistenceService = pojoClassResourceUtils.getPersistenceService();

		final List<POJOCLASS> objects = persistenceService.getObjects();

		if (objects == null) {

			BasicResource.LOG.debug("couldn't find " + pojoClassResourceUtils.getClaszName() + "s");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		if (objects.isEmpty()) {

			BasicResource.LOG.debug("there are no " + pojoClassResourceUtils.getClaszName() + "s");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		BasicResource.LOG.debug("got all " + pojoClassResourceUtils.getClaszName() + "s = ' = '" + ToStringBuilder.reflectionToString(objects) + "'");

		final String objectsJSON;

		try {

			objectsJSON = pojoClassResourceUtils.getObjectMapper().writeValueAsString(objects);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform " + pojoClassResourceUtils.getClaszName() + "s list object to JSON string.\n"
					+ e.getMessage());
		}

		BasicResource.LOG.debug("return all " + pojoClassResourceUtils.getClaszName() + "s '" + objectsJSON + "'");

		dmpStatus.stop(context);
		return buildResponse(objectsJSON);
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

			idEncoded = URLEncoder.encode(object.getId().toString(), "UTF-8");
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
	 * @param object the given object
	 * @return the updated object
	 */
	protected abstract POJOCLASS prepareObjectForUpdate(final POJOCLASS objectFromJSON, final POJOCLASS object);

	/**
	 * Creates and persists a new object into the database.
	 * 
	 * @param objectFromJSON the new object
	 * @param persistenceService the related persistence service
	 * @return the persisted object
	 * @throws DMPPersistenceException
	 */
	protected POJOCLASS createObject(final POJOCLASS objectFromJSON, final POJOCLASSPERSISTENCESERVICE persistenceService)
			throws DMPPersistenceException {

		return persistenceService.createObject();
	}

	/**
	 * Persists a new object that was received via an API request into the database.
	 * 
	 * @param objectJSONString
	 * @return
	 * @throws DMPControllerException
	 */
	protected POJOCLASS addObject(final String objectJSONString) throws DMPControllerException {

		final String enhancedObjectJSONString = pojoClassResourceUtils.prepareObjectJSONString(objectJSONString);

		// get the deserialisised object from the enhanced JSON string

		final POJOCLASS objectFromJSON = pojoClassResourceUtils.deserializeObjectJSONString(enhancedObjectJSONString);

		// create a new persistent object

		final POJOCLASSPERSISTENCESERVICE persistenceService = pojoClassResourceUtils.getPersistenceService();
		final POJOCLASS object;

		try {

			object = createObject(objectFromJSON, persistenceService);
		} catch (final DMPPersistenceException e) {

			BasicResource.LOG.debug("something went wrong while " + pojoClassResourceUtils.getClaszName() + " creation");

			throw new DMPControllerException("something went wrong while " + pojoClassResourceUtils.getClaszName() + " creation\n" + e.getMessage());
		}

		if (object == null) {

			throw new DMPControllerException("fresh " + pojoClassResourceUtils.getClaszName() + " shouldn't be null");
		}

		final POJOCLASS preparedObject = prepareObjectForUpdate(objectFromJSON, object);

		// update the persistent object in the DB

		try {

			final POJOCLASS updatedObject = persistenceService.updateObjectTransactional(preparedObject);

			return updatedObject;
		} catch (final DMPPersistenceException e) {

			BasicResource.LOG.debug("something went wrong while " + pojoClassResourceUtils.getClaszName() + " updating");

			throw new DMPControllerException("something went wrong while " + pojoClassResourceUtils.getClaszName() + " updating\n" + e.getMessage());
		}
	}
}
