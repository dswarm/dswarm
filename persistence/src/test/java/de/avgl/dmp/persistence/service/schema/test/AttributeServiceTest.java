package de.avgl.dmp.persistence.service.schema.test;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.service.schema.AttributeService;
import de.avgl.dmp.persistence.service.test.AdvancedJPAServiceTest;


public class AttributeServiceTest extends AdvancedJPAServiceTest<Attribute, AttributeService> {

	private static final org.apache.log4j.Logger	LOG					= org.apache.log4j.Logger.getLogger(AttributeServiceTest.class);

	private final ObjectMapper						objectMapper = injector.getInstance(ObjectMapper.class);

	public AttributeServiceTest() {

		super("attribute", AttributeService.class);
	}

	@Test
	public void testSimpleAttribute() {

		Attribute attribute = createObject("http://purl.org/dc/terms/title");

		attribute.setName("title");

		updateObjectTransactional(attribute);

		Attribute updatedAttribute = getObject(attribute);

		Assert.assertNotNull("the attribute name of the updated resource shouldn't be null", updatedAttribute.getName());
		Assert.assertEquals("the attribute's name are not equal", attribute.getName(), updatedAttribute.getName());

		String json = null;

		try {
			
			json = objectMapper.writeValueAsString(updatedAttribute);
		} catch (JsonProcessingException e) {
			
			e.printStackTrace();
		}

		LOG.debug("attribute json: " + json);

		// clean up DB
		deletedObject(attribute.getId());
	}
}
