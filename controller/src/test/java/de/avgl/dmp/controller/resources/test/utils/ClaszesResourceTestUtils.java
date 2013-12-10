package de.avgl.dmp.controller.resources.test.utils;

import org.junit.Assert;

import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.service.schema.ClaszService;

public class ClaszesResourceTestUtils extends BasicResourceTestUtils<ClaszService, Clasz, String> {

	public ClaszesResourceTestUtils() {

		super("classes", Clasz.class, ClaszService.class);
	}

	@Override
	public void compareObjects(final Clasz expectedObject, final Clasz actualObject) {

		super.compareObjects(expectedObject, actualObject);

		if (expectedObject.getId() != null) {

			Assert.assertNotNull("the class description shouldn't be null", actualObject.getId());
			Assert.assertEquals("the class descriptions should be equal", expectedObject.getId(), actualObject.getId());
		}

		if (expectedObject.getName() != null) {

			Assert.assertNotNull("the class name shouldn't be null", actualObject.getName());
			Assert.assertEquals("the class names should be equal", expectedObject.getName(), actualObject.getName());
		}
	}
}
