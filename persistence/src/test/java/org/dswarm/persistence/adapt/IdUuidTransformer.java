/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.adapt;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.dswarm.persistence.test.DMPPersistenceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 *
 */
public class IdUuidTransformer {

	private static final ObjectMapper mapper = new ObjectMapper();

	private static final String ID_FIELD_NAME   = "id";
	private static final String UUID_FIELD_NAME = "uuid";

	public static JsonNode transformIdToUuidInJsonString(final String jsonString) throws IOException {

		final ObjectNode json = mapper.readValue(jsonString, ObjectNode.class);

		final List<JsonNode> matches = json.findParents(ID_FIELD_NAME);

		for (final JsonNode match : matches) {

			final long id = match.get(ID_FIELD_NAME).asLong();

			((ObjectNode) match).remove(ID_FIELD_NAME);
			((ObjectNode) match).put(UUID_FIELD_NAME, "" + id);
		}

		return json;
	}

	public static void transformIdToUuidInJsonFile(final String fileName, final String rootPath) throws IOException, URISyntaxException,
			JsonModelExportException {

		final String jsonString = DMPPersistenceUtil.getResourceAsString(fileName);
		final JsonNode transformedJSON = transformIdToUuidInJsonString(jsonString);

		final URI fileURI = DMPPersistenceTestUtils.getResourceURI(fileName, rootPath);

		DMPPersistenceTestUtils.writeToFile(transformedJSON, fileURI);
	}

	public static void transformIdToUuidInJsonFile(final URI fileURI) throws IOException, URISyntaxException, JsonModelExportException {

		final String jsonString = DMPPersistenceTestUtils.readResource(fileURI);
		final JsonNode transformedJSON = transformIdToUuidInJsonString(jsonString);

		DMPPersistenceTestUtils.writeToFile(transformedJSON, fileURI);
	}
}
