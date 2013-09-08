package de.avgl.dmp.controller.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.net.HttpHeaders;
import de.avgl.dmp.controller.mapping.JsonToPojoMapper;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.converter.resources.PojoToXMLBuilder;
import de.avgl.dmp.converter.resources.TransformationsCoverterException;
import de.avgl.dmp.persistence.model.Transformation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

@Path("transformations")
public class TransformationsResource {

	private Response buildResponse(String responseContent) {
		return Response.ok(responseContent).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}

	@POST
	@Path("/echo")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response run(final String jsonObjectString) throws IOException {

		final JsonNodeFactory factory = JsonNodeFactory.instance;

		final ObjectNode responseJSON = new ObjectNode(factory);

		responseJSON.put("response_message", "this is your response message");

		ObjectMapper mapper = new ObjectMapper();
		final JaxbAnnotationModule module = new JaxbAnnotationModule();
		// configure as necessary
		mapper.registerModule(module);
		ObjectNode json = mapper.readValue(jsonObjectString, ObjectNode.class);

		if (json != null) {
			responseJSON.put("request_message", json);
		}

		return buildResponse(responseJSON.toString());
	}


	@POST
	@Path("/pojo")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response runToPojo(final String jsonObjectString) throws IOException {

		final JsonNodeFactory factory = JsonNodeFactory.instance;

		final ObjectNode responseJSON = new ObjectNode(factory);

		responseJSON.put("response_message", "this is your response message");

		final List<Transformation> pojos = new JsonToPojoMapper().apply(jsonObjectString);
		String responseText = pojos.toString();
		responseJSON.put("request_message", responseText);

		return buildResponse(responseJSON.toString());
	}


	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_XML)
	public Response runToXML(final String jsonObjectString) throws IOException, TransformationsCoverterException {

		final List<Transformation> pojos = new JsonToPojoMapper().apply(jsonObjectString);
		final String xml = new PojoToXMLBuilder().apply(pojos).toString();

		return buildResponse(xml);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response runWithMetamorph(final String jsonObjectString) throws IOException, TransformationsCoverterException {

		final List<Transformation> pojos = new JsonToPojoMapper().apply(jsonObjectString);

		final TransformationFlow flow = TransformationFlow.from(pojos);
		final String result = flow.apply(TransformationFlow.DEFAULT_RECORD);

		return buildResponse(result);
	}

	@OPTIONS
	public Response getOptions() {

		return Response.ok().header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, OPTIONS, HEAD")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Origin, X-Requested-With, Content-Type").build();
	}
}
