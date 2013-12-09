package de.avgl.dmp.controller.resources.test;

import java.io.IOException;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.DMPObject;
import de.avgl.dmp.persistence.service.BasicJPAService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * 
 * @author tgaengler
 *
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public abstract class BasicResourceTest<POJOCLASSPERSISTENCESERVICE extends BasicJPAService<POJOCLASS, POJOCLASSIDTYPE>, POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, POJOCLASSIDTYPE>
		extends ResourceTest {

	private static final org.apache.log4j.Logger		LOG					= org.apache.log4j.Logger.getLogger(BasicResourceTest.class);

	protected String									objectJSONString	= null;
	protected POJOCLASS									expectedObject		= null;
	private Set<POJOCLASS>								expectedObjects		= null;

	private final POJOCLASSPERSISTENCESERVICE			persistenceService;

	private final ObjectMapper							objectMapper		= injector.getInstance(ObjectMapper.class);

	private final String								objectJSONFileName;

	private final Class<POJOCLASS>						pojoClass;

	private final Class<POJOCLASSPERSISTENCESERVICE>	persistenceServiceClass;

	private final String								pojoClassName;

	public BasicResourceTest(final Class<POJOCLASS> pojoClassArg, final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg,
			final String resourceIdentifier, final String objectJSONFileNameArg) {

		super(resourceIdentifier);

		pojoClass = pojoClassArg;
		persistenceServiceClass = persistenceServiceClassArg;

		pojoClassName = pojoClass.getSimpleName();

		persistenceService = injector.getInstance(persistenceServiceClass);
		objectJSONFileName = objectJSONFileNameArg;
	}

	@Before
	public void prepare() throws IOException {

		objectJSONString = DMPPersistenceUtil.getResourceAsString(objectJSONFileName);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);
	}

	@Test
	public void testPOSTConfigurations() throws Exception {

		final POJOCLASS actualObject = createObjectInternal();

		cleanUpDB(actualObject);
	}

	@Test
	public void testGETObjects() throws Exception {

		final POJOCLASS actualObject = createObjectInternal();

		LOG.debug("try to retrieve " + pojoClassName + "s");

		final Response response = target().request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseObjects = response.readEntity(String.class);

		expectedObjects = Sets.newHashSet();
		expectedObjects.add(actualObject);

		evaluateObjects(expectedObjects, responseObjects);

		cleanUpDB(actualObject);
	}

	@Test
	public void testGETObject() throws Exception {

		final POJOCLASS actualObject = createObjectInternal();

		LOG.debug("try to retrieve " + pojoClassName + "s");

		final Response response = target(String.valueOf(actualObject.getId())).request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseObjectJSON = response.readEntity(String.class);

		Assert.assertNotNull("response " + pojoClassName + " JSON shouldn't be null", responseObjectJSON);

		final POJOCLASS responseObject = objectMapper.readValue(responseObjectJSON, pojoClass);

		Assert.assertNotNull("response " + pojoClassName + " shouldn't be null", responseObject);

		compareObjects(actualObject, responseObject);

		cleanUpDB(responseObject);
	}

	protected abstract boolean compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject);

	protected abstract boolean evaluateObjects(final Set<POJOCLASS> expectedObjects, final String actualObjects) throws Exception;

	private POJOCLASS createObjectInternal() throws Exception {

		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(objectJSONString));

		Assert.assertEquals("201 Created was expected", 201, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final POJOCLASS actualObject = objectMapper.readValue(responseString, pojoClass);

		compareObjects(expectedObject, actualObject);

		return actualObject;
	}

	private void cleanUpDB(final POJOCLASS object) {

		// clean-up DB

		final POJOCLASSIDTYPE objectId = object.getId();

		persistenceService.deleteObject(objectId);

		final POJOCLASS deletedObject = persistenceService.getObject(objectId);

		Assert.assertNull("the deleted " + pojoClassName + " should be null", deletedObject);
	}
}
