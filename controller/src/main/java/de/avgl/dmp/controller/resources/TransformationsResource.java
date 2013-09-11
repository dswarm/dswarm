package de.avgl.dmp.controller.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.HttpHeaders;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.converter.resources.TransformationsConverter;
import de.avgl.dmp.persistence.model.Transformation;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

	@POST
	@Path("/echo")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response run(final String jsonObjectString) {

		final JsonNodeFactory factory = JsonNodeFactory.instance;

		final ObjectNode responseJSON = new ObjectNode(factory);

		responseJSON.put("response_message", "this is your response message");

		ObjectNode json = TransformationsConverter.toObjectNode(jsonObjectString);

		if (json != null) {
			responseJSON.put("request_message", json);
		}

		return Response.ok(responseJSON.toString()).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}


	@POST
	@Path("/pojo")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response runToPojo(final String jsonObjectString) {

		final JsonNodeFactory factory = JsonNodeFactory.instance;

		final ObjectNode responseJSON = new ObjectNode(factory);

		responseJSON.put("response_message", "this is your response message");

		List<Transformation> pojos = null;
		try {
			pojos = TransformationsConverter.toPojo(jsonObjectString);
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		if (pojos != null) {
			String responseText = pojos.toString();
			responseJSON.put("request_message", responseText);
		}

		return Response.ok(responseJSON.toString()).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}


	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_XML)
	public Response runToXML(final String jsonObjectString) {
		String xml = null;
		try {
			final List<Transformation> pojos = TransformationsConverter.toPojo(jsonObjectString);
			xml = TransformationsConverter.createDom(pojos);
		} catch (IOException | ParserConfigurationException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		return Response.ok(xml).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response runWithMetamorph(final String jsonObjectString) throws IOException, ParserConfigurationException {

		final File file = TransformationsConverter.createMorphFile(jsonObjectString);
		final String result = TransformationFlow.flow(file);

		return Response.ok(result).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}

	@OPTIONS
	public Response getOptions() {

		return Response.ok().header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, OPTIONS, HEAD")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Origin, X-Requested-With, Content-Type").build();
	}
}
