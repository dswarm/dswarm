package de.avgl.dmp.persistence.service.resource.test.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;

import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.model.resource.proxy.ProxyResource;
import de.avgl.dmp.persistence.service.resource.ResourceService;
import de.avgl.dmp.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;

public class ResourceServiceTestUtils extends ExtendedBasicDMPJPAServiceTestUtils<ResourceService, ProxyResource, Resource> {

	private final ConfigurationServiceTestUtils	configurationsResourceTestUtils;

	public ResourceServiceTestUtils() {

		super(Resource.class, ResourceService.class);

		configurationsResourceTestUtils = new ConfigurationServiceTestUtils();
	}

	@Override
	public void compareObjects(final Resource expectedObject, final Resource actualObject) {

		super.compareObjects(expectedObject, actualObject);

		compareResources(expectedObject, actualObject);
	}

	private void compareResources(final Resource expectedResource, final Resource actualResource) {

		if (expectedResource.getAttributes() != null) {

			Assert.assertNotNull("attributes are null", actualResource.getAttributes());

			final ObjectNode attributes = expectedResource.getAttributes();

			final Iterator<Entry<String, JsonNode>> attributeEntriesIter = attributes.fields();

			final ObjectNode responseAttributes = actualResource.getAttributes();

			Assert.assertNotNull("response attributes shouldn't be null", responseAttributes);

			while (attributeEntriesIter.hasNext()) {

				final Entry<String, JsonNode> attributeEntry = attributeEntriesIter.next();

				final String attributeKey = attributeEntry.getKey();

				if (attributeKey.equals("path")) {

					// skip path attribute

					continue;
				}

				final JsonNode attributeValueNode = responseAttributes.get(attributeKey);

				Assert.assertNotNull("attribute '" + attributeKey + "' is not part of the response resource attributes", attributeValueNode);

				final String attributeValue = attributeEntry.getValue().asText();

				Assert.assertTrue("the attribute values of '" + attributeKey + "' are not equal. expected = '" + attributeValue + "'; was = '"
						+ attributeValueNode.asText() + "'", attributeValue.equals(attributeValueNode.asText()));
			}
		}

		if (expectedResource.getConfigurations() != null && !expectedResource.getConfigurations().isEmpty()) {

			final Set<Configuration> actualConfigurations = actualResource.getConfigurations();

			Assert.assertNotNull("configurations of actual resource '" + actualResource.getId() + "' shouldn't be null", actualConfigurations);
			Assert.assertFalse("configurations of actual resource '" + actualResource.getId() + "' shouldn't be empty",
					actualConfigurations.isEmpty());

			final Map<Long, Configuration> actualConfigurationsMap = Maps.newHashMap();

			for (final Configuration actualConfiguration : actualConfigurations) {

				actualConfigurationsMap.put(actualConfiguration.getId(), actualConfiguration);
			}

			configurationsResourceTestUtils.compareObjects(expectedResource.getConfigurations(), actualConfigurationsMap);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Resource prepareObjectForUpdate(final Resource objectWithUpdates, final Resource object) {

		super.prepareObjectForUpdate(object, objectWithUpdates);

		final Set<Configuration> configurations = objectWithUpdates.getConfigurations();

		object.setConfigurations(configurations);

		final ResourceType type = objectWithUpdates.getType();

		object.setType(type);

		final ObjectNode attributes = objectWithUpdates.getAttributes();

		object.setAttributes(attributes);

		return object;
	}

	@Override
	public void reset() {

		configurationsResourceTestUtils.reset();
	}
}
