package de.avgl.dmp.persistence.model.internal.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.internal.rdf.helper.ConverterHelper;
import de.avgl.dmp.persistence.model.internal.rdf.helper.ConverterHelperHelper;
import de.avgl.dmp.persistence.model.internal.rdf.helper.SchemaHelper;
import de.avgl.dmp.persistence.model.internal.rdf.helper.SchemaHelperHelper;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * A {@link Model} implementation for RDF data.
 * 
 * @author tgaengler
 */
public class RDFModel implements Model {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(RDFModel.class);

	private final com.hp.hpl.jena.rdf.model.Model	model;
	private final String							recordURI;
	private final String							recordClassURI;

	/**
	 * Creates a new {@link RDFModel} with a given Jena model instance.
	 * 
	 * @param modelArg a Jena model instance that hold the RDF data
	 */
	public RDFModel(final com.hp.hpl.jena.rdf.model.Model modelArg) {

		model = modelArg;
		recordURI = null;
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
		recordURI = recordURIArg;
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
		recordURI = recordURIArg;
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
	 * Gets the record identifier.
	 * 
	 * @return the record identifier
	 */
	public String getRecordURI() {

		return recordURI;
	}

	/**
	 * Gets the record class identifier.
	 * 
	 * @return the record class identifier
	 */
	public String getRecordClassURI() {

		return recordClassURI;
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

		if (recordURI == null) {

			LOG.debug("resource URI is null, can't convert model to JSON");

			return null;
		}

		// System.out.println("write rdf model '" + resourceURI + "' in n3");
		// model.write(System.out, "N3");

		final Resource recordResource = model.getResource(recordURI);

		if (recordResource == null) {

			LOG.debug("couldn't find record resource for record  uri '" + recordURI + "' in model");

			return null;
		}

		final ObjectNode json = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

		convertRDFToJSON(recordResource, json, json);

		return json;
	}

	@Override
	public JsonNode getSchema() {

		if (model == null) {

			LOG.debug("model is null, can't convert model to JSON");

			return null;
		}

		if (recordURI == null) {

			LOG.debug("resource URI is null, can't convert model to JSON");

			return null;
		}

		// System.out.println("write rdf model '" + resourceURI + "' in n3");
		// model.write(System.out, "N3");

		final Resource recordResource = model.getResource(recordURI);

		if (recordResource == null) {

			LOG.debug("couldn't find record resource for record  uri '" + recordURI + "' in model");

			return null;
		}

		final ObjectNode json = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

		final JsonNode result = determineSchema(recordResource, json, json);
		
		// TODO: normalize

		return result;
	}

	private JsonNode convertRDFToJSON(final Resource resource, final ObjectNode rootJson, final ObjectNode json) {

		final StmtIterator iter = resource.listProperties();
		final Map<String, ConverterHelper> converterHelpers = Maps.newLinkedHashMap();
		final List<Statement> statements = iter.toList();

		for (final Statement statement : statements) {

			final String propertyURI = statement.getPredicate().getURI();
			final RDFNode rdfNode = statement.getObject();

			if (rdfNode.isLiteral()) {

				String propName = "@" + propertyURI.substring(propertyURI.lastIndexOf('#') + 1);
				if ("@value".equals(propName)) {
					propName = "#text";
				}
				ConverterHelperHelper.addLiteralToConverterHelper(converterHelpers, propName, rdfNode);

				continue;
			}

			if (rdfNode.asNode().isBlank()) {

				final ObjectNode objectNode = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

				final JsonNode jsonNode = convertRDFToJSON(rdfNode.asResource(), rootJson, objectNode);

				final String propName = propertyURI.substring(propertyURI.lastIndexOf('#') + 1);
				ConverterHelperHelper.addJSONNodeToConverterHelper(converterHelpers, propName, jsonNode);

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

	private JsonNode determineSchema(final Resource resource, final ObjectNode rootJson, final JsonNode json) {

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

				final JsonNode jsonNode = determineSchema(rdfNode.asResource(), rootJson, objectNode);

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

				final JsonNode jsonNode = determineSchema(rdfNode.asResource(), rootJson, objectNode);

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
		} else if(schemaHelpers.size() == 1) {
			
			final SchemaHelper schemaHelper = schemaHelpers.values().iterator().next();
			
			final JsonNode result = schemaHelper.build(DMPPersistenceUtil.getJSONObjectMapper().createObjectNode());
			
			return result;
		}

		return json;
	}
}
