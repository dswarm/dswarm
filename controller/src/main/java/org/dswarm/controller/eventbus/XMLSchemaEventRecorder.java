package org.dswarm.controller.eventbus;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.service.schema.SchemaService;

@Singleton
public class XMLSchemaEventRecorder {

	private final SchemaService	schemaService;

	@Inject
	public XMLSchemaEventRecorder(final SchemaService schemaService/* , final EventBus eventBus */) {

		this.schemaService = schemaService;

		// eventBus.register(this);
	}

	// @Subscribe
	public void convertConfiguration(final XMLSchemaEvent event) {
		final Configuration configuration = event.getConfiguration();
		final Resource resource = event.getResource();

		final String filename = resource.getAttribute("path").asText();

		// TODO: fixme, if needed
	}
}
