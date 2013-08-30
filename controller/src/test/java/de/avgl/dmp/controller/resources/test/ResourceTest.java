package de.avgl.dmp.controller.resources.test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.After;
import org.junit.Before;

import de.avgl.dmp.controller.Main;

public class ResourceTest {

	protected HttpServer	server;
	protected Client		client;
	protected WebTarget		target;
	protected String		resourceIdentifier;

	public ResourceTest(final String resourceIdentifier) {

		this.resourceIdentifier = resourceIdentifier;
	}

	@Before
	public void setUp() throws Exception {
		// start the server
		server = Main.startServer();
		// create the client
		client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();

		target = client.target(Main.BASE_URI);
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
	}

}
