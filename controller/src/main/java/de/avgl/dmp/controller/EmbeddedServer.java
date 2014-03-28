package de.avgl.dmp.controller;

import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;

import javax.servlet.DispatcherType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.servlet.FilterRegistration;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.servlet.ServletContainer;

import com.google.inject.servlet.GuiceFilter;

import de.avgl.dmp.controller.servlet.DMPInjector;

/**
 * The embedded web server for the backend API.<br/>
 * note: currently, Grizzly is utilised
 *
 * @author phorn
 * @author tgaengler
 */
public class EmbeddedServer {

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(EmbeddedServer.class);

	/**
	 * The name of the property for the context path of the backend API at the backend web server.
	 */
	public static final String						CONTEXT_PATH_PROPERTY	= "dmp.http.context_path";

	/**
	 * The name of the property for the port of the backend web server.
	 */
	public static final String						HTTP_PORT_PROPERTY		= "dmp.http.port";

	/**
	 * The name of the property for the hostname or ip of the backend web server.
	 */
	public static final String						HTTP_HOST_PROPERTY		= "dmp.http.host";

	/**
	 * The default port of the backend web server.
	 */
	protected static final int						DEFAULT_PORT			= 8087;

	/**
	 * The default ip of the backend web server.
	 */
	protected static final String					DEFAULT_HOST			= "127.0.0.1";

	/**
	 * The default context path of the backend API at the backend web server.
	 */
	protected static final String					DEFAULT_CONTEXT_PATH	= "/dmp";

	/**
	 * The backend web server.
	 */
	private HttpServer								httpServer;

	/**
	 * Starts the backend web server.
	 *
	 * @return the backend web server.
	 * @throws IOException
	 */
	public HttpServer start() throws IOException {

		return start(false);
	}

	/**
	 * Starts the backend web server.
	 *
	 * @param skipStart a flag that indicates, whether the backend web server should be really started or not
	 * @return the backend web server
	 * @throws IOException
	 */
	public HttpServer start(final boolean skipStart) throws IOException {

		EmbeddedServer.LOG.info("Starting backend web server (grizzly)");

		final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(getBaseUri(), false);
		server.getListener("grizzly").setMaxFormPostSize(Integer.MAX_VALUE);

		final WebappContext context = new WebappContext("DMP 2000 Backend", getContextPath());

		context.addListener(DMPInjector.class);

		final ServletRegistration servletRegistration = context.addServlet("ServletContainer", ServletContainer.class);
		servletRegistration.addMapping("/*");
		servletRegistration.setInitParameter("javax.ws.rs.Application", "de.avgl.dmp.controller.providers.DMPApplication");

		final FilterRegistration registration = context.addFilter("GuiceFilter", GuiceFilter.class);
		registration.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), "/*");

		server.getServerConfiguration().addHttpHandler(new StaticHttpHandler("src/docs/ui/docs/"), "/docs");

		context.deploy(server);

		if (!skipStart) {
			server.start();
		}

		httpServer = server;
		return server;
	}

	/**
	 * Stops the backend web server.
	 */
	public void stop() {

		EmbeddedServer.LOG.info("Shutting down backend web server (grizzly)");

		httpServer.stop();
	}

	/**
	 * Gets the base URI of the backend API.
	 *
	 * @return the base URI
	 */
	public URI getBaseUri() {

		return UriBuilder.fromUri("http://0.0.0.0/").host(getHost()).port(getPort()).path(getContextPath()).build();
	}

	/**
	 * Gets the hostname or ip of the backend web server.
	 *
	 * @return host name or ip of the backend web server
	 */
	private String getHost() {

		final String value = System.getProperty(EmbeddedServer.HTTP_HOST_PROPERTY);

		if (value != null) {

			return value;
		}

		return EmbeddedServer.DEFAULT_HOST;
	}

	/**
	 * Gets the context path of the backend API at the backend webserver.
	 *
	 * @return the context path of the backend API at the backend webserver
	 */
	private String getContextPath() {

		final String value = System.getProperty(EmbeddedServer.CONTEXT_PATH_PROPERTY);

		if (value != null) {

			if ("/".equals(value.substring(0, 1))) {

				return value;
			}

			EmbeddedServer.LOG.warn("Value of " + EmbeddedServer.CONTEXT_PATH_PROPERTY + " property must start with a '/' [" + value + "]."
					+ " Using default [" + EmbeddedServer.DEFAULT_CONTEXT_PATH + "].");
		}

		return EmbeddedServer.DEFAULT_CONTEXT_PATH;
	}

	/**
	 * Gets the port of the backend web server.
	 *
	 * @return the port of the backend web server
	 */
	private int getPort() {

		final String value = System.getProperty(EmbeddedServer.HTTP_PORT_PROPERTY);

		if (value != null) {

			try {

				final int port = Integer.parseInt(value);

				if (port <= 0) {

					throw new NumberFormatException("port must be positive.");
				}

				return port;
			} catch (final NumberFormatException e) {

				EmbeddedServer.LOG.warn("Value of " + EmbeddedServer.HTTP_PORT_PROPERTY + " property is not a  valid positive integer [" + value
						+ "]." + " Using default [" + EmbeddedServer.DEFAULT_PORT + "].", e);
			}
		}

		return EmbeddedServer.DEFAULT_PORT;
	}

	/**
	 * Creates and starts the backend web server.
	 * @throws IOException
	 */
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
				} catch (final Exception e) {

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
				} catch (final InterruptedException ignore) {

					LOG.fatal("The backend web server execution thread was interrupted.");
				}
			}
		}, "dmp/grizzly");

		keepAliveThread.setDaemon(false);
		keepAliveThread.start();
	}
}
