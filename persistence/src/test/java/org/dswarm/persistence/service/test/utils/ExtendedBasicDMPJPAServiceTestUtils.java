package org.dswarm.persistence.service.test.utils;

import org.junit.Assert;

import org.dswarm.persistence.model.ExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.ProxyExtendedBasicDMPJPAObject;
import org.dswarm.persistence.service.ExtendedBasicDMPJPAService;

public abstract class ExtendedBasicDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE extends ExtendedBasicDMPJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyExtendedBasicDMPJPAObject<POJOCLASS>, POJOCLASS extends ExtendedBasicDMPJPAObject>
		extends BasicDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	public ExtendedBasicDMPJPAServiceTestUtils(final Class<POJOCLASS> pojoClassArg,
			final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg) {

		super(pojoClassArg, persistenceServiceClassArg);
	}

	
	/**
	 * {@inheritDoc} <br />
	 * Assert that both objects have no or equal descriptions.
	 * 
	 */
	@Override
	public void compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) {

		super.compareObjects(expectedObject, actualObject);
		
		Assert.assertEquals("the " + pojoClassName + " descriptions should be equal", expectedObject.getDescription(),
					actualObject.getDescription());		
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name and description of the object.
	 */
	@Override
	protected POJOCLASS prepareObjectForUpdate(final POJOCLASS objectWithUpdates, final POJOCLASS object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		object.setDescription(objectWithUpdates.getDescription());

		return object;
	}
}
