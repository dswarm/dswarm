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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.AdvancedDMPJPAObject;
import org.dswarm.persistence.model.proxy.ProxyAdvancedDMPJPAObject;
import org.dswarm.persistence.model.types.Tuple;
import org.dswarm.persistence.service.AdvancedDMPJPAService;

public abstract class AdvancedDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE extends AdvancedDMPJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyAdvancedDMPJPAObject<POJOCLASS>, POJOCLASS extends AdvancedDMPJPAObject>
		extends BasicDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {
	
	protected static final Map<String, Tuple<String, String>> commonTermsMap = new HashMap<>();

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
	
	/**
	 * note: if the object was created before, you'll get the cached result. if you would like to get a fresh object from the database (e.g. for uniqueness test etc.), then you should utilise, e.g., the #createAttribute(String, String) method (of the concrete implementation)
	 * 
	 * @param identifier
	 * @return
	 * @throws Exception
	 */
	@Override
	public POJOCLASS getObject(final String identifier) throws Exception {

		if (!cache.containsKey(identifier)) {

			if(!commonTermsMap.containsKey(identifier)) {

				throw new DMPPersistenceException(identifier + " is no common object, please define it or utilise another appropriated method for creating it");
			}

			final Tuple<String, String> tuple = commonTermsMap.get(identifier);

			cache.put(identifier, createObject(tuple.v1(), tuple.v2()));
		}

		return cache.get(identifier);
	}
	
	public abstract POJOCLASS createObject(final String identifier, final String name) throws Exception;
}
