/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
