/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.status.DMPStatus;
import org.dswarm.controller.utils.DataModelUtil;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.flow.TransformationFlow;
import org.dswarm.converter.morph.MorphScriptBuilder;
import org.dswarm.persistence.model.job.Job;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.types.Tuple;
import org.dswarm.persistence.service.InternalModelServiceFactory;
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

	private static final Logger							LOG	= LoggerFactory.getLogger(TasksResource.class);

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
	 * The metrics registry.
	 */
	private final DMPStatus dmpStatus;

	/**
	 * The object mapper that can be utilised to de-/serialise JSON nodes.
	 */
	private final ObjectMapper objectMapper;

	private final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider;

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
	public Response executeTask(@ApiParam(value = "task (as JSON)", required = true) final String jsonObjectString,
			@ApiParam("perist result set") @QueryParam("persist") @DefaultValue(value = "false") final Boolean persistResult) throws IOException,
			DMPConverterException, DMPControllerException {

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

		final DataModel inputDataModel = task.getInputDataModel();

		if (inputDataModel == null) {

			TasksResource.LOG.error("there is no input data model for this task");

			throw new DMPConverterException("there is no input data model for this task");
		}

		final Resource dataResource = inputDataModel.getDataResource();

		if (dataResource == null) {

			TasksResource.LOG.error("there is no data resource for this input data model of this task");

			throw new DMPConverterException("there is no data resource for this input data model of this task");
		}

		final Configuration configuration = inputDataModel.getConfiguration();

		if (configuration == null) {

			TasksResource.LOG.error("there is no configuration for this input data model of this task");

			throw new DMPConverterException("there is no configuration for this input data model of this task");
		}

		final TransformationFlow flow = TransformationFlow.fromTask(task, internalModelServiceFactoryProvider);

		final Optional<Iterator<Tuple<String, JsonNode>>> inputData = dataModelUtil.getData(inputDataModel.getId());

		if (!inputData.isPresent()) {

			TasksResource.LOG.error("couldn't find input data for task execution");

			throw new DMPConverterException("couldn't find input data for task execution");
		}

		final Iterator<Tuple<String, JsonNode>> tupleIterator = inputData.get();

		final boolean writeResultToDatahub;

		writeResultToDatahub = persistResult != null && persistResult;

		final String result = flow.apply(tupleIterator, writeResultToDatahub);

		if (result == null) {

			TasksResource.LOG.debug("result of task execution is null");

			return buildResponse(null);
		}

		// transform model json to fe friendly json
		final ArrayNode resultJSON = objectMapper.readValue(result, ArrayNode.class);

		if (resultJSON == null) {

			final String message = "couldn't deserialize result JSON from string";

			TasksResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		if (resultJSON.size() <= 0) {

			TasksResource.LOG.debug("result of task execution is empty");

			return buildResponse(null);
		}

		final ArrayNode feFriendlyJSON = transformModelJSONtoFEFriendlyJSON(resultJSON);

		final String resultString = objectMapper.writeValueAsString(feFriendlyJSON);

		return buildResponse(resultString);
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

	private ArrayNode transformModelJSONtoFEFriendlyJSON(final ArrayNode resultJSON) {

		final ArrayNode feFriendlyJSON = objectMapper.createArrayNode();

		for (final JsonNode entry : resultJSON) {

			final Iterator<String> fieldNamesIter = entry.fieldNames();

			if (fieldNamesIter == null || !fieldNamesIter.hasNext()) {

				continue;
			}

			final String recordURI = fieldNamesIter.next();
			final JsonNode recordContentNode = entry.get(recordURI);

			if (recordContentNode == null) {

				continue;
			}

			final ObjectNode recordNode = objectMapper.createObjectNode();
			recordNode.put(DMPPersistenceUtil.RECORD_ID, recordURI);
			recordNode.set(DMPPersistenceUtil.RECORD_DATA, recordContentNode);

			feFriendlyJSON.add(recordNode);
		}
		return feFriendlyJSON;
	}

}
