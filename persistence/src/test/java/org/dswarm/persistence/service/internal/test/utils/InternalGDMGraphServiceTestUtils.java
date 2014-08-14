package org.dswarm.persistence.service.internal.test.utils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.Assert;

import org.dswarm.persistence.GuicedTest;

/**
 * @author tgaengler
 */
public final class InternalGDMGraphServiceTestUtils {

	/**
	 * cleans the complete graph db.
	 * @param graphEndpoint The URL for our Neo4j extension
	 */
	public static void cleanGraphDB() {

		final String graphEndpoint = GuicedTest.configValue("dswarm.db.graph.endpoint", String.class);

		final ClientBuilder builder = ClientBuilder.newBuilder();

		final Client c = builder.build();
		final WebTarget target = c.target(graphEndpoint);

		final Response response = target.path("/maintain/delete").request().delete(Response.class);

		Assert.assertEquals("expected 200", 200, response.getStatus());
	}
}
