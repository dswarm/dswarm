package de.avgl.dmp.controller.resources.test.utils;

import java.util.Iterator;
import java.util.Map.Entry;

import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;

public class ConfigurationsResourceTestUtils extends BasicResourceTestUtils<ConfigurationService, Configuration, Long> {

	public ConfigurationsResourceTestUtils() {

		super("configurations", Configuration.class, ConfigurationService.class);
	}

	@Override
	public void compareObjects(final Configuration expectedObject, final Configuration actualObject) {
		
		super.compareObjects(expectedObject, actualObject);
		
		compareConfigurations(expectedObject, actualObject);
	}
	
	private void compareConfigurations(final Configuration expectedConfiguration, final Configuration actualConfiguration) {

		if (expectedConfiguration.getName() != null) {

			Assert.assertNotNull("the configuration name shouldn't be null", actualConfiguration.getName());
			Assert.assertEquals("the configuration names should be equal", expectedConfiguration.getName(), actualConfiguration.getName());
		}

		if (expectedConfiguration.getDescription() != null) {

			Assert.assertNotNull("the configuration description shouldn't be null", actualConfiguration.getDescription());
			Assert.assertEquals("the configuration descriptions should be equal", expectedConfiguration.getDescription(),
					actualConfiguration.getDescription());
		}
		
		Assert.assertNotNull("parameters are null", actualConfiguration.getParameters());
		Assert.assertEquals("parameters are not equal", expectedConfiguration.getParameters(), actualConfiguration.getParameters());

		final ObjectNode parameters = expectedConfiguration.getParameters();

		final Iterator<Entry<String, JsonNode>> parameterEntriesIter = parameters.fields();

		final ObjectNode responseParameters = actualConfiguration.getParameters();

		Assert.assertNotNull("response parameters shoudln't be null", responseParameters);

		while (parameterEntriesIter.hasNext()) {

			final Entry<String, JsonNode> parameterEntry = parameterEntriesIter.next();

			final String parameterKey = parameterEntry.getKey();

			final JsonNode parameterValueNode = responseParameters.get(parameterKey);

			Assert.assertNotNull("parameter '" + parameterKey + "' is not part of the response configuration parameters", parameterValueNode);

			final String parameterValue = parameterEntry.getValue().asText();

			Assert.assertTrue("the parameter values of '" + parameterKey + "' are not equal. expected = '" + parameterValue + "'; was = '"
					+ parameterValueNode.asText() + "'", parameterValue.equals(parameterValueNode.asText()));
		}
	}
}
