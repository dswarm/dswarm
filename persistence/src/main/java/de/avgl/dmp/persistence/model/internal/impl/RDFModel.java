package de.avgl.dmp.persistence.model.internal.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.internal.rdf.helper.AttributePathHelper;
import de.avgl.dmp.persistence.model.internal.rdf.helper.AttributePathHelperHelper;
import de.avgl.dmp.persistence.model.internal.rdf.helper.ConverterHelper;
import de.avgl.dmp.persistence.model.internal.rdf.helper.ConverterHelperHelper;
import de.avgl.dmp.persistence.model.internal.rdf.helper.SchemaHelper;
import de.avgl.dmp.persistence.model.internal.rdf.helper.SchemaHelperHelper;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;
import de.avgl.dmp.persistence.util.RDFUtil;

/**
 * A {@link Model} implementation for RDF data.
 * 
 * @author tgaengler
 */
public class RDFModel implements Model {

	private static final org.apache.log4j.Logger	LOG							= org.apache.log4j.Logger.getLogger(RDFModel.class);

	private final com.hp.hpl.jena.rdf.model.Model	model;
	private final Set<String>						recordURIs;
	private final String							recordClassURI;

	private boolean									areRecordURIsInitialized	= false;

	/**
	 * Creates a new {@link RDFModel} with a given Jena model instance.
	 * 
	 * @param modelArg a Jena model instance that hold the RDF data
	 */
	public RDFModel(final com.hp.hpl.jena.rdf.model.Model modelArg) {

		model = modelArg;
		recordURIs = Sets.newHashSet();
		recordClassURI = null;
	}

	/**
	 * Creates a new {@link RDFModel} with a given Jena model instance and an identifier of the record.
	 * 
	 * @param modelArg a Jena model instance that hold the RDF data
	 * @param recordURIArg the record identifier
	 */
	public RDFModel(final com.hp.hpl.jena.rdf.model.Model modelArg, final String recordURIArg) {

		model = modelArg;
		recordURIs = Sets.newHashSet();

		if (recordURIArg != null) {

			recordURIs.add(recordURIArg);
		}

		recordClassURI = null;
	}

	/**
	 * Creates a new {@link RDFModel} with a given Jena model instance, an identifier of the record and an identifier of the
	 * record class.
	 * 
	 * @param modelArg a Jena model instance that hold the RDF data
	 * @param recordURIArg the record identifier
	 * @param recordClassURIArg the record class identifier
	 */
	public RDFModel(final com.hp.hpl.jena.rdf.model.Model modelArg, final String recordURIArg, final String recordClassURIArg) {

		model = modelArg;
		recordURIs = Sets.newHashSet();

		if (recordURIArg != null) {

			recordURIs.add(recordURIArg);
		}

		recordClassURI = recordClassURIArg;
	}

	/**
	 * Gets the Jena model with the RDF data.
	 * 
	 * @return the Jena model with the RDF data
	 */
	public com.hp.hpl.jena.rdf.model.Model getModel() {

		return model;
	}

	/**
	 * Gets the record identifiers.
	 * 
	 * @return the record identifiers
	 */
	public Set<String> getRecordURIs() {

		if (recordURIs == null || recordURIs.isEmpty()) {

			if (!areRecordURIsInitialized) {

				final Set<Resource> recordResources = RDFUtil.getRecordResources(getRecordClassURI(), getModel());

				if (recordResources != null) {

					recordURIs.clear();

					for (final Resource recordResource : recordResources) {

						recordURIs.add(recordResource.getURI());
					}
				}

				areRecordURIsInitialized = true;

				return getRecordURIs();
			}

			return null;
		}

		return recordURIs;
	}

	/**
	 * Gets the record class identifier.
	 * 
	 * @return the record class identifier
	 */
	public String getRecordClassURI() {

		return recordClassURI;
	}

	public void setRecordURIs(final Set<String> recordURIsArg) {

		recordURIs.clear();

		if (recordURIsArg != null) {

			recordURIs.addAll(recordURIsArg);
		}
	}

	/**
	 * TODO: (maybe) implement JSON serialisation for multiple records
	 */
	@Override
	public JsonNode toJSON() {

		if (model == null) {

			LOG.debug("model is null, can't convert model to JSON");

			return null;
		}

		if (getRecordURIs() == null) {

			LOG.debug("resource URI is null, can't convert model to JSON");

			return null;
		}

		// System.out.println("write rdf model '" + resourceURI + "' in n3");
		// model.write(System.out, "N3");

		final Resource recordResource = model.getResource(getRecordURIs().iterator().next());

		if (recordResource == null) {

			LOG.debug("couldn't find record resource for record  uri '" + getRecordURIs() + "' in model");

			return null;
		}

		final ObjectNode json = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

		convertRDFToJSON(recordResource, json, json);

		return json;
	}

