package org.dswarm.persistence.service.test.utils;

import org.junit.Assert;

import org.dswarm.persistence.model.BasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.ProxyBasicDMPJPAObject;
import org.dswarm.persistence.service.BasicDMPJPAService;

public abstract class BasicDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE extends BasicDMPJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyBasicDMPJPAObject<POJOCLASS>, POJOCLASS extends BasicDMPJPAObject>
		extends BasicJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS, Long> {

	public BasicDMPJPAServiceTestUtils(final Class<POJOCLASS> pojoClassArg, final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg) {

		super(pojoClassArg, persistenceServiceClassArg);
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert that their names are equal.
	 * 
	 * @param expectedObject
	 * @param actualObject
	 */
	@Override
	public void compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) {

		super.compareObjects(expectedObject, actualObject);

		Assert.assertEquals("the " + pojoClassName + " names should be equal", expectedObject.getName(), actualObject.getName());

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
