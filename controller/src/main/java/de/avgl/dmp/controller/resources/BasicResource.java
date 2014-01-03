package de.avgl.dmp.controller.resources;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.DMPJsonException;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.DMPObject;
import de.avgl.dmp.persistence.service.BasicJPAService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
public abstract class BasicResource<POJOCLASSPERSISTENCESERVICE extends BasicJPAService<POJOCLASS, POJOCLASSIDTYPE>, POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, POJOCLASSIDTYPE> {

	private static final org.apache.log4j.Logger			LOG	= org.apache.log4j.Logger.getLogger(BasicResource.class);

	protected final Class<POJOCLASS>						clasz;
	protected final String									className;

	protected final Provider<POJOCLASSPERSISTENCESERVICE>	persistenceServiceProvider;

	protected final DMPStatus								dmpStatus;

	protected final ObjectMapper							objectMapper;

	@Context
	UriInfo													uri;

	protected BasicResource(final Class<POJOCLASS> clasz, final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg,
			final ObjectMapper objectMapperArg, final DMPStatus dmpStatusArg) {

		this.clasz = clasz;
		this.className = clasz.getSimpleName();

		persistenceServiceProvider = persistenceServiceProviderArg;

		dmpStatus = dmpStatusArg;
		objectMapper = objectMapperArg;
	}

	public Class<POJOCLASS> getClasz() {

		return clasz;
	}

	protected Response buildResponse(final String responseContent) {

		return Response.ok(responseContent).build();
	}

	/**
	 * this endpoint returns an object of the type of the POJO class as JSON representation for the provided object id
	 * 
	 * @param id a attribute id
	 * @return jsonObjectString a JSON representation of an object of the type of the POJO class
	 */
	// @GET
	// @Path("/{id}")
	// @Produces(MediaType.APPLICATION_JSON)
	public Response getObject(/* @PathParam("id") */final POJOCLASSIDTYPE id) throws DMPControllerException {

		final Timer.Context context = dmpStatus.getSingleObject(className, this.getClass());

		BasicResource.LOG.debug("try to get " + className + " with id '" + id + "'");

		final POJOCLASSPERSISTENCESERVICE persistenceService = persistenceServiceProvider.get();
		final POJOCLASS object = persistenceService.getObject(id);

		if (object == null) {

			BasicResource.LOG.debug("couldn't find " + className + " '" + id + "'");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}
		BasicResource.LOG.debug("got " + className + " with id '" + id + "' = '" + ToStringBuilder.reflectionToString(object) + "'");

		final String objectJSON;
		try {

			objectJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(object);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform " + className + " to JSON string.\n" + e.getMessage());
		}

		BasicResource.LOG.debug("return " + className + " with id '" + id + "' = '" + objectJSON + "'");

		dmpStatus.stop(context);
		return buildResponse(objectJSON);
	}

	/**
	 * this endpoint consumes an object of the type of the POJO class as JSON representation and persists this object in the
	 * database
	 * 
	 * @param jsonObjectString a JSON representation of one object of the type of the POJO class
	 */
	// @POST
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	public Response createObject(final String jsonObjectString) throws DMPControllerException {

		final Timer.Context context = dmpStatus.createNewObject(className, this.getClass());

		BasicResource.LOG.debug("try to create new " + className);

		final POJOCLASS object = addObject(jsonObjectString);

		if (object == null) {

			BasicResource.LOG.debug("couldn't add " + className);

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't add " + className);
		}

		BasicResource.LOG.debug("added new " + className + " = '" + ToStringBuilder.reflectionToString(object) + "'");

		final String objectJSON;

		try {

			objectJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(object);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform " + className + " to JSON string.\n" + e.getMessage());
		}

		final URI objectURI = createObjectURI(object);

		if (objectURI == null) {

			BasicResource.LOG.debug("something went wrong, while minting the URL of the new " + className);

			dmpStatus.stop(context);
			throw new DMPControllerException("something went wrong, while minting the URL of the new " + className);
		}

		BasicResource.LOG.debug("return new " + className + " at '" + objectURI.toString() + "' with content '" + objectJSON + "'");

		dmpStatus.stop(context);
		return Response.created(objectURI).entity(objectJSON).build();
	}

	// @ApiOperation(value = "get all objects ", notes = "Returns a list objects.")
	// @GET
	// @Produces(MediaType.APPLICATION_JSON)
	public Response getObjects() throws DMPControllerException {

		final Timer.Context context = dmpStatus.getAllObjects(className, this.getClass());

		BasicResource.LOG.debug("try to get all " + className + "s");

		final POJOCLASSPERSISTENCESERVICE persistenceService = persistenceServiceProvider.get();

		final List<POJOCLASS> objects = persistenceService.getObjects();

		if (objects == null) {

			BasicResource.LOG.debug("couldn't find " + className + "s");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		if (objects.isEmpty()) {

			BasicResource.LOG.debug("there are no " + className + "s");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		BasicResource.LOG.debug("got all " + className + "s = ' = '" + ToStringBuilder.reflectionToString(objects) + "'");

		final String objectsJSON;

		try {

			objectsJSON = objectMapper.writeValueAsString(objects);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform " + className + "s list object to JSON string.\n" + e.getMessage());
		}

		BasicResource.LOG.debug("return all " + className + "s '" + objectsJSON + "'");

		dmpStatus.stop(context);
		return buildResponse(objectsJSON);
	}

	public URI createObjectURI(final POJOCLASS object) {

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

	protected abstract POJOCLASS prepareObjectForUpdate(final POJOCLASS objectFromJSON, final POJOCLASS object);

	protected POJOCLASS createObject(final POJOCLASS objectFromJSON, final POJOCLASSPERSISTENCESERVICE persistenceService)
			throws DMPPersistenceException {

		return persistenceService.createObject();
	}

	protected POJOCLASS addObject(final String objectJSONString) throws DMPControllerException {

		final POJOCLASS objectFromJSON;

		// get the deserialisised object from the JSON string

		try {

			objectFromJSON = objectMapper.readValue(objectJSONString, clasz);
		} catch (final JsonMappingException je) {

			throw new DMPJsonException("something went wrong while deserializing the " + className + " JSON string", je);
		} catch (final IOException e) {

			BasicResource.LOG.debug("something went wrong while deserializing the " + className + " JSON string");

			throw new DMPControllerException("something went wrong while deserializing the " + className + " JSON string.\n" + e.getMessage());
		}

		if (objectFromJSON == null) {
			throw new DMPControllerException("deserialized " + className + " is null");
		}

		// create a new persistent object

		final POJOCLASSPERSISTENCESERVICE persistenceService = persistenceServiceProvider.get();
		final POJOCLASS object;

		try {

			object = createObject(objectFromJSON, persistenceService);
		} catch (final DMPPersistenceException e) {

			BasicResource.LOG.debug("something went wrong while " + className + " creation");

			throw new DMPControllerException("something went wrong while " + className + " creation\n" + e.getMessage());
		}

		if (object == null) {

			throw new DMPControllerException("fresh " + className + " shouldn't be null");
		}

		final POJOCLASS preparedObject = prepareObjectForUpdate(objectFromJSON, object);

		// update the persistent object in the DB

		try {

			persistenceService.updateObjectTransactional(preparedObject);
		} catch (final DMPPersistenceException e) {

			BasicResource.LOG.debug("something went wrong while " + className + " updating");

			throw new DMPControllerException("something went wrong while " + className + " updating\n" + e.getMessage());
		}

		return preparedObject;
	}
}
