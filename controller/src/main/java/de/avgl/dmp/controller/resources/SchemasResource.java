package de.avgl.dmp.controller.resources;

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
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.schema.SchemaService;

@RequestScoped
@Api(value = "/schemas", description = "Operations about schemas")
@Path("schemas")
public class SchemasResource extends BasicResource<SchemaService, Schema, Long> {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(SchemasResource.class);

	@Inject
	public SchemasResource(final Provider<SchemaService> schemaServiceProviderArg, final ObjectMapper objectMapper,
			final DMPStatus dmpStatus) {

		super(Schema.class, schemaServiceProviderArg, objectMapper, dmpStatus);
	}

	@ApiOperation(value = "get the schema that matches the given id", notes = "Returns the Schema object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "schema identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.getObject(id);
	}
	
	@ApiOperation(value = "create a new schema", notes = "Returns a new Schema object.", response = Schema.class)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "schema identifier", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/* 
	//  not requested so far:
	
	@ApiOperation(value = "get all schemas ", notes = "Returns a list of Schema objects.")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}
	*/
	
	@Override
	protected Schema prepareObjectForUpdate(final Schema objectFromJSON, final Schema object) {
		final String name = objectFromJSON.getName();

		if (name != null) {

			object.setName(name);
		}

		/*
		final String description = objectFromJSON.getDescription();

		if (description != null) {

			object.setDescription(description);
		} */

		return object;

	}
	
}
