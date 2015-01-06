/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.persistence.service.test;

import java.util.Set;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.model.DMPJPAObject;
import org.dswarm.persistence.model.proxy.ProxyDMPJPAObject;
import org.dswarm.persistence.service.BasicIDJPAService;

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
