package de.avgl.dmp.persistence.model.schema.test;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;

public class NameSpacePrefixRegistry {
	
	public static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String RDA = "http://rdvocab.info/Elements/";
	public static final String FOAF = "http://xmlns.com/foaf/0.1/";
	public static final String DC = "http://purl.org/dc/elements/1.1/";
	public static final String DCTERMS = "http://purl.org/dc/terms/";
	public static final String BIBO = "http://purl.org/ontology/bibo/";
	
	private Map<String, String>						namespaces		= Maps.newLinkedHashMap();
	private Map<String, String>						prefixes		= Maps.newLinkedHashMap();


	public NameSpacePrefixRegistry() {
		
		super();
		
		registerNameSpace("rdf", RDF);
		registerNameSpace("rda", RDA);
		registerNameSpace("foaf", FOAF);
		registerNameSpace("dc", DC);
		registerNameSpace("dcterms", DCTERMS);
		registerNameSpace("bibo", BIBO);
	}

	public void registerNameSpace(String prefix, String uri) {
		namespaces.put(prefix, uri);
		prefixes.put(uri, prefix);
	}
	
	public String getPrefix(String uri) {
		return prefixes.get(uri);
	}
	
	public String getNamespace(String prefix) {
		return namespaces.get(prefix);
	}
	
	public String toString(){
		
		String s = "Namespaces overview: " + System.lineSeparator();
		
		Set<Entry<String, String>> entrySet =  prefixes.entrySet();
		
		for (Iterator<Entry<String, String>> iterator = entrySet.iterator(); iterator.hasNext();) {
			Entry<String, String> entry = (Entry<String, String>) iterator
					.next();
			s += entry.getKey() + " : " +  entry.getValue();
		}
		
		return s;
	}
	

}
