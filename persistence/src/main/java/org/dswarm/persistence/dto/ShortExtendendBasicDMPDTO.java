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
package org.dswarm.persistence.dto;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.dswarm.persistence.model.ExtendedBasicDMPJPAObject;

public final class ShortExtendendBasicDMPDTO extends BasicDMPDTO<ShortExtendendBasicDMPDTO> {

	@JsonCreator
	public ShortExtendendBasicDMPDTO(
			@JsonProperty("uuid")
			final String uuid,
			@JsonProperty("name")
			final String name,
			@JsonProperty("description")
			final String description,
			@JsonProperty("_href")
			final String href) {
		super(uuid, name, description, href);
	}

	@Override
	protected ShortExtendendBasicDMPDTO copyWithHref(final String objectURI) {
		return new ShortExtendendBasicDMPDTO(uuid, name, description, objectURI);
	}

	public static ShortExtendendBasicDMPDTO of(final ExtendedBasicDMPJPAObject pojo, final URI objectURI) {
		final String uriString = objectURI == null ? null : objectURI.toString();
		return new ShortExtendendBasicDMPDTO(
				pojo.getUuid(), pojo.getName(), pojo.getDescription(), uriString);
	}
}
