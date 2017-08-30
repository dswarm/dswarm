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
package org.dswarm.persistence.model.internal.gdm;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.graph.json.LiteralNode;
import org.dswarm.graph.json.Node;
import org.dswarm.graph.json.Resource;
import org.dswarm.graph.json.ResourceNode;
import org.dswarm.graph.json.Statement;
import org.dswarm.graph.json.util.Util;
import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.internal.helper.AttributePathHelper;
import org.dswarm.persistence.model.internal.helper.AttributePathHelperHelper;
import org.dswarm.persistence.model.internal.helper.SchemaHelper;
import org.dswarm.persistence.model.internal.helper.SchemaHelperHelper;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
public class GDMModel implements Model {

	private static final Logger LOG = LoggerFactory.getLogger(GDMModel.class);

	private final org.dswarm.graph.json.Model model;

	/**
	 * note: should only contain the _record_ URIs, i.e., no other resource URIs of resource that are sub entities of records (except they are _records_ themselves)
	 */
	private final Set<String> recordURIs = Sets.newLinkedHashSet();
	private final String recordClassURI;

	private boolean areRecordURIsInitialized;

	/**
	 * Creates a new {@link GDMModel} with a given GDM model instance.
	 *
	 * @param modelArg a GDM model instance that hold the GDM data
	 */
	public GDMModel(final org.dswarm.graph.json.Model modelArg) {

		model = modelArg;
		recordClassURI = null;
	}

	/**
	 * Creates a new {@link GDMModel} with a given GDM model instance and an identifier of the record.
	 *
	 * @param modelArg     a GDM model instance that hold the RDF data
	 * @param recordURIArg the record identifier
	 */
	public GDMModel(final org.dswarm.graph.json.Model modelArg, final String recordURIArg) {

		model = modelArg;

		if (recordURIArg != null) {

			recordURIs.add(recordURIArg);
		}

		recordClassURI = null;
	}

	/**
	 * Creates a new {@link GDMModel} with a given GDM model instance and an identifier of the record.
	 *
	 * @param modelArg          a GDM model instance that hold the RDF data
	 * @param recordURIArg      the record identifier
	 * @param recordClassURIArg the URI of the record class
	 */
	public GDMModel(final org.dswarm.graph.json.Model modelArg, final String recordURIArg, final String recordClassURIArg) {

		model = modelArg;

		if (recordURIArg != null) {

			recordURIs.add(recordURIArg);
		}

		recordClassURI = recordClassURIArg;
	}

	/**
	 * Gets the GDM model with the GDM data.
	 *
	 * @return the GDM model with the GDM data
	 */
	public org.dswarm.graph.json.Model getModel() {

		return model;
	}

	/**
	 * Gets the record identifiers.
	 *
	 * @return the record identifiers
	 */
	public Set<String> getRecordURIs() {

		if (recordURIs.isEmpty()) {

			if (!areRecordURIsInitialized) {

				// TODO: do not iterate over all resources of the model - only _record_ resources are needed here (_record_ resources should be contain a statement with the record class as type at least)
				final Set<Resource> recordResources = Sets.newLinkedHashSet(getModel().getResources());

				recordURIs.clear();

				recordURIs.addAll(recordResources.stream().map(Resource::getUri).collect(Collectors.toList()));

				areRecordURIsInitialized = true;

				return getRecordURIs();
			}

			return null;
		} else if (!areRecordURIsInitialized) {

			areRecordURIsInitialized = true;
		}

		return recordURIs;
	}

	@Override
	public JsonNode toRawJSON() {

		// note: simply copied from RDFModel and adapted

		if (model == null) {

			GDMModel.LOG.debug("model is null, can't convert model to JSON");

			return null;
		}

		if (getRecordURIs() == null) {

			GDMModel.LOG.debug("resource URI is null, can't convert model to JSON");

			return null;
		}
		final Iterator<String> iter = getRecordURIs().iterator();
		final String resourceURI = iter.next();
		final Resource recordResource = model.getResource(resourceURI);

		if (recordResource == null) {

			GDMModel.LOG.debug("couldn't find record resource for record  uri '{}' in model", resourceURI);

			return null;
		}

		final ArrayNode json = DMPPersistenceUtil.getJSONObjectMapper().createArrayNode();

		// determine record resource node from statements of the record resource
		final ResourceNode recordResourceNode = Util.getResourceNode(resourceURI, recordResource);

		if (recordResourceNode == null) {

			GDMModel.LOG.debug("couldn't find record resource node for record  uri '{}' in model", resourceURI);

			return null;
		}

		Util.convertToGDMCompactJSON(recordResource, recordResourceNode, json, model);

		return json;
	}

