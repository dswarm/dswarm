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
