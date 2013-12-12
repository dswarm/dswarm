package de.avgl.dmp.controller.resources.test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import de.avgl.dmp.controller.EmbeddedServer;
import de.avgl.dmp.controller.servlet.DMPInjector;

public class ResourceTest extends GuicedTest {

	private static final org.apache.log4j.Logger	LOG		= org.apache.log4j.Logger.getLogger(ResourceTest.class);

	protected static EmbeddedServer                 grizzlyServer;

	protected String								resourceIdentifier;
	protected static final int						port	= 9998;

	public ResourceTest(final String resourceIdentifier) {

		this.resourceIdentifier = resourceIdentifier;
	}

	@BeforeClass
	public static void startUp() throws Exception {
		GuicedTest.startUp();

		System.setProperty(EmbeddedServer.CONTEXT_PATH_PROPERTY, "/test");
		System.setProperty(EmbeddedServer.HTTP_HOST_PROPERTY, "127.0.0.1");
		System.setProperty(EmbeddedServer.HTTP_PORT_PROPERTY, String.valueOf(port));

		DMPInjector.injector = injector;

		grizzlyServer = new EmbeddedServer();
		grizzlyServer.start();
	}

	@AfterClass
	public static void tearDown() throws Exception {

		GuicedTest.tearDown();
		grizzlyServer.stop();
	}

	protected Client client() {

		LOG.debug("create Jersey client for test");

		final ClientBuilder builder = ClientBuilder.newBuilder();

		return builder
				.register(MultiPartFeature.class)
				.register(de.avgl.dmp.controller.providers.ExceptionHandler.class)
				.build();
	}

	protected WebTarget target() {

		WebTarget target = client().target(grizzlyServer.getBaseUri());

		if (resourceIdentifier != null) {
			target = target.path(resourceIdentifier);
		}

		return target;
	}

	protected WebTarget target(String... path) {

		WebTarget target = target();

		for (String p : path) {
			target = target.path(p);
		}

		return target;
	}

	protected String baseUri() {
		return grizzlyServer.getBaseUri().toString();
	}
}
