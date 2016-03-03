/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.converter.flow;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import org.apache.commons.lang3.NotImplementedException;
import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.stream.pipe.Filter;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.pipe.timing.TimerBasedFactory;
import org.dswarm.graph.json.Model;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.observables.ConnectableObservable;

import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Flow that executes a given set of transformations on data of a given data model.
 *
 * @author phorn
 * @author tgaengler
 * @author sreichert
 * @author polowins
 */
public class GDMTriplesTransformationFlow extends TransformationFlow<Model> {

	private static final Logger LOG = LoggerFactory.getLogger(GDMTriplesTransformationFlow.class);

	@Inject
	private GDMTriplesTransformationFlow(
			final Provider<InternalModelServiceFactory> internalModelServiceFactoryProviderArg,
			@Named("Monitoring") final MetricRegistry registry,
			final TimerBasedFactory timerBasedFactory,
			@Assisted final Metamorph transformer,
			@Assisted final String scriptArg,
			@Assisted final Optional<DataModel> outputDataModelArg,
			@Assisted final Optional<Filter> optionalSkipFilterArg) {

		super(internalModelServiceFactoryProviderArg, registry, timerBasedFactory, transformer, scriptArg, outputDataModelArg, optionalSkipFilterArg);
	}

	public Observable<String> applyRecord(final String record) throws DMPConverterException {

		throw new NotImplementedException("TODO");
	}

	protected ConnectableObservable<Model> transformResultModel(final Observable<org.dswarm.persistence.model.internal.Model> model) {

		final AtomicInteger resultCounter = new AtomicInteger(0);

		return model.onBackpressureBuffer(10000).doOnSubscribe(() -> GDMTriplesTransformationFlow.LOG.debug("subscribed to results observable in transformation engine"))
				.doOnNext(resultObj -> {

					resultCounter.incrementAndGet();

					if (resultCounter.get() == 1) {

						GDMTriplesTransformationFlow.LOG.debug("received first result in transformation engine");
					}
				}).doOnCompleted(() -> GDMTriplesTransformationFlow.LOG.debug("received '{}' results in transformation engine overall", resultCounter.get()))
				.cast(org.dswarm.persistence.model.internal.gdm.GDMModel.class)
				.map(org.dswarm.persistence.model.internal.gdm.GDMModel::getModel).onBackpressureBuffer(10000).publish();
	}

	protected AndThenWaitFor<Model, Response> concatStreams(final Observable<Response> writeResponse) {

		return new AndThenWaitFor<>(writeResponse, GDMTriplesTransformationFlow::createModel);
	}

	private static Model createModel() {

		return new Model();
	}
}
