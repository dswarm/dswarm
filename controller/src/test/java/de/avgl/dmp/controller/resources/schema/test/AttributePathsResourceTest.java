package de.avgl.dmp.controller.resources.schema.test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.controller.resources.schema.test.utils.AttributePathsResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.AttributesResourceTestUtils;
import de.avgl.dmp.controller.resources.test.BasicResourceTest;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttributePath;
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.service.schema.test.utils.AttributePathServiceTestUtils;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributePathsResourceTest
		extends
		BasicResourceTest<AttributePathsResourceTestUtils, AttributePathServiceTestUtils, AttributePathService, ProxyAttributePath, AttributePath, Long> {

	private static final Logger LOG = LoggerFactory.getLogger(AttributePathsResourceTest.class);

	private final AttributesResourceTestUtils attributeResourceTestUtils;

	private Attribute actualAttribute1;

	private Attribute actualAttribute2;

	public AttributePathsResourceTest() {

		super(AttributePath.class, AttributePathService.class, "attributepaths", "attribute_path.json", new AttributePathsResourceTestUtils());

		attributeResourceTestUtils = new AttributesResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		actualAttribute1 = attributeResourceTestUtils.createObject("attribute6.json");
		actualAttribute2 = attributeResourceTestUtils.createObject("attribute7.json");

		// manipulate attribute path attributes
		final String attributePathJSONString = DMPPersistenceUtil.getResourceAsString("attribute_path.json");
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

		AttributePathsResourceTest.LOG.debug("start attribute paths uniqueness test");

		AttributePath attributePath1 = null;

		try {

			attributePath1 = pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
		} catch (final Exception e) {

			AttributePathsResourceTest.LOG.error("coudln't create attribute path 1 for uniqueness test");

			Assert.assertTrue(false);
		}

		Assert.assertNotNull("attribute path 1 shouldn't be null in uniqueness test", attributePath1);

		AttributePath attributePath2 = null;

		try {

			final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
					.post(Entity.json(objectJSONString));

			Assert.assertEquals("200 OK was expected", 200, response.getStatus());

			final String responseString = response.readEntity(String.class);

			Assert.assertNotNull("the response JSON shouldn't be null", responseString);

			attributePath2 = objectMapper.readValue(responseString, pojoClass);

			compareObjects(expectedObject, attributePath2);
		} catch (final Exception e) {

			AttributePathsResourceTest.LOG.error("couldn't create attribute path 2 for uniqueness test");

			Assert.assertTrue(false);
		}

		Assert.assertNotNull("attribute path 2 shouldn't be null in uniqueness test", attributePath2);

		Assert.assertEquals("the attribute paths should be equal", attributePath1, attributePath2);

		cleanUpDB(attributePath1);

		AttributePathsResourceTest.LOG.debug("end attribute paths uniqueness test");
	}

	@Override
	public void testPUTObject() throws Exception {

		AttributePathsResourceTest.LOG.debug("start attribute path update test");

		AttributePath attributePath = null;

		try {

			attributePath = pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
		} catch (final Exception e) {

			AttributePathsResourceTest.LOG.error("coudln't create attribute path for update test");

			Assert.assertTrue(false);
		}

		Assert.assertNotNull("attribute path shouldn't be null in update test", attributePath);

		final Attribute actualAttribute3 = attributeResourceTestUtils.createObject("attribute3.json");

		attributePath.addAttribute(actualAttribute3);

		final String attributePathJSONString = objectMapper.writeValueAsString(attributePath);

		final AttributePath updateAttributePath = pojoClassResourceTestUtils.updateObject(attributePathJSONString, attributePath);

		Assert.assertEquals("the persisted attribute path shoud be equal to the modified attribute path for update", updateAttributePath,
				attributePath);
		Assert.assertEquals("number of attribute elements in attribute path should be equal", updateAttributePath.getAttributePath().size(),
				attributePath.getAttributePath().size());

		cleanUpDB(attributePath);

		attributeResourceTestUtils.deleteObject(actualAttribute3);

		AttributePathsResourceTest.LOG.debug("end attribute update test");
	}

	@Test
	public void testPUTObjectWExistingAttributePath() throws Exception {

		AttributePathsResourceTest.LOG.debug("start attribute path update test with existing attribute path");

		final Attribute actualAttribute3 = attributeResourceTestUtils.createObject("attribute3.json");

		expectedObject.addAttribute(actualAttribute3);

		objectJSONString = objectMapper.writeValueAsString(expectedObject);

		AttributePath attributePath = null;

		try {

			attributePath = pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
		} catch (final Exception e) {

			AttributePathsResourceTest.LOG.error("coudln't create attribute path for update test");

			Assert.assertTrue(false);
		}

		Assert.assertNotNull("attribute path shouldn't be null in update test", attributePath);

		final AttributePath retrievedAttributePath = pojoClassResourceTestUtils.getObjectAndCompare(attributePath);

		// remove an attribute
		retrievedAttributePath.removeAttribute(actualAttribute3, 2);

		String attributePathJSONString = objectMapper.writeValueAsString(retrievedAttributePath);

		AttributePath modifiedAttributePath = null;

		try {

			modifiedAttributePath = pojoClassResourceTestUtils.createObject(attributePathJSONString, retrievedAttributePath);
		} catch (final Exception e) {

			AttributePathsResourceTest.LOG.error("coudln't create modified attribute path for update test");

			Assert.assertTrue(false);
		}

		Assert.assertNotNull("attribute path shouldn't be null in update test", modifiedAttributePath);

		// add an attribute
		modifiedAttributePath.addAttribute(actualAttribute3);

		attributePathJSONString = objectMapper.writeValueAsString(modifiedAttributePath);

		final AttributePath updateAttributePath = pojoClassResourceTestUtils.updateObject(attributePathJSONString, modifiedAttributePath);

		// ids should differ
		Assert.assertNotEquals("the persisted attribute path shoud not be equal to the modified attribute path for update", modifiedAttributePath,
				updateAttributePath);
		// ids should be the same
		Assert.assertEquals("the persisted attribute path shoud be equal to the modified attribute path for update", attributePath,
				updateAttributePath);
		Assert.assertEquals("number of attribute elements in attribute path should be equal", attributePath.getAttributePath().size(),
				updateAttributePath.getAttributePath().size());
		Assert.assertNotEquals("number of attribute elements in attribute path should be equal", retrievedAttributePath.getAttributePath().size(),
				updateAttributePath.getAttributePath().size());
		Assert.assertEquals("number of attribute elements in attribute path should be equal", 3, updateAttributePath.getAttributePath().size());

		cleanUpDB(attributePath);
		cleanUpDB(modifiedAttributePath);

		attributeResourceTestUtils.deleteObject(actualAttribute3);

		AttributePathsResourceTest.LOG.debug("end attribute update test with existing attribute path");
	}

	@After
	public void tearDown2() throws Exception {

		attributeResourceTestUtils.deleteObject(actualAttribute1);
		attributeResourceTestUtils.deleteObject(actualAttribute2);
	}
}
