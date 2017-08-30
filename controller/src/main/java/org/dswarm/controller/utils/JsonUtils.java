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
package org.dswarm.controller.utils;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tgaengler
 */
public final class JsonUtils {

	private static final Logger LOG = LoggerFactory.getLogger(JsonUtils.class);

	public static Optional<String> getStringValue(final String key, final JsonNode json) {

		if (json == null) {

			LOG.debug("cannot read string value from given JSON, because it does not exist");

			return Optional.empty();
		}

		final JsonNode node = json.get(key);

		if (node == null) {

			return Optional.empty();
		}

		return Optional.ofNullable(node.asText());
	}

	public static Optional<Integer> getIntValue(final String key, final JsonNode json) {

		if (json == null) {

			LOG.debug("cannot read integer value from given JSON, because it does not exist");

			return Optional.empty();
		}

		final JsonNode node = json.get(key);

		if (node == null) {

			return Optional.empty();
		}

		return Optional.ofNullable(node.asInt());
	}

	public static boolean getBooleanValue(final String key, final JsonNode json, final boolean defaultValue) {

		if (json == null) {

			LOG.debug("cannot read boolean value from given JSON, because it does not exist. will take default value '{}' instead.", defaultValue);

			return defaultValue;
		}

		final JsonNode node = json.get(key);

		if (node != null) {

			final boolean value = node.asBoolean();

			LOG.debug("{} = {}", key, value);

			return value;
		} else {

			LOG.debug("{} = {} (default value)", key, defaultValue);

			return defaultValue;
		}
	}

	public static Optional<Set<String>> getStringSetValue(final String key, final JsonNode json) {

		if (json == null) {

			LOG.debug("cannot read string set value from given JSON, because it does not exist");

			return Optional.empty();
		}

		final JsonNode node = json.get(key);

		if (node == null) {

			return Optional.empty();
		}

		final Optional<Set<String>> optionalValue;

		final Set<String> set = new LinkedHashSet<>();

		for (final JsonNode entryNode : node) {

			final String entry = entryNode.asText();

			set.add(entry);
		}

		optionalValue = Optional.of(set);

		return optionalValue;
	}
}
