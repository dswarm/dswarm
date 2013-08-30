package de.avgl.dmp.controller.resources.test;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;

public class MyResourceTest extends ResourceTest {

	public MyResourceTest() {
		super("myresource");
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
}
