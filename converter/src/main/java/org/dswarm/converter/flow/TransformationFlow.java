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
package org.dswarm.converter.flow;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.inject.Provider;
import javaslang.Tuple2;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.culturegraph.mf.exceptions.MorphDefException;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.stream.pipe.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import org.dswarm.converter.DMPConverterError;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.DMPMorphDefException;
import org.dswarm.converter.flow.model.MorphTask;
import org.dswarm.converter.mf.stream.GDMEncoder;
import org.dswarm.converter.mf.stream.GDMModelReceiver;
import org.dswarm.converter.mf.stream.reader.JsonNodeReader;
import org.dswarm.converter.pipe.timing.TimerBasedFactory;
import org.dswarm.graph.json.*;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.UpdateFormat;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.utils.ClaszUtils;
import org.dswarm.persistence.service.InternalModelService;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.dswarm.persistence.util.GDMUtil;

/**
 * Flow that executes a given set of transformations on data of a given data model.
 *
 * @author phorn
 * @author tgaengler
 * @author sreichert
 * @author polowins
 */
public abstract class TransformationFlow<RESULTFORMAT> {

	private static final Logger LOG = LoggerFactory.getLogger(TransformationFlow.class);

	protected static final String TRANSFORMATION_ENGINE_IDENTIFIER = "transformation engine";
	private static final Predicate RDF_TYPE_PREDICATE = new Predicate(GDMUtil.RDF_type);

	private final Map<String, ResourceNode> resourceNodeCache = new ConcurrentHashMap<>();

	protected final Metamorph transformer;

	private final String script;

	protected final Optional<Filter> optionalSkipFilter;

	protected final Optional<DataModel> outputDataModel;

	private final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider;

	protected final TimerBasedFactory timerBasedFactory;

	protected final Timer morphTimer;

	private static final String DSWARM_GDM_THREAD_NAMING_PATTERN = "dswarm-gdm-%d";

	private static final ExecutorService GDM_EXECUTOR_SERVICE = Executors
			.newCachedThreadPool(
					new BasicThreadFactory.Builder().daemon(false).namingPattern(DSWARM_GDM_THREAD_NAMING_PATTERN).build());
	private static final Scheduler GDM_SCHEDULER = Schedulers.from(GDM_EXECUTOR_SERVICE);

	protected TransformationFlow(final Provider<InternalModelServiceFactory> internalModelServiceFactoryProviderArg,
	                             final MetricRegistry registry,
	                             final TimerBasedFactory timerBasedFactory,
	                             final Metamorph transformer,
	                             final String scriptArg,
	                             final Optional<DataModel> outputDataModelArg,
	                             final Optional<Filter> optionalSkipFilterArg) {

		this.timerBasedFactory = timerBasedFactory;
		this.transformer = transformer;
		script = scriptArg == null ? "" : scriptArg;
		outputDataModel = outputDataModelArg;
		optionalSkipFilter = optionalSkipFilterArg;
		internalModelServiceFactoryProvider = internalModelServiceFactoryProviderArg;

		morphTimer = registry.timer("metamorph");
	}

	public String getScript() {

		return script;
	}

	public abstract Observable<String> applyRecord(final String record) throws DMPConverterException;

	public Observable<String> applyResource(final String resourcePath) throws DMPConverterException {

		try {
			return applyRecord(DMPPersistenceUtil.getResourceAsString(resourcePath));
		} catch (final IOException e) {

			return Observable.error(new DMPConverterException(e.getMessage(), e));
		}
	}

