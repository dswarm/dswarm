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
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@RequestScoped
@Api(value = "/_stats", description = "Get statistics about the server.")
@Path("_stats")
public class MetricsResource {

	private final MetricRegistry registry;
	private final ObjectMapper mapper;

	private static final CacheControl CACHE_CONTROL;
	static {
		final CacheControl control = new CacheControl();
		control.setMustRevalidate(true);
		control.setNoCache(true);
		control.setNoStore(true);
		CACHE_CONTROL = control;
	}

	@Inject
	public MetricsResource(final MetricRegistry registry) {

		this.registry = registry;
		mapper = configureObjectMapper();
	}

	private static ObjectMapper configureObjectMapper() {
		final TimeUnit rateUnit = TimeUnit.SECONDS;
		final TimeUnit durationUnit = TimeUnit.MILLISECONDS;
		final boolean showSamples = false;
		final MetricsModule metricsModule = new MetricsModule(rateUnit, durationUnit, showSamples);
		return new ObjectMapper().registerModule(metricsModule);
	}

	@ApiOperation("get a bunch of metrics and gauges and timers since the last server restart. rates are in per-second, durations in milliseconds.")
	@Metered
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStats(
			@ApiParam(value = "json pretty print", required = false, defaultValue = "false", name = "pretty") @DefaultValue("false") @QueryParam("pretty") final boolean pretty)
			throws IOException {
		final ObjectMapper requestMapper = mapper.configure(SerializationFeature.INDENT_OUTPUT, pretty);
		final String serializedMetrics = requestMapper.writeValueAsString(registry);
		return Response.ok(serializedMetrics).cacheControl(CACHE_CONTROL).build();
	}

}
