package de.avgl.dmp.persistence.service.test;

import org.junit.Assert;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.BasicDMPObject;
import de.avgl.dmp.persistence.service.AdvancedJPAService;

public abstract class AdvancedJPAServiceTest<POJOCLASS extends BasicDMPObject, JPASERVICEIMPL extends AdvancedJPAService<POJOCLASS>> extends
		BasicJPAServiceTest<POJOCLASS, JPASERVICEIMPL, String> {

	private static final org.apache.log4j.Logger	LOG			= org.apache.log4j.Logger.getLogger(AdvancedJPAServiceTest.class);

	public AdvancedJPAServiceTest(final String type, final Class<JPASERVICEIMPL> jpaServiceClass) {

		super(type, jpaServiceClass);
	}

	protected POJOCLASS createObject(final String id) {

		POJOCLASS object = null;

		try {

			object = jpaService.createObjectTransactional(id);
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong during object creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull(type + " shouldn't be null", object);
		Assert.assertNotNull(type + " id shouldn't be null", object.getId());

		LOG.debug("created new " + type + " with id = '" + object.getId() + "'");
		
		getObject(object);

		return object;
	}
}
