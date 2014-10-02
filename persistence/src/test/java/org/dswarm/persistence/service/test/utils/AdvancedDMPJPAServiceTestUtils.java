/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.service.test.utils;

import org.junit.Assert;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.AdvancedDMPJPAObject;
import org.dswarm.persistence.model.proxy.ProxyAdvancedDMPJPAObject;
import org.dswarm.persistence.service.AdvancedDMPJPAService;

public abstract class AdvancedDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE extends AdvancedDMPJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyAdvancedDMPJPAObject<POJOCLASS>, POJOCLASS extends AdvancedDMPJPAObject>
		extends BasicDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	public AdvancedDMPJPAServiceTestUtils(final Class<POJOCLASS> pojoClassArg, final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg) {

		super(pojoClassArg, persistenceServiceClassArg);
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert that their URIs are equal. <br />
	 * 
	 * @param expectedObject
	 * @param actualObject
	 */
	@Override
	public void compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) {

		super.compareObjects(expectedObject, actualObject);

		Assert.assertEquals("the " + pojoClassName + " uris should be equal", expectedObject.getUri(), actualObject.getUri());

	}

	@Override
	protected PROXYPOJOCLASS createObject(final POJOCLASS object) throws DMPPersistenceException {

		return jpaService.createObjectTransactional(object);
	}
}
