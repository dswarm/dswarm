/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.controller.providers;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic exception handler for providing exceptions at client side of the backend API.
 * 
 * @author phorn
 * @param <E>
 */
public abstract class BaseExceptionHandler<E extends Exception> implements ExceptionMapper<E> {

	private static final Logger	LOG	= LoggerFactory.getLogger(BaseExceptionHandler.class);

	/**
	 * Creates a response with the given message and status as payload.
	 * 
	 * @param message the exception message
	 * @param status the HTTP status code
	 * @return the exception response
	 */
	protected Response createResponse(final String message, final int status) {

		final JsonNodeFactory factory = JsonNodeFactory.instance;
		final ObjectNode responseJSON = new ObjectNode(factory);

		responseJSON.put("status", "nok");
		responseJSON.put("status_code", status);
		responseJSON.put("error", message);

		return Response.status(status).entity(responseJSON.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
	}

	/**
	 * Creates a response with the given message and HTTP status code 500 as payload.
	 * 
	 * @param message the exception message
	 * @return the exception response
	 */
	protected Response createResponse(final String message) {

		return createResponse(message, 500);
	}

	/**
	 * Logs and gets the error message of the exception.
	 * 
	 * @param exception the exception that was thrown by the backend API
	 * @return the error message of the exception
	 */
	protected String errorMessage(final E exception) {

		BaseExceptionHandler.LOG.error(
				String.format("exception was thrown:\ntype = '%s'\nmessage = %s", exception.getClass().getCanonicalName(), exception.getMessage()),
				exception);

		return exception.getMessage();
	}
}
