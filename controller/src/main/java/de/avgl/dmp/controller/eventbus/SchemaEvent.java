package de.avgl.dmp.controller.eventbus;

import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;

public class SchemaEvent {

	public static enum SchemaType {
		CSV, XML, XSD;

		public static SchemaType fromString(final String type) {
			if ("schema".equals(type)) {
				return XSD;
			}
			if ("csv".equals(type)) {
				return CSV;
			}
			if ("xml".equals(type)) {
				return XSD;
			}
			throw new IllegalArgumentException("No schema type for [" + type + "]");
		}
	}


	private final Configuration configuration;
	private final SchemaType schemaType;
	private final Resource resource;

	public SchemaEvent(final Resource resource, final Configuration configuration, final SchemaType schemaType) {

		this.resource = resource;
		this.configuration = configuration;
		this.schemaType = schemaType;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public Resource getResource() {
		return resource;
	}

	public SchemaType getSchemaType() {
		return schemaType;
	}
}