	@Override
	public JsonNode getSchema() {

		final Set<AttributePathHelper> attributePaths = getAttributePaths();

		if (attributePaths == null) {

			return null;
		}

		final JsonNode schema = determineSchema(attributePaths);

		return schema;
	}

	@Override
	public Set<AttributePathHelper> getAttributePaths() {

		if (model == null) {

			LOG.debug("model is null, can't determine attribute paths from JSON");

			return null;
		}

		if (recordURIs == null) {

			LOG.debug("resource URIs are null, can't determine attribute paths from JSON");

			return null;
		}

		// System.out.println("write rdf model '" + resourceURI + "' in n3");
		// model.write(System.out, "N3");

		// TODO: enable attribute path retrieval from all records, currently, only one record is utilised for schema determination

		final Resource recordResource = model.getResource(getRecordURIs().iterator().next());

		if (recordResource == null) {

			LOG.debug("couldn't find record resource for record  uri '" + getRecordURIs() + "' in model");

			return null;
		}

		final ObjectNode json = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

		final JsonNode result = determineUnnormalizedSchema(recordResource, json, json);

		Set<AttributePathHelper> attributePaths = Sets.newCopyOnWriteArraySet();

		attributePaths = determineAttributePaths(result, attributePaths, new AttributePathHelper());

		return attributePaths;
	}

	private JsonNode convertRDFToJSON(final Resource resource, final ObjectNode rootJson, final ObjectNode json) {

		final StmtIterator iter = resource.listProperties();
		final Map<String, ConverterHelper> converterHelpers = Maps.newLinkedHashMap();
		final List<Statement> statements = iter.toList();

		for (final Statement statement : statements) {

			final String propertyURI = statement.getPredicate().getURI();
			final RDFNode rdfNode = statement.getObject();

			if (rdfNode.isLiteral()) {

				ConverterHelperHelper.addLiteralToConverterHelper(converterHelpers, propertyURI, rdfNode);

				continue;
			}

			if (rdfNode.asNode().isBlank()) {

				final ObjectNode objectNode = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

				final JsonNode jsonNode = convertRDFToJSON(rdfNode.asResource(), rootJson, objectNode);

				ConverterHelperHelper.addJSONNodeToConverterHelper(converterHelpers, propertyURI, jsonNode);

				continue;
			}

			if (rdfNode.isURIResource()) {

				final Resource object = rdfNode.asResource();

				final StmtIterator objectIter = object.listProperties();

				if (objectIter == null || !objectIter.hasNext()) {

					ConverterHelperHelper.addURIResourceToConverterHelper(converterHelpers, propertyURI, rdfNode);

					continue;
				}

				// resource has an uri, but is deeper in the hierarchy -> it will be attached to the root json node as separate
				// entry

				final ObjectNode objectNode = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

				final JsonNode jsonNode = convertRDFToJSON(rdfNode.asResource(), rootJson, objectNode);

				rootJson.put(rdfNode.asResource().getURI(), jsonNode);
			}
		}

		for (final Entry<String, ConverterHelper> converterHelperEntry : converterHelpers.entrySet()) {

			converterHelperEntry.getValue().build(json);
		}

		return json;
	}

	private JsonNode determineUnnormalizedSchema(final Resource resource, final ObjectNode rootJson, final JsonNode json) {

		final StmtIterator iter = resource.listProperties();
		final Map<String, SchemaHelper> schemaHelpers = Maps.newLinkedHashMap();
		final List<Statement> statements = iter.toList();

		for (final Statement statement : statements) {

			final String propertyURI = statement.getPredicate().getURI();
			final RDFNode rdfNode = statement.getObject();

			if (rdfNode.isLiteral()) {

				SchemaHelperHelper.addPropertyToSchemaHelpers(schemaHelpers, propertyURI);

				continue;
			}

			if (rdfNode.asNode().isBlank()) {

				final ObjectNode objectNode = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

				final JsonNode jsonNode = determineUnnormalizedSchema(rdfNode.asResource(), rootJson, objectNode);

				SchemaHelperHelper.addJSONNodeToSchemaHelper(schemaHelpers, propertyURI, jsonNode);

				continue;
			}

			if (rdfNode.isURIResource()) {

				final Resource object = rdfNode.asResource();

				final StmtIterator objectIter = object.listProperties();

				if (objectIter == null || !objectIter.hasNext()) {

					SchemaHelperHelper.addPropertyToSchemaHelpers(schemaHelpers, propertyURI);

					continue;
				}

				// resource has an uri, but is deeper in the hierarchy -> it will be attached to the root json node as separate
				// entry

				final ObjectNode objectNode = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

				final JsonNode jsonNode = determineUnnormalizedSchema(rdfNode.asResource(), rootJson, objectNode);

				SchemaHelperHelper.addJSONNodeToSchemaHelper(schemaHelpers, propertyURI, jsonNode);
			}
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

			final JsonNode result = schemaHelper.build(DMPPersistenceUtil.getJSONObjectMapper().createObjectNode());

			return result;
		}

		return json;
	}

