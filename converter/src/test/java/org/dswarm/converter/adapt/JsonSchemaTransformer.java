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
package org.dswarm.converter.adapt;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonSchemaTransformer {

	public static final JsonSchemaTransformer INSTANCE = new JsonSchemaTransformer();

	private static final Logger log = LoggerFactory.getLogger(JsonSchemaTransformer.class);

	private final ObjectMapper mapper = new ObjectMapper();

	private static final String keyAttributePaths  = "attribute_paths";
	private static final String keyAttributePath   = "attribute_path";
	private static final String keySchema          = "schema";
	private static final String keyInputDataModel  = "input_data_model";
	private static final String keyOutputDataModel = "output_data_model";
	private static final String keyType            = "type";
	private static final String keyName            = "name";
	private static final String keyId              = "id";

	private static final String valueSchemaAttributePathInstance = "SchemaAttributePathInstance";

	private int generatedId;
	private boolean produceDummyIds = false;

	/**
	 * @param jsonContent The complete json content
	 * @return The rootNode
	 * @throws JsonModelTransformException
	 */
	public JsonNode transformFixAttributePathInstance(final String jsonContent, final boolean produceDummyIds) throws JsonModelTransformException,
			JsonModelAlreadyTransformedException {

		this.produceDummyIds = produceDummyIds;

		try {
			resetGeneratedId();

			final JsonNode nodeRoot = objectifyJsonInput(jsonContent);

			try {
				updateDataModelNode(nodeRoot.get(JsonSchemaTransformer.keyInputDataModel));
				updateDataModelNode(nodeRoot.get(JsonSchemaTransformer.keyOutputDataModel));
			} catch (final JsonModelAlreadyTransformedException e) {
				JsonSchemaTransformer.log.warn(e.getMessage());
				throw e;
			} finally {
				logObjectJSON(nodeRoot);
				resetGeneratedId();
			}

			return nodeRoot;
		} catch (final IOException e) {
			JsonSchemaTransformer.log.error(e.getMessage(), e);
			throw new JsonModelTransformException(e);
		}
	}

	private void resetGeneratedId() {

		if (!produceDummyIds) {
			generatedId = 1;
		} else {

			generatedId = -1;
		}
	}

	private void updateDataModelNode(final JsonNode parent) throws JsonModelAlreadyTransformedException {
		if (parent == null) {
			return;
		}

		final JsonNode nodeSchema = parent.get(JsonSchemaTransformer.keySchema);
		updateSchemaNode(nodeSchema);
	}

	public Optional<JsonNode> updateSchemaNode(final JsonNode nodeSchema) {
		if (nodeSchema == null) {
			return Optional.absent();
		}

		// skip resource which are already transformed
		final JsonNode valueForNodeType = nodeSchema.get(JsonSchemaTransformer.keyType);
		if (valueForNodeType != null) {
			if (valueForNodeType.asText().equals(JsonSchemaTransformer.valueSchemaAttributePathInstance)) {
				throw new JsonModelAlreadyTransformedException("Resource already transformed.");
			}
		}

		final ArrayNode oldAttributePaths = (ArrayNode) nodeSchema.get(JsonSchemaTransformer.keyAttributePaths);
		final ArrayNode newAttributePaths = mapper.createArrayNode();
		for (final JsonNode oldAttributePath : oldAttributePaths) {
			final int id = generateId();
			final JsonNode attributePathInstance = createSchemaAttributePathInstanceNode(id, "sapi_" + id, oldAttributePath);
			newAttributePaths.add(attributePathInstance);
		}
		((ObjectNode) nodeSchema).replace(JsonSchemaTransformer.keyAttributePaths, newAttributePaths);

		return Optional.of(nodeSchema);
	}

	private int generateId() {

		if (!produceDummyIds) {

			return generatedId++;
		} else {

			return generatedId--;
		}
	}

	private JsonNode createSchemaAttributePathInstanceNode(final int id, final String name, final JsonNode attributePath) {
		final ObjectNode node = mapper.createObjectNode();
		node.put(JsonSchemaTransformer.keyType, JsonSchemaTransformer.valueSchemaAttributePathInstance);
		node.put(JsonSchemaTransformer.keyName, name);
		node.put(JsonSchemaTransformer.keyId, id);
		node.set(JsonSchemaTransformer.keyAttributePath, attributePath);
		return node;
	}

	private ObjectNode objectifyJsonInput(final String jsonInput) throws IOException {
		return mapper.readValue(jsonInput, ObjectNode.class);
	}

	private void logObjectJSON(final Object object) {
		try {
			final String json = mapper.writeValueAsString(object);
			JsonSchemaTransformer.log.debug(json);
		} catch (final JsonProcessingException e) {
			JsonSchemaTransformer.log.error("Unable to serialize " + object.getClass().getName() + " to JSON", e);
		}
	}

	public ObjectMapper getMapper() {
		return mapper;
	}
}
