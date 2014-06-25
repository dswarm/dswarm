package org.dswarm.persistence.model.schema.test;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Clasz;

public class ClaszTest extends GuicedTest {

	private static final Logger	LOG				= LoggerFactory.getLogger(ClaszTest.class);

	private final ObjectMapper	objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	@Test
	public void simpleClaszTest() {

		final String biboDocumentId = "http://purl.org/ontology/bibo/Document";
		final String biboDocumentName = "document";

		final Clasz biboDocument = new Clasz(biboDocumentId, biboDocumentName);

		Assert.assertNotNull("the clasz id shouldn't be null", biboDocument.getUri());
		Assert.assertEquals("the clasz ids are not equal", biboDocumentId, biboDocument.getUri());
		Assert.assertNotNull("the clasz name shouldn't be null", biboDocument.getName());
		Assert.assertEquals("the clasz names are not equal", biboDocumentName, biboDocument.getName());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(biboDocument);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ClaszTest.LOG.debug("clasz json: " + json);
	}

}
