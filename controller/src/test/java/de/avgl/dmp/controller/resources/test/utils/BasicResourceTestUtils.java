package de.avgl.dmp.controller.resources.test.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

import de.avgl.dmp.controller.resources.test.ResourceTest;
import de.avgl.dmp.persistence.model.DMPObject;
import de.avgl.dmp.persistence.service.BasicJPAService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public abstract class BasicResourceTestUtils<POJOCLASSPERSISTENCESERVICE extends BasicJPAService<POJOCLASS, POJOCLASSIDTYPE>, POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, POJOCLASSIDTYPE> extends ResourceTest {

	private static final org.apache.log4j.Logger		LOG					= org.apache.log4j.Logger.getLogger(BasicResourceTestUtils.class);
	
	protected final Class<POJOCLASS>	pojoClass;

	protected final String			pojoClassName;
	
	protected final POJOCLASSPERSISTENCESERVICE			persistenceService;
	
	protected final Class<POJOCLASSPERSISTENCESERVICE>	persistenceServiceClass;
	
	protected final ObjectMapper objectMapper;

	public BasicResourceTestUtils(final String resourceIdentifier, final Class<POJOCLASS> pojoClassArg, final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg) {

		super(resourceIdentifier);
		
		pojoClass = pojoClassArg;
		pojoClassName = pojoClass.getSimpleName();
		
		persistenceServiceClass = persistenceServiceClassArg;
		
		persistenceService = injector.getInstance(persistenceServiceClass);
		
		objectMapper = injector.getInstance(ObjectMapper.class);
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

			final POJOCLASS responseObject = objectMapper.readValue(((ObjectNode) responseObjectJSON).toString(),
					pojoClass);

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

		return persistenceService.getObjects();
	}

	public POJOCLASS createObject(final String objectJSONFileName) throws Exception {
		
		final String objectJSONString = DMPPersistenceUtil.getResourceAsString(objectJSONFileName);
		final POJOCLASS expectedObject = objectMapper.readValue(objectJSONString, pojoClass);

		final POJOCLASS actualObject = createObject(objectJSONString, expectedObject);
		
		return actualObject;
	}
	
	public POJOCLASS createObject(final String objectJSONString, final POJOCLASS expectedObject) throws Exception {

		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(objectJSONString));

		Assert.assertEquals("201 Created was expected", 201, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final POJOCLASS actualObject = objectMapper.readValue(responseString, pojoClass);

		compareObjects(expectedObject, actualObject);

		return actualObject;
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
			
			if(toBeDeletedObject == null) {
				
				LOG.info(pojoClassName + " with id '" + objectId + "' has already been deleted from DB or never existed there");
				
				return;
			}

			persistenceService.deleteObject(objectId);

			final POJOCLASS deletedObject = persistenceService.getObject(objectId);

			Assert.assertNull("the deleted " + pojoClassName + " should be null", deletedObject);
		}
	}
	
	public abstract void reset();
}
