package de.avgl.dmp.controller.providers;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.HttpHeaders;

@Provider
public class ExceptionHandler implements ExceptionMapper<Exception> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ExceptionHandler.class);

	@Override
	public Response toResponse(final Exception exception) {

		LOG.error("exception was thrown:\ntype = '" + exception.getClass().getCanonicalName() + "'\n" + exception.getMessage());

		exception.printStackTrace();

		final String[] clientSegments = exception.getMessage().split(":");
		final String clientMessage = clientSegments[clientSegments.length - 1];

		final JsonNodeFactory factory = JsonNodeFactory.instance;
		final ObjectNode responseJSON = new ObjectNode(factory);

		responseJSON.put("status", "nok");
		responseJSON.put("error", clientMessage);
		
		Integer status = null;
		
		if(WebApplicationException.class.isInstance(exception)) {
			
			WebApplicationException webApplicationException = (WebApplicationException) exception;
			
			final Response response = webApplicationException.getResponse();
			
			if(response != null) {
				
				final int statusInt = response.getStatus();
				
				status = Integer.valueOf(statusInt);
			}
		}
		
		final int responseStatus;
		
		if(status != null) {
			
			responseStatus = status.intValue();
		} else {
			
			responseStatus = 500;
		}

		return Response.status(responseStatus).entity(responseJSON.toString()).type(MediaType.APPLICATION_JSON_TYPE)
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}
}
