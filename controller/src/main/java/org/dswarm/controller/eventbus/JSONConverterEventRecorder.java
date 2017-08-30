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

import java.util.concurrent.CompletableFuture;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;

import org.dswarm.converter.flow.JSONSourceResourceGDMStmtsFlow;
import org.dswarm.converter.flow.JsonResourceFlowFactory;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.monitoring.MonitoringLogger;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.service.internal.graph.util.SchemaDeterminator;

/**
 * An event recorder for converting JSON documents.
 *
 * @author phorn
 * @author tgaengler
 */
@Singleton
public class JSONConverterEventRecorder extends ConverterEventRecorder<JSONConverterEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(JSONConverterEventRecorder.class);

	private static final String TYPE = "JSON";

	/**
	 * The internal model service factory
	 */
	private final Provider<JsonResourceFlowFactory> jsonFlowFactory;

	/**
	 * Creates a new event recorder for converting JSON documents with the given internal model service factory and event bus.
	 *
	 * @param internalModelServiceFactory an internal model service factory
	 */
	@Inject
	public JSONConverterEventRecorder(
			final InternalModelServiceFactory internalModelServiceFactory,
			final Provider<JsonResourceFlowFactory> jsonFlowFactory,
			final Provider<MonitoringLogger> loggerProvider,
			final Provider<SchemaDeterminator> schemaDeterminatorProvider) {

		super(internalModelServiceFactory, loggerProvider, schemaDeterminatorProvider, TYPE);

		this.jsonFlowFactory = jsonFlowFactory;
	}

	@Override
	protected Observable<GDMModel> convertData(final DataModel dataModel, final boolean utiliseExistingSchema, final Scheduler scheduler,
			final String path, final boolean hasSchema) {

		final CompletableFuture<JSONSourceResourceGDMStmtsFlow> futureFlow = CompletableFuture
				.supplyAsync(() -> jsonFlowFactory.get().fromDataModel(dataModel, utiliseExistingSchema));
		final Observable<JSONSourceResourceGDMStmtsFlow> obserableFlow = Observable.from(futureFlow);

		return obserableFlow.subscribeOn(scheduler).flatMap(flow -> {

			LOG.debug("process JSON data resource at '{}' into data model '{}' (has schema = '{}')", path, dataModel.getUuid(), hasSchema);

			return flow.applyResource(path);
		});
	}
}
