package org.dswarm.controller.utils;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

/**
 * @author tgaengler
 */
public final class ResourceUtils {

	public static String readHeaders(final HttpHeaders httpHeaders) {

		final MultivaluedMap<String, String> headerParams = httpHeaders.getRequestHeaders();

		final StringBuilder sb = new StringBuilder();

		for (final Map.Entry<String, List<String>> entry : headerParams.entrySet()) {

			final String headerIdentifier = entry.getKey();
			final List<String> headerValues = entry.getValue();

			sb.append("\t\t").append(headerIdentifier).append(" = ");

			for (final String headerValue : headerValues) {

				sb.append(headerValue).append(", ");
			}

			sb.append("\n");
		}

		return sb.toString();
	}
}
