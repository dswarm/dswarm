/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.controller.resources.job;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Observer;

import org.dswarm.common.types.Tuple;
import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.utils.DataModelUtil;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.flow.TransformationFlow;
import org.dswarm.converter.flow.TransformationFlowFactory;
import org.dswarm.converter.morph.MorphScriptBuilder;
import org.dswarm.persistence.monitoring.MonitoringLogger;
import org.dswarm.persistence.monitoring.MonitoringHelper;
import org.dswarm.persistence.model.job.Job;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * A resource (controller service) for {@link Task}s.
 *
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/tasks", description = "Operations about tasks.")
@Path("/tasks")
public class TasksResource {

	private static final Logger LOG = LoggerFactory.getLogger(TasksResource.class);

	public static final String TASK_IDENTIFIER             = "task";
	public static final String AT_MOST_IDENTIFIER          = "at_most";
	public static final String PERSIST_IDENTIFIER          = "persist";
	public static final String RETURN_IDENTIFIER           = "do_not_return_data";
	public static final String SELECTED_RECORDS_IDENTIFIER = "selected_records";

	/**
	 * The base URI of this resource.
	 */
	@Context
	UriInfo uri;

	/**
	 * The data model util.
	 */
	private final DataModelUtil dataModelUtil;

	/**
	 * The object mapper that can be utilised to de-/serialise JSON nodes.
	 */
	private final ObjectMapper objectMapper;

	private final TransformationFlowFactory  transformationFlowFactory;
	private final Provider<MonitoringLogger> monitoringLogger;

	/**
	 * Creates a new resource (controller service) for {@link Transformation}s with the provider of the transformation persistence
	 * service, the object mapper and metrics registry.
	 * @param dataModelUtilArg the data model util
	 * @param objectMapperArg  an object mapper
	 * @param transformationFlowFactoryArg the factory for creating transformation flows
	 * @param monitoringLogger A logger that produces the logfiles for the monitoring
	 */
	@Inject
	public TasksResource(
			final DataModelUtil dataModelUtilArg,
			final ObjectMapper objectMapperArg,
			final TransformationFlowFactory transformationFlowFactoryArg,
			@Named("Monitoring") final Provider<MonitoringLogger> monitoringLogger) {

		dataModelUtil = dataModelUtilArg;
		objectMapper = objectMapperArg;
		transformationFlowFactory = transformationFlowFactoryArg;
		this.monitoringLogger = monitoringLogger;
	}

	/**
	 * Builds a positive response with the given content.
	 *
	 * @param responseContent a response message
	 * @return the response
	 */
	private static Response buildResponse(final String responseContent) {

		return Response.ok(responseContent).build();
	}

