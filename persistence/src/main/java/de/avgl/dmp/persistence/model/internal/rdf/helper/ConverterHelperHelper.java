package de.avgl.dmp.persistence.model.internal.rdf.helper;

import java.util.Map;

import com.hp.hpl.jena.rdf.model.RDFNode;

public class ConverterHelperHelper {

	public static Map<String, ConverterHelper> addLiteralToConverterHelper(final Map<String, ConverterHelper> converterHelpers,
			final String property, final RDFNode rdfNode) {

		if (!converterHelpers.containsKey(property)) {

			final LiteralConverterHelper converterHelper = new LiteralConverterHelper(property);
			converterHelpers.put(property, converterHelper);
		}

		((LiteralConverterHelper) converterHelpers.get(property)).addLiteral(rdfNode.asLiteral().toString());

		return converterHelpers;
	}
}
