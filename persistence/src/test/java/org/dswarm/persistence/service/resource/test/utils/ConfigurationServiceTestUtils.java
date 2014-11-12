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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import org.junit.Assert;

import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.proxy.ProxyConfiguration;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;

public class ConfigurationServiceTestUtils extends ExtendedBasicDMPJPAServiceTestUtils<ConfigurationService, ProxyConfiguration, Configuration> {

	public ConfigurationServiceTestUtils() {

		super(Configuration.class, ConfigurationService.class);
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert that both {@link Configuration}s have no or equal parameters. TODO compareConfigurations
	 */
	@Override
	public void compareObjects(final Configuration expectedConfiguration, final Configuration actualConfiguration) {

		super.compareObjects(expectedConfiguration, actualConfiguration);

		Assert.assertEquals("parameters are not equal", expectedConfiguration.getParameters(), actualConfiguration.getParameters());

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
		Assert.assertNotNull("updated configuration id shouldn't be null", updatedConfiguration.getId());

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

			newResources = resources;
		}

		object.setResources(newResources);

		return object;
	}

	@Override
	public void reset() {

	}
}
