/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.dswarm.common.types.Tuple;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.helper.AttributePathHelper;
import org.dswarm.persistence.model.internal.helper.AttributePathHelperHelper;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.schema.*;
import org.dswarm.persistence.util.GDMUtil;
import org.dswarm.xsd2jsonschema.JsonSchemaParser;
import org.dswarm.xsd2jsonschema.model.JSRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * @author tgaengler
 */
public class XMLSchemaParser {

	private static final Logger LOG = LoggerFactory.getLogger(XMLSchemaParser.class);

	private final Provider<SchemaService> schemaServiceProvider;

	private final Provider<ClaszService> classServiceProvider;

	private final Provider<AttributePathService> attributePathServiceProvider;

	private final Provider<SchemaAttributePathInstanceService> schemaAttributePathInstanceServiceProvider;

	private final Provider<AttributeService> attributeServiceProvider;

	private final Provider<ObjectMapper> objectMapperProvider;

	private static final String ROOT_NODE_IDENTIFIER = "__ROOT_NODE__";
	private static final String UNKNOWN_JSON_SCHEMA_ATTRIBUTE_TYPE = "__UNKNOWN__";
	private static final String STRING_JSON_SCHEMA_ATTRIBUTE_TYPE = "string";
	private static final String OBJECT_JSON_SCHEMA_ATTRIBUTE_TYPE = "object";
	private static final String ARRAY_JSON_SCHEMA_ATTRIBUTE_TYPE = "array";

	private static final String JSON_SCHEMA_PROPERTIES_IDENTIFIER = "properties";
	private static final String JSON_SCHEMA_ITEMS_IDENTIFIER = "items";
	private static final String JSON_SCHEMA_TYPE_IDENTIFIER = "type";
	private static final String JSON_SCHEMA_MIXED_IDENTIFIER = "mixed";
	private static final String JSON_SCHEMA_TITLE_IDENTIFIER = "title";

	private boolean includeRecordTag = false;

