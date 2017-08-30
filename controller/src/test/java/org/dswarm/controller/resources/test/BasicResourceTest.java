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
package org.dswarm.controller.resources.test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.resources.test.utils.BasicResourceTestUtils;
import org.dswarm.controller.test.GuicedTest;
import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.proxy.ProxyDMPObject;
import org.dswarm.persistence.service.BasicJPAService;
import org.dswarm.persistence.service.test.utils.BasicJPAServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @param <POJOCLASSRESOURCETESTUTILS>
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @author tgaengler
 */
public abstract class BasicResourceTest<POJOCLASSRESOURCETESTUTILS extends BasicResourceTestUtils<POJOCLASSPERSISTENCESERVICETESTUTILS, POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS>, POJOCLASSPERSISTENCESERVICETESTUTILS extends BasicJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS>, POJOCLASSPERSISTENCESERVICE extends BasicJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyDMPObject<POJOCLASS>, POJOCLASS extends DMPObject>
		extends ResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(BasicResourceTest.class);

	protected String         objectJSONString         = null;
	protected POJOCLASS      expectedObject           = null;
	protected Set<POJOCLASS> expectedObjects          = null;
	protected String         updateObjectJSONFileName = null;

	protected POJOCLASSPERSISTENCESERVICE persistenceService;

	protected ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	protected final String objectJSONFileName;

	protected final Class<POJOCLASS> pojoClass;

	protected final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClass;

	protected final String pojoClassName;

	protected POJOCLASSRESOURCETESTUTILS pojoClassResourceTestUtils;

	public BasicResourceTest(final Class<POJOCLASS> pojoClassArg, final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg,
			final String resourceIdentifier, final String objectJSONFileNameArg, final POJOCLASSRESOURCETESTUTILS pojoClassResourceTestUtilsArg) {

		super(resourceIdentifier);

		pojoClass = pojoClassArg;
		persistenceServiceClass = persistenceServiceClassArg;

		pojoClassName = pojoClass.getSimpleName();

		objectJSONFileName = objectJSONFileNameArg;

		pojoClassResourceTestUtils = pojoClassResourceTestUtilsArg;

		initObjects();
	}

	@Override public void prepare() throws Exception {
		super.prepare();

		objectJSONString = DMPPersistenceUtil.getResourceAsString(objectJSONFileName);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);
	}

	@Override
	protected void initObjects() {

		super.initObjects();

		objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);
		persistenceService = GuicedTest.injector.getInstance(persistenceServiceClass);
	}

	@Test
	public void testPOSTObjects() throws Exception {

		BasicResourceTest.LOG.debug("start POST " + pojoClassName + "s test");

		createObjectInternal();

		BasicResourceTest.LOG.debug("end POST " + pojoClassName + "s test");
	}

	@Test
	public void testGETObjects() throws Exception {

		BasicResourceTest.LOG.debug("start GET " + pojoClassName + "s test");

		final List<POJOCLASS> testIndependentPersistedObjects = pojoClassResourceTestUtils.getObjects();

		final POJOCLASS actualObject = createObjectInternal();

		BasicResourceTest.LOG.debug("try to retrieve " + pojoClassName + "s");

		final Response response = target().request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseObjects = response.readEntity(String.class);

		expectedObjects = Sets.newHashSet();
		expectedObjects.addAll(testIndependentPersistedObjects);
		expectedObjects.add(actualObject);

		pojoClassResourceTestUtils.reset();
		evaluateObjects(expectedObjects, responseObjects);

		BasicResourceTest.LOG.debug("end GET " + pojoClassName + "s");
	}

	@Test
	public void testGETObject() throws Exception {

		BasicResourceTest.LOG.debug("start GET " + pojoClassName + " test");

		final POJOCLASS actualObject = createObjectInternal();

		pojoClassResourceTestUtils.getObjectAndCompare(actualObject);

		BasicResourceTest.LOG.debug("end GET " + pojoClassName);
	}

	@Test
	public void testPUTObject() throws Exception {
		BasicResourceTest.LOG.debug("start PUT " + pojoClassName + " test");

		final POJOCLASS persistedObject = createObjectInternal();

		final String idEncoded = URLEncoder.encode(persistedObject.getUuid(), "UTF-8");

		Assert.assertNotNull("the id shouldn't be null", idEncoded);

		final POJOCLASS updatedObject = updateObject(persistedObject);

		BasicResourceTest.LOG.debug("try to retrieve updated " + pojoClassName + " with id '" + idEncoded + "'");

		final Response response = target(idEncoded).request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseObjectJSON = response.readEntity(String.class);

		Assert.assertNotNull("response " + pojoClassName + " JSON shouldn't be null", responseObjectJSON);

		final POJOCLASS responseObject = objectMapper.readValue(responseObjectJSON, pojoClass);

		Assert.assertNotNull("response " + pojoClassName + " shouldn't be null", responseObject);

		pojoClassResourceTestUtils.reset();
		compareObjects(updatedObject, responseObject);

		BasicResourceTest.LOG.debug("end PUT " + pojoClassName);
	}

	@Test
	public void testDELETEObject() throws Exception {

		BasicResourceTest.LOG.debug("start DELETE " + pojoClassName + " test");

		final POJOCLASS actualObject = createObjectInternal();

		final String objectId = actualObject.getUuid();
		String idEncoded = null;

		try {

			idEncoded = URLEncoder.encode(objectId, "UTF-8");
		} catch (final UnsupportedEncodingException e) {

			BasicResourceTest.LOG.debug("couldn't encode id", e);

			Assert.assertTrue(false);
		}

		Assert.assertNotNull("the id shouldn't be null", idEncoded);

		BasicResourceTest.LOG.debug("try to retrieve " + pojoClassName + " with id '" + idEncoded + "'");

		final Response response = target(idEncoded).request().delete();

		Assert.assertEquals("204 NO CONTENT was expected", 204, response.getStatus());

		final POJOCLASS deletedObject = persistenceService.getObject(objectId);

		Assert.assertNull(deletedObject);

		BasicResourceTest.LOG.debug("end DELETE " + pojoClassName);
	}

	protected boolean compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) throws JsonProcessingException, JSONException {

		pojoClassResourceTestUtils.compareObjects(expectedObject, actualObject);

		return true;
	}

	protected boolean evaluateObjects(final Set<POJOCLASS> expectedObjects, final String actualObjects) throws Exception {

		pojoClassResourceTestUtils.evaluateObjects(actualObjects, expectedObjects);

		return true;
	}

	protected POJOCLASS createObjectInternal() throws Exception {

		return pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
	}

	protected POJOCLASS updateObject(final POJOCLASS persistedObject) throws Exception {

		return pojoClassResourceTestUtils.updateObject(persistedObject, updateObjectJSONFileName);
	}

	protected void deletedObject(final POJOCLASS object) {

		pojoClassResourceTestUtils.deleteObject(object);
	}
}
