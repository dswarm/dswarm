package de.avgl.dmp.controller.resources.test;

import org.junit.After;

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

		final String attribute1JSONString = DMPPersistenceUtil.getResourceAsString("attribute.json");
		final Attribute expectedAttribute1 = objectMapper.readValue(attribute1JSONString, Attribute.class);

		actualAttribute1 = attributeResourceTestUtils.createObject(attribute1JSONString, expectedAttribute1);

		final String attribute2JSONString = DMPPersistenceUtil.getResourceAsString("attribute2.json");
		final Attribute expectedAttribute2 = objectMapper.readValue(attribute2JSONString, Attribute.class);

		actualAttribute2 = attributeResourceTestUtils.createObject(attribute2JSONString, expectedAttribute2);

		super.prepare();
	}

	@After
	public void tearDown2() throws Exception {

		attributeResourceTestUtils.deleteObject(actualAttribute1);
		attributeResourceTestUtils.deleteObject(actualAttribute2);
	}
}
