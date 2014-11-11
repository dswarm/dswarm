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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.proxy.ProxyDMPObject;
import org.dswarm.persistence.service.BasicJPAService;
import org.dswarm.persistence.service.test.BasicJPAServiceTest;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Maps;

public abstract class BasicJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE extends BasicJPAService<PROXYPOJOCLASS, POJOCLASS, POJOCLASSIDTYPE>, PROXYPOJOCLASS extends ProxyDMPObject<POJOCLASS, POJOCLASSIDTYPE>, POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, POJOCLASSIDTYPE>
		extends BasicJPAServiceTest<PROXYPOJOCLASS, POJOCLASS, POJOCLASSPERSISTENCESERVICE, POJOCLASSIDTYPE> {

	private static final Logger							LOG	= LoggerFactory.getLogger(BasicJPAServiceTestUtils.class);

	protected final Class<POJOCLASS>					pojoClass;

	protected final String								pojoClassName;

	protected final Class<POJOCLASSPERSISTENCESERVICE>	persistenceServiceClass;

	protected final ObjectMapper						objectMapper;
	
	protected Map<String, POJOCLASS> cache = new HashMap<>();

	public abstract POJOCLASS getObject( final JsonNode objectDescription) throws Exception;
	
	public abstract POJOCLASS getObject(final String identifier) throws Exception;
	
	public BasicJPAServiceTestUtils(final Class<POJOCLASS> pojoClassArg, final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg) {

		super(pojoClassArg.getSimpleName(), persistenceServiceClassArg);

		pojoClass = pojoClassArg;
		pojoClassName = pojoClass.getSimpleName();

		persistenceServiceClass = persistenceServiceClassArg;

		objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);
	}
	
	
	
	/**
	 * Assert that neither {@code expectedObject} nor {@code actualObject} is null.
	 * 
	 * @param expectedObject
	 * @param actualObject
	 */
	public void compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) {

		Assert.assertNotNull("excepted " + pojoClassName + " shouldn't be null", expectedObject);
		Assert.assertNotNull("actual " + pojoClassName + " shouldn't be null", actualObject);
	}

	public void evaluateObjects(final String objectsJSON, final Set<POJOCLASS> expectedObjects) throws Exception {

		Assert.assertNotNull("the " + pojoClassName + "s JSON string shouldn't be null", objectsJSON);

		final Map<POJOCLASSIDTYPE, POJOCLASS> responseObjects = Maps.newLinkedHashMap();
		final ArrayNode responseObjectsJSONArray = objectMapper.readValue(objectsJSON, ArrayNode.class);

		Assert.assertNotNull("response " + pojoClassName + "s JSON array shouldn't be null", responseObjectsJSONArray);

		final Iterator<JsonNode> responseObjectsJSONIter = responseObjectsJSONArray.iterator();

		while (responseObjectsJSONIter.hasNext()) {

			final JsonNode responseObjectJSON = responseObjectsJSONIter.next();

			final POJOCLASS responseObject = objectMapper.readValue(responseObjectJSON.toString(), pojoClass);

			responseObjects.put(responseObject.getId(), responseObject);
		}

		compareObjects(expectedObjects, responseObjects);
	}

	/**
	 * Assert that expectedObjects and actualObjects have the same size.<br />
	 * Assert that both collections contain equal objects regarding id and name.
	 * 
	 * @param expectedObjects
	 * @param actualObjects
	 */
	public void compareObjects(final Set<POJOCLASS> expectedObjects, final Map<POJOCLASSIDTYPE, POJOCLASS> actualObjects) {

		Assert.assertNotNull("expected objects shouldn't be null", expectedObjects);
		Assert.assertNotNull("actual objects shouldn't be null", actualObjects);

		Assert.assertEquals("different number of " + pojoClassName + " objects.", expectedObjects.size(), actualObjects.size());

		for (final POJOCLASS expectedObject : expectedObjects) {

			final POJOCLASS actualObject = actualObjects.get(expectedObject.getId());

			Assert.assertNotNull(pojoClassName + " for id '" + expectedObject.getId() + "' shouldn't be null", actualObject);
			Assert.assertEquals(pojoClassName + "s are not equal", expectedObject, actualObject);

			compareObjects(expectedObject, actualObject);
		}
	}

	public List<POJOCLASS> getObjects() {

		return jpaService.getObjects();
	}

	@Override
	public POJOCLASS getObject(final POJOCLASS expectedObject) {

		POJOCLASS responseObject = null;

		responseObject = jpaService.getObject(expectedObject.getId());

		Assert.assertNotNull("the updated " + type + " shouldn't be null", responseObject);
		Assert.assertEquals("the " + type + "s are not equal", expectedObject, responseObject);

		reset();
		compareObjects(expectedObject, responseObject);

		return responseObject;
	}

	/**
	 * creates an object and compares the result via #compareObjects
	 *
	 * @param object
	 * @param expectedObject
	 * @return
	 * @throws Exception
	 */
	public POJOCLASS createObject(final POJOCLASS object, final POJOCLASS expectedObject) throws Exception {

		PROXYPOJOCLASS proxyObject = null;

		try {

			proxyObject = createObject(object);
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong during object creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull(type + " shouldn't be null", proxyObject);
		Assert.assertNotNull(type + " id shouldn't be null", proxyObject.getId());

		BasicJPAServiceTestUtils.LOG.debug("created new " + type + " with id = '" + proxyObject.getId() + "'");

		final POJOCLASS createdObject = proxyObject.getObject();
		final POJOCLASS objectWithUpdates = prepareObjectForUpdate(object, createdObject);

		try {

			proxyObject = jpaService.updateObjectTransactional(objectWithUpdates);
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong during object update.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull(type + " shouldn't be null", proxyObject);

		compareObjects(expectedObject, proxyObject.getObject());

		return proxyObject.getObject();
	}

	public POJOCLASS updateObject(final POJOCLASS updateObject, final POJOCLASS expectedObject) throws Exception {

		PROXYPOJOCLASS proxyUpdatedObject = null;

		try {

			proxyUpdatedObject = jpaService.updateObjectTransactional(updateObject);
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the " + type, false);
		}

		Assert.assertNotNull("the proxy object of " + type + " shouldn't be null", proxyUpdatedObject);

		compareObjects(expectedObject, proxyUpdatedObject.getObject());

		return proxyUpdatedObject.getObject();
	}

	public void deleteObject(final POJOCLASS object) {

		final POJOCLASSIDTYPE objectId = object.getId();

		deleteObject(objectId);
	}

	/**
	 * Prepares a given object with information from an object with updates.
	 * 
	 * @param objectWithUpdates an object with updates
	 * @param object the given object
	 * @return the updated object
	 */
	protected abstract POJOCLASS prepareObjectForUpdate(final POJOCLASS objectWithUpdates, final POJOCLASS object);

	protected PROXYPOJOCLASS createObject(final POJOCLASS object) throws DMPPersistenceException {

		return jpaService.createObjectTransactional();
	}

	/**
	 * Creates a new object of the concrete POJO class.
	 * 
	 * @return a new instance of the concrete POJO class
	 * @throws DMPPersistenceException if something went wrong.
	 */
	protected POJOCLASS createNewObject() throws DMPPersistenceException {

		final POJOCLASS object;

		try {

			object = pojoClass.newInstance();
		} catch (final InstantiationException | IllegalAccessException e) {

			BasicJPAServiceTestUtils.LOG.error("something went wrong while " + pojoClassName + "object creation", e);

			throw new DMPPersistenceException(e.getMessage());
		}

		return object;
	}

	public abstract void reset();
}