	public ConnectableObservable<RESULTFORMAT> apply(final Observable<Tuple2<String, JsonNode>> tuples,
	                                                 final ObjectPipe<Tuple2<String, JsonNode>, StreamReceiver> opener,
	                                                 final boolean writeResultToDatahub,
	                                                 final boolean doNotReturnJsonToCaller,
	                                                 final boolean enableVersioning,
	                                                 final Scheduler scheduler) throws DMPConverterException {

		final MorphTask morphTask = new MorphTask(morphTimer, timerBasedFactory, outputDataModel, TRANSFORMATION_ENGINE_IDENTIFIER, optionalSkipFilter, opener, transformer);

		final ConnectableObservable<org.dswarm.persistence.model.internal.Model> model = doPostProcessingOfResultModel(morphTask.getWriter(), scheduler);

		final Optional<Observable<RESULTFORMAT>> optionalResultObservable;
		final Optional<ConnectableObservable<RESULTFORMAT>> optionalConnectableResultObservable;

		if (doNotReturnJsonToCaller) {

			optionalResultObservable = Optional.of(Observable.empty());
			optionalConnectableResultObservable = Optional.empty();
		} else {

			optionalResultObservable = Optional.empty();

			optionalConnectableResultObservable = Optional.ofNullable(transformResultModel(model));
		}

		final Observable<Response> writeResponse = writeResultToDatahub(writeResultToDatahub, enableVersioning, model);

		final ConnectableObservable<RESULTFORMAT> resultformatObservable = Observable.create(wireTransformationFlowMorphConnector(doNotReturnJsonToCaller, optionalResultObservable, optionalConnectableResultObservable, scheduler, writeResponse, morphTask.getMorphContext(), tuples, opener, morphTask.getWriter()), Emitter.BackpressureMode.BUFFER)
				.doOnCompleted(() -> logTransformationFlowEnd(opener, morphTask.getConverter(), morphTask.getWriter(), writeResultToDatahub))
				.observeOn(scheduler)
				.publish();

		model.connect();

		return resultformatObservable;
	}

	public ConnectableObservable<RESULTFORMAT> apply(final Observable<Tuple2<String, JsonNode>> tuples,
	                                                 final boolean writeResultToDatahub,
	                                                 final boolean doNotReturnJsonToCaller,
	                                                 final boolean enableVersioning,
	                                                 final Scheduler scheduler) throws DMPConverterException {

		final JsonNodeReader opener = new JsonNodeReader();

		return apply(tuples, opener, writeResultToDatahub, doNotReturnJsonToCaller, enableVersioning, scheduler);
	}

	static Metamorph createMorph(final Reader morphString) throws DMPMorphDefException {
		try {
			return new Metamorph(morphString);
		} catch (final MorphDefException e) {
			throw new DMPMorphDefException(e.getMessage(), e);
		}
	}

	static Filter createFilter(final Reader filterString) throws DMPMorphDefException {
		return new Filter(createMorph(filterString));
	}

	static Reader readString(final String string) throws DMPConverterError {
		if (string == null) {
			throw new DMPConverterError("The script string must not be null");
		}
		return new StringReader(string);
	}

	static Optional<Reader> readString(final Optional<String> string) throws DMPConverterException {
		return string.map(TransformationFlow::readString);
	}

	static Reader readFile(final File file) throws DMPConverterException {
		try {
			return Files
					.asCharSource(file, StandardCharsets.UTF_8)
					.openBufferedStream();
		} catch (final IOException e) {
			throw new DMPConverterException("Could not read script file", e);
		}
	}

	static Reader readResource(final String path) throws DMPConverterException {
		try {
			return Resources
					.asCharSource(Resources.getResource(path), StandardCharsets.UTF_8)
					.openBufferedStream();
		} catch (final IOException e) {
			throw new DMPConverterException("Could not read script resource", e);
		}
	}

	private Optional<String> getDataModelSchemaRecordClassURI() {
		return outputDataModel.flatMap(dataModel ->
				Optional.ofNullable(dataModel.getSchema()).flatMap(schema ->
						Optional.ofNullable(schema.getRecordClass())
								.map(Clasz::getUri)));
	}

	private ResourceNode getOrCreateResourceNode(final String resourceURI) {

		return resourceNodeCache.computeIfAbsent(resourceURI, resourceURI1 -> new ResourceNode(resourceURI));
	}

