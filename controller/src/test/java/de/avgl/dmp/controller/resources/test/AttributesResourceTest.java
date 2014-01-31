package de.avgl.dmp.controller.resources.test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.avgl.dmp.controller.resources.test.utils.AttributesResourceTestUtils;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttribute;
import de.avgl.dmp.persistence.service.schema.AttributeService;

public class AttributesResourceTest extends BasicResourceTest<AttributesResourceTestUtils, AttributeService, ProxyAttribute, Attribute, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributesResourceTest.class);

	public AttributesResourceTest() {

		super(Attribute.class, AttributeService.class, "attributes", "attribute1.json", new AttributesResourceTestUtils());
	}

	@Test
	public void testUniquenessOfAttributes() {
		
		LOG.debug("start attribute uniqueness test");

		Attribute attribute1 = null;

		try {
			
			attribute1 = pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
		} catch (Exception e) {
			
			LOG.error("coudln't create attribute 1 for uniqueness test");
			
			Assert.assertTrue(false);
		}
		
		Assert.assertNotNull("attribute 1 shouldn't be null in uniqueness test", attribute1);

		Attribute attribute2 = null;

		try {
			
			final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
					.post(Entity.json(objectJSONString));

			Assert.assertEquals("200 OK was expected", 200, response.getStatus());

			final String responseString = response.readEntity(String.class);

			Assert.assertNotNull("the response JSON shouldn't be null", responseString);

			attribute2 = objectMapper.readValue(responseString, pojoClass);

			compareObjects(expectedObject, attribute2);
			
		} catch (Exception e) {
			
			LOG.error("coudln't create attribute 2 for uniqueness test");
			
			Assert.assertTrue(false);
		}
		
		Assert.assertNotNull("attribute 2 shouldn't be null in uniqueness test", attribute2);
		
		Assert.assertEquals("the attributes should be equal", attribute1, attribute2);
		
		cleanUpDB(attribute1);
		
		LOG.debug("end attribute uniqueness test");
	}
	
	/**
	 * note: this operation is not supported right now
	 */
	@Ignore
	@Override
	public void testPUTObject() throws Exception {

		//super.testPUTObject();
		
		// TODO: [@fniederlein] implement test
	}
}
