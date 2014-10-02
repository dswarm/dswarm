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

import org.dswarm.persistence.model.BasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.ProxyBasicDMPJPAObject;
import org.dswarm.persistence.service.BasicDMPJPAService;

public abstract class BasicDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE extends BasicDMPJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyBasicDMPJPAObject<POJOCLASS>, POJOCLASS extends BasicDMPJPAObject>
		extends BasicJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS, Long> {

	public BasicDMPJPAServiceTestUtils(final Class<POJOCLASS> pojoClassArg, final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg) {

		super(pojoClassArg, persistenceServiceClassArg);
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert that their names are equal.
	 * 
	 * @param expectedObject
	 * @param actualObject
	 */
	@Override
	public void compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) {

		super.compareObjects(expectedObject, actualObject);

		Assert.assertEquals("the " + pojoClassName + " names should be equal", expectedObject.getName(), actualObject.getName());

	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name of the object.
	 */
	@Override
	protected POJOCLASS prepareObjectForUpdate(final POJOCLASS objectWithUpdates, final POJOCLASS object) {

		object.setName(objectWithUpdates.getName());

		return object;
	}
}
