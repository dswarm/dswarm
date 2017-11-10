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
package org.dswarm.controller.eventbus;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Provider;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import org.dswarm.common.types.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;
import rx.observables.BlockingObservable;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.graph.json.Model;
import org.dswarm.graph.json.Resource;
import org.dswarm.persistence.DMPPersistenceError;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.UpdateFormat;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.resource.utils.ResourceStatics;
import org.dswarm.persistence.monitoring.MonitoringHelper;
import org.dswarm.persistence.monitoring.MonitoringLogger;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.service.internal.graph.util.SchemaDeterminator;

/**
 * An event recorder for converting XML or JSON documents.
 *
 * @author phorn
 * @author tgaengler
 */
public abstract class ConverterEventRecorder<CONVERTER_EVENT_IMPL extends ConverterEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(ConverterEventRecorder.class);

	/**
	 * The internal model service factory
	 */
	private final InternalModelServiceFactory internalServiceFactory;
	protected final Provider<MonitoringLogger> loggerProvider;
	private final Provider<SchemaDeterminator> schemaDeterminatorProvider;
	private final String type;

	private static final String DSWARM_GDM_THREAD_NAMING_PATTERN = "dswarm-gdm-%d";

	private static final ExecutorService GDM_EXECUTOR_SERVICE = Executors
			.newCachedThreadPool(
					new BasicThreadFactory.Builder().daemon(false).namingPattern(DSWARM_GDM_THREAD_NAMING_PATTERN).build());
	private static final Scheduler GDM_SCHEDULER = Schedulers.from(GDM_EXECUTOR_SERVICE);

	/**
	 * Creates a new event recorder for converting XML or JSON documents with the given internal model service factory and event bus.
	 *
	 * @param internalModelServiceFactory an internal model service factory
	 */
	public ConverterEventRecorder(
			final InternalModelServiceFactory internalModelServiceFactory,
			final Provider<MonitoringLogger> loggerProvider,
			final Provider<SchemaDeterminator> schemaDeterminatorProvider,
			final String typeArg) {

		internalServiceFactory = internalModelServiceFactory;
		this.loggerProvider = loggerProvider;
		this.schemaDeterminatorProvider = schemaDeterminatorProvider;
		type = typeArg;
	}

	/**
	 * Processes the XML or JSON document of the data model of the given event and persists the converted data.
	 *
	 * @param event an converter event that provides a data model
	 */
	// @Subscribe
	public void processDataModel(final CONVERTER_EVENT_IMPL event) throws DMPControllerException {

		final DataModel dataModel = event.getDataModel();
		final UpdateFormat updateFormat = event.getUpdateFormat();
		final boolean enableVersioning = event.isEnableVersioning();

		try (final MonitoringHelper ignore = loggerProvider.get().startIngest(dataModel)) {

			processDataModel(dataModel, updateFormat, enableVersioning);
		}
	}

	public void processDataModel(final DataModel dataModel, final UpdateFormat updateFormat, final boolean enableVersioning)
			throws DMPControllerException {

		final Tuple<ConnectableObservable<GDMModel>, ConnectableObservable<org.dswarm.persistence.model.internal.Model>> connectableObservableTuple = doIngestInternal(dataModel, false, Schedulers.newThread());
		final ConnectableObservable<GDMModel> connectableSource = connectableObservableTuple.v1();
		final ConnectableObservable<org.dswarm.persistence.model.internal.Model> connectableResult = connectableObservableTuple.v2();

		final ConnectableObservable<org.dswarm.persistence.model.internal.Model> connectableResult2 = connectableResult
				.onBackpressureBuffer()
				.publish();

		connectableResult.connect();

		try {

			final ConnectableObservable<Response> writeResponse = internalServiceFactory.getInternalGDMGraphService()
					.updateObject(dataModel.getUuid(), connectableResult2.observeOn(GDM_SCHEDULER).onBackpressureBuffer(10000), updateFormat, enableVersioning)
					.onBackpressureBuffer()
					.doOnSubscribe(() -> LOG.debug("subscribed to write response observable"))
					.publish();

			connectableResult2.connect();

			writeResponse.ignoreElements()
					.cast(Void.class)
					.doOnError(e -> {

						LOG.error("error in here", e);
					});

			// TODO: delegate observable
			final BlockingObservable<Response> blockingObservable = writeResponse.toBlocking();

			writeResponse.connect();
			connectableSource.connect();

			blockingObservable.firstOrDefault(null);

			LOG.debug("processed {} data resource into data model '{}'", type, dataModel.getUuid());
		} catch (final DMPPersistenceException e) {

			final String message = String.format("couldn't persist the converted data of data model '%s'", dataModel.getUuid());

			ConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(String.format("%s %s", message, e.getMessage()), e);
		}
	}

	public Observable<org.dswarm.persistence.model.internal.Model> doIngest(final DataModel dataModel,
	                                                                        final boolean utiliseExistingSchema,
	                                                                        final Scheduler scheduler) throws DMPControllerException {

		final Tuple<ConnectableObservable<GDMModel>, ConnectableObservable<org.dswarm.persistence.model.internal.Model>> connectableObservableTuple = doIngestInternal(dataModel, utiliseExistingSchema, scheduler);
		final ConnectableObservable<GDMModel> connectableSource = connectableObservableTuple.v1();
		final ConnectableObservable<org.dswarm.persistence.model.internal.Model> connectableResult = connectableObservableTuple.v2();

		final Observable<org.dswarm.persistence.model.internal.Model> result = connectableResult.onBackpressureBuffer()
				.doOnSubscribe(() -> connectableSource.connect());

		connectableResult.connect();

		return result;


	}

	private Tuple<ConnectableObservable<GDMModel>, ConnectableObservable<org.dswarm.persistence.model.internal.Model>> doIngestInternal(final DataModel dataModel,
	                                                                                                                                    final boolean utiliseExistingSchema,
	                                                                                                                                    final Scheduler scheduler) throws DMPControllerException {
		// TODO: enable monitoring here

		LOG.debug("try to process {} data resource into data model '{}' (utilise existing schema = '{}')", type, dataModel.getUuid(),
				utiliseExistingSchema);

		try {

			final SchemaDeterminator schemaDeterminator = schemaDeterminatorProvider.get();
			final DataModel freshDataModel = schemaDeterminator.determineSchema(dataModel.getUuid());
			final boolean isSchemaAnInbuiltSchema = schemaDeterminator.isSchemaAnInbuiltSchema(freshDataModel);
			final boolean hasSchema = isSchemaAnInbuiltSchema || utiliseExistingSchema;

			final AtomicInteger counter = new AtomicInteger(0);
			final AtomicLong statementCounter = new AtomicLong(0);

			//LOG.debug("XML records = '{}'", gdmModels.size());

			final String path = dataModel.getDataResource().getAttribute(ResourceStatics.PATH).asText();

			final Observable<GDMModel> convertedData = convertData(dataModel, utiliseExistingSchema, scheduler, path, hasSchema);
			final ConnectableObservable<GDMModel> connectableConvertedData = convertedData
					.onBackpressureBuffer()
					.publish();

			final ConnectableObservable<org.dswarm.persistence.model.internal.Model> postProcessedConvertedData = connectableConvertedData.filter(gdmModel -> {

				try {

					final Model model = gdmModel.getModel();

					if (model == null) {

						LOG.debug("model is not available");

						return false;
					}

					final Collection<Resource> resources = model.getResources();

					if (resources == null || resources.isEmpty()) {

						LOG.debug("resources from model are not available");

						return false;
					}

					statementCounter.addAndGet(model.size());

					if (counter.incrementAndGet() == 1) {

						LOG.debug(
								"transformed first record of {} data resource to GDM for data model '{}' with '{}' statements (data resource at '{}')",
								type, dataModel.getUuid(), statementCounter.get(), path);
					}

					final boolean updateDataModelDirectly = false;

					schemaDeterminator.optionallyEnhancedDataModel(freshDataModel, gdmModel, model, hasSchema, updateDataModelDirectly);

					//final int current = counter.incrementAndGet();

					//LOG.debug("XML resource number '{}' with '{}' and resources size = '{}'", current, resources.iterator().next().getUri(), resources.size());

					return true;
				} catch (DMPPersistenceException e) {

					final String message = String
							.format("something went wrong, while data model enhancement for data model '%s'", freshDataModel.getUuid());

					LOG.error(message, e);

					throw DMPPersistenceError.wrap(e);
				}
			})
					.cast(org.dswarm.persistence.model.internal.Model.class)
					.doOnCompleted(
							() -> {

								final int recordCount = counter.get();

								if (recordCount == 0) {

									final Configuration configuration = dataModel.getConfiguration();
									final Optional<JsonNode> optionalRecordTagNode = Optional.ofNullable(
											configuration.getParameter(ConfigurationStatics.RECORD_TAG));

									final Optional<String> optionalRecordTag;

									if (optionalRecordTagNode.isPresent()) {

										optionalRecordTag = Optional.ofNullable(optionalRecordTagNode.get().asText(null));
									} else {

										optionalRecordTag = Optional.empty();
									}

									final String messageStart = String.format(
											"couldn't transform any record from %s data resource at '%s' to GDM for data model '%s'; ", type, path,
											dataModel.getUuid());

									final StringBuilder messageSB = new StringBuilder();
									messageSB.append(messageStart);

									if (optionalRecordTag.isPresent()) {

										messageSB.append("maybe you set a wrong record tag (current one = '").append(optionalRecordTag.get()).append("')");
									} else {

										messageSB.append("maybe because you set no record tag");
									}

									throw new RuntimeException(messageSB.toString());
								}

								try {

									// update data model only once (per processing)
									schemaDeterminator.updateDataModel(freshDataModel);
								} catch (final DMPPersistenceException e) {

									throw DMPPersistenceError.wrap(e);
								}

								LOG.info(
										"transformed {} data resource to GDM for data model '{}' - transformed '{}' records with '{}' statements (data resource at '{}')",
										type, dataModel.getUuid(), recordCount, statementCounter.get(), path);
							})
					.doOnSubscribe(() -> LOG.debug("subscribed to {} ingest", type))
					.onBackpressureBuffer()
					.publish();

			return Tuple.tuple(connectableConvertedData, postProcessedConvertedData);
		} catch (final NullPointerException e) {

			final String message = String.format("couldn't convert the %s data of data model '%s'", type, dataModel.getUuid());

			ConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(String.format("%s %s", message, e.getMessage()), e);
		} catch (final Exception e) {

			final String message = String.format("really couldn't convert the %s data of data model '%s'", type, dataModel.getUuid());

			ConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(String.format("%s %s", message, e.getMessage()), e);
		}
	}

	protected abstract Observable<GDMModel> convertData(final DataModel dataModel, final boolean utiliseExistingSchema, final Scheduler scheduler,
	                                                    final String path, final boolean hasSchema);
}
