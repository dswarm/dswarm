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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class BasicDMPDTO<SELF extends BasicDMPDTO<SELF>> {
	@JsonProperty("uuid")
	public final String uuid;

	@JsonProperty("name")
	public final String name;

	@JsonProperty("description")
	public final String description;

	@JsonProperty("_href")
	public final String href;

	protected BasicDMPDTO(
			final String uuid,
			final String name,
			final String description,
			final String href) {
		this.uuid = uuid;
		this.name = name;
		this.description = description;
		this.href = href;
	}

	public final SELF withHref(final URI objectURI) {
		final String uriString = objectURI == null ? null : objectURI.toString();
		return copyWithHref(uriString);
	}

	@Override
	public final String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	protected abstract SELF copyWithHref(final String objectURI);
}
