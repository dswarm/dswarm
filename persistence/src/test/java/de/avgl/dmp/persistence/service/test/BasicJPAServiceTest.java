package de.avgl.dmp.persistence.service.test;

import org.junit.Assert;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.DMPObject;
import de.avgl.dmp.persistence.service.BasicJPAService;

public abstract class BasicJPAServiceTest<POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, JPASERVICEIMPL extends BasicJPAService<POJOCLASS, POJOCLASSIDTYPE>, POJOCLASSIDTYPE> extends GuicedTest {

	private static final org.apache.log4j.Logger	LOG			= org.apache.log4j.Logger.getLogger(BasicJPAServiceTest.class);

	protected final String							type;
	protected final Class<JPASERVICEIMPL>			jpaServiceClass;
	protected JPASERVICEIMPL						jpaService	= null;

	public BasicJPAServiceTest(final String type, final Class<JPASERVICEIMPL> jpaServiceClass) {

		this.type = type;
		this.jpaServiceClass = jpaServiceClass;

		jpaService = injector.getInstance(jpaServiceClass);

		Assert.assertNotNull(type + " service shouldn't be null", jpaService);
	}

	protected POJOCLASS createObject() {

		POJOCLASS object = null;

		try {

			object = jpaService.createObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong during object creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull(type + " shouldn't be null", object);
		Assert.assertNotNull(type + " id shouldn't be null", object.getId());

		LOG.debug("created new " + type + " with id = '" + object.getId() + "'");
		
		getObject(object);

		return object;
	}

	protected POJOCLASS updateObjectTransactional(final POJOCLASS object) {

		POJOCLASS updatedObject = null;

		try {

			updatedObject = jpaService.updateObjectTransactional(object);
		} catch (DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updaging the " + type, false);
		}

		return updatedObject;
	}

	protected POJOCLASS getObject(final POJOCLASS object) {

		POJOCLASS bbject = null;

		bbject = jpaService.getObject(object.getId());

		Assert.assertNotNull("the updated " + type + " shoudln't be null", bbject);
		Assert.assertEquals("the " + type + "s are not equal", object, bbject);

		return bbject;
	}

	protected void deletedObject(final POJOCLASSIDTYPE id) {

		jpaService.deleteObject(id);

		final POJOCLASS deletedObject = jpaService.getObject(id);

		Assert.assertNull("deleted " + type + " shouldn't exist any more", deletedObject);
	}
}
