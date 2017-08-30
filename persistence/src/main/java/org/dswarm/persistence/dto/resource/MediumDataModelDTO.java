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
package org.dswarm.persistence.dto.resource;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.dswarm.persistence.dto.BasicDMPDTO;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.schema.Schema;

public final class MediumDataModelDTO extends BasicDMPDTO<MediumDataModelDTO> {

	private final Resource resource;

	private final Configuration configuration;

	private final Schema schema;

	@JsonCreator
	public MediumDataModelDTO(
			@JsonProperty("uuid")
			final String uuid,
			@JsonProperty("name")
			final String name,
			@JsonProperty("description")
			final String description,
			@JsonProperty("_href")
			final String href,
			@JsonProperty("data_resource")
			final Resource resource,
			@JsonProperty("configuration")
			final Configuration configuration,
			@JsonProperty("schema")
			final Schema schema) {
		super(uuid, name, description, href);
		this.resource = resource;
		this.configuration = configuration;
		this.schema = schema;
	}

	@JsonProperty("data_resource")
	public Resource getDataResource() {
		return resource;
	}

	@JsonProperty("configuration")
	public Configuration getConfiguration() {
		return configuration;
	}

	@JsonProperty("schema")
	public Schema getSchema() {
		return schema;
	}

	@Override
	protected MediumDataModelDTO copyWithHref(final String objectURI) {
		return new MediumDataModelDTO(uuid, name, description, objectURI, resource, configuration, schema);
	}

	public static MediumDataModelDTO of(final DataModel dataModel, final URI objectURI) {
		final String uriString = objectURI == null ? null : objectURI.toString();
		final Schema shortSchema = dataModel.getSchema() == null ? null : new Schema(dataModel.getSchema().getUuid());

		if (shortSchema != null) {

			shortSchema.setName(dataModel.getSchema().getName());
		}

		return new MediumDataModelDTO(
				dataModel.getUuid(),
				dataModel.getName(),
				dataModel.getDescription(),
				uriString,
				dataModel.getDataResource(),
				dataModel.getConfiguration(),
				shortSchema);
	}
}
