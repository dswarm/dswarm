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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.controller.utils.DataModelUtil;
import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.persistence.model.job.Job;
import de.avgl.dmp.persistence.model.job.Task;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.types.Tuple;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;

/**
 * A resource (controller service) for {@link Task}s.
 * 
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/tasks", description = "Operations about tasks.")
@Path("/tasks")
public class TasksResource {

	private static final org.apache.log4j.Logger		LOG	= org.apache.log4j.Logger.getLogger(TasksResource.class);

	/**
	 * The base URI of this resource.
	 */
	@Context
	UriInfo												uri;

	/**
	 * The data model util.
	 */
	private final DataModelUtil							dataModelUtil;

	/**
	 * The metrics registry.
	 */
	private final DMPStatus								dmpStatus;

	/**
	 * The object mapper that can be utilised to de-/serialise JSON nodes.
	 */
	private final ObjectMapper							objectMapper;

	private final Provider<InternalModelServiceFactory>	internalModelServiceFactoryProvider;

	/**
	 * Creates a new resource (controller service) for {@link Transformation}s with the provider of the transformation persistence
	 * service, the object mapper and metrics registry.
	 * 
	 * @param dataModelUtilArg the data model util
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	@Inject
	public TasksResource(final DataModelUtil dataModelUtilArg, final ObjectMapper objectMapperArg, final DMPStatus dmpStatusArg,
			final Provider<InternalModelServiceFactory> internalModelServiceFactoryProviderArg) {

		dataModelUtil = dataModelUtilArg;
		dmpStatus = dmpStatusArg;
		objectMapper = objectMapperArg;
		internalModelServiceFactoryProvider = internalModelServiceFactoryProviderArg;
	}

	/**
	 * Builds a positive response with the given content.
	 * 
	 * @param responseContent a response message
	 * @return the response
	 */
	private Response buildResponse(final String responseContent) {

		return Response.ok(responseContent).build();
	}

	// START FROM JobsResource

	/**
	 * This endpoint executes the task that is given via its JSON representation and returns the result of the task execution.
	 * 
	 * @param jsonObjectString a JSON representation of one task
	 * @return the result of the task execution
	 * @throws IOException
	 * @throws DMPConverterException
	 * @throws DMPControllerException 
	 */
	@ApiOperation(value = "execute the given task", notes = "Returns the result data (as JSON) for this task execution.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "task was successfully executed"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response executeTask(@ApiParam(value = "task (as JSON)", required = true) final String jsonObjectString) throws IOException,
			DMPConverterException, DMPControllerException {

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

		// TODO: make write to DB optional

		final TransformationFlow flow = TransformationFlow.fromTask(task, internalModelServiceFactoryProvider);

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
		
		final Optional<Iterator<Tuple<String, JsonNode>>> inputData = dataModelUtil.getData(inputDataModel.getId());

		if (!inputData.isPresent()) {

			LOG.error("couldn't find input data for task execution");

			throw new DMPConverterException("couldn't find input data for task execution");
		}

		final Iterator<Tuple<String, JsonNode>> tupleIterator = inputData.get();

		final String result = flow.apply(tupleIterator);
		
//		if(result == null) {
//			
//			LOG.debug("result of task execution is null");
//			
//			// TODO: or throw an exception here?
//		}
//		
//		// transform model json to fe friendly json
//		final ObjectNode resultJSON = objectMapper.readValue(result, ObjectNode.class);
//		
//		if(resultJSON == null) {
//			
//			final String message = "couldn't deserialize result JSON from string";
//			
//			LOG.error(message);
//			
//			throw new DMPControllerException(message);
//		}
//		
//		if(resultJSON.size() <= 0) {
//			
//			// TODO: continue here
//		}

		return buildResponse(result);
	}

	/**
	 * This endpoint executes the task that is given via its JSON representation and returns the result of the task execution.
	 * 
	 * @param jsonObjectString a JSON representation of one task
	 * @return the result of the task execution
	 * @throws IOException
	 * @throws DMPConverterException
	 */
	@ApiOperation(value = "get the metamorph script of the given task", notes = "Returns the Metamorph as XML.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "metamorph could be built successfully"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_XML)
	public String renderMetamorph(@ApiParam(value = "task (as JSON)", required = true) final String jsonObjectString) throws IOException,
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

		final TransformationFlow flow = TransformationFlow.fromTask(task, internalModelServiceFactoryProvider);

		return flow.getScript();
	}

}
