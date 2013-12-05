package de.avgl.dmp.persistence.service.resource.test;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.persist.Transactional;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;
import de.avgl.dmp.persistence.service.resource.ResourceService;
import de.avgl.dmp.persistence.service.test.IDBasicJPAServiceTest;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class ResourceServiceTest extends IDBasicJPAServiceTest<Resource, ResourceService, Long> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(ResourceServiceTest.class);

	final String									attributeKey	= "path";
	final String									attributeValue	= "/path/to/file.end";

	public ResourceServiceTest() {

		super("resource", ResourceService.class);
	}

	@Test
	public void testSimpleResource() {

		Resource resource = createSimpleResource();

		updateObjectTransactional(resource);

		Resource updatedResource = getObject(resource);

		checkSimpleResource(resource, updatedResource);

		// clean up DB
		deletedObject(resource.getId());
	}

	@Test
	public void testComplexResource() {

		final Resource resource = createSimpleResource();

		final ConfigurationService configurationService = injector.getInstance(ConfigurationService.class);

		Assert.assertNotNull("configuration service shouldn't be null", configurationService);

		// create first configuration

		Configuration configuration = null;

		try {
			configuration = configurationService.createObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while configuration creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("configuration shouldn't be null", configuration);
		Assert.assertNotNull("configuration id shouldn't be null", configuration.getId());

		final Long configurationId = configuration.getId();

		final ObjectNode parameters = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
		final String parameterKey = "fieldseparator";
		final String parameterValue = ";";
		parameters.put(parameterKey, parameterValue);

		configuration.setParameters(parameters);

		// add configuration to resource

		resource.addConfiguration(configuration);

		// update resource

		updateObjectTransactional(resource);

		final Resource updatedResource = getObject(resource);

		checkSimpleResource(resource, updatedResource);

		checkComplexResource(resource, updatedResource, parameterKey, parameterValue);

		// modify first configuration

		final String modifiedParameterValue = "|";

		parameters.put(parameterKey, modifiedParameterValue);

		configuration.setParameters(parameters);

		// replace configuration

		updatedResource.replaceConfiguration(configuration);

		// update resource

		updateObjectTransactional(updatedResource);

		final Resource updatedResource2 = getObject(updatedResource);

		checkSimpleResource(updatedResource, updatedResource2);

		checkComplexResource(updatedResource, updatedResource2, parameterKey, modifiedParameterValue);

		// create second configuration

		Configuration configuration2 = null;

		try {
			configuration2 = configurationService.createObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while configuration creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("configuration shouldn't be null", configuration2);
		Assert.assertNotNull("configuration id shouldn't be null", configuration2.getId());

		final Long configuration2Id = configuration2.getId();

		final ObjectNode parameters2 = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
		final String parameterKey2 = "lineseparator";
		final String parameterValue2 = "\n";
		parameters2.put(parameterKey2, parameterValue2);

		configuration2.setParameters(parameters2);

		// add configuration to resource

		updatedResource2.addConfiguration(configuration2);

		// update resource

		updateObjectTransactional(updatedResource2);

		final Resource updatedResource3 = getObject(updatedResource2);

		checkSimpleResource(updatedResource2, updatedResource3);

		checkComplexResource(updatedResource2, updatedResource3);

		String resourceJSON = null;

		try {

			resourceJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(updatedResource3);
		} catch (final JsonProcessingException e) {

			LOG.debug("couldn't transform resource object to JSON string.\n" + e.getMessage());
		}

		System.out.println("resource configurations size: " + updatedResource3.getConfigurations().size());
		System.out.println("resource JSON: " + resourceJSON);

		// clean up DB
		configurationService.deleteObject(configurationId);
		configurationService.deleteObject(configuration2Id);
		deletedObject(resource.getId());

		final Configuration deletedConfiguration = configurationService.getObject(configurationId);

		Assert.assertNull("the deleted configuration shouldn't exist any more", deletedConfiguration);

		final Configuration deletedConfiguration2 = configurationService.getObject(configuration2Id);

		Assert.assertNull("the deleted configuration shouldn't exist any more", deletedConfiguration2);
	}

	private Resource createSimpleResource() {

		final Resource resource = createObject();

		resource.setName("bla");
		resource.setDescription("blubblub");
		resource.setType(ResourceType.FILE);

		final ObjectNode attributes = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
		attributes.put(attributeKey, attributeValue);

		resource.setAttributes(attributes);

		return resource;
	}

	private void checkSimpleResource(final Resource resource, final Resource updatedResource) {

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

	private void checkComplexResource(final Resource resource, final Resource updatedResource, final String parameterKey, final String parameterValue) {

		checkComplexResource(resource, updatedResource);

		Assert.assertEquals("the configuration of the resource is not equal", resource.getConfigurations().iterator().next(), resource
				.getConfigurations().iterator().next());
		Assert.assertEquals("the configuration parameter '" + parameterKey + "' of the resource is not equal", resource.getConfigurations()
				.iterator().next().getParameter(parameterKey), resource.getConfigurations().iterator().next().getParameter(parameterKey));
		Assert.assertEquals("the configuration parameter value for '" + parameterKey + "' of the resource is not equal", resource.getConfigurations()
				.iterator().next().getParameter(parameterKey).asText(), resource.getConfigurations().iterator().next().getParameter(parameterKey)
				.asText());
	}

	private void checkComplexResource(final Resource resource, final Resource updatedResource) {

		Assert.assertNotNull("the configurations of the updated resource shouldn't be null", updatedResource.getConfigurations());
		Assert.assertEquals("the configurations of the resource are not equal", resource.getConfigurations(), updatedResource.getConfigurations());
		Assert.assertEquals("the configurations' size of the resource are not equal", resource.getConfigurations().size(), updatedResource.getConfigurations().size());
	}
}
