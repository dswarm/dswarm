package org.dswarm.persistence.model.internal;

import java.util.Set;

import org.dswarm.persistence.model.internal.helper.AttributePathHelper;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The model interface for holding and providing data for further processing.
 * 
 * @author tgaengler
 */
public interface Model {

	/**
	 * Serializes the data (i.e. only the data - without record identifier) of the model to JSON.
	 * 
	 * @return a JSON serialisation of the data of the model
	 */
	JsonNode toRawJSON();

	/**
	 * Serializes the data (incl. record identifiers -> record identifiers are the keys of the JSON objects of the JSON array) of
	 * the model to JSON.
	 * 
	 * @return a JSON serialisation of the data of the model
	 */
	JsonNode toJSON();

	JsonNode getSchema();

	Set<AttributePathHelper> getAttributePaths();

	String getRecordClassURI();

	void setRecordURIs(final Set<String> recordURIs);
}
