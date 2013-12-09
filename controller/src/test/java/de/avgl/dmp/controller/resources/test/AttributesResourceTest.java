package de.avgl.dmp.controller.resources.test;

import java.util.Set;

import org.junit.Ignore;

import de.avgl.dmp.controller.resources.test.utils.AttributesResourceTestUtils;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.service.schema.AttributeService;

public class AttributesResourceTest extends BasicResourceTest<AttributesResourceTestUtils, AttributeService, Attribute, String> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributesResourceTest.class);

	public AttributesResourceTest() {

		super(Attribute.class, AttributeService.class, "attributes", "attribute.json", new AttributesResourceTestUtils());
	}

	/**
	 * note: this operation is not supported right now
	 */
	@Ignore
	@Override
	public void testGETObject() throws Exception {

		//super.testGETObject();
	}

	@Override
	protected boolean compareObjects(final Attribute expectedObject, final Attribute actualObject) {

		pojoClassResourceTestUtils.compareObjects(expectedObject, actualObject);

		return true;
	}

	@Override
	protected boolean evaluateObjects(final Set<Attribute> expectedObjects, final String actualObjects) throws Exception {

		pojoClassResourceTestUtils.evaluateObjects(actualObjects, expectedObjects);

		return true;
	}
}
