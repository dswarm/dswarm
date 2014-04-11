package de.avgl.dmp.persistence.model.internal.gdm;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.avgl.dmp.graph.json.LiteralNode;
import de.avgl.dmp.graph.json.Node;
import de.avgl.dmp.graph.json.Resource;
import de.avgl.dmp.graph.json.ResourceNode;
import de.avgl.dmp.graph.json.Statement;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.internal.gdm.helper.ConverterHelperGDMHelper;
import de.avgl.dmp.persistence.model.internal.helper.AttributePathHelper;
import de.avgl.dmp.persistence.model.internal.helper.ConverterHelper;
import de.avgl.dmp.persistence.model.internal.rdf.helper.ConverterHelperJenaHelper;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;
import de.avgl.dmp.persistence.util.GDMUtil;

/**
 * @author tgaengler
 */
public class GDMModel implements Model {

	private static final org.apache.log4j.Logger	LOG							= org.apache.log4j.Logger.getLogger(GDMModel.class);

	private final de.avgl.dmp.graph.json.Model		model;
	private final Set<String>						recordURIs;
	private final String							recordClassURI;

	private boolean									areRecordURIsInitialized	= false;

	/**
	 * Creates a new {@link GDMModel} with a given GDM model instance.
	 * 
	 * @param modelArg a GDM model instance that hold the GDM data
	 */
	public GDMModel(final de.avgl.dmp.graph.json.Model modelArg) {

		model = modelArg;
		recordURIs = Sets.newHashSet();
		recordClassURI = null;
	}

	/**
	 * Creates a new {@link GDMModel} with a given GDM model instance and an identifier of the record.
	 * 
	 * @param modelArg a GDM model instance that hold the RDF data
	 * @param recordURIArg the record identifier
	 */
	public GDMModel(final de.avgl.dmp.graph.json.Model modelArg, final String recordURIArg) {

		model = modelArg;
		recordURIs = Sets.newHashSet();

		if (recordURIArg != null) {

			recordURIs.add(recordURIArg);
		}

		recordClassURI = null;
	}

	/**
	 * Creates a new {@link GDMModel} with a given GDM model instance and an identifier of the record.
	 * 
	 * @param modelArg a GDM model instance that hold the RDF data
	 * @param recordURIArg the record identifier
	 * @param recordClassURIArg the URI of the record class
	 */
	public GDMModel(final de.avgl.dmp.graph.json.Model modelArg, final String recordURIArg, final String recordClassURIArg) {

		model = modelArg;
		recordURIs = Sets.newHashSet();

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
	public de.avgl.dmp.graph.json.Model getModel() {

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

				final Set<Resource> recordResources = Sets.newLinkedHashSet(getModel().getResources());

				if (recordResources != null) {

					recordURIs.clear();

					for (final Resource recordResource : recordResources) {

						recordURIs.add(recordResource.getUri());
					}
				}

				areRecordURIsInitialized = true;

				return getRecordURIs();
			}

			return null;
		}

		return recordURIs;
	}

	@Override
	public JsonNode toRawJSON() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonNode getSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<AttributePathHelper> getAttributePaths() {
		// TODO Auto-generated method stub
		return null;
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

		final Iterator<String> iter = getRecordURIs().iterator();

		if (iter == null) {

			return null;
		}

		if (!iter.hasNext()) {

			// no entries

			return null;
		}

		final ArrayNode jsonArray = DMPPersistenceUtil.getJSONObjectMapper().createArrayNode();

		while (iter.hasNext()) {

			final String resourceURI = iter.next();
			final Resource recordResource = model.getResource(resourceURI);

			if (recordResource == null) {

				LOG.debug("couldn't find record resource for record  uri '" + resourceURI + "' in model");

				return null;
			}

			final ObjectNode json = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

			// determine record resource node from statements of the record resource
			final ResourceNode recordResourceNode = GDMUtil.getResourceNode(resourceURI, recordResource);
			
			if(recordResourceNode == null) {
				
				LOG.debug("couldn't find record resource node for record  uri '" + resourceURI + "' in model");
				
				return null;
			}

			convertRDFToJSON(recordResource, recordResourceNode, json, json);

			if (json == null) {

				// TODO: maybe log something here

				continue;
			}

			final ObjectNode resourceJson = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

			resourceJson.put(resourceURI, json);
			jsonArray.add(resourceJson);
		}

		return jsonArray;
	}

	private JsonNode convertRDFToJSON(final Resource recordResource, final Node resourceNode, final ObjectNode rootJson, final ObjectNode json) {

		final Map<String, ConverterHelper> converterHelpers = Maps.newLinkedHashMap();

		// filter record resource statements to statements for subject uri/id (resource node))
		final Set<Statement> statements = GDMUtil.getResourceStatement(resourceNode, recordResource);

		for (final Statement statement : statements) {

			final String propertyURI = statement.getPredicate().getUri();
			final Node gdmNode = statement.getObject();

			if (gdmNode instanceof LiteralNode) {

				ConverterHelperGDMHelper.addLiteralToConverterHelper(converterHelpers, propertyURI, gdmNode);

				continue;
			}

			if (gdmNode instanceof ResourceNode) {

				final ResourceNode object = (ResourceNode) gdmNode;

				// filter record resource statements to statements for object uri (object node))
				final Set<Statement> objectStatements = GDMUtil.getResourceStatement(object, recordResource);

				if (objectStatements == null || objectStatements.isEmpty()) {

					ConverterHelperGDMHelper.addURIResourceToConverterHelper(converterHelpers, propertyURI, gdmNode);

					continue;
				}

				// resource has an uri, but is deeper in the hierarchy -> it will be attached to the root json node as separate
				// entry

				final ObjectNode objectNode = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

				final JsonNode jsonNode = convertRDFToJSON(recordResource, object, rootJson, objectNode);

				rootJson.put(object.getUri(), jsonNode);

				continue;
			}

			// node is (/must be) a blank node

			final ObjectNode objectNode = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

			final JsonNode jsonNode = convertRDFToJSON(recordResource, gdmNode, rootJson, objectNode);

			ConverterHelperJenaHelper.addJSONNodeToConverterHelper(converterHelpers, propertyURI, jsonNode);
		}

		for (final Entry<String, ConverterHelper> converterHelperEntry : converterHelpers.entrySet()) {

			converterHelperEntry.getValue().build(json);
		}

		return json;
	}

}
