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
package org.dswarm.persistence.service.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.proxy.ProxyDMPObject;
import org.dswarm.persistence.service.BasicJPAService;

public abstract class BasicJPAServiceTest<PROXYPOJOCLASS extends ProxyDMPObject<POJOCLASS, POJOCLASSIDTYPE>, POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, JPASERVICEIMPL extends BasicJPAService<PROXYPOJOCLASS, POJOCLASS, POJOCLASSIDTYPE>, POJOCLASSIDTYPE>
		extends GuicedTest {

	private static final Logger LOG = LoggerFactory.getLogger(BasicJPAServiceTest.class);

	protected ObjectMapper objectMapper = null;

	protected final String                type;
	protected final Class<JPASERVICEIMPL> jpaServiceClass;
	protected JPASERVICEIMPL jpaService = null;

	@Test
	public abstract void testSimpleObject() throws Exception;

	public BasicJPAServiceTest(final String type, final Class<JPASERVICEIMPL> jpaServiceClass) {

		this.type = type;
		this.jpaServiceClass = jpaServiceClass;

		initObjects();
	}

	public JPASERVICEIMPL getJpaService() {
		return jpaService;
	}

	protected void initObjects() {

		super.initObjects();

		jpaService = GuicedTest.injector.getInstance(jpaServiceClass);
		objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

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

	protected POJOCLASS getObject(final POJOCLASS object) throws JsonProcessingException, JSONException {

		final POJOCLASS persitentObject = jpaService.getObject(object.getId());

		Assert.assertNotNull("the updated " + type + " shoudln't be null", persitentObject);
		Assert.assertEquals("the " + type + "s are not equal", object, persitentObject);

		return persitentObject;
	}

	protected void deleteObject(final POJOCLASSIDTYPE id) {

		jpaService.deleteObject(id);

		final POJOCLASS deletedObject = jpaService.getObject(id);

		Assert.assertNull("deleted " + type + " shouldn't exist any more", deletedObject);
	}

	protected void logObjectJSON(final POJOCLASS object) {

		try {

			final String json = objectMapper.writeValueAsString(object);

			BasicJPAServiceTest.LOG.debug(type + " json: " + json);
		} catch (final JsonProcessingException e) {

			BasicJPAServiceTest.LOG.error("couldn't serialize " + getClass().getName() + " to JSON", e);
		}
	}
}
