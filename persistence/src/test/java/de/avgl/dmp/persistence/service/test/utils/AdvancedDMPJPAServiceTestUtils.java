package de.avgl.dmp.persistence.service.test.utils;

import org.junit.Assert;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.AdvancedDMPJPAObject;
import de.avgl.dmp.persistence.model.proxy.ProxyAdvancedDMPJPAObject;
import de.avgl.dmp.persistence.service.AdvancedDMPJPAService;

public abstract class AdvancedDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE extends AdvancedDMPJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyAdvancedDMPJPAObject<POJOCLASS>, POJOCLASS extends AdvancedDMPJPAObject>
		extends BasicDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	public AdvancedDMPJPAServiceTestUtils(final Class<POJOCLASS> pojoClassArg, final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg) {

		super(pojoClassArg, persistenceServiceClassArg);
	}

	@Override
	public void compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) {

		super.compareObjects(expectedObject, actualObject);

		if (expectedObject.getUri() != null) {

			Assert.assertNotNull("the " + pojoClassName + " uri shouldn't be null", actualObject.getUri());
			Assert.assertEquals("the " + pojoClassName + " uris should be equal", expectedObject.getUri(), actualObject.getUri());
		}
	}

	@Override
	protected PROXYPOJOCLASS createObject(final POJOCLASS object) throws DMPPersistenceException {
		
		return jpaService.createObjectTransactional(object);
	}
}
