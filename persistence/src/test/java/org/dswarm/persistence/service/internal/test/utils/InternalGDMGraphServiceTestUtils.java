package org.dswarm.persistence.service.internal.test.utils;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.google.common.io.Resources;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tgaengler
 */
public final class InternalGDMGraphServiceTestUtils {

	private static final Logger	LOG	= LoggerFactory.getLogger(InternalGDMGraphServiceTestUtils.class);

	/**
	 * cleans the complete graph db.
	 */
	public static void cleanGraphDB() {

		final URL resource = Resources.getResource("dmp.properties");
		final Properties properties = new Properties();

		try {

			properties.load(resource.openStream());
		} catch (final IOException e) {

			InternalGDMGraphServiceTestUtils.LOG.error("Could not load dmp.properties", e);
		}

		final String graphEndpoint = properties.getProperty("dmp_graph_endpoint", "http://localhost:7474/graph");

		final ClientBuilder builder = ClientBuilder.newBuilder();

		final Client c = builder.build();
		final WebTarget target = c.target(graphEndpoint);

		final Response response = target.path("/maintain/delete").request().delete(Response.class);

		Assert.assertEquals("expected 200", 200, response.getStatus());
	}
}
