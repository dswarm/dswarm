package de.avgl.dmp.controller.resources.test;

import java.io.IOException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.client.ClientResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import de.avgl.dmp.persistence.model.TestObject;

public class MyResourceTest extends ResourceTest {
	
	private ObjectNode	testObjectJSON	= null;

	public MyResourceTest() {
		super("myresource");
	}
	
	@Before
	public void prepare() {

		final TestObject testObject = new TestObject();

		final ObjectMapper mapper = new ObjectMapper();

		final JaxbAnnotationModule module = new JaxbAnnotationModule();
		// configure as necessary
		mapper.registerModule(module);

		String str = null;

		try {
			str = mapper.writeValueAsString(testObject);
		} catch (JsonProcessingException e) {

			e.printStackTrace();

			Assert.assertTrue("something went wrong", false);
		}

		try {
			testObjectJSON= mapper.readValue(str, ObjectNode.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Assert.assertTrue(true);
	}

	/**
	 * Test to see that the message "Got it!" is sent in the response.
	 */
	@Test
	public void testGetItJSON() {

		String responseMsg = target.path(resourceIdentifier).request().accept(MediaType.APPLICATION_JSON).get(String.class);
		Assert.assertEquals("{\"message\":\"Hello World\"}", responseMsg);
	}

	/**
	 * Test to see that the message "Got it!" is sent in the response.
	 */
	@Test
	public void testGetItXML() {
		String responseMsg = target.path(resourceIdentifier).request().accept(MediaType.APPLICATION_XML).get(String.class);
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><testObject><message>Hello World</message></testObject>",
				responseMsg);
	}

	/**
	 * Test to see that the message "Got it!" is sent in the response.
	 */
	@Test
	public void testGetIt() {
		String responseMsg = target.path(resourceIdentifier).request().accept(MediaType.TEXT_PLAIN).get(String.class);
		Assert.assertEquals("Got it!", responseMsg);
	}
	
	/**
	 * test post of transformations
	 */
	@Test
	public void testPOSTJSON() {

		// String responseMsg = target.path(resourceIdentifier).request()
		// .accept(MediaType.APPLICATION_JSON).post(Entity.json(transformationJSON.toString()), String.class);
		//
		// Assert.assertEquals("{\"message\":\"Hello World\"}", responseMsg);

		// POST method
		ClientResponse response = target.path(resourceIdentifier + "/test").request(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(testObjectJSON.toString()), ClientResponse.class);

		// check response status code
		if (response.getStatus() != 200) {
			
			throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		}
		
		String result = null;
		
		try {
			result = IOUtils.toString(response.getEntityStream(), "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Assert.assertEquals("wrong", "bla", result);
	}
}
