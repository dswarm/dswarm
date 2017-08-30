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
package org.dswarm.controller.resources;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
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

		try {
			final String idEncoded = URLEncoder.encode(uuid, "UTF-8");
			final String path = createObjectPath(idEncoded);

			final URI baseURI = uri.getBaseUri();
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

	private String createObjectPath(final String uuid) {

		final String basePath = StringUtils.stripEnd(uri.getBaseUri().getPath(), "/");

		final Stream<String> pathWithoutId =
				uri.getPathSegments().stream()
						.map(PathSegment::getPath)
						.filter(Predicate.isEqual(uuid).negate());

		final Stream<String> pathSegments = Stream.concat(
				Stream.of(basePath),
				Stream.concat(pathWithoutId, Stream.of(uuid))
		);

		return pathSegments.collect(Collectors.joining("/"));
	}
}
