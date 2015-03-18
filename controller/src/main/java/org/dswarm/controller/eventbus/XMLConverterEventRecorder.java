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
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.service.InternalModelServiceFactory;

/**
 * An event recorder for converting XML documents.
 *
 * @author phorn
 * @author tgaengler
 */
@Singleton
public class XMLConverterEventRecorder {

	private static final Logger					LOG	= LoggerFactory.getLogger(XMLConverterEventRecorder.class);

	/**
	 * The internal model service factory
	 */
	private final InternalModelServiceFactory	internalServiceFactory;
	private final Provider<XmlResourceFlowFactory> xmlFlowFactory;

	/**
	 * Creates a new event recorder for converting XML documents with the given internal model service factory and event bus.
	 *
	 * @param internalModelServiceFactory an internal model service factory
	 */
	@Inject
	public XMLConverterEventRecorder(
			final InternalModelServiceFactory internalModelServiceFactory,
			final Provider<XmlResourceFlowFactory> xmlFlowFactory) {
		internalServiceFactory = internalModelServiceFactory;
		this.xmlFlowFactory = xmlFlowFactory;
	}

	/**
	 * Processes the XML document of the data model of the given event and persists the converted data.
	 *
	 * @param event an converter event that provides a data model
	 */
	// @Subscribe
	public void processDataModel(final XMLConverterEvent event) throws DMPControllerException {

		final DataModel dataModel = event.getDataModel();

		GDMModel result = null;

		try {

			final XMLSourceResourceGDMStmtsFlow flow = xmlFlowFactory.get().fromDataModel(dataModel);

			final String path = dataModel.getDataResource().getAttribute("path").asText();
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

		} catch (final NullPointerException e) {

			final String message = "couldn't convert the XML data of data model '" + dataModel.getUuid() + "'";

			XMLConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(message + " " + e.getMessage(), e);
		} catch (final Exception e) {

			final String message = "really couldn't convert the XML data of data model '" + dataModel.getUuid() + "'";

			XMLConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(message + " " + e.getMessage(), e);
		}

		try {

			internalServiceFactory.getInternalGDMGraphService().createObject(dataModel.getUuid(), result);
		} catch (final DMPPersistenceException e) {

			final String message = "couldn't persist the converted data of data model '" + dataModel.getUuid() + "'";

			XMLConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(message + " " + e.getMessage(), e);
		}
	}
}
