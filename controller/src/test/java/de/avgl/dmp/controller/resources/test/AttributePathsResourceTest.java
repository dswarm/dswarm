package de.avgl.dmp.controller.resources.test;

import org.junit.After;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.controller.resources.test.utils.AttributePathsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.AttributesResourceTestUtils;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
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

	@After
	public void tearDown2() throws Exception {

		attributeResourceTestUtils.deleteObject(actualAttribute1);
		attributeResourceTestUtils.deleteObject(actualAttribute2);
	}
}
