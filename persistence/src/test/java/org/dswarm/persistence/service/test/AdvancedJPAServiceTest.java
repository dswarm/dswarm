/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
import org.dswarm.persistence.model.AdvancedDMPJPAObject;
import org.dswarm.persistence.model.proxy.ProxyAdvancedDMPJPAObject;
import org.dswarm.persistence.service.AdvancedDMPJPAService;

public abstract class AdvancedJPAServiceTest<PROXYPOJOCLASS extends ProxyAdvancedDMPJPAObject<POJOCLASS>, POJOCLASS extends AdvancedDMPJPAObject, JPASERVICEIMPL extends AdvancedDMPJPAService<PROXYPOJOCLASS, POJOCLASS>>
		extends BasicJPAServiceTest<PROXYPOJOCLASS, POJOCLASS, JPASERVICEIMPL> {

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
