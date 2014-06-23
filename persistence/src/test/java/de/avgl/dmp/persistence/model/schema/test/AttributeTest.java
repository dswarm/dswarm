package de.avgl.dmp.persistence.model.schema.test;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.schema.Attribute;

public class AttributeTest extends GuicedTest {

	private static final Logger	LOG				= LoggerFactory.getLogger(AttributeTest.class);

	private final ObjectMapper	objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	@Test
	public void simpleAttributeTest() {

		final String attributeId = "http://purl.org/dc/terms/title";
		final String attributeName = "title";

		final Attribute dctermsTitle = new Attribute(attributeId);
		dctermsTitle.setName(attributeName);

		Assert.assertNotNull("the attribute id shouldn't be null", dctermsTitle.getUri());
		Assert.assertEquals("the attribute ids are not equal", attributeId, dctermsTitle.getUri());
		Assert.assertNotNull("the attribute name shouldn't be null", dctermsTitle.getName());
		Assert.assertEquals("the attribute names are not equal", attributeName, dctermsTitle.getName());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(dctermsTitle);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		AttributeTest.LOG.debug("attribute json: " + json);
	}

}
