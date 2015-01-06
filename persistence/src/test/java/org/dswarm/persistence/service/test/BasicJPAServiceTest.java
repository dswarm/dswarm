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

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.proxy.ProxyDMPObject;
import org.dswarm.persistence.service.BasicJPAService;

public abstract class BasicJPAServiceTest<PROXYPOJOCLASS extends ProxyDMPObject<POJOCLASS, POJOCLASSIDTYPE>, POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, JPASERVICEIMPL extends BasicJPAService<PROXYPOJOCLASS, POJOCLASS, POJOCLASSIDTYPE>, POJOCLASSIDTYPE>
		extends GuicedTest {

	private static final Logger				LOG			= LoggerFactory.getLogger(BasicJPAServiceTest.class);

	protected final String					type;
	protected final Class<JPASERVICEIMPL>	jpaServiceClass;
	protected JPASERVICEIMPL				jpaService	= null;

	public BasicJPAServiceTest(final String type, final Class<JPASERVICEIMPL> jpaServiceClass) {

		this.type = type;
		this.jpaServiceClass = jpaServiceClass;

		jpaService = GuicedTest.injector.getInstance(jpaServiceClass);

		Assert.assertNotNull(type + " service shouldn't be null", jpaService);
	}

	protected PROXYPOJOCLASS createObject() {

		PROXYPOJOCLASS proxyObject = null;

		try {

			proxyObject = jpaService.createObjectTransactional();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong during object creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull(type + " shouldn't be null", proxyObject);
		Assert.assertNotNull(type + " id shouldn't be null", proxyObject.getId());

		BasicJPAServiceTest.LOG.debug("created new " + type + " with id = '" + proxyObject.getId() + "'");

		return proxyObject;
	}

	protected PROXYPOJOCLASS updateObjectTransactional(final POJOCLASS object) {

		PROXYPOJOCLASS proxyUpdatedObject = null;

		try {

			proxyUpdatedObject = jpaService.updateObjectTransactional(object);
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updaging the " + type, false);
		}

		return proxyUpdatedObject;
	}

	protected POJOCLASS getObject(final POJOCLASS object) {

		POJOCLASS bbject = null;

		bbject = jpaService.getObject(object.getId());

		Assert.assertNotNull("the updated " + type + " shoudln't be null", bbject);
		Assert.assertEquals("the " + type + "s are not equal", object, bbject);

		return bbject;
	}

	protected void deleteObject(final POJOCLASSIDTYPE id) {

		jpaService.deleteObject(id);

		final POJOCLASS deletedObject = jpaService.getObject(id);

		Assert.assertNull("deleted " + type + " shouldn't exist any more", deletedObject);
	}
}
