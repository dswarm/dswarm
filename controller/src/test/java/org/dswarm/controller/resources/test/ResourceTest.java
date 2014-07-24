package org.dswarm.controller.resources.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.EmbeddedServer;
import org.dswarm.controller.ServerConfig;
import org.dswarm.controller.providers.handler.ExceptionHandler;
import org.dswarm.controller.test.GuicedTest;
import org.dswarm.persistence.service.MaintainDBService;

public class ResourceTest extends GuicedTest {

	private static final Logger		LOG		= LoggerFactory.getLogger(ResourceTest.class);

	protected static EmbeddedServer	grizzlyServer;

	protected String				resourceIdentifier;

	protected static final ServerConfig SERVER_CONFIG = new ServerConfig(9998, "127.0.0.1", "/test");

	protected MaintainDBService		maintainDBService;

	public ResourceTest(final String resourceIdentifier) {

		this.resourceIdentifier = resourceIdentifier;
	}

	protected void initObjects() {
		maintainDBService = GuicedTest.injector.getInstance(MaintainDBService.class);
	}

	@BeforeClass
	public static void startUp() throws Exception {

		GuicedTest.startUp();

		ResourceTest.grizzlyServer = new EmbeddedServer(GuicedTest.injector, SERVER_CONFIG);
		ResourceTest.grizzlyServer.start();
	}

	@AfterClass
	public static void tearDown() throws Exception {

		GuicedTest.tearDown();
		ResourceTest.grizzlyServer.stop();
	}

	protected void restartServer() throws Exception {

		GuicedTest.tearDown();
		ResourceTest.grizzlyServer.stop();
		GuicedTest.startUp();
		ResourceTest.grizzlyServer.start(GuicedTest.injector);
	}

	protected Client client() {

		ResourceTest.LOG.debug("create Jersey client for test");

		final ClientBuilder builder = ClientBuilder.newBuilder();

		return builder.register(MultiPartFeature.class).register(ExceptionHandler.class).build();
	}

	protected WebTarget target() {

		WebTarget target = client().target(ResourceTest.grizzlyServer.getBaseUri());

		if (resourceIdentifier != null) {
			target = target.path(resourceIdentifier);
		}

		return target;
	}

	protected WebTarget target(final String... path) {

		WebTarget target = target();

		for (final String p : path) {
			target = target.path(p);
		}

		return target;
	}

	protected String baseUri() {
		return ResourceTest.grizzlyServer.getBaseUri().toString();
	}

	protected String executeCommand(final String command) throws Exception {

		final Process process = Runtime.getRuntime().exec(command);
		final int exitStatus = process.waitFor();

		Assert.assertEquals("exit status should be 0", 0, exitStatus);

		final StringBuilder sb = new StringBuilder();

		final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = reader.readLine();
		while (line != null) {
			sb.append(line);
			line = reader.readLine();
		}

		ResourceTest.LOG.debug("got result from command execution '" + command + "' = '" + sb.toString() + "'");

		return sb.toString();
	}
}
