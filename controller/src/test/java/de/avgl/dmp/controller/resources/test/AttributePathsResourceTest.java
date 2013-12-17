package de.avgl.dmp.controller.resources.test;

import org.junit.After;

import de.avgl.dmp.controller.resources.test.utils.AttributePathsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.AttributesResourceTestUtils;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.service.schema.AttributePathService;

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

		actualAttribute1 = attributeResourceTestUtils.createObject("attribute1.json");
		actualAttribute2 = attributeResourceTestUtils.createObject("attribute2.json");

		super.prepare();
	}

	@After
	public void tearDown2() throws Exception {

		attributeResourceTestUtils.deleteObject(actualAttribute1);
		attributeResourceTestUtils.deleteObject(actualAttribute2);
	}
}
