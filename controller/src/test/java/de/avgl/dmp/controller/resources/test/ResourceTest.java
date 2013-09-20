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

	protected HttpServer	server;
	protected Client		client;
	protected WebTarget		target;
	protected String		resourceIdentifier;

	public ResourceTest(final String resourceIdentifier) {

		this.resourceIdentifier = resourceIdentifier;
	}

	@Before
	public void setUp() throws Exception {

		createClient();
	}

	protected void createClient() {

		System.out.print("create client");

		final Main main = Main.create(9998);

		// start the server
		server = main.startServer();
		// create the client
		client = JerseyClientBuilder.newBuilder()
		// .register(JacksonJaxbJsonProvider.class)
				.register(MultiPartFeature.class).register(de.avgl.dmp.controller.providers.ExceptionHandler.class).build();

		target = client.target(main.getBaseUri());
	}

	@After
	public void tearDown() throws Exception {

		closeClient();
	}

	protected void closeClient() {

		System.out.print("close client");

		client.close();
		server.stop();
	}

}
