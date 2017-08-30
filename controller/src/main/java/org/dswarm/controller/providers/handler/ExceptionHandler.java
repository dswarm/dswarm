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

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.dswarm.controller.providers.BaseExceptionHandler;

/**
 * A default exception handler for providing exceptions at client side of the backend API.
 *
 * @author phorn
 */
@Provider
public class ExceptionHandler extends BaseExceptionHandler<Exception> {

	protected static final String UNDEFINED_ERROR = "undefined error";

	@Override
	protected Response.Status getStatusFrom(final Exception exception) {

		return Response.Status.INTERNAL_SERVER_ERROR;
	}

	@Override
	protected String getErrorMessageFrom(final Exception exception) {

		if (exception == null) {

			return UNDEFINED_ERROR;
		}

		final Optional<String> optionalExceptionMessage = getOptionalExceptionMessage(exception);

		if(!optionalExceptionMessage.isPresent()) {

			return UNDEFINED_ERROR;
		}

		final String exceptionMessage = optionalExceptionMessage.get();
		final String[] clientSegments = exceptionMessage.split("Exception:");

		return clientSegments[clientSegments.length - 1];
	}

	private Optional<String> getOptionalExceptionMessage(final Throwable exception) {

		final String exceptionMessage = exception.getMessage();

		if (exceptionMessage == null || exceptionMessage.trim().isEmpty()) {

			final Throwable exceptionCause = exception.getCause();

			if (exceptionCause == null) {

				// no (further) exception cause available

				return Optional.empty();
			}

			// traverse the exception cause tree until an exception is found with a message or the end of the exception cause tree is reached

			return getOptionalExceptionMessage(exceptionCause);
		}

		return Optional.of(exceptionMessage);
	}
}
