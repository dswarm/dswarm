package de.avgl.dmp.persistence.model.internal.rdf.helper;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 
 * @author tgaengler
 *
 */
public class Object {
	
	private final String literalOrURI;
	private final JsonNode jsonNode;
	private final boolean isJsonNode;
	
	public Object(final String literalOrURI) {
		
		this.literalOrURI = literalOrURI;
		this.jsonNode = null;
		isJsonNode = false;
	}
	
	public Object(final JsonNode jsonNode) {
		
		this.jsonNode = jsonNode;
		this.literalOrURI = null;
		isJsonNode = true;
	}
	
	public boolean isJsonNode() {
		
		return isJsonNode;
	}
	
	public String getLiteralOrURI() {
		
		return literalOrURI;
	}
	
	public JsonNode getJsonNode() {
		
		return jsonNode;
	}
}
