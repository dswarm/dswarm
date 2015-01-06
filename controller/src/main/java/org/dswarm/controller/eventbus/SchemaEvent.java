/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.controller.eventbus;

import org.dswarm.persistence.model.resource.DataModel;

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
			if ("mabxml".equals(type)) {
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
