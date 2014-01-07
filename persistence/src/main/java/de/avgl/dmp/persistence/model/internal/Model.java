package de.avgl.dmp.persistence.model.internal;

import com.fasterxml.jackson.databind.JsonNode;

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
}
