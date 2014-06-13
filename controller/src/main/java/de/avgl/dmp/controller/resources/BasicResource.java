package de.avgl.dmp.controller.resources;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.utils.BasicResourceUtils;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.DMPObject;
import de.avgl.dmp.persistence.model.proxy.ProxyDMPObject;
import de.avgl.dmp.persistence.model.proxy.RetrievalType;
import de.avgl.dmp.persistence.service.BasicJPAService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic resource (controller service), whose concrete implementations can be derived with a given implementation of
 * {@link DMPObject} and the related identifier type. This service delivers basic controller layer functionality to create a new
 * object or retrieve existing ones.<br/>
 * TODO: implement update an existing object and delete existing objects
 * 
 * @author tgaengler
 * @author fniederlein
 * @param <POJOCLASSPERSISTENCESERVICE> the concrete persistence service of the resource that is related to the concrete POJO
 *            class
 * @param <POJOCLASS> the concrete POJO class of the resource
 * @param <POJOCLASSIDTYPE> the related identifier type of the concrete POJO class of the resource
 */
public abstract class BasicResource<POJOCLASSRESOURCEUTILS extends BasicResourceUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS, POJOCLASSIDTYPE>, POJOCLASSPERSISTENCESERVICE extends BasicJPAService<PROXYPOJOCLASS, POJOCLASS, POJOCLASSIDTYPE>, PROXYPOJOCLASS extends ProxyDMPObject<POJOCLASS, POJOCLASSIDTYPE>, POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, POJOCLASSIDTYPE> {

	private static final Logger				LOG	= LoggerFactory.getLogger(BasicResource.class);

	protected final POJOCLASSRESOURCEUTILS	pojoClassResourceUtils;

	/**
	 * The metrics registry.
	 */
	protected final DMPStatus				dmpStatus;

	/**
	 * The base URI of this resource.
	 */
	@Context
	UriInfo									uri;

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
		BasicResource.LOG.debug("got " + pojoClassResourceUtils.getClaszName() + " with id '" + id + "'");
		BasicResource.LOG.trace(" = '" + ToStringBuilder.reflectionToString(object) + "'");

		final String objectJSON;
		try {

			objectJSON = pojoClassResourceUtils.getObjectMapper().writeValueAsString(object);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform " + pojoClassResourceUtils.getClaszName() + " to JSON string.\n" + e.getMessage());
		}

		BasicResource.LOG.debug("return " + pojoClassResourceUtils.getClaszName() + " with id '" + id + "'");
		BasicResource.LOG.trace(" = '" + objectJSON + "'");

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

		final PROXYPOJOCLASS proxyObject = addObject(jsonObjectString);

		if (proxyObject == null) {

			BasicResource.LOG.debug("couldn't add " + pojoClassResourceUtils.getClaszName());

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't add " + pojoClassResourceUtils.getClaszName());
		}

		final POJOCLASS object = proxyObject.getObject();

		if (object == null) {

			BasicResource.LOG.debug("couldn't add " + pojoClassResourceUtils.getClaszName());

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't add " + pojoClassResourceUtils.getClaszName());
		}

		BasicResource.LOG.debug("added new " + pojoClassResourceUtils.getClaszName() + " with id = '" + object.getId() + "' ");
		BasicResource.LOG.trace(" = '" + ToStringBuilder.reflectionToString(object) + "'");

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

		BasicResource.LOG.debug("return new " + pojoClassResourceUtils.getClaszName() + " at '" + objectURI.toString() + "'");
		BasicResource.LOG.trace("with content '" + objectJSON + "'");

		dmpStatus.stop(context);

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

				BasicResource.LOG.debug("something went wrong, while evaluating the retrieval type of the " + pojoClassResourceUtils.getClaszName());

