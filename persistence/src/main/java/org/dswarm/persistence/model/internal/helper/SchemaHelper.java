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
package org.dswarm.persistence.model.internal.helper;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Lists;

import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * A converter helper for RDF to JSON transformation.
 * 
 * @author tgaengler
 */
public class SchemaHelper {

	/**
	 * The property of this converter helper.
	 */
	protected final String			property;

	/**
	 * The objects of this converter helper.
	 */
	private final List<JsonNode>	objects;

	/**
	 * Creates a new converter helper with the given property.
	 * 
	 * @param property a property
	 */
	public SchemaHelper(final String property) {

		this.property = property;
		objects = Lists.newLinkedList();
	}

	/**
	 * Adds a JSON node to the object list.
	 * 
	 * @param jsonNode a JSON node
	 */
	public void addJsonNode(final JsonNode jsonNode) {

		objects.add(jsonNode);
	}

	/**
	 * Return true, if the object list consists of more than one object; otherwise false.
	 * 
	 * @return true, if the object list consists of more than one object; otherwise false
	 */
	public boolean isArray() {

		return objects.size() > 1;
	}

	/**
	 * Serialises the property + object list to a JSON object.
	 * 
	 * @param json the JSON object that should be filled
	 * @return the filled JSON object
	 */
	public JsonNode build(final ObjectNode json) {

		if (isArray()) {

			final ArrayNode arrayNode = DMPPersistenceUtil.getJSONObjectMapper().createArrayNode();

			for (final JsonNode object : objects) {

				arrayNode.add(object);
			}

			if (arrayNode.size() > 0) {

				json.set(property, arrayNode);
			} else {

				return new TextNode(property);
			}
		} else if (!objects.isEmpty()) {

			final JsonNode object = objects.get(0);

			json.set(property, object);
		} else {

			return new TextNode(property);
		}

		return json;
	}

}
