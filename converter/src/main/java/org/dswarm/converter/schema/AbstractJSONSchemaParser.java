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
package org.dswarm.converter.schema;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Provider;
import javaslang.Tuple;
import javaslang.Tuple2;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.helper.AttributePathHelper;
import org.dswarm.persistence.model.internal.helper.AttributePathHelperHelper;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.dswarm.persistence.service.schema.SchemaService;

/**
 * @author tgaengler
 */
public abstract class AbstractJSONSchemaParser {

	private static final String DEFAULT_RECORD_CLASS_IDENTIFIER = "Record";
	private static final String RECORD_CLASS_POSTFIX = "Type";
	private final Provider<SchemaService> schemaServiceProvider;

	private final Provider<ClaszService> classServiceProvider;

	private final Provider<AttributePathService> attributePathServiceProvider;

	private final Provider<SchemaAttributePathInstanceService> schemaAttributePathInstanceServiceProvider;

	private final Provider<AttributeService> attributeServiceProvider;

	protected final Provider<ObjectMapper> objectMapperProvider;

	protected static final String ROOT_NODE_IDENTIFIER = "__ROOT_NODE__";
	private static final String UNKNOWN_JSON_SCHEMA_ATTRIBUTE_TYPE = "__UNKNOWN__";
	static final String STRING_JSON_SCHEMA_ATTRIBUTE_TYPE = "string";
	static final String OBJECT_JSON_SCHEMA_ATTRIBUTE_TYPE = "object";
	private static final String ARRAY_JSON_SCHEMA_ATTRIBUTE_TYPE = "array";

	private static final String JSON_SCHEMA_PROPERTIES_IDENTIFIER = "properties";
	private static final String JSON_SCHEMA_ITEMS_IDENTIFIER = "items";
	private static final String JSON_SCHEMA_TYPE_IDENTIFIER = "type";
	private static final String JSON_SCHEMA_ENUM_IDENTIFIER = "enum";
	private static final String JSON_SCHEMA_PATTERN_IDENTIFIER = "pattern";
	static final String JSON_SCHEMA_MIXED_IDENTIFIER = "mixed";
	private static final String JSON_SCHEMA_TITLE_IDENTIFIER = "title";

	private boolean includeRecordTag = false;

	public AbstractJSONSchemaParser(final Provider<SchemaService> schemaServiceProviderArg,
	                                final Provider<ClaszService> classServiceProviderArg,
	                                final Provider<AttributePathService> attributePathServiceProviderArg,
	                                final Provider<SchemaAttributePathInstanceService> schemaAttributePathInstanceServiceProviderArg,
	                                final Provider<AttributeService> attributeServiceProviderArg,
	                                final Provider<ObjectMapper> objectMapperProviderArg) {

		schemaServiceProvider = schemaServiceProviderArg;
		classServiceProvider = classServiceProviderArg;
		attributePathServiceProvider = attributePathServiceProviderArg;
		schemaAttributePathInstanceServiceProvider = schemaAttributePathInstanceServiceProviderArg;
		attributeServiceProvider = attributeServiceProviderArg;
		objectMapperProvider = objectMapperProviderArg;
	}

	public void setIncludeRecordTag(final boolean includeRecordTagArg) {

		includeRecordTag = includeRecordTagArg;
	}

	public Optional<Schema> parse(final String schemaFilePath,
	                              final String recordTag,
	                              final String uuid,
	                              final String schemaName,
	                              final String baseURI) throws DMPPersistenceException {

		return parse(schemaFilePath, recordTag, uuid, schemaName, baseURI, Optional.empty());
	}

	public Optional<Schema> parse(final String schemaFilePath,
	                              final String recordTag,
	                              final String uuid,
	                              final String schemaName,
	                              final String baseURI,
	                              final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs) throws DMPPersistenceException {

		return parse(schemaFilePath, recordTag, uuid, schemaName, baseURI, optionalAttributePathsSAPIUUIDs, Optional.empty());
	}

