package org.dswarm.persistence;

/**
 * Very simple value class for the configuration of the graph database. Needed b/c Jersey resources cannot be injected with @Named
 * injectables, since hk2 (the Jersey DI locator) does not understand @Named)
 */
public class GraphDatabaseConfig {

	private final String	endpoint;

	public GraphDatabaseConfig(final String endpoint) {
		this.endpoint = endpoint;
	}

	/**
	 * @return the endpoint/url to the graph database
	 */
	public String getEndpoint() {
		return endpoint;
	}
}
