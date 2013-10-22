package de.avgl.dmp.persistence.model.internal;

import com.fasterxml.jackson.databind.JsonNode;


public interface Model {
	
	JsonNode toJSON();
}