	public Optional<Schema> parse(final String schemaFilePath,
	                              final String recordTag,
	                              final String uuid,
	                              final String schemaName,
	                              final String baseURI,
	                              final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs,
	                              final Optional<Set<String>> optionalExcludeAttributePathStubs) throws DMPPersistenceException {

		final Optional<Tuple2<Schema, Set<AttributePathHelper>>> optionalResult = parseSeparatelyInternal(schemaFilePath, recordTag, uuid,
				schemaName, baseURI);

		if (!optionalResult.isPresent()) {

			return Optional.empty();
		}

		final Schema schema = optionalResult.get()._1;
		final Set<AttributePathHelper> attributePaths = optionalResult.get()._2;

		SchemaUtils.addAttributePaths(schema, attributePaths, attributePathServiceProvider, schemaAttributePathInstanceServiceProvider,
				attributeServiceProvider, optionalAttributePathsSAPIUUIDs, optionalExcludeAttributePathStubs);

		final Schema updatedSchema = SchemaUtils.updateSchema(schema, schemaServiceProvider);

		return Optional.ofNullable(updatedSchema);
	}

	public Optional<Tuple2<Schema, Map<String, AttributePathHelper>>> parseSeparately(final String schemaFilePath,
	                                                                                  final String recordTag,
	                                                                                  final String uuid,
	                                                                                  final String schemaName,
	                                                                                  final String baseURI) throws DMPPersistenceException {

		final Optional<Tuple2<Schema, Set<AttributePathHelper>>> optionalResult = parseSeparatelyInternal(schemaFilePath, recordTag, uuid,
				schemaName, baseURI);

		if (!optionalResult.isPresent()) {

			return Optional.empty();
		}

		final Schema schema = optionalResult.get()._1;
		final Set<AttributePathHelper> attributePaths = optionalResult.get()._2;
		final Optional<Map<String, AttributePathHelper>> optionalAttributePathsMap = convertSetToMap(Optional.of(attributePaths));

		return Optional.of(Tuple.of(schema, optionalAttributePathsMap.get()));
	}

	/**
	 * i.e. the attribute paths are not added to the schema yet
	 *
	 * @param schemaFilePath
	 * @param recordTag
	 * @param uuid
	 * @param schemaName
	 * @return
	 * @throws org.dswarm.persistence.DMPPersistenceException
	 */
	private Optional<Tuple2<Schema, Set<AttributePathHelper>>> parseSeparatelyInternal(final String schemaFilePath,
	                                                                                   final String recordTag,
	                                                                                   final String uuid,
	                                                                                   final String schemaName,
	                                                                                   final String baseURI)
			throws DMPPersistenceException {

		final Optional<List<JsonNode>> optionalRecordTags = getRecordTagNodes(schemaFilePath, recordTag);

		if (!optionalRecordTags.isPresent()) {

			return Optional.empty();
		}

		final List<JsonNode> recordTagNodes = optionalRecordTags.get();

		final Optional<Schema> optionalSchema = createSchema(uuid, schemaName, baseURI);

		if (!optionalSchema.isPresent()) {

			return Optional.empty();
		}

		final Schema schema = optionalSchema.get();

		addRecordClass(recordTagNodes, schema);

		final Set<AttributePathHelper> attributePaths = parseAttributePaths(recordTagNodes, Optional.ofNullable(schema.getUuid()), Optional.ofNullable(baseURI));

		return Optional.of(Tuple.of(schema, attributePaths));
	}

	public Optional<Set<AttributePathHelper>> parseAttributePaths(final String schemaFilePath,
	                                                              final Optional<String> optionalRecordTag) {

		if (optionalRecordTag.isPresent()) {

			return parseAttributePaths(schemaFilePath, optionalRecordTag.get());
		} else {

			return parseAttributePaths(schemaFilePath);
		}
	}

