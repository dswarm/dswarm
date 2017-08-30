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

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A helper class with static methods for the converter helper.
 * 
 * @author tgaengler
 */
public final class SchemaHelperHelper {

	/**
	 * Adds a literal to the converter helper of the given property.
	 * 
	 * @param schemaHelpers a map of converter helpers
	 * @param property the property, where the literal belongs to
	 * @return the map of converter helpers
	 */
	public static Map<String, SchemaHelper> addPropertyToSchemaHelpers(final Map<String, SchemaHelper> schemaHelpers, final String property) {

		SchemaHelperHelper.checkSchemaHelpers(schemaHelpers, property);

		return schemaHelpers;
	}

	/**
	 * Adds a JSON node to the converter helper of the given property.
	 * 
	 * @param schemaHelpers a map of converter helpers
	 * @param property the property, where the JSON node belongs to
	 * @param jsonNode the JSON node
	 * @return the map of converter helpers
	 */
	public static Map<String, SchemaHelper> addJSONNodeToSchemaHelper(final Map<String, SchemaHelper> schemaHelpers, final String property,
			final JsonNode jsonNode) {

		SchemaHelperHelper.checkSchemaHelpers(schemaHelpers, property);

		schemaHelpers.get(property).addJsonNode(jsonNode);

		return schemaHelpers;
	}

	/**
	 * Checks the map of converter helpers, whether it contains a converter helper entry for the given property. If the converter
	 * helpers map doesn't contain an entry for this property, it will be added.
	 * 
	 * @param schemaHelpers a map of converter helpers
	 * @param property a property
	 * @return the map of converter helpers
	 */
	private static Map<String, SchemaHelper> checkSchemaHelpers(final Map<String, SchemaHelper> schemaHelpers, final String property) {

		if (!schemaHelpers.containsKey(property)) {

			final SchemaHelper schemaHelper = new SchemaHelper(property);
			schemaHelpers.put(property, schemaHelper);
		}

		return schemaHelpers;
	}
}
