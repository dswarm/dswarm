package de.avgl.dmp.controller.resources.schema;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.BasicDMPResource;
import de.avgl.dmp.controller.resources.schema.utils.AttributePathsResourceUtils;
import de.avgl.dmp.controller.resources.schema.utils.AttributesResourceUtils;
import de.avgl.dmp.controller.resources.schema.utils.SchemasResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttribute;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttributePath;
import de.avgl.dmp.persistence.model.schema.proxy.ProxySchema;
import de.avgl.dmp.persistence.model.schema.utils.SchemaUtils;
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.service.schema.AttributeService;
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
public class SchemasResource extends BasicDMPResource<SchemasResourceUtils, SchemaService, ProxySchema, Schema> {

	private static final Logger			LOG	= LoggerFactory.getLogger(SchemasResource.class);

	private final ResourceUtilsFactory	utilsFactory;
	private final ObjectMapper objectMapper;

	/**
	 * Creates a new resource (controller service) for {@link Schema}s with the provider of the schema persistence service, the
	 * object mapper and metrics registry.
	 *
	 * @param schemaServiceProviderArg the schema persistence service provider
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	@Inject
	public SchemasResource(final ResourceUtilsFactory utilsFactoryArg, final ObjectMapper objectMapperArg, final DMPStatus dmpStatusArg) throws DMPControllerException {

		super(utilsFactoryArg.reset().get(SchemasResourceUtils.class), dmpStatusArg);

		utilsFactory = utilsFactoryArg;
		objectMapper = objectMapperArg;
	}

	/**
	 * This endpoint returns a schema as JSON representation for the provided schema identifier.
	 *
	 * @param id a schema identifier
	 * @return a JSON representation of a schema
	 */
	@ApiOperation(value = "get the schema that matches the given id", notes = "Returns the Schema object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the schema (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find a schema for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
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
	@ApiResponses(value = { @ApiResponse(code = 201, message = "schema was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
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
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all available schemas (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any schema, i.e., there are no schemas available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * This endpoint consumes a schema as JSON representation and updates this schema in the database.
	 *
	 * @param jsonObjectString a JSON representation of one schema
	 * @param id a schema identifier
	 * @return the updated schema as JSON representation
	 * @throws DMPControllerException
	 */
	@Override
	@ApiOperation(value = "update schema with given id ", notes = "Returns an updated Schema object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "schema was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a schema for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "schema (as JSON)", required = true) final String jsonObjectString,
			@ApiParam(value = "schema identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.updateObject(jsonObjectString, id);
	}

	/**
	 * This endpoint consumes an ordered list of attribute names (of an attribute path) as JSON (array) representation and creates
	 * an attribute path (incl. attributes) from them an updates the schema with this attribute path in the database.
	 *
	 * @param attributeNamesJSONArrayString an ordered list of attribute names (of an attribute path) as JSON (array)
	 *            representation
	 * @param id a schema identifier
	 * @return the updated schema as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create attribute path from the given attribute names and update schema with this attribute path", notes = "Returns an updated Schema object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "schema was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a schema for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addAttributePath(
			@ApiParam(value = "an ordered list of attribute names (of an attribute path) as JSON (array)", required = true) final String attributeNamesJSONArrayString,
			@ApiParam(value = "schema identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		SchemasResource.LOG.debug("try to create attribute path from '" + attributeNamesJSONArrayString + "' and add it to "
				+ pojoClassResourceUtils.getClaszName() + " with id '" + id + "'");

		final SchemaService persistenceService = pojoClassResourceUtils.getPersistenceService();
		final Schema object = persistenceService.getObject(id);

		if (object == null) {

			SchemasResource.LOG.debug("couldn't find " + pojoClassResourceUtils.getClaszName() + " '" + id + "'");

			return Response.status(Status.NOT_FOUND).build();
		}

		SchemasResource.LOG.debug("got " + pojoClassResourceUtils.getClaszName() + " with id '" + id + "'");
		SchemasResource.LOG.trace(" = '" + ToStringBuilder.reflectionToString(object) + "'");

		final AttributePath attributePath = createAttributePath(attributeNamesJSONArrayString, id);

		object.addAttributePath(attributePath);

		final ProxySchema proxySchema = updateObject(object, id);

		if (proxySchema == null) {

			final String message = "couldn't retrieve update schema from db";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final Schema updatedSchema = proxySchema.getObject();

		if (updatedSchema == null) {

			final String message = "couldn't retrieve updated schema from db";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final String updatedSchemaString = pojoClassResourceUtils.serializeObject(updatedSchema);

		return buildResponse(updatedSchemaString);
	}

	/**
	 * This endpoint consumes an attribute name and creates an attribute path (incl. new attribute) with help of the given
	 * attribute path (by id) and the freshly created attribute, and updates the schema with this attribute path in the database.
	 *
	 * @param schemaId a schema identifier
	 * @param attributePathId a attribute path identifier
	 * @param attributeName the name of the attribute that should be created and added at the end of the given attribute path
	 * @return the updated schema as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create attribute path from the given attribute names and update schema with this attribute path", notes = "Returns an updated Schema object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "schema was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a schema for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Path("/{schemaid}/attributepaths/{attributepathid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addAttribute(@ApiParam(value = "schema identifier", required = true) @PathParam("schemaid") final Long schemaId,
			@ApiParam(value = "attribute path identifier", required = true) @PathParam("attributepathid") final Long attributePathId,
			@ApiParam(value = "attribute name", required = true) final String attributeNameJson)
			throws DMPControllerException, IOException {

		final SchemaService persistenceService = pojoClassResourceUtils.getPersistenceService();
		final Schema object = persistenceService.getObject(schemaId);

		if (object == null) {

			SchemasResource.LOG.debug("couldn't find " + pojoClassResourceUtils.getClaszName() + " '" + schemaId + "'");

			return Response.status(Status.NOT_FOUND).build();
		}

		SchemasResource.LOG.debug("got " + pojoClassResourceUtils.getClaszName() + " with id '" + schemaId + "'");
		SchemasResource.LOG.trace(" = '" + ToStringBuilder.reflectionToString(object) + "'");

		final AttributePathsResourceUtils attributePathResourceUtils = utilsFactory.get(AttributePathsResourceUtils.class);
		final AttributePathService attributePathService = attributePathResourceUtils.getPersistenceService();

		final AttributePath baseAttributePath = getAttributePath(attributePathId, attributePathService);
		final LinkedList<Attribute> baseAttributes = baseAttributePath.getAttributePath();

		final String attributeName = objectMapper.readTree(attributeNameJson).path("attribute_name").asText();
		final Attribute newAttribute = createOrGetAttribute(attributeName, schemaId);

		final LinkedList<Attribute> attributes = Lists.newLinkedList(baseAttributes);
		attributes.add(newAttribute);

		final AttributePath attributePath = createOrGetAttributePath(attributes, attributePathService);

		object.addAttributePath(attributePath);

		final ProxySchema proxySchema = updateObject(object, schemaId);

		if (proxySchema == null) {

			final String message = "couldn't retrieve update schema from db";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final Schema updatedSchema = proxySchema.getObject();

		if (updatedSchema == null) {

			final String message = "couldn't retrieve updated schema from db";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final String updatedSchemaString = pojoClassResourceUtils.serializeObject(updatedSchema);

		return buildResponse(updatedSchemaString);
	}

	/**
	 * This endpoint deletes a schema that matches the given id.
	 *
	 * @param id a schema identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 *         went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "delete schema that matches the given id", notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "schema was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a schema for the given id"),
			@ApiResponse(code = 409, message = "schema couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "schema identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, attribute paths and record class of the schema.
	 */
	@Override
	protected Schema prepareObjectForUpdate(final Schema objectFromJSON, final Schema object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		final Set<AttributePath> attributePaths = objectFromJSON.getAttributePaths();

		object.setAttributePaths(attributePaths);

		final Clasz recordClass = objectFromJSON.getRecordClass();

		object.setRecordClass(recordClass);

		return object;
	}

	private AttributePath createAttributePath(final String attributeNamesJSONArrayString, final Long id) throws DMPControllerException {

		if (attributeNamesJSONArrayString == null) {

			final String message = "attribute names JSON array string shouldn't be null";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		ArrayNode attributeNamesJSONArray = null;

		try {

			attributeNamesJSONArray = pojoClassResourceUtils.getObjectMapper().readValue(attributeNamesJSONArrayString, ArrayNode.class);
		} catch (final IOException e) {

			final String message = "couldn't deserialize attribute names JSON array";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		if (attributeNamesJSONArray == null) {

			final String message = "attribute names JSON array shouldn't be null";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		if (attributeNamesJSONArray.size() <= 0) {

			final String message = "attribute names JSON array shouldn't be empty";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final String schemaBaseURI = SchemaUtils.determineSchemaURI(id);
		final AttributesResourceUtils attributeResourceUtils = utilsFactory.get(AttributesResourceUtils.class);
		final AttributeService attributeService = attributeResourceUtils.getPersistenceService();
		final LinkedList<Attribute> attributes = Lists.newLinkedList();

		for (final JsonNode attributeNameNode : attributeNamesJSONArray) {

			final String attributeName = attributeNameNode.asText();
			final Attribute attribute = createOrGetAttribute(attributeName, schemaBaseURI, attributeService);

			attributes.add(attribute);
		}

		final AttributePathsResourceUtils attributePathResourceUtils = utilsFactory.get(AttributePathsResourceUtils.class);
		final AttributePathService attributePathService = attributePathResourceUtils.getPersistenceService();

		return createOrGetAttributePath(attributes, attributePathService);
	}

	private AttributePath createOrGetAttributePath(final LinkedList<Attribute> attributes, final AttributePathService attributePathService)
			throws DMPControllerException {

		ProxyAttributePath proxyAttributePath = null;

		try {

			proxyAttributePath = attributePathService.createOrGetObjectTransactional(attributes);
		} catch (final DMPPersistenceException e) {

			final String message = "couldn't create or get attribute path";

			SchemasResource.LOG.error(message, e);

			throw new DMPControllerException(message);
		}

		if (proxyAttributePath == null) {

			final String message = "couldn't retrieve attribute path from db";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final AttributePath attributePath = proxyAttributePath.getObject();

		if (attributePath == null) {

			final String message = "couldn't retrieve attribute path from db";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		return attributePath;
	}

	private Attribute createOrGetAttribute(final String attributeName, final Long schemaId) throws DMPControllerException {

		if (attributeName == null) {

			final String message = "attribute name does not exists";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		if (attributeName.trim().isEmpty()) {

			final String message = "attribute name is an empty string";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final String schemaBaseURI = SchemaUtils.determineSchemaURI(schemaId);
		final AttributesResourceUtils attributeResourceUtils = utilsFactory.get(AttributesResourceUtils.class);
		final AttributeService attributeService = attributeResourceUtils.getPersistenceService();

		return createOrGetAttribute(attributeName, schemaBaseURI, attributeService);
	}

	private Attribute createOrGetAttribute(final String attributeName, final String schemaBaseURI, final AttributeService attributeService)
			throws DMPControllerException {

		final String attributeURI = SchemaUtils.mintAttributeURI(attributeName, schemaBaseURI);

		ProxyAttribute proxyAttribute = null;

		try {

			proxyAttribute = attributeService.createOrGetObjectTransactional(attributeURI);
		} catch (final DMPPersistenceException e) {

			final String message = "couldn't create or get attribute for '" + attributeURI + "'";

			SchemasResource.LOG.error(message, e);

			throw new DMPControllerException(message);
		}

		if (proxyAttribute == null) {

			final String message = "couldn't retrieve attribute for '" + attributeURI + "' from db";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final Attribute attribute = proxyAttribute.getObject();

		if (attribute == null) {

			final String message = "couldn't retrieve attribute for '" + attributeURI + "' from db";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		proxyAttribute = null;
		attribute.setName(attributeName);

		try {

			proxyAttribute = attributeService.updateObjectTransactional(attribute);
		} catch (final DMPPersistenceException e) {

			final String message = "couldn't update attribute for '" + attributeURI + "'";

			SchemasResource.LOG.error(message, e);

			throw new DMPControllerException(message);
		}

		if (proxyAttribute == null) {

			final String message = "couldn't retrieve updated attribute for '" + attributeURI + "' from db";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final Attribute updatedAttribute = proxyAttribute.getObject();

		if (updatedAttribute == null) {

			final String message = "couldn't retrieve updated attribute for '" + attributeURI + "' from db";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		return updatedAttribute;
	}

	private AttributePath getAttributePath(final Long attributePathId, final AttributePathService attributePathService) throws DMPControllerException {

		if (attributePathId == null) {

			final String message = "attribute path id should be set";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final AttributePath attributePath = attributePathService.getObject(attributePathId);

		if (attributePath == null) {

			final String message = "couldn't retrieve attribute path for '" + attributePathId + "' from db";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		return attributePath;
	}

}
