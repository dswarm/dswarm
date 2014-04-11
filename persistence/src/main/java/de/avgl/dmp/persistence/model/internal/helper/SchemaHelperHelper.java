package de.avgl.dmp.persistence.model.internal.helper;

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
	 * @param rdfNode the RDF node to contains the literal
	 * @return the map of converter helpers
	 */
	public static Map<String, SchemaHelper> addPropertyToSchemaHelpers(final Map<String, SchemaHelper> schemaHelpers,
			final String property) {

		checkSchemaHelpers(schemaHelpers, property);

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
	public static Map<String, SchemaHelper> addJSONNodeToSchemaHelper(final Map<String, SchemaHelper> schemaHelpers,
			final String property, final JsonNode jsonNode) {

		checkSchemaHelpers(schemaHelpers, property);

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
