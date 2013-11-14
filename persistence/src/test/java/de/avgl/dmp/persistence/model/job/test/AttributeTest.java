package de.avgl.dmp.persistence.model.job.test;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.job.Attribute;
import de.avgl.dmp.persistence.services.test.AttributeServiceTest;


public class AttributeTest extends GuicedTest {
	
	private static final org.apache.log4j.Logger	LOG					= org.apache.log4j.Logger.getLogger(AttributeTest.class);
	
	private final ObjectMapper						objectMapper = injector.getInstance(ObjectMapper.class);
	
	@Test
	public void simpleAttributeTest() {
		
		final String attributeId = "http://purl.org/dc/terms/title";
		final String attributeName = "title";
		
		final Attribute dctermsTitle = new Attribute(attributeId);
		dctermsTitle.setName(attributeName);
		
		Assert.assertNotNull("the attribute id shouldn't be null", dctermsTitle.getId());
		Assert.assertEquals("the attribute ids are not equal", attributeId, dctermsTitle.getId());
		Assert.assertNotNull("the attribute name shouldn't be null", dctermsTitle.getName());
		Assert.assertEquals("the attribute names are not equal", attributeName, dctermsTitle.getName());

		String json = null;

		try {
			
			json = objectMapper.writeValueAsString(dctermsTitle);
		} catch (JsonProcessingException e) {
			
			e.printStackTrace();
		}

		LOG.debug("attribute json: " + json);
	}

}
