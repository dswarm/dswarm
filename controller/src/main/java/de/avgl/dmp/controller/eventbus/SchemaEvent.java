package de.avgl.dmp.controller.eventbus;

import de.avgl.dmp.persistence.model.resource.DataModel;

public class SchemaEvent extends DataModelEvent {

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

	private final SchemaType	schemaType;

	public SchemaEvent(final DataModel dataModel, final SchemaType schemaType) {

		super(dataModel);

		this.schemaType = schemaType;
	}

	public SchemaType getSchemaType() {
		return schemaType;
	}
}
