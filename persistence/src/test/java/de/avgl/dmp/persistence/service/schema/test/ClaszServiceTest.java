package de.avgl.dmp.persistence.service.schema.test;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.service.schema.ClaszService;
import de.avgl.dmp.persistence.service.test.AdvancedJPAServiceTest;

public class ClaszServiceTest extends AdvancedJPAServiceTest<Clasz, ClaszService> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(ClaszServiceTest.class);

	private final ObjectMapper						objectMapper	= injector.getInstance(ObjectMapper.class);

	public ClaszServiceTest() {

		super("class", ClaszService.class);
	}

	@Test
	public void testSimpleAttribute() {

		Clasz clasz = createObject("http://purl.org/ontology/bibo/Document");

		clasz.setName("document");

		updateObjectTransactional(clasz);

		Clasz updatedClass = getObject(clasz);

		Assert.assertNotNull("the attribute name of the updated resource shouldn't be null", updatedClass.getName());
		Assert.assertEquals("the attribute's name are not equal", clasz.getName(), updatedClass.getName());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedClass);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("class json: " + json);

		// clean up DB
		deletedObject(clasz.getId());
	}
}
