/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
