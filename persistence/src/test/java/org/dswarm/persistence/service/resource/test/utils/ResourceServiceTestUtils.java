package org.dswarm.persistence.service.resource.test.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.model.resource.proxy.ProxyResource;
import org.dswarm.persistence.service.resource.ResourceService;
import org.dswarm.persistence.service.test.utils.BasicJPAServiceTestUtils;
import org.dswarm.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;
import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ResourceServiceTestUtils extends ExtendedBasicDMPJPAServiceTestUtils<ResourceService, ProxyResource, Resource> {

	private final ConfigurationServiceTestUtils	configurationsResourceTestUtils;

	public ResourceServiceTestUtils() {

		super(Resource.class, ResourceService.class);

		configurationsResourceTestUtils = new ConfigurationServiceTestUtils();
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert that both {@link Resource}s have either no attributes or equal collections, i.e. the same number of attributes and
	 * the same pairs of keys and values; the attributes with keys 'path' and 'filesize' are not part of the comparison. <br />
	 * Assert that both {@link Resource}s have either no or equal configurations, see
	 * {@link BasicJPAServiceTestUtils#compareObjects(Set, Map)} for details.
	 */
	@Override
	public void compareObjects(final Resource expectedResource, final Resource actualResource) {

		super.compareObjects(expectedResource, actualResource);

		final ObjectNode expectedAttributes = expectedResource.getAttributes();
		final ObjectNode actualAttributes = actualResource.getAttributes();

		// compare Attributes
		if (expectedAttributes == null) {

			Assert.assertNull("actual resource shouldn't have attributes", actualAttributes);

		} else {

			Assert.assertNotNull("actual attributes shouldn't be null", actualAttributes);

			Assert.assertEquals("different number of attributes.", expectedAttributes.size(), actualAttributes.size());

			final Iterator<Entry<String, JsonNode>> expectedAttributeEntriesIter = expectedAttributes.fields();

			while (expectedAttributeEntriesIter.hasNext()) {

				final Entry<String, JsonNode> expectedAttributeEntry = expectedAttributeEntriesIter.next();

				final String expectedAttributeKey = expectedAttributeEntry.getKey();

				final JsonNode actualAttributeValueNode = actualAttributes.get(expectedAttributeKey);

				if (expectedAttributeKey.equals("path")) {

					Assert.assertNotNull("the actual resource should have a path attribute", actualAttributeValueNode);

					// skip comparison of attribute values
					continue;
				}
				if (expectedAttributeKey.equals("filesize")) {

					Assert.assertNotNull("the actual resource should have a filesize attribute", actualAttributeValueNode);

					// skip comparison of attribute values
					continue;
				}

				Assert.assertNotNull("attribute '" + expectedAttributeKey + "' is not part of the actual resource's attributes",
						actualAttributeValueNode);

				final String expectedAttributeValue = expectedAttributeEntry.getValue().asText();

				Assert.assertEquals("the attribute values of '" + expectedAttributeKey + "' are not equal. ", expectedAttributeValue,
						actualAttributeValueNode.asText());

			}
		}

		// compare configurations
		if (expectedResource.getConfigurations() == null || expectedResource.getConfigurations().isEmpty()) {

			final boolean actualResourceHasNoConfiguration = (actualResource.getConfigurations() == null || actualResource.getConfigurations()
					.isEmpty());
			Assert.assertTrue("the actual resource shouldn't have any configurations", actualResourceHasNoConfiguration);

		} else { // !null && !empty

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

	public Resource createResource(final String name, final String description, final ResourceType resourceType, final ObjectNode attributes,
			final Set<Configuration> configurations) throws Exception {

		final Resource resource = new Resource();

		resource.setName(name);
		resource.setDescription(description);
		resource.setType(resourceType);
		resource.setAttributes(attributes);
		resource.setConfigurations(configurations);

		// clone resource since it gets it configuration removed while beeing prepared for creation (update)
		final String resourceJSONString = objectMapper.writeValueAsString(resource);
		final Resource expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

		final Resource updatedResource = createObject(resource, expectedResource);

		Assert.assertNotNull("updated resource shouldn't be null", updatedResource);
		Assert.assertNotNull("updated resource id shouldn't be null", updatedResource.getId());

		return updatedResource;
	}

	/**
	 * note: legacy
	 * 
	 * @param resource
	 * @param updatedResource
	 * @param attributeKey
	 * @param attributeValue
	 */
	public void checkSimpleResource(final Resource resource, final Resource updatedResource, final String attributeKey, final String attributeValue) {

		Assert.assertNotNull("the name of the updated resource shouldn't be null", updatedResource.getName());
		Assert.assertEquals("the names of the resource are not equal", resource.getName(), updatedResource.getName());
		Assert.assertNotNull("the description of the updated resource shouldn't be null", updatedResource.getDescription());
		Assert.assertEquals("the descriptions of the resource are not equal", resource.getDescription(), updatedResource.getDescription());
		Assert.assertNotNull("the type of the updated resource shouldn't be null", updatedResource.getType());
		Assert.assertEquals("the types of the resource are not equal", resource.getType(), updatedResource.getType());
		Assert.assertNotNull("the attributes of the updated resource shouldn't be null", updatedResource.getAttributes());
		Assert.assertEquals("the attributes of the resource are not equal", resource.getAttributes(), updatedResource.getAttributes());
		Assert.assertNotNull("the attribute value shouldn't be null", resource.getAttribute(attributeKey));
		Assert.assertEquals("the attribute value should be equal", resource.getAttribute(attributeKey).asText(), attributeValue);
	}

	/**
	 * note: legacy
	 * 
	 * @param resource
	 * @param updatedResource
	 * @param parameterKey
	 * @param parameterValue
	 */
	public void checkComplexResource(final Resource resource, final Resource updatedResource, final String parameterKey, final String parameterValue) {

		checkComplexResource(resource, updatedResource);

		Assert.assertEquals("the configuration of the resource is not equal", resource.getConfigurations().iterator().next(), resource
				.getConfigurations().iterator().next());
		Assert.assertEquals("the configuration parameter '" + parameterKey + "' of the resource is not equal", resource.getConfigurations()
				.iterator().next().getParameter(parameterKey), resource.getConfigurations().iterator().next().getParameter(parameterKey));
		Assert.assertEquals("the configuration parameter value for '" + parameterKey + "' of the resource is not equal", resource.getConfigurations()
				.iterator().next().getParameter(parameterKey).asText(), resource.getConfigurations().iterator().next().getParameter(parameterKey)
				.asText());
	}

	/**
	 * note: legacy
	 * 
	 * @param resource
	 * @param updatedResource
	 */
	public void checkComplexResource(final Resource resource, final Resource updatedResource) {

		Assert.assertNotNull("the configurations of the updated resource shouldn't be null", updatedResource.getConfigurations());
		Assert.assertEquals("the configurations of the resource are not equal", resource.getConfigurations(), updatedResource.getConfigurations());
		Assert.assertEquals("the configurations' size of the resource are not equal", resource.getConfigurations().size(), updatedResource
				.getConfigurations().size());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Resource prepareObjectForUpdate(final Resource objectWithUpdates, final Resource object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		final Set<Configuration> configurations = objectWithUpdates.getConfigurations();
		final Set<Configuration> newConfigurations;

		if (configurations != null) {

			newConfigurations = Sets.newCopyOnWriteArraySet();

			for (final Configuration configuration : configurations) {

				configuration.removeResource(objectWithUpdates);
				configuration.addResource(object);

				newConfigurations.add(configuration);
			}
		} else {

			newConfigurations = configurations;
		}

		object.setConfigurations(newConfigurations);

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
