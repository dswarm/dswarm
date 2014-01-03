package de.avgl.dmp.controller.resources.job;

import java.io.IOException;
import java.util.Iterator;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.controller.utils.DataModelUtil;
import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.persistence.model.job.Job;
import de.avgl.dmp.persistence.model.job.Task;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.types.Tuple;

/**
 * A resource (controller service) for {@link Task}s.
 * 
 * @author tgaengler
 *
 */
@RequestScoped
@Api(value = "/tasks", description = "Operations about tasks.")
@Path("/tasks")
public class TasksResource {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(TasksResource.class);

	@Context
	UriInfo											uri;

	private final DataModelUtil			schemaDataUtil;

	private final DMPStatus							dmpStatus;

	private final ObjectMapper						objectMapper;

	@Inject
	public TasksResource(final DataModelUtil schemaDataUtil, final ObjectMapper objectMapperArg, final DMPStatus dmpStatusArg) {

		this.schemaDataUtil = schemaDataUtil;

		dmpStatus = dmpStatusArg;
		objectMapper = objectMapperArg;
	}

	private Response buildResponse(final String responseContent) {
		return Response.ok(responseContent).build();
	}

	// START FROM JobsResource

	@ApiOperation(value = "execute the given task", notes = "Returns the result JSON object fo this task execution.")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response executeTask(@ApiParam(value = "task (as JSON)", required = true) final String jsonObjectString) throws IOException,
			DMPConverterException {

		final Task task;

		task = objectMapper.readValue(jsonObjectString, Task.class);

		if (task == null) {

			LOG.error("couldn't parse task JSON to Task");

			throw new DMPConverterException("couldn't parse task JSON to Task");
		}

		final Job job = task.getJob();

		if (job == null) {

			LOG.error("there is no job for this task");

			throw new DMPConverterException("there is no job for this task");
		}

		if (job.getMappings() == null) {

			LOG.error("there is are no mappings for this job of this task");

			throw new DMPConverterException("there is are no mappings for this job of this task");
		}

		final DataModel inputDataModel = task.getInputDataModel();

		if (inputDataModel == null) {

			LOG.error("there is no input data model for this task");

			throw new DMPConverterException("there is no input data model for this task");
		}

		final TransformationFlow flow = TransformationFlow.fromTask(task);

		final Resource dataResource = inputDataModel.getDataResource();

		if (dataResource == null) {

			LOG.error("there is no data resource for this input data model of this task");

			throw new DMPConverterException("there is no data resource for this input data model of this task");
		}

		final Configuration configuration = inputDataModel.getConfiguration();

		if (configuration == null) {

			LOG.error("there is no configuration for this input data model of this task");

			throw new DMPConverterException("there is no configuration for this input data model of this task");
		}

		// [@tgaengler]: @phorn what is the purpose of this record prefix here?
		// final List<String> parts = new ArrayList<String>(2);
		// parts.add("record");
		//
		// final String name = configuration.getName();
		// if (name != null && !name.isEmpty()) {
		// parts.add(name);
		// }
		//
		// final String recordPrefix = Joiner.on('.').join(parts);

		//final Optional<Iterator<Tuple<String, JsonNode>>> inputData = schemaDataUtil.getData(dataResource.getId(), configuration.getId());
		final Optional<Iterator<Tuple<String, JsonNode>>> inputData = schemaDataUtil.getData(inputDataModel.getId());

		if (!inputData.isPresent()) {

			LOG.error("couldn't find input data for task execution");

			throw new DMPConverterException("couldn't find input data for task execution");
		}

		final Iterator<Tuple<String, JsonNode>> tupleIterator = inputData.get();

		// if(tupleIterator.hasNext()) {
		//
		// final Tuple<String, JsonNode> tuple = tupleIterator.next();
		//
		// System.out.println("id = '" + tuple.v1() + "'");
		//
		// final JsonNode jsonNode = tuple.v2();
		//
		// if(jsonNode != null) {
		//
		// final ObjectNode node = (ObjectNode) jsonNode;
		//
		// System.out.println("model = '" + objectMapper.writeValueAsString(node) + "'");
		// }
		// }

		final String result = flow.apply(tupleIterator);
		// flow.apply(tupleIterator, new JsonNodeReader(recordPrefix));

		return buildResponse(result);
	}

