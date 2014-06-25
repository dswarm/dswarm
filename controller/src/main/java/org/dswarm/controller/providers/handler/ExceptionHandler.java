package org.dswarm.controller.providers.handler;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response toResponse(final Exception exception) {

		final String[] clientSegments = errorMessage(exception).split(":");
		final String message = clientSegments[clientSegments.length - 1];

		return createResponse(message);
	}
}
