package de.avgl.dmp.controller.resources.test;

import java.io.IOException;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

import de.avgl.dmp.controller.resources.test.utils.ResourceTestUtils;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.services.ConfigurationService;
import de.avgl.dmp.persistence.services.ResourceService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class ConfigurationsResourceTest extends ResourceTest {

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(ConfigurationsResourceTest.class);

	private String									configurationJSONString	= null;
	private Configuration							expectedConfiguration	= null;
	private Set<Configuration>						expectedConfigurations	= null;

	private final ConfigurationService				configurationService = injector.getInstance(ConfigurationService.class);

	private final ObjectMapper						objectMapper = injector.getInstance(ObjectMapper.class);


	public ConfigurationsResourceTest() {
		super("configurations");
	}

	@Before
	public void prepare() throws IOException {
		configurationJSONString = DMPPersistenceUtil.getResourceAsString("configuration.json");
		expectedConfiguration = DMPPersistenceUtil.getJSONObjectMapper().readValue(configurationJSONString, Configuration.class);
	}

	@Test
	public void testPOSTConfigurations() throws Exception {

		final Configuration actualConfiguration = createConfigurationInternal();

		cleanUpDB(actualConfiguration);
	}

	@Test
	public void testGETConfigurations() throws Exception {

		final Configuration actualConfiguration = createConfigurationInternal();

		LOG.debug("try to retrieve configurations");

		final Response response = target().request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseConfigurations = response.readEntity(String.class);

		expectedConfigurations = Sets.newHashSet();
		expectedConfigurations.add(actualConfiguration);

		ResourceTestUtils.evaluateConfigurations(responseConfigurations, expectedConfigurations);

		cleanUpDB(actualConfiguration);
	}

	@Test
	public void testGETConfiguration() throws Exception {

		final Configuration actualConfiguration = createConfigurationInternal();

		LOG.debug("try to retrieve configurations");

		final Response response = target(String.valueOf(actualConfiguration.getId())).request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseConfigurationJSON = response.readEntity(String.class);

		Assert.assertNotNull("response configuration JSON shouldn't be null", responseConfigurationJSON);

		final Configuration responseConfiguration = objectMapper
				.readValue(responseConfigurationJSON, Configuration.class);

		Assert.assertNotNull("response configuration shouldn't be null", responseConfiguration);

		ResourceTestUtils.compareConfigurations(actualConfiguration, responseConfiguration);

		cleanUpDB(responseConfiguration);
	}

	private Configuration createConfigurationInternal() throws Exception {

		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(configurationJSONString));

		Assert.assertEquals("201 Created was expected", 201, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final Configuration actualConfiguration = objectMapper.readValue(responseString, Configuration.class);

		ResourceTestUtils.compareConfigurations(expectedConfiguration, actualConfiguration);

		return actualConfiguration;
	}

	private void cleanUpDB(final Configuration configuration) {

		// clean-up DB

		final Long configurationId = configuration.getId();

		configurationService.deleteObject(configurationId);

		final Configuration deletedConfiguration = configurationService.getObject(configurationId);

		Assert.assertNull("the deleted configuration should be null", deletedConfiguration);
	}
}
