package de.avgl.dmp.controller.eventbus;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.service.schema.SchemaService;

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
