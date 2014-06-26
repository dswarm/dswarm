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
		headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "accept, origin, x-requested-with, content-type");

		headers.add(HttpHeaders.X_POWERED_BY, "d:swarm");
	}
}
