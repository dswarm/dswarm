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

import javax.ws.rs.core.Response;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.culturegraph.mf.types.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

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
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.UpdateFormat;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.model.resource.utils.ResourceStatics;
import org.dswarm.persistence.monitoring.MonitoringHelper;
import org.dswarm.persistence.monitoring.MonitoringLogger;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.util.GDMUtil;

@Singleton
public class CSVConverterEventRecorder {

	private static final Logger LOG                 = LoggerFactory.getLogger(CSVConverterEventRecorder.class);
	private static final String RECORD_TYPE_POSTFIX = "RecordType";

	private final Provider<CSVResourceFlowFactory> flowFactory;
	private final InternalModelServiceFactory      internalServiceFactory;
	private final Provider<MonitoringLogger>       loggerProvider;

	@Inject
	public CSVConverterEventRecorder(
			final Provider<CSVResourceFlowFactory> flowFactory,
			final InternalModelServiceFactory internalServiceFactory,
			final Provider<MonitoringLogger> loggerProvider) {

		this.flowFactory = flowFactory;
		this.internalServiceFactory = internalServiceFactory;
		this.loggerProvider = loggerProvider;
	}

	public void convertConfiguration(final CSVConverterEvent event) throws DMPControllerException {

		final DataModel dataModel = event.getDataModel();
		final UpdateFormat updateFormat = event.getUpdateFormat();
		final boolean enableVersioning = event.isEnableVersioning();

		try (final MonitoringHelper ignore = loggerProvider.get().startIngest(dataModel)) {
			convertConfiguration(dataModel, updateFormat, enableVersioning);
		}
	}

	private void convertConfiguration(final DataModel dataModel, final UpdateFormat updateFormat, final boolean enableVersioning)
			throws DMPControllerException {

		LOG.debug("try to process csv data resource into data model '{}'", dataModel.getUuid());

		Observable<Collection<Triple>> result = null;
		try {

			final CSVSourceResourceTriplesFlow flow = flowFactory.get().fromDataModel(dataModel);

			final String path = dataModel.getDataResource().getAttribute(ResourceStatics.PATH).asText();

			LOG.debug("process csv data resource at '{}' into data model '{}'", path, dataModel.getUuid());

			result = flow.applyFile(path);

		} catch (final DMPConverterException | NullPointerException e) {

			final String message = String.format("couldn't convert the CSV data of data model '%s'", dataModel.getUuid());

			CSVConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(String.format("%s %s", message, e.getMessage()), e);
		} catch (final Exception e) {

			final String message = String.format("really couldn't convert the CSV data of data model '%s'", dataModel.getUuid());

			CSVConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(String.format("%s %s", message, e.getMessage()), e);
		}

		if (result != null) {

			LOG.debug("transformed CSV data resource to triples for data model '{}'", dataModel.getUuid());

			// convert result to GDM

			final String dataResourceBaseSchemaURI = DataModelUtils.determineDataModelSchemaBaseURI(dataModel);
			final String recordClassURI = dataResourceBaseSchemaURI + RECORD_TYPE_POSTFIX;
			final ResourceNode recordClassNode = new ResourceNode(recordClassURI);

			//final AtomicInteger counter = new AtomicInteger(0);

			//LOG.debug("CSV triples = '{}'", result.size());

			final Observable<org.dswarm.persistence.model.internal.Model> models = result
					.map(triples -> {

						// 1. create resource uri from subject (line number)
						final Resource recordResource = DataModelUtils.mintRecordResource(dataModel);

						//final int current = counter.incrementAndGet();

						//LOG.debug("CSV resource number '{}' with '{}'", current, recordResource.getUri());

						// add resource type statement to model
						recordResource.addStatement(new ResourceNode(recordResource.getUri()), new Predicate(GDMUtil.RDF_type), recordClassNode);

						triples.stream().forEach(triple -> {

							// 2. create property object from property uri string
							final Predicate property = new Predicate(triple.getPredicate());

							final ResourceNode subject = (ResourceNode) recordResource.getStatements().iterator().next().getSubject();

							// 3. convert objects (string literals) to string literals (?)
							final LiteralNode object = new LiteralNode(triple.getObject());

							recordResource.addStatement(subject, property, object);
						});

						return recordResource;
					}).map(finalResource -> {

						final Model model = new Model();
						model.addResource(finalResource);

						//final int current2 = counter.get();
						//LOG.debug("CSV resource number '{}' with '{}' and '{}' statement", current2, finalResource.getUri(),
						//		finalResource.size());

						return new GDMModel(model, null, recordClassURI);
					});

			models.doOnCompleted(() -> LOG.debug("transformed CSV data resource to GDM for data model '{}'", dataModel.getUuid()));

			try {

				final Observable<Response> writeResponse = internalServiceFactory.getInternalGDMGraphService()
						.updateObject(dataModel.getUuid(), models, updateFormat, enableVersioning);

				//LOG.debug("before to blocking");

				// TODO: delegate observable
				writeResponse.toBlocking().firstOrDefault(null);

				LOG.debug("processed CSV data resource into data model '{}'", dataModel.getUuid());
			} catch (final DMPPersistenceException e) {

				final String message = String.format("couldn't persist the converted CSV data of data model '%s'", dataModel.getUuid());

				CSVConverterEventRecorder.LOG.error(message, e);

				throw new DMPControllerException(String.format("%s %s", message, e.getMessage()), e);
			}
		}
	}
}
