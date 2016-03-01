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
package org.dswarm.converter.flow;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.ws.rs.core.Response;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Iterators;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import org.culturegraph.mf.exceptions.MorphDefException;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.StreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.stream.pipe.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import org.dswarm.common.types.Tuple;
import org.dswarm.converter.DMPConverterError;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.DMPMorphDefException;
import org.dswarm.converter.mf.stream.GDMEncoder;
import org.dswarm.converter.mf.stream.GDMModelReceiver;
import org.dswarm.converter.mf.stream.reader.JsonNodeReader;
import org.dswarm.converter.pipe.StreamUnflattener;
import org.dswarm.converter.pipe.timing.ObjectTimer;
import org.dswarm.converter.pipe.timing.StreamTimer;
import org.dswarm.converter.pipe.timing.TimerBasedFactory;
import org.dswarm.graph.json.Model;
import org.dswarm.graph.json.Predicate;
import org.dswarm.graph.json.Resource;
import org.dswarm.graph.json.ResourceNode;
import org.dswarm.init.util.DMPStatics;
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
public class TransformationFlow {

	private static final Logger LOG = LoggerFactory.getLogger(TransformationFlow.class);

	private static final String TRANSFORMATION_ENGINE_IDENTIFIER = "transformation engine";
	private static final Predicate RDF_TYPE_PREDICATE = new Predicate(GDMUtil.RDF_type);

	private final Map<String, ResourceNode> resourceNodeCache = new ConcurrentHashMap<>();

	private final Metamorph transformer;

	private final String script;

	private final Optional<Filter> optionalSkipFilter;

	private final Optional<DataModel> outputDataModel;

	private final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider;

	private final TimerBasedFactory timerBasedFactory;

	private final Timer morphTimer;

