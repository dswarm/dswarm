package de.avgl.dmp.controller.resources.test.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class ResourceTestUtils {

	public static void compareConfigurations(final Configuration expectedConfiguration, final Configuration actualConfiguration) {

		Assert.assertNotNull("excepted configuration shouldn't be null", expectedConfiguration);
		Assert.assertNotNull("actual configuration shouldn't be null", actualConfiguration);

		if (expectedConfiguration.getName() != null) {

			Assert.assertNotNull("the configuration name shouldn't be null", actualConfiguration.getName());
			Assert.assertEquals("the configuration names should be equal", expectedConfiguration.getName(), actualConfiguration.getName());
		}

		if (expectedConfiguration.getDescription() != null) {

			Assert.assertNotNull("the configuration description shouldn't be null", actualConfiguration.getDescription());
			Assert.assertEquals("the configuration descriptions should be equal", expectedConfiguration.getDescription(),
					actualConfiguration.getDescription());
		}

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

	public static void evaluateConfigurations(final String configurationsJSON, final Set<Configuration> expectedConfigurations) throws Exception {

		Assert.assertNotNull("the resource configurations string", configurationsJSON);

		final Map<Long, Configuration> responseConfigurations = Maps.newLinkedHashMap();
		final ArrayNode responseConfigurationsJSONArray = DMPPersistenceUtil.getJSONObjectMapper().readValue(configurationsJSON, ArrayNode.class);

		Assert.assertNotNull("response configurations JSON array shouldn't be null", responseConfigurationsJSONArray);

		final Iterator<JsonNode> responseConfigurationJSONIter = responseConfigurationsJSONArray.iterator();

		while (responseConfigurationJSONIter.hasNext()) {

			final JsonNode responseConfigurationJSON = responseConfigurationJSONIter.next();

			final Configuration responseConfiguration = DMPPersistenceUtil.getJSONObjectMapper().readValue(
					((ObjectNode) responseConfigurationJSON).toString(), Configuration.class);

			responseConfigurations.put(responseConfiguration.getId(), responseConfiguration);
		}

		compareConfigurations(expectedConfigurations, responseConfigurations);
	}

	public static void compareConfigurations(final Set<Configuration> expectedConfigurations, final Map<Long, Configuration> actualConfigurations) {

		for (final Configuration expectedConfiguration : expectedConfigurations) {

			final Configuration actualConfiguration = actualConfigurations.get(expectedConfiguration.getId());

			Assert.assertNotNull("response configuration for id '" + expectedConfiguration.getId() + "' shouldn't be null", actualConfiguration);
			Assert.assertEquals("configurations are not equal", expectedConfiguration, actualConfiguration);
			
			if (expectedConfiguration.getResources() != null) {
				
				Assert.assertNotNull("configuration resources are null", actualConfiguration.getResources());
			}
			
			Assert.assertNotNull("parameters are null", actualConfiguration.getParameters());
			Assert.assertEquals("parameters are not equal", expectedConfiguration.getParameters(), actualConfiguration.getParameters());

			ResourceTestUtils.compareConfigurations(expectedConfiguration, actualConfiguration);
		}
	}

	public static void compareSchemas(Schema expectedSchema, Schema actualSchema) {
		// TODO Auto-generated method stub
		
	}

	public static void evaluateSchemas(String responseSchemas,
			Set<Schema> expectedSchemas) {
		// TODO Auto-generated method stub
		
	}
}
