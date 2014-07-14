package org.dswarm.persistence.service.test;

import java.util.Set;

import org.dswarm.persistence.model.DMPJPAObject;
import org.dswarm.persistence.model.proxy.ProxyDMPJPAObject;
import org.dswarm.persistence.service.BasicIDJPAService;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public abstract class IDBasicJPAServiceTest<PROXYPOJOCLASS extends ProxyDMPJPAObject<POJOCLASS>, POJOCLASS extends DMPJPAObject, JPASERVICEIMPL extends BasicIDJPAService<PROXYPOJOCLASS, POJOCLASS>>
		extends BasicJPAServiceTest<PROXYPOJOCLASS, POJOCLASS, JPASERVICEIMPL, Long> {

	private static final Logger	LOG	= LoggerFactory.getLogger(IDBasicJPAServiceTest.class);

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

		IDBasicJPAServiceTest.LOG.debug("start id generation test for " + type);

		final Set<POJOCLASS> objectes = Sets.newLinkedHashSet();

		for (int i = 0; i < 10; i++) {

			final PROXYPOJOCLASS proxyObject = createObject();

			objectes.add(proxyObject.getObject());
		}

		Assert.assertEquals(type + "s set size should be 10", 10, objectes.size());

		// clean-up DB table
		for (final POJOCLASS object : objectes) {

			jpaService.deleteObject(object.getId());
		}

		IDBasicJPAServiceTest.LOG.debug("end id generation test for " + type);
	}
}
