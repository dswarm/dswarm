package org.dswarm.controller.providers.handler;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.dswarm.controller.providers.BaseExceptionHandler;
import org.dswarm.converter.DMPMorphDefException;

/**
 * An exception handler for providing exact messages for wrong Metamorph definitions
 *
 * @author phorn
 */
@Provider
public class DMPMorphDefExceptionHandler extends BaseExceptionHandler<DMPMorphDefException> {

	@Override
	public Response toResponse(final DMPMorphDefException exception) {

		final String message = errorMessage(exception);
		return createResponse(message);
	}
}
