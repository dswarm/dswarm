package de.avgl.dmp.persistence.model.utils;

import de.avgl.dmp.init.DMPException;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.proxy.ProxyConfiguration;
import de.avgl.dmp.persistence.service.BasicJPAService;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;


public class ConfigurationReferenceDeserializer extends ReferenceDeserializer<ProxyConfiguration, Configuration> {

	@Override
	BasicJPAService<ProxyConfiguration, Configuration, Long> getJpaService() throws DMPException {
		return DMPPersistenceUtil.getInjector().getInstance(ConfigurationService.class);
	}

}
