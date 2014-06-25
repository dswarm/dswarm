package org.dswarm.controller.eventbus;

import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;

public class XMLSchemaEvent {

	private final Configuration	configuration;
	private final Resource		resource;

	public XMLSchemaEvent(final Configuration configuration, final Resource resource) {
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
