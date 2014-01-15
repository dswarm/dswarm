package de.avgl.dmp.controller.resources.resource.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.resources.utils.ExtendedBasicDMPResourceUtils;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class ConfigurationsResourceUtils extends ExtendedBasicDMPResourceUtils<ConfigurationService, Configuration> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ConfigurationsResourceUtils.class);

	@Inject
	public ConfigurationsResourceUtils(final Provider<ConfigurationService> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg) {

		super(Configuration.class, persistenceServiceProviderArg, objectMapperProviderArg);
	}
}
