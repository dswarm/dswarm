package de.avgl.dmp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Main class.
 */
public class Main {

	private static final org.apache.log4j.Logger	log	= org.apache.log4j.Logger.getLogger(Main.class);

	// Base URI the Grizzly HTTP server will listen on
	public static final String						BASE_URI;

	static {

		final InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("dmp.properties");
		final Properties properties = new Properties();

		try {

			properties.load(inStream);
		} catch (IOException e) {

			log.debug("could not load properties");
		}

		final String host = properties.getProperty("backend_http_server_host");
		final int port = Integer.valueOf(properties.getProperty("backend_http_server_port")).intValue();

		final URI baseUri = UriBuilder.fromUri("http://" + host).port(port).path("dmp/").build();

		BASE_URI = baseUri.toString();
	}

	/**
	 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
	 *
	 * @return Grizzly HTTP server.
	 */
	public static HttpServer startServer() {
		// create a resource config that scans for JAX-RS resources and
		// providers
		// in de.avgl.dmp.controller.resources package
		final ResourceConfig rc = new ResourceConfig()
		// .register(JacksonJaxbJsonProvider.class)
				.packages("de.avgl.dmp.controller.resources");

		// create and start a new instance of grizzly http server
		// exposing the Jersey application at BASE_URI
		final HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);

		return httpServer;
	}

	/**
	 * Main method.
	 *
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final HttpServer server = startServer();
		System.out.println(String.format("Jersey app started with WADL available at " + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
		System.in.read();
		server.stop();
	}
}