	public Optional<Map<String, AttributePathHelper>> parseAttributePathsMap(final String schemaFilePath,
	                                                                         final Optional<String> optionalRecordTag) {

		// TODO: delegate to upper (?)
		final Optional<String> optionalSchemaUUID = Optional.empty();
		final Optional<String> optionalBaseURI = Optional.empty();

		return parseAttributePathsMap(schemaFilePath, optionalRecordTag, optionalSchemaUUID, optionalBaseURI, Optional.empty());
	}

	public Optional<Map<String, AttributePathHelper>> parseAttributePathsMap(final String schemaFilePath,
	                                                                         final Optional<String> optionalRecordTag,
	                                                                         final Optional<String> optionalSchemaUUID,
	                                                                         final Optional<String> optionalBaseURI,
	                                                                         final Optional<Set<String>> optionalExcludeAttributePathStubs) {

		if (optionalRecordTag.isPresent()) {

			return convertSetToMap(parseAttributePaths(schemaFilePath, optionalRecordTag.get(), optionalSchemaUUID, optionalBaseURI, optionalExcludeAttributePathStubs));
		} else {

			return convertSetToMap(parseAttributePaths2(schemaFilePath, optionalSchemaUUID, optionalBaseURI, optionalExcludeAttributePathStubs));
		}
	}

	public Optional<Set<AttributePathHelper>> parseAttributePaths(final String schemaFilePath,
	                                                              final String recordTag) {

		// TODO: delegate to upper (?)
		final Optional<String> optionalSchemaUUID = Optional.empty();
		final Optional<String> optionalBaseURI = Optional.empty();

		return parseAttributePaths(schemaFilePath, recordTag, optionalSchemaUUID, optionalBaseURI, Optional.empty());
	}

	public Optional<Set<AttributePathHelper>> parseAttributePaths(final String schemaFilePath,
	                                                              final String recordTag,
	                                                              final Optional<String> optionalSchemaUUID,
	                                                              final Optional<String> optionalBaseURI,
	                                                              final Optional<Set<String>> optionalExcludeAttributePathStubs) {

		final Optional<List<JsonNode>> optionalRecordTags = getRecordTagNodes(schemaFilePath, recordTag);

		return parseAttributePaths(optionalRecordTags, optionalSchemaUUID, optionalBaseURI, optionalExcludeAttributePathStubs);
	}

	public Optional<Set<AttributePathHelper>> parseAttributePaths(final String schemaFilePath) {

		final Optional<String> optionalSchemaUUID = Optional.empty();
		final Optional<String> optionalBaseURI = Optional.empty();

		return parseAttributePaths2(schemaFilePath, optionalSchemaUUID, optionalBaseURI, Optional.empty());
	}

	public Optional<Set<AttributePathHelper>> parseAttributePaths2(final String schemaFilePath,
	                                                               final Optional<String> optionalSchemaUUID,
	                                                               final Optional<String> optionalBaseURI,
	                                                               final Optional<Set<String>> optionalExcludeAttributePathStubs) {

		final Optional<ObjectNode> optionalJSONSchema = getJSONSchema(schemaFilePath);

		if (!optionalJSONSchema.isPresent()) {

			return Optional.empty();
		}

		final List<JsonNode> rootNodes = Lists.newCopyOnWriteArrayList();

		// prepare root node
		final ObjectNode jsonSchema = optionalJSONSchema.get();

		final Optional<JsonNode> optionalTitleNode = Optional.ofNullable(jsonSchema.get(AbstractJSONSchemaParser.JSON_SCHEMA_TITLE_IDENTIFIER));

		final String rootNodeIdentifier;

		if (optionalTitleNode.isPresent()) {

			rootNodeIdentifier = optionalTitleNode.get().asText();
			jsonSchema.remove(AbstractJSONSchemaParser.JSON_SCHEMA_TITLE_IDENTIFIER);
		} else {

			rootNodeIdentifier = AbstractJSONSchemaParser.ROOT_NODE_IDENTIFIER;
		}

		final ObjectNode rootNode = objectMapperProvider.get().createObjectNode();
		rootNode.set(rootNodeIdentifier, jsonSchema);

		rootNodes.add(rootNode);

		final Optional<List<JsonNode>> optionalRootNodes = Optional.of(rootNodes);

		return parseAttributePaths(optionalRootNodes, optionalSchemaUUID, optionalBaseURI, optionalExcludeAttributePathStubs);
	}

