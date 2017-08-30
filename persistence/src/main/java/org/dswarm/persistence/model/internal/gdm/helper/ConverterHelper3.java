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
package org.dswarm.persistence.model.internal.gdm.helper;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.dswarm.graph.json.util.Util;
import org.dswarm.graph.json.util.helper.Object;

/**
 * A converter helper for RDF to JSON transformation.
 *
 * @author tgaengler
 */
public class ConverterHelper3 {

	/**
	 * The property of this converter helper.
	 */
	private final String property;

	/**
	 * indicates, whether the values of this field should be rendered as array or not
	 */
	private final boolean isArray;

	/**
	 * The objects of this converter helper.
	 */
	private final List<Object> objects = new ArrayList<>();

	/**
	 * Creates a new converter helper with the given property.
	 *
	 * @param property a property
	 */
	public ConverterHelper3(final String property, final boolean isArray) {

		this.property = property;
		this.isArray = isArray;
	}

	/**
	 * Adds a literal or URI to the object list.
	 *
	 * @param object a new literal or URI
	 */
	public void addLiteralOrURI(final String object) {

		objects.add(new Object(object));
	}

	/**
	 * Adds a JSON node to the object list.
	 *
	 * @param jsonNode a JSON node
	 */
	public void addJsonNode(final JsonNode jsonNode) {

		objects.add(new Object(jsonNode));
	}

	/**
	 * Return true, if the object list consists of more than one object; otherwise false.
	 *
	 * @return true, if the object list consists of more than one object; otherwise false
	 */
	public boolean isArray() {

		return isArray;
	}

	/**
	 * Serialises the property + object list to a JSON object.
	 *
	 * @param json the JSON object that should be filled
	 * @return the filled JSON object
	 */
	public ObjectNode build(final ObjectNode json) {

		if (isArray()) {

			return buildArray(json);
		}

		final Object singleObject = objects.get(0);

		if (singleObject.isJsonNode()) {

			return buildObject(json, singleObject);
		}

		return buildLiteral(json, singleObject);
	}

	private ObjectNode buildLiteral(final ObjectNode json,
	                                final Object object) {

		json.put(property, object.getLiteralOrURI());

		return json;
	}

	private ObjectNode buildObject(final ObjectNode json,
	                               final Object object) {

		json.set(property, object.getJsonNode());

		return json;
	}

	private ObjectNode buildArray(final ObjectNode json) {

		final ArrayNode arrayNode = Util.getJSONObjectMapper().createArrayNode();

		objects.forEach(object -> {

			if (object.isJsonNode()) {

				arrayNode.add(object.getJsonNode());

				return;
			}

			arrayNode.add(object.getLiteralOrURI());
		});

		json.set(property, arrayNode);

		return json;
	}

}

