package de.avgl.dmp.controller.resources.test;

import java.util.LinkedList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.controller.resources.test.utils.AttributePathsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.AttributesResourceTestUtils;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class AttributePathsResourceTest extends BasicResourceTest<AttributePathsResourceTestUtils, AttributePathService, AttributePath, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributePathsResourceTest.class);

	private final AttributesResourceTestUtils		attributeResourceTestUtils;

	private Attribute								actualAttribute1;

	private Attribute								actualAttribute2;

	public AttributePathsResourceTest() {

		super(AttributePath.class, AttributePathService.class, "attributepaths", "attribute_path.json", new AttributePathsResourceTestUtils());

		attributeResourceTestUtils = new AttributesResourceTestUtils();
	
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		actualAttribute1 = attributeResourceTestUtils.createObject("attribute1.json");
		actualAttribute2 = attributeResourceTestUtils.createObject("attribute2.json");

		// manipulate attribute path attributes
		String attributePathJSONString = DMPPersistenceUtil.getResourceAsString("attribute_path.json");
		final ObjectNode attributePathJSON = objectMapper.readValue(attributePathJSONString, ObjectNode.class);

		final ArrayNode attributessArray = objectMapper.createArrayNode();

		final String attribute1JSONString = objectMapper.writeValueAsString(actualAttribute1);
		final ObjectNode attribute1JSON = objectMapper.readValue(attribute1JSONString, ObjectNode.class);

		final String attribute2JSONString = objectMapper.writeValueAsString(actualAttribute2);
		final ObjectNode attribute2JSON = objectMapper.readValue(attribute2JSONString, ObjectNode.class);

		attributessArray.add(attribute1JSON);
		attributessArray.add(attribute2JSON);

		attributePathJSON.put("attributes", attributessArray);

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(attributePathJSON);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);
	}
	
	@Test
	public void testUniquenessOfAttributePaths() {
		
		LOG.debug("start attribute paths uniqueness test");

		AttributePath attributePath1 = null;

		try {
			
			attributePath1 = pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
		} catch (Exception e) {
			
			LOG.error("coudln't create attribute path 1 for uniqueness test");
			
			Assert.assertTrue(false);
		}
		
		Assert.assertNotNull("attribute path 1 shouldn't be null in uniqueness test", attributePath1);

		AttributePath attributePath2 = null;

		try {
			
			attributePath2 = pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
		} catch (Exception e) {
			
			LOG.error("couldn't create attribute path 2 for uniqueness test");
			
			Assert.assertTrue(false);
		}
		
		Assert.assertNotNull("attribute path 2 shouldn't be null in uniqueness test", attributePath2);
		
		Assert.assertEquals("the attribute paths should be equal", attributePath1, attributePath2);
		
		cleanUpDB(attributePath1);
		
		LOG.debug("end attribute paths uniqueness test");
	}

	@Override
	public void testPUTObject() throws Exception {

		LOG.debug("start attribute path update test");

		AttributePath attributePath = null;

		try {
			
			attributePath = pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
		} catch (Exception e) {
			
			LOG.error("coudln't create attribute path for update test");
			
			Assert.assertTrue(false);
		}
		
		Assert.assertNotNull("attribute path shouldn't be null in update test", attributePath);
		
		Attribute actualAttribute3 = attributeResourceTestUtils.createObject("attribute3.json");
		
		attributePath.addAttribute(actualAttribute3);
		
		String attributePathJSONString = objectMapper.writeValueAsString(attributePath);
		
		AttributePath updateAttributePath = pojoClassResourceTestUtils.updateObject(attributePathJSONString, attributePath);
		
		Assert.assertEquals("the persisted attribute path shoud be equal to the modified attribute path for update", updateAttributePath, attributePath);
		Assert.assertEquals("number of attribute elements in attribute path should be equal", updateAttributePath.getAttributePath().size(), attributePath.getAttributePath().size());
				
		cleanUpDB(attributePath);
		
		LOG.debug("end attribute update test");
	}
	
	@After
	public void tearDown2() throws Exception {

		attributeResourceTestUtils.deleteObject(actualAttribute1);
		attributeResourceTestUtils.deleteObject(actualAttribute2);
	}
}
