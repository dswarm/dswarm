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
package org.dswarm.persistence.model.internal;

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import org.dswarm.persistence.model.internal.helper.AttributePathHelper;

/**
 * The model interface for holding and providing data for further processing.
 * 
 * @author tgaengler
 */
public interface Model {

	/**
	 * Serializes the data (i.e. only the data - without record identifier) of the model to JSON.
	 * 
	 * @return a JSON serialisation of the data of the model
	 */
	JsonNode toRawJSON();

	/**
	 * Serializes the data (incl. record identifiers -> record identifiers are the keys of the JSON objects of the JSON array) of
	 * the model to JSON.
	 * 
	 * @return a JSON serialisation of the data of the model
	 */
	JsonNode toGDMCompactJSON();

	JsonNode toGDMSimpleJSON();

	JsonNode toGDMSimpleShortJSON();

	JsonNode toJSON();

	JsonNode getSchema();

	Set<AttributePathHelper> getAttributePaths();

	String getRecordClassURI();

	void setRecordURIs(final Set<String> recordURIs);
}
