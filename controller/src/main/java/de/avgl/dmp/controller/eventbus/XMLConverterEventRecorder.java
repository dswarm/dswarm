package de.avgl.dmp.controller.eventbus;

import java.util.List;

import org.junit.Assert;

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
import de.avgl.dmp.persistence.service.InternalServiceFactory;

@Singleton
public class XMLConverterEventRecorder {

	private final InternalServiceFactory	internalServiceFactory;

	@Inject
	public XMLConverterEventRecorder(final InternalServiceFactory internalServiceFactory, final EventBus eventBus) {

		this.internalServiceFactory = internalServiceFactory;
		eventBus.register(this);
	}

	// @Subscribe
	// public void convertConfiguration(final XMLConverterEvent event) {
	// final Configuration configuration = event.getConfiguration();
	// final Resource resource = event.getResource();
	//
	// RDFModel result = null;
	// try {
	// final XMLSourceResourceTriplesFlow flow = new XMLSourceResourceTriplesFlow(configuration, resource);
	//
	// final String path = resource.getAttribute("path").asText();
	// result = flow.applyResource(path);
	//
	// } catch (final DMPConverterException | NullPointerException e) {
	// e.printStackTrace();
	// }
	//
	// if (result != null) {
	// try {
	// internalServiceFactory.getInternalTripleService().createObject(resource.getId(), configuration.getId(), result);
	// } catch (final DMPPersistenceException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }

	@Subscribe
	public void processDataModel(final ConverterEvent event) {

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

				Assert.assertNotNull("the RDF triples of the RDF model shouldn't be null", rdfModel.getModel());

				model.add(rdfModel.getModel());

				if (recordClassUri == null) {

					recordClassUri = rdfModel.getRecordClassURI();
				}
			}

			result = new RDFModel(model, null, recordClassUri);

		} catch (final DMPConverterException | NullPointerException e) {

			e.printStackTrace();
		}

		if (result != null) {
			try {
				internalServiceFactory.getInternalTripleService().createObject(dataModel.getId(), result);
			} catch (final DMPPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
