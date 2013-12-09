package de.avgl.dmp.controller.resources.test;

import java.util.Set;

import de.avgl.dmp.controller.resources.test.utils.ResourceTestUtils;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;

public class ConfigurationsResourceTest extends BasicResourceTest<ConfigurationService, Configuration, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ConfigurationsResourceTest.class);

	public ConfigurationsResourceTest() {

		super(Configuration.class, ConfigurationService.class, "configurations", "configuration.json");
	}

	@Override
	protected boolean compareObjects(final Configuration expectedObject, final Configuration actualObject) {

		ResourceTestUtils.compareConfigurations(expectedObject, actualObject);

		return true;
	}

	@Override
	protected boolean evaluateObjects(final Set<Configuration> expectedObjects, final String actualObjects) throws Exception {

		ResourceTestUtils.evaluateConfigurations(actualObjects, expectedObjects);

		return true;
	}
}
