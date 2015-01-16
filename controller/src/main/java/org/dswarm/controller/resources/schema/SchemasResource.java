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

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.BasicDMPResource;
import org.dswarm.controller.status.DMPStatus;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.dswarm.persistence.service.schema.SchemaService;

/**
 * A resource (controller service) for {@link Schema}s.
 *
 * @author tgaengler
 * @author jpolowinski
 */
@RequestScoped
@Api(value = "/schemas", description = "Operations about schemas")
@Path("schemas")
public class SchemasResource extends BasicDMPResource<SchemaService, ProxySchema, Schema> {

	private static final Logger LOG = LoggerFactory.getLogger(SchemasResource.class);

	private final Provider<AttributeService>                   attributeServiceProvider;
	private final Provider<AttributePathService>               attributePathServiceProvider;
	private final Provider<SchemaAttributePathInstanceService> schemaAttributePathInstanceServiceProvider;

	/**
	 * Creates a new resource (controller service) for {@link Schema}s with the provider of the schema persistence service, the
	 * object mapper and metrics registry.
	 *
	 * @param persistenceServiceProviderArg
	 * @param attributeServiceProviderArg
	 * @param attributePathServiceProviderArg
	 * @param schemaAttributePathInstanceServiceProviderArg
	 * @param objectMapperProviderArg                       an object mapper
	 * @param dmpStatusArg                                  a metrics registry
	 * @throws DMPControllerException
	 */
	@Inject
	public SchemasResource(final Provider<SchemaService> persistenceServiceProviderArg, final Provider<AttributeService> attributeServiceProviderArg,
			final Provider<AttributePathService> attributePathServiceProviderArg,
			final Provider<SchemaAttributePathInstanceService> schemaAttributePathInstanceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg, final DMPStatus dmpStatusArg)
			throws DMPControllerException {

		super(Schema.class, persistenceServiceProviderArg, objectMapperProviderArg, dmpStatusArg);

		attributeServiceProvider = attributeServiceProviderArg;
		attributePathServiceProvider = attributePathServiceProviderArg;
		schemaAttributePathInstanceServiceProvider = schemaAttributePathInstanceServiceProviderArg;
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
	public Response getObject(@ApiParam(value = "schema identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

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
	 * @param uuid             a schema identifier
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
			@ApiParam(value = "schema identifier", required = true) @PathParam("id") final String uuid) throws DMPControllerException {

		return super.updateObject(jsonObjectString, uuid);
	}

	/**
	 * This endpoint consumes an ordered list of attribute descriptions - names + URIs - (of an attribute path) as JSON (array)
	 * representation and creates an attribute path (incl. attributes) from them an updates the schema with this attribute path in
	 * the database.
	 *
	 * @param attributeDescriptionsJSONArrayString an ordered list of attribute descriptions - names + URIs - (of an attribute
	 *                                             path) as JSON (array) representation
	 * @param uuid                                 a schema identifier
	 * @return the updated schema as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create attribute path from the given attribute descriptions (names + URIs) and update schema with this attribute path",
			notes = "Returns an updated Schema object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "schema was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a schema for the given uuid"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Path("/{uuid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addAttributePath(
			@ApiParam(value = "an ordered list of attribute descriptions - names + URIs + (of an attribute path) as JSON (array)",
					required = true) final String attributeDescriptionsJSONArrayString,
			@ApiParam(value = "schema identifier", required = true) @PathParam("uuid") final String uuid) throws DMPControllerException {

		SchemasResource.LOG.debug("try to create attribute path from '" + attributeDescriptionsJSONArrayString + "' and add it to "
				+ pojoClassName + " with uuid '" + uuid + "'");

		final SchemaService persistenceService = persistenceServiceProvider.get();
		final Schema object = persistenceService.getObject(uuid);

		if (object == null) {

			SchemasResource.LOG.debug("couldn't find " + pojoClassName + " '" + uuid + "'");

			return Response.status(Status.NOT_FOUND).build();
		}

		SchemasResource.LOG.debug("got " + pojoClassName + " with uuid '" + uuid + "'");
		SchemasResource.LOG.trace(" = '" + ToStringBuilder.reflectionToString(object) + "'");

		final SchemaAttributePathInstance attributePath = createAttributePath(attributeDescriptionsJSONArrayString, uuid);

		object.addAttributePath(attributePath);

		final ProxySchema proxySchema = updateObject(object, uuid);

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

		final String updatedSchemaString = serializeObject(updatedSchema);

		return buildResponse(updatedSchemaString);
	}

	/**
	 * This endpoint consumes an attribute name and creates an attribute path (incl. new attribute) with help of the given
	 * attribute path (by id) and the freshly created attribute, and updates the schema with this attribute path in the database.
	 *
	 * @param schemaUuid                     a schema identifier
	 * @param attributePathUuid              a attribute path identifier
	 * @param attributeDescriptionJSONString the name of the attribute that should be created and added at the end of the given
	 *                                       attribute path
	 * @return the updated schema as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create attribute path from the given attribute names and update schema with this attribute path",
			notes = "Returns an updated Schema object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "schema was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a schema for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Path("/{schemauuid}/attributepaths/{attributepathuuid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addAttribute(@ApiParam(value = "schema identifier", required = true) @PathParam("schemauuid") final String schemaUuid,
			@ApiParam(value = "attribute path identifier", required = true) @PathParam("attributepathuuid") final String attributePathUuid,
			@ApiParam(value = "attribute description (name + URI)", required = true) final String attributeDescriptionJSONString)
			throws DMPControllerException {

		final SchemaService persistenceService = persistenceServiceProvider.get();
		final Schema object = persistenceService.getObject(schemaUuid);

		if (object == null) {

			SchemasResource.LOG.debug("couldn't find " + pojoClassName + " '" + schemaUuid + "'");

			return Response.status(Status.NOT_FOUND).build();
		}

		SchemasResource.LOG.debug("got " + pojoClassName + " with id '" + schemaUuid + "'");
		SchemasResource.LOG.trace(" = '" + ToStringBuilder.reflectionToString(object) + "'");

		final AttributePathService attributePathService = attributePathServiceProvider.get();

		final AttributePath baseAttributePath = getAttributePath(attributePathUuid, attributePathService);
		final List<Attribute> baseAttributes = baseAttributePath.getAttributePath();

		final ObjectNode attributeDescriptionJSON;

		try {

			attributeDescriptionJSON = objectMapperProvider.get().readValue(attributeDescriptionJSONString, ObjectNode.class);
		} catch (final IOException e) {

			throw new DMPControllerException("couldn't parse attribute description (name + URI)");
		}

		if (attributeDescriptionJSON == null) {

			throw new DMPControllerException("there is no attribute description");
		}

		final String schemaNamespaceURI = SchemaUtils.determineSchemaNamespaceURI(schemaUuid);
		final AttributeService attributeService = attributeServiceProvider.get();

		final Attribute newAttribute = createOrGetAttribute(attributeDescriptionJSON, schemaNamespaceURI, attributeService);

		final LinkedList<Attribute> attributes = Lists.newLinkedList(baseAttributes);
		attributes.add(newAttribute);

		final SchemaAttributePathInstance attributePath;

		final SchemaAttributePathInstanceService schemaAttributePathInstanceService = schemaAttributePathInstanceServiceProvider.get();

		try {

			attributePath = SchemaUtils.createSchemaAttributePathInstance(attributes, attributePathService, schemaAttributePathInstanceService);
		} catch (final DMPPersistenceException e) {

			final String message = "couldn't create schema attribute path instance";

			SchemasResource.LOG.error(message, e);

			throw new DMPControllerException(message);
		}

		object.addAttributePath(attributePath);

		final ProxySchema proxySchema = updateObject(object, schemaUuid);

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

		final String updatedSchemaString = serializeObject(updatedSchema);

		return buildResponse(updatedSchemaString);
	}

	/**
	 * This endpoint deletes a schema that matches the given id.
	 *
	 * @param id a schema identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 * went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "delete schema that matches the given id",
			notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "schema was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a schema for the given id"),
			@ApiResponse(code = 409, message = "schema couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "schema identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, attribute paths and record class and (optionally) content schema of the schema.
	 */
	@Override
	protected Schema prepareObjectForUpdate(final Schema objectFromJSON, final Schema object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		final Collection<SchemaAttributePathInstance> attributePaths = objectFromJSON.getAttributePaths();

		object.setAttributePaths(attributePaths);

		final Clasz recordClass = objectFromJSON.getRecordClass();

		object.setRecordClass(recordClass);

		final ContentSchema contentSchema = objectFromJSON.getContentSchema();

		object.setContentSchema(contentSchema);

		return object;
	}

	private SchemaAttributePathInstance createAttributePath(final String attributeDescriptionsJSONArrayString, final String uuid)
			throws DMPControllerException {

		if (attributeDescriptionsJSONArrayString == null) {

			final String message = "attribute descriptions JSON array string shouldn't be null";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		ArrayNode attributeDescriptionsJSONArray = null;

		try {

			attributeDescriptionsJSONArray = objectMapperProvider.get()
					.readValue(attributeDescriptionsJSONArrayString, ArrayNode.class);
		} catch (final IOException e) {

			final String message = "couldn't deserialize attribute descriptions JSON array";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		if (attributeDescriptionsJSONArray == null) {

			final String message = "attribute descriptions JSON array shouldn't be null";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		if (attributeDescriptionsJSONArray.size() <= 0) {

			final String message = "attribute descriptions JSON array shouldn't be empty";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final String schemaNamespaceURI = SchemaUtils.determineSchemaNamespaceURI(uuid);
		final AttributeService attributeService = attributeServiceProvider.get();
		final LinkedList<Attribute> attributes = Lists.newLinkedList();

		for (final JsonNode attributeDescriptionNode : attributeDescriptionsJSONArray) {

			final Attribute attribute = createOrGetAttribute(attributeDescriptionNode, schemaNamespaceURI, attributeService);

			attributes.add(attribute);
		}

		final AttributePathService attributePathService = attributePathServiceProvider.get();

		final SchemaAttributePathInstanceService schemaAttributePathInstanceService = schemaAttributePathInstanceServiceProvider.get();

		try {

			return SchemaUtils.createSchemaAttributePathInstance(attributes, attributePathService, schemaAttributePathInstanceService);
		} catch (final DMPPersistenceException e) {

			final String message = "couldn't create schema attribute path instance";

			SchemasResource.LOG.error(message, e);

			throw new DMPControllerException(message);
		}
	}

	private Attribute createOrGetAttribute(final String attributeName, final String attributeURI, final AttributeService attributeService)
			throws DMPControllerException {

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

	private AttributePath getAttributePath(final String attributePathUuid, final AttributePathService attributePathService)
			throws DMPControllerException {

		if (attributePathUuid == null) {

			final String message = "attribute path id should be set";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final AttributePath attributePath = attributePathService.getObject(attributePathUuid);

		if (attributePath == null) {

			final String message = "couldn't retrieve attribute path for '" + attributePathUuid + "' from db";

			SchemasResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		return attributePath;
	}

	private Attribute createOrGetAttribute(final JsonNode attributeDescriptionNode, final String schemaNamespaceURI,
			final AttributeService attributeService) throws DMPControllerException {

		final JsonNode attributeNameNode = attributeDescriptionNode.get("name");
		final JsonNode attributeURINode = attributeDescriptionNode.get("uri");

		final String attributeName;

		if (attributeNameNode != null) {

			attributeName = attributeNameNode.asText();
		} else {

			throw new DMPControllerException("there is no name for this attribute");
		}

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

		final String attributeURI;

		if (attributeURINode != null) {

			final String tempAttributeURI = attributeURINode.asText();

			boolean validURI = false;

			try {

				final URI attributeURIObject = URI.create(tempAttributeURI);

				if (attributeURIObject != null && attributeURIObject.getScheme() != null) {

					validURI = true;
				}
			} catch (final IllegalArgumentException e) {

				validURI = false;
			}

			if (!validURI) {

				throw new DMPControllerException("'" + tempAttributeURI + "' is not a valid URI");
			}

			attributeURI = tempAttributeURI;
		} else {

			attributeURI = SchemaUtils.mintAttributeURI(attributeName, schemaNamespaceURI);
		}

		return createOrGetAttribute(attributeName, attributeURI, attributeService);
	}

}
