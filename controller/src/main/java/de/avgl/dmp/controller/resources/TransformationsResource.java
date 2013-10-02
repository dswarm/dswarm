package de.avgl.dmp.controller.resources;

import java.io.IOException;
import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.net.HttpHeaders;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.converter.morph.MorphScriptBuilder;
import de.avgl.dmp.persistence.mapping.JsonToPojoMapper;
import de.avgl.dmp.persistence.model.job.Transformation;

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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_XML)
	public Response runToXML(final String jsonObjectString) throws IOException, DMPConverterException {

		final Transformation transformation = new JsonToPojoMapper().toTransformation(jsonObjectString);

		final String xml = new MorphScriptBuilder().apply(transformation).toString();

		return buildResponse(xml);
	}

	/**
	 * this endpoint consumes a transformation as JSON representation
	 *
	 * @param jsonObjectString a JSON representation of one transformation
	 * @return
	 * @throws IOException
	 * @throws DMPConverterException
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response runWithMetamorph(final String jsonObjectString) throws IOException, DMPConverterException {

		final Transformation transformation = new JsonToPojoMapper().toTransformation(jsonObjectString);

		final TransformationFlow flow = TransformationFlow.fromTransformation(transformation);
		final String result = flow.applyResource(TransformationFlow.DEFAULT_RESOURCE_PATH);

		return buildResponse(result);
	}

	@OPTIONS
	public Response getOptions() {

		return Response.ok().header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, OPTIONS, HEAD")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Origin, X-Requested-With, Content-Type").build();
	}
}
