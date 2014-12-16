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
package org.dswarm.persistence.service.resource.test.utils;

import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import org.json.JSONException;
import org.junit.Assert;
import org.skyscreamer.jsonassert.JSONAssert;

import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.proxy.ProxyConfiguration;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class ConfigurationServiceTestUtils extends ExtendedBasicDMPJPAServiceTestUtils<ConfigurationService, ProxyConfiguration, Configuration> {

	private final ResourceServiceTestUtils resourceServiceTestUtils;

	public ConfigurationServiceTestUtils() {

		super(Configuration.class, ConfigurationService.class);

		resourceServiceTestUtils = new ResourceServiceTestUtils(this);
	}

	public ConfigurationServiceTestUtils(final ResourceServiceTestUtils resourceServiceTestUtilsArg) {

		super(Configuration.class, ConfigurationService.class);

		resourceServiceTestUtils = resourceServiceTestUtilsArg;
	}

	@Override public Configuration createObject(JsonNode objectDescription) throws Exception {
		return null;
	}

	@Override public Configuration createObject(String identifier) throws Exception {
		return null;
	}

	@Override public Configuration createAndPersistDefaultObject() throws Exception {

		final ObjectNode parameters = new ObjectNode(objectMapper.getNodeFactory());
		final String parameterValue = ";";
		parameters.put(ConfigurationStatics.COLUMN_DELIMITER, parameterValue);

		return createConfiguration("my configuration", "configuration description", parameters);
	}

	@Override public Configuration createDefaultObject() throws Exception {
		return null;
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert that both {@link Configuration}s have no or equal parameters. TODO compareConfigurations
	 */
	@Override
	public void compareObjects(final Configuration expectedConfiguration, final Configuration actualConfiguration)
			throws JsonProcessingException, JSONException {

		super.compareObjects(expectedConfiguration, actualConfiguration);

		if(expectedConfiguration.getParameters() == null) {

			Assert.assertNull(actualConfiguration.getParameters());
		} else {

			Assert.assertNotNull(actualConfiguration.getParameters());

			final String expectedParametersJSONString = objectMapper.writeValueAsString(expectedConfiguration.getParameters());
			final String actualParametersJSONString = objectMapper.writeValueAsString(actualConfiguration.getParameters());

			JSONAssert.assertEquals(expectedParametersJSONString, actualParametersJSONString, false);
		}

		// TODO SR: we may compare resources here but need to make sure not to run into cycles since resources link to
		// configurations...
	}

	public Configuration createConfiguration(final String name, final String description, final ObjectNode parameters) throws Exception {

		final Configuration configuration = new Configuration();

		configuration.setName(name);
		configuration.setDescription(description);
		configuration.setParameters(parameters);

		final Configuration updatedConfiguration = createAndCompareObject(configuration, configuration);

		Assert.assertNotNull("updated configuration shouldn't be null", updatedConfiguration);
		Assert.assertNotNull("updated configuration id shouldn't be null", updatedConfiguration.getUuid());

		return updatedConfiguration;
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

		final Set<Resource> resources = objectWithUpdates.getResources();
		final Set<Resource> newResources;

		if (resources != null) {

			newResources = Sets.newCopyOnWriteArraySet();

			for (final Resource resource : resources) {

				resource.removeConfiguration(objectWithUpdates);
				resource.addConfiguration(object);

				newResources.add(resource);
			}
		} else {

			newResources = null;
		}

		object.setResources(newResources);

		return object;
	}

	@Override
	public void reset() {

	}

	public Configuration getAlternativeConfiguration() throws Exception {

		final ObjectNode parameters2 = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
		final String parameterKey2 = "lineseparator";
		final String parameterValue2 = "\n";
		parameters2.put(parameterKey2, parameterValue2);

		return createConfiguration(null, null, parameters2);
	}
}
