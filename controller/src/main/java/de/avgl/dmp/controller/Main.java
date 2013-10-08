package de.avgl.dmp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.glassfish.grizzly.http.server.HttpServer;

/**
 * Main class.
 */
public class Main {

	private static final org.apache.log4j.Logger	log	= org.apache.log4j.Logger.getLogger(Main.class);

	private EmbeddedServer server;

	public Main(Properties properties) {
		final String host = properties.getProperty("backend_http_server_host");
		final String port = properties.getProperty("backend_http_server_port");

		System.setProperty("dmp.http.host", host);
		System.setProperty("dmp.http.port", port);
	}

	public String getBaseUri() {
		return server.getBaseUri().toString();
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
		server = new EmbeddedServer();

		try {
			return server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
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
	 * @param args main args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Main main = Main.create();
		// not that HotSpot might optimize this away
		main.toString();

		EmbeddedServer.main(args);
	}
}
