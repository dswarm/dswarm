package de.avgl.dmp.controller;

import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;

import javax.servlet.DispatcherType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.FilterRegistration;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.servlet.ServletContainer;

import com.google.inject.servlet.GuiceFilter;

import de.avgl.dmp.controller.doc.SwaggerConfig;
import de.avgl.dmp.controller.servlet.DMPInjector;

public class EmbeddedServer {

	private static final org.apache.log4j.Logger	log						= org.apache.log4j.Logger.getLogger(EmbeddedServer.class);

	public static final String						CONTEXT_PATH_PROPERTY	= "dmp.http.context_path";
	public static final String						HTTP_PORT_PROPERTY		= "dmp.http.port";
	public static final String						HTTP_HOST_PROPERTY		= "dmp.http.host";

	protected static final int						DEFAULT_PORT			= 8087;
	protected static final String					DEFAULT_HOST			= "127.0.0.1";
	protected static final String					DEFAULT_CONTEXT_PATH	= "/dmp";

	protected static final String					API_VERSION				= "1.0.0";

	private HttpServer								httpServer;

	public HttpServer start() throws IOException {
		return start(false);
	}

	public HttpServer start(final boolean skipStart) throws IOException {
		log.info("Starting grizzly");

		final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(getBaseUri(), false);
		server.getListener("grizzly").setMaxFormPostSize(Integer.MAX_VALUE);

		final WebappContext context = new WebappContext("DMP 2000 Backend", getContextPath());

		context.addListener(DMPInjector.class);

		final ServletRegistration servletRegistration = context.addServlet("ServletContainer", ServletContainer.class);
		servletRegistration.addMapping("/*");
		servletRegistration.setInitParameter("javax.ws.rs.Application", "de.avgl.dmp.controller.providers.DMPApplication");

		final FilterRegistration registration = context.addFilter("GuiceFilter", GuiceFilter.class);
		registration.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), "/*");

		context.deploy(server);

		if (!skipStart) {
			server.start();
		}

		httpServer = server;
		return server;
	}

	public void stop() throws Exception {
		httpServer.stop();
	}

	public URI getBaseUri() {
		return UriBuilder.fromUri("http://0.0.0.0/").host(getHost()).port(getPort()).path(getContextPath()).build();
	}

	public String getHost() {
		final String value = System.getProperty(HTTP_HOST_PROPERTY);
		if (value != null) {
			return value;
		}

		return DEFAULT_HOST;
	}

	public String getContextPath() {
		final String value = System.getProperty(CONTEXT_PATH_PROPERTY);
		if (value != null) {

			if (value.substring(0, 1).equals("/")) {
				return value;
			}

			log.warn("Value of " + CONTEXT_PATH_PROPERTY + " property must start with a '/' [" + value + "]." + " Using default ["
					+ DEFAULT_CONTEXT_PATH + "].");
		}

		return DEFAULT_CONTEXT_PATH;
	}

	public int getPort() {
		final String value = System.getProperty(HTTP_PORT_PROPERTY);
		if (value != null) {

			try {
				final int port = Integer.parseInt(value);
				if (port <= 0) {
					throw new NumberFormatException("port must be positive.");
				}

				return port;
			} catch (NumberFormatException e) {
				log.warn("Value of " + HTTP_PORT_PROPERTY + " property is not a  valid positive integer [" + value + "]." + " Using default ["
						+ DEFAULT_PORT + "].", e);
			}
		}

		return DEFAULT_PORT;
	}

	public static void main(final String[] args) throws IOException {
		final EmbeddedServer main = new EmbeddedServer();
		main.start();

		System.out.println(String.format("Jersey app with WADL available at " + "%s/application.wadl\nHit ^C to stop it...", main.getBaseUri()));

		final CountDownLatch keepAliveLatch = new CountDownLatch(1);
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				try {
					main.stop();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					keepAliveLatch.countDown();
				}
			}
		});

		final Thread keepAliveThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					keepAliveLatch.await();
				} catch (InterruptedException ignore) {
				}
			}
		}, "dmp/grizzly");

		keepAliveThread.setDaemon(false);
		keepAliveThread.start();
	}
}