	protected ConnectableObservable<org.dswarm.persistence.model.internal.Model> doPostProcessingOfResultModel(final GDMModelReceiver writer, final Scheduler scheduler) {

		final ConnectableObservable<GDMModel> modelConnectableObservable = writer.getObservable()
				.observeOn(scheduler)
				.onBackpressureBuffer(10000)
				.publish();
		final ConnectableObservable<org.dswarm.persistence.model.internal.Model> model = doPostProcessingOfResultModel(modelConnectableObservable);
		modelConnectableObservable.connect();

		return model;
	}

	private ConnectableObservable<org.dswarm.persistence.model.internal.Model> doPostProcessingOfResultModel(final Observable<GDMModel> modelObservable) {

		final Optional<String> optionalDataModelSchemaRecordClassURI = getDataModelSchemaRecordClassURI();

		final String defaultRecordClassURI = optionalDataModelSchemaRecordClassURI.orElse(ClaszUtils.BIBO_DOCUMENT_URI);

		final AtomicInteger counter = new AtomicInteger(0);
		final AtomicInteger counter2 = new AtomicInteger(0);
		final AtomicLong statementCounter = new AtomicLong(0);

		return modelObservable
				.doOnSubscribe(() -> TransformationFlow.LOG.debug("subscribed on transformation result observable"))
				.doOnNext(gdmModel1 -> {

					if (counter.incrementAndGet() == 1) {

						LOG.debug("start processing first record in transformation engine");
					}
				})
				.filter(TransformationFlow::validateGDMModel)
				.map(this::optionallySetRecordURI)
				.map(gdmModel3 -> optionallyEnhanceType(defaultRecordClassURI, gdmModel3))
				.doOnNext(gdmModel4 -> {

					statementCounter.addAndGet(gdmModel4.getModel().size());

					if (counter2.incrementAndGet() == 1) {

						LOG.info("processed first record with '{}' statements in transformation engine", statementCounter.get());
					}
				})
				.cast(org.dswarm.persistence.model.internal.Model.class)
				.doOnCompleted(() -> LOG.info("processed '{}' records (from '{}') with '{}' statements in transformation engine", counter2.get(), counter.get(), statementCounter.get())).publish();
	}

	protected Observable<Response> writeResultToDatahub(final boolean writeResultToDatahub,
	                                                    final boolean enableVersioning,
	                                                    final Observable<org.dswarm.persistence.model.internal.Model> model) {

		return writeResultToDatahubInternal(writeResultToDatahub, enableVersioning, model);
	}

	protected abstract ConnectableObservable<RESULTFORMAT> transformResultModel(final Observable<org.dswarm.persistence.model.internal.Model> model);

	protected Action1<Emitter<RESULTFORMAT>> wireTransformationFlowMorphConnector(final boolean doNotReturnJsonToCaller,
	                                                                              final Optional<Observable<RESULTFORMAT>> optionalResultObservable,
	                                                                              final Optional<ConnectableObservable<RESULTFORMAT>> optionalConnectableResultObservable,
	                                                                              final Scheduler scheduler,
	                                                                              final Observable<Response> writeResponse,
	                                                                              final Context morphContext,
	                                                                              final Observable<Tuple2<String, JsonNode>> tuples,
	                                                                              final ObjectPipe<Tuple2<String, JsonNode>, StreamReceiver> opener,
	                                                                              final GDMModelReceiver writer) {

		return subscriber -> {

			final Observable<RESULTFORMAT> finalResultObservable;

			if (doNotReturnJsonToCaller) {

				finalResultObservable = optionalResultObservable.get();
			} else {

				finalResultObservable = optionalConnectableResultObservable.get();
			}

			finalResultObservable.observeOn(scheduler)
					.compose(concatStreams(writeResponse))
					.doOnCompleted(morphContext::stop)
					.subscribe(subscriber);

			if (!doNotReturnJsonToCaller) {

				optionalConnectableResultObservable.get().connect();
			}

			final AtomicInteger counter = new AtomicInteger(0);

			final Observable<Tuple2<String, JsonNode>> tupleObservable = tuples.observeOn(scheduler);

			tupleObservable.doOnNext(tuple -> {

				if (counter.incrementAndGet() == 1) {

					LOG.debug("received first record in transformation engine");
				}
			}).doOnCompleted(opener::closeStream)
					.doOnCompleted(() -> LOG.info("received '{}' records in transformation engine", counter.get()))
					.subscribe(opener::process, writer::propagateError, () -> LOG.debug("DONE"));
		};
	}

