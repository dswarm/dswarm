package de.avgl.dmp.controller.providers.handler;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import de.avgl.dmp.controller.providers.BaseExceptionHandler;

@Provider
public class ExceptionHandler extends BaseExceptionHandler<Exception> {

	@Override
	public Response toResponse(final Exception exception) {

		final String[] clientSegments = errorMessage(exception).split(":");
		final String message = clientSegments[clientSegments.length - 1];

		return createResponse(message);
	}
}