				dmpStatus.stop(context);
				throw new DMPControllerException("something went wrong, while evaluating the retrieval type of the "
						+ pojoClassResourceUtils.getClaszName());
		}

		return responseBuilder.entity(objectJSON).build();
	}

	/**
	 * This endpoint consumes an object of the type of the POJO class as JSON representation and update this object in the
	 * database.
	 * 
	 * @param jsonObjectString a JSON representation of one object of the type of the POJO class
	 * @param id an object id
	 * @return the persisted object as JSON representation
	 * @throws DMPControllerException
	 */
	// @PUT
	// @Path("/{id}")
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(final String jsonObjectString, /* @PathParam("id") */final POJOCLASSIDTYPE id) throws DMPControllerException {

		final Timer.Context context = dmpStatus.updateSingleObject(pojoClassResourceUtils.getClaszName(), this.getClass());

		BasicResource.LOG.debug("try to update " + pojoClassResourceUtils.getClaszName() + " '" + id + "'");

		final POJOCLASS objectFromDB = retrieveObject(id, jsonObjectString);

		if (objectFromDB == null) {

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		final PROXYPOJOCLASS proxyObject = refreshObject(jsonObjectString, objectFromDB, id);

		if (proxyObject == null) {

			BasicResource.LOG.debug("couldn't update " + pojoClassResourceUtils.getClaszName() + " '" + id + "'");

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't update " + pojoClassResourceUtils.getClaszName() + " '" + id + "'");
		}

		final POJOCLASS object = proxyObject.getObject();

		if (object == null) {

			BasicResource.LOG.debug("couldn't update " + pojoClassResourceUtils.getClaszName() + " '" + id + "'");

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't update " + pojoClassResourceUtils.getClaszName() + " '" + id + "'");
		}

		BasicResource.LOG.debug("updated " + pojoClassResourceUtils.getClaszName() + " '" + id + "'");
		BasicResource.LOG.trace(" = '" + ToStringBuilder.reflectionToString(object) + "'");

		final String objectJSON;

		try {

			objectJSON = pojoClassResourceUtils.getObjectMapper().writeValueAsString(object);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform " + pojoClassResourceUtils.getClaszName() + " to JSON string.\n" + e.getMessage());
		}

		BasicResource.LOG.debug("return updated " + pojoClassResourceUtils.getClaszName() + " with id '" + object.getId() + "'");
		BasicResource.LOG.trace(" = '" + objectJSON + "'");

		dmpStatus.stop(context);

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

				BasicResource.LOG.debug("something went wrong, while evaluating the retrieval type of the " + pojoClassResourceUtils.getClaszName());

				dmpStatus.stop(context);
				throw new DMPControllerException("something went wrong, while evaluating the retrieval type of the "
						+ pojoClassResourceUtils.getClaszName());
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

		BasicResource.LOG.debug("got all " + pojoClassResourceUtils.getClaszName() + "s ");
		BasicResource.LOG.trace(" = '" + ToStringBuilder.reflectionToString(objects) + "'");

		final String objectsJSON;

		try {

			objectsJSON = pojoClassResourceUtils.getObjectMapper().writeValueAsString(objects);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform " + pojoClassResourceUtils.getClaszName() + "s list object to JSON string.\n"
					+ e.getMessage());
		}

		BasicResource.LOG.debug("return all " + pojoClassResourceUtils.getClaszName() + "s ");
		BasicResource.LOG.trace("'" + objectsJSON + "'");

		dmpStatus.stop(context);
		return buildResponse(objectsJSON);
	}

	/**
	 * This endpoint deletes an object identified by the id.
	 * 
	 * @param id an object id
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 *         went wrong
	 * @throws DMPControllerException
	 */
	// @ApiOperation(value = "delete an object by id ", notes = "Returns a status.")
	// @DELETE
	// @Path("/{id}")
	public Response deleteObject(/* @PathParam("id") */final POJOCLASSIDTYPE id) throws DMPControllerException {

		final Timer.Context context = dmpStatus.deleteObject(pojoClassResourceUtils.getClaszName(), this.getClass());

		BasicResource.LOG.debug("try to delete " + pojoClassResourceUtils.getClaszName() + " with id '" + id + "'");

		final POJOCLASSPERSISTENCESERVICE persistenceService = pojoClassResourceUtils.getPersistenceService();
		POJOCLASS object = persistenceService.getObject(id);

		if (object == null) {

			BasicResource.LOG.debug("couldn't find " + pojoClassResourceUtils.getClaszName() + " '" + id + "'");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}
		BasicResource.LOG.debug("got " + pojoClassResourceUtils.getClaszName() + " with id '" + id + "' ");
		BasicResource.LOG.trace(" = '" + ToStringBuilder.reflectionToString(object) + "'");

		persistenceService.deleteObject(id);

		object = persistenceService.getObject(id);

		if (object != null) {

			BasicResource.LOG.debug("couldn't delete " + pojoClassResourceUtils.getClaszName() + " '" + id + "'");

			dmpStatus.stop(context);
			return Response.status(Status.CONFLICT).build();
		}

		BasicResource.LOG.debug("deletion of " + pojoClassResourceUtils.getClaszName() + " with id '" + id + " was successful");

		dmpStatus.stop(context);
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
	 * Persists a new object that was received via an API request into the database.
	 * 
	 * @param objectJSONString
	 * @return
	 * @throws DMPControllerException
	 */
	protected PROXYPOJOCLASS addObject(final String objectJSONString) throws DMPControllerException {

		final String enhancedObjectJSONString = pojoClassResourceUtils.prepareObjectJSONString(objectJSONString);

		// get the deserialisised object from the enhanced JSON string

		final POJOCLASS objectFromJSON = pojoClassResourceUtils.deserializeObjectJSONString(enhancedObjectJSONString);

		// create a new persistent object

		final POJOCLASSPERSISTENCESERVICE persistenceService = pojoClassResourceUtils.getPersistenceService();
		final PROXYPOJOCLASS proxyObject;

		try {

			proxyObject = pojoClassResourceUtils.createObject(objectFromJSON, persistenceService);
		} catch (final DMPPersistenceException e) {

			BasicResource.LOG.debug("something went wrong while " + pojoClassResourceUtils.getClaszName() + " creation");

			throw new DMPControllerException("something went wrong while " + pojoClassResourceUtils.getClaszName() + " creation\n" + e.getMessage());
		}

		if (proxyObject == null) {

			throw new DMPControllerException("fresh " + pojoClassResourceUtils.getClaszName() + " shouldn't be null");
		}

		final POJOCLASS object = proxyObject.getObject();

		if (object == null) {

			throw new DMPControllerException("fresh " + pojoClassResourceUtils.getClaszName() + " shouldn't be null");
		}

		final POJOCLASS preparedObject = prepareObjectForUpdate(objectFromJSON, object);

		// update the persistent object in the DB

		try {

			final PROXYPOJOCLASS proxyUpdatedObject = persistenceService.updateObjectTransactional(preparedObject);

			if (proxyUpdatedObject == null) {

				throw new DMPControllerException("something went wrong while " + pojoClassResourceUtils.getClaszName() + " updating");
			}

			return pojoClassResourceUtils.getPersistenceService().createNewProxyObject(proxyUpdatedObject.getObject(), proxyObject.getType());
		} catch (final DMPPersistenceException e) {

			BasicResource.LOG.debug("something went wrong while " + pojoClassResourceUtils.getClaszName() + " updating");

			throw new DMPControllerException("something went wrong while " + pojoClassResourceUtils.getClaszName() + " updating\n" + e.getMessage());
		}
	}

	/**
	 * Persists an existing object that was received via an API request into the database.
	 * 
	 * @param objectJSONString
	 * @param id
	 * @return
	 * @throws DMPControllerException
	 */
	protected PROXYPOJOCLASS refreshObject(final String objectJSONString, final POJOCLASS object, final POJOCLASSIDTYPE id)
			throws DMPControllerException {

		// enhance object JSON as necessary

		final String enhancedObjectJSONString = pojoClassResourceUtils.prepareObjectJSONString(objectJSONString);

		// get the deserialisised object from the enhanced JSON string

		final POJOCLASS objectFromJSON = pojoClassResourceUtils.deserializeObjectJSONString(enhancedObjectJSONString);

		final POJOCLASS preparedObject = prepareObjectForUpdate(objectFromJSON, object);

		return updateObject(preparedObject, id);
	}

	protected POJOCLASS retrieveObject(final POJOCLASSIDTYPE id, final String jsonObjectString) throws DMPControllerException {

		// get persistent object per id

		final POJOCLASSPERSISTENCESERVICE persistenceService = pojoClassResourceUtils.getPersistenceService();
		final POJOCLASS object = persistenceService.getObject(id);

		if (object == null) {

			BasicResource.LOG.debug(pojoClassResourceUtils.getClaszName() + " for id '" + id + "' does not exist, i.e., it cannot be updated");

			return null;
		}

		BasicResource.LOG.debug("got " + pojoClassResourceUtils.getClaszName() + " with id '" + id + "' ");
		BasicResource.LOG.trace("= '" + ToStringBuilder.reflectionToString(object) + "'");

		return object;

	}

	protected PROXYPOJOCLASS updateObject(final POJOCLASS preparedObject, final POJOCLASSIDTYPE id) throws DMPControllerException {

		// update the persistent object in the DB

		final POJOCLASSPERSISTENCESERVICE persistenceService = pojoClassResourceUtils.getPersistenceService();

		try {

			final PROXYPOJOCLASS proxyPreparedObject = persistenceService.updateObjectTransactional(preparedObject);

			return proxyPreparedObject;
		} catch (final DMPPersistenceException e) {

			BasicResource.LOG.debug("something went wrong while updating " + pojoClassResourceUtils.getClaszName() + "  for id '" + id + "'");

			throw new DMPControllerException("something went wrong while updating" + pojoClassResourceUtils.getClaszName() + "  for id '" + id
					+ "'\n" + e.getMessage());
		}
	}
}