	private Optional<Set<AttributePathHelper>> parseAttributePaths(final Optional<List<JsonNode>> optionalRecordTags) {

		final Optional<String> optionalSchemaUUID = Optional.empty();
		final Optional<String> optionalBaseURI = Optional.empty();

		return parseAttributePaths(optionalRecordTags, optionalSchemaUUID, optionalBaseURI, Optional.empty());
	}

	private Optional<Set<AttributePathHelper>> parseAttributePaths(final Optional<List<JsonNode>> optionalRecordTags,
	                                                               final Optional<String> optionalSchemaUUID,
	                                                               final Optional<String> optionalBaseURI,
	                                                               final Optional<Set<String>> optionalExcludeAttributePathStubs) {

		if (!optionalRecordTags.isPresent()) {

			return Optional.empty();
		}

		final List<JsonNode> recordTagNodes = optionalRecordTags.get();

		final Set<AttributePathHelper> attributePaths = parseAttributePaths(recordTagNodes, optionalSchemaUUID, optionalBaseURI, optionalExcludeAttributePathStubs);

		return Optional.ofNullable(attributePaths);
	}

	private Set<AttributePathHelper> parseAttributePaths(final List<JsonNode> recordTagNodes,
	                                                     final Optional<String> optionalSchemaUUID,
	                                                     final Optional<String> optionalBaseURI) {

		return parseAttributePaths(recordTagNodes, optionalSchemaUUID, optionalBaseURI, Optional.empty());
	}

	private Set<AttributePathHelper> parseAttributePaths(final List<JsonNode> recordTagNodes,
	                                                     final Optional<String> optionalSchemaUUID,
	                                                     final Optional<String> optionalBaseURI,
	                                                     final Optional<Set<String>> optionalExcludeAttributePathStubs) {

		final Set<AttributePathHelper> attributePaths = Sets.newCopyOnWriteArraySet();

		// attribute path retrieval from all records
		for (final JsonNode recordTagNode : recordTagNodes) {

			Set<AttributePathHelper> recordTagNodeAttributePaths = Sets.newLinkedHashSet();

			recordTagNodeAttributePaths = determineAttributePaths(recordTagNode, optionalSchemaUUID, optionalBaseURI, recordTagNodeAttributePaths, new AttributePathHelper(), includeRecordTag);

			if (recordTagNodeAttributePaths != null && !recordTagNodeAttributePaths.isEmpty()) {

				final Set<AttributePathHelper> finalRecordTagNodeAttributePaths;

				if (optionalExcludeAttributePathStubs.isPresent()) {

					final Set<String> excludeAttributePathStubs = optionalExcludeAttributePathStubs.get();

					finalRecordTagNodeAttributePaths = new LinkedHashSet<>();

					for (final AttributePathHelper recordTagNodeAttributePath : recordTagNodeAttributePaths) {

						final String recordTagNodeAttributePathString = recordTagNodeAttributePath.toString();

						boolean excludeRecordTagNodeAttributePath = false;

						for (final String excludeAttributePathStub : excludeAttributePathStubs) {

							if (recordTagNodeAttributePathString.startsWith(excludeAttributePathStub)) {

								excludeRecordTagNodeAttributePath = true;

								break;
							}
						}

						// if excludeRecordTagNodeAttributePath == true, then this attribute path will be excluded
						if (!excludeRecordTagNodeAttributePath) {

							finalRecordTagNodeAttributePaths.add(recordTagNodeAttributePath);
						}
					}
				} else {

					finalRecordTagNodeAttributePaths = recordTagNodeAttributePaths;
				}

				attributePaths.addAll(finalRecordTagNodeAttributePaths);
			}
		}

		return attributePaths;
	}

