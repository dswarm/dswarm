package org.dswarm.controller.resources.resource.test;

import org.dswarm.controller.resources.resource.test.utils.ConfigurationsResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.proxy.ProxyConfiguration;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.resource.test.utils.ConfigurationServiceTestUtils;

public class ConfigurationsResourceTest
		extends
		BasicResourceTest<ConfigurationsResourceTestUtils, ConfigurationServiceTestUtils, ConfigurationService, ProxyConfiguration, Configuration, Long> {

	public ConfigurationsResourceTest() {

		super(Configuration.class, ConfigurationService.class, "configurations", "controller_configuration.json",
				new ConfigurationsResourceTestUtils());

		updateObjectJSONFileName = "configuration2.json";
	}
}
