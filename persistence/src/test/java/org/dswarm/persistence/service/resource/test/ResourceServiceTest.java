package org.dswarm.persistence.service.resource.test;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.model.resource.proxy.ProxyResource;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.resource.ResourceService;
import org.dswarm.persistence.service.resource.test.utils.ResourceServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ResourceServiceTest extends IDBasicJPAServiceTest<ProxyResource, Resource, ResourceService> {

	private static final Logger				LOG				= LoggerFactory.getLogger(ResourceServiceTest.class);

	final String							attributeKey	= "path";
	final String							attributeValue	= "/path/to/file.end";

	private final ResourceServiceTestUtils	resourceServiceTestUtils;

	public ResourceServiceTest() {

		super("resource", ResourceService.class);

		resourceServiceTestUtils = new ResourceServiceTestUtils();
	}

	@Test
	public void testSimpleResource() {

		final Resource resource = createSimpleResource();

		updateObjectTransactional(resource);

		final Resource updatedResource = getObject(resource);

		checkSimpleResource(resource, updatedResource);

		// clean up DB
		deleteObject(resource.getId());
	}

	@Test
	public void testComplexResource() {

		final Resource resource = createSimpleResource();

		final ConfigurationService configurationService = GuicedTest.injector.getInstance(ConfigurationService.class);

		Assert.assertNotNull("configuration service shouldn't be null", configurationService);

		// create first configuration

		Configuration configuration = null;

		try {
			configuration = configurationService.createObjectTransactional().getObject();
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

		resourceServiceTestUtils.checkComplexResource(resource, updatedResource, parameterKey, parameterValue);

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

		resourceServiceTestUtils.checkComplexResource(updatedResource, updatedResource2, parameterKey, modifiedParameterValue);

		// create second configuration

		Configuration configuration2 = null;

		try {
			configuration2 = configurationService.createObjectTransactional().getObject();
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

		resourceServiceTestUtils.checkComplexResource(updatedResource2, updatedResource3);

		String resourceJSON = null;

		try {

			resourceJSON = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(updatedResource3);
		} catch (final JsonProcessingException e) {

			ResourceServiceTest.LOG.debug("couldn't transform resource object to JSON string.\n" + e.getMessage());
		}

		ResourceServiceTest.LOG.debug("resource configurations size: " + updatedResource3.getConfigurations().size());
		ResourceServiceTest.LOG.debug("resource JSON: " + resourceJSON);

		// clean up DB
		configurationService.deleteObject(configurationId);
		configurationService.deleteObject(configuration2Id);
		deleteObject(resource.getId());

		final Configuration deletedConfiguration = configurationService.getObject(configurationId);

		Assert.assertNull("the deleted configuration shouldn't exist any more", deletedConfiguration);

		final Configuration deletedConfiguration2 = configurationService.getObject(configuration2Id);

		Assert.assertNull("the deleted configuration shouldn't exist any more", deletedConfiguration2);
	}

	private Resource createSimpleResource() {

		final Resource resource = createObject().getObject();

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
}