	@Inject
	public XMLSchemaParser(final Provider<SchemaService> schemaServiceProviderArg,
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

	// Map<String, String>

	public Optional<Schema> parse(final String xmlSchemaFilePath,
	                              final String recordTag,
	                              final String uuid,
	                              final String schemaName,
	                              final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs) throws DMPPersistenceException {

		final Optional<Tuple<Schema, Set<AttributePathHelper>>> optionalResult = parseSeparatelyInternal(xmlSchemaFilePath, recordTag, uuid,
				schemaName);

		if (!optionalResult.isPresent()) {

			return Optional.empty();
		}

		final Schema schema = optionalResult.get().v1();
		final Set<AttributePathHelper> attributePaths = optionalResult.get().v2();

		SchemaUtils.addAttributePaths(schema, attributePaths, attributePathServiceProvider, schemaAttributePathInstanceServiceProvider,
				attributeServiceProvider, optionalAttributePathsSAPIUUIDs);

		final Schema updatedSchema = SchemaUtils.updateSchema(schema, schemaServiceProvider);

		return Optional.ofNullable(updatedSchema);
	}

	public Optional<Schema> parse(final String xmlSchemaFilePath,
	                              final String recordTag,
	                              final String uuid,
	                              final String schemaName) throws DMPPersistenceException {

		return parse(xmlSchemaFilePath, recordTag, uuid, schemaName, Optional.empty());
	}

	public Optional<Tuple<Schema, Map<String, AttributePathHelper>>> parseSeparately(final String xmlSchemaFilePath,
	                                                                                 final String recordTag,
	                                                                                 final String uuid,
	                                                                                 final String schemaName) throws DMPPersistenceException {

		final Optional<Tuple<Schema, Set<AttributePathHelper>>> optionalResult = parseSeparatelyInternal(xmlSchemaFilePath, recordTag, uuid,
				schemaName);

		if (!optionalResult.isPresent()) {

			return Optional.empty();
		}

		final Schema schema = optionalResult.get().v1();
		final Set<AttributePathHelper> attributePaths = optionalResult.get().v2();
		final Optional<Map<String, AttributePathHelper>> optionalAttributePathsMap = convertSetToMap(Optional.of(attributePaths));

		return Optional.of(Tuple.tuple(schema, optionalAttributePathsMap.get()));
	}

	/**
	 * i.e. the attribute paths are not added to the schema yet
	 *
	 * @param xmlSchemaFilePath
	 * @param recordTag
	 * @param uuid
	 * @param schemaName
	 * @return
	 * @throws DMPPersistenceException
	 */
	private Optional<Tuple<Schema, Set<AttributePathHelper>>> parseSeparatelyInternal(final String xmlSchemaFilePath,
	                                                                                  final String recordTag,
	                                                                                  final String uuid,
	                                                                                  final String schemaName)
			throws DMPPersistenceException {

		final Optional<List<JsonNode>> optionalRecordTags = getRecordTagNodes(xmlSchemaFilePath, recordTag);

		if (!optionalRecordTags.isPresent()) {

			return Optional.empty();
		}

		final List<JsonNode> recordTagNodes = optionalRecordTags.get();

		final Optional<Schema> optionalSchema = createSchema(uuid, schemaName);

		if (!optionalSchema.isPresent()) {

			return Optional.empty();
		}

		final Schema schema = optionalSchema.get();

		addRecordClass(recordTagNodes, schema);

		final Set<AttributePathHelper> attributePaths = parseAttributePaths(recordTagNodes);

		return Optional.of(Tuple.tuple(schema, attributePaths));
	}

	public Optional<Set<AttributePathHelper>> parseAttributePaths(final String xmlSchemaFilePath,
	                                                              final Optional<String> optionalRecordTag) {

		if (optionalRecordTag.isPresent()) {

			return parseAttributePaths(xmlSchemaFilePath, optionalRecordTag.get());
		} else {

			return parseAttributePaths(xmlSchemaFilePath);
		}
	}

	public Optional<Map<String, AttributePathHelper>> parseAttributePathsMap(final String xmlSchemaFilePath,
	                                                                         final Optional<String> optionalRecordTag) {

		if (optionalRecordTag.isPresent()) {

			return convertSetToMap(parseAttributePaths(xmlSchemaFilePath, optionalRecordTag.get()));
		} else {

			return convertSetToMap(parseAttributePaths(xmlSchemaFilePath));
		}
	}

	public Optional<Set<AttributePathHelper>> parseAttributePaths(final String xmlSchemaFilePath,
	                                                              final String recordTag) {

		final Optional<List<JsonNode>> optionalRecordTags = getRecordTagNodes(xmlSchemaFilePath, recordTag);

		return parseAttributePaths(optionalRecordTags);
	}

	public Optional<Set<AttributePathHelper>> parseAttributePaths(final String xmlSchemaFilePath) {

		final Optional<ObjectNode> optionalJSONSchema = getJSONSchema(xmlSchemaFilePath);

		if (!optionalJSONSchema.isPresent()) {

			return Optional.empty();
		}

		final List<JsonNode> rootNodes = Lists.newCopyOnWriteArrayList();

		// prepare root node
		final ObjectNode jsonSchema = optionalJSONSchema.get();

		final Optional<JsonNode> optionalTitleNode = Optional.ofNullable(jsonSchema.get(XMLSchemaParser.JSON_SCHEMA_TITLE_IDENTIFIER));

		final String rootNodeIdentifier;

		if (optionalTitleNode.isPresent()) {

			rootNodeIdentifier = optionalTitleNode.get().asText();
			jsonSchema.remove(XMLSchemaParser.JSON_SCHEMA_TITLE_IDENTIFIER);
		} else {

			rootNodeIdentifier = XMLSchemaParser.ROOT_NODE_IDENTIFIER;
		}

		final ObjectNode rootNode = objectMapperProvider.get().createObjectNode();
		rootNode.set(rootNodeIdentifier, jsonSchema);

		rootNodes.add(rootNode);

		final Optional<List<JsonNode>> optionalRootNodes = Optional.of(rootNodes);

		return parseAttributePaths(optionalRootNodes);
	}

	private Optional<Set<AttributePathHelper>> parseAttributePaths(final Optional<List<JsonNode>> optionalRecordTags) {

		if (!optionalRecordTags.isPresent()) {

			return Optional.empty();
		}

		final List<JsonNode> recordTagNodes = optionalRecordTags.get();

		final Set<AttributePathHelper> attributePaths = parseAttributePaths(recordTagNodes);

		return Optional.ofNullable(attributePaths);
	}

	private Set<AttributePathHelper> parseAttributePaths(final List<JsonNode> recordTagNodes) {

		final Set<AttributePathHelper> attributePaths = Sets.newCopyOnWriteArraySet();

		// attribute path retrieval from all records
		for (final JsonNode recordTagNode : recordTagNodes) {

			Set<AttributePathHelper> recordTagNodeAttributePaths = Sets.newLinkedHashSet();

			recordTagNodeAttributePaths = determineAttributePaths(recordTagNode, recordTagNodeAttributePaths, new AttributePathHelper(), includeRecordTag);

			if (recordTagNodeAttributePaths != null && !recordTagNodeAttributePaths.isEmpty()) {

				attributePaths.addAll(recordTagNodeAttributePaths);
			}
		}

		return attributePaths;
	}

	private Optional<List<JsonNode>> getRecordTagNodes(final String xmlSchemaFilePath,
	                                                   final String recordTag) {

		final Optional<ObjectNode> jsonSchemaOptional = getJSONSchema(xmlSchemaFilePath);

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

		final Iterator<Map.Entry<String, JsonNode>> iter = newCurrentJSONSchemaNode.fields();

		while (iter.hasNext()) {

			final Map.Entry<String, JsonNode> currentJSONSchemaNodeEntry = iter.next();

			if (currentJSONSchemaNodeEntry.getKey().endsWith(recordTag)) {

				final JsonNode currentJSONSchemaNodeEntryValue = currentJSONSchemaNodeEntry.getValue();
				final String type = determineJSONSchemaNodeType(currentJSONSchemaNodeEntryValue);

				// to go deeper, when it's an array
				if (!type.equals(XMLSchemaParser.ARRAY_JSON_SCHEMA_ATTRIBUTE_TYPE)) {

					final ObjectNode recordTagNode = objectMapperProvider.get().createObjectNode();
					recordTagNode.set(currentJSONSchemaNodeEntry.getKey(), currentJSONSchemaNodeEntry.getValue());

					recordTagNodes.add(recordTagNode);

					break;
				}
			}

			getRecordTagNodes(currentJSONSchemaNodeEntry.getValue(), recordTag, recordTagNodes);
		}
	}

	private Optional<ObjectNode> getJSONSchema(final String xmlSchemaFilePath) {

		final JsonSchemaParser schemaParser = new JsonSchemaParser();

		final URL resourceURL = Resources.getResource(xmlSchemaFilePath);
		final ByteSource byteSource = Resources.asByteSource(resourceURL);

		try {

			schemaParser.parse(byteSource.openStream());
		} catch (final SAXException e) {

			LOG.error("couldn't parse XML schema '{}'", xmlSchemaFilePath, e);

			return Optional.empty();
		} catch (final IOException e) {

			LOG.error("couldn't read XML schema '{}'", xmlSchemaFilePath, e);

			return Optional.empty();
		}
		final JSRoot root;

		try {

			root = schemaParser.apply(XMLSchemaParser.ROOT_NODE_IDENTIFIER);
		} catch (final SAXException e) {

			LOG.error("couldn't convert XSD to JSON schema for '{}'", xmlSchemaFilePath, e);

			return Optional.empty();
		}

		final ObjectNode json;

		try {

			json = root.toJson(objectMapperProvider.get());
		} catch (final IOException e) {

			LOG.error("couldn't serialize JSON schema for '{}'", xmlSchemaFilePath, e);

			return Optional.empty();
		}

		return Optional.ofNullable(json);
	}

	private Set<AttributePathHelper> determineAttributePaths(final JsonNode jsonSchemaAttributeNode,
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

		if (type.equals(XMLSchemaParser.ARRAY_JSON_SCHEMA_ATTRIBUTE_TYPE)) {

			// do something with the array, i.e., go deeper in hierarchy (via recursion)
			final JsonNode jsonSchemaAttributeItemsNode = jsonSchemaAttributeContentNode.get(XMLSchemaParser.JSON_SCHEMA_ITEMS_IDENTIFIER);
			final Tuple<List<JsonNode>, List<JsonNode>> resultSet = determineAttributeAndElementNodes(jsonSchemaAttributeItemsNode);

			determineAttributePaths(attributePaths, attributePath, resultSet.v1());
			determineAttributePaths(attributePaths, attributePath, resultSet.v2());

			return attributePaths;

		} else {

			final boolean isXMLAttribute = attribute.startsWith("@");

			final String finalAttribute;

			if (isXMLAttribute) {

				finalAttribute = attribute.substring(1, attribute.length());
			} else {

				finalAttribute = attribute;
			}

			final AttributePathHelper finalAttributePathHelper;

			if (addRootAttribute) {

				finalAttributePathHelper = AttributePathHelperHelper.addAttributePath(finalAttribute, attributePaths, attributePath);
			} else {

				finalAttributePathHelper = attributePath;
			}

			if ((type.equals(XMLSchemaParser.STRING_JSON_SCHEMA_ATTRIBUTE_TYPE) || type.equals(OBJECT_JSON_SCHEMA_ATTRIBUTE_TYPE))
					&& !isXMLAttribute) {

				// add rdf:type attribute
				AttributePathHelperHelper.addAttributePath(GDMUtil.RDF_type, attributePaths, finalAttributePathHelper);
			}

			final JsonNode jsonSchemaAttributePropertiesNode = jsonSchemaAttributeContentNode.get(XMLSchemaParser.JSON_SCHEMA_PROPERTIES_IDENTIFIER);

			final boolean noProperties = jsonSchemaAttributePropertiesNode == null || jsonSchemaAttributePropertiesNode.size() <= 0;

			final JsonNode mixedNode = jsonSchemaAttributeContentNode.get(XMLSchemaParser.JSON_SCHEMA_MIXED_IDENTIFIER);

			final boolean isMixed = mixedNode != null && mixedNode.asBoolean();

			final boolean addRDFValueAttributePath =
					(type.equals(XMLSchemaParser.STRING_JSON_SCHEMA_ATTRIBUTE_TYPE) || (type.equals(OBJECT_JSON_SCHEMA_ATTRIBUTE_TYPE) && isMixed))
							&& !isXMLAttribute;

			if (noProperties) {

				addRDFValueAttributePath(addRDFValueAttributePath, attributePaths, finalAttributePathHelper);

				return attributePaths;
			}

			final Tuple<List<JsonNode>, List<JsonNode>> resultSet = determineAttributeAndElementNodes(jsonSchemaAttributePropertiesNode);

			determineAttributePaths(attributePaths, finalAttributePathHelper, resultSet.v1());
			addRDFValueAttributePath(addRDFValueAttributePath, attributePaths, finalAttributePathHelper);
			determineAttributePaths(attributePaths, finalAttributePathHelper, resultSet.v2());

			return attributePaths;
		}
	}

	private void determineAttributePaths(final Set<AttributePathHelper> attributePaths,
	                                     final AttributePathHelper finalAttributePathHelper,
	                                     final List<JsonNode> newJSONSchemaAttributeNodes) {

		for (final JsonNode newJSONSchemaAttributeNode : newJSONSchemaAttributeNodes) {

			determineAttributePaths(newJSONSchemaAttributeNode, attributePaths, finalAttributePathHelper, true);
		}
	}

	private Optional<String> getAttribute(final JsonNode jsonSchemaAttributeNode) {

		if (jsonSchemaAttributeNode == null || !jsonSchemaAttributeNode.fieldNames().hasNext()) {

			return Optional.empty();
		}

		final String attribute = jsonSchemaAttributeNode.fieldNames().next();

		return Optional.ofNullable(attribute);
	}

	private void addRDFValueAttributePath(final boolean addRDFValueAttributePath,
	                                      final Set<AttributePathHelper> attributePaths,
	                                      final AttributePathHelper attributePath) {

		if (addRDFValueAttributePath) {

			// add rdf:value attribute
			AttributePathHelperHelper.addAttributePath(GDMUtil.RDF_value, attributePaths, attributePath);
		}
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
	                                      final String name) throws DMPPersistenceException {

		final Optional<Schema> optionalSchema = createSchema(uuid);

		if (!optionalSchema.isPresent()) {

			return Optional.empty();
		}

		final Schema schema = optionalSchema.get();

		if (name != null) {

			schema.setName(name);
		}

		return Optional.of(schema);
	}

	private String determineJSONSchemaNodeType(final JsonNode jsonSchemaNode) {

		final JsonNode typeNode = jsonSchemaNode.get(XMLSchemaParser.JSON_SCHEMA_TYPE_IDENTIFIER);

		final String type;

		// determine schema element type, e.g., 'object' or 'string'
		if (typeNode != null) {

			type = typeNode.asText();
		} else {

			type = XMLSchemaParser.UNKNOWN_JSON_SCHEMA_ATTRIBUTE_TYPE;
		}

		return type;
	}

	private Optional<JsonNode> determineCurrentJSONSchemaNode(final JsonNode jsonSchemaNode) {

		final JsonNode currentJSONSchemaNodeProperties = jsonSchemaNode.get(XMLSchemaParser.JSON_SCHEMA_PROPERTIES_IDENTIFIER);

		if (currentJSONSchemaNodeProperties != null && currentJSONSchemaNodeProperties.size() > 0) {

			return Optional.of(currentJSONSchemaNodeProperties);
		}

		final JsonNode currentJSONSchemaNodeItems = jsonSchemaNode.get(XMLSchemaParser.JSON_SCHEMA_ITEMS_IDENTIFIER);

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
	private Tuple<List<JsonNode>, List<JsonNode>> determineAttributeAndElementNodes(final JsonNode jsonSchemaAttributeContentSubNode) {

		final Iterator<Map.Entry<String, JsonNode>> iter = jsonSchemaAttributeContentSubNode.fields();

		final List<JsonNode> newAttributeNodes = Lists.newArrayList();
		final List<JsonNode> newElementNodes = Lists.newArrayList();

		while (iter.hasNext()) {

			final Map.Entry<String, JsonNode> entry = iter.next();

			final ObjectNode newJSONSchemaAttributeNode = objectMapperProvider.get().createObjectNode();

			final String newAttribute = entry.getKey();

			newJSONSchemaAttributeNode.set(newAttribute, entry.getValue());

			if (newAttribute.startsWith("@")) {

				newAttributeNodes.add(newJSONSchemaAttributeNode);
			} else {

				newElementNodes.add(newJSONSchemaAttributeNode);
			}
		}

		return Tuple.tuple(newAttributeNodes, newElementNodes);
	}

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

			final String recordTagAttributeURI = SchemaUtils.mintSchemaTermURI(recordTagAttribute, schema.getUuid());

			final String recordClassUri = recordTagAttributeURI + "Type";

			SchemaUtils.addRecordClass(schema, recordClassUri, classServiceProvider);
		}
	}

}
