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
package org.dswarm.controller.providers.handler;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.dswarm.controller.DMPJsonException;
import org.dswarm.controller.providers.BaseExceptionHandler;

/**
 * An exception handler for providing JSON exceptions at client side of the backend API
 *
 * @author phorn
 */
@Provider
public class DMPJsonExceptionHandler extends BaseExceptionHandler<DMPJsonException> {

	private static final Pattern PATTERN = Pattern.compile(
			"Unrecognized field (\"[^\"]+?\") \\(class [\\S]+?\\), not marked as ignorable \\((\\d+) known properties: , ([^\\)]+?)\\]\\).*",
			Pattern.DOTALL | Pattern.MULTILINE);

	@Override
	protected Response.Status getStatusFrom(final DMPJsonException exception) {
		return parse(exception).isPresent()
				? Response.Status.BAD_REQUEST
				: Response.Status.INTERNAL_SERVER_ERROR;
	}

	@Override
	protected String getErrorMessageFrom(final DMPJsonException exception) {
		return parse(exception).map(matcher -> {
			final String unknownField = matcher.group(1);
			final String numFields = matcher.group(2);
			final String availFields = matcher.group(3);

			return String.format("Unknown Field %s, must use one of the %s: {%s}", unknownField, numFields, availFields);
		}).orElseGet(exception::getMessage);
	}

	private Optional<Matcher> parse(final DMPJsonException exception) {

		final Throwable exceptionCause = exception.getCause();
		final Matcher matcher = DMPJsonExceptionHandler.PATTERN.matcher(exceptionCause.getMessage());
		if (matcher.matches() && matcher.groupCount() >= 3) {
			return Optional.of(matcher);
		} else {
			return Optional.empty();
		}
	}
}
