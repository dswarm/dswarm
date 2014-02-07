package de.avgl.dmp.persistence.service.resource.test.utils;

import java.util.Iterator;
import java.util.Map.Entry;

import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.proxy.ProxyConfiguration;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;
import de.avgl.dmp.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;

public class ConfigurationServiceTestUtils extends ExtendedBasicDMPJPAServiceTestUtils<ConfigurationService, ProxyConfiguration, Configuration> {

	public ConfigurationServiceTestUtils() {

		super(Configuration.class, ConfigurationService.class);
	}

	@Override
	public void compareObjects(final Configuration expectedObject, final Configuration actualObject) {

		super.compareObjects(expectedObject, actualObject);

		compareConfigurations(expectedObject, actualObject);
	}

	private void compareConfigurations(final Configuration expectedConfiguration, final Configuration actualConfiguration) {

		Assert.assertNotNull("parameters are null", actualConfiguration.getParameters());
		Assert.assertEquals("parameters are not equal", expectedConfiguration.getParameters(), actualConfiguration.getParameters());

		final ObjectNode parameters = expectedConfiguration.getParameters();

		final Iterator<Entry<String, JsonNode>> parameterEntriesIter = parameters.fields();

		final ObjectNode responseParameters = actualConfiguration.getParameters();

		Assert.assertNotNull("response parameters shouldn't be null", responseParameters);

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
	
	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, description, resources and parameters of the configuration.
	 */
	@Override
	protected Configuration prepareObjectForUpdate(final Configuration objectWithUpdates, final Configuration object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		final ObjectNode parameters = objectWithUpdates.getParameters();

		object.setParameters(parameters);

		object.setResources(objectWithUpdates.getResources());

		return object;
	}

	@Override
	public void reset() {

	}
}
