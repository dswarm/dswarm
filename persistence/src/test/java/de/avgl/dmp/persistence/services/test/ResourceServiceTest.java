package de.avgl.dmp.persistence.services.test;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.init.util.DMPUtil;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.model.test.BasicJPAServiceTest;
import de.avgl.dmp.persistence.services.ResourceService;

public class ResourceServiceTest extends BasicJPAServiceTest<Resource, ResourceService> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ResourceServiceTest.class);

	public ResourceServiceTest() {

		super("resource", ResourceService.class);
	}

	@Test
	public void testSimpleResource() {

		Resource resource = createObject();

		resource.setName("bla");
		resource.setDescription("blubblub");
		resource.setType(ResourceType.FILE);

		final ObjectNode attributes = new ObjectNode(DMPUtil.getJSONFactory());
		final String attributeKey = "path";
		final String attributeValue = "/path/to/file.end";
		attributes.put(attributeKey, attributeValue);

		resource.setAttributes(attributes);

		updateObject(resource);

		Resource updatedResource = getUpdatedObject(resource);

		Assert.assertNotNull("the name of the updated resource shouldn't be null", updatedResource.getName());
		Assert.assertEquals("the names of the resource are not equal", resource.getName(), updatedResource.getName());
		Assert.assertNotNull("the description of the updated resource shouldn't be null", updatedResource.getDescription());
		Assert.assertEquals("the descriptions of the resource are not equal", resource.getDescription(), updatedResource.getDescription());
		Assert.assertNotNull("the type of the updated resource shouldn't be null", updatedResource.getType());
		Assert.assertEquals("the types of the resource are not equal", resource.getType(), updatedResource.getType());
		Assert.assertNotNull("the attribute of the updated resource shouldn't be null", updatedResource.getAttributes());
		Assert.assertEquals("the attributes of the resource are not equal", resource.getAttributes(), updatedResource.getAttributes());
		Assert.assertNotNull("the attribute value shouldn't be null", resource.getAttribute(attributeKey));
		Assert.assertEquals("the attribute value should be equal", resource.getAttribute(attributeKey).asText(), attributeValue);

		// clean up DB
		deletedObject(resource.getId());
	}
}
