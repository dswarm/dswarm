package de.avgl.dmp.persistence.service.test.utils;

import org.junit.Assert;

import de.avgl.dmp.persistence.model.BasicDMPJPAObject;
import de.avgl.dmp.persistence.model.proxy.ProxyBasicDMPJPAObject;
import de.avgl.dmp.persistence.service.BasicDMPJPAService;

public abstract class BasicDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE extends BasicDMPJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyBasicDMPJPAObject<POJOCLASS>, POJOCLASS extends BasicDMPJPAObject>
		extends BasicJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS, Long> {

	public BasicDMPJPAServiceTestUtils(final Class<POJOCLASS> pojoClassArg, final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg) {

		super(pojoClassArg, persistenceServiceClassArg);
	}

	@Override
	public void compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) {

		super.compareObjects(expectedObject, actualObject);

		if (expectedObject.getName() != null) {

			Assert.assertNotNull("the " + pojoClassName + " name shouldn't be null", actualObject.getName());
			Assert.assertEquals("the " + pojoClassName + " names should be equal", expectedObject.getName(), actualObject.getName());
		}
	}
	
	/**
	 * {@inheritDoc}<br/>
	 * Updates the name of the object.
	 */
	@Override
	protected POJOCLASS prepareObjectForUpdate(final POJOCLASS objectWithUpdates, final POJOCLASS object) {

		object.setName(objectWithUpdates.getName());

		return object;
	}
}
