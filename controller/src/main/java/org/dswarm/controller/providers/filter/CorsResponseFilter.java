/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.controller.providers.filter;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

import com.google.common.net.HttpHeaders;

/**
 * A filter for providing the CORS headers of a HTTP response.
 * 
 * @author phorn
 */
@Priority(Priorities.HEADER_DECORATOR)
public class CorsResponseFilter implements ContainerResponseFilter {

	/**
	 * {@inheritDoc}<br/>
	 * Creates the CORS headers of a HTTP response.<br>
	 * note: [@tgaengler] note every resource provides all HTTP methods ...
	 */
	@Override
	public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
		final MultivaluedMap<String, Object> headers = responseContext.getHeaders();

		headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, OPTIONS, HEAD, PUT, POST, DELETE, PATCH");
		headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Authorization, Origin, X-Requested-With, Content-Type");

		headers.add(HttpHeaders.X_POWERED_BY, "d:swarm");
	}
}