	private Set<AttributePathHelper> determineAttributePaths(final JsonNode unnormalizedSchema, Set<AttributePathHelper> attributePaths,
			AttributePathHelper attributePath) {

		if (ArrayNode.class.isInstance(unnormalizedSchema)) {

			final ArrayNode jsonArray = (ArrayNode) unnormalizedSchema;

			for (final JsonNode entryNode : jsonArray) {

				final Set<AttributePathHelper> newAttributePaths = determineAttributePaths(entryNode, attributePaths, attributePath);
				attributePaths.addAll(newAttributePaths);
			}

		} else if (ObjectNode.class.isInstance(unnormalizedSchema)) {

			final ObjectNode jsonObject = (ObjectNode) unnormalizedSchema;

			Iterator<String> fieldNames = jsonObject.fieldNames();

			while (fieldNames.hasNext()) {

				final String fieldName = fieldNames.next();

				final AttributePathHelper newAttributePath = AttributePathHelperHelper.addAttributePath(fieldName, attributePaths, attributePath);

				final JsonNode valueNode = jsonObject.get(fieldName);

				final Set<AttributePathHelper> newAttributePaths = determineAttributePaths(valueNode, attributePaths, newAttributePath);
				attributePaths.addAll(newAttributePaths);
			}

		} else if (TextNode.class.isInstance(unnormalizedSchema)) {

			AttributePathHelperHelper.addAttributePath(unnormalizedSchema, attributePaths, attributePath);
		}

		return attributePaths;
	}

	private JsonNode determineSchema(final Set<AttributePathHelper> attributePaths) {

		if (attributePaths.size() > 1) {

			return generateSchema(Lists.newArrayList(attributePaths), 1);
		} else {

			final AttributePathHelper attributePathHelper = attributePaths.iterator().next();

			if (attributePathHelper.length() > 1) {

				// only one attribute path
				final LinkedList<String> attributePath = attributePathHelper.getAttributePath();

				boolean deepestAttributeTransformed = false;

				JsonNode previousAttribute = null;
				ObjectNode currentAttribute = null;

				while (attributePath.size() > 0) {

					final String attribute = attributePath.getLast();

					if (deepestAttributeTransformed) {

						currentAttribute = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();
						currentAttribute.put(attribute, previousAttribute);
						previousAttribute = currentAttribute;
					} else {

						previousAttribute = new TextNode(attribute);
						deepestAttributeTransformed = true;
					}

					attributePath.removeLast();
				}

				return currentAttribute;
			} else {

				// only one attribute

				return new TextNode(attributePathHelper.toString());
			}
		}
	}

	private JsonNode generateSchema(final List<AttributePathHelper> attributePaths, final int level) {

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
			final Map<String, AttributePathHelper> levelRootAttributePaths = Maps.newHashMap();

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

					jsonArray.add(levelRootAttributePath.getAttributePath().getLast());
				}
			}

			return jsonArray;
		} else {

			if (hasNextLevel) {

				return generateNextLevelSchemaForRootAttributePath(orderedAttributePaths, firstRootAttributePathInLevel, firstAttributePathInLevel,
						level);
			} else {

				return new TextNode(firstAttributePathInLevel.getAttributePath().getLast());
			}
		}
	}

	private JsonNode generateNextLevelSchemaForRootAttributePath(final List<AttributePathHelper> attributePaths, final String levelRootAttributePath,
			final AttributePathHelper sampleAttributePath, final int level) {

		final List<AttributePathHelper> nextLevelAttributePathsForRootAttributePath = AttributePathHelperHelper
				.getNextAttributePathHelpersForLevelRootAttributePath(attributePaths, levelRootAttributePath, level);

		if (nextLevelAttributePathsForRootAttributePath != null) {

			final ObjectNode jsonObject = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

			final JsonNode nextLevelAttributePathJson = generateSchema(nextLevelAttributePathsForRootAttributePath, level + 1);

			jsonObject.put(sampleAttributePath.getAttributePath().get(level - 1), nextLevelAttributePathJson);

			return jsonObject;
		} else {

			return new TextNode(sampleAttributePath.getAttributePath().get(level - 1));
		}
	}
}
