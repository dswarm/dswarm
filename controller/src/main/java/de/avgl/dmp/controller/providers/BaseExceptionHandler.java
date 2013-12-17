package de.avgl.dmp.controller.providers;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;


public abstract class BaseExceptionHandler<E extends Exception> implements ExceptionMapper<E> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(BaseExceptionHandler.class);

	protected Response createResponse(final String message, final int status) {

		final JsonNodeFactory factory = JsonNodeFactory.instance;
		final ObjectNode responseJSON = new ObjectNode(factory);

		responseJSON.put("status", "nok");
		responseJSON.put("status_code", status);
		responseJSON.put("error", message);

		return Response.status(status).entity(responseJSON.toString())
				.type(MediaType.APPLICATION_JSON_TYPE).build();
	}

	protected Response createResponse(final String message) {

		return createResponse(message, 500);
	}

	protected String errorMessage(final E exception) {
		LOG.error(String.format("exception was thrown:\ntype = '%s'\nmessage = %s", exception.getClass().getCanonicalName(), exception.getMessage()), exception);

		return exception.getMessage();
	}
}
