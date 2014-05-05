package de.avgl.dmp.persistence.service.resource.test.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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

	public Resource createResource(final String name, final String description, final ResourceType resourceType, final ObjectNode attributes,
			final Set<Configuration> configurations) throws Exception {

		final Resource resource = new Resource();

		resource.setName(name);
		resource.setDescription(description);
		resource.setType(resourceType);
		resource.setAttributes(attributes);
		resource.setConfigurations(configurations);

		final Resource updatedResource = createObject(resource, resource);

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

				if (attributeKey.equals("path") || attributeKey.equals("filesize")) {

					// skip attribute

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
