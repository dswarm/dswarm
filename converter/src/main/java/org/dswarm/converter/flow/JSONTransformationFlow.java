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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.core.Response;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Iterators;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import javaslang.Tuple2;
import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.stream.pipe.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import org.dswarm.common.types.Tuple;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.pipe.timing.TimerBasedFactory;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * Flow that executes a given set of transformations on data of a given data model.
 *
 * @author phorn
 * @author tgaengler
 * @author sreichert
 * @author polowins
 */
public class JSONTransformationFlow extends TransformationFlow<JsonNode> {

	private static final Logger LOG = LoggerFactory.getLogger(JSONTransformationFlow.class);

	@Inject
	private JSONTransformationFlow(final Provider<InternalModelServiceFactory> internalModelServiceFactoryProviderArg,
	                               @Named("Monitoring") final MetricRegistry registry,
	                               final TimerBasedFactory timerBasedFactory,
	                               @Assisted final Metamorph transformer,
	                               @Assisted final String scriptArg,
	                               @Assisted final Optional<DataModel> outputDataModelArg,
	                               @Assisted final Optional<Filter> optionalSkipFilterArg) {

		super(internalModelServiceFactoryProviderArg, registry, timerBasedFactory, transformer, scriptArg, outputDataModelArg, optionalSkipFilterArg);
	}

	public Observable<String> applyRecord(final String record) throws DMPConverterException {

		// TODO: convert JSON string to Iterator with tuples of string + JsonNode pairs
		List<Tuple<String, JsonNode>> tuplesList = null;

		try {

			tuplesList = DMPPersistenceUtil.getJSONObjectMapper().readValue(record, new TypeReference<List<Tuple<String, JsonNode>>>() {

			});
		} catch (final JsonParseException e) {

			JSONTransformationFlow.LOG.debug("couldn't parse the transformation result tuples to a JSON string");
		} catch (final JsonMappingException e) {

			JSONTransformationFlow.LOG.debug("couldn't map the transformation result tuples to a JSON string");
		} catch (final IOException e) {

			JSONTransformationFlow.LOG.debug("something went wrong while processing the transformation result tuples to a JSON string");
		}

		if (tuplesList == null) {

			final String msg = "couldn't process the transformation result tuples to a JSON string";
			JSONTransformationFlow.LOG.debug(msg);

			return Observable.error(new DMPConverterException(msg));
		}

		final List<Tuple2<String, JsonNode>> finalTuplesList = javaslang.collection.List.ofAll(tuplesList)
				.map(tuple -> javaslang.Tuple.of(tuple.v1(), tuple.v2()))
				.toJavaList();

		final boolean writeResultToDatahub = false;
		final boolean doNotReturnJsonToCaller = false;
		final boolean enableVersioning = true;

		final ConnectableObservable<JsonNode> observable = apply(Observable.from(finalTuplesList), writeResultToDatahub, doNotReturnJsonToCaller, enableVersioning, Schedulers.newThread());

		final Observable<String> result = observable.reduce(
				DMPPersistenceUtil.getJSONObjectMapper().createArrayNode(),
				ArrayNode::add
		).map(arrayNode -> {
			try {
				return DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(arrayNode);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		});

		observable.connect();

		return result;
	}

	protected ConnectableObservable<JsonNode> transformResultModel(final Observable<org.dswarm.persistence.model.internal.Model> model) {

		final AtomicInteger resultCounter = new AtomicInteger(0);

		// transform to FE friendly JSON => or use Model#toJSON() ;)

		return model.onBackpressureBuffer(10000)
				.doOnSubscribe(() -> JSONTransformationFlow.LOG.debug("subscribed to results observable in transformation engine"))
				.doOnNext(resultObj -> {

					resultCounter.incrementAndGet();

					if (resultCounter.get() == 1) {

						JSONTransformationFlow.LOG.debug("received first result in transformation engine");
					}
				})
				.doOnCompleted(() -> JSONTransformationFlow.LOG.debug("received '{}' results in transformation engine overall", resultCounter.get()))
				.map(org.dswarm.persistence.model.internal.Model::toGDMCompactJSON)
				.flatMapIterable(nodes -> {

					final ArrayList<JsonNode> nodeList = new ArrayList<>();

					Iterators.addAll(nodeList, nodes.elements());

					return nodeList;
				})
				.onBackpressureBuffer(10000)
				.publish();
	}

	@Override
	protected AndThenWaitFor<JsonNode, Response> concatStreams(final Observable<Response> writeResponse) {

		return new AndThenWaitFor<>(writeResponse, DMPPersistenceUtil.getJSONObjectMapper()::createArrayNode);
	}
}