	/**
	 * This endpoint executes the task that is given in the request JSON representation and returns the result of the task execution. The JSON request contains besides the task some more parameters. These are:<br>
	 *     - selected_records: a set of selected record identifiers, i.e., the task will only be executed on these records
	 *     - at_most: the number of result records that should be returned at most (optional)
	 *     - persist: flag that indicates whether the result should be persisted in the datahub or not (optional)
	 *
	 * @param jsonObjectString a JSON representation of the request JSON (incl. task)
	 * @return the result of the task execution
	 * @throws IOException
	 * @throws DMPConverterException
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "execute the given task", notes = "Returns the result data (as JSON) for this task execution.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "task was successfully executed"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void executeTask(@ApiParam(value = "task execution request (as JSON)", required = true) final String jsonObjectString,
			@Suspended final AsyncResponse asyncResponse)
			throws IOException,
			DMPConverterException, DMPControllerException {

		if (jsonObjectString == null) {

			final String message = "couldn't process task execution request JSON, because it's null";

			TasksResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final ObjectNode requestJSON = objectMapper.readValue(jsonObjectString, ObjectNode.class);

		if (requestJSON == null) {

			final String message = "couldn't deserialize task execution request JSON";

			TasksResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final JsonNode taskNode = requestJSON.get(TasksResource.TASK_IDENTIFIER);

		if (taskNode == null) {

			final String message = "couldn't process task execution request JSON, because the task JSON node null";

			TasksResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final String taskNodeString = objectMapper.writeValueAsString(taskNode);

		if (taskNodeString == null) {

			final String message = "couldn't process task execution request JSON, because couldn't process task JSON node";

			TasksResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final Task task;

		task = objectMapper.readValue(taskNodeString, Task.class);

		if (task == null) {

			final String message = "couldn't parse task JSON to Task";

			TasksResource.LOG.error(message);

			throw new DMPConverterException(message);
		}

		final Job job = task.getJob();

		if (job == null) {

			final String message = "there is no job for this task";

			TasksResource.LOG.error(message);

			throw new DMPConverterException(message);
		}

		if (job.getMappings() == null) {

			final String message = "there is are no mappings for this job of this task";

			TasksResource.LOG.error(message);

			throw new DMPConverterException(message);
		}

		final DataModel inputDataModel = task.getInputDataModel();

		if (inputDataModel == null) {

			final String message = "there is no input data model for this task";

			TasksResource.LOG.error(message);

			throw new DMPConverterException(message);
		}

		final Resource dataResource = inputDataModel.getDataResource();

		if (dataResource == null) {

			final String message = "there is no data resource for this input data model of this task";

			TasksResource.LOG.error(message);

			throw new DMPConverterException(message);
		}

		final Configuration configuration = inputDataModel.getConfiguration();

		if (configuration == null) {

			final String message = "there is no configuration for this input data model of this task";

			TasksResource.LOG.error(message);

			throw new DMPConverterException(message);
		}

		final Observable<Tuple<String, JsonNode>> inputData;

		final Optional<Set<String>> optionalSelectedRecords = getStringSetValue(TasksResource.SELECTED_RECORDS_IDENTIFIER, requestJSON);

		if (optionalSelectedRecords.isPresent()) {

			// retrieve data only for selected records

			inputData = dataModelUtil.getRecordsData(optionalSelectedRecords.get(), inputDataModel.getUuid());
		} else {

			final Optional<Integer> optionalAtMost = getIntValue(TasksResource.AT_MOST_IDENTIFIER, requestJSON);

			inputData = dataModelUtil.getData(inputDataModel.getUuid(), optionalAtMost);
		}

		final boolean writeResultToDatahub = getBooleanValue(TasksResource.PERSIST_IDENTIFIER, requestJSON, false);

		final boolean returnJsonToCaller = getBooleanValue(TasksResource.RETURN_IDENTIFIER, requestJSON, true);

		final Observable<JsonNode> result;

		try (final MonitoringHelper ignore = monitoringLogger.get().startExecution(task)) {

			final TransformationFlow flow = transformationFlowFactory.fromTask(task);
			result = flow.apply(inputData, writeResultToDatahub, returnJsonToCaller);
		}

		if (result == null) {

			TasksResource.LOG.debug("result of task execution is null");

			asyncResponse.resume(Response.noContent().build());

			return;
		}

		if (!returnJsonToCaller) {

			asyncResponse.resume(Response.noContent().build());

			return;
		}

		// transform model json to fe friendly json
		final ArrayNode feFriendlyJSON = objectMapper.createArrayNode();

		result.subscribe(new Observer<JsonNode>() {

			@Override public void onCompleted() {

				final String resultString;
				try {
					resultString = objectMapper.writeValueAsString(feFriendlyJSON);

					asyncResponse.resume(buildResponse(resultString));
				} catch (JsonProcessingException e) {

					asyncResponse.resume(e);
				}
			}

			@Override public void onError(Throwable e) {

				final String message = "couldn't deserialize result JSON from string";

				TasksResource.LOG.error(message);

				asyncResponse.resume(new DMPControllerException(message));
			}

			@Override public void onNext(JsonNode jsonNode) {

				feFriendlyJSON.add(transformModelJSONtoFEFriendlyJSON(jsonNode));
			}
		});
	}

	/**
	 * This endpoint returns the metamorph script of the given task as XML.
	 *
	 * @param jsonObjectString a JSON representation of one task
	 * @return the result of the task execution
	 * @throws IOException
	 * @throws DMPConverterException
	 */
	@ApiOperation(value = "get the metamorph script of the given task", notes = "Returns the Metamorph as XML.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "metamorph could be built successfully"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_XML)
	public String renderMetamorph(@ApiParam(value = "task (as JSON)", required = true) final String jsonObjectString) throws IOException,
			DMPConverterException {

		final Task task;

		task = objectMapper.readValue(jsonObjectString, Task.class);

		if (task == null) {

			TasksResource.LOG.error("couldn't parse task JSON to Task");

			throw new DMPConverterException("couldn't parse task JSON to Task");
		}

		final Job job = task.getJob();

		if (job == null) {

			TasksResource.LOG.error("there is no job for this task");

			throw new DMPConverterException("there is no job for this task");
		}

		if (job.getMappings() == null) {

			TasksResource.LOG.error("there is are no mappings for this job of this task");

			throw new DMPConverterException("there is are no mappings for this job of this task");
		}

		return new MorphScriptBuilder().apply(task).toString();
	}

	private JsonNode transformModelJSONtoFEFriendlyJSON(final JsonNode resultJSON) {
		//
		//		final ArrayNode feFriendlyJSON = objectMapper.createArrayNode();
		//
		//		for (final JsonNode entry : resultJSON) {

		final Iterator<String> fieldNamesIter = resultJSON.fieldNames();

		if (fieldNamesIter == null || !fieldNamesIter.hasNext()) {

			return null;
		}

		final String recordURI = fieldNamesIter.next();
		final JsonNode recordContentNode = resultJSON.get(recordURI);

		if (recordContentNode == null) {

			return null;
		}

		final ObjectNode recordNode = objectMapper.createObjectNode();
		recordNode.put(DMPPersistenceUtil.RECORD_ID, recordURI);
		recordNode.set(DMPPersistenceUtil.RECORD_DATA, recordContentNode);

		//			feFriendlyJSON.add(recordNode);
		//		}
		//		return feFriendlyJSON;

		return recordNode;
	}

	private Optional<Integer> getIntValue(final String key, final JsonNode json) {

		final JsonNode node = json.get(key);
		final Optional<Integer> optionalValue;

		if (node != null) {

			optionalValue = Optional.fromNullable(node.asInt());
		} else {

			optionalValue = Optional.absent();
		}

		return optionalValue;
	}

	private boolean getBooleanValue(final String key, final JsonNode json, final boolean defaultValue) {

		final JsonNode node = json.get(key);

		if (node != null) {

			return node.asBoolean();
		} else {

			return defaultValue;
		}
	}

	private Optional<Set<String>> getStringSetValue(final String key, final JsonNode json) {

		final JsonNode node = json.get(key);
		final Optional<Set<String>> optionalValue;

		if (node != null) {

			final Set<String> set = new LinkedHashSet<>();

			for (final JsonNode entryNode : node) {

				final String entry = entryNode.asText();

				set.add(entry);
			}

			optionalValue = Optional.of(set);
		} else {

			optionalValue = Optional.absent();
		}

		return optionalValue;
	}

}
