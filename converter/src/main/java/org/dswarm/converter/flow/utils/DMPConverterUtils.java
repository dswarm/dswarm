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
package org.dswarm.converter.flow.utils;

import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
public class DMPConverterUtils {

	public static JsonNode removeRecordIdFields(final JsonNode jsonNode) {

		if (!jsonNode.isContainerNode()) {

			return jsonNode;
		}

		if (jsonNode.isArray()) {

			final ArrayNode arrayNode = DMPPersistenceUtil.getJSONObjectMapper().createArrayNode();

			for (final JsonNode entry : jsonNode) {

				final JsonNode manipulatedJSON = removeRecordIdFields(entry);
				arrayNode.add(manipulatedJSON);
			}

			return arrayNode;
		}

		final JsonNode cleanedJSON = ((ObjectNode) jsonNode).without(DMPPersistenceUtil.RECORD_ID);

		final Iterator<Map.Entry<String, JsonNode>> iter = cleanedJSON.fields();

		final ObjectNode newJSON = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

		while(iter.hasNext()) {

			final Map.Entry<String, JsonNode> entry = iter.next();

			final JsonNode result = removeRecordIdFields(entry.getValue());

			newJSON.set(entry.getKey(), result);
		}

		return newJSON;
	}
}
