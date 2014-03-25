package de.avgl.dmp.persistence.model.internal;

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import de.avgl.dmp.persistence.model.internal.rdf.helper.AttributePathHelper;

/**
 * The model interface for holding and providing data for further processing.
 * 
 * @author tgaengler
 */
public interface Model {

	/**
	 * Serializes the data of the model to JSON.
	 * 
	 * @return a JSON serialisation of the data of the model
	 */
	JsonNode toJSON();
	
	JsonNode getSchema();
	
	Set<AttributePathHelper> getAttributePaths();
	
	String getRecordClassURI();
	
	void setRecordURIs(final Set<String> recordURIs);
}