	private Optional<List<JsonNode>> getRecordTagNodes(final String schemaFilePath,
	                                                   final String recordTag) {

		final Optional<ObjectNode> jsonSchemaOptional = getJSONSchema(schemaFilePath);

		if (!jsonSchemaOptional.isPresent()) {

			return Optional.empty();
		}

		final List<JsonNode> recordTagNodes = getRecordTagNodes(jsonSchemaOptional.get(), recordTag);

		return Optional.ofNullable(recordTagNodes);
	}

	private List<JsonNode> getRecordTagNodes(final ObjectNode jsonSchema, final String recordTag) {

		final List<JsonNode> recordTagNodes = Lists.newCopyOnWriteArrayList();

		getRecordTagNodes(jsonSchema, recordTag, recordTagNodes);

		return recordTagNodes;
	}

	private void getRecordTagNodes(final JsonNode currentJSONSchemaNode,
	                               final String recordTag,
	                               final List<JsonNode> recordTagNodes) {

		final Optional<JsonNode> optionalCurrentJSONSchemaNode = determineCurrentJSONSchemaNode(currentJSONSchemaNode);

		if (!optionalCurrentJSONSchemaNode.isPresent()) {

			return;
		}

		final JsonNode newCurrentJSONSchemaNode = optionalCurrentJSONSchemaNode.get();

		if (recordTag == null) {

			// take everything from the root on (since no record tag was given)

			final ObjectNode rootNode = objectMapperProvider.get().createObjectNode();
			rootNode.set(AbstractJSONSchemaParser.ROOT_NODE_IDENTIFIER, currentJSONSchemaNode);

			recordTagNodes.add(rootNode);

			return;
		}

		final Iterator<Map.Entry<String, JsonNode>> iter = newCurrentJSONSchemaNode.fields();

		while (iter.hasNext()) {

			final Map.Entry<String, JsonNode> currentJSONSchemaNodeEntry = iter.next();

			if (currentJSONSchemaNodeEntry.getKey().endsWith(recordTag)) {

				final JsonNode currentJSONSchemaNodeEntryValue = currentJSONSchemaNodeEntry.getValue();
				final String type = determineJSONSchemaNodeType(currentJSONSchemaNodeEntryValue);

				// to go deeper, when it's an array
				if (!type.equals(AbstractJSONSchemaParser.ARRAY_JSON_SCHEMA_ATTRIBUTE_TYPE)) {

					final ObjectNode recordTagNode = objectMapperProvider.get().createObjectNode();
					recordTagNode.set(currentJSONSchemaNodeEntry.getKey(), currentJSONSchemaNodeEntry.getValue());

					recordTagNodes.add(recordTagNode);

					break;
				}
			}

			getRecordTagNodes(currentJSONSchemaNodeEntry.getValue(), recordTag, recordTagNodes);
		}
	}

	abstract protected Optional<ObjectNode> getJSONSchema(final String schemaFilePath);

	protected String determineAttributeURI(final String attribute,
	                                       final Optional<String> optionalSchemaUUID,
	                                       final Optional<String> optionalBaseURI) {

		if (optionalSchemaUUID.isPresent()) {

			final String schemaUUID = optionalSchemaUUID.get();

			return SchemaUtils.mintSchemaTermURI(attribute, schemaUUID, optionalBaseURI);
		}

		final String baseURI;

		if (optionalBaseURI.isPresent()) {

			baseURI = optionalBaseURI.get();
		} else {

			baseURI = null;
		}

		return SchemaUtils.mintTermUri(attribute, baseURI);
	}

