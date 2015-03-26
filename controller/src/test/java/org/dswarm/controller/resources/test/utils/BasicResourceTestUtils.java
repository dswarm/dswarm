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
package org.dswarm.controller.resources.test.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONException;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.resources.test.ResourceTest;
import org.dswarm.controller.test.GuicedTest;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.proxy.ProxyDMPObject;
import org.dswarm.persistence.service.BasicJPAService;
import org.dswarm.persistence.service.test.utils.BasicJPAServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public abstract class BasicResourceTestUtils<POJOCLASSPERSISTENCESERVICETESTUTILS extends BasicJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS>, POJOCLASSPERSISTENCESERVICE extends BasicJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyDMPObject<POJOCLASS>, POJOCLASS extends DMPObject>
		extends ResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(BasicResourceTestUtils.class);

	protected final Class<POJOCLASS> pojoClass;

	protected final String pojoClassName;

	protected final POJOCLASSPERSISTENCESERVICE persistenceService;

	protected final POJOCLASSPERSISTENCESERVICETESTUTILS persistenceServiceTestUtils;

	protected final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClass;

	protected final Class<POJOCLASSPERSISTENCESERVICETESTUTILS> persistenceServiceTestUtilsClass;

	protected final ObjectMapper objectMapper;

	public BasicResourceTestUtils(final String resourceIdentifier, final Class<POJOCLASS> pojoClassArg,
			final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg,
			final Class<POJOCLASSPERSISTENCESERVICETESTUTILS> persistenceServiceTestUtilsClassArg) {

		super(resourceIdentifier);

		pojoClass = pojoClassArg;
		pojoClassName = pojoClass.getSimpleName();

		persistenceServiceClass = persistenceServiceClassArg;

		persistenceServiceTestUtilsClass = persistenceServiceTestUtilsClassArg;

		persistenceService = GuicedTest.injector.getInstance(persistenceServiceClass);

		persistenceServiceTestUtils = GuicedTest.injector
				.getInstance(persistenceServiceTestUtilsClass);// createNewPersistenceServiceTestUtilsInstance();
		// injector.getInstance(persistenceServiceTestUtilsClass); -> doesn't seem to work right - how can I inject test class
		// from other sub modules?

		objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);
	}

	public POJOCLASSPERSISTENCESERVICETESTUTILS getPersistenceServiceTestUtils() {

		return persistenceServiceTestUtils;
	}

	public void compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) throws JsonProcessingException, JSONException {

		persistenceServiceTestUtils.compareObjects(expectedObject, actualObject);
	}

	public void evaluateObjects(final String objectsJSON, final Set<POJOCLASS> expectedObjects) throws Exception {

		persistenceServiceTestUtils.evaluateObjects(objectsJSON, expectedObjects);
	}

	public void compareObjects(final Set<POJOCLASS> expectedObjects, final Map<String, POJOCLASS> actualObjects)
			throws JsonProcessingException, JSONException {

		persistenceServiceTestUtils.compareObjects(expectedObjects, actualObjects);
	}

	public List<POJOCLASS> getObjects() {

		return persistenceService.getObjects();
	}

	public POJOCLASS getObjectAndCompare(final POJOCLASS expectedObject) throws Exception {

		final POJOCLASS responseObject = getObject(expectedObject.getUuid());

		reset();
		compareObjects(expectedObject, responseObject);

		return responseObject;
	}

	public POJOCLASS getObject(final String uuid) throws Exception {

		String idEncoded = null;

		try {

			idEncoded = URLEncoder.encode(uuid, "UTF-8");
		} catch (final UnsupportedEncodingException e) {

			BasicResourceTestUtils.LOG.debug("couldn't encode uuid", e);

			Assert.assertTrue(false);
		}

		Assert.assertNotNull("the uuid shouldn't be null", idEncoded);

		BasicResourceTestUtils.LOG.debug("try to retrieve " + pojoClassName + " with uuid '" + idEncoded + "'");

		final Response response = target(idEncoded).request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseObjectJSON = response.readEntity(String.class);

		Assert.assertNotNull("response " + pojoClassName + " JSON shouldn't be null", responseObjectJSON);

		final POJOCLASS responseObject = objectMapper.readValue(responseObjectJSON, pojoClass);

		Assert.assertNotNull("response " + pojoClassName + " shouldn't be null", responseObject);

		return responseObject;
	}

	/**
	 * Load file containing JSON string and call {@link #createObject(String, DMPObject)} to create it in db.
	 *
	 * @param objectJSONFileName name of file containing JSON string of object to be created
	 * @return the object returned by {@link #createObject(String, DMPObject)}.
	 * @throws Exception
	 */
	public POJOCLASS createObject(final String objectJSONFileName) throws Exception {

		final String objectJSONString = DMPPersistenceUtil.getResourceAsString(objectJSONFileName);
		final POJOCLASS expectedObject = objectMapper.readValue(objectJSONString, pojoClass);

		final POJOCLASS actualObject = createObject(objectJSONString, expectedObject);

		return actualObject;
	}

	public POJOCLASS createObject(final String objectJSONString, final POJOCLASS expectedObject) throws Exception {

		final POJOCLASS actualObject = createObjectWithoutComparison(objectJSONString);
		compareObjects(expectedObject, actualObject);

		return actualObject;
	}

	/**
	 * Creates the object in db and asserts the response status is '201 created' but does not compare the response with the JSON
	 * string.
	 *
	 * @param objectJSONString the JSON string of the object to be created
	 * @return the actual object as created in db, never null.
	 * @throws Exception
	 */
	public POJOCLASS createObjectWithoutComparison(final String objectJSONString) throws Exception {

		final Response response = executeCreateObject(objectJSONString);

		Assert.assertEquals("201 Created was expected", 201, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		return objectMapper.readValue(responseString, pojoClass);
	}

	public POJOCLASS updateObject(final POJOCLASS persistedObject, final String updateObjectJSONFileName) throws Exception {

		String updateObjectJSONString = DMPPersistenceUtil.getResourceAsString(updateObjectJSONFileName);

		final ObjectNode objectJSON = objectMapper.readValue(updateObjectJSONString, ObjectNode.class);
		objectJSON.put("uuid", String.valueOf(persistedObject.getUuid()));

		updateObjectJSONString = objectMapper.writeValueAsString(objectJSON);

		final POJOCLASS expectedObject = objectMapper.readValue(updateObjectJSONString, pojoClass);

		return updateObject(updateObjectJSONString, expectedObject);
	}

	public Response executeCreateObject(final String objectJSONString) throws Exception {

		return target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(objectJSONString));
	}

	public POJOCLASS updateObject(final String updateObjectJSONString, final POJOCLASS expectedObject) throws Exception {

		final String objectUuid = objectMapper.readValue(updateObjectJSONString, pojoClass).getUuid();

		Assert.assertEquals("the ids of the updated object should be equal", expectedObject.getUuid(), objectUuid);

		final POJOCLASS updatedObject = updateObjectWithoutComparison(updateObjectJSONString, objectUuid);

		compareObjects(expectedObject, updatedObject);

		return updatedObject;
	}

	public POJOCLASS updateObjectWithoutComparison(final POJOCLASS updateObject) throws Exception {

		final String updateObjectJSONString = objectMapper.writeValueAsString(updateObject);
		final String objectUuid = updateObject.getUuid();

		return updateObjectWithoutComparison(updateObjectJSONString, objectUuid);
	}

	private POJOCLASS updateObjectWithoutComparison(final String updateObjectJSONString, final String objectUuid) throws Exception {

		final Response response = target(String.valueOf(objectUuid)).request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.put(Entity.json(updateObjectJSONString));

		Assert.assertEquals("200 Updated was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		return objectMapper.readValue(responseString, pojoClass);
	}

	public void deleteObject(final POJOCLASS object) {

		if (object != null) {

			// clean-up DB

			final String objectUuid = object.getUuid();

			final POJOCLASS toBeDeletedObject = persistenceService.getObject(objectUuid);

			if (toBeDeletedObject == null) {

				BasicResourceTestUtils.LOG
						.info(pojoClassName + " with id '" + objectUuid + "' has already been deleted from DB or never existed there");

				return;
			}

			persistenceService.deleteObject(objectUuid);

			final POJOCLASS deletedObject = persistenceService.getObject(objectUuid);

			Assert.assertNull("the deleted " + pojoClassName + " should be null", deletedObject);
		}
	}

	public void deleteObjectViaPersistenceServiceTestUtils(final POJOCLASS object) {

		if (object != null) {

			persistenceServiceTestUtils.deleteObject(object);
		}
	}

	public void reset() {

		persistenceServiceTestUtils.reset();
	}

	/**
	 * Creates a new object of the concrete POJO class.
	 *
	 * @return a new instance of the concrete POJO class
	 * @throws DMPPersistenceException if something went wrong.
	 */
	private POJOCLASSPERSISTENCESERVICETESTUTILS createNewPersistenceServiceTestUtilsInstance() {

		final POJOCLASSPERSISTENCESERVICETESTUTILS object;

		try {

			object = persistenceServiceTestUtilsClass.newInstance();
		} catch (final InstantiationException | IllegalAccessException e) {

			BasicResourceTestUtils.LOG.error("something went wrong while " + persistenceServiceTestUtilsClass.getSimpleName() + "object creation", e);

			return null;
		}

		return object;
	}
}
