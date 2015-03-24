package org.dswarm.controller.resources;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBaseResource {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractBaseResource.class);

	/**
	 * The base URI of this resource.
	 */
	@Context
	UriInfo uri;

	/**
	 * Builds a positive response with the given content.
	 *
	 * @param responseContent a response message
	 * @return the response
	 */
	protected static Response buildResponse(final String responseContent) {

		return Response.ok(responseContent).build();
	}

	/**
	 * Creates the resource URI for the given object.
	 *
	 * @param uuid an ID string
	 * @return the resource URI for the given object
	 */
	protected final URI createObjectURI(final String uuid) {

		final URI baseURI = uri.getBaseUri();
		final List<PathSegment> pathSegments = uri.getPathSegments();

		try {
			final String idEncoded = URLEncoder.encode(uuid, "UTF-8");
			final String path = pathSegments.stream()
					.map(PathSegment::getPath)
					.filter(Predicate.isEqual(idEncoded).negate())
					.collect(Collectors.joining("/", uri.getBaseUri().getPath(), "/" + idEncoded));

			return new URI(
					baseURI.getScheme(),
					baseURI.getAuthority(),
					path,
					null,
					null);
		} catch (UnsupportedEncodingException | URISyntaxException e) {
			LOG.debug("couldn't encode id", e);
			return null;
		}
	}
}
