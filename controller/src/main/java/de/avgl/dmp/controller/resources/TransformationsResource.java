package de.avgl.dmp.controller.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.HttpHeaders;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.converter.resources.JsonToPojoMapper;
import de.avgl.dmp.converter.resources.PojoToXMLBuilder;
import de.avgl.dmp.converter.resources.TransformationsConverter;
import de.avgl.dmp.converter.resources.TransformationsCoverterException;
import de.avgl.dmp.persistence.model.Transformation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
	public Response run(final String jsonObjectString) throws IOException {

		final JsonNodeFactory factory = JsonNodeFactory.instance;

		final ObjectNode responseJSON = new ObjectNode(factory);

		responseJSON.put("response_message", "this is your response message");

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode json = mapper.readValue(jsonObjectString, ObjectNode.class);

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
			pojos = new JsonToPojoMapper().apply(jsonObjectString);
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
	public Response runToXML(final String jsonObjectString) throws IOException, TransformationsCoverterException {

		final List<Transformation> pojos = new JsonToPojoMapper().apply(jsonObjectString);
		final String xml = new PojoToXMLBuilder().apply(pojos).toString();

		return Response.ok(xml).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response runWithMetamorph(final String jsonObjectString) throws IOException, TransformationsCoverterException {

		final File file = TransformationsConverter.toMetamorph(jsonObjectString);
		final TransformationFlow flow = TransformationFlow.from(file);
		final String result = flow.apply(TransformationFlow.DEFAULT_RECORD);

		return Response.ok(result).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}

	@OPTIONS
	public Response getOptions() {

		return Response.ok().header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, OPTIONS, HEAD")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Origin, X-Requested-With, Content-Type").build();
	}
}
