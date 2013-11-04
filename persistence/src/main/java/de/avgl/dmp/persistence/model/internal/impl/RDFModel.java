package de.avgl.dmp.persistence.model.internal.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.internal.rdf.helper.ConverterHelper;
import de.avgl.dmp.persistence.model.internal.rdf.helper.ConverterHelperHelper;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 *
 * @author tgaengler
 *
 */
public class RDFModel implements Model {

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(RDFModel.class);


	private final com.hp.hpl.jena.rdf.model.Model	model;
	private final String							resourceURI;

	public RDFModel(final com.hp.hpl.jena.rdf.model.Model modelArg, final String resourceURIArg) {

		model = modelArg;
		resourceURI = resourceURIArg;
	}

	public com.hp.hpl.jena.rdf.model.Model getModel() {

		return model;
	}

	public String getResourceURI() {

		return resourceURI;
	}

	@Override
	public JsonNode toJSON() {

		if (model == null) {

			LOG.debug("model is null, can't convert model to JSON");

			return null;
		}
		
		// model.write(System.out, "N3");

		if (resourceURI == null) {

			LOG.debug("resource URI is null, can't convert model to JSON");

			return null;
		}

		final Resource resource = model.getResource(resourceURI);

		if (resource == null) {

			LOG.debug("couldn't find resource for resource uri '" + resourceURI + "' in model");

			return null;
		}

		final ObjectNode json = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

		convertRDFToJSON(resource, json, json);

		return json;
	}

	private JsonNode convertRDFToJSON(final Resource resource, final ObjectNode rootJson, final ObjectNode json) {

		final StmtIterator iter = resource.listProperties();
		final Map<String, ConverterHelper> converterHelpers = Maps.newHashMap();
		final List<Statement> statements = iter.toList();

		for(final Statement statement : statements) {

			final String propertyURI = statement.getPredicate().getURI();
			final RDFNode rdfNode = statement.getObject();

			if (rdfNode.isLiteral()) {


				String propName = "@" + propertyURI.substring(propertyURI.lastIndexOf('#') + 1);
				if (propName.equals("@value")) {
					propName = "#text";
				}
				ConverterHelperHelper.addLiteralToConverterHelper(converterHelpers, propName, rdfNode);

				continue;
			}

			if (rdfNode.asNode().isBlank()) {

				final ObjectNode objectNode = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

				final JsonNode jsonNode = convertRDFToJSON(rdfNode.asResource(), rootJson, objectNode);

				String propName = propertyURI.substring(propertyURI.lastIndexOf('#') + 1);
				ConverterHelperHelper.addBNodeToConverterHelper(converterHelpers, propName, jsonNode);

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

		for(final Entry<String, ConverterHelper> converterHelperEntry : converterHelpers.entrySet()) {

			converterHelperEntry.getValue().build(json);
		}

		return json;
	}
}