	protected abstract AndThenWaitFor<RESULTFORMAT, Response> concatStreams(final Observable<Response> writeResponse);

	protected static void logTransformationFlowEnd(final ObjectPipe<Tuple2<String, JsonNode>, StreamReceiver> opener,
	                                               final GDMEncoder converter,
	                                               final GDMModelReceiver writer,
	                                               final boolean writeResultToDatahub) {

		if (JsonNodeReader.class.isInstance(opener)) {

			JsonNodeReader jsonNodeReader = (JsonNodeReader) opener;

			LOG.debug("processed '{}' records with opener in transformation engine", jsonNodeReader.getCounter().get());
		}

		LOG.debug("received '{}' ('{}') records + emitted '{}' ('{}') records in converter in transformation engine",
				converter.getInComingCounter(), converter.getInComingCounter2(), converter.getOutGoingCounter(),
				converter.getOutGoingCounter2());

		final int outGoingCounter;

		if (writeResultToDatahub) {

			outGoingCounter = writer.getOutGoingCounter() / 2;
		} else {
			outGoingCounter = writer.getOutGoingCounter();
		}

		LOG.debug("received '{}' records + emitted '{}' (discarded '{}') records in writer in transformation engine",
				writer.getInComingCounter(), outGoingCounter, writer.getNonOutGoingCounter());
	}

	protected static <T> boolean exists(final Optional<T> optional, final java.util.function.Predicate<T> predicate) {
		return optional.isPresent() && predicate.test(optional.get());
	}

	protected static <T, U> boolean hasDefined(final Optional<T> optional, final Function<T, U> access) {
		return exists(optional, t -> access.apply(t) != null);
	}

	private GDMModel optionallySetRecordURI(final GDMModel gdmModel) {


		final Model model = gdmModel.getModel();

		final String recordURI = gdmModel.getRecordURIs().iterator().next();

		final Resource recordResource = model.getResource(recordURI);

		// search for rdf:about statement
		final Optional<Statement> optionalRdfAboutStatement = findRDFAboutStatement(recordResource);

		if (!optionalRdfAboutStatement.isPresent()) {

			// no rdf:about available -> keep record URI (and the whole model) as it is

			return gdmModel;
		}

		final Statement rdfAboutStatement = optionalRdfAboutStatement.get();

		final Node newRecordURINode = rdfAboutStatement.getObject();

		if (!LiteralNode.class.isInstance(newRecordURINode)) {

			// new record URI node should be a literal node

			return gdmModel;
		}

		final String newRecordURI = ((LiteralNode) newRecordURINode).getValue();

		final ResourceNode newRecordResourceNode = getOrCreateResourceNode(newRecordURI);

		final Resource newRecordResource = new Resource(newRecordURI);

		final Set<Statement> statements = recordResource.getStatements();

		statements.stream()
				.filter(statement -> !GDMUtil.RDF_about.equals(statement.getPredicate().getUri()))
				.forEach(statement1 -> {

					statement1.setSubject(newRecordResourceNode);
					newRecordResource.addStatement(statement1);
				});

		final Model newModel = new Model();
		newModel.addResource(newRecordResource);

		model.getResources().stream()
				.filter(resource -> !recordURI.equals(resource.getUri()))
				.forEach(newModel::addResource);

		return new GDMModel(newModel, newRecordURI, gdmModel.getRecordClassURI());
	}

