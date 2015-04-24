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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.converter.flow.XMLSourceResourceGDMStmtsFlow;
import org.dswarm.converter.flow.XmlResourceFlowFactory;
import org.dswarm.graph.json.Model;
import org.dswarm.graph.json.Resource;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.resource.UpdateFormat;
import org.dswarm.persistence.model.resource.utils.ResourceStatics;
import org.dswarm.persistence.monitoring.MonitoringLogger;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.monitoring.MonitoringHelper;
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

		try (final MonitoringHelper ignore = loggerProvider.get().startIngest(dataModel)) {
			processDataModel(dataModel, updateFormat);
		}
	}

	public void processDataModel(final DataModel dataModel, final UpdateFormat updateFormat) throws DMPControllerException {

		LOG.debug("try to process xml data resource into data model '{}'", dataModel.getUuid());

		GDMModel result = null;

		try {

			final XMLSourceResourceGDMStmtsFlow flow = xmlFlowFactory.get().fromDataModel(dataModel);

			final String path = dataModel.getDataResource().getAttribute(ResourceStatics.PATH).asText();

			LOG.debug("process xml data resource at '{}' into data model '{}'", path, dataModel.getUuid());

			final List<GDMModel> gdmModels = flow.applyResource(path);

			// write GDM models at once
			final Model model = new Model();
			String recordClassUri = null;

			for (final GDMModel gdmModel : gdmModels) {

				if (gdmModel.getModel() != null) {

					final Model aModel = gdmModel.getModel();

					if (aModel.getResources() != null) {

						final Collection<Resource> resources = aModel.getResources();

						for (final Resource resource : resources) {

							model.addResource(resource);

							if (recordClassUri == null) {

								recordClassUri = gdmModel.getRecordClassURI();
							}
						}
					}
				}
			}

			result = new GDMModel(model, null, recordClassUri);

			LOG.debug("transformed xml data resource at '{}' to GDM for data model '{}'", path, dataModel.getUuid());
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

			internalServiceFactory.getInternalGDMGraphService().updateObject(dataModel.getUuid(), result, updateFormat);

			LOG.debug("processed xml data resource  into data model '{}'", dataModel.getUuid());
		} catch (final DMPPersistenceException e) {

			final String message = String.format("couldn't persist the converted data of data model '%s'", dataModel.getUuid());

			XMLConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(String.format("%s %s", message, e.getMessage()), e);
		}
	}
}
