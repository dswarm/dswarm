/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.controller.resources.status;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@RequestScoped
@Api(value = "/_health", description = "Perform some health checks, e.g. database connectivity.")
@Path("_health")
public class HealthResource {

	private final HealthCheckRegistry	healthCheckRegistry;
	private final ObjectMapper			objectMapper;

	@Inject
	public HealthResource(final HealthCheckRegistry healthCheckRegistry, final ObjectMapper objectMapper) {
		this.healthCheckRegistry = healthCheckRegistry;
		this.objectMapper = objectMapper;
	}

	@ApiOperation("do a health check (includes check for database connection)")
	@Timed
	@GET
	@Path("/{check: (\\w+)?}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response singleHealthCheck(
			@ApiParam(value = "perform this health check (_all for doing all checks)", required = false, defaultValue = "_all", name = "check") @PathParam("check") final String check,
			@ApiParam(value = "json pretty print", required = false, defaultValue = "false", name = "pretty") @DefaultValue("false") @QueryParam("pretty") final boolean pretty)
			throws IOException {

		final StringWriter writer = new StringWriter();
		final JsonGenerator generator = objectMapper.getFactory().createGenerator(writer);

		if (pretty) {

			generator.useDefaultPrettyPrinter();
		}

		generator.writeStartObject();

		if (Strings.isNullOrEmpty(check) || "_all".equals(check)) {

			final Map<String, HealthCheck.Result> results = healthCheckRegistry.runHealthChecks();
			for (final Map.Entry<String, HealthCheck.Result> entry : results.entrySet()) {
				renderHealthCheckResult(generator, entry.getKey(), entry.getValue());
			}
		} else {

			final HealthCheck.Result result = healthCheckRegistry.runHealthCheck(check);
			renderHealthCheckResult(generator, check, result);
		}

		generator.writeEndObject();
		generator.flush();
		generator.close();

		return Response.ok(writer.toString()).header(HttpHeaders.CACHE_CONTROL, "must-revalidate,no-cache,no-store").build();
	}

	private void renderHealthCheckResult(final JsonGenerator generator, final String checkName, final HealthCheck.Result result) throws IOException {
		generator.writeObjectFieldStart(checkName);

		if (result.isHealthy()) {
			generator.writeBooleanField("healthy", true);
		} else {
			generator.writeBooleanField("healthy", false);
			generator.writeStringField("reason", result.getMessage());
			@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
			final Throwable error = result.getError();
			if (error != null) {
				generator.writeStringField("error", error.getMessage());
			}
		}

		generator.writeEndObject();
	}
}
