package de.avgl.dmp.controller.resources.schema;

import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.BasicDMPResource;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.schema.SchemaService;

/**
 * A resource (controller service) for {@link Schema}s.
 * 
 * @author tgaengler
 * @author jpolowinski
 */
@RequestScoped
@Api(value = "/schemas", description = "Operations about schemas")
@Path("schemas")
public class SchemasResource extends BasicDMPResource<SchemaService, Schema> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(SchemasResource.class);

	/**
	 * Creates a new resource (controller service) for {@link Schema}s with the provider of the schema persistence service, the
	 * object mapper and metrics registry.
	 * 
	 * @param schemaServiceProviderArg the schema persistence service provider
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	@Inject
	public SchemasResource(final Provider<SchemaService> schemaServiceProviderArg, final ObjectMapper objectMapper, final DMPStatus dmpStatus) {

		super(Schema.class, schemaServiceProviderArg, objectMapper, dmpStatus);
	}

	/**
	 * This endpoint returns a schema as JSON representation for the provided schema identifier.
	 * 
	 * @param id a schema identifier
	 * @return a JSON representation of a schema
	 */
	@ApiOperation(value = "get the schema that matches the given id", notes = "Returns the Schema object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "schema identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {
		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a schema as JSON representation and persists this schema in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one schema
	 * @return the persisted schema as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new schema", notes = "Returns a new Schema object.", response = Schema.class)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "schema (as JSON)", required = true) final String jsonObjectString) throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all schemas as JSON representation.
	 * 
	 * @return a list of all schemas as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all schemas ", notes = "Returns a list of Schema objects.")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, attribute paths and record class of the schema.
	 */
	@Override
	protected Schema prepareObjectForUpdate(final Schema objectFromJSON, final Schema object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		final Set<AttributePath> attributePaths = objectFromJSON.getAttributePaths();

		if (attributePaths != null && !attributePaths.isEmpty()) {

			object.setAttributePaths(attributePaths);
		}

		final Clasz recordClass = objectFromJSON.getRecordClass();

		if (recordClass != null) {

			object.setRecordClass(recordClass);
		}

		return object;
	}

}
