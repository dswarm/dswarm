package de.avgl.dmp.controller.resources;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.google.inject.Provider;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.DMPObject;
import de.avgl.dmp.persistence.service.BasicJPAService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
public abstract class BasicResource<POJOCLASSPERSISTENCESERVICE extends BasicJPAService<POJOCLASS, POJOCLASSIDTYPE>, POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, POJOCLASSIDTYPE> {

	private static final org.apache.log4j.Logger		LOG	= org.apache.log4j.Logger.getLogger(BasicResource.class);

	protected final Class<POJOCLASS>					clasz;
	protected final String								className;

	private final Provider<POJOCLASSPERSISTENCESERVICE>	persistenceServiceProvider;

	private final DMPStatus								dmpStatus;

	private final ObjectMapper							objectMapper;

	@Context
	UriInfo												uri;

	public BasicResource(final Class<POJOCLASS> clasz, final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg,
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

	private Response buildResponse(final String responseContent) {

		return Response.ok(responseContent).build();
	}

	/**
	 * this endpoint returns an object of the type of the POJO class as JSON representation for the provided object id
	 * 
	 * @param id a attribute id
	 * @return jsonObjectString a JSON representation of an object of the type of the POJO class
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getObject(@PathParam("id") final POJOCLASSIDTYPE id) throws DMPControllerException {

		final Timer.Context context = dmpStatus.getSingleObject(className, this.getClass());

		LOG.debug("try to get " + className + " with id '" + id + "'");

		final POJOCLASSPERSISTENCESERVICE persistenceService = persistenceServiceProvider.get();
		final POJOCLASS object = persistenceService.getObject(id);

		if (object == null) {

			LOG.debug("couldn't find " + className + " '" + id + "'");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}
		LOG.debug("got " + className + " with id '" + id + "' = '" + ToStringBuilder.reflectionToString(object) + "'");

		String objectJSON = null;
		try {

			objectJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(object);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform " + className + " to JSON string.\n" + e.getMessage());
		}

		LOG.debug("return " + className + " with id '" + id + "' = '" + objectJSON + "'");

		dmpStatus.stop(context);
		return buildResponse(objectJSON);
	}

	/**
	 * this endpoint consumes an object of the type of the POJO class as JSON representation and persists this object in the
	 * database
	 * 
	 * @param jsonObjectString a JSON representation of one object of the type of the POJO class
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createObject(final String jsonObjectString) throws DMPControllerException {

		final Timer.Context context = dmpStatus.createNewObject(className, this.getClass());

		LOG.debug("try to create new " + className);

		final POJOCLASS object = addObject(jsonObjectString);

		if (object == null) {

			LOG.debug("couldn't add " + className);

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't add " + className);
		}

		LOG.debug("added new " + className + " = '" + ToStringBuilder.reflectionToString(object) + "'");

		String objectJSON = null;

		try {

			objectJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(object);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform " + className + " to JSON string.\n" + e.getMessage());
		}

		final URI objectURI = createObjectURI(object);
		
		if(objectURI == null) {
			
			LOG.debug("something went wrong, while minting the URL of the new " + className);
			
			dmpStatus.stop(context);
			throw new DMPControllerException("something went wrong, while minting the URL of the new " + className);
		}

		LOG.debug("return new " + className + " at '" + objectURI.toString() + "' with content '" + objectJSON + "'");

		dmpStatus.stop(context);
		return Response.created(objectURI).entity(objectJSON).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getObjects() throws DMPControllerException {

		final Timer.Context context = dmpStatus.getAllObjects(className, this.getClass());

		LOG.debug("try to get all " + className + "s");

		final POJOCLASSPERSISTENCESERVICE persistenceService = persistenceServiceProvider.get();

		final List<POJOCLASS> objects = persistenceService.getObjects();

		if (objects == null) {

			LOG.debug("couldn't find " + className + "s");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		if (objects.isEmpty()) {

			LOG.debug("there are no " + className + "s");

			dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		LOG.debug("got all " + className + "s = ' = '" + ToStringBuilder.reflectionToString(objects) + "'");

		String objectsJSON;

		try {

			objectsJSON = objectMapper.writeValueAsString(objects);
		} catch (final JsonProcessingException e) {

			dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform " + className + "s list object to JSON string.\n" + e.getMessage());
		}

		LOG.debug("return all " + className + "s '" + objectsJSON + "'");

		dmpStatus.stop(context);
		return buildResponse(objectsJSON);
	}

	@OPTIONS
	public Response getOptions() {

		return Response.ok().header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, OPTIONS, HEAD")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Origin, X-Requested-With, Content-Type").build();
	}

	@Path("/{id}")
	@OPTIONS
	public Response getObjectOptions() {

		return Response.ok().header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, OPTIONS, HEAD")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Origin, X-Requested-With, Content-Type").build();
	}

	protected URI createObjectURI(final POJOCLASS object) {

		final URI baseURI = uri.getRequestUri();
		
		String idEncoded = null;
		
		try {
			
			idEncoded = URLEncoder.encode(object.getId().toString(), "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			
			LOG.debug("couldn't encode id", e);
			
			return null;
		}

		final URI objectURI = URI.create(baseURI.toString() + "/" + idEncoded);

		return objectURI;
	}

	protected abstract POJOCLASS prepareObjectForUpdate(final POJOCLASS objectFromJSON, final POJOCLASS object);

	protected POJOCLASS createObject(final POJOCLASS objectFromJSON, final POJOCLASSPERSISTENCESERVICE persistenceService)
			throws DMPPersistenceException {

		return persistenceService.createObject();
	}

	private POJOCLASS addObject(final String objectJSONString) throws DMPControllerException {

		POJOCLASS objectFromJSON = null;

		// get the deserialisised object from the JSON string

		try {
			
			objectFromJSON = objectMapper.readValue(objectJSONString, clasz);
		} catch (final JsonParseException e) {
			
			LOG.debug("something went wrong while deserializing the " + className + " JSON string");
			
			throw new DMPControllerException("something went wrong while deserializing the " + className + " JSON string.\n" + e.getMessage());
		} catch (final JsonMappingException e) {
			
			LOG.debug("something went wrong while deserializing the " + className + " JSON string");
			
			throw new DMPControllerException("something went wrong while deserializing the " + className + " JSON string.\n" + e.getMessage());
		} catch (final IOException e) {
			
			LOG.debug("something went wrong while deserializing the " + className + " JSON string");
			
			throw new DMPControllerException("something went wrong while deserializing the " + className + " JSON string.\n" + e.getMessage());
		}

		if (objectFromJSON == null) {
			throw new DMPControllerException("deserialized " + className + " is null");
		}

		// create a new persistent object

		final POJOCLASSPERSISTENCESERVICE persistenceService = persistenceServiceProvider.get();
		POJOCLASS object = null;

		try {
			
			object = createObject(objectFromJSON, persistenceService);
		} catch (final DMPPersistenceException e) {
			
			LOG.debug("something went wrong while " + className + " creation");
			
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
			
			LOG.debug("something went wrong while " + className + " updating");
			
			throw new DMPControllerException("something went wrong while " + className + " updating\n" + e.getMessage());
		}

		return preparedObject;
	}
}
