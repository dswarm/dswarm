package de.avgl.dmp.controller.resources.test.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.controller.resources.test.ResourceTest;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.DMPObject;
import de.avgl.dmp.persistence.model.proxy.ProxyDMPObject;
import de.avgl.dmp.persistence.service.BasicJPAService;
import de.avgl.dmp.persistence.service.test.utils.BasicJPAServiceTestUtils;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public abstract class BasicResourceTestUtils<POJOCLASSPERSISTENCESERVICETESTUTILS extends BasicJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS, POJOCLASSIDTYPE>, POJOCLASSPERSISTENCESERVICE extends BasicJPAService<PROXYPOJOCLASS, POJOCLASS, POJOCLASSIDTYPE>, PROXYPOJOCLASS extends ProxyDMPObject<POJOCLASS, POJOCLASSIDTYPE>, POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, POJOCLASSIDTYPE>
		extends ResourceTest {

	private static final org.apache.log4j.Logger				LOG	= org.apache.log4j.Logger.getLogger(BasicResourceTestUtils.class);

	protected final Class<POJOCLASS>							pojoClass;

	protected final String										pojoClassName;

	protected final POJOCLASSPERSISTENCESERVICE					persistenceService;

	protected final POJOCLASSPERSISTENCESERVICETESTUTILS		persistenceServiceTestUtils;

	protected final Class<POJOCLASSPERSISTENCESERVICE>			persistenceServiceClass;

	protected final Class<POJOCLASSPERSISTENCESERVICETESTUTILS>	persistenceServiceTestUtilsClass;

	protected final ObjectMapper								objectMapper;

	public BasicResourceTestUtils(final String resourceIdentifier, final Class<POJOCLASS> pojoClassArg,
			final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg,
			final Class<POJOCLASSPERSISTENCESERVICETESTUTILS> persistenceServiceTestUtilsClassArg) {

		super(resourceIdentifier);

		pojoClass = pojoClassArg;
		pojoClassName = pojoClass.getSimpleName();

		persistenceServiceClass = persistenceServiceClassArg;

		persistenceServiceTestUtilsClass = persistenceServiceTestUtilsClassArg;

		persistenceService = injector.getInstance(persistenceServiceClass);

		persistenceServiceTestUtils = injector.getInstance(persistenceServiceTestUtilsClass);// createNewPersistenceServiceTestUtilsInstance();
		// injector.getInstance(persistenceServiceTestUtilsClass); -> doesn't seem to work right - how can I inject test class
		// from other sub modules?

		objectMapper = injector.getInstance(ObjectMapper.class);
	}

	public POJOCLASSPERSISTENCESERVICETESTUTILS getPersistenceServiceTestUtils() {

		return persistenceServiceTestUtils;
	}

	public void compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) {

		persistenceServiceTestUtils.compareObjects(expectedObject, actualObject);
	}

	public void evaluateObjects(final String objectsJSON, final Set<POJOCLASS> expectedObjects) throws Exception {

		persistenceServiceTestUtils.evaluateObjects(objectsJSON, expectedObjects);
	}

	public void compareObjects(final Set<POJOCLASS> expectedObjects, final Map<POJOCLASSIDTYPE, POJOCLASS> actualObjects) {

		persistenceServiceTestUtils.compareObjects(expectedObjects, actualObjects);
	}

	public List<POJOCLASS> getObjects() {

		return persistenceService.getObjects();
	}

	public POJOCLASS getObjectAndCompare(final POJOCLASS expectedObject) throws Exception {

		final POJOCLASS responseObject = getObject(expectedObject.getId());

		reset();
		compareObjects(expectedObject, responseObject);

		return responseObject;
	}

	public POJOCLASS getObject(final POJOCLASSIDTYPE id) throws Exception {

		String idEncoded = null;

		try {

			idEncoded = URLEncoder.encode(id.toString(), "UTF-8");
		} catch (final UnsupportedEncodingException e) {

			LOG.debug("couldn't encode id", e);

			Assert.assertTrue(false);
		}

		Assert.assertNotNull("the id shouldn't be null", idEncoded);

		LOG.debug("try to retrieve " + pojoClassName + " with id '" + idEncoded + "'");

		final Response response = target(idEncoded).request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseObjectJSON = response.readEntity(String.class);

		Assert.assertNotNull("response " + pojoClassName + " JSON shouldn't be null", responseObjectJSON);

		final POJOCLASS responseObject = objectMapper.readValue(responseObjectJSON, pojoClass);

		Assert.assertNotNull("response " + pojoClassName + " shouldn't be null", responseObject);

		return responseObject;
	}

	public POJOCLASS createObject(final String objectJSONFileName) throws Exception {

		final String objectJSONString = DMPPersistenceUtil.getResourceAsString(objectJSONFileName);
		final POJOCLASS expectedObject = objectMapper.readValue(objectJSONString, pojoClass);

		final POJOCLASS actualObject = createObject(objectJSONString, expectedObject);

		return actualObject;
	}

	public POJOCLASS createObject(final String objectJSONString, final POJOCLASS expectedObject) throws Exception {

		final Response response = executeCreateObject(objectJSONString);

		Assert.assertEquals("201 Created was expected", 201, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final POJOCLASS actualObject = objectMapper.readValue(responseString, pojoClass);

		compareObjects(expectedObject, actualObject);

		return actualObject;
	}

	public Response executeCreateObject(final String objectJSONString) throws Exception {

		return target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(objectJSONString));
	}

	public POJOCLASS updateObject(final POJOCLASS persistedObject, final String updateObjectJSONFileName) throws Exception {

		String updateObjectJSONString = DMPPersistenceUtil.getResourceAsString(updateObjectJSONFileName);

		final ObjectNode objectJSON = objectMapper.readValue(updateObjectJSONString, ObjectNode.class);
		objectJSON.put("id", String.valueOf(persistedObject.getId()));

		updateObjectJSONString = objectMapper.writeValueAsString(objectJSON);

		final POJOCLASS expectedObject = objectMapper.readValue(updateObjectJSONString, pojoClass);

		final POJOCLASS updatedObject = updateObject(updateObjectJSONString, expectedObject);

		return updatedObject;
	}

	public POJOCLASS updateObject(final String updateObjectJSONString, final POJOCLASS expectedObject) throws Exception {

		POJOCLASSIDTYPE objectId = objectMapper.readValue(updateObjectJSONString, pojoClass).getId();

		Assert.assertEquals("the id of the updeted object should be equal", objectId, expectedObject.getId());

		final Response response = target(String.valueOf(objectId)).request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.put(Entity.json(updateObjectJSONString));

		Assert.assertEquals("200 Updated was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final POJOCLASS updatedObject = objectMapper.readValue(responseString, pojoClass);

		compareObjects(expectedObject, updatedObject);

		return updatedObject;
	}

	public void deleteObject(final POJOCLASS object) {

		if (object != null) {

			// clean-up DB

			final POJOCLASSIDTYPE objectId = object.getId();

			final POJOCLASS toBeDeletedObject = persistenceService.getObject(objectId);

			if (toBeDeletedObject == null) {

				LOG.info(pojoClassName + " with id '" + objectId + "' has already been deleted from DB or never existed there");

				return;
			}

			persistenceService.deleteObject(objectId);

			final POJOCLASS deletedObject = persistenceService.getObject(objectId);

			Assert.assertNull("the deleted " + pojoClassName + " should be null", deletedObject);
		}
	}

	public void deleteObjectViaPersistenceServiceTestUtils(final POJOCLASS object) {

		if(object != null) {

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

			LOG.error("something went wrong while " + persistenceServiceTestUtilsClass.getSimpleName() + "object creation", e);

			return null;
		}

		return object;
	}
}
