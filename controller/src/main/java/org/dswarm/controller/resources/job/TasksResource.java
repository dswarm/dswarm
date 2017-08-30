/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.xml.stream.XMLStreamException;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import javaslang.Tuple2;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import org.dswarm.common.MediaTypeUtil;
import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.utils.DataModelUtil;
import org.dswarm.controller.utils.JsonUtils;
import org.dswarm.controller.utils.ResourceUtils;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.export.QuadRDFExporter;
import org.dswarm.converter.export.RDFExporter;
import org.dswarm.converter.export.SolrUpdateXMLExporter;
import org.dswarm.converter.export.TripleRDFExporter;
import org.dswarm.converter.export.XMLExporter;
import org.dswarm.converter.flow.GDMModelTransformationFlow;
import org.dswarm.converter.flow.GDMModelTransformationFlowFactory;
import org.dswarm.converter.morph.MorphScriptBuilder;
import org.dswarm.graph.json.Model;
import org.dswarm.graph.json.stream.ModelBuilder;
import org.dswarm.graph.json.util.Util;
import org.dswarm.persistence.DMPPersistenceError;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.internal.gdm.GDMModelUtil;
import org.dswarm.persistence.model.job.Job;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.resource.utils.ResourceStatics;
import org.dswarm.persistence.monitoring.MonitoringHelper;
import org.dswarm.persistence.monitoring.MonitoringLogger;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.dswarm.persistence.util.GDMUtil;

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

	public static final String TASK_IDENTIFIER = "task";
	public static final String AT_MOST_IDENTIFIER = "at_most";
	public static final String RETURN_AT_MOST_IDENTIFIER = "return_at_most";
	public static final String PERSIST_IDENTIFIER = "persist";
	public static final String RETURN_IDENTIFIER = "do_not_return_data";
	public static final String SELECTED_RECORDS_IDENTIFIER = "selected_records";
	public static final String DO_INGEST_ON_THE_FLY_IDENTIFIER = "do_ingest_on_the_fly";
	public static final String UTILISE_EXISTING_INPUT_SCHEMA_IDENTIFIER = "utilise_existing_input_schema";
	public static final String DO_VERSIONING_ON_RESULT_IDENTIFIER = "do_versioning_on_result";

	private static final String DSWARM_INGEST_THREAD_NAMING_PATTERN = "dswarm-ingest-%d";

	private static final ExecutorService INGEST_EXECUTOR_SERVICE = Executors
			.newCachedThreadPool(
					new BasicThreadFactory.Builder().daemon(false).namingPattern(DSWARM_INGEST_THREAD_NAMING_PATTERN).build());
	private static final Scheduler INGEST_SCHEDULER = Schedulers.from(INGEST_EXECUTOR_SERVICE);

	private static final String DSWARM_TRANSFORMATION_ENGINE_THREAD_NAMING_PATTERN = "dswarm-transformation-engine-%d";

	private static final ExecutorService TRANSFORMATION_ENGINE_EXECUTOR_SERVICE = Executors
			.newCachedThreadPool(
					new BasicThreadFactory.Builder().daemon(false).namingPattern(DSWARM_TRANSFORMATION_ENGINE_THREAD_NAMING_PATTERN).build());
	private static final Scheduler TRANSFORMATION_ENGINE_SCHEDULER = Schedulers.from(TRANSFORMATION_ENGINE_EXECUTOR_SERVICE);

	private static final String DSWARM_EXPORT_THREAD_NAMING_PATTERN = "dswarm-export-%d";

	private static final ExecutorService EXPORT_EXECUTOR_SERVICE = Executors
			.newCachedThreadPool(
					new BasicThreadFactory.Builder().daemon(false).namingPattern(DSWARM_EXPORT_THREAD_NAMING_PATTERN).build());
	private static final Scheduler EXPORT_SCHEDULER = Schedulers.from(EXPORT_EXECUTOR_SERVICE);
	private static final String ERROR_IDENTIFIER = "error";
	private static final String MESSAGE_IDENTIFIER = "message";
	private static final String STACKTRACE_IDENTIFIER = "stacktrace";

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

	private final GDMModelTransformationFlowFactory transformationFlowFactory;
	private final Provider<MonitoringLogger> monitoringLogger;

	/**
	 * Creates a new resource (controller service) for {@link Transformation}s with the provider of the transformation persistence
	 * service, the object mapper and metrics registry.
	 *
	 * @param dataModelUtilArg             the data model util
	 * @param objectMapperArg              an object mapper
	 * @param transformationFlowFactoryArg the factory for creating transformation flows
	 * @param monitoringLogger             A logger that produces the logfiles for the monitoring
	 */
	@Inject
	public TasksResource(
			final DataModelUtil dataModelUtilArg,
			final ObjectMapper objectMapperArg,
			final GDMModelTransformationFlowFactory transformationFlowFactoryArg,
			@Named("Monitoring") final Provider<MonitoringLogger> monitoringLogger) {

		dataModelUtil = dataModelUtilArg;
		objectMapper = objectMapperArg;
		transformationFlowFactory = transformationFlowFactoryArg;
		this.monitoringLogger = monitoringLogger;
	}

	/**
	 * This endpoint executes the task that is given in the request JSON representation and returns the result of the task execution. The JSON request contains besides the task some more parameters. These are:<br>
	 * - selected_records: a set of selected record identifiers, i.e., the task will only be executed on these records
	 * - at_most: the number of result records that should be returned at most (optional)
	 * - persist: flag that indicates whether the result should be persisted in the datahub or not (optional)
	 * <p>
	 * returns the result of the task execution in the requested format (media type, e.g., "application/json", "application/solr+update+xml", "application/xml", "application/n-triples", "application/n-quads", "application/trig", "application/gdm+json")
	 *
	 * @param jsonObjectString a JSON representation of the request JSON (incl. task)
	 * @throws IOException
	 * @throws DMPConverterException
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "execute the given task", notes = "Returns the result data (as JSON) for this task execution.")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "task was successfully executed"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)")})
	@Timed
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({MediaType.APPLICATION_JSON,
			MediaTypeUtil.SOLR_UPDATE_XML,
			MediaType.APPLICATION_XML,
			MediaTypeUtil.N_TRIPLES,
			MediaTypeUtil.TURTLE,
			MediaTypeUtil.N_QUADS,
			MediaTypeUtil.TRIG,
			MediaTypeUtil.TRIX,
			MediaTypeUtil.RDF_THRIFT,
			MediaTypeUtil.GDM_JSON,
			MediaTypeUtil.GDM_COMPACT_JSON,
			MediaTypeUtil.GDM_COMPACT_FE_JSON,
			MediaTypeUtil.GDM_SIMPLE_JSON,
			MediaTypeUtil.GDM_SIMPLE_SHORT_JSON,
			MediaTypeUtil.JSC_JSON,
			MediaTypeUtil.JSC_LDJ})
	public void executeTask(@ApiParam(value = "task execution request (as JSON)", required = true) final String jsonObjectString,
	                        @Context final HttpHeaders requestHeaders,
	                        @Suspended final AsyncResponse asyncResponse) throws IOException, DMPConverterException, DMPControllerException {

		final String headers = ResourceUtils.readHeaders(requestHeaders);

		TasksResource.LOG.debug("try to process task with\n{}", headers);

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

		final DataModel inputDataModel = getInputDataModel(task);

		//check schema equality, i.e. is input schema equals to output schema
		final boolean isOutputSchemaEqualsToInputSchema = isOutputSchemaEqualsToInputSchema(task, inputDataModel);

		//check job / mappings
		final boolean hasMappings = hasMappings(task);

		if (isOutputSchemaEqualsToInputSchema && !hasMappings) {

			// do simple format converting

			final ConnectableObservable<Tuple2<String, JsonNode>> connectableInputData = Observable.from(Collections.<Tuple2<String, JsonNode>>emptyList()).publish();
			final ConnectableObservable<GDMModel> connectableResult = getInputDataAsGDMModel(requestJSON, task, inputDataModel).publish();

			doExport(requestHeaders, asyncResponse, task, connectableResult, connectableInputData);

			return;
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

		final Observable<Tuple2<String, JsonNode>> inputData = getInputDataAndMapToMappingInputFormat(requestJSON, task, inputDataModel);

		final ConnectableObservable<Tuple2<String, JsonNode>> connectableInputData = inputData.publish();

		final boolean writeResultToDatahub = JsonUtils.getBooleanValue(TasksResource.PERSIST_IDENTIFIER, requestJSON, false);

		final boolean doNotReturnJsonToCaller = JsonUtils.getBooleanValue(TasksResource.RETURN_IDENTIFIER, requestJSON, false);

		final boolean doVersioningOnResult = JsonUtils.getBooleanValue(TasksResource.DO_VERSIONING_ON_RESULT_IDENTIFIER, requestJSON, true);

		if (!doVersioningOnResult) {

			TasksResource.LOG.debug("skip result versioning");
		}

		final ConnectableObservable<GDMModel> connectableResult;

		try (final MonitoringHelper ignore = monitoringLogger.get().startExecution(task)) {

			final GDMModelTransformationFlow flow = transformationFlowFactory.fromTask(task);
			final ConnectableObservable<GDMModel> apply = flow.apply(connectableInputData, writeResultToDatahub, doNotReturnJsonToCaller, doVersioningOnResult, TRANSFORMATION_ENGINE_SCHEDULER);
			final Observable<GDMModel> buffer = apply.observeOn(TRANSFORMATION_ENGINE_SCHEDULER)
					.onBackpressureBuffer(10000);

			final Optional<Integer> optionalReturnAtMost = JsonUtils.getIntValue(TasksResource.RETURN_AT_MOST_IDENTIFIER, requestJSON);
			final Observable<GDMModel> returnAtMost;

			if (optionalReturnAtMost.isPresent()) {

				final Integer count = optionalReturnAtMost.get();

				TasksResource.LOG.debug("return at most '{}' records for task execution on task '{}' with input data model '{}' (input data resource = '{}')", count, task.getUuid(), inputDataModel.getUuid(), getInputDataResourceFileName(task));

				returnAtMost = buffer.take(count);
			} else {

				returnAtMost = buffer;
			}

			connectableResult = returnAtMost.publish();
			apply.connect();
		}

		if (doNotReturnJsonToCaller) {

			returnEmptyResponse(asyncResponse, connectableResult);

			connectableResult.connect();
			connectableInputData.connect();

			return;
		}

		doExport(requestHeaders, asyncResponse, task, connectableResult, connectableInputData);
	}

	private boolean hasMappings(final Task task) throws DMPConverterException {

		final Job job = task.getJob();

		if (job == null) {

			return false;
		}

		final Set<Mapping> mappings = job.getMappings();
		if (mappings == null) {

			return false;
		}

		if (!mappings.isEmpty()) {

			return false;
		}

		return true;
	}

	private boolean isOutputSchemaEqualsToInputSchema(final Task task, final DataModel inputDataModel) {

		final Schema inputSchema = inputDataModel.getSchema();

		if (inputSchema == null) {

			return false;
		}

		final DataModel outputDataModel = task.getOutputDataModel();

		if (outputDataModel == null) {

			return false;
		}

		final Schema outputSchema = outputDataModel.getSchema();

		if (outputSchema == null) {

			return false;
		}

		if (!(inputSchema.getUuid() != null && outputSchema.getUuid() != null && inputSchema.getUuid().equals(outputSchema.getUuid()))) {

			return false;
		}

		return true;
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
	@ApiResponses(value = {@ApiResponse(code = 200, message = "metamorph could be built successfully"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)")})
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

	private Observable<Tuple2<String, JsonNode>> getInputDataAndMapToMappingInputFormat(final ObjectNode requestJSON,
	                                                                                   final Task task,
	                                                                                   final DataModel inputDataModel) throws DMPControllerException {

		final boolean doIngestOnTheFly = JsonUtils.getBooleanValue(TasksResource.DO_INGEST_ON_THE_FLY_IDENTIFIER, requestJSON, false);

		if (doIngestOnTheFly) {

			LOG.debug("do ingest on-the-fly for task execution of task '{}'", task.getUuid());

			DataModelUtil.checkDataResource(inputDataModel);

			final boolean utiliseExistingInputSchema = JsonUtils.getBooleanValue(TasksResource.UTILISE_EXISTING_INPUT_SCHEMA_IDENTIFIER, requestJSON,
					false);

			return dataModelUtil.doIngestAndMapToMappingInputFormat(inputDataModel, utiliseExistingInputSchema, INGEST_SCHEDULER);
		}

		final Optional<Set<String>> optionalSelectedRecords = JsonUtils.getStringSetValue(TasksResource.SELECTED_RECORDS_IDENTIFIER, requestJSON);

		if (optionalSelectedRecords.isPresent()) {

			// retrieve data only for selected records

			return dataModelUtil.getRecordsDataAndMapToMappingInputFormat(optionalSelectedRecords.get(), inputDataModel.getUuid());
		}

		final Optional<Integer> optionalAtMost = JsonUtils.getIntValue(TasksResource.AT_MOST_IDENTIFIER, requestJSON);

		if (optionalAtMost.isPresent()) {

			final Integer count = optionalAtMost.get();

			TasksResource.LOG.debug("do task execution on task '{}' with input data model '{}' for '{}' records", task.getUuid(), inputDataModel.getUuid(), count);
		}

		return dataModelUtil.getDataAndMapToMappingInputFormat(inputDataModel.getUuid(), optionalAtMost);
	}

	private Observable<GDMModel> getInputDataAsGDMModel(final ObjectNode requestJSON,
	                                                    final Task task,
	                                                    final DataModel inputDataModel) throws DMPControllerException {

		final boolean doIngestOnTheFly = JsonUtils.getBooleanValue(TasksResource.DO_INGEST_ON_THE_FLY_IDENTIFIER, requestJSON, false);

		if (doIngestOnTheFly) {

			LOG.debug("do ingest on-the-fly for task execution of task '{}' (input data resource = '{}')", task.getUuid(), getInputDataResourceFileName(task));

			DataModelUtil.checkDataResource(inputDataModel);

			final boolean utiliseExistingInputSchema = JsonUtils.getBooleanValue(TasksResource.UTILISE_EXISTING_INPUT_SCHEMA_IDENTIFIER, requestJSON,
					false);

			return dataModelUtil.doIngest(inputDataModel, utiliseExistingInputSchema, INGEST_SCHEDULER);
		}

		final Optional<Set<String>> optionalSelectedRecords = JsonUtils.getStringSetValue(TasksResource.SELECTED_RECORDS_IDENTIFIER, requestJSON);

		if (optionalSelectedRecords.isPresent()) {

			// retrieve data only for selected records

			return dataModelUtil.getRecordsDataAsGDMModel(optionalSelectedRecords.get(), inputDataModel.getUuid());
		}

		final Optional<Integer> optionalAtMost = JsonUtils.getIntValue(TasksResource.AT_MOST_IDENTIFIER, requestJSON);

		if (optionalAtMost.isPresent()) {

			final Integer count = optionalAtMost.get();

			TasksResource.LOG.debug("do task execution on task '{}' with input data model '{}' (input data resource = '{}') for '{}' records", task.getUuid(), inputDataModel.getUuid(), getInputDataResourceFileName(task), count);
		}

		return dataModelUtil.getDataAsGDMModel(inputDataModel.getUuid(), optionalAtMost);
	}

	private void doExport(final HttpHeaders requestHeaders,
	                      final AsyncResponse asyncResponse,
	                      final Task task,
	                      final ConnectableObservable<GDMModel> connectableResult,
	                      final ConnectableObservable<Tuple2<String, JsonNode>> connectableInputData) throws DMPControllerException {

		LOG.debug("do export for task execution of task '{}' (input data resource = '{}')", task.getUuid(), getInputDataResourceFileName(task));

		final Optional<MediaType> optionalResponseMediaType = determineResponseMediaType(requestHeaders);

		if (!optionalResponseMediaType.isPresent()) {

			// media type is not supported
			asyncResponse.resume(Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build());

			return;
		}

		final MediaType responseMediaType = optionalResponseMediaType.get();

		final Future<StreamingOutput> futureStream = CompletableFuture.supplyAsync(() -> {

			final CountDownLatch countDownLatch = new CountDownLatch(1);

			return os -> generateResponseOutputStream(asyncResponse, task, connectableResult, connectableInputData, responseMediaType, countDownLatch, os);
		}, EXPORT_EXECUTOR_SERVICE);

		try {

			EXPORT_EXECUTOR_SERVICE.submit(() -> {

				try {

					LOG.debug("do async task execution response");

					asyncResponse.resume(Response.ok(futureStream.get(), responseMediaType).build());
				} catch (final InterruptedException | ExecutionException e) {

					final String message = "something went wrong";

					LOG.error(message, e);

					throw new RuntimeException(message, e);
				}
			});
		} catch (final RuntimeException e) {

			final String message = "something went wrong";

			throw new DMPControllerException(message, e);
		}
	}

	private void generateResponseOutputStream(final AsyncResponse asyncResponse,
	                                          final Task task,
	                                          final ConnectableObservable<GDMModel> connectableResult,
	                                          final ConnectableObservable<Tuple2<String, JsonNode>> connectableInputData,
	                                          final MediaType responseMediaType,
	                                          final CountDownLatch countDownLatch,
	                                          final OutputStream os) {

		try {

			LOG.debug("start preparing {} export", responseMediaType.toString());

			final BufferedOutputStream bos = new BufferedOutputStream(os, 1024);

			final Observable<Void> resultObservable;

			switch (responseMediaType.toString()) {

				case MediaType.APPLICATION_JSON:

					resultObservable = doJSONExport(connectableResult.observeOn(EXPORT_SCHEDULER), responseMediaType, bos);

					break;
				case MediaTypeUtil.SOLR_UPDATE_XML:

					resultObservable = doSolrUpdateXMLExport(connectableResult.observeOn(EXPORT_SCHEDULER), responseMediaType, bos);

					break;
				case MediaType.APPLICATION_XML:

					resultObservable = doXMLExport(connectableResult.observeOn(EXPORT_SCHEDULER), responseMediaType, bos, task);

					break;
				case MediaTypeUtil.N_TRIPLES:
				case MediaTypeUtil.TURTLE:

					resultObservable = doTripleRDFExport(connectableResult.observeOn(EXPORT_SCHEDULER), responseMediaType, bos);

					break;
				case MediaTypeUtil.N_QUADS:
				case MediaTypeUtil.TRIG:
				case MediaTypeUtil.TRIX:
				case MediaTypeUtil.RDF_THRIFT:

					resultObservable = doQuadRDFExport(connectableResult.observeOn(EXPORT_SCHEDULER), responseMediaType, bos, task);

					break;
				case MediaTypeUtil.GDM_JSON:

					resultObservable = doGDMJSONExport(connectableResult.observeOn(EXPORT_SCHEDULER), responseMediaType, bos);

					break;
				case MediaTypeUtil.GDM_COMPACT_JSON:

					resultObservable = doGDMCompactJSONExport(connectableResult.observeOn(EXPORT_SCHEDULER), responseMediaType, bos);

					break;
				case MediaTypeUtil.GDM_COMPACT_FE_JSON:

					resultObservable = doGDMCompactFEJSONExport(connectableResult.observeOn(EXPORT_SCHEDULER), responseMediaType, bos);

					break;
				case MediaTypeUtil.GDM_SIMPLE_JSON:

					resultObservable = doGDMSimpleJSONExport(connectableResult.observeOn(EXPORT_SCHEDULER), responseMediaType, bos);

					break;
				case MediaTypeUtil.GDM_SIMPLE_SHORT_JSON:

					resultObservable = doGDMSimpleShortJSONExport(connectableResult.observeOn(EXPORT_SCHEDULER), responseMediaType, bos);

					break;
				case MediaTypeUtil.JSC_JSON:

					resultObservable = doJSCJSONExport(connectableResult.observeOn(EXPORT_SCHEDULER), responseMediaType, bos, task);

					break;
				case MediaTypeUtil.JSC_LDJ:

					resultObservable = doJSCLDJExport(connectableResult.observeOn(EXPORT_SCHEDULER), responseMediaType, bos, task);

					break;
				default:

					// media type is not supported
					asyncResponse.resume(Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build());

					countDownLatch.countDown();

					return;
			}

			connectableResult.connect();

			resultObservable.observeOn(EXPORT_SCHEDULER)
					.doOnSubscribe(() -> LOG.debug("subscribed to {} export in task resource", responseMediaType.toString()))
					.doOnCompleted(() -> {

						try {

							bos.flush();
							os.flush();
							bos.close();
							os.close();
						} catch (final IOException e) {

							asyncResponse.resume(e);
						} finally {

							countDownLatch.countDown();
						}

						LOG.debug("finished transforming GDM to {}", responseMediaType.toString());
					})
					.doOnError(throwable -> {

						final String message = String.format("couldn't process task '%s' (maybe %s export; input data resource = '%s') successfully", task.getUuid(), responseMediaType.toString(), getInputDataResourceFileName(task));

						TasksResource.LOG.error(message, throwable);

						try {

							// write error to output stream

							final StringWriter stringWriter = new StringWriter();
							final PrintWriter printWriter = new PrintWriter(stringWriter);
							throwable.printStackTrace(printWriter);

							final ObjectNode errorJSON = objectMapper.createObjectNode();
							final ObjectNode innerErrorJSON = objectMapper.createObjectNode();

							errorJSON.set(ERROR_IDENTIFIER, innerErrorJSON);
							innerErrorJSON.put(MESSAGE_IDENTIFIER, message);
							innerErrorJSON.put(STACKTRACE_IDENTIFIER, stringWriter.toString());

							stringWriter.close();
							printWriter.close();

							bos.write(objectMapper.writeValueAsBytes(errorJSON));

							bos.close();
							os.close();
						} catch (IOException e) {

							final String message2 = String.format("couldn't process task '%s' (maybe %s export; input data resource = '%s') successfully", task.getUuid(), responseMediaType.toString(), getInputDataResourceFileName(task));

							TasksResource.LOG.error(message2, e);

							asyncResponse.resume(new DMPControllerException(message, throwable));
						} finally {

							countDownLatch.countDown();
						}
					}).subscribe();

			connectableInputData.connect();

			// just for count down latch, taken from http://christopher-batey.blogspot.de/2014/12/streaming-large-payloads-over-http-from.html
			try {

				countDownLatch.await();
			} catch (InterruptedException e) {

				LOG.warn("Current thread interrupted, resetting flag");

				Thread.currentThread().interrupt();
			}
		} catch (final XMLStreamException | DMPConverterException | DMPControllerException e) {

			final String message = String.format("couldn't process task '%s' (maybe %s export; input data resource = '%s') successfully", task.getUuid(), responseMediaType.toString(), getInputDataResourceFileName(task));

			TasksResource.LOG.error(message, e);

			asyncResponse.resume(new DMPControllerException(message, e));
		}
	}

	private Observable<Void> doJSONExport(final Observable<GDMModel> gdmModelObservable,
	                                      final MediaType responseMediaType,
	                                      final BufferedOutputStream bos) throws DMPControllerException {

		return doGDMJSONExport(gdmModelObservable, responseMediaType, bos, GDMModel::toJSON);
	}

	private Observable<Void> doGDMCompactJSONExport(final Observable<GDMModel> gdmModelObservable,
	                                                final MediaType responseMediaType,
	                                                final BufferedOutputStream bos) throws DMPControllerException {

		return doGDMJSONExport(gdmModelObservable, responseMediaType, bos, GDMModel::toGDMCompactJSON);
	}

	private Observable<Void> doGDMSimpleJSONExport(final Observable<GDMModel> gdmModelObservable,
	                                               final MediaType responseMediaType,
	                                               final BufferedOutputStream bos) throws DMPControllerException {

		return doGDMJSONExport(gdmModelObservable, responseMediaType, bos, GDMModel::toGDMSimpleJSON);
	}

	private Observable<Void> doGDMSimpleShortJSONExport(final Observable<GDMModel> gdmModelObservable,
	                                                    final MediaType responseMediaType,
	                                                    final BufferedOutputStream bos) throws DMPControllerException {

		return doGDMJSONExport(gdmModelObservable, responseMediaType, bos, GDMModel::toGDMSimpleShortJSON);
	}

	private Observable<Void> doJSCJSONExport(final Observable<GDMModel> gdmModelObservable,
	                                         final MediaType responseMediaType,
	                                         final BufferedOutputStream bos,
	                                         final Task task) throws DMPControllerException {

		final Func1<GDMModel, JsonNode> transformationFunction = toJSCJSONRecord(task);

		return doGDMJSONExport(gdmModelObservable, responseMediaType, bos, transformationFunction);
	}

	private Observable<Void> doJSCLDJExport(final Observable<GDMModel> gdmModelObservable,
	                                        final MediaType responseMediaType,
	                                        final BufferedOutputStream bos,
	                                        final Task task) throws DMPControllerException {

		final Func1<GDMModel, JsonNode> transformationFunction = toJSCJSONRecord(task);

		return doGDMLDJExport(gdmModelObservable, responseMediaType, bos, transformationFunction);
	}

	private Observable<Void> doGDMJSONExport(final Observable<GDMModel> gdmModelObservable,
	                                         final MediaType responseMediaType,
	                                         final BufferedOutputStream bos) throws DMPControllerException {

		final AtomicInteger resultCounter = new AtomicInteger(0);

		try {

			final ModelBuilder modelBuilder = new ModelBuilder(bos);

			return gdmModelObservable.onBackpressureBuffer(10000)
					.doOnSubscribe(() -> TasksResource.LOG.debug("subscribed to {} export", responseMediaType))
					.doOnNext(resultObj -> {

						resultCounter.incrementAndGet();

						if (resultCounter.get() == 1) {

							TasksResource.LOG.debug("received first result for {} export in task resource", responseMediaType);
						}
					})
					.map(GDMModel::getModel)
					.map(Model::getResources)
					.map(resources -> {

						resources.forEach(resource -> {

							try {

								modelBuilder.addResource(resource);
								bos.flush();
							} catch (final IOException e) {

								throw DMPPersistenceError.wrap(new DMPPersistenceException("something went wrong while serialising the GDM JSON", e));
							}
						});

						return resources;
					})
					.onBackpressureBuffer(10000)
					.doOnCompleted(() -> {

						TasksResource.LOG.debug("received '{}' results for {} export in task resource overall", resultCounter.get(), responseMediaType);

						try {

							modelBuilder.build();
						} catch (final IOException e) {

							throw DMPPersistenceError.wrap(new DMPPersistenceException("something went wrong while building the GDM JSON", e));
						}
					})
					.ignoreElements().cast(Void.class);
		} catch (final IOException e) {

			throw new DMPControllerException("something went wrong while serialising the GDM JSON", e);
		}
	}

	private Observable<Void> doSolrUpdateXMLExport(final Observable<GDMModel> result,
	                                               final MediaType responseMediaType,
	                                               final BufferedOutputStream bos) throws XMLStreamException, DMPConverterException {

		final SolrUpdateXMLExporter solrUpdateXMLExporter = new SolrUpdateXMLExporter();

		LOG.debug("trigger {} export", responseMediaType.toString());

		final ConnectableObservable<JsonNode> jsonResult = generateGDMCompactJSONResult(result);

		final Observable<Void> resultObservable = solrUpdateXMLExporter.generate(jsonResult, bos).ignoreElements().cast(Void.class);

		jsonResult.connect();

		return resultObservable;
	}

	private Observable<Void> doXMLExport(final Observable<GDMModel> result,
	                                     final MediaType responseMediaType,
	                                     final BufferedOutputStream bos,
	                                     final Task task) throws XMLStreamException, DMPConverterException {

		// collect input parameter for exporter

		final DataModel finalOutputDataModel = getOutputDataModel(task);

		// record tag
		final Optional<Configuration> optionalConfiguration = Optional.ofNullable(finalOutputDataModel.getConfiguration());
		final Optional<String> optionalRecordTag = DataModelUtil.determineRecordTag(optionalConfiguration);
		final Optional<String> java8OptionalRecordTag = guavaOptionalToJava8Optional(optionalRecordTag);

		// record class uri
		final Optional<String> optionalRecordClassURI = DataModelUtil.determineRecordClassURI(finalOutputDataModel);

		// original data model type
		final Optional<String> optionalOriginalDataModelType = DataModelUtil.determineOriginalDataModelType(finalOutputDataModel,
				optionalConfiguration);
		final Optional<String> java8OptionalOriginalDataModelType = guavaOptionalToJava8Optional(
				optionalOriginalDataModelType);

		final String recordClassUri = optionalRecordClassURI.get();

		LOG.debug("create {} exporter with: record tag = '{}' :: record class URI = '{}' :: original data model type = '{}'",
				responseMediaType.toString(), optionalRecordTag, recordClassUri, java8OptionalOriginalDataModelType);

		final XMLExporter xmlExporter = new XMLExporter(java8OptionalRecordTag, recordClassUri, Optional.empty(), java8OptionalOriginalDataModelType);

		LOG.debug("trigger {} export", responseMediaType.toString());

		final ConnectableObservable<JsonNode> jsonResult = generateGDMCompactJSONResult(result);

		final Observable<Void> resultObservable = xmlExporter.generate(jsonResult, bos).ignoreElements().cast(Void.class);

		jsonResult.connect();

		return resultObservable;
	}

	private Observable<Void> doTripleRDFExport(final Observable<GDMModel> result,
	                                           final MediaType responseMediaType,
	                                           final BufferedOutputStream bos) throws XMLStreamException {

		final RDFExporter rdfExporter = new TripleRDFExporter(responseMediaType);

		return doRDFExport(result, bos, rdfExporter);
	}

	private Observable<Void> doQuadRDFExport(final Observable<GDMModel> result,
	                                         final MediaType responseMediaType,
	                                         final BufferedOutputStream bos,
	                                         final Task task) throws XMLStreamException, DMPConverterException {

		final DataModel finalOutputDataModel = getOutputDataModel(task);
		final String dataModelUuid = finalOutputDataModel.getUuid();
		final String dataModelURI = GDMUtil.getDataModelGraphURI(dataModelUuid);

		final RDFExporter rdfExporter = new QuadRDFExporter(responseMediaType, dataModelURI);

		return doRDFExport(result, bos, rdfExporter);
	}

	private Observable<Void> doRDFExport(final Observable<GDMModel> result,
	                                     final BufferedOutputStream bos,
	                                     final RDFExporter rdfExporter) throws XMLStreamException {

		final ConnectableObservable<GDMModel> publish = result.publish();
		final Observable<JsonNode> generate = rdfExporter.generate(result, bos);
		publish.connect();

		return generate.ignoreElements().cast(Void.class);
	}

	private DataModel getInputDataModel(final Task task) throws DMPConverterException {

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
		return inputDataModel;
	}

	private DataModel getOutputDataModel(final Task task) throws DMPConverterException {

		final DataModel outputDataModel = task.getOutputDataModel();

		if (outputDataModel == null) {

			final String message = "there is no output data model for this task";

			TasksResource.LOG.error(message);

			throw new DMPConverterException(message);
		}

		final Optional<DataModel> optionalFreshOutputDataModel = dataModelUtil.fetchDataModel(outputDataModel.getUuid());

		final DataModel finalOutputDataModel;

		if (optionalFreshOutputDataModel.isPresent()) {

			finalOutputDataModel = optionalFreshOutputDataModel.get();
		} else {

			finalOutputDataModel = outputDataModel;
		}

		return finalOutputDataModel;
	}

	private Optional<MediaType> determineResponseMediaType(final HttpHeaders requestHeaders) {

		final List<MediaType> acceptableMediaTypes = requestHeaders.getAcceptableMediaTypes();

		if (acceptableMediaTypes == null || acceptableMediaTypes.isEmpty()) {

			// default media type is application/json
			return Optional.of(MediaType.APPLICATION_JSON_TYPE);
		}

		// TODO: this might be improved
		final Optional<MediaType> mediaTypeOptional = acceptableMediaTypes.stream()
				.filter(mediaType -> MediaType.APPLICATION_JSON_TYPE.equals(mediaType)
						|| MediaType.APPLICATION_XML_TYPE.equals(mediaType)
						|| MediaTypeUtil.SOLR_UPDATE_XML_TYPE.equals(mediaType)
						|| MediaTypeUtil.N_TRIPLES_TYPE.equals(mediaType)
						|| MediaTypeUtil.TURTLE_TYPE.equals(mediaType)
						|| MediaTypeUtil.N_QUADS_TYPE.equals(mediaType)
						|| MediaTypeUtil.TRIG_TYPE.equals(mediaType)
						|| MediaTypeUtil.TRIX_TYPE.equals(mediaType)
						|| MediaTypeUtil.RDF_THRIFT_TYPE.equals(mediaType)
						|| MediaTypeUtil.GDM_JSON_TYPE.equals(mediaType)
						|| MediaTypeUtil.GDM_COMPACT_JSON_TYPE.equals(mediaType)
						|| MediaTypeUtil.GDM_COMPACT_FE_JSON_TYPE.equals(mediaType)
						|| MediaTypeUtil.GDM_SIMPLE_JSON_TYPE.equals(mediaType)
						|| MediaTypeUtil.GDM_SIMPLE_SHORT_JSON_TYPE.equals(mediaType)
						|| MediaTypeUtil.JSC_JSON_TYPE.equals(mediaType)
						|| MediaTypeUtil.JSC_LDJ_TYPE.equals(mediaType))
				.findFirst();

		if (mediaTypeOptional.isPresent()) {

			return mediaTypeOptional;
		}

		// media type is not supported

		return Optional.empty();
	}

	private void returnEmptyResponse(final AsyncResponse asyncResponse,
	                                 final Observable<GDMModel> result) {

		result.subscribe(new Observer<GDMModel>() {

			@Override
			public void onCompleted() {

				TasksResource.LOG.debug("processed task successfully, don't return data to caller");

				asyncResponse.resume(Response.noContent().build());
			}

			@Override
			public void onError(final Throwable e) {

				final String message = "couldn't process task successfully";

				TasksResource.LOG.error(message, e);

				asyncResponse.resume(new DMPControllerException(message, e));
			}

			@Override
			public void onNext(final GDMModel jsonNode) {

				// nothing to do here
			}
		});
	}

	private Observable<Void> doGDMCompactFEJSONExport(final Observable<GDMModel> result,
	                                                  final MediaType responseMediaType,
	                                                  final BufferedOutputStream bos) throws DMPControllerException {

		// transform model json to fe friendly json
		final ArrayNode feFriendlyJSON = objectMapper.createArrayNode();
		final AtomicInteger counter = new AtomicInteger(0);

		// TODO rewrite this part up from here to properly utilise the reactive pattern

		final ConnectableObservable<JsonNode> jsonResult = generateGDMCompactJSONResult(result);

		final Observable<Void> voidObservable = jsonResult.doOnSubscribe(() -> TasksResource.LOG.debug("subscribed to {} export on task resource", responseMediaType))
				.doOnCompleted(() -> {

					try {

						objectMapper.writeValue(bos, feFriendlyJSON);

						TasksResource.LOG.debug("processed task successfully, return data to caller; received '{}' records overall", counter.get());
					} catch (final IOException e) {

						final String message = String.format("something went wrong while serializing the %s data", responseMediaType);

						TasksResource.LOG.error(message, e);

						throw new RuntimeException(message, e);
					}
				})
				.doOnError(e -> TasksResource.LOG.error("couldn't deserialize result {} from string", responseMediaType, e))
				.doOnNext(jsonNode -> {

					counter.incrementAndGet();

					if (counter.get() == 1) {

						TasksResource.LOG.debug("recieved first record for JSON export in task resource");
					}

					feFriendlyJSON.add(transformGDMCompactJSONtoGDMCompactFEJSON(jsonNode));
				})
				.ignoreElements().cast(Void.class);

		jsonResult.connect();

		return voidObservable;
	}

	private ConnectableObservable<JsonNode> generateGDMCompactJSONResult(final Observable<GDMModel> model) {

		final AtomicInteger resultCounter = new AtomicInteger(0);

		// transform to FE friendly JSON => or use Model#toJSON() ;)

		return model.onBackpressureBuffer(10000)
				.doOnSubscribe(() -> TasksResource.LOG.debug("subscribed to results observable in task resource"))
				.doOnNext(resultObj -> {

					resultCounter.incrementAndGet();

					if (resultCounter.get() == 1) {

						TasksResource.LOG.debug("received first result in task resource");
					}
				})
				.doOnCompleted(() -> TasksResource.LOG.debug("received '{}' results in task resource overall", resultCounter.get()))
				.map(org.dswarm.persistence.model.internal.Model::toGDMCompactJSON)
				.flatMapIterable(nodes -> {

					final ArrayList<JsonNode> nodeList = new ArrayList<>();

					Iterators.addAll(nodeList, nodes.elements());

					return nodeList;
				})
				.onBackpressureBuffer(10000)
				.publish();
	}

	private JsonNode transformGDMCompactJSONtoGDMCompactFEJSON(final JsonNode resultJSON) {

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

		return recordNode;
	}

	private Observable<Void> doGDMJSONExport(final Observable<GDMModel> gdmModelObservable,
	                                         final MediaType responseMediaType,
	                                         final BufferedOutputStream bos,
	                                         final Func1<GDMModel, JsonNode> transformationFunction) throws DMPControllerException {

		final AtomicInteger resultCounter = new AtomicInteger(0);
		try {
			final JsonGenerator jg = objectMapper.getFactory().createGenerator(bos, JsonEncoding.UTF8);

			return gdmModelObservable.onBackpressureBuffer(10000)
					.doOnSubscribe(() -> TasksResource.LOG.debug("subscribed to {} export", responseMediaType))
					.doOnNext(resultObj -> {

						resultCounter.incrementAndGet();

						if (resultCounter.get() == 1) {

							try {

								jg.writeStartArray();
							} catch (final IOException e) {

								throw DMPPersistenceError.wrap(new DMPPersistenceException(String.format("something went wrong while serialising the %s", responseMediaType), e));
							}

							TasksResource.LOG.debug("received first result for {} export in task resource", responseMediaType);
						}
					})
					.map(transformationFunction)
					.map(model -> {

						try {

							jg.writeTree(model.get(0));
							bos.flush();
						} catch (final IOException e) {

							throw DMPPersistenceError.wrap(new DMPPersistenceException(String.format("something went wrong while serialising the %s", responseMediaType), e));
						}

						return model;
					})
					.onBackpressureBuffer(10000)
					.doOnCompleted(() -> {

						if (resultCounter.get() > 0) {

							try {

								jg.writeEndArray();
							} catch (final IOException e) {

								throw DMPPersistenceError.wrap(new DMPPersistenceException(String.format("something went wrong while serialising the %s", responseMediaType), e));
							}
						}

						try {

							jg.close();
						} catch (final IOException e) {

							throw DMPPersistenceError.wrap(new DMPPersistenceException(String.format("something went wrong while serialising the %s", responseMediaType), e));
						}

						TasksResource.LOG.debug("received '{}' results for {} export in task resource overall", resultCounter.get(), responseMediaType);
					})
					.ignoreElements().cast(Void.class);
		} catch (final IOException e) {

			throw new DMPControllerException(String.format("something went wrong while serialising the %s", responseMediaType), e);
		}
	}

	private Observable<Void> doGDMLDJExport(final Observable<GDMModel> gdmModelObservable,
	                                        final MediaType responseMediaType,
	                                        final BufferedOutputStream bos,
	                                        final Func1<GDMModel, JsonNode> transformationFunction) throws DMPControllerException {

		final AtomicInteger resultCounter = new AtomicInteger(0);
		final AtomicLong statementCounter = new AtomicLong(0);
		final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(bos));

		return gdmModelObservable.onBackpressureBuffer(10000)
				.doOnSubscribe(() -> TasksResource.LOG.debug("subscribed to {} export", responseMediaType))
				.doOnNext(resultObj -> {

					resultCounter.incrementAndGet();

					if (resultCounter.get() == 1) {

						TasksResource.LOG.debug("received first result (with '{}' statements) for {} export in task resource", resultObj.getModel().size(), responseMediaType);

						try {

							TasksResource.LOG.trace("first result = '{}'", Util.getJSONObjectMapper().writeValueAsString(resultObj.getModel()));
						} catch (JsonProcessingException e) {

							TasksResource.LOG.trace("something went wrong with serializing first result");
						}
					}
				})
				.map(gdmModel -> {

					statementCounter.addAndGet(gdmModel.getModel().size());

					return gdmModel;
				})
				.map(transformationFunction)
				.map(model -> {

					try {

						bw.write(objectMapper.writeValueAsString(model.get(0)));
						bw.newLine();
						bw.flush();
						bos.flush();
					} catch (final IOException e) {

						throw DMPPersistenceError.wrap(new DMPPersistenceException(String.format("something went wrong while serialising the %s", responseMediaType), e));
					}

					return model;
				})
				.onBackpressureBuffer(10000)
				.doOnCompleted(() -> {

					try {

						bw.close();
					} catch (final IOException e) {

						throw DMPPersistenceError.wrap(new DMPPersistenceException(String.format("something went wrong while serialising the %s", responseMediaType), e));
					}

					TasksResource.LOG.debug("received '{}' results (with '{}' statements) for {} export in task resource overall", resultCounter.get(), statementCounter.get(), responseMediaType);
				})
				.ignoreElements().cast(Void.class);
	}

	private java.util.Optional<String> guavaOptionalToJava8Optional(final Optional<String> optionalString) {

		if (optionalString.isPresent()) {

			return java.util.Optional.of(optionalString.get());
		}

		return java.util.Optional.empty();
	}

	private static Func1<GDMModel, JsonNode> toJSCJSONRecord(final Task task) {

		return gdmModel -> {

			final Optional<JsonNode> optionalResult = GDMModelUtil.toJSCJSON(gdmModel.getModel(), gdmModel.getRecordURIs(), task.getOutputDataModel().getSchema());

			if (!optionalResult.isPresent()) {

				return null;
			}

			return optionalResult.get();
		};
	}

	private static String getInputDataResourceFileName(final Task task) {

		if(task == null) {

			return null;
		}

		final DataModel inputDataModel = task.getInputDataModel();

		if(inputDataModel == null) {

			return null;
		}

		final Resource inputDataResource = inputDataModel.getDataResource();

		if(inputDataResource == null) {

			return null;
		}

		final JsonNode inputDataResourcePath = inputDataResource.getAttribute(ResourceStatics.PATH);

		if(inputDataResourcePath == null) {

			return inputDataResource.getName();
		}

		return inputDataResourcePath.asText();
	}

}
