package de.avgl.dmp.controller.resources.resource.test.utils;

import de.avgl.dmp.controller.resources.test.utils.ExtendedBasicDMPResourceTestUtils;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.proxy.ProxyConfiguration;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;
import de.avgl.dmp.persistence.service.resource.test.utils.ConfigurationServiceTestUtils;

public class ConfigurationsResourceTestUtils extends
		ExtendedBasicDMPResourceTestUtils<ConfigurationServiceTestUtils, ConfigurationService, ProxyConfiguration, Configuration> {

	public ConfigurationsResourceTestUtils() {

		super("configurations", Configuration.class, ConfigurationService.class, ConfigurationServiceTestUtils.class);
	}
}
