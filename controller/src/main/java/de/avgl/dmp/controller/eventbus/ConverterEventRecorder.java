package de.avgl.dmp.controller.eventbus;

import com.google.common.eventbus.Subscribe;
import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.CSVSourceResourceTriplesFlow;
import de.avgl.dmp.persistence.model.internal.InternalMemoryDb;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;

import java.io.IOException;
import java.util.List;

public class ConverterEventRecorder {

	final InternalMemoryDb db;

	public ConverterEventRecorder(final InternalMemoryDb db) {

		this.db = db;
	}

	@Subscribe public void convertConfiguration(ConverterEvent event) {
		final Configuration configuration = event.getConfiguration();
		final Resource resource = event.getResource();

		List<org.culturegraph.mf.types.Triple> result = null;
		try {
			final CSVSourceResourceTriplesFlow flow = CSVSourceResourceTriplesFlow.fromConfiguration(configuration);

			final String path = resource.getAttribute("path").asText();
			result = flow.applyFile(path);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (DMPConverterException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		if (result != null) {
			for (org.culturegraph.mf.types.Triple triple : result) {
				db.put(resource.getId(), configuration.getId(), triple.getSubject(), triple.getPredicate(), triple.getObject());
			}
		}
	}
}
