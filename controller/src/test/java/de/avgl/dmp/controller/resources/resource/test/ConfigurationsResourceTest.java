package de.avgl.dmp.controller.resources.resource.test;

import de.avgl.dmp.controller.resources.resource.test.utils.ConfigurationsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.BasicResourceTest;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.proxy.ProxyConfiguration;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;
import de.avgl.dmp.persistence.service.resource.test.utils.ConfigurationServiceTestUtils;

public class ConfigurationsResourceTest
		extends
		BasicResourceTest<ConfigurationsResourceTestUtils, ConfigurationServiceTestUtils, ConfigurationService, ProxyConfiguration, Configuration, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ConfigurationsResourceTest.class);

	public ConfigurationsResourceTest() {

		super(Configuration.class, ConfigurationService.class, "configurations", "configuration.json", new ConfigurationsResourceTestUtils());

		updateObjectJSONFileName = "configuration2.json";
	}
}
