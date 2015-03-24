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
package org.dswarm.persistence.dto.resource;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.dswarm.persistence.dto.BasicDMPDTO;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;

public final class MediumDataModelDTO extends BasicDMPDTO<MediumDataModelDTO> {

	private final Resource resource;

	private final Configuration configuration;

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
			final Configuration configuration) {
		super(uuid, name, description, href);
		this.resource = resource;
		this.configuration = configuration;
	}

	public Resource getDataResource() {
		return resource;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	protected MediumDataModelDTO copyWithHref(final String objectURI) {
		return new MediumDataModelDTO(uuid, name, description, objectURI, resource, configuration);
	}

	public static MediumDataModelDTO of(final DataModel dataModel, final URI objectURI) {
		final String uriString = objectURI == null ? null : objectURI.toString();
		return new MediumDataModelDTO(
				dataModel.getUuid(),
				dataModel.getName(),
				dataModel.getDescription(),
				uriString,
				dataModel.getDataResource(),
				dataModel.getConfiguration());
	}
}
