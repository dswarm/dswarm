package de.avgl.dmp.controller.resources.test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.After;
import org.junit.Before;

import de.avgl.dmp.controller.Main;

public class ResourceTest {

	private static final org.apache.log4j.Logger	LOG		= org.apache.log4j.Logger.getLogger(ResourceTest.class);

	protected HttpServer							server;
	protected Client								client;
	protected WebTarget								target;
	protected String								resourceIdentifier;
	protected static final int						port	= 9998;
	protected String								baseURI	= null;

	public ResourceTest(final String resourceIdentifier) {

		this.resourceIdentifier = resourceIdentifier;
	}

	@Before
	public void setUp() throws Exception {

		createClient();
	}

	protected void createClient() {

		LOG.debug("create Jersey client for test");

		final Main main = Main.create(port);

		// start the server
		server = main.startServer();
		// create the client
		client = JerseyClientBuilder.newBuilder()
		// .register(JacksonJaxbJsonProvider.class)
				.register(MultiPartFeature.class).register(de.avgl.dmp.controller.providers.ExceptionHandler.class).build();

		target = client.target(main.getBaseUri());
		baseURI = main.getBaseUri();
	}

	@After
	public void tearDown() throws Exception {

		closeClient();
	}

	protected void closeClient() {

		LOG.debug("close Jersey client of test");

		client.close();
		server.stop();
	}

}
