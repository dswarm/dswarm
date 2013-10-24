package de.avgl.dmp.controller.eventbus;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.XMLSourceResourceTriplesFlow;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.internal.impl.RDFModel;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.services.InternalService;

@Singleton
public class XMLConverterEventRecorder {

	private final InternalService	internalService;

	@Inject
	public XMLConverterEventRecorder(@Named("Triple") final InternalService internalService, final EventBus eventBus) {

		this.internalService = internalService;
		eventBus.register(this);
	}

	@Subscribe
	public void convertConfiguration(final XMLConverterEvent event) {
		final Configuration configuration = event.getConfiguration();
		final Resource resource = event.getResource();

		RDFModel result = null;
		try {
			final XMLSourceResourceTriplesFlow flow = new XMLSourceResourceTriplesFlow(configuration, resource);

			final String path = resource.getAttribute("path").asText();
			result = flow.applyResource(path);

		} catch (final DMPConverterException e) {
			e.printStackTrace();
		} catch (final NullPointerException e) {
			e.printStackTrace();
		}

		if (result != null) {
				try {
					internalService.createObject(resource.getId(), configuration.getId(), result);
				} catch (DMPPersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
}
