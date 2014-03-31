package de.avgl.dmp.persistence.model.internal.gdm;

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.internal.rdf.helper.AttributePathHelper;

/**
 * @author tgaengler
 */
public class GDMModel implements Model {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(GDMModel.class);

	private final de.avgl.dmp.graph.json.Model		model;
	private final Set<String>						recordURIs;
	private final String							recordClassURI;

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
	 * Gets the GDM model with the GDM data.
	 * 
	 * @return the GDM model with the GDM data
	 */
	public de.avgl.dmp.graph.json.Model getModel() {

		return model;
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

	@Override
	public JsonNode toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

}
