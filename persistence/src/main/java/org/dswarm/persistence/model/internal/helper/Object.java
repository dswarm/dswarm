package org.dswarm.persistence.model.internal.helper;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A helper object for RDF to JSON transformation.
 *
 * @author tgaengler
 */
public class Object {

	private final String	literalOrURI;
	private final JsonNode	jsonNode;
	private final boolean	isJsonNode;

	/**
	 * Creates a new helper object with a given literal or URI.
	 *
	 * @param literalOrURI a literal or URI
	 */
	public Object(final String literalOrURI) {

		this.literalOrURI = literalOrURI;
		jsonNode = null;
		isJsonNode = false;
	}

	/**
	 * Creates a new helper object with a given JSON node.
	 *
	 * @param jsonNode a JSON node
	 */
	public Object(final JsonNode jsonNode) {

		this.jsonNode = jsonNode;
		literalOrURI = null;
		isJsonNode = true;
	}

	/**
	 * Returns true, if this helper object wraps a JSON node; otherwise false.
	 *
	 * @return true, if this helper object wraps a JSON node; otherwise false
	 */
	public boolean isJsonNode() {

		return isJsonNode;
	}

	/**
	 * Gets the literal or URI of this helper object.
	 *
	 * @return the literal or URI
	 */
	public String getLiteralOrURI() {

		return literalOrURI;
	}

	/**
	 * Gets the JSON node of this helper object.
	 *
	 * @return the JSON node
	 */
	public JsonNode getJsonNode() {

		return jsonNode;
	}
}
