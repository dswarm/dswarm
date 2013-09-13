package de.avgl.dmp.persistence.model.test;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.DMPJPAObject;
import de.avgl.dmp.persistence.services.BasicJPAService;

public abstract class BasicJPAServiceTest<POJOCLASS extends DMPJPAObject, JPASERVICEIMPL extends BasicJPAService<POJOCLASS>> {

	private static final org.apache.log4j.Logger	LOG			= org.apache.log4j.Logger.getLogger(BasicJPAServiceTest.class);

	protected final String							type;
	protected final Class<JPASERVICEIMPL>			jpaServiceClass;
	protected JPASERVICEIMPL						jpaService	= null;

	public BasicJPAServiceTest(final String type, final Class<JPASERVICEIMPL> jpaServiceClass) {

		this.type = type;
		this.jpaServiceClass = jpaServiceClass;

		try {
			jpaService = this.jpaServiceClass.newInstance();
		} catch (InstantiationException e1) {

			LOG.error("something went wrong while " + type + " service creation.\n" + e1.getMessage());

			Assert.assertTrue("something went wrong while " + type + " service creation.\n" + e1.getMessage(), false);
		} catch (IllegalAccessException e1) {

			LOG.error("something went wrong while " + type + " service creation.\n" + e1.getMessage());

			Assert.assertTrue("something went wrong while " + type + " service creation.\n" + e1.getMessage(), false);
		}

		Assert.assertNotNull(type + " service shouldn't be null", jpaService);
	}

	/**
	 * Test for identifier generation: Creates ten instances (incl. identifier generation) of the specific class, writes them to
	 * the databases and check the size of the set afterwards.<br>
	 * Created by: tgaengler
	 */
	@Test
	public void idGenerationTest() {

		final Set<POJOCLASS> objectes = Sets.newLinkedHashSet();

		for (int i = 0; i < 10; i++) {

			POJOCLASS object = createObject();

			objectes.add(object);
		}

		Assert.assertEquals(type + "s set size should be 10", 10, objectes.size());

		// clean-up DB table
		for (final POJOCLASS object : objectes) {

			jpaService.deleteObject(object.getId());
		}
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

		LOG.debug("create new " + type + " with id = '" + object.getId() + "'");

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
	
	protected POJOCLASS getUpdatedObject(final POJOCLASS object) {
		
		POJOCLASS updatedObject = null;
		
		updatedObject = jpaService.getObject(object.getId());
		
		Assert.assertNotNull("the updated " + type + " shoudln't be null", updatedObject);
		Assert.assertEquals("the " + type + "s are not equal", object, updatedObject);
		
		return updatedObject;
	}

	protected void deletedObject(final Long id) {

		jpaService.deleteObject(id);

		final POJOCLASS deletedObject = jpaService.getObject(id);

		Assert.assertNull("deleted " + type + " shouldn't exist any more", deletedObject);
	}
}
