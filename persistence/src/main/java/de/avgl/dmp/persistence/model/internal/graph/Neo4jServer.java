package de.avgl.dmp.persistence.model.internal.graph;

import java.util.HashMap;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.neo4j.server.configuration.Configurator;

public class Neo4jServer {

	private final GraphDatabaseService			database;
	private final WrappingNeoServerBootstrapper	server;
	private final Configurator					config;

	public Neo4jServer() {

		database = new GraphDatabaseFactory().newEmbeddedDatabase("ttl.db");

		HashMap<String, String> settings = new HashMap<String, String>();
		settings.put(Configurator.WEBSERVER_PORT_PROPERTY_KEY, "7475");
		settings.put(Configurator.WEBSERVER_HTTPS_ENABLED_PROPERTY_KEY, "false");

		config = new Neo4jServerConfig(settings);

		server = new WrappingNeoServerBootstrapper((GraphDatabaseAPI) database, config);
		server.start();
	}

	public void start() {

		server.start();
	}

	public void stop() {

		server.stop();
		database.shutdown();
	}

	public WrappingNeoServerBootstrapper getServer() {

		return server;
	}

	public GraphDatabaseService getDatabase() {

		return database;
	}
}
