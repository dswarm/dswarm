package de.avgl.dmp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Main class.
 */
public class Main {

	private static final org.apache.log4j.Logger	log	= org.apache.log4j.Logger.getLogger(Main.class);

	// Base URI the Grizzly HTTP server will listen on
	public final String								BASE_URI;

	public Main(Properties properties) {
		final String host = properties.getProperty("backend_http_server_host");
		final int port = Integer.valueOf(properties.getProperty("backend_http_server_port"));

		final URI baseUri = UriBuilder.fromUri("http://" + host).port(port).path("dmp/").build();

		BASE_URI = baseUri.toString();
	}

	public String getBaseUri() {
		return BASE_URI;
	}

	private static Properties loadProperties(String propertiesPath) {
		final InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesPath);
		final Properties properties = new Properties();

		try {

			properties.load(inStream);
		} catch (IOException e) {

			log.debug("could not load properties");
		}

		return properties;
	}

	private static Properties loadProperties() {
		return loadProperties("dmp.properties");
	}

	/**
	 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
	 * 
	 * @return Grizzly HTTP server.
	 */
	public HttpServer startServer() {
		// create a resource config that scans for JAX-RS resources and
		// providers
		// in de.avgl.dmp.controller.resources package
		final ResourceConfig rc = new ResourceConfig()
		// .register(JacksonJaxbJsonProvider.class)
				.packages("de.avgl.dmp.controller.resources")
				.register(MultiPartFeature.class)
				.register(de.avgl.dmp.controller.providers.ExceptionHandler.class);

		// create and start a new instance of grizzly http server
		// exposing the Jersey application at BASE_URI
		
		final HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
		httpServer.getListener("grizzly").setMaxFormPostSize(Integer.MAX_VALUE);

		return httpServer;
	}

	public static Main create(int port) {
		final Properties properties = loadProperties();
		properties.setProperty("backend_http_server_port", Integer.valueOf(port).toString());

		return new Main(properties);
	}

	public static Main create() {
		final Properties properties = loadProperties();

		return new Main(properties);
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final Main main = Main.create();
		final HttpServer server = main.startServer();
		System.out.println(String.format("Jersey app started with WADL available at " + "%sapplication.wadl\nHit enter to stop it...",
				main.getBaseUri()));
		System.in.read();
		server.stop();
	}
}
