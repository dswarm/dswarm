package org.dswarm.persistence.service.resource.test.utils;

import java.util.Set;

import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.proxy.ProxyConfiguration;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;
import org.junit.Assert;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;

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

		final Configuration updatedConfiguration = createObject(configuration, configuration);

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
