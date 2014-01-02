package de.avgl.dmp.persistence.model.internal.rdf.helper;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * A helper class with static methods for the converter helper.
 * 
 * @author tgaengler
 */
public final class ConverterHelperHelper {

	/**
	 * Adds a literal to the converter helper of the given property.
	 * 
	 * @param converterHelpers a map of converter helpers
	 * @param property the property, where the literal belongs to
	 * @param rdfNode the RDF node to contains the literal
	 * @return the map of converter helpers
	 */
	public static Map<String, ConverterHelper> addLiteralToConverterHelper(final Map<String, ConverterHelper> converterHelpers,
			final String property, final RDFNode rdfNode) {

		checkConverterHelpers(converterHelpers, property);

		converterHelpers.get(property).addLiteralOrURI(rdfNode.asLiteral().toString());

		return converterHelpers;
	}

	/**
	 * Adds a JSON node to the converter helper of the given property.
	 * 
	 * @param converterHelpers a map of converter helpers
	 * @param property the property, where the JSON node belongs to
	 * @param jsonNode the JSON node
	 * @return the map of converter helpers
	 */
	public static Map<String, ConverterHelper> addJSONNodeToConverterHelper(final Map<String, ConverterHelper> converterHelpers,
			final String property, final JsonNode jsonNode) {

		checkConverterHelpers(converterHelpers, property);

		converterHelpers.get(property).addJsonNode(jsonNode);

		return converterHelpers;
	}

	/**
	 * Adds a URI to the converter helper of the give property.
	 * 
	 * @param converterHelpers a map of converter helpers
	 * @param property the property, where the URI belongs to
	 * @param rdfNode the RDF node that contains the URI
	 * @return the map of converter helpers
	 */
	public static Map<String, ConverterHelper> addURIResourceToConverterHelper(final Map<String, ConverterHelper> converterHelpers,
			final String property, final RDFNode rdfNode) {

		checkConverterHelpers(converterHelpers, property);

		converterHelpers.get(property).addLiteralOrURI(rdfNode.asResource().getURI());

		return converterHelpers;
	}

	/**
	 * Checks the map of converter helpers, whether it contains a converter helper entry for the given property. If the converter
	 * helpers map doesn't contain an entry for this property, it will be added.
	 * 
	 * @param converterHelpers a map of converter helpers
	 * @param property a property
	 * @return the map of converter helpers
	 */
	private static Map<String, ConverterHelper> checkConverterHelpers(final Map<String, ConverterHelper> converterHelpers, final String property) {

		if (!converterHelpers.containsKey(property)) {

			final ConverterHelper converterHelper = new ConverterHelper(property);
			converterHelpers.put(property, converterHelper);
		}

		return converterHelpers;
	}
}
