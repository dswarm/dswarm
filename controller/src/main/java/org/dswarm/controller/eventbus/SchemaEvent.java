/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
import org.dswarm.persistence.model.resource.UpdateFormat;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;

public class SchemaEvent extends DataModelEvent {

	public enum SchemaType {
		CSV, XML, XSD, JSON;

		public static SchemaType fromString(final String type) {

			if (type == null) {

				throw new IllegalArgumentException("couldn't determine schema type, because it is null");
			}

			switch (type) {

				case ConfigurationStatics.SCHEMA_STORAGE_TYPE:
				case ConfigurationStatics.XML_STORAGE_TYPE:
				case ConfigurationStatics.MABXML_STORAGE_TYPE:
				case ConfigurationStatics.MARCXML_STORAGE_TYPE:
				case ConfigurationStatics.PNX_STORAGE_TYPE:
				case ConfigurationStatics.OAI_PMH_DC_ELEMENTS_STORAGE_TYPE:
				case ConfigurationStatics.OAI_PMH_DCE_AND_EDM_ELEMENTS_STORAGE_TYPE:
				case ConfigurationStatics.OAIPMH_DC_TERMS_STORAGE_TYPE:
				case ConfigurationStatics.OAIPMH_MARCXML_STORAGE_TYPE:

					return XSD;
				case ConfigurationStatics.CSV_STORAGE_TYPE:

					return CSV;
				case ConfigurationStatics.JSON_STORAGE_TYPE:

					return JSON;
				default:

					throw new IllegalArgumentException("No schema type for [" + type + "]");
			}
		}
	}

	private final SchemaType schemaType;

	public SchemaEvent(final DataModel dataModel, final SchemaType schemaType, final UpdateFormat updateFormat, final boolean enableVersioning) {

		super(dataModel, updateFormat, enableVersioning);

		this.schemaType = schemaType;
	}

	public SchemaType getSchemaType() {
		return schemaType;
	}
}
