package de.avgl.dmp.controller.resources.test.utils;

import org.junit.Assert;

import de.avgl.dmp.persistence.model.AdvancedDMPJPAObject;
import de.avgl.dmp.persistence.model.proxy.ProxyAdvancedDMPJPAObject;
import de.avgl.dmp.persistence.service.AdvancedDMPJPAService;

public abstract class AdvancedDMPResourceTestUtils<POJOCLASSPERSISTENCESERVICE extends AdvancedDMPJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyAdvancedDMPJPAObject<POJOCLASS>, POJOCLASS extends AdvancedDMPJPAObject>
		extends BasicDMPResourceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	public AdvancedDMPResourceTestUtils(final String resourceIdentifier, final Class<POJOCLASS> pojoClassArg,
			final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg) {

		super(resourceIdentifier, pojoClassArg, persistenceServiceClassArg);
	}

	@Override
	public void compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) {

		super.compareObjects(expectedObject, actualObject);

		if (expectedObject.getUri() != null) {

			Assert.assertNotNull("the " + pojoClassName + " name shouldn't be null", actualObject.getUri());
			Assert.assertEquals("the " + pojoClassName + " names should be equal", expectedObject.getUri(), actualObject.getUri());
		}
	}
}
