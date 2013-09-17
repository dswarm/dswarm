package de.avgl.dmp.controller.resources;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.net.HttpHeaders;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.persistence.mapping.JsonToPojoMapper;
import de.avgl.dmp.persistence.model.transformation.Transformation;

@Path("jobs")
public class JobsResource {

	private Response buildResponse(String responseContent) {
		return Response.ok(responseContent).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response executeJob(final String jsonObjectString) throws IOException, DMPConverterException {

		final List<Transformation> pojos = new JsonToPojoMapper().apply(jsonObjectString);

		final TransformationFlow flow = TransformationFlow.fromTransformations(pojos);
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
