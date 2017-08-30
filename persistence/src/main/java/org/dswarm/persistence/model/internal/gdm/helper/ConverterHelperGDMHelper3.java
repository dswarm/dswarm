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

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import org.dswarm.graph.json.LiteralNode;
import org.dswarm.graph.json.Node;
import org.dswarm.graph.json.ResourceNode;

/**
 * A helper class with static methods for the converter helper.
 *
 * @author tgaengler
 */
public final class ConverterHelperGDMHelper3 {

	/**
	 * Adds a literal to the converter helper of the given property.
	 *
	 * @param converterHelpers a map of converter helpers
	 * @param property         the property, where the literal belongs to
	 * @param gdmNode          the node to contains the literal
	 * @return the map of converter helpers
	 */
	public static Map<String, ConverterHelper3> addLiteralToConverterHelper(final Map<String, ConverterHelper3> converterHelpers,
	                                                                        final String property,
	                                                                        final Node gdmNode,
	                                                                        final boolean isArrayAttributePath) {

		getOrCreateConverterHelper(converterHelpers, property, isArrayAttributePath).addLiteralOrURI(((LiteralNode) gdmNode).getValue());

		return converterHelpers;
	}

	/**
	 * Adds a JSON node to the converter helper of the given property.
	 *
	 * @param converterHelpers a map of converter helpers
	 * @param property         the property, where the JSON node belongs to
	 * @param jsonNode         the JSON node
	 * @return the map of converter helpers
	 */
	public static Map<String, ConverterHelper3> addJSONNodeToConverterHelper(final Map<String, ConverterHelper3> converterHelpers,
	                                                                         final String property,
	                                                                         final JsonNode jsonNode,
	                                                                         final boolean isArrayAttributePath) {

		getOrCreateConverterHelper(converterHelpers, property, isArrayAttributePath).addJsonNode(jsonNode);

		return converterHelpers;
	}

	/**
	 * Adds a URI to the converter helper of the give property.
	 *
	 * @param converterHelpers a map of converter helpers
	 * @param property         the property, where the URI belongs to
	 * @param gdmNode          the node that contains the URI
	 * @return the map of converter helpers
	 */
	public static Map<String, ConverterHelper3> addURIResourceToConverterHelper(final Map<String, ConverterHelper3> converterHelpers,
	                                                                            final String property,
	                                                                            final Node gdmNode,
	                                                                            final boolean isArrayAttributePath) {

		getOrCreateConverterHelper(converterHelpers, property, isArrayAttributePath).addLiteralOrURI(((ResourceNode) gdmNode).getUri());

		return converterHelpers;
	}

	/**
	 * Checks the map of converter helpers, whether it contains a converter helper entry for the given property. If the converter
	 * helpers map doesn't contain an entry for this property, it will be added.
	 *
	 * @param converterHelpers a map of converter helpers
	 * @param property         a property
	 * @return the map of converter helpers
	 */
	private static ConverterHelper3 getOrCreateConverterHelper(final Map<String, ConverterHelper3> converterHelpers,
	                                                           final String property,
	                                                           final boolean isArrayAttributePath) {

		return converterHelpers.computeIfAbsent(property, property1 -> new ConverterHelper3(property, isArrayAttributePath));
	}
}

