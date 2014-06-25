package org.dswarm.persistence.model.schema;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;

public class NameSpacePrefixRegistry {

	public static final String			RDF			= "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String			RDFS		= "http://www.w3.org/2000/01/rdf-schema#";
	public static final String			RDA			= "http://rdvocab.info/Elements/";
	public static final String			FOAF		= "http://xmlns.com/foaf/0.1/";
	public static final String			DC			= "http://purl.org/dc/elements/1.1/";
	public static final String			DCTERMS		= "http://purl.org/dc/terms/";
	public static final String			BIBO		= "http://purl.org/ontology/bibo/";
	public static final String			ISBD		= "http://iflastandards.info/ns/isbd/elements/";
	public static final String			OWL			= "http://www.w3.org/2002/07/owl#";
	public static final String			UMBEL		= "http://umbel.org/umbel#";
	public static final String			BIBRM		= "http://vocab.ub.uni-leipzig.de/bibrm/";

	private final Map<String, String>	namespaces	= Maps.newLinkedHashMap();
	private final Map<String, String>	prefixes	= Maps.newLinkedHashMap();

	public NameSpacePrefixRegistry() {

		super();

		registerNameSpace("rdf", NameSpacePrefixRegistry.RDF);
		registerNameSpace("rdfs", NameSpacePrefixRegistry.RDFS);
		registerNameSpace("owl", NameSpacePrefixRegistry.OWL);
		registerNameSpace("rda", NameSpacePrefixRegistry.RDA);
		registerNameSpace("foaf", NameSpacePrefixRegistry.FOAF);
		registerNameSpace("dc", NameSpacePrefixRegistry.DC);
		registerNameSpace("dcterms", NameSpacePrefixRegistry.DCTERMS);
		registerNameSpace("bibo", NameSpacePrefixRegistry.BIBO);
		registerNameSpace("isbd", NameSpacePrefixRegistry.ISBD);
		registerNameSpace("umbel", NameSpacePrefixRegistry.UMBEL);
		registerNameSpace("bibrm", NameSpacePrefixRegistry.BIBRM);
	}

	public void registerNameSpace(final String prefix, final String uri) {
		namespaces.put(prefix, uri);
		prefixes.put(uri, prefix);
	}

	public String getPrefix(final String uri) {
		return prefixes.get(uri);
	}

	public String getNamespace(final String prefix) {
		return namespaces.get(prefix);
	}

	@Override
	public String toString() {

		String s = "Namespaces overview: " + System.lineSeparator();

		final Set<Entry<String, String>> entrySet = prefixes.entrySet();

		for (final Entry<String, String> entry : entrySet) {

			s += entry.getKey() + " : " + entry.getValue();
		}

		return s;
	}

}