	private Set<AttributePathHelper> determineAttributePaths(final JsonNode jsonSchemaAttributeNode,
	                                                         final Optional<String> optionalSchemaUUID,
	                                                         final Optional<String> optionalBaseURI,
	                                                         final Set<AttributePathHelper> attributePaths,
	                                                         final AttributePathHelper attributePath,
	                                                         final boolean addRootAttribute) {

		final Optional<String> optionalAttribute = getAttribute(jsonSchemaAttributeNode);

		if (!optionalAttribute.isPresent()) {

			return attributePaths;
		}

		final String attribute = optionalAttribute.get();

		final JsonNode jsonSchemaAttributeContentNode = jsonSchemaAttributeNode.get(attribute);

		final String type = determineJSONSchemaNodeType(jsonSchemaAttributeContentNode);

		if (type.equals(AbstractJSONSchemaParser.ARRAY_JSON_SCHEMA_ATTRIBUTE_TYPE)) {

			// do something with the array, i.e., go deeper in hierarchy (via recursion)
			final JsonNode jsonSchemaAttributeItemsNode = jsonSchemaAttributeContentNode.get(AbstractJSONSchemaParser.JSON_SCHEMA_ITEMS_IDENTIFIER);

			if (jsonSchemaAttributeItemsNode == null) {

				return attributePaths;
			}

			final String arrayType = determineJSONSchemaNodeType(jsonSchemaAttributeItemsNode);

			if (arrayType.equals(AbstractJSONSchemaParser.STRING_JSON_SCHEMA_ATTRIBUTE_TYPE)
					&& (jsonSchemaAttributeItemsNode.get(AbstractJSONSchemaParser.JSON_SCHEMA_ENUM_IDENTIFIER) != null
					|| jsonSchemaAttributeItemsNode.get(AbstractJSONSchemaParser.JSON_SCHEMA_PATTERN_IDENTIFIER) != null
					|| jsonSchemaAttributeItemsNode.size() == 1)) {

				// attribute has enum values, or a pattern, or is simply an array, i.e., simply add the attribute path

				final String finalAttributeURI = determineAttributeURI(attribute, optionalSchemaUUID, optionalBaseURI);
				final boolean multivalue = true;
				AttributePathHelperHelper.addAttributePath(finalAttributeURI, multivalue, attributePaths, attributePath);

				return attributePaths;
			}

			final JsonNode finalJsonSchemaAttributeItemsNode;
			final AttributePathHelper finalAttributePath;

			if (arrayType.equals(AbstractJSONSchemaParser.OBJECT_JSON_SCHEMA_ATTRIBUTE_TYPE)) {

				finalJsonSchemaAttributeItemsNode = jsonSchemaAttributeItemsNode.get(AbstractJSONSchemaParser.JSON_SCHEMA_PROPERTIES_IDENTIFIER);

				final String finalAttributeURI = determineAttributeURI(attribute, optionalSchemaUUID, optionalBaseURI);
				final boolean multivalue = true;
				finalAttributePath = AttributePathHelperHelper.addAttributePath(finalAttributeURI, multivalue, attributePaths, attributePath);

				optionalAddRDFTypeAttributePath(attributePaths, arrayType, attribute, finalAttributePath);
			} else {

				finalJsonSchemaAttributeItemsNode = jsonSchemaAttributeItemsNode;
				finalAttributePath = attributePath;
			}

			final Tuple2<List<JsonNode>, List<JsonNode>> resultSet = determineAttributeAndElementNodes(finalJsonSchemaAttributeItemsNode);

			determineAttributePaths(optionalSchemaUUID, optionalBaseURI, attributePaths, finalAttributePath, resultSet._1);
			determineAttributePaths(optionalSchemaUUID, optionalBaseURI, attributePaths, finalAttributePath, resultSet._2);

			return attributePaths;

		} else {

			final String finalAttributeURI = determineAttributeURI(attribute, optionalSchemaUUID, optionalBaseURI);

			final AttributePathHelper finalAttributePathHelper;

			if (addRootAttribute) {

				final Boolean multivalue = null;
				finalAttributePathHelper = AttributePathHelperHelper.addAttributePath(finalAttributeURI, multivalue, attributePaths, attributePath);
			} else {

				finalAttributePathHelper = attributePath;
			}

			optionalAddRDFTypeAttributePath(attributePaths, type, attribute, finalAttributePathHelper);

			final JsonNode jsonSchemaAttributePropertiesNode = jsonSchemaAttributeContentNode.get(AbstractJSONSchemaParser.JSON_SCHEMA_PROPERTIES_IDENTIFIER);

			final boolean noProperties = jsonSchemaAttributePropertiesNode == null || jsonSchemaAttributePropertiesNode.size() <= 0;

			final boolean addRDFValueAttributePath = doAddRDFValueAttributePath(jsonSchemaAttributeContentNode, type, attribute);

			if (noProperties) {

				optionalAddRDFValueAttributePath(addRDFValueAttributePath, attributePaths, finalAttributePathHelper);

				return attributePaths;
			}

			final Tuple2<List<JsonNode>, List<JsonNode>> resultSet = determineAttributeAndElementNodes(jsonSchemaAttributePropertiesNode);

			determineAttributePaths(optionalSchemaUUID, optionalBaseURI, attributePaths, finalAttributePathHelper, resultSet._1);
			optionalAddRDFValueAttributePath(addRDFValueAttributePath, attributePaths, finalAttributePathHelper);
			determineAttributePaths(optionalSchemaUUID, optionalBaseURI, attributePaths, finalAttributePathHelper, resultSet._2);

			return attributePaths;
		}
	}

