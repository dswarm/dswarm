package org.dswarm.controller.eventbus;

import java.util.List;
import java.util.Map;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.flow.CSVResourceFlowFactory;
import org.dswarm.converter.flow.CSVSourceResourceTriplesFlow;
import org.dswarm.graph.json.LiteralNode;
import org.dswarm.graph.json.Predicate;
import org.dswarm.graph.json.Resource;
import org.dswarm.graph.json.ResourceNode;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CSVConverterEventRecorder {

	private static final Logger					LOG	= LoggerFactory.getLogger(CSVConverterEventRecorder.class);

	private final InternalModelServiceFactory	internalServiceFactory;

	@Inject
	public CSVConverterEventRecorder(final InternalModelServiceFactory internalServiceFactory/* , final EventBus eventBus */) {

		this.internalServiceFactory = internalServiceFactory;
		// eventBus.register(this);
	}

	// @Subscribe
	public void convertConfiguration(final CSVConverterEvent event) throws DMPControllerException {

		final DataModel dataModel = event.getDataModel();

		List<org.culturegraph.mf.types.Triple> result = null;
		try {
			final CSVSourceResourceTriplesFlow flow = CSVResourceFlowFactory.fromDataModel(dataModel, CSVSourceResourceTriplesFlow.class);

			final String path = dataModel.getDataResource().getAttribute("path").asText();
			result = flow.applyFile(path);

		} catch (final DMPConverterException | NullPointerException e) {

			final String message = "couldn't convert the CSV data of data model '" + dataModel.getId() + "'";

			CSVConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(message + " " + e.getMessage(), e);
		} catch (final Exception e) {

			final String message = "really couldn't convert the CSV data of data model '" + dataModel.getId() + "'";

			CSVConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(message + " " + e.getMessage(), e);
		}

		if (result != null) {

			// convert result to GDM
			final Map<Long, org.dswarm.graph.json.Resource> recordResources = Maps.newLinkedHashMap();

			final org.dswarm.graph.json.Model model = new org.dswarm.graph.json.Model();

			final String dataResourceBaseSchemaURI = DataModelUtils.determineDataModelSchemaBaseURI(dataModel);
			final String recordClassURI = dataResourceBaseSchemaURI + "RecordType";
			final ResourceNode recordClassNode = new ResourceNode(recordClassURI);

			for (final org.culturegraph.mf.types.Triple triple : result) {

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

				internalServiceFactory.getInternalGDMGraphService().createObject(dataModel.getId(), gdmModel);
			} catch (final DMPPersistenceException e) {

				final String message = "couldn't persist the converted CSV data of data model '" + dataModel.getId() + "'";

				CSVConverterEventRecorder.LOG.error(message, e);

				throw new DMPControllerException(message + " " + e.getMessage(), e);
			}
		}
	}
}
