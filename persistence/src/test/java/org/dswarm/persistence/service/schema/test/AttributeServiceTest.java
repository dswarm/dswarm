package org.dswarm.persistence.service.schema.test;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.test.AdvancedJPAServiceTest;

public class AttributeServiceTest extends AdvancedJPAServiceTest<ProxyAttribute, Attribute, AttributeService> {

	private static final Logger	LOG				= LoggerFactory.getLogger(AttributeServiceTest.class);

	private final ObjectMapper	objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	public AttributeServiceTest() {

		super("attribute", AttributeService.class);
	}

	@Test
	public void testSimpleAttribute() {

		final Attribute attribute = createObject("http://purl.org/dc/terms/title").getObject();

		attribute.setName("title");

		updateObjectTransactional(attribute);

		final Attribute updatedAttribute = getObject(attribute);

		Assert.assertNotNull("the attribute name of the updated resource shouldn't be null", updatedAttribute.getName());
		Assert.assertEquals("the attribute's name are not equal", attribute.getName(), updatedAttribute.getName());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedAttribute);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		AttributeServiceTest.LOG.debug("attribute json: " + json);

		// clean up DB
		deleteObject(attribute.getId());
	}

	@Test
	public void testUniquenessOfAttributes() {

		final Attribute attribute1 = createAttribute();
		final Attribute attribute2 = createAttribute();

		Assert.assertNotNull("attribute1 shouldn't be null", attribute1);
		Assert.assertNotNull("attribute2 shouldn't be null", attribute2);
		Assert.assertNotNull("attribute1 id shouldn't be null", attribute1.getId());
		Assert.assertNotNull("attribute2 id shouldn't be null", attribute2.getId());
		Assert.assertEquals("the attributes should be equal", attribute1, attribute2);
		Assert.assertNotNull("attribute1 uri shouldn't be null", attribute1.getUri());
		Assert.assertNotNull("attribute2 uri shouldn't be null", attribute2.getUri());
		Assert.assertNotNull("attribute1 uri shouldn't be empty", attribute1.getUri().trim().isEmpty());
		Assert.assertNotNull("attribute2 uri shouldn't be empty", attribute2.getUri().trim().isEmpty());
		Assert.assertEquals("the attribute uris should be equal", attribute1.getUri(), attribute2.getUri());
		Assert.assertNotNull("attribute1 uri shouldn't be null", attribute1.getName());
		Assert.assertNotNull("attribute2 uri shouldn't be null", attribute2.getName());
		Assert.assertNotNull("attribute1 uri shouldn't be empty", attribute1.getName().trim().isEmpty());
		Assert.assertNotNull("attribute2 uri shouldn't be empty", attribute2.getName().trim().isEmpty());
		Assert.assertEquals("the attribute uris should be equal", attribute1.getName(), attribute2.getName());

		// clean up DB
		deleteObject(attribute1.getId());
	}

	private Attribute createAttribute() {

		final Attribute attribute = createObject("http://purl.org/dc/terms/title").getObject();

		attribute.setName("title");

		updateObjectTransactional(attribute);

		final Attribute updatedAttribute = getObject(attribute);

		return updatedAttribute;
	}
}
