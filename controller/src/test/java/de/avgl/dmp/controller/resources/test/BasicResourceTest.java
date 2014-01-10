package de.avgl.dmp.controller.resources.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

import de.avgl.dmp.controller.resources.test.utils.BasicResourceTestUtils;
import de.avgl.dmp.persistence.model.DMPObject;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.service.BasicJPAService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 * @param <POJOCLASSRESOURCETESTUTILS>
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public abstract class BasicResourceTest<POJOCLASSRESOURCETESTUTILS extends BasicResourceTestUtils<POJOCLASSPERSISTENCESERVICE, POJOCLASS, POJOCLASSIDTYPE>, POJOCLASSPERSISTENCESERVICE extends BasicJPAService<POJOCLASS, POJOCLASSIDTYPE>, POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, POJOCLASSIDTYPE>
		extends ResourceTest {

	private static final org.apache.log4j.Logger		LOG					= org.apache.log4j.Logger.getLogger(BasicResourceTest.class);

	protected String									objectJSONString	= null;
	protected POJOCLASS									expectedObject		= null;
	protected Set<POJOCLASS>							expectedObjects		= null;
	//protected POJOCLASSIDTYPE							objectId			= null;
	protected String									updateObjectJSONFileName	= null;

	protected final POJOCLASSPERSISTENCESERVICE			persistenceService;

	protected final ObjectMapper						objectMapper		= injector.getInstance(ObjectMapper.class);

	protected final String								objectJSONFileName;

	protected final Class<POJOCLASS>					pojoClass;

	protected final Class<POJOCLASSPERSISTENCESERVICE>	persistenceServiceClass;

	protected final String								pojoClassName;

	protected final POJOCLASSRESOURCETESTUTILS			pojoClassResourceTestUtils;

	public BasicResourceTest(final Class<POJOCLASS> pojoClassArg, final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg,
			final String resourceIdentifier, final String objectJSONFileNameArg, final POJOCLASSRESOURCETESTUTILS pojoClassResourceTestUtilsArg) {

		super(resourceIdentifier);

		pojoClass = pojoClassArg;
		persistenceServiceClass = persistenceServiceClassArg;

		pojoClassName = pojoClass.getSimpleName();

		persistenceService = injector.getInstance(persistenceServiceClass);
		objectJSONFileName = objectJSONFileNameArg;

		pojoClassResourceTestUtils = pojoClassResourceTestUtilsArg;
	}

	@Before
	public void prepare() throws Exception {

		objectJSONString = DMPPersistenceUtil.getResourceAsString(objectJSONFileName);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);
	}

	@Test
	public void testPOSTObjects() throws Exception {

		LOG.debug("start POST " + pojoClassName + "s test");

		final POJOCLASS actualObject = createObjectInternal();

		cleanUpDB(actualObject);

		LOG.debug("end POST " + pojoClassName + "s test");
	}

	@Test
	public void testGETObjects() throws Exception {

		LOG.debug("start GET " + pojoClassName + "s test");

		final POJOCLASS actualObject = createObjectInternal();

		LOG.debug("try to retrieve " + pojoClassName + "s");

		final Response response = target().request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseObjects = response.readEntity(String.class);

		expectedObjects = Sets.newHashSet();
		expectedObjects.add(actualObject);

		pojoClassResourceTestUtils.reset();
		evaluateObjects(expectedObjects, responseObjects);

		cleanUpDB(actualObject);

		LOG.debug("end GET " + pojoClassName + "s");
	}

	@Test
	public void testGETObject() throws Exception {

		LOG.debug("start GET " + pojoClassName + " test");

		final POJOCLASS actualObject = createObjectInternal();

		String idEncoded = null;

		try {

			idEncoded = URLEncoder.encode(actualObject.getId().toString(), "UTF-8");
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

		pojoClassResourceTestUtils.reset();
		compareObjects(actualObject, responseObject);

		cleanUpDB(responseObject);

		LOG.debug("end GET " + pojoClassName);
	}
	
	@Test
	public void testPUTObject() throws Exception {
		LOG.debug("start PUT " + pojoClassName + " test");

		/*POJOCLASSIDTYPE objectId = pojoClassResourceTestUtils.createObject(updateObjectJSONFileName).getId();

		Assert.assertNotNull("couldn't get object id", objectId);
		*/
		
		POJOCLASS actualObject = createObjectInternal();

		String idEncoded = null;

		try {

			idEncoded = URLEncoder.encode(actualObject.getId().toString(), "UTF-8");
		} catch (final UnsupportedEncodingException e) {

			LOG.debug("couldn't encode id", e);

			Assert.assertTrue(false);
		}

		Assert.assertNotNull("the id shouldn't be null", idEncoded);
		
		actualObject = updateObject(idEncoded);
		
		LOG.debug("try to retrieve updated " + pojoClassName + " with id '" + idEncoded + "'");

		final Response response = target(idEncoded).request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseObjectJSON = response.readEntity(String.class);

		Assert.assertNotNull("response " + pojoClassName + " JSON shouldn't be null", responseObjectJSON);

		final POJOCLASS responseObject = objectMapper.readValue(responseObjectJSON, pojoClass);

		Assert.assertNotNull("response " + pojoClassName + " shouldn't be null", responseObject);

		pojoClassResourceTestUtils.reset();
		compareObjects(actualObject, responseObject);

		cleanUpDB(responseObject);

		LOG.debug("end PUT " + pojoClassName);
	}

	protected boolean compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) {

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
	
	protected POJOCLASS updateObject(final String objectId) throws Exception {

		return pojoClassResourceTestUtils.updateObject(objectId, updateObjectJSONFileName);
	}

	protected void cleanUpDB(final POJOCLASS object) {

		pojoClassResourceTestUtils.deleteObject(object);
	}
}
