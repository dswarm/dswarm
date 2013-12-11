package de.avgl.dmp.controller.resources.test.utils;

import org.junit.Assert;

import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.service.schema.AttributeService;

public class AttributesResourceTestUtils extends BasicResourceTestUtils<AttributeService, Attribute, String> {

	public AttributesResourceTestUtils() {

		super("attributes", Attribute.class, AttributeService.class);
	}

	@Override
	public void compareObjects(final Attribute expectedObject, final Attribute actualObject) {

		super.compareObjects(expectedObject, actualObject);

		if (expectedObject.getId() != null) {

			Assert.assertNotNull("the attribute description shouldn't be null", actualObject.getId());
			Assert.assertEquals("the attribute descriptions should be equal", expectedObject.getId(), actualObject.getId());
		}

		if (expectedObject.getName() != null) {

			Assert.assertNotNull("the attribute name shouldn't be null", actualObject.getName());
			Assert.assertEquals("the attribute names should be equal", expectedObject.getName(), actualObject.getName());
		}
	}
}
