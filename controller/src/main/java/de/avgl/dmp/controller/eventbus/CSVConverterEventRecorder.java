package de.avgl.dmp.controller.eventbus;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.CSVResourceFlowFactory;
import de.avgl.dmp.converter.flow.CSVSourceResourceTriplesFlow;
import de.avgl.dmp.graph.json.LiteralNode;
import de.avgl.dmp.graph.json.Predicate;
import de.avgl.dmp.graph.json.Resource;
import de.avgl.dmp.graph.json.ResourceNode;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.internal.gdm.GDMModel;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.utils.DataModelUtils;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;

@Singleton
public class CSVConverterEventRecorder {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(CSVConverterEventRecorder.class);

	private final InternalModelServiceFactory		internalServiceFactory;

	@Inject
	public CSVConverterEventRecorder(final InternalModelServiceFactory internalServiceFactory, final EventBus eventBus) {

		this.internalServiceFactory = internalServiceFactory;
		eventBus.register(this);
	}

	@Subscribe
	public void convertConfiguration(final CSVConverterEvent event) {

		final DataModel dataModel = event.getDataModel();

		List<org.culturegraph.mf.types.Triple> result = null;
		try {
			final CSVSourceResourceTriplesFlow flow = CSVResourceFlowFactory.fromDataModel(dataModel, CSVSourceResourceTriplesFlow.class);

			final String path = dataModel.getDataResource().getAttribute("path").asText();
			result = flow.applyFile(path);

		} catch (DMPConverterException | NullPointerException e) {
			e.printStackTrace();
		}

		if (result != null) {

			// convert result to GDM
			final Map<Long, de.avgl.dmp.graph.json.Resource> recordResources = Maps.newLinkedHashMap();

			final de.avgl.dmp.graph.json.Model model = new de.avgl.dmp.graph.json.Model();

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

				CSVConverterEventRecorder.LOG.error("couldn't persist the converted data of data model '" + dataModel.getId() + "'", e);
			}
		}
	}
}
