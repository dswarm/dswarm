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
package org.dswarm.persistence.converter;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tgaengler
 */
@Converter
public class StringOrderedListConverter implements AttributeConverter<Set<String>, byte[]> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(StringOrderedListConverter.class);

	private static final ObjectMapper objectMapper     = new ObjectMapper();
	private static final String       EMPTY_JSON_ARRAY = "[]";

	@Override public byte[] convertToDatabaseColumn(final Set<String> orderedList) {

		if (orderedList == null) {

			return null;
		}

		if (orderedList.isEmpty()) {

			return EMPTY_JSON_ARRAY.getBytes(Charsets.UTF_8);
		}

		final ArrayNode jsonArray = objectMapper.createArrayNode();

		for (final String listEntry : orderedList) {

			jsonArray.add(listEntry);
		}

		try {

			return objectMapper.writeValueAsString(jsonArray).getBytes(Charsets.UTF_8);
		} catch (final JsonProcessingException e) {

			LOG.error("couldn't serialize ordered list/JSON array to string");

			return null;
		}
	}

	@Override public Set<String> convertToEntityAttribute(final byte[] dbData) {

		if (dbData == null) {

			return null;
		}

		final ArrayNode jsonArray;

		try {

			jsonArray = objectMapper.readValue(dbData, ArrayNode.class);
		} catch (final IOException e) {

			LOG.error("couldn't deserialize JSON array string '{}'", dbData);

			return null;
		}

		if (jsonArray == null) {

			LOG.error("deserialized JSON array shouldn't be null (JSON array string was = '{}')", dbData);

			return null;
		}

		final Set<String> orderedList = new LinkedHashSet<>();

		for (final JsonNode jsonNode : jsonArray) {

			final String listEntry = jsonNode.asText();

			orderedList.add(listEntry);
		}

		return orderedList;
	}
}
