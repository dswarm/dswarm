/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.persistence.service.resource.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.proxy.ProxyConfiguration;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;

public class ConfigurationServiceTest extends IDBasicJPAServiceTest<ProxyConfiguration, Configuration, ConfigurationService> {

	private static final Logger	LOG				= LoggerFactory.getLogger(ConfigurationServiceTest.class);

	private final ObjectMapper	objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	public ConfigurationServiceTest() {

		super("configuration", ConfigurationService.class);
	}

	@Test
	public void testSimpleConfiguration() {

		final Configuration configuration = createObject().getObject();

		configuration.setName("my configuration");
		configuration.setDescription("configuration description");

		final ObjectNode parameters = new ObjectNode(objectMapper.getNodeFactory());
		final String parameterKey = "fileseparator";
		final String parameterValue = ";";
		parameters.put(parameterKey, parameterValue);

		configuration.setParameters(parameters);

		updateObjectTransactional(configuration);

		final Configuration updatedConfiguration = getObject(configuration);

		Assert.assertNotNull("the configuration name of the updated resource shouldn't be null", updatedConfiguration.getName());
		Assert.assertEquals("the configuration' names of the resource are not equal", configuration.getName(), updatedConfiguration.getName());
		Assert.assertNotNull("the configuration description of the updated resource shouldn't be null", updatedConfiguration.getDescription());
		Assert.assertEquals("the configuration descriptions of the resource are not equal", configuration.getDescription(),
				updatedConfiguration.getDescription());
		Assert.assertNotNull("the configuration parameters of the updated resource shouldn't be null", updatedConfiguration.getParameters());
		Assert.assertEquals("the configurations parameters of the resource are not equal", configuration.getParameters(),
				updatedConfiguration.getParameters());
		Assert.assertNotNull("the parameter value shouldn't be null", configuration.getParameter(parameterKey));
		Assert.assertEquals("the parameter value should be equal", configuration.getParameter(parameterKey).asText(), parameterValue);

		String json = null;

		try {
			json = objectMapper.writeValueAsString(updatedConfiguration);
		} catch (final JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ConfigurationServiceTest.LOG.debug("configuration json: " + json);

		// clean up DB
		deleteObject(configuration.getId());
	}
}
