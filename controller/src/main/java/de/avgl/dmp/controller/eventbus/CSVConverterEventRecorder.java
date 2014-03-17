package de.avgl.dmp.controller.eventbus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.CSVResourceFlowFactory;
import de.avgl.dmp.converter.flow.CSVSourceResourceTriplesFlow;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.internal.impl.RDFModel;
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

			// convert result to RDF
			final Map<Long, Resource> recordResources = Maps.newHashMap();

			final com.hp.hpl.jena.rdf.model.Model model = ModelFactory.createDefaultModel();

			final String dataResourceBaseSchemaURI = DataModelUtils.determineDataResourceSchemaBaseURI(dataModel);
			final String recordClassURI = dataResourceBaseSchemaURI + "RecordType";

			for (final org.culturegraph.mf.types.Triple triple : result) {

				// 1. create resource uri from subject (line number)
				// 2. create property object from property uri string
				// 3. convert objects (string literals) to string literals (?)

				final Resource subject = mintRecordResource(Long.valueOf(triple.getSubject()), dataModel, recordResources, model, recordClassURI);
				final Property property = ResourceFactory.createProperty(triple.getPredicate());

				model.add(subject, property, triple.getObject());

				// final MemoryDBInputModel mdbim = new MemoryDBInputModel(triple);
				//
				// try {
				//
				// internalServiceFactory.getMemoryDbInternalService().createObject(dataModel.getId(), mdbim);
				// } catch (final DMPPersistenceException e) {
				//
				// e.printStackTrace();
				// }
			}

			final RDFModel rdfModel = new RDFModel(model, null, recordClassURI);

			try {

				internalServiceFactory.getInternalGraphService().createObject(dataModel.getId(), rdfModel);
			} catch (final DMPPersistenceException e) {

				CSVConverterEventRecorder.LOG.error("couldn't persist the converted data of data model '" + dataModel.getId() + "'", e);
			}
		}
	}

	private Resource mintRecordResource(final Long identifier, final DataModel dataModel, final Map<Long, Resource> recordResources,
			final com.hp.hpl.jena.rdf.model.Model model, final String recordClassURI) {

		if (identifier != null) {

			if (recordResources.containsKey(identifier)) {

				return recordResources.get(identifier);
			}
		}

		// mint completely new uri

		final StringBuilder sb = new StringBuilder();

		if (dataModel != null) {

			// create uri from resource id and configuration id and random uuid

			sb.append("http://data.slub-dresden.de/datamodels/").append(dataModel.getId()).append("/records/");
		} else {

			// create uri from random uuid

			sb.append("http://data.slub-dresden.de/records/");
		}

		final String recordURI = sb.append(UUID.randomUUID()).toString();
		final Resource recordResource = ResourceFactory.createResource(recordURI);

		if (identifier != null) {

			recordResources.put(identifier, recordResource);
		}

		// add resource type statement to model
		model.add(recordResource, RDF.type, ResourceFactory.createResource(recordClassURI));

		return recordResource;
	}
}
