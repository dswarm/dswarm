package de.avgl.dmp.controller.eventbus;

import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;

public class CSVConverterEvent {
	private final Configuration configuration;
	private final Resource resource;

	public CSVConverterEvent(final Configuration configuration, final Resource resource) {
		this.configuration = configuration;
		this.resource = resource;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public Resource getResource() {
		return resource;
	}
}
