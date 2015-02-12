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
package org.dswarm.controller.resources.status;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@RequestScoped
@Api(value = "/_ping", description = "Ping the server for a heartbeat.")
@Path("_ping")
public final class HeartbeatResource {

	private static final String RESPONSE = "pong";

	private static final CacheControl CACHE_CONTROL;
	static {
		final CacheControl control = new CacheControl();
		control.setMustRevalidate(true);
		control.setNoCache(true);
		control.setNoStore(true);
		CACHE_CONTROL = control;
	}

	@ApiOperation("send a ping, receive a pong")
	@Timed
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public static Response getPong() {
		return Response.ok(RESPONSE).cacheControl(CACHE_CONTROL).build();
	}
}
