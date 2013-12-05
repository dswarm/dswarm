package de.avgl.dmp.controller.resources;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.schema.SchemaService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/*
 code adapted from ConfigurationsResource
 */

@RequestScoped
@Path("schemas")
public class SchemasResource {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(SchemasResource.class);

	private final Provider<SchemaService> schemaServiceProvider;

	@Context
	UriInfo uri;

	@Inject
	public SchemasResource(Provider<SchemaService> configurationServiceProvider) {
		this.schemaServiceProvider = configurationServiceProvider;
	}

	private Response buildResponse(final String responseContent) {
		return Response.ok(responseContent).build();
	}

	/**
	 * this endpoint returns a schema as JSON representation for the provided schema id
	 *
	 * @param id a schema id
	 * @return jsonObjectString a JSON representation of the schema
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSchema(@PathParam("id") final Long id)
			throws DMPControllerException {

		LOG.debug("try to get schema with id '" + id + "'");
		final SchemaService schemaService = schemaServiceProvider.get();
		final Schema schema = schemaService.getObject(id);

		if (schema == null) {
			LOG.debug("couldn't find schema '" + id + "'");
			return Response.status(Status.NOT_FOUND).build();
		}
		LOG.debug("got schema with id '" + id + "' = '"
				+ ToStringBuilder.reflectionToString(schema) + "'");

		String schemaJSON = null;
		try {
			schemaJSON = DMPPersistenceUtil.getJSONObjectMapper()
					.writeValueAsString(schema);
		} catch (final JsonProcessingException e) {
			throw new DMPControllerException(
					"couldn't transform schema to JSON string.\n"
							+ e.getMessage());
		}

		LOG.debug("return schema with id '" + id + "' = '" + schemaJSON + "'");
		return buildResponse(schemaJSON);
	}
	
	/**
	 * this endpoint consumes a schema as JSON representation and persists this schema in the database
	 *
	 * @param jsonObjectString a JSON representation of one schema
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	//@Produces(MediaType.APPLICATION_JSON)
	public Response createSchema(final String jsonObjectString) throws DMPControllerException {

		LOG.debug("try to create new schema");

		final Schema schema = addSchema(jsonObjectString);

		if (schema == null) {
			LOG.debug("couldn't add schema ");
			throw new DMPControllerException("couldn't add schema");
		}
		LOG.debug("added new schema = '" + ToStringBuilder.reflectionToString(schema) + "'");

		String schemaJSON = null;

		try {
			schemaJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(schema);
		} catch (final JsonProcessingException e) {
			throw new DMPControllerException("couldn't transform schema to JSON string.\n" + e.getMessage());
		}

		final URI baseURI = uri.getRequestUri();
		final URI schemaURI = URI.create(baseURI.toString() + "/" + schema.getId());

		LOG.debug("return new schema at '" + schemaURI.toString() + "' with content '" + schemaJSON + "'");

		return Response.created(schemaURI).entity(schemaJSON).build();
	}

	private Schema addSchema(final String schemaJSONString) throws DMPControllerException {
	
		Schema schemaFromJSON = null;
	
		// get the deserialised schema from the JSON string
		
		try {
			schemaFromJSON = DMPPersistenceUtil.getJSONObjectMapper().readValue(schemaJSONString, Schema.class);
		} catch (final JsonParseException e) {
			LOG.debug("something went wrong while deserializing the schema JSON string");
			throw new DMPControllerException("something went wrong while deserializing the schema JSON string.\n" + e.getMessage());
		} catch (final JsonMappingException e) {
			LOG.debug("something went wrong while deserializing the schema JSON string");
			throw new DMPControllerException("something went wrong while deserializing the schema JSON string.\n" + e.getMessage());
		} catch (final IOException e) {
			LOG.debug("something went wrong while deserializing the schema JSON string");
			throw new DMPControllerException("something went wrong while deserializing the schema JSON string.\n" + e.getMessage());
		}
	
		if (schemaFromJSON == null) {
			throw new DMPControllerException("deserialized schema is null");
		}
	
		// create a new persistent schema
		
		final SchemaService schemaService = schemaServiceProvider.get();
		Schema schema = null;
	
		try {
			schema = schemaService.createObject();
		} catch (final DMPPersistenceException e) {
			LOG.debug("something went wrong while schema creation");
			throw new DMPControllerException("something went wrong while schema creation\n" + e.getMessage());
		}
	
		if (schema == null) {
			throw new DMPControllerException("fresh schema shouldn't be null");
		}
	
		// copy all settings from the deserialized schema to the persistent schema
		
		final String name = schemaFromJSON.getName();
	
		if (name != null) {
			schema.setName(name);
		}
	
		/* Schemas don't have a description for now
		final String description = schemaFromJSON.getDescription();
	
		if (description != null) {
			schema.setDescription(description);
		}
		*/
		
		// update the persistent schema in the DB
			
		try {
			schemaService.updateObjectTransactional(schema);
		} catch (final DMPPersistenceException e) {
			LOG.debug("something went wrong while schema updating");
			throw new DMPControllerException("something went wrong while schema updating\n" + e.getMessage());
		}
	
		return schema;
	}

	
	
	
}
