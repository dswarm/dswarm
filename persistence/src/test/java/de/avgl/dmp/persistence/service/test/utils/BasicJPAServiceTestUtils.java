package de.avgl.dmp.persistence.service.test.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.DMPObject;
import de.avgl.dmp.persistence.model.proxy.ProxyDMPObject;
import de.avgl.dmp.persistence.service.BasicJPAService;
import de.avgl.dmp.persistence.service.test.BasicJPAServiceTest;

public abstract class BasicJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE extends BasicJPAService<PROXYPOJOCLASS, POJOCLASS, POJOCLASSIDTYPE>, PROXYPOJOCLASS extends ProxyDMPObject<POJOCLASS, POJOCLASSIDTYPE>, POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, POJOCLASSIDTYPE>
		extends BasicJPAServiceTest<PROXYPOJOCLASS, POJOCLASS, POJOCLASSPERSISTENCESERVICE, POJOCLASSIDTYPE> {

	private static final org.apache.log4j.Logger		LOG	= org.apache.log4j.Logger.getLogger(BasicJPAServiceTestUtils.class);

	protected final Class<POJOCLASS>					pojoClass;

	protected final String								pojoClassName;

	protected final Class<POJOCLASSPERSISTENCESERVICE>	persistenceServiceClass;

	protected final ObjectMapper						objectMapper;

	public BasicJPAServiceTestUtils(final Class<POJOCLASS> pojoClassArg, final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg) {

		super(pojoClassArg.getSimpleName(), persistenceServiceClassArg);

		pojoClass = pojoClassArg;
		pojoClassName = pojoClass.getSimpleName();

		persistenceServiceClass = persistenceServiceClassArg;

		objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);
	}

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

			final POJOCLASS responseObject = objectMapper.readValue(((ObjectNode) responseObjectJSON).toString(), pojoClass);

			responseObjects.put(responseObject.getId(), responseObject);
		}

		compareObjects(expectedObjects, responseObjects);
	}

	public void compareObjects(final Set<POJOCLASS> expectedObjects, final Map<POJOCLASSIDTYPE, POJOCLASS> actualObjects) {

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

	public POJOCLASS getObject(final POJOCLASS expectedObject) {

		POJOCLASS responseObject = null;

		responseObject = jpaService.getObject(expectedObject.getId());

		Assert.assertNotNull("the updated " + type + " shouldn't be null", responseObject);
		Assert.assertEquals("the " + type + "s are not equal", expectedObject, responseObject);

		reset();
		compareObjects(expectedObject, responseObject);

		return responseObject;
	}

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

		POJOCLASS createdObject = proxyObject.getObject();
		POJOCLASS objectWithUpdates = prepareObjectForUpdate(object, createdObject);

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

			object =  pojoClass.newInstance();
		} catch (final InstantiationException | IllegalAccessException e) {

			BasicJPAServiceTestUtils.LOG.error("something went wrong while " + pojoClassName + "object creation", e);

			throw new DMPPersistenceException(e.getMessage());
		}

		return object;
	}

	public abstract void reset();
}
