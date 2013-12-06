package de.avgl.dmp.persistence.model.schema.test;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;


public class AttributePathTest extends GuicedTest {
	

	private static final org.apache.log4j.Logger	LOG					= org.apache.log4j.Logger.getLogger(AttributePathTest.class);
	
	private final ObjectMapper						objectMapper = injector.getInstance(ObjectMapper.class);
	
	@Test
	public void simpleAttributePathTest() {
		
		final String dctermsTitleId = "http://purl.org/dc/terms/title";
		final String dctermsTitleName = "title";
		
		final Attribute dctermsTitle = createAttribute(dctermsTitleId, dctermsTitleName);
		
		final String dctermsHasPartId = "http://purl.org/dc/terms/hasPart";
		final String dctermsHasPartName = "hasPart";
		
		final Attribute dctermsHasPart = createAttribute(dctermsHasPartId, dctermsHasPartName);
		
		final AttributePath attributePath = new AttributePath();
		//attributePath.setId(UUID.randomUUID().toString());
		
		attributePath.addAttribute(dctermsHasPart);
		attributePath.addAttribute(dctermsTitle);

		String json = null;

		try {
			
			json = objectMapper.writeValueAsString(attributePath);
		} catch (JsonProcessingException e) {
			
			e.printStackTrace();
		}

		LOG.debug("attribute path json: " + json);
	}
	
	private Attribute createAttribute(final String id, final String name) {
		
		final Attribute attribute = new Attribute(id);
		attribute.setName(name);
		
		Assert.assertNotNull("the attribute id shouldn't be null", attribute.getId());
		Assert.assertEquals("the attribute ids are not equal", id, attribute.getId());
		Assert.assertNotNull("the attribute name shouldn't be null", attribute.getName());
		Assert.assertEquals("the attribute names are not equal", name, attribute.getName());
		
		return attribute;
	}

}
