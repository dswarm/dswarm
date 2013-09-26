package de.avgl.dmp.persistence.model.utils;

import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.services.ConfigurationService;


public class ConfigurationReferenceDeserializer extends ReferenceDeserializer<Configuration> {

	public ConfigurationReferenceDeserializer() {
		
		super(new ConfigurationService());
	}

}
