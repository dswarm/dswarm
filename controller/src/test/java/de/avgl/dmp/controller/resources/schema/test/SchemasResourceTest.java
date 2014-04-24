package de.avgl.dmp.controller.resources.schema.test;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Assert;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.avgl.dmp.controller.resources.schema.test.utils.AttributePathsResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.AttributesResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.ClaszesResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.SchemasResourceTestUtils;
import de.avgl.dmp.controller.resources.test.BasicResourceTest;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.schema.proxy.ProxySchema;
import de.avgl.dmp.persistence.service.schema.SchemaService;
import de.avgl.dmp.persistence.service.schema.test.utils.SchemaServiceTestUtils;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class SchemasResourceTest extends
		BasicResourceTest<SchemasResourceTestUtils, SchemaServiceTestUtils, SchemaService, ProxySchema, Schema, Long> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(SchemasResourceTest.class);

	private final AttributesResourceTestUtils		attributesResourceTestUtils;

	private final ClaszesResourceTestUtils			claszesResourceTestUtils;

	private final AttributePathsResourceTestUtils	attributePathsResourceTestUtils;

	private final SchemasResourceTestUtils			schemasResourceTestUtils;

	final Map<Long, Attribute>						attributes		= Maps.newHashMap();

	final Map<Long, AttributePath>					attributePaths	= Maps.newLinkedHashMap();

	private Clasz									recordClass;

	private Clasz									recordClass2;

	public SchemasResourceTest() {

		super(Schema.class, SchemaService.class, "schemas", "schema.json", new SchemasResourceTestUtils());

		attributesResourceTestUtils = new AttributesResourceTestUtils();
		claszesResourceTestUtils = new ClaszesResourceTestUtils();
		attributePathsResourceTestUtils = new AttributePathsResourceTestUtils();
		schemasResourceTestUtils = new SchemasResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		for (int i = 3; i < 6; i++) {

			if(i == 4) {

				// exclude attributes from internal model schema (because they should already exist)

				continue;
			}

			final String attributeJSONFileName = "attribute" + i + ".json";

			final Attribute actualAttribute = attributesResourceTestUtils.createObject(attributeJSONFileName);

			attributes.put(actualAttribute.getId(), actualAttribute);
		}

		recordClass = claszesResourceTestUtils.createObject("clasz1.json");

		// prepare schema json for attribute path ids manipulation
		final ObjectNode objectJSON = objectMapper.readValue(objectJSONString, ObjectNode.class);

		for (int j = 1; j < 4; j++) {

			if(j == 2) {

				// exclude attribute paths from internal model schema (because they should already exist)

				continue;
			}

			final String attributePathJSONFileName = "attribute_path" + j + ".json";

			String attributePathJSONString = DMPPersistenceUtil.getResourceAsString(attributePathJSONFileName);
			final AttributePath attributePath = objectMapper.readValue(attributePathJSONString, AttributePath.class);

			final LinkedList<Attribute> attributes = attributePath.getAttributePath();
			final LinkedList<Attribute> newAttributes = Lists.newLinkedList();

			for (final Attribute attribute : attributes) {

				for (final Attribute newAttribute : this.attributes.values()) {

					if (attribute.getUri().equals(newAttribute.getUri())) {

						newAttributes.add(newAttribute);

						break;
					}
				}
			}

			attributePath.setAttributePath(newAttributes);

			attributePathJSONString = objectMapper.writeValueAsString(attributePath);
			final AttributePath expectedAttributePath = objectMapper.readValue(attributePathJSONString, AttributePath.class);
			final AttributePath actualAttributePath = attributePathsResourceTestUtils.createObject(attributePathJSONString, expectedAttributePath);

			attributePaths.put(actualAttributePath.getId(), actualAttributePath);
		}

		// manipulate attribute paths (incl. their attributes)
		final ArrayNode attributePathsArray = objectMapper.createArrayNode();

		for (final AttributePath attributePath : attributePaths.values()) {

			final String attributePathJSONString = objectMapper.writeValueAsString(attributePath);
			final ObjectNode attributePathJSON = objectMapper.readValue(attributePathJSONString, ObjectNode.class);

			attributePathsArray.add(attributePathJSON);
		}

		objectJSON.put("attribute_paths", attributePathsArray);

		// manipulate record class
		final String recordClassJSONString = objectMapper.writeValueAsString(recordClass);
		final ObjectNode recordClassJSON = objectMapper.readValue(recordClassJSONString, ObjectNode.class);

		objectJSON.put("record_class", recordClassJSON);

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

	@After
	public void tearDown2() throws Exception {

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

		firstAttributePath.addAttribute(attribute);

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

		Assert.assertNotNull("the schema JSON string shouldn't be null", updateSchemaJSONString);

		final Schema updateSchema = schemasResourceTestUtils.updateObject(updateSchemaJSONString, expectedSchema);

		recordClass2 = updateSchema.getRecordClass();

		Assert.assertEquals("persisted and updated clasz uri should be equal", updateSchema.getRecordClass().getUri(), biboBookId);
		Assert.assertEquals("persisted and updated clasz name should be equal", updateSchema.getRecordClass().getName(), biboBookName);
		Assert.assertEquals("persisted and updated schema name should be equal", updateSchema.getName(), updateSchemaNameString);

		return updateSchema;
	}
}
