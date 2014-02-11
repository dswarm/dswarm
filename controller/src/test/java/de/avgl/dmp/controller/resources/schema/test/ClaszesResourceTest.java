package de.avgl.dmp.controller.resources.schema.test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.controller.resources.schema.test.utils.ClaszesResourceTestUtils;
import de.avgl.dmp.controller.resources.test.BasicResourceTest;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyClasz;
import de.avgl.dmp.persistence.service.schema.ClaszService;

public class ClaszesResourceTest extends BasicResourceTest<ClaszesResourceTestUtils, ClaszService, ProxyClasz, Clasz, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ClaszesResourceTest.class);

	public ClaszesResourceTest() {

		super(Clasz.class, ClaszService.class, "classes", "clasz.json", new ClaszesResourceTestUtils());

	}

	@Test
	public void testUniquenessOfClasses() {

		ClaszesResourceTest.LOG.debug("start classes uniqueness test");

		Clasz clasz1 = null;

		try {

			clasz1 = pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
		} catch (final Exception e) {

			ClaszesResourceTest.LOG.error("coudln't create class 1 for uniqueness test");

			Assert.assertTrue(false);
		}

		Assert.assertNotNull("class 1 shouldn't be null in uniqueness test", clasz1);

		Clasz clasz2 = null;

		try {

			final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
					.post(Entity.json(objectJSONString));

			Assert.assertEquals("200 OK was expected", 200, response.getStatus());

			final String responseString = response.readEntity(String.class);

			Assert.assertNotNull("the response JSON shouldn't be null", responseString);

			clasz2 = objectMapper.readValue(responseString, pojoClass);

			compareObjects(expectedObject, clasz2);
		} catch (final Exception e) {

			ClaszesResourceTest.LOG.error("couldn't create class 2 for uniqueness test");

			Assert.assertTrue(false);
		}

		Assert.assertNotNull("class 2 shouldn't be null in uniqueness test", clasz2);

		Assert.assertEquals("the classes should be equal", clasz1, clasz2);

		cleanUpDB(clasz1);

		ClaszesResourceTest.LOG.debug("end class uniqueness test");
	}

	@Override
	public void testPUTObject() throws Exception {

		ClaszesResourceTest.LOG.debug("start class update test with uri manipulation");

		Clasz clasz = null;

		try {

			clasz = pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
		} catch (final Exception e) {

			ClaszesResourceTest.LOG.error("coudln't create class for update test");

			Assert.assertTrue(false);
		}

		Assert.assertNotNull("class shouldn't be null in update test", clasz);

		// modify class for update
		clasz.setName(clasz.getName() + " update");

		String claszJSONString = objectMapper.writeValueAsString(clasz);

		Clasz updateClasz = pojoClassResourceTestUtils.updateObject(claszJSONString, clasz);

		Assert.assertEquals("the persisted class shoud be equal to the modified class for update", updateClasz, clasz);

		final ObjectNode claszJSON = objectMapper.readValue(claszJSONString, ObjectNode.class);

		Assert.assertNotNull("the class JSON shouldn't be null", claszJSON);

		// uniqueness dosn't allow that
		claszJSON.put("uri", clasz.getUri().replaceAll("http", "https"));

		claszJSONString = objectMapper.writeValueAsString(claszJSON);

		final Clasz modifiedClaszFromJSON = objectMapper.readValue(claszJSONString, Clasz.class);

		Clasz modifiedClasz = null;

		try {

			modifiedClasz = pojoClassResourceTestUtils.createObject(claszJSONString, modifiedClaszFromJSON);
		} catch (final Exception e) {

			ClaszesResourceTest.LOG.error("couldn't create modified class for update test");

			Assert.assertTrue(false);
		}

		updateClasz = pojoClassResourceTestUtils.updateObject(claszJSONString, modifiedClaszFromJSON);

		Assert.assertNotNull("class shouldn't be null", clasz);
		Assert.assertNotNull("class attribute shouldn't be null", updateClasz);
		Assert.assertEquals("ids of the modified class should be equal", modifiedClasz.getId(), updateClasz.getId());
		Assert.assertNotEquals("id should be different, when uri was \"updated\"", updateClasz.getId(), clasz.getId());

		Assert.assertNotEquals("id should be different, when uri was \"updated\" (uniqueness dosn't allow update of uri)", updateClasz.getId(),
				clasz.getId());

		Assert.assertEquals("uri's should be equal", updateClasz.getUri(), modifiedClasz.getUri());
		Assert.assertNotEquals("uniqueness dosn't allow update of uri", updateClasz.getUri(), clasz.getUri());

		cleanUpDB(clasz);
		cleanUpDB(updateClasz);

		ClaszesResourceTest.LOG.debug("end class update test with uri manipulation");
	}

	@Test
	public void testPUTObjectWNonExistingURI() throws Exception {

		ClaszesResourceTest.LOG.debug("start class update test with non-existing uri (manipulation)");

		Clasz clasz = null;

		try {

			clasz = pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
		} catch (final Exception e) {

			ClaszesResourceTest.LOG.error("coudln't create clasz for update test");

			Assert.assertTrue(false);
		}

		Assert.assertNotNull("clasz shouldn't be null in update test", clasz);

		// modify attribute for update
		clasz.setName(clasz.getName() + " update");

		String claszJSONString = objectMapper.writeValueAsString(clasz);

		final ObjectNode claszJSON = objectMapper.readValue(claszJSONString, ObjectNode.class);

		Assert.assertNotNull("the clasz JSON shouldn't be null", claszJSON);

		// uniqueness dosn't allow that
		claszJSON.put("uri", clasz.getUri().replaceAll("http", "https"));

		claszJSONString = objectMapper.writeValueAsString(claszJSON);

		final Long objectId = objectMapper.readValue(claszJSONString, pojoClass).getId();

		Assert.assertEquals("the id of the updated object should be equal", objectId, clasz.getId());

		final Response response = target(String.valueOf(objectId)).request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.put(Entity.json(claszJSONString));

		Assert.assertEquals("404 NOT FOUND was expected, i.e., no class with the given URI exists in the DB", 404, response.getStatus());

		cleanUpDB(clasz);

		ClaszesResourceTest.LOG.debug("end class update test with non-existing uri (manipulation)");
	}
}
