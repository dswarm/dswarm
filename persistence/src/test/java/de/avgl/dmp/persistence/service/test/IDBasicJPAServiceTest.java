package de.avgl.dmp.persistence.service.test;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.DMPObject;
import de.avgl.dmp.persistence.service.BasicJPAService;

public abstract class IDBasicJPAServiceTest<POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, JPASERVICEIMPL extends BasicJPAService<POJOCLASS, POJOCLASSIDTYPE>, POJOCLASSIDTYPE>
		extends BasicJPAServiceTest<POJOCLASS, JPASERVICEIMPL, POJOCLASSIDTYPE> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(IDBasicJPAServiceTest.class);

	public IDBasicJPAServiceTest(final String type, final Class<JPASERVICEIMPL> jpaServiceClass) {

		super(type, jpaServiceClass);
	}

	/**
	 * Test for identifier generation: Creates ten instances (incl. identifier generation) of the specific class, writes them to
	 * the databases and check the size of the set afterwards.<br>
	 * Created by: tgaengler
	 */
	@Test
	public void idGenerationTest() {
		
		LOG.debug("start id generation test for " + type);

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
		
		LOG.debug("end id generation test for " + type);
	}
}