	@Override
	public JsonNode getSchema() {

		// note: simply copied from RDFModel

		final Set<AttributePathHelper> attributePaths = getAttributePaths();

		if (attributePaths == null) {

			return null;
		}

		return determineSchema(attributePaths);
	}

	@Override
	public Set<AttributePathHelper> getAttributePaths() {

		GDMModel.LOG.debug("try to determine attribute paths from model");

		if (model == null) {

			GDMModel.LOG.debug("model is null, can't determine attribute paths from JSON");

			return null;
		}

		if (recordURIs == null) {

			GDMModel.LOG.debug("resource URIs are null, can't determine attribute paths from JSON");

			return null;
		}

		final Set<AttributePathHelper> attributePaths = new LinkedHashSet<>();

		// attribute path retrieval from all records
		for (final String resourceURI : getRecordURIs()) {

			final Resource recordResource = model.getResource(resourceURI);

			if (recordResource == null) {

				GDMModel.LOG.debug("couldn't find record resource for record  uri '{}' in model", resourceURI);

				continue;
			}

			final ObjectNode json = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

			// determine record resource node from statements of the record resource
			final ResourceNode recordResourceNode = Util.getResourceNode(resourceURI, recordResource);

			if (recordResourceNode == null) {

				GDMModel.LOG.debug("couldn't find record resource node for record  uri '{}' in model", resourceURI);

				continue;
			}

			final JsonNode result = determineUnnormalizedSchema(recordResource, recordResourceNode, json, json);

			Set<AttributePathHelper> recordAttributePaths = new LinkedHashSet<>();

			recordAttributePaths = determineAttributePaths(result, recordAttributePaths, new AttributePathHelper());

			if (recordAttributePaths != null && !recordAttributePaths.isEmpty()) {

				attributePaths.addAll(recordAttributePaths);
			}
		}

		GDMModel.LOG.debug("determined attribute paths from model");

		return attributePaths;
	}

	@Override
	public String getRecordClassURI() {

		return recordClassURI;
	}

