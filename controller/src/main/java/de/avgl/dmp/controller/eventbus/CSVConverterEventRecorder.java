package de.avgl.dmp.controller.eventbus;

import java.util.List;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.CSVResourceFlowFactory;
import de.avgl.dmp.converter.flow.CSVSourceResourceTriplesFlow;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.services.InternalService;

@Singleton
public class CSVConverterEventRecorder {

	private final InternalService<Model> internalService;

	@Inject
	public CSVConverterEventRecorder(final InternalService<Model> internalService, final EventBus eventBus) {

		this.internalService = internalService;
		eventBus.register(this);
	}

	@Subscribe public void convertConfiguration(final CSVConverterEvent event) {
		final Configuration configuration = event.getConfiguration();
		final Resource resource = event.getResource();

		List<org.culturegraph.mf.types.Triple> result = null;
		try {
			final CSVSourceResourceTriplesFlow flow = CSVResourceFlowFactory.fromConfiguration(configuration, CSVSourceResourceTriplesFlow.class);

			final String path = resource.getAttribute("path").asText();
			result = flow.applyFile(path);

		} catch (DMPConverterException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		if (result != null) {
			for (final org.culturegraph.mf.types.Triple triple : result) {
				internalService.createObject(resource.getId(), configuration.getId(), triple.getSubject(), triple.getPredicate(), triple.getObject());
			}
		}
	}
}