	@Inject
	private TransformationFlow(
			final Provider<InternalModelServiceFactory> internalModelServiceFactoryProviderArg,
			@Named("Monitoring") final MetricRegistry registry,
			final TimerBasedFactory timerBasedFactory,
			@Assisted final Metamorph transformer,
			@Assisted final String scriptArg,
			@Assisted final Optional<DataModel> outputDataModelArg,
			@Assisted final Optional<Filter> optionalSkipFilterArg) {

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

	public Observable<String> applyRecord(final String record) throws DMPConverterException {

		// TODO: convert JSON string to Iterator with tuples of string + JsonNode pairs
		List<Tuple<String, JsonNode>> tuplesList = null;

		try {

			tuplesList = DMPPersistenceUtil.getJSONObjectMapper().readValue(record, new TypeReference<List<Tuple<String, JsonNode>>>() {

			});
		} catch (final JsonParseException e) {

			TransformationFlow.LOG.debug("couldn't parse the transformation result tuples to a JSON string");
		} catch (final JsonMappingException e) {

			TransformationFlow.LOG.debug("couldn't map the transformation result tuples to a JSON string");
		} catch (final IOException e) {

			TransformationFlow.LOG.debug("something went wrong while processing the transformation result tuples to a JSON string");
		}

		if (tuplesList == null) {

			final String msg = "couldn't process the transformation result tuples to a JSON string";
			TransformationFlow.LOG.debug(msg);

			return Observable.error(new DMPConverterException(msg));
		}

		final boolean writeResultToDatahub = false;
		final boolean doNotReturnJsonToCaller = false;
		final boolean enableVersioning = true;

		return apply(Observable.from(tuplesList), writeResultToDatahub, doNotReturnJsonToCaller, enableVersioning, Schedulers.newThread()).reduce(
				DMPPersistenceUtil.getJSONObjectMapper().createArrayNode(),
				ArrayNode::add
		).map(arrayNode -> {
			try {
				return DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(arrayNode);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public Observable<String> applyResource(final String resourcePath) throws DMPConverterException {

		try {
			return applyRecord(DMPPersistenceUtil.getResourceAsString(resourcePath));
		} catch (final IOException e) {

			return Observable.error(new DMPConverterException(e.getMessage(), e));
		}
	}

	public Observable<JsonNode> apply(
			final Observable<Tuple<String, JsonNode>> tuples,
			final ObjectPipe<Tuple<String, JsonNode>, StreamReceiver> opener,
			final boolean writeResultToDatahub, final boolean doNotReturnJsonToCaller, final boolean enableVersioning, final Scheduler scheduler)
			throws DMPConverterException {

		final Context morphContext = morphTimer.time();

		LOG.debug("start processing some records in transformation engine");

		// final String recordDummy = "record";

		final StreamTimer inputTimer = timerBasedFactory.forStream("stream-input");
		final ObjectTimer gdmModelsTimer = timerBasedFactory.forObject("gdm-models");
		final StreamUnflattener unflattener = new StreamUnflattener("", DMPStatics.ATTRIBUTE_DELIMITER);
		//		final StreamJsonCollapser collapser = new StreamJsonCollapser();
		final GDMEncoder converter = new GDMEncoder(outputDataModel);

		final GDMModelReceiver writer = new GDMModelReceiver(TRANSFORMATION_ENGINE_IDENTIFIER);

		final StreamPipe<StreamReceiver> starter;
		if (optionalSkipFilter.isPresent()) {

			// skip filter + input timer
			starter = opener
					.setReceiver(optionalSkipFilter.get())
					.setReceiver(inputTimer);

		} else {

			// input timer
			starter = opener.setReceiver(inputTimer);
		}

		starter
				.setReceiver(transformer)
				.setReceiver(unflattener)
				.setReceiver(converter)
				.setReceiver(gdmModelsTimer)
				.setReceiver(writer);

		// transform to FE friendly JSON => or use Model#toJSON() ;)

		final Optional<String> optionalDataModelSchemaRecordClassURI =
				getDataModelSchemaRecordClassURI();

		final String defaultRecordClassURI =
				optionalDataModelSchemaRecordClassURI.orElse(ClaszUtils.BIBO_DOCUMENT_URI);

		final AtomicInteger counter = new AtomicInteger(0);
		final AtomicInteger counter2 = new AtomicInteger(0);
		final AtomicLong statementCounter = new AtomicLong(0);

		final Observable<org.dswarm.persistence.model.internal.Model> model = writer.getObservable().filter(gdmModel -> {

			if (counter.incrementAndGet() == 1) {

				LOG.debug("start processing first record in transformation engine");
			}

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
		}).map(gdmModel -> {

			final GDMModel finalGDMModel;

			// TODO: this a WORKAROUND to insert a default type (data model schema record class URI or bibo:Document) for records in the output data model
			if (gdmModel.getRecordClassURI() == null) {

				final String recordURI = gdmModel.getRecordURIs().iterator().next();

				final Resource recordResource = gdmModel.getModel().getResource(recordURI);

				if (recordResource != null) {

					// TODO check this: subject OK?
					recordResource.addStatement(getOrCreateResourceNode(recordResource.getUri()), RDF_TYPE_PREDICATE,
							getOrCreateResourceNode(defaultRecordClassURI));
				}

				// re-write GDM model
				finalGDMModel = new GDMModel(gdmModel.getModel(), recordURI, defaultRecordClassURI);
			} else {

				finalGDMModel = gdmModel;
			}

			statementCounter.addAndGet(gdmModel.getModel().size());

			if (counter2.incrementAndGet() == 1) {

				LOG.info("processed first record with '{}' statements in transformation engine", statementCounter.get());
			}

			return finalGDMModel;
		}).cast(org.dswarm.persistence.model.internal.Model.class).doOnCompleted(
				() -> LOG.info("processed '{}' records (from '{}') with '{}' statements in transformation engine", counter2.get(), counter.get(),
						statementCounter.get()));

		final Observable<JsonNode> resultObservable;

		if (doNotReturnJsonToCaller) {

			resultObservable = Observable.empty();
		} else {

			final AtomicInteger resultCounter = new AtomicInteger(0);

			resultObservable = model.onBackpressureBuffer(10000).doOnSubscribe(() -> TransformationFlow.LOG.debug("subscribed to results observable in transformation engine"))
					.doOnNext(resultObj -> {

						resultCounter.incrementAndGet();

						if (resultCounter.get() == 1) {

							TransformationFlow.LOG.debug("received first result in transformation engine");
						}
					}).doOnCompleted(() -> TransformationFlow.LOG.debug("received '{}' results in transformation engine overall", resultCounter.get()))
					.map(org.dswarm.persistence.model.internal.Model::toJSON).flatMapIterable(nodes -> {

						final ArrayList<JsonNode> nodeList = new ArrayList<>();

						Iterators.addAll(nodeList, nodes.elements());

						return nodeList;
					}).onBackpressureBuffer(10000);
		}
		final Observable<Response> writeResponse;

		if (writeResultToDatahub) {

			LOG.debug("write transformation result to datahub");

			if (hasDefined(outputDataModel, DMPObject::getUuid)) {

				// write result to graph db
				final InternalModelService internalModelService = internalModelServiceFactoryProvider.get().getInternalGDMGraphService();

				try {

					writeResponse = internalModelService.updateObject(outputDataModel.get().getUuid(), model.onBackpressureBuffer(10000), UpdateFormat.DELTA, enableVersioning);
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

		} else {

			writeResponse = Observable.empty();
		}

		return Observable.create(new Observable.OnSubscribe<JsonNode>() {

			@Override
			public void call(final Subscriber<? super JsonNode> subscriber) {

				resultObservable.subscribeOn(scheduler)
						.compose(new AndThenWaitFor<>(writeResponse, DMPPersistenceUtil.getJSONObjectMapper()::createArrayNode))
						.doOnCompleted(morphContext::stop)
						.subscribe(subscriber);

				final AtomicInteger counter = new AtomicInteger(0);

				final Observable<Tuple<String, JsonNode>> tupleObservable = tuples.subscribeOn(scheduler);

				tupleObservable.doOnNext(tuple -> {

					if (counter.incrementAndGet() == 1) {

						LOG.debug("received first record in transformation engine");
					}
				}).doOnCompleted(opener::closeStream)
						.doOnCompleted(() -> LOG.info("received '{}' records in transformation engine", counter.get()))
						.subscribe(opener::process, writer::propagateError, () -> LOG.debug("DONE"));
			}
		}).doOnCompleted(() -> {

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
		}).subscribeOn(scheduler);
	}

	public Observable<JsonNode> apply(final Observable<Tuple<String, JsonNode>> tuples, final boolean writeResultToDatahub,
	                                  final boolean doNotReturnJsonToCaller, final boolean enableVersioning, final Scheduler scheduler) throws DMPConverterException {

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

	private static <T> boolean exists(final Optional<T> optional, final java.util.function.Predicate<T> predicate) {
		return optional.isPresent() && predicate.test(optional.get());
	}

	private static <T, U> boolean hasDefined(final Optional<T> optional, final Function<T, U> access) {
		return exists(optional, t -> access.apply(t) != null);
	}

	private static class AndThenWaitFor<T, U> implements Observable.Transformer<T, T> {

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
