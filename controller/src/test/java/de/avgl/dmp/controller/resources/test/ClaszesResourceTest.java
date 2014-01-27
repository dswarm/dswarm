package de.avgl.dmp.controller.resources.test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.controller.resources.test.utils.ClaszesResourceTestUtils;
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
		
		LOG.debug("start classes uniqueness test");

		Clasz clasz1 = null;

		try {
			
			clasz1 = pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
		} catch (Exception e) {
			
			LOG.error("coudln't create class 1 for uniqueness test");
			
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
		} catch (Exception e) {
			
			LOG.error("couldn't create class 2 for uniqueness test");
			
			Assert.assertTrue(false);
		}
		
		Assert.assertNotNull("class 2 shouldn't be null in uniqueness test", clasz2);
		
		Assert.assertEquals("the classes should be equal", clasz1, clasz2);
		
		cleanUpDB(clasz1);
		
		LOG.debug("end class uniqueness test");
	}
	
	@Override
	public void testPUTObject() throws Exception {

		LOG.debug("start class update test");

		Clasz clasz = null;

		try {
			
			clasz = pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
		} catch (Exception e) {
			
			LOG.error("coudln't create class for update test");
			
			Assert.assertTrue(false);
		}
		
		Assert.assertNotNull("class shouldn't be null in update test", clasz);
		
		//modify class for update
		clasz.setName(clasz.getName() + " update");
		
		String claszJSONString = objectMapper.writeValueAsString(clasz);
		
		Clasz updateClasz = pojoClassResourceTestUtils.updateObject(claszJSONString, clasz);
		
		Assert.assertEquals("the persisted class shoud be equal to the modified class for update", updateClasz, clasz);
		
		ObjectNode claszJSON = objectMapper.readValue(claszJSONString, ObjectNode.class);
		
		Assert.assertNotNull("the class JSON shouldn't be null", claszJSON);

		//uniqueness dosn't allow that
		claszJSON.put("uri", clasz.getUri().replaceAll("http", "https"));
		
		claszJSONString = objectMapper.writeValueAsString(claszJSON);
		
		clasz = objectMapper.readValue(claszJSONString, Clasz.class);
		
		updateClasz = pojoClassResourceTestUtils.updateObject(claszJSONString, clasz);
		
		Assert.assertNotNull("class shouldn't be null", clasz);
		Assert.assertNotNull("class attribute shouldn't be null", updateClasz);
		Assert.assertNotEquals("id should be different, when uri was \"updated\"", updateClasz.getId(), clasz.getId());
		
		Assert.assertNotEquals("uniqueness dosn't allow update of uri", updateClasz.getUri(), clasz.getUri());
		
		//TODO: [@fniederlein] after class persistence adjustments the test have to check if a new class was created
		
		cleanUpDB(clasz);
		
		LOG.debug("end class update test");
	}
}
