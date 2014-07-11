package org.dswarm.controller.providers.handler;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.dswarm.controller.providers.BaseExceptionHandler;

/**
 * An exception handler for providing web application exceptions at client side of the backend API.
 * 
 * @author phorn
 */
@Provider
public class WebApplicationExceptionHandler extends BaseExceptionHandler<WebApplicationException> {

	@Override
	public Response toResponse(final WebApplicationException exception) {

		final String message = errorMessage(exception);
		final int status = exception.getResponse().getStatus();

		return createResponse(message, status);
	}
}
