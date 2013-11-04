package de.avgl.dmp.controller.resources;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.mapping.JsonToPojoMapper;
import de.avgl.dmp.persistence.model.job.Job;

@RequestScoped
@Path("jobs")
public class JobsResource {

	private final Provider<JsonToPojoMapper> pojoMapperProvider;

	@Inject
	public JobsResource(final Provider<JsonToPojoMapper> pojoMapperProvider) {

		this.pojoMapperProvider = pojoMapperProvider;
	}

	private Response buildResponse(final String responseContent) {
		return Response.ok(responseContent).build();
	}

	@Path("/demo")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response executeJobDemo(final String jsonObjectString) throws IOException, DMPConverterException {

		final Job job;
		try {
			job = pojoMapperProvider.get().toJob(jsonObjectString);
		} catch (DMPPersistenceException e) {
			throw new DMPConverterException(e.getMessage());
		}

		final TransformationFlow flow = TransformationFlow.fromJob(job);
		final String result = flow.applyResourceDemo(TransformationFlow.DEFAULT_RESOURCE_PATH);

		return buildResponse(result);
	}
}
