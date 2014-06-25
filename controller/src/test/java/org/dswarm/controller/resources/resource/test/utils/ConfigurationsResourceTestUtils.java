package org.dswarm.controller.resources.resource.test.utils;

import org.dswarm.controller.resources.test.utils.ExtendedBasicDMPResourceTestUtils;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.proxy.ProxyConfiguration;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.resource.test.utils.ConfigurationServiceTestUtils;

public class ConfigurationsResourceTestUtils extends
		ExtendedBasicDMPResourceTestUtils<ConfigurationServiceTestUtils, ConfigurationService, ProxyConfiguration, Configuration> {

	public ConfigurationsResourceTestUtils() {

		super("configurations", Configuration.class, ConfigurationService.class, ConfigurationServiceTestUtils.class);
	}
}
