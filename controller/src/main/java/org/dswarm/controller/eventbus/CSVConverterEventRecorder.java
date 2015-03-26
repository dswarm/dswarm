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

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.culturegraph.mf.types.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.flow.CSVResourceFlowFactory;
import org.dswarm.converter.flow.CSVSourceResourceTriplesFlow;
import org.dswarm.graph.json.LiteralNode;
import org.dswarm.graph.json.Model;
import org.dswarm.graph.json.Predicate;
import org.dswarm.graph.json.Resource;
import org.dswarm.graph.json.ResourceNode;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.MonitoringLogger;
import org.dswarm.persistence.MonitoringLogger.MonitoringHelper;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.service.InternalModelServiceFactory;

@Singleton
public class CSVConverterEventRecorder {

	private static final Logger					LOG	= LoggerFactory.getLogger(CSVConverterEventRecorder.class);

	private final Provider<CSVResourceFlowFactory> flowFactory2;
	private final InternalModelServiceFactory	internalServiceFactory;
	private final Provider<MonitoringLogger> loggerProvider;

	@Inject
	public CSVConverterEventRecorder(
			final Provider<CSVResourceFlowFactory> flowFactory2,
			final InternalModelServiceFactory internalServiceFactory,
			final Provider<MonitoringLogger> loggerProvider) {

		this.flowFactory2 = flowFactory2;
		this.internalServiceFactory = internalServiceFactory;
		this.loggerProvider = loggerProvider;
	}

	public void convertConfiguration(final CSVConverterEvent event) throws DMPControllerException {
		final DataModel dataModel = event.getDataModel();

		try (final MonitoringHelper ignore = loggerProvider.get().startIngest(dataModel)) {
			convertConfiguration(dataModel);
		}
	}

	private void convertConfiguration(final DataModel dataModel) throws DMPControllerException {

		List<Triple> result = null;
		try {
			final CSVSourceResourceTriplesFlow flow = flowFactory2.get().fromDataModel(dataModel);

			final String path = dataModel.getDataResource().getAttribute("path").asText();
			result = flow.applyFile(path);

		} catch (final DMPConverterException | NullPointerException e) {

			final String message = "couldn't convert the CSV data of data model '" + dataModel.getUuid() + "'";

			CSVConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(message + " " + e.getMessage(), e);
		} catch (final Exception e) {

			final String message = "really couldn't convert the CSV data of data model '" + dataModel.getUuid() + "'";

			CSVConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(message + " " + e.getMessage(), e);
		}

		if (result != null) {

			// convert result to GDM
			final Map<Long, Resource> recordResources = Maps.newLinkedHashMap();

			final Model model = new Model();

			final String dataResourceBaseSchemaURI = DataModelUtils.determineDataModelSchemaBaseURI(dataModel);
			final String recordClassURI = dataResourceBaseSchemaURI + "RecordType";
			final ResourceNode recordClassNode = new ResourceNode(recordClassURI);

			for (final Triple triple : result) {

				// 1. create resource uri from subject (line number)
				// 2. create property object from property uri string
				// 3. convert objects (string literals) to string literals (?)

				final Resource recordResource = DataModelUtils.mintRecordResource(Long.valueOf(triple.getSubject()), dataModel, recordResources,
						model, recordClassNode);
				final Predicate property = new Predicate(triple.getPredicate());

				final ResourceNode subject = (ResourceNode) recordResource.getStatements().iterator().next().getSubject();

				recordResource.addStatement(subject, property, new LiteralNode(triple.getObject()));
			}

			final GDMModel gdmModel = new GDMModel(model, null, recordClassURI);

			try {

				internalServiceFactory.getInternalGDMGraphService().createObject(dataModel.getUuid(), gdmModel);
			} catch (final DMPPersistenceException e) {

				final String message = "couldn't persist the converted CSV data of data model '" + dataModel.getUuid() + "'";

				CSVConverterEventRecorder.LOG.error(message, e);

				throw new DMPControllerException(message + " " + e.getMessage(), e);
			}
		}
	}
}
