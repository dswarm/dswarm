package de.avgl.dmp.controller.resources.test;

import java.util.LinkedList;
import java.util.Map;

import org.junit.After;
import org.junit.Ignore;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.avgl.dmp.controller.resources.test.utils.AttributePathsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.AttributesResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.ClaszesResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.SchemasResourceTestUtils;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.schema.SchemaService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class SchemasResourceTest extends BasicResourceTest<SchemasResourceTestUtils, SchemaService, Schema, Long> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(SchemasResourceTest.class);

	private final AttributesResourceTestUtils		attributesResourceTestUtils;

	private final ClaszesResourceTestUtils			claszesResourceTestUtils;

	private final AttributePathsResourceTestUtils	attributePathsResourceTestUtils;

	final Map<Long, Attribute>						attributes		= Maps.newHashMap();

	final Map<Long, AttributePath>					attributePaths	= Maps.newLinkedHashMap();

	private Clasz									recordClass;

	public SchemasResourceTest() {

		super(Schema.class, SchemaService.class, "schemas", "schema.json", new SchemasResourceTestUtils());

		attributesResourceTestUtils = new AttributesResourceTestUtils();
		claszesResourceTestUtils = new ClaszesResourceTestUtils();
		attributePathsResourceTestUtils = new AttributePathsResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		for (int i = 1; i < 6; i++) {

			final String attributeJSONFileName = "attribute" + i + ".json";

			final Attribute actualAttribute = attributesResourceTestUtils.createObject(attributeJSONFileName);

			attributes.put(actualAttribute.getId(), actualAttribute);
		}

		recordClass = claszesResourceTestUtils.createObject("clasz.json");

		// prepare schema json for attribute path ids manipulation
		final ObjectNode objectJSON = objectMapper.readValue(objectJSONString, ObjectNode.class);

		for (int j = 1; j < 4; j++) {

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

	@After
	public void tearDown2() throws Exception {

		for (final AttributePath attributePath : attributePaths.values()) {

			attributePathsResourceTestUtils.deleteObject(attributePath);
		}

		for (final Attribute attribute : attributes.values()) {

			attributesResourceTestUtils.deleteObject(attribute);
		}

		claszesResourceTestUtils.deleteObject(recordClass);
	}
	
	@Ignore
	@Override
	public void testPUTObject() throws Exception {

		//super.testPUTObject();
		
		// TODO: [@fniederlein] implement test
	}
}
