package de.avgl.dmp.controller.resources.test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.avgl.dmp.controller.Main;

public class MyResourceTest {

	private HttpServer server;
	private WebTarget target;

	@Before
	public void setUp() throws Exception {
		// start the server
		server = Main.startServer();
		// create the client
		Client c = ClientBuilder.newClient();

		target = c.target(Main.BASE_URI);
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
	}

	/**
	 * Test to see that the message "Got it!" is sent in the response.
	 */
	@Test
	public void testGetItJSON() {
		
		String responseMsg = target.path("myresource").request()
				.accept(MediaType.APPLICATION_JSON).get(String.class);
		Assert.assertEquals("{\"message\":\"Hello World\"}", responseMsg);
	}

	/**
	 * Test to see that the message "Got it!" is sent in the response.
	 */
	@Test
	public void testGetItXML() {
		String responseMsg = target.path("myresource").request()
				.accept(MediaType.APPLICATION_XML).get(String.class);
		Assert.assertEquals(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><testObject><message>Hello World</message></testObject>",
				responseMsg);
	}

	/**
	 * Test to see that the message "Got it!" is sent in the response.
	 */
	@Test
	public void testGetIt() {
		String responseMsg = target.path("myresource").request()
				.accept(MediaType.TEXT_PLAIN).get(String.class);
		Assert.assertEquals("Got it!", responseMsg);
	}
}
