package de.avgl.dmp.persistence.service.test;

import org.junit.Assert;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.AdvancedDMPJPAObject;
import de.avgl.dmp.persistence.model.proxy.ProxyAdvancedDMPJPAObject;
import de.avgl.dmp.persistence.service.AdvancedDMPJPAService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AdvancedJPAServiceTest<PROXYPOJOCLASS extends ProxyAdvancedDMPJPAObject<POJOCLASS>, POJOCLASS extends AdvancedDMPJPAObject, JPASERVICEIMPL extends AdvancedDMPJPAService<PROXYPOJOCLASS, POJOCLASS>>
		extends IDBasicJPAServiceTest<PROXYPOJOCLASS, POJOCLASS, JPASERVICEIMPL> {

	private static final Logger LOG = LoggerFactory.getLogger(AdvancedJPAServiceTest.class);

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
