package de.avgl.dmp.controller.resources.test;

import java.util.Set;

import org.junit.Ignore;

import de.avgl.dmp.controller.resources.test.utils.AttributesResourceTestUtils;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.service.schema.AttributeService;

public class AttributesResourceTest extends BasicResourceTest<AttributeService, Attribute, String> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributesResourceTest.class);

	private final AttributesResourceTestUtils		attributesResourceTestUtils;

	public AttributesResourceTest() {

		super(Attribute.class, AttributeService.class, "attributes", "attribute.json");

		attributesResourceTestUtils = new AttributesResourceTestUtils();
	}

	/**
	 * note: this operation is not supported right now
	 */
	@Ignore
	@Override
	public void testGETObject() throws Exception {

		super.testGETObject();
	}

	@Override
	protected boolean compareObjects(final Attribute expectedObject, final Attribute actualObject) {

		// ResourceTestUtils.compareAttributes(expectedObject, actualObject);

		attributesResourceTestUtils.compareObjects(expectedObject, actualObject);

		return true;
	}

	@Override
	protected boolean evaluateObjects(final Set<Attribute> expectedObjects, final String actualObjects) throws Exception {

		// ResourceTestUtils.evaluateAttributes(actualObjects, expectedObjects);

		attributesResourceTestUtils.evaluateObjects(actualObjects, expectedObjects);

		return true;
	}
}