	protected abstract void optionalAddRDFTypeAttributePath(final Set<AttributePathHelper> attributePaths,
	                                                        final String type,
	                                                        final String attribute,
	                                                        final AttributePathHelper finalAttributePathHelper);

	protected abstract boolean doAddRDFValueAttributePath(final JsonNode jsonSchemaAttributeContentNode,
	                                                      final String type,
	                                                      final String attribute);

	protected abstract void optionalAddRDFValueAttributePath(final boolean addRDFValueAttributePath,
	                                                         final Set<AttributePathHelper> attributePaths,
	                                                         final AttributePathHelper attributePath);

	private void determineAttributePaths(final Optional<String> optionalSchemaUUID,
	                                     final Optional<String> optionalBaseURI,
	                                     final Set<AttributePathHelper> attributePaths,
	                                     final AttributePathHelper finalAttributePathHelper,
	                                     final List<JsonNode> newJSONSchemaAttributeNodes) {

		for (final JsonNode newJSONSchemaAttributeNode : newJSONSchemaAttributeNodes) {

			determineAttributePaths(newJSONSchemaAttributeNode, optionalSchemaUUID, optionalBaseURI, attributePaths, finalAttributePathHelper, true);
		}
	}

	private Optional<String> getAttribute(final JsonNode jsonSchemaAttributeNode) {

		if (jsonSchemaAttributeNode == null || !jsonSchemaAttributeNode.fieldNames().hasNext()) {

			return Optional.empty();
		}

		final String attribute = jsonSchemaAttributeNode.fieldNames().next();

		return Optional.ofNullable(attribute);
	}

	private Optional<Schema> createSchema(final String uuid) throws DMPPersistenceException {

		// create new schema
		final ProxySchema proxySchema = schemaServiceProvider.get().createObjectTransactional(uuid);

		final Schema schema;

		if (proxySchema != null) {

			schema = proxySchema.getObject();
		} else {

			schema = null;
		}

		return Optional.ofNullable(schema);
	}

	private Optional<Schema> createSchema(final String uuid,
	                                      final String name,
	                                      final String baseURI) throws DMPPersistenceException {

		final Optional<Schema> optionalSchema = createSchema(uuid);

		if (!optionalSchema.isPresent()) {

			return Optional.empty();
		}

		final Schema schema = optionalSchema.get();

		if (name != null) {

			schema.setName(name);
		}

		if (baseURI != null) {

			schema.setBaseURI(baseURI);
		}

		return Optional.of(schema);
	}

	private String determineJSONSchemaNodeType(final JsonNode jsonSchemaNode) {

		final JsonNode typeNode = jsonSchemaNode.get(AbstractJSONSchemaParser.JSON_SCHEMA_TYPE_IDENTIFIER);

		final String type;

		// determine schema element type, e.g., 'object' or 'string'
		if (typeNode != null) {

			type = typeNode.asText();
		} else {

			type = AbstractJSONSchemaParser.UNKNOWN_JSON_SCHEMA_ATTRIBUTE_TYPE;
		}

		return type;
	}

