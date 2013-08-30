package de.avgl.dmp.controller.resources;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.net.HttpHeaders;

@Path("transformations")
public class TransformationsResource {

	// TODO: something for later create a nice domain model that can be utilised for message exchange
	//
	// @POST
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// public TestObject run(final Transformation transformation) {
	//
	// final TestObject testObject = new TestObject();
	// testObject.setMessage(transformation.toString());
	//
	// return testObject;
	// }

	@Path("/test")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response run(final String jsonObjectString) {

		final JsonNodeFactory factory = JsonNodeFactory.instance;

		final ObjectNode responseJSON = new ObjectNode(factory);

		responseJSON.put("response_message", "this is your response message");

		if (jsonObjectString != null) {

			final ObjectMapper mapper = new ObjectMapper();

			final JaxbAnnotationModule module = new JaxbAnnotationModule();
			// configure as necessary
			mapper.registerModule(module);

			ObjectNode jsonObject = null;

			try {
				jsonObject = mapper.readValue(jsonObjectString, ObjectNode.class);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (jsonObject != null) {

				responseJSON.put("request_message", jsonObject);
			}
		}

		return Response.ok(responseJSON.toString()).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, OPTIONS, HEAD")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Origin, X-Requested-With, Content-Type").build();
	}

	@Path("/test")
	@OPTIONS
	public Response getOptions() {

		return Response.ok().header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, OPTIONS, HEAD")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Origin, X-Requested-With, Content-Type").build();
	}
}