	private GDMModel optionallyEnhanceType(final String defaultRecordClassURI,
	                                       final GDMModel gdmModel) {

		if (gdmModel.getRecordClassURI() != null) {

			return gdmModel;
		}

		// TODO: this a WORKAROUND to insert a default type (data model schema record class URI or bibo:Document) for records in the output data model

		final String recordURI = gdmModel.getRecordURIs().iterator().next();

		final Resource recordResource = gdmModel.getModel().getResource(recordURI);

		if (recordResource != null) {

			// TODO check this: subject OK?
			recordResource.addStatement(getOrCreateResourceNode(recordResource.getUri()), RDF_TYPE_PREDICATE,
					getOrCreateResourceNode(defaultRecordClassURI));
		}

		// re-write GDM model
		return new GDMModel(gdmModel.getModel(), recordURI, defaultRecordClassURI);
	}

	private Observable<Response> writeResultToDatahubInternal(final boolean writeResultToDatahub,
	                                                          final boolean enableVersioning,
	                                                          final Observable<org.dswarm.persistence.model.internal.Model> model) {

		if (!writeResultToDatahub) {

			return Observable.empty();
		}

		final Observable<Response> writeResponse;

		LOG.debug("write transformation result to datahub");

		if (hasDefined(outputDataModel, DMPObject::getUuid)) {

			// write result to graph db
			final InternalModelService internalModelService = internalModelServiceFactoryProvider.get().getInternalGDMGraphService();

			try {

				writeResponse = internalModelService.updateObject(outputDataModel.get().getUuid(), model.observeOn(GDM_SCHEDULER).onBackpressureBuffer(10000), UpdateFormat.DELTA, enableVersioning);
			} catch (final DMPPersistenceException e) {

				final String message = "couldn't persist the result of the transformation: " + e.getMessage();
				TransformationFlow.LOG.error(message);

				throw DMPConverterError.wrap(new DMPConverterException(message, e));
			}

		} else {

			final String message = "couldn't persist the result of the transformation, because there is no output data model assigned at this task";

			TransformationFlow.LOG.error(message);

			writeResponse = Observable.empty();
		}

		return writeResponse;
	}

	private static Boolean validateGDMModel(final GDMModel gdmModel) {

		//final int current = counter.incrementAndGet();

		//LOG.debug("processed resource model number '{}'", current);

		final Model model1 = gdmModel.getModel();

		if (model1 == null) {

			LOG.debug("no model available");

			return false;
		}

		final Collection<Resource> resources = model1.getResources();

		if (resources == null || resources.isEmpty()) {

			LOG.debug("no resources in model available");

			return false;
		}

		//LOG.debug("processed resource model number '{}' with '{}' and resource size '{}'", current, resources.iterator().next().getUri(), resources.size());

		final Set<String> recordURIsFromGDMModel = gdmModel.getRecordURIs();

		return !(recordURIsFromGDMModel == null || recordURIsFromGDMModel.isEmpty());
	}

	private static Optional<Statement> findRDFAboutStatement(final Resource recordResource) {

		final Set<Statement> statements = recordResource.getStatements();

		if (statements == null || statements.isEmpty()) {

			return Optional.empty();
		}

		return statements.stream()
				.filter(statement -> GDMUtil.RDF_about.equals(statement.getPredicate().getUri()))
				.findFirst();
	}

	protected static class AndThenWaitFor<T, U> implements Observable.Transformer<T, T> {

		private final Observable<U> other;
		private final Supplier<T> emptyResultValue;

		public AndThenWaitFor(final Observable<U> other, final Supplier<T> emptyResultValue) {
			this.other = other;
			this.emptyResultValue = emptyResultValue;
		}

		@Override
		public Observable<T> call(final Observable<T> thisOne) {
			return thisOne.concatWith(other.ignoreElements().map(ignored -> emptyResultValue.get()));
		}
	}
}
