package de.avgl.dmp.persistence.model.resource.test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * Created by tgaengler on 21/05/14.
 */
public class ConfigurationTest extends GuicedTest {

	private final ObjectMapper	objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	@Test
	public void simpleConfigurationTest() throws IOException {

		final String configurationJSONString = DMPPersistenceUtil.getResourceAsString("configuration.json");

		final Configuration configuration = objectMapper.readValue(configurationJSONString, Configuration.class);

		final ObjectNode configurationJSON = objectMapper.readValue(configurationJSONString, ObjectNode.class);

		final String finalExpectedConfigurationJSONString = objectMapper.writeValueAsString(configurationJSON);
		final String finalActualConfigurationJSONString = objectMapper.writeValueAsString(configuration);

		Assert.assertEquals(finalExpectedConfigurationJSONString.length(), finalActualConfigurationJSONString.length());
	}
}
