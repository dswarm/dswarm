package de.avgl.dmp.controller.resources;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.HttpHeaders;
import de.avgl.dmp.converter.resources.TransformationsConverter;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ExceptionHandler implements ExceptionMapper<Exception> {
	@Override
	public Response toResponse(Exception exception) {
		exception.printStackTrace();

		final String[] clientSegments = exception.getMessage().split(":");
		final String clientMessage = clientSegments[clientSegments.length - 1];

		final JsonNodeFactory factory = JsonNodeFactory.instance;
		final ObjectNode responseJSON = new ObjectNode(factory);

		responseJSON.put("status", "nok");
		responseJSON.put("error", clientMessage);

		return Response.status(500)
				.entity(responseJSON.toString())
				.type(MediaType.APPLICATION_JSON_TYPE)
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
				.build();
	}
}