	// @Path("/demo")
	// @POST
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response executeJobDemo(final String jsonObjectString) throws IOException, DMPConverterException {
	//
	// // TODO: fixme
	//
	// // final Job job;
	// // try {
	// // job = pojoMapperProvider.get().toJob(jsonObjectString);
	// // } catch (DMPPersistenceException e) {
	// // throw new DMPConverterException(e.getMessage());
	// // }
	// //
	// // final TransformationFlow flow = TransformationFlow.fromJob(job);
	// final String result = null;
	// // flow.applyResourceDemo(TransformationFlow.DEFAULT_RESOURCE_PATH);
	//
	// return buildResponse(result);
	// }

	// END FROM JobsResource

	// START FROM TransformationsResource

	// @POST
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_XML)
	// public Response runToXML(final String jsonObjectString) throws IOException, DMPConverterException {
	//
	// // TODO: fixme
	//
	// // final Transformation transformations;
	// // try {
	// // transformations = pojoMapperProvider.get().toTransformation(jsonObjectString);
	// // } catch (DMPPersistenceException e) {
	// // throw new DMPConverterException(e.getMessage());
	// // }
	//
	// final String xml = null;
	//
	// // new MorphScriptBuilder().apply(transformations).toString();
	//
	// return buildResponse(xml);
	// }
	//
	// /**
	// * this endpoint consumes a transformations as JSON representation
	// *
	// * @param jsonObjectString a JSON representation of one transformations
	// * @return
	// * @throws IOException
	// * @throws DMPConverterException
	// * @throws DMPControllerException
	// */
	// @POST
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response runWithMetamorph(final String jsonObjectString, @QueryParam("resourceId") final Long resourceId,
	// @QueryParam("configurationId") final Long configurationId) throws IOException, DMPConverterException,
	// DMPControllerException {
	//
	// // TODO: fixme
	//
	// // final Transformation transformations;
	// //
	// // try {
	// // transformations = pojoMapperProvider.get().toTransformation(jsonObjectString);
	// // } catch (DMPPersistenceException e) {
	// // throw new DMPConverterException(e.getMessage());
	// // }
	//
	// if (resourceId == null) {
	//
	// throw new DMPControllerException("No resource id defined for this transformations task. Please set a resource id.");
	// }
	//
	// if (configurationId == null) {
	//
	// throw new
	// DMPControllerException("No configuration id defined for this transformations task. Please set a configuration id.");
	// }
	//
	// // TODO: fixme
	//
	// // final TransformationFlow flow = TransformationFlow.fromTransformation(transformations);
	//
	// // final long resourceId = transformations.getSource().getResourceId();
	// // final long configurationId = transformations.getSource().getConfigurationId();
	// //
	// // final Optional<Configuration> configurationOptional = schemaDataUtil.fetchConfiguration(resourceId, configurationId);
	//
	// // final List<String> parts = new ArrayList<String>(2);
	// // parts.add("record");
	// //
	// // if (configurationOptional.isPresent()) {
	// // final String name = configurationOptional.get().getName();
	// // if (name != null && !name.isEmpty()) {
	// // parts.add(name);
	// // }
	// // }
	// //
	// // final String recordPrefix = Joiner.on('.').join(parts);
	//
	// final Optional<Iterator<Tuple<String, JsonNode>>> inputData = schemaDataUtil.getData(resourceId, configurationId);
	//
	// if (!inputData.isPresent()) {
	//
	// throw new DMPConverterException("couldn't find input data for transformations");
	// }
	//
	// final Iterator<Tuple<String, JsonNode>> tupleIterator = inputData.get();
	//
	// // TODO: fime
	//
	// final String result = null;
	// // flow.apply(tupleIterator, new JsonNodeReader());
	//
	// return buildResponse(result);
	// }
	//
	// /**
	// * this endpoint consumes a transformations as JSON representation
	// *
	// * @param jsonObjectString a JSON representation of one transformations
	// * @return
	// * @throws IOException
	// * @throws DMPConverterException
	// */
	// @Path("/demo")
	// @POST
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response runWithMetamorphDemo(final String jsonObjectString) throws IOException, DMPConverterException {
	//
	// // TODO: fixme
	//
	// // final Transformation transformations;
	// // try {
	// // transformations = pojoMapperProvider.get().toTransformation(jsonObjectString);
	// // } catch (DMPPersistenceException e) {
	// // throw new DMPConverterException(e.getMessage());
	// // }
	//
	// final TransformationFlow flow = null;
	// // TransformationFlow.fromTransformation(transformations);
	//
	// // final String result = flow.applyResourceDemo(TransformationFlow.DEFAULT_RESOURCE_PATH);
	// final String result = "{\"status\":\"nok\",\"reason\":\"not implemented / in transition\"}";
	//
	// return buildResponse(result);
	// }

	// END FROM TransformationsResource

}
