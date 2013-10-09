package de.avgl.dmp.controller.eventbus;

import java.io.File;
import java.io.IOException;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.xml.sax.SAXException;

import de.avgl.dmp.controller.jsonschema.JsonSchemaParser;
import de.avgl.dmp.persistence.model.jsonschema.JSRoot;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.services.SchemaService;

@Singleton
public class XMLSchemaEventRecorder {

	private final Provider<JsonSchemaParser> schemaParserProvider;
	private final SchemaService schemaService;

	@Inject
	public XMLSchemaEventRecorder(final Provider<JsonSchemaParser> schemaParserProvider, final SchemaService schemaService, final EventBus eventBus) {
		this.schemaParserProvider = schemaParserProvider;
		this.schemaService = schemaService;

		eventBus.register(this);
	}

	@Subscribe public void convertConfiguration(XMLSchemaEvent event) {
		final Configuration configuration = event.getConfiguration();
		final Resource resource = event.getResource();

		final JsonSchemaParser schemaParser = schemaParserProvider.get();

		final String filename = resource.getAttribute("path").asText();

		try {
			schemaParser.parse(new File(filename));
			final JSRoot root = schemaParser.apply(resource.getName());

			schemaService.createObject(resource.getId(), configuration.getId(), root);

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