	private Optional<JsonNode> determineCurrentJSONSchemaNode(final JsonNode jsonSchemaNode) {

		final JsonNode currentJSONSchemaNodeProperties = jsonSchemaNode.get(AbstractJSONSchemaParser.JSON_SCHEMA_PROPERTIES_IDENTIFIER);

		if (currentJSONSchemaNodeProperties != null && currentJSONSchemaNodeProperties.size() > 0) {

			return Optional.of(currentJSONSchemaNodeProperties);
		}

		final JsonNode currentJSONSchemaNodeItems = jsonSchemaNode.get(AbstractJSONSchemaParser.JSON_SCHEMA_ITEMS_IDENTIFIER);

		if (currentJSONSchemaNodeItems != null && currentJSONSchemaNodeItems.size() > 0) {

			return Optional.of(currentJSONSchemaNodeItems);
		}

		return Optional.empty();
	}

	/**
	 * v1 = attribute nodes
	 * v2 = element nodes
	 *
	 * @param jsonSchemaAttributeContentSubNode
	 * @return
	 */
	private Tuple2<List<JsonNode>, List<JsonNode>> determineAttributeAndElementNodes(final JsonNode jsonSchemaAttributeContentSubNode) {

		final Iterator<Map.Entry<String, JsonNode>> iter = jsonSchemaAttributeContentSubNode.fields();

		final List<JsonNode> newAttributeNodes = Lists.newArrayList();
		final List<JsonNode> newElementNodes = Lists.newArrayList();

		while (iter.hasNext()) {

			final Map.Entry<String, JsonNode> entry = iter.next();

			final ObjectNode newJSONSchemaAttributeNode = objectMapperProvider.get().createObjectNode();

			final String newAttribute = entry.getKey();

			newJSONSchemaAttributeNode.set(newAttribute, entry.getValue());

			addAttributeNode(newAttributeNodes, newElementNodes, newJSONSchemaAttributeNode, newAttribute);
		}

		return Tuple.of(newAttributeNodes, newElementNodes);
	}

	protected abstract void addAttributeNode(final List<JsonNode> newAttributeNodes,
	                                         final List<JsonNode> newElementNodes,
	                                         final ObjectNode newJSONSchemaAttributeNode,
	                                         final String newAttribute);

	private Optional<Map<String, AttributePathHelper>> convertSetToMap(final Optional<Set<AttributePathHelper>> optionalAttributePaths) {

		if (!optionalAttributePaths.isPresent()) {

			return Optional.empty();
		}

		final Map<String, AttributePathHelper> attributePathsMap = new LinkedHashMap<>();

		for (final AttributePathHelper attributePath : optionalAttributePaths.get()) {

			attributePathsMap.put(attributePath.toString(), attributePath);
		}

		return Optional.of(attributePathsMap);
	}

	private void addRecordClass(final List<JsonNode> recordTagNodes,
	                            final Schema schema) throws DMPPersistenceException {

		final Optional<String> optionalRecordTagAttribute = getAttribute(recordTagNodes.get(0));

		if (optionalRecordTagAttribute.isPresent()) {

			final String recordTagAttribute = optionalRecordTagAttribute.get();

			final String finalRecordTagAttribute;

			if (recordTagAttribute.equals(AbstractJSONSchemaParser.ROOT_NODE_IDENTIFIER)) {

				finalRecordTagAttribute = DEFAULT_RECORD_CLASS_IDENTIFIER;
			} else {

				finalRecordTagAttribute = recordTagAttribute;
			}

			final String recordTagAttributeURI = SchemaUtils.mintSchemaTermURI(finalRecordTagAttribute, schema.getUuid(), Optional.ofNullable(schema.getBaseURI()));

			final String recordClassUri = recordTagAttributeURI + RECORD_CLASS_POSTFIX;

			SchemaUtils.addRecordClass(schema, recordClassUri, classServiceProvider);
		}
	}

}
