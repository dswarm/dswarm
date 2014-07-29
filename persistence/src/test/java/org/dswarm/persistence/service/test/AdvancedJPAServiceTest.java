package org.dswarm.persistence.service.test;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.AdvancedDMPJPAObject;
import org.dswarm.persistence.model.proxy.ProxyAdvancedDMPJPAObject;
import org.dswarm.persistence.service.AdvancedDMPJPAService;

public abstract class AdvancedJPAServiceTest<PROXYPOJOCLASS extends ProxyAdvancedDMPJPAObject<POJOCLASS>, POJOCLASS extends AdvancedDMPJPAObject, JPASERVICEIMPL extends AdvancedDMPJPAService<PROXYPOJOCLASS, POJOCLASS>>
		extends IDBasicJPAServiceTest<PROXYPOJOCLASS, POJOCLASS, JPASERVICEIMPL> {

	private static final Logger	LOG	= LoggerFactory.getLogger(AdvancedJPAServiceTest.class);

	public AdvancedJPAServiceTest(final String type, final Class<JPASERVICEIMPL> jpaServiceClass) {

		super(type, jpaServiceClass);
	}

	protected PROXYPOJOCLASS createObject(final String id) {

		PROXYPOJOCLASS proxyObject = null;

		try {

			proxyObject = jpaService.createOrGetObjectTransactional(id);
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong during object creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull(type + " shouldn't be null", proxyObject);
		Assert.assertNotNull(type + " id shouldn't be null", proxyObject.getId());

		AdvancedJPAServiceTest.LOG.debug("created new " + type + " with id = '" + proxyObject.getId() + "'");

		return proxyObject;
	}
}
