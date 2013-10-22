package de.avgl.dmp.persistence.model.internal.impl;

import java.util.Map;

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

public class RDFModel implements Model {

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

			return null;
		}

		if (resourceURI == null) {

			return null;
		}

		final Resource resource = model.getResource(resourceURI);

		if (resource == null) {

			return null;
		}

		final ObjectNode json = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

		convertRDFToJSON(resource, json);

		return json;
	}

	private JsonNode convertRDFToJSON(final Resource resource, final ObjectNode json) {

		final StmtIterator iter = resource.listProperties();
		final Map<String, ConverterHelper> converterHelpers = Maps.newHashMap();

		while (iter.hasNext()) {

			final Statement statement = iter.next();
			final String propertyURI = statement.getPredicate().getURI();
			final RDFNode rdfNode = statement.getObject();

			if (rdfNode.isLiteral()) {

				ConverterHelperHelper.addLiteralToConverterHelper(converterHelpers, propertyURI, rdfNode);

				continue;
			}

			// TODO: continue here: how to handle mixed object types (e.g. resource URI vs. resource object or literal vs.
			// resource object) => solution combine ResourceConverterHelper + LiteralConverterHelper into one class with two lists
			// (one for Strings and one for JsonNode objects)
		}

		return json;
	}
}
