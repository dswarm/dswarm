package de.avgl.dmp.controller.resources.test.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

import de.avgl.dmp.persistence.model.DMPObject;
import de.avgl.dmp.persistence.service.BasicJPAService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public abstract class BasicResourceTestUtils<POJOCLASSPERSISTENCESERVICE extends BasicJPAService<POJOCLASS, POJOCLASSIDTYPE>, POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, POJOCLASSIDTYPE> {

	private final Class<POJOCLASS>	pojoClass;

	private final String			pojoClassName;

	public BasicResourceTestUtils(final Class<POJOCLASS> pojoClassArg) {

		pojoClass = pojoClassArg;
		pojoClassName = pojoClass.getSimpleName();
	}

	public void compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) {

		Assert.assertNotNull("excepted " + pojoClassName + " shouldn't be null", expectedObject);
		Assert.assertNotNull("actual " + pojoClassName + " shouldn't be null", actualObject);
	}

	public void evaluateObjects(final String objectsJSON, final Set<POJOCLASS> expectedObjects) throws Exception {

		Assert.assertNotNull("the " + pojoClassName + "s JSON string shouldn't be null", objectsJSON);

		final Map<POJOCLASSIDTYPE, POJOCLASS> responseObjects = Maps.newLinkedHashMap();
		final ArrayNode responseObjectsJSONArray = DMPPersistenceUtil.getJSONObjectMapper().readValue(objectsJSON, ArrayNode.class);

		Assert.assertNotNull("response " + pojoClassName + "s JSON array shouldn't be null", responseObjectsJSONArray);

		final Iterator<JsonNode> responseObjectsJSONIter = responseObjectsJSONArray.iterator();

		while (responseObjectsJSONIter.hasNext()) {

			final JsonNode responseObjectJSON = responseObjectsJSONIter.next();

			final POJOCLASS responseObject = DMPPersistenceUtil.getJSONObjectMapper().readValue(((ObjectNode) responseObjectJSON).toString(),
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
}
