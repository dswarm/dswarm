package org.dswarm.persistence.service.schema.test;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.proxy.ProxyClasz;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.test.utils.ClaszServiceTestUtils;
import org.dswarm.persistence.service.test.AdvancedJPAServiceTest;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClaszServiceTest extends AdvancedJPAServiceTest<ProxyClasz, Clasz, ClaszService> {

	private static final Logger			LOG				= LoggerFactory.getLogger(ClaszServiceTest.class);

	private final ObjectMapper			objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private final ClaszServiceTestUtils	claszServiceTestUtils;

	public ClaszServiceTest() {

		super("class", ClaszService.class);

		claszServiceTestUtils = new ClaszServiceTestUtils();
	}

	@Test
	public void testSimpleAttribute() {

		final Clasz clasz = createObject("http://purl.org/ontology/bibo/Document").getObject();

		clasz.setName("document");

		updateObjectTransactional(clasz);

		final Clasz updatedClass = getObject(clasz);

		Assert.assertNotNull("the attribute name of the updated resource shouldn't be null", updatedClass.getName());
		Assert.assertEquals("the attribute's name are not equal", clasz.getName(), updatedClass.getName());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedClass);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ClaszServiceTest.LOG.debug("class json: " + json);

		// clean up DB
		claszServiceTestUtils.deleteObject(clasz);
	}

	@Test
	public void testUniquenessOfClasses() {

		final Clasz clasz1 = createClass();
		final Clasz clasz2 = createClass();

		Assert.assertNotNull("attribute1 shouldn't be null", clasz1);
		Assert.assertNotNull("attribute2 shouldn't be null", clasz2);
		Assert.assertNotNull("attribute1 id shouldn't be null", clasz1.getId());
		Assert.assertNotNull("attribute2 id shouldn't be null", clasz2.getId());
		Assert.assertEquals("the attributes should be equal", clasz1, clasz2);
		Assert.assertNotNull("attribute1 uri shouldn't be null", clasz1.getUri());
		Assert.assertNotNull("attribute2 uri shouldn't be null", clasz2.getUri());
		Assert.assertNotNull("attribute1 uri shouldn't be empty", clasz1.getUri().trim().isEmpty());
		Assert.assertNotNull("attribute2 uri shouldn't be empty", clasz2.getUri().trim().isEmpty());
		Assert.assertEquals("the attribute uris should be equal", clasz1.getUri(), clasz2.getUri());
		Assert.assertNotNull("attribute1 uri shouldn't be null", clasz1.getName());
		Assert.assertNotNull("attribute2 uri shouldn't be null", clasz2.getName());
		Assert.assertNotNull("attribute1 uri shouldn't be empty", clasz1.getName().trim().isEmpty());
		Assert.assertNotNull("attribute2 uri shouldn't be empty", clasz2.getName().trim().isEmpty());
		Assert.assertEquals("the attribute uris should be equal", clasz1.getName(), clasz2.getName());

		// clean up DB
		claszServiceTestUtils.deleteObject(clasz1);
	}

	private Clasz createClass() {

		final Clasz clasz = createObject("http://purl.org/ontology/bibo/Document").getObject();

		clasz.setName("document");

		updateObjectTransactional(clasz);

		final Clasz updatedClasz = getObject(clasz);

		return updatedClasz;
	}
}
