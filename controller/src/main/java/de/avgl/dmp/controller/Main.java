package de.avgl.dmp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.glassfish.grizzly.http.server.HttpServer;

/**
 * The main class of the backend API. Wraps the backend web server where the backend API is located.
 * 
 * @author phorn
 * @author tgaengler
 */
public class Main {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(Main.class);

	/**
	 * The (embedded) backend web server.
	 */
	private EmbeddedServer							server;

	/**
	 * Inits the properties for the backend web server.
	 * 
	 * @param properties user properties
	 */
	private Main(final Properties properties) {

		final String host = properties.getProperty("backend_http_server_host");
		final String port = properties.getProperty("backend_http_server_port");

		System.setProperty("dmp.http.host", host);
		System.setProperty("dmp.http.port", port);
	}

	/**
	 * Gets the base URI of the backend API.
	 * 
	 * @return the base URI of the backend API
	 */
	public String getBaseUri() {

		return server.getBaseUri().toString();
	}

	/**
	 * Loads the user properties from the given file path.
	 * 
	 * @param propertiesPath the properties file path
	 * @return the user properties
	 */
	private static Properties loadProperties(final String propertiesPath) {

		final InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesPath);
		final Properties properties = new Properties();

		try {

			properties.load(inStream);
		} catch (final IOException e) {

			Main.LOG.error("could not load properties");
		}

		return properties;
	}

	/**
	 * Loads the user from the default properties file path.
	 * 
	 * @return the user properties.
	 */
	private static Properties loadProperties() {

		return Main.loadProperties("dmp.properties");
	}

	/**
	 * Starts the (embedded) backend web server exposing resources defined in this application.
	 * 
	 * @return the (embedded) backend web server
	 */
	public HttpServer startServer() {

		server = new EmbeddedServer();

		try {

			return server.start();
		} catch (final IOException e) {

			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Creates the backend API (incl. its hosting backend web server at the given port).
	 * 
	 * @param port the port of the backend web server
	 * @return the main class of the backend API
	 */
	public static Main create(final int port) {

		final Properties properties = Main.loadProperties();
		properties.setProperty("backend_http_server_port", String.valueOf(port));

		return new Main(properties);
	}

	/**
	 * Creates the backend API (incl. its hosting backend web server).
	 * 
	 * @return the main class of the backend API
	 */
	public static Main create() {

		final Properties properties = Main.loadProperties();

		return new Main(properties);
	}

	/**
	 * Creates and starts the backend API (incl. its hosting backend web server).
	 * 
	 * @param args main args
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {
		
		final Main main = Main.create();
		// not that HotSpot might optimize this away
		main.toString();

		EmbeddedServer.main(args);
	}
}
