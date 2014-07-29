package org.dswarm.controller.resources.schema.test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import org.dswarm.controller.resources.schema.test.utils.AttributePathsResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.AttributesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.ClaszesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.ContentSchemasResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.SchemasResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.service.schema.test.utils.SchemaServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class SchemasResourceTest extends
		BasicResourceTest<SchemasResourceTestUtils, SchemaServiceTestUtils, SchemaService, ProxySchema, Schema, Long> {

	private AttributesResourceTestUtils		attributesResourceTestUtils;

	private ClaszesResourceTestUtils		claszesResourceTestUtils;

	private AttributePathsResourceTestUtils	attributePathsResourceTestUtils;

	private SchemasResourceTestUtils		schemasResourceTestUtils;

	private ContentSchemasResourceTestUtils	contentSchemasResourceTestUtils;

	final Map<Long, Attribute>				attributes		= Maps.newHashMap();

	final Map<Long, AttributePath>			attributePaths	= Maps.newLinkedHashMap();

	private Clasz							recordClass;

	private Clasz							recordClass2;

	private ContentSchema					contentSchema;

	public SchemasResourceTest() {

		super(Schema.class, SchemaService.class, "schemas", "schema.json", new SchemasResourceTestUtils());
	}

	@Override
	protected void initObjects() {

		super.initObjects();

		pojoClassResourceTestUtils = new SchemasResourceTestUtils();
		attributesResourceTestUtils = new AttributesResourceTestUtils();
		claszesResourceTestUtils = new ClaszesResourceTestUtils();
		attributePathsResourceTestUtils = new AttributePathsResourceTestUtils();
		schemasResourceTestUtils = new SchemasResourceTestUtils();
		contentSchemasResourceTestUtils = new ContentSchemasResourceTestUtils();
	}

	private void resetObjectVars() {

		attributes.clear();
		attributePaths.clear();
		recordClass = null;
		contentSchema = null;
	}

	@Override
	public void prepare() throws Exception {

		restartServer();
		initObjects();
		resetObjectVars();

		// note: due to the exclusion of various attribute and attribute path (that already exist in the database) - the resulted
		// schema doesn't fully reflect the schema as it is present in the schema.json example

		super.prepare();

		for (int i = 1; i < 6; i++) {

			if (i == 2 || i == 4) {

				// exclude attributes from internal model schema (because they should already exist)

				continue;
			}

			final String attributeJSONFileName = "attribute" + i + ".json";

			attributesResourceTestUtils.prepareAttribute(attributeJSONFileName, attributes);
		}

		recordClass = claszesResourceTestUtils.createObject("clasz1.json");

		// prepare schema json for attribute path ids manipulation
		final ObjectNode objectJSON = objectMapper.readValue(objectJSONString, ObjectNode.class);

		for (int j = 1; j < 4; j++) {

			if (j == 2) {

				// exclude attribute paths from internal model schema (because they should already exist)

				continue;
			}

			final String attributePathJSONFileName = "attribute_path" + j + ".json";

			attributePathsResourceTestUtils.prepareAttributePath(attributePathJSONFileName, attributePaths, attributes);
		}

		// manipulate attribute paths (incl. their attributes)
		final ArrayNode schemaAttributePathsArray = objectMapper.createArrayNode();

		for (final AttributePath attributePath : attributePaths.values()) {

			final String attributePathJSONString = objectMapper.writeValueAsString(attributePath);
			final ObjectNode attributePathJSON = objectMapper.readValue(attributePathJSONString, ObjectNode.class);

			schemaAttributePathsArray.add(attributePathJSON);
		}

		objectJSON.put("attribute_paths", schemaAttributePathsArray);

		// START content schema

		final String contentSchemaJSONString = DMPPersistenceUtil.getResourceAsString("content_schema.json");
		final ObjectNode contentSchemaJSON = objectMapper.readValue(contentSchemaJSONString, ObjectNode.class);

		// manipulate key attribute paths (incl. their attributes)
		final ArrayNode keyAttributePathsArray = objectMapper.createArrayNode();

		for (final AttributePath attributePath : attributePaths.values()) {

			final String attributePathJSONString = objectMapper.writeValueAsString(attributePath);
			final ObjectNode attributePathJSON = objectMapper.readValue(attributePathJSONString, ObjectNode.class);

			keyAttributePathsArray.add(attributePathJSON);
		}

		contentSchemaJSON.put("key_attribute_paths", keyAttributePathsArray);

		final Attribute rdfValue = attributesResourceTestUtils.getObject((long) 43);
		attributes.put(rdfValue.getId(), rdfValue);
		final AttributePath valueAttributePath = attributePathsResourceTestUtils.prepareAttributePath("attribute_path8.json", attributePaths,
				attributes);

		// manipulate value attribute path
		final String valueAttributePathJSONString = objectMapper.writeValueAsString(valueAttributePath);
		final ObjectNode valueAttributePathJSON = objectMapper.readValue(valueAttributePathJSONString, ObjectNode.class);

		contentSchemaJSON.put("value_attribute_path", valueAttributePathJSON);

		final String finalContentSchemaJSONString = objectMapper.writeValueAsString(contentSchemaJSON);
		final ContentSchema expectedContentSchema = objectMapper.readValue(finalContentSchemaJSONString, ContentSchema.class);

		contentSchema = contentSchemasResourceTestUtils.createObject(finalContentSchemaJSONString, expectedContentSchema);

		// END content schema

		// manipulate record class
		final String recordClassJSONString = objectMapper.writeValueAsString(recordClass);
		final ObjectNode recordClassJSON = objectMapper.readValue(recordClassJSONString, ObjectNode.class);

		objectJSON.put("record_class", recordClassJSON);

		// manipulate content schema
		final String persistentContentSchemaJSONString = objectMapper.writeValueAsString(contentSchema);
		final ObjectNode persistentContentSchemaJSON = objectMapper.readValue(persistentContentSchemaJSONString, ObjectNode.class);

		objectJSON.put("content_schema", persistentContentSchemaJSON);

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(objectJSON);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);
	}

	@Override
	public void testPUTObject() throws Exception {

		super.testPUTObject();

		if (recordClass2 != null) {

			claszesResourceTestUtils.deleteObject(recordClass2);
		}
	}

	@Test
	public void testAddAttributePath() throws Exception {

		final Schema schema = createObjectInternal();

		final String schemaNamespaceURI = SchemaUtils.determineSchemaNamespaceURI(schema.getId());
		final String attributeName1 = "attribute one";
		final String attributeName2 = "attribute2";
		final String attributeName3 = "a3";
		final String attributeUri1 = SchemaUtils.mintAttributeURI(attributeName1, schemaNamespaceURI);
		final String attributeUri2 = SchemaUtils.mintAttributeURI(attributeName2, schemaNamespaceURI);
		final String attributeUri3 = SchemaUtils.mintAttributeURI(attributeName3, schemaNamespaceURI);

		final ArrayNode attributeDescriptionsJSONArray = objectMapper.createArrayNode();
		final ObjectNode attributeDescriptionJSON1 = createAttributeDescription(attributeName1, attributeUri1);
		final ObjectNode attributeDescriptionJSON2 = createAttributeDescription(attributeName2, attributeUri2);
		final ObjectNode attributeDescriptionJSON3 = createAttributeDescription(attributeName3, null);
		attributeDescriptionsJSONArray.add(attributeDescriptionJSON1);
		attributeDescriptionsJSONArray.add(attributeDescriptionJSON2);
		attributeDescriptionsJSONArray.add(attributeDescriptionJSON3);

		final String attributeNamesJSONArrayString = objectMapper.writeValueAsString(attributeDescriptionsJSONArray);

		final Response response = target().path("/" + schema.getId()).request(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(attributeNamesJSONArrayString));

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final Schema updatedSchema = objectMapper.readValue(responseString, Schema.class);

		final LinkedList<String> attributeURIs = Lists.newLinkedList();
		attributeURIs.add(attributeUri1);
		attributeURIs.add(attributeUri2);
		attributeURIs.add(attributeUri3);

		Assert.assertNotNull(updatedSchema);

		final Set<AttributePath> attributePaths = updatedSchema.getAttributePaths();

		Assert.assertNotNull(attributePaths);

		boolean foundAttributePath = false;

		for (final AttributePath attributePath : attributePaths) {

			final LinkedList<Attribute> attributes = attributePath.getAttributePath();

			Assert.assertNotNull(attributes);

			final Iterator<String> attributeURIsIter = attributeURIs.iterator();

			boolean match = false;

			for (final Attribute attribute : attributes) {

				Assert.assertNotNull(attribute.getName());

				if (!attributeURIsIter.hasNext()) {

					match = false;

					break;
				}

				final String attributeURI = attributeURIsIter.next();

				if (!attribute.getUri().equals(attributeURI)) {

					match = false;

					break;
				}

				match = true;
			}

			if (match && attributeURIs.size() == attributes.size()) {

				foundAttributePath = true;

				break;
			}
		}

		Assert.assertTrue(foundAttributePath);

		// prepare tear down

		for (final AttributePath attributePath : attributePaths) {

			this.attributePaths.put(attributePath.getId(), attributePath);

			final Set<Attribute> attributes = attributePath.getAttributes();

			if (attributes != null) {

				for (final Attribute attribute : attributes) {

					this.attributes.put(attribute.getId(), attribute);
				}
			}
		}

		cleanUpDB(updatedSchema);
	}

	@Test
	public void testAddAttributePath2() throws Exception {

		final Schema schema = createObjectInternal();

		final String schemaNamespaceURI = SchemaUtils.determineSchemaNamespaceURI(schema.getId());
		final String attributeName1 = "attribute one";
		final String attributeUri1 = SchemaUtils.mintAttributeURI(attributeName1, schemaNamespaceURI);

		final AttributePath baseAttributePath = schema.getAttributePaths().iterator().next();
		final Long baseAttributePathId = baseAttributePath.getId();

		final Map<String, String> jsonMap = Maps.newHashMap();
		jsonMap.put("name", attributeName1);
		jsonMap.put("uri", attributeUri1);
		final String payloadJson = objectMapper.writeValueAsString(jsonMap);

		final Response response = target().path("/" + schema.getId() + "/attributepaths/" + baseAttributePathId)
				.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(payloadJson, MediaType.APPLICATION_JSON_TYPE));

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final Schema updatedSchema = objectMapper.readValue(responseString, Schema.class);

		final LinkedList<String> attributeURIs = Lists.newLinkedList();

		for (final Attribute attribute : baseAttributePath.getAttributePath()) {

			attributeURIs.add(attribute.getUri());
		}

		attributeURIs.add(attributeUri1);

		Assert.assertNotNull(updatedSchema);

		final Set<AttributePath> attributePaths = updatedSchema.getAttributePaths();

		Assert.assertNotNull(attributePaths);

		boolean foundAttributePath = false;

		for (final AttributePath attributePath : attributePaths) {

			final LinkedList<Attribute> attributes = attributePath.getAttributePath();

			Assert.assertNotNull(attributes);

			final Iterator<String> attributeURIsIter = attributeURIs.iterator();

			boolean match = false;

			for (final Attribute attribute : attributes) {

				if (!attributeURIsIter.hasNext()) {

					match = false;

					break;
				}

				final String attributeURI = attributeURIsIter.next();

				if (!attribute.getUri().equals(attributeURI)) {

					match = false;

					break;
				}

				match = true;
			}

			if (match && attributeURIs.size() == attributes.size()) {

				foundAttributePath = true;

				break;
			}
		}

		Assert.assertTrue(foundAttributePath);

		// prepare tear down

		for (final AttributePath attributePath : attributePaths) {

			this.attributePaths.put(attributePath.getId(), attributePath);

			final Set<Attribute> attributes = attributePath.getAttributes();

			if (attributes != null) {

				for (final Attribute attribute : attributes) {

					this.attributes.put(attribute.getId(), attribute);
				}
			}
		}

		cleanUpDB(updatedSchema);
	}

	@After
	public void tearDown2() throws Exception {

		contentSchemasResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(contentSchema);

		for (final AttributePath attributePath : attributePaths.values()) {

			attributePathsResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attributePath);
		}

		for (final Attribute attribute : attributes.values()) {

			attributesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attribute);
		}

		claszesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(recordClass);
	}

	@Override
	protected Schema updateObject(final Schema persistedSchema) throws Exception {

		final Set<AttributePath> persistedAttributePaths = persistedSchema.getAttributePaths();
		final AttributePath firstAttributePath = persistedAttributePaths.iterator().next();

		final String attributeJSONString = DMPPersistenceUtil.getResourceAsString("attribute3.json");
		final Attribute expectedAttribute = objectMapper.readValue(attributeJSONString, Attribute.class);

		final Response response = attributesResourceTestUtils.executeCreateObject(attributeJSONString);

		// attribute4 already exists in the DB at this moment, hence 200 (instead of 201) will be returned
		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final Attribute attribute = objectMapper.readValue(responseString, Attribute.class);

		attributesResourceTestUtils.compareObjects(expectedAttribute, attribute);

		final LinkedList<Attribute> firstAttributePathAttributesList = firstAttributePath.getAttributePath();
		firstAttributePathAttributesList.add(attribute);

		final AttributePath newFirstAttributePath = attributePathsResourceTestUtils.getPersistenceServiceTestUtils().createAttributePath(
				firstAttributePathAttributesList);
		Assert.assertNotNull(newFirstAttributePath);
		attributePaths.put(newFirstAttributePath.getId(), newFirstAttributePath);
		persistedSchema.removeAttributePath(firstAttributePath);
		persistedSchema.addAttributePath(newFirstAttributePath);

		// clasz update (with a non-persistent class)
		final String biboBookId = "http://purl.org/ontology/bibo/Bookibook";
		final String biboBookName = "bookibook";
		final Clasz biboBook = new Clasz(biboBookId, biboBookName);
		persistedSchema.setRecordClass(biboBook);

		String updateSchemaJSONString = objectMapper.writeValueAsString(persistedSchema);
		final ObjectNode updateSchemaJSON = objectMapper.readValue(updateSchemaJSONString, ObjectNode.class);

		// schema name update
		final String updateSchemaNameString = persistedSchema.getName() + " update";
		updateSchemaJSON.put("name", updateSchemaNameString);

		updateSchemaJSONString = objectMapper.writeValueAsString(updateSchemaJSON);

		final Schema expectedSchema = objectMapper.readValue(updateSchemaJSONString, Schema.class);
		expectedSchema.setContentSchema(null);

		Assert.assertNotNull("the schema JSON string shouldn't be null", updateSchemaJSONString);

		final Schema updateSchema = schemasResourceTestUtils.updateObject(updateSchemaJSONString, expectedSchema);

		recordClass2 = updateSchema.getRecordClass();

		Assert.assertEquals("persisted and updated clasz uri should be equal", updateSchema.getRecordClass().getUri(), biboBookId);
		Assert.assertEquals("persisted and updated clasz name should be equal", updateSchema.getRecordClass().getName(), biboBookName);
		Assert.assertEquals("persisted and updated schema name should be equal", updateSchema.getName(), updateSchemaNameString);

		return updateSchema;
	}

	private ObjectNode createAttributeDescription(final String name, final String uri) {

		final ObjectNode attributeDescriptionJSON = objectMapper.createObjectNode();
		attributeDescriptionJSON.put("name", name);

		if (uri != null) {

			attributeDescriptionJSON.put("uri", uri);
		}

		return attributeDescriptionJSON;
	}
}
