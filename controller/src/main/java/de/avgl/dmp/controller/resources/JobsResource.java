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

import de.avgl.dmp.controller.utils.InternalSchemaDataUtil;
import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.persistence.mapping.JsonToPojoMapper;

@RequestScoped
@Path("jobs")
class JobsResource {

	private final Provider<JsonToPojoMapper> pojoMapperProvider;
	private final InternalSchemaDataUtil schemaDataUtil;

	@Inject
	public JobsResource(final Provider<JsonToPojoMapper> pojoMapperProvider,
			   final InternalSchemaDataUtil schemaDataUtil) {

		this.pojoMapperProvider = pojoMapperProvider;
		this.schemaDataUtil = schemaDataUtil;
	}

	private Response buildResponse(final String responseContent) {
		return Response.ok(responseContent).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response executeJob(final String jsonObjectString) throws IOException, DMPConverterException {

//		final Job job;
//		try {

			// TODO: fixme

			//job = pojoMapperProvider.get().toJob(jsonObjectString);
//		} catch (final DMPPersistenceException e) {
//			throw new DMPConverterException(e.getMessage());
//		}
//
//		if(job == null) {
//
//			throw new DMPConverterException("couldn't JSON to Job");
//		}

//		if(job.getTransformations() == null || job.getTransformations().isEmpty()) {
//
//			throw new DMPConverterException("there no transformations for this job");
//		}

//		final TransformationFlow flow = TransformationFlow.fromJob(job);
//
//		final Transformation transformation = job.getTransformations().get(0);
//
//		final long resourceId = transformation.getSource().getResourceId();
//		final long configurationId = transformation.getSource().getConfigurationId();

//		final Optional<Configuration> configurationOptional = schemaDataUtil.fetchConfiguration(resourceId, configurationId);
//
//		final List<String> parts = new ArrayList<String>(2);
//		parts.add("record");
//
//		if (configurationOptional.isPresent()) {
//			final String name = configurationOptional.get().getName();
//			if (name != null && !name.isEmpty()) {
//				parts.add(name);
//			}
//		}
//
//		final String recordPrefix = Joiner.on('.').join(parts);
//
//		final Optional<Iterator<Tuple<String, JsonNode>>> inputData = schemaDataUtil.getData(resourceId, configurationId);
//
//		if (!inputData.isPresent()) {
//			throw new DMPConverterException("couldn't find input data for transformation");
//		}
//
//		final Iterator<Tuple<String, JsonNode>> tupleIterator = inputData.get();

		final String result = null;

				//flow.apply(tupleIterator, new JsonNodeReader(recordPrefix));

		return buildResponse(result);
	}

	@Path("/demo")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response executeJobDemo(final String jsonObjectString) throws IOException, DMPConverterException {

		// TODO: fixme

//		final Job job;
//		try {
//			job = pojoMapperProvider.get().toJob(jsonObjectString);
//		} catch (DMPPersistenceException e) {
//			throw new DMPConverterException(e.getMessage());
//		}
//
//		final TransformationFlow flow = TransformationFlow.fromJob(job);
		final String result = null;
				// flow.applyResourceDemo(TransformationFlow.DEFAULT_RESOURCE_PATH);

		return buildResponse(result);
	}
}
