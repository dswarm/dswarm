package de.avgl.dmp.persistence.service.test;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.DMPJPAObject;
import de.avgl.dmp.persistence.model.proxy.ProxyDMPJPAObject;
import de.avgl.dmp.persistence.service.BasicIDJPAService;

public abstract class IDBasicJPAServiceTest<PROXYPOJOCLASS extends ProxyDMPJPAObject<POJOCLASS>, POJOCLASS extends DMPJPAObject, JPASERVICEIMPL extends BasicIDJPAService<PROXYPOJOCLASS, POJOCLASS>>
		extends BasicJPAServiceTest<PROXYPOJOCLASS, POJOCLASS, JPASERVICEIMPL, Long> {

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
