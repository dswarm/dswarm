package de.avgl.dmp.controller.eventbus;

import java.util.List;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.XMLSourceResourceTriplesFlow;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.internal.impl.RDFModel;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;

/**
 * An event recorder for converting XML documents.
 * 
 * @author phorn
 * @author tgaengler
 */
@Singleton
public class XMLConverterEventRecorder {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(XMLConverterEventRecorder.class);

	/**
	 * The internal model service factory
	 */
	private final InternalModelServiceFactory		internalServiceFactory;

	/**
	 * Creates a new event recorder for converting XML documents with the given internal model service factory and event bus.
	 * 
	 * @param internalModelServiceFactory an internal model service factory
	 * @param eventBus an event bus, where this event record will be registered
	 */
	@Inject
	public XMLConverterEventRecorder(final InternalModelServiceFactory internalModelServiceFactory, final EventBus eventBus) {

		this.internalServiceFactory = internalModelServiceFactory;
		eventBus.register(this);
	}

	/**
	 * Processes the XML document of the data model of the given event and persists the converted data.
	 * 
	 * @param event an converter event that provides a data model
	 */
	@Subscribe
	public void processDataModel(final XMLConverterEvent event) {

		final DataModel dataModel = event.getDataModel();

		RDFModel result = null;

		try {

			final XMLSourceResourceTriplesFlow flow = new XMLSourceResourceTriplesFlow(dataModel);

			final String path = dataModel.getDataResource().getAttribute("path").asText();
			final List<RDFModel> rdfModels = flow.applyResource(path);

			// write RDF models at once
			final com.hp.hpl.jena.rdf.model.Model model = ModelFactory.createDefaultModel();
			String recordClassUri = null;

			for (final RDFModel rdfModel : rdfModels) {

				if (rdfModel.getModel() != null) {

					model.add(rdfModel.getModel());

					if (recordClassUri == null) {

						recordClassUri = rdfModel.getRecordClassURI();
					}
				}
			}

			result = new RDFModel(model, null, recordClassUri);

		} catch (final DMPConverterException | NullPointerException e) {

			LOG.error("couldn't convert the XML data of data model '" + dataModel.getId() + "'", e);
		}

		if (result != null) {

			try {

				internalServiceFactory.getInternalTripleService().createObject(dataModel.getId(), result);
			} catch (final DMPPersistenceException e) {

				LOG.error("couldn't persist the converted data of data model '" + dataModel.getId() + "'", e);
			}
		}
	}
}
