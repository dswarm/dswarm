package de.avgl.dmp.persistence.model.utils;

import de.avgl.dmp.init.DMPException;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.services.BasicJPAService;
import de.avgl.dmp.persistence.services.ConfigurationService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;


public class ConfigurationReferenceDeserializer extends ReferenceDeserializer<Configuration> {

	public ConfigurationReferenceDeserializer() {
		super();
	}

	@Override
	BasicJPAService<Configuration, Long> getJpaService() throws DMPException {
		return DMPPersistenceUtil.getInjector().getInstance(ConfigurationService.class);
	}

}
