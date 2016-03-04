/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.*;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.dswarm.common.MediaTypeUtil;
import org.dswarm.common.types.Tuple;
import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.utils.DataModelUtil;
import org.dswarm.controller.utils.JsonUtils;
import org.dswarm.controller.utils.ResourceUtils;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.export.Exporter;
import org.dswarm.converter.export.RDFExporter;
import org.dswarm.converter.export.XMLExporter;
import org.dswarm.converter.flow.GDMModelTransformationFlow;
import org.dswarm.converter.flow.GDMModelTransformationFlowFactory;
import org.dswarm.converter.morph.MorphScriptBuilder;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.job.Job;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.monitoring.MonitoringHelper;
import org.dswarm.persistence.monitoring.MonitoringLogger;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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

	private static final MediaType N_TRIPLES_MEDIA_TYPE = new MediaType("application", "n-triples");

	public static final String TASK_IDENTIFIER = "task";
	public static final String AT_MOST_IDENTIFIER = "at_most";
	public static final String PERSIST_IDENTIFIER = "persist";
	public static final String RETURN_IDENTIFIER = "do_not_return_data";
	public static final String SELECTED_RECORDS_IDENTIFIER = "selected_records";
	public static final String DO_INGEST_ON_THE_FLY_IDENTIFIER = "do_ingest_on_the_fly";
	public static final String UTILISE_EXISTING_INPUT_SCHEMA_IDENTIFIER = "utilise_existing_input_schema";
	public static final String DO_EXPORT_ON_THE_FLY_IDENTIFIER = "do_export_on_the_fly";
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
	public static final String ERROR_IDENTIFIER = "error";
	public static final String MESSAGE_IDENTIFIER = "message";
	public static final String STACKTRACE_IDENTIFIER = "stacktrace";

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
	 * @param dataModelUtilArg the data model util
	 * @param objectMapperArg  an object mapper
	 * @param transformationFlowFactoryArg the factory for creating transformation flows
	 * @param monitoringLogger A logger that produces the logfiles for the monitoring
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
	 * Builds a positive response with the given content.
	 *
	 * @param responseContent a response message
	 * @return the response
	 */
	private static Response buildResponse(final String responseContent, final MediaType mediaType) {

		final Response.ResponseBuilder responseBuilder = Response.ok(responseContent);

		if (mediaType != null) {

			responseBuilder.type(mediaType);
		}

		return responseBuilder.build();
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
	@ApiResponses(value = {@ApiResponse(code = 200, message = "task was successfully executed"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)")})
	@Timed
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaTypeUtil.N_TRIPLES})
	public void executeTask(@ApiParam(value = "task execution request (as JSON)", required = true) final String jsonObjectString,
	                        @Context final HttpHeaders requestHeaders, @Suspended final AsyncResponse asyncResponse)
			throws IOException,
			DMPConverterException, DMPControllerException {

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

		final boolean doIngestOnTheFly = JsonUtils.getBooleanValue(TasksResource.DO_INGEST_ON_THE_FLY_IDENTIFIER, requestJSON, false);

		if (doIngestOnTheFly) {

			LOG.debug("do ingest on-the-fly for task execution of task '{}'", task.getUuid());

			DataModelUtil.checkDataResource(inputDataModel);

			final boolean utiliseExistingInputSchema = JsonUtils.getBooleanValue(TasksResource.UTILISE_EXISTING_INPUT_SCHEMA_IDENTIFIER, requestJSON,
					false);

			inputData = dataModelUtil.doIngest(inputDataModel, utiliseExistingInputSchema, INGEST_SCHEDULER);
		} else {

			final Optional<Set<String>> optionalSelectedRecords = JsonUtils.getStringSetValue(TasksResource.SELECTED_RECORDS_IDENTIFIER, requestJSON);

			if (optionalSelectedRecords.isPresent()) {

				// retrieve data only for selected records

				inputData = dataModelUtil.getRecordsData(optionalSelectedRecords.get(), inputDataModel.getUuid());
			} else {

				final Optional<Integer> optionalAtMost = JsonUtils.getIntValue(TasksResource.AT_MOST_IDENTIFIER, requestJSON);

				inputData = dataModelUtil.getData(inputDataModel.getUuid(), optionalAtMost);
			}
		}

		final boolean writeResultToDatahub = JsonUtils.getBooleanValue(TasksResource.PERSIST_IDENTIFIER, requestJSON, false);

		final boolean doNotReturnJsonToCaller = JsonUtils.getBooleanValue(TasksResource.RETURN_IDENTIFIER, requestJSON, false);

		final boolean doVersioningOnResult = JsonUtils.getBooleanValue(TasksResource.DO_VERSIONING_ON_RESULT_IDENTIFIER, requestJSON, true);

		final boolean doExportOnTheFly = JsonUtils.getBooleanValue(TasksResource.DO_EXPORT_ON_THE_FLY_IDENTIFIER, requestJSON, false);

		final boolean doNotReturnJsonToCaller2 = !doExportOnTheFly && doNotReturnJsonToCaller;

		if (!doVersioningOnResult) {

			TasksResource.LOG.debug("skip result versioning");
		}

		final Observable<GDMModel> result;

		try (final MonitoringHelper ignore = monitoringLogger.get().startExecution(task)) {

			final GDMModelTransformationFlow flow = transformationFlowFactory.fromTask(task);
			result = flow.apply(inputData, writeResultToDatahub, doNotReturnJsonToCaller2, doVersioningOnResult, TRANSFORMATION_ENGINE_SCHEDULER)
					.subscribeOn(TRANSFORMATION_ENGINE_SCHEDULER);
		}

		if (result == null) {

			TasksResource.LOG.debug("result of task execution is null");

			asyncResponse.resume(Response.noContent().build());

			return;
		}

		final ConnectableObservable<JsonNode> jsonResult = generateJSONResult(result);

		// note: you can only do one of this, i.e., export result as xml or as json
		if (doExportOnTheFly) {

			LOG.debug("do export on-the-fly for task execution of task '{}'", task.getUuid());

			final List<MediaType> acceptableMediaTypes = requestHeaders.getAcceptableMediaTypes();

			final MediaType responseMediaType;

			if (acceptableMediaTypes == null || acceptableMediaTypes.isEmpty()) {

				// default media type is application/xml
				responseMediaType = MediaType.APPLICATION_XML_TYPE;
			} else {

				final Optional<MediaType> mediaTypeOptional = acceptableMediaTypes.stream()
						.filter(mediaType -> {

							if (MediaType.APPLICATION_XML_TYPE.equals(mediaType) || MediaTypeUtil.N_TRIPLES_TYPE.equals(mediaType)) {

								return true;
							}

							return false;
						}).findFirst();

				if (mediaTypeOptional.isPresent()) {

					responseMediaType = mediaTypeOptional.get();
				} else {

					// media type is not supported
					asyncResponse.resume(Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build());

					return;
				}
			}

			final Future<StreamingOutput> futureStream = CompletableFuture.supplyAsync(() -> {

				final CountDownLatch countDownLatch = new CountDownLatch(1);

				return os -> {

					try {

						LOG.debug("start preparing {} export", responseMediaType.toString());

						final BufferedOutputStream bos = new BufferedOutputStream(os, 1024);

						// collect input parameter for exporter

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

						// record tag
						final Optional<Configuration> optionalConfiguration = Optional.ofNullable(finalOutputDataModel.getConfiguration());
						final Optional<String> optionalRecordTag = DataModelUtil.determineRecordTag(optionalConfiguration);
						final java.util.Optional<String> java8OptionalRecordTag = guavaOptionalToJava8Optional(optionalRecordTag);

						// record class uri
						final Optional<String> optionalRecordClassURI = DataModelUtil.determineRecordClassURI(finalOutputDataModel);

						// original data model type
						final Optional<String> optionalOriginalDataModelType = DataModelUtil.determineOriginalDataModelType(finalOutputDataModel,
								optionalConfiguration);
						final java.util.Optional<String> java8OptionalOriginalDataModelType = guavaOptionalToJava8Optional(
								optionalOriginalDataModelType);

						final String recordClassUri = optionalRecordClassURI.get();

						LOG.debug("create {} exporter with: record tag = '{}' :: record class URI = '{}' :: original data model type = '{}'",
								responseMediaType.toString(), optionalRecordTag, recordClassUri, java8OptionalOriginalDataModelType);

						final Exporter exporter;

						switch (responseMediaType.toString()) {

							case MediaType.APPLICATION_XML:

								exporter = new XMLExporter(java8OptionalRecordTag, recordClassUri, java.util.Optional.<String>empty(), java8OptionalOriginalDataModelType);

								break;
							case MediaTypeUtil.N_TRIPLES:

								exporter = new RDFExporter(responseMediaType);

								break;
							default:

								// media type is not supported
								asyncResponse.resume(Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build());

								countDownLatch.countDown();

								return;
						}

						LOG.debug("trigger {} export", responseMediaType.toString());

						final Observable<JsonNode> resultObservable = exporter.generate(jsonResult.subscribeOn(EXPORT_SCHEDULER), bos);

						resultObservable.subscribeOn(EXPORT_SCHEDULER)
								.doOnSubscribe(() -> LOG.debug("subscribed to {} export in task resource", responseMediaType.toString()))
								.subscribe(new Observer<JsonNode>() {

									@Override
									public void onCompleted() {

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
									}

									@Override
									public void onError(final Throwable throwable) {

										final String message = String.format("couldn't process task (maybe %s export) successfully", responseMediaType.toString());

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

											final String message2 = String.format("couldn't process task (maybe %s export) successfully", responseMediaType.toString());

											TasksResource.LOG.error(message2, e);

											asyncResponse.resume(new DMPControllerException(message, throwable));
										} finally {

											countDownLatch.countDown();
										}
									}

									@Override
									public void onNext(final JsonNode result) {

										// nothing to do
									}
								});

						// just for count down latch, taken from http://christopher-batey.blogspot.de/2014/12/streaming-large-payloads-over-http-from.html
						try {

							countDownLatch.await();
						} catch (InterruptedException e) {

							LOG.warn("Current thread interrupted, resetting flag");

							Thread.currentThread().interrupt();
						}
					} catch (final XMLStreamException | DMPConverterException e) {

						final String message = String.format("couldn't process task (maybe %s export) successfully", responseMediaType.toString());

						TasksResource.LOG.error(message, e);

						asyncResponse.resume(new DMPControllerException(message, e));
					}
				};
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
		} else {

			if (doNotReturnJsonToCaller) {

				jsonResult.subscribe(new Observer<JsonNode>() {

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
					public void onNext(final JsonNode jsonNode) {

						// nothing to do here
					}
				});

				jsonResult.connect();

				return;
			}

			// transform model json to fe friendly json
			final ArrayNode feFriendlyJSON = objectMapper.createArrayNode();
			final AtomicInteger counter = new AtomicInteger(0);

			jsonResult.doOnSubscribe(() -> TasksResource.LOG.debug("subscribed to JSON export on task resource")).subscribe(new Observer<JsonNode>() {

				@Override
				public void onCompleted() {

					final String resultString;
					try {
						resultString = objectMapper.writeValueAsString(feFriendlyJSON);

						TasksResource.LOG.debug("processed task successfully, return data to caller; received '{}' records overall", counter.get());

						asyncResponse.resume(buildResponse(resultString, MediaType.APPLICATION_JSON_TYPE));
					} catch (JsonProcessingException e) {

						asyncResponse.resume(e);
					}
				}

				@Override
				public void onError(final Throwable e) {

					final String message = "couldn't deserialize result JSON from string";

					TasksResource.LOG.error(message, e);

					asyncResponse.resume(new DMPControllerException(message, e));
				}

				@Override
				public void onNext(final JsonNode jsonNode) {

					counter.incrementAndGet();

					if (counter.get() == 1) {

						TasksResource.LOG.debug("recieved first record for JSON export in task resource");
					}

					feFriendlyJSON.add(transformModelJSONtoFEFriendlyJSON(jsonNode));
				}
			});
		}

		jsonResult.connect();
	}

	private ConnectableObservable<JsonNode> generateJSONResult(final Observable<GDMModel> model) {

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
				.map(org.dswarm.persistence.model.internal.Model::toJSON)
				.flatMapIterable(nodes -> {

					final ArrayList<JsonNode> nodeList = new ArrayList<>();

					Iterators.addAll(nodeList, nodes.elements());

					return nodeList;
				})
				.onBackpressureBuffer(10000)
				.publish();
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

	private JsonNode transformModelJSONtoFEFriendlyJSON(final JsonNode resultJSON) {

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

	private java.util.Optional<String> guavaOptionalToJava8Optional(final Optional<String> optionalString) {

		if (optionalString.isPresent()) {

			return java.util.Optional.of(optionalString.get());
		}

		return java.util.Optional.empty();
	}

}