	@Override
	public void setRecordURIs(final Set<String> recordURIsArg) {

		recordURIs.clear();

		if (recordURIsArg != null) {

			recordURIs.addAll(recordURIsArg);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JsonNode toGDMCompactJSON() {

		return Util.toGDMCompactJSON(model, getRecordURIs());
	}

	@Override
	public JsonNode toGDMSimpleJSON() {

		return Util.toGDMSimpleJSON(model, getRecordURIs());
	}

	@Override
	public JsonNode toGDMSimpleShortJSON() {

		return Util.toGDMSimpleShortJSON(model, getRecordURIs());
	}

	@Override
	public JsonNode toJSON() {

		return Util.toJSON(model, getRecordURIs());
	}

	private JsonNode determineUnnormalizedSchema(final Resource recordResource,
	                                             final Node resourceNode,
	                                             final ObjectNode rootJson,
	                                             final JsonNode json) {

		// filter record resource statements to statements for subject uri/id (resource node))
		final Set<Statement> statements = Util.getResourceStatement(resourceNode, recordResource);

		final Map<String, SchemaHelper> schemaHelpers = Maps.newLinkedHashMap();

		for (final Statement statement : statements) {

			final String propertyURI = statement.getPredicate().getUri();
			final Node gdmNode = statement.getObject();

			if (gdmNode instanceof LiteralNode) {

				SchemaHelperHelper.addPropertyToSchemaHelpers(schemaHelpers, propertyURI);

				continue;
			}

			if (gdmNode instanceof ResourceNode) {

				final ResourceNode object = (ResourceNode) gdmNode;

				// filter record resource statements to statements for object uri (object node))
				final Set<Statement> objectStatements = Util.getResourceStatement(object, recordResource);

				if (objectStatements == null || objectStatements.isEmpty()) {

					SchemaHelperHelper.addPropertyToSchemaHelpers(schemaHelpers, propertyURI);

					continue;
				}

				// resource has an uri, but is deeper in the hierarchy -> it will be attached to the root json node as separate
				// entry

				final ObjectNode objectNode = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

				final JsonNode jsonNode = determineUnnormalizedSchema(recordResource, gdmNode, rootJson, objectNode);

				SchemaHelperHelper.addJSONNodeToSchemaHelper(schemaHelpers, propertyURI, jsonNode);

				continue;
			}

			// node must be a blank node

			final ObjectNode objectNode = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

			final JsonNode jsonNode = determineUnnormalizedSchema(recordResource, gdmNode, rootJson, objectNode);

			SchemaHelperHelper.addJSONNodeToSchemaHelper(schemaHelpers, propertyURI, jsonNode);
		}

		if (schemaHelpers.size() > 1) {

			final ArrayNode arrayNode = DMPPersistenceUtil.getJSONObjectMapper().createArrayNode();

			for (final Entry<String, SchemaHelper> schemaHelperEntry : schemaHelpers.entrySet()) {

				final JsonNode result = schemaHelperEntry.getValue().build(DMPPersistenceUtil.getJSONObjectMapper().createObjectNode());

				arrayNode.add(result);
			}

			return arrayNode;
		} else if (schemaHelpers.size() == 1) {

			final SchemaHelper schemaHelper = schemaHelpers.values().iterator().next();

			return schemaHelper.build(DMPPersistenceUtil.getJSONObjectMapper().createObjectNode());
		}

		return json;
	}

	private Set<AttributePathHelper> determineAttributePaths(final JsonNode unnormalizedSchema,
	                                                         final Set<AttributePathHelper> attributePaths,
	                                                         final AttributePathHelper attributePath) {

		// note: simply copied from RDFModel
		// TODO: create abstracted class for RDFModel + GDMModel to share methods

		final JsonNodeType nodeType = unnormalizedSchema.getNodeType();

		switch (nodeType) {

			case ARRAY:

				final ArrayNode jsonArray = (ArrayNode) unnormalizedSchema;

				for (final JsonNode entryNode : jsonArray) {

					determineAttributePaths(entryNode, attributePaths, attributePath);
				}

				break;
			case OBJECT:

				final ObjectNode jsonObject = (ObjectNode) unnormalizedSchema;

				final Iterator<String> fieldNames = jsonObject.fieldNames();

				fieldNames.forEachRemaining(fieldName -> {

					final Boolean multivalue = null;

					final AttributePathHelper newAttributePath = AttributePathHelperHelper.addAttributePath(fieldName, multivalue, attributePaths, attributePath);

					final JsonNode valueNode = jsonObject.get(fieldName);

					determineAttributePaths(valueNode, attributePaths, newAttributePath);
				});

				break;
			default:

				AttributePathHelperHelper.addAttributePath(unnormalizedSchema, attributePaths, attributePath);
		}

		return attributePaths;
	}

	private JsonNode determineSchema(final Set<AttributePathHelper> attributePaths) {

		// note: simply copied from RDFModel
		// TODO: create abstracted class for RDFModel + GDMModel to share methods

		if (attributePaths.size() > 1) {

			return generateSchema(Lists.newArrayList(attributePaths), 1);
		} else {

			final AttributePathHelper attributePathHelper = attributePaths.iterator().next();

			if (attributePathHelper.length() > 1) {
				// only one attribute path
				final List<String> attributePath = attributePathHelper.getAttributePath();

				boolean deepestAttributeTransformed = false;

				JsonNode previousAttribute = null;
				ObjectNode currentAttribute = null;

				while (!attributePath.isEmpty()) {

					final int lastItem = attributePath.size() - 1;
					final String attribute = attributePath.get(lastItem);

					if (deepestAttributeTransformed) {

						currentAttribute = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();
						currentAttribute.set(attribute, previousAttribute);
						previousAttribute = currentAttribute;
					} else {

						previousAttribute = new TextNode(attribute);
						deepestAttributeTransformed = true;
					}

					attributePath.remove(lastItem);
				}

				return currentAttribute;
			} else {

				// only one attribute

				return new TextNode(attributePathHelper.toString());
			}
		}
	}

	private JsonNode generateSchema(final List<AttributePathHelper> attributePaths,
	                                final int level) {

		// note: simply copied from RDFModel
		// TODO: create abstracted class for RDFModel + GDMModel to share methods

		final List<AttributePathHelper> orderedAttributePaths = AttributePathHelperHelper.prepareAttributePathHelpers(
				Lists.newArrayList(attributePaths), level);

		if (orderedAttributePaths == null) {

			return null;
		}

		final boolean hasNextLevel = AttributePathHelperHelper.hasNextLevel(orderedAttributePaths, level);

		final AttributePathHelper firstAttributePathInLevel = orderedAttributePaths.iterator().next();
		final String firstRootAttributePathInLevel = AttributePathHelperHelper.determineLevelRootAttributePath(firstAttributePathInLevel, level);

		final boolean levelAsArray = AttributePathHelperHelper.levelAsArray(orderedAttributePaths, firstRootAttributePathInLevel);

		if (levelAsArray) {

			final ArrayNode jsonArray = DMPPersistenceUtil.getJSONObjectMapper().createArrayNode();

			// determine level root attribute paths
			final Map<String, AttributePathHelper> levelRootAttributePaths = Maps.newLinkedHashMap();

			for (final AttributePathHelper attributePathHelper : orderedAttributePaths) {

				final String levelRootAttributePath = AttributePathHelperHelper.determineLevelRootAttributePath(attributePathHelper, level);

				if (!levelRootAttributePaths.containsKey(levelRootAttributePath)) {

					levelRootAttributePaths.put(levelRootAttributePath, attributePathHelper);
				}
			}

			if (hasNextLevel) {

				for (final Entry<String, AttributePathHelper> levelRootAttributePathEntry : levelRootAttributePaths.entrySet()) {

					final JsonNode nextLevelSchemaJson = generateNextLevelSchemaForRootAttributePath(orderedAttributePaths,
							levelRootAttributePathEntry.getKey(), levelRootAttributePathEntry.getValue(), level);

					jsonArray.add(nextLevelSchemaJson);
				}
			} else {

				for (final AttributePathHelper levelRootAttributePath : levelRootAttributePaths.values()) {

					final List<String> attributePath = levelRootAttributePath.getAttributePath();
					jsonArray.add(attributePath.get(attributePath.size() - 1));
				}
			}

			return jsonArray;
		} else {

			if (hasNextLevel) {

				return generateNextLevelSchemaForRootAttributePath(orderedAttributePaths, firstRootAttributePathInLevel, firstAttributePathInLevel,
						level);
			} else {

				final List<String> attributePath = firstAttributePathInLevel.getAttributePath();
				return new TextNode(attributePath.get(attributePath.size() - 1));
			}
		}
	}

	private JsonNode generateNextLevelSchemaForRootAttributePath(final List<AttributePathHelper> attributePaths,
	                                                             final String levelRootAttributePath,
	                                                             final AttributePathHelper sampleAttributePath,
	                                                             final int level) {

		// note: simply copied from RDFModel
		// TODO: create abstracted class for RDFModel + GDMModel to share methods

		final List<AttributePathHelper> nextLevelAttributePathsForRootAttributePath = AttributePathHelperHelper
				.getNextAttributePathHelpersForLevelRootAttributePath(attributePaths, levelRootAttributePath, level);

		if (nextLevelAttributePathsForRootAttributePath != null) {

			final ObjectNode jsonObject = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

			final JsonNode nextLevelAttributePathJson = generateSchema(nextLevelAttributePathsForRootAttributePath, level + 1);

			jsonObject.set(sampleAttributePath.getAttributePath().get(level - 1), nextLevelAttributePathJson);

			return jsonObject;
		} else {

			return new TextNode(sampleAttributePath.getAttributePath().get(level - 1));
		}
	}
}
