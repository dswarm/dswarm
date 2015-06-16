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
package org.dswarm.controller.eventbus;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.core.Response;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Action0;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.converter.flow.XMLSourceResourceGDMStmtsFlow;
import org.dswarm.converter.flow.XmlResourceFlowFactory;
import org.dswarm.graph.json.Model;
import org.dswarm.graph.json.Resource;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.UpdateFormat;
import org.dswarm.persistence.model.resource.utils.ResourceStatics;
import org.dswarm.persistence.monitoring.MonitoringHelper;
import org.dswarm.persistence.monitoring.MonitoringLogger;
import org.dswarm.persistence.service.InternalModelServiceFactory;

/**
 * An event recorder for converting XML documents.
 *
 * @author phorn
 * @author tgaengler
 */
@Singleton
public class XMLConverterEventRecorder {

	private static final Logger LOG = LoggerFactory.getLogger(XMLConverterEventRecorder.class);

	/**
	 * The internal model service factory
	 */
	private final InternalModelServiceFactory      internalServiceFactory;
	private final Provider<XmlResourceFlowFactory> xmlFlowFactory;
	private final Provider<MonitoringLogger>       loggerProvider;

	/**
	 * Creates a new event recorder for converting XML documents with the given internal model service factory and event bus.
	 *
	 * @param internalModelServiceFactory an internal model service factory
	 */
	@Inject
	public XMLConverterEventRecorder(
			final InternalModelServiceFactory internalModelServiceFactory,
			final Provider<XmlResourceFlowFactory> xmlFlowFactory,
			final Provider<MonitoringLogger> loggerProvider) {
		internalServiceFactory = internalModelServiceFactory;
		this.xmlFlowFactory = xmlFlowFactory;
		this.loggerProvider = loggerProvider;
	}

	/**
	 * Processes the XML document of the data model of the given event and persists the converted data.
	 *
	 * @param event an converter event that provides a data model
	 */
	// @Subscribe
	public void processDataModel(final XMLConverterEvent event) throws DMPControllerException {

		final DataModel dataModel = event.getDataModel();
		final UpdateFormat updateFormat = event.getUpdateFormat();
		final boolean enableVersioning = event.isEnableVersioning();

		try (final MonitoringHelper ignore = loggerProvider.get().startIngest(dataModel)) {
			processDataModel(dataModel, updateFormat, enableVersioning);
		}
	}

	public void processDataModel(final DataModel dataModel, final UpdateFormat updateFormat, final boolean enableVersioning)
			throws DMPControllerException {

		LOG.debug("try to process xml data resource into data model '{}'", dataModel.getUuid());

		rx.Observable<org.dswarm.persistence.model.internal.Model> result = null;

		try {

			final XMLSourceResourceGDMStmtsFlow flow = xmlFlowFactory.get().fromDataModel(dataModel);

			final String path = dataModel.getDataResource().getAttribute(ResourceStatics.PATH).asText();

			LOG.debug("process xml data resource at '{}' into data model '{}'", path, dataModel.getUuid());

			final Observable<GDMModel> gdmModels = flow.applyResource(path);

			//final AtomicInteger counter = new AtomicInteger(0);

			//LOG.debug("XML records = '{}'", gdmModels.size());

			result = gdmModels.filter(gdmModel -> {

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

				//final int current = counter.incrementAndGet();

				//LOG.debug("XML resource number '{}' with '{}' and resources size = '{}'", current, resources.iterator().next().getUri(), resources.size());

				return true;

			}).cast(org.dswarm.persistence.model.internal.Model.class).doOnCompleted(
					() -> LOG.debug("transformed xml data resource at '{}' to GDM for data model '{}'", path, dataModel.getUuid()));
		} catch (final NullPointerException e) {

			final String message = String.format("couldn't convert the XML data of data model '%s'", dataModel.getUuid());

			XMLConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(String.format("%s %s", message, e.getMessage()), e);
		} catch (final Exception e) {

			final String message = String.format("really couldn't convert the XML data of data model '%s'", dataModel.getUuid());

			XMLConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(String.format("%s %s", message, e.getMessage()), e);
		}

		try {

			final Observable<Response> writeResponse = internalServiceFactory.getInternalGDMGraphService()
					.updateObject(dataModel.getUuid(), result, updateFormat, enableVersioning);

			//LOG.debug("before to blocking");

			// TODO: delegate observable
			writeResponse.toBlocking().firstOrDefault(null);

			LOG.debug("processed xml data resource into data model '{}'", dataModel.getUuid());
		} catch (final DMPPersistenceException e) {

			final String message = String.format("couldn't persist the converted data of data model '%s'", dataModel.getUuid());

			XMLConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(String.format("%s %s", message, e.getMessage()), e);
		}
	}
}
