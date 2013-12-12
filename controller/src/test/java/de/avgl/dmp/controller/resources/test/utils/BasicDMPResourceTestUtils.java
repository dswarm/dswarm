package de.avgl.dmp.controller.resources.test.utils;

import org.junit.Assert;

import de.avgl.dmp.persistence.model.BasicDMPJPAObject;
import de.avgl.dmp.persistence.service.BasicDMPJPAService;

public abstract class BasicDMPResourceTestUtils<POJOCLASSPERSISTENCESERVICE extends BasicDMPJPAService<POJOCLASS>, POJOCLASS extends BasicDMPJPAObject> extends BasicResourceTestUtils<POJOCLASSPERSISTENCESERVICE, POJOCLASS, Long> {

	public BasicDMPResourceTestUtils(final String resourceIdentifier, final Class<POJOCLASS> pojoClassArg, final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg) {

		super(resourceIdentifier, pojoClassArg, persistenceServiceClassArg);
	}

	@Override
	public void compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) {

		super.compareObjects(expectedObject, actualObject);

		if (expectedObject.getName() != null) {

			Assert.assertNotNull("the " + pojoClassName + " name shouldn't be null", actualObject.getName());
			Assert.assertEquals("the " + pojoClassName + " names should be equal", expectedObject.getName(), actualObject.getName());
		}
	}
}
