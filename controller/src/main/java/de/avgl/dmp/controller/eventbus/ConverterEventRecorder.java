package de.avgl.dmp.controller.eventbus;

import java.util.List;

import com.google.common.eventbus.Subscribe;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.CSVResourceFlowFactory;
import de.avgl.dmp.converter.flow.CSVSourceResourceTriplesFlow;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.services.InternalService;

public class ConverterEventRecorder {

	private InternalService internalService;

	public ConverterEventRecorder(final InternalService internalService) {

		this.internalService = internalService;
	}

	@Subscribe public void convertConfiguration(ConverterEvent event) {
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
			for (org.culturegraph.mf.types.Triple triple : result) {
				internalService.createObject(resource.getId(), configuration.getId(), triple.getSubject(), triple.getPredicate(), triple.getObject());
			}
		}
	}
}
