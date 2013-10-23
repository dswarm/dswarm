package de.avgl.dmp.persistence.model.internal.rdf.helper;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class ConverterHelperHelper {

	public static Map<String, ConverterHelper> addLiteralToConverterHelper(final Map<String, ConverterHelper> converterHelpers,
			final String property, final RDFNode rdfNode) {

		checkConverterHelpers(converterHelpers, property);
		
		converterHelpers.get(property).addLiteralOrURI(rdfNode.asLiteral().toString());

		return converterHelpers;
	}
	
	public static Map<String, ConverterHelper> addBNodeToConverterHelper(final Map<String, ConverterHelper> converterHelpers,
			final String property, final JsonNode jsonNode) {

		checkConverterHelpers(converterHelpers, property);
		
		converterHelpers.get(property).addJsonNode(jsonNode);

		return converterHelpers;
	}
	
	public static Map<String, ConverterHelper> addURIResourceToConverterHelper(final Map<String, ConverterHelper> converterHelpers,
			final String property, final RDFNode rdfNode) {

		checkConverterHelpers(converterHelpers, property);
		
		converterHelpers.get(property).addLiteralOrURI(rdfNode.asResource().getURI());

		return converterHelpers;
	}

	private static Map<String, ConverterHelper> checkConverterHelpers(final Map<String, ConverterHelper> converterHelpers, final String property) {

		if (!converterHelpers.containsKey(property)) {

			final ConverterHelper converterHelper = new ConverterHelper(property);
			converterHelpers.put(property, converterHelper);
		}
		
		return converterHelpers;
	}
}
