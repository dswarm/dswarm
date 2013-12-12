package de.avgl.dmp.controller.providers.handler;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import de.avgl.dmp.controller.providers.BaseExceptionHandler;


@Provider
public class WebApplicationExceptionHandler extends BaseExceptionHandler<WebApplicationException> {

	@Override
	public Response toResponse(final WebApplicationException exception) {

		final String message = errorMessage(exception);
		final int status = exception.getResponse().getStatus();

		return createResponse(message, status);
	}
}
