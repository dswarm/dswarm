package org.dswarm.persistence.service.schema.test;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePath;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.test.utils.AttributePathServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.AttributeServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;

public class AttributePathServiceTest extends IDBasicJPAServiceTest<ProxyAttributePath, AttributePath, AttributePathService> {

	private static final Logger					LOG				= LoggerFactory.getLogger(AttributePathServiceTest.class);

	private final AttributeServiceTestUtils		attributeServiceTestUtils;
	private final AttributePathServiceTestUtils	attributePathServiceTestUtils;

	private final ObjectMapper					objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	public AttributePathServiceTest() {

		super("attribute path", AttributePathService.class);

		attributeServiceTestUtils = new AttributeServiceTestUtils();
		attributePathServiceTestUtils = new AttributePathServiceTestUtils();
	}

	@Test
	public void testSimpleAttributePath2() throws Exception {

		AttributePathServiceTest.LOG.debug("start simple attribute path test 2");

		final AttributePath attributePath = createAttributePath();
		final AttributePath updatedAttributePath = createObject(attributePath);

		Assert.assertNotNull("the attribute path's attribute of the updated attribute path shouldn't be null", updatedAttributePath.getAttributes());
		Assert.assertEquals("the attribute path's attributes are not equal", attributePath.getAttributes(), updatedAttributePath.getAttributes());
		Assert.assertNotNull("the attribute path's ordered list of the updated attribute path shouldn't be null",
				updatedAttributePath.getAttributePath());
		Assert.assertEquals("the attribute path's ordered lists are not equal", attributePath.getAttributePath(),
				updatedAttributePath.getAttributePath());
		Assert.assertEquals("the first attributes of the attribute path are not equal", attributePath.getAttributePath().get(0), updatedAttributePath
				.getAttributePath().get(0));
		Assert.assertNotNull("the attribute path string of the updated attribute path shouldn't be null", updatedAttributePath.toAttributePath());
		Assert.assertEquals("the attribute path's strings are not equal", attributePath.toAttributePath(), updatedAttributePath.toAttributePath());

		final Set<Attribute> attributeSet1 = updatedAttributePath.getAttributes();

		Assert.assertEquals("the attribute path's attributes size are not equal", attributePath.getAttributes().size(), attributeSet1.size());
		Assert.assertEquals("the attribute path's attributes size is not 2", 2, attributeSet1.size());
		Assert.assertFalse("the attribute path's attributes set shouldn't be empty", attributeSet1.isEmpty());

		// clean up DB
		deleteObject(updatedAttributePath.getId());

		// TODO: for some reason the loop on the set won't be executed, when all tests are executed at once and only when this
		// test class is executed alone, i.e., the attributes
		// won't be deleted from the DB
		// => this might be a Hibernate bug, because the set was a specific Hibernate Set implementation here (PersistenceSet);
		// this set wraps for some reason the true set, i.e., at least I saw via debugging a 'set' parameter that was the expected
		// CopyOnWriteSet

		// delete the attributes created for the example paths
		for (final Attribute attribute : attributeSet1) {

			attributeServiceTestUtils.deleteObject(attribute);
		}

		AttributePathServiceTest.LOG.debug("end simple attribute path test 2");
	}

	@Test
	public void testUniquenessOfAttributePath() throws Exception {

		AttributePathServiceTest.LOG.debug("start uniqueness of attribute path test");

		final AttributePath attributePath1 = configureUniqueExampleAttributePath();
		final AttributePath attributePath2 = configureUniqueExampleAttributePath();

		Assert.assertEquals("ids of attribute paths should be equal", attributePath1.getId(), attributePath2.getId());

		final String jsonPath = attributePath1.getAttributePathAsJSONObjectString();

		final List<AttributePath> attributePathList = jpaService.getAttributePathsWithPath(jsonPath);
		AttributePathServiceTest.LOG.debug("Number of AttributePath instances with the identical path" + jsonPath + ":" + attributePathList.size());
		Assert.assertTrue("There is more than one AttributePath instance with an identical path!", attributePathList.size() == 1);

		final Set<Attribute> attributeSet1 = attributePath1.getAttributes();

		Assert.assertEquals("the attribute path's attributes size is not 2", 2, attributeSet1.size());
		Assert.assertFalse("the attribute path's attributes set shouldn't be empty", attributeSet1.isEmpty());

		// clean up DB
		deleteObject(attributePath1.getId());

		// delete the attributes created for the example paths
		for (final Attribute attribute : attributeSet1) {

			attributeServiceTestUtils.deleteObject(attribute);
		}

		AttributePathServiceTest.LOG.debug("start uniquness of attribute path test");
	}

	@Test
	public void testSimpleAttributePath() throws Exception {

		AttributePathServiceTest.LOG.debug("start simple attribute path test");

		final Attribute dctermsTitle = attributeServiceTestUtils.createAttribute("http://purl.org/dc/terms/title", "title");

		final Attribute dctermsHasPart = attributeServiceTestUtils.createAttribute("http://purl.org/dc/terms/hasPart", "hasPart");

		final AttributePath attributePath = createObject().getObject();

		attributePath.addAttribute(dctermsHasPart);
		attributePath.addAttribute(dctermsTitle);

		System.out.println("attribute title = '" + dctermsTitle.toString());
		System.out.println("attribute hasPart = '" + dctermsHasPart.toString());
		System.out.println("attribute path = '" + attributePath.toString());

		final AttributePath updatedAttributePath = updateObjectTransactional(attributePath).getObject();

		Assert.assertNotNull("the attribute path's attribute of the updated attribute path shouldn't be null", updatedAttributePath.getAttributes());
		Assert.assertEquals("the attribute path's attributes size are not equal", attributePath.getAttributes(), updatedAttributePath.getAttributes());
		Assert.assertEquals("the first attributes of the attribute path are not equal", attributePath.getAttributePath().get(0), updatedAttributePath
				.getAttributePath().get(0));
		Assert.assertNotNull("the attribute path string of the updated attribute path shouldn't be null", updatedAttributePath.toAttributePath());
		Assert.assertEquals("the attribute path's strings are not equal", attributePath.toAttributePath(), updatedAttributePath.toAttributePath());

		String json = null;

		try {
			json = objectMapper.writeValueAsString(updatedAttributePath);
		} catch (final JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		AttributePathServiceTest.LOG.debug("attribute path json: " + json);

		// clean up DB
		deleteObject(attributePath.getId());
		attributeServiceTestUtils.deleteObject(dctermsHasPart);
		attributeServiceTestUtils.deleteObject(dctermsTitle);

		AttributePathServiceTest.LOG.debug("end simple attribute path test");
	}

	private AttributePath configureUniqueExampleAttributePath() throws Exception {

		final AttributePath tempAttributePath = createAttributePath();

		final AttributePath attributePath = createObject(tempAttributePath.getAttributePath());

		return attributePath;
	}

	private AttributePath createAttributePath() throws Exception {

		final AttributePath attributePath = new AttributePath();

		final Attribute dctermsTitle = attributeServiceTestUtils.createAttribute("http://purl.org/dc/terms/title", "title");
		final Attribute dctermsHasPart = attributeServiceTestUtils.createAttribute("http://purl.org/dc/terms/hasPart", "hasPart");

		attributePath.addAttribute(dctermsHasPart);
		attributePath.addAttribute(dctermsTitle);

		System.out.println("attribute title = '" + dctermsTitle.toString());
		System.out.println("attribute hasPart = '" + dctermsHasPart.toString());
		System.out.println("temp attribute path = '" + attributePath.toString());

		return attributePath;

	}

	private AttributePath createObject(final LinkedList<Attribute> attributePath) {
		AttributePath object = null;
		try {
			object = jpaService.createOrGetObjectTransactional(attributePath).getObject();
			System.out.println(object);
		} catch (final DMPPersistenceException e) {
			Assert.assertTrue("something went wrong during attribute path creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull(type + " shouldn't be null", object);
		Assert.assertNotNull(type + " id shouldn't be null", object.getId());
		Assert.assertNotNull(type + " path shouldn't be null", object.getAttributePathAsJSONObjectString());

		AttributePathServiceTest.LOG.debug("created new attribute path with id = '" + object.getId() + "'" + " and path = '"
				+ object.getAttributePathAsJSONObjectString() + "'");

		return object;
	}

	private AttributePath createObject(final AttributePath attributePath) throws Exception {

		final AttributePath object = attributePathServiceTestUtils.createObject(attributePath, attributePath);

		AttributePathServiceTest.LOG.debug("created new attribute path with id = '" + object.getId() + "'" + " and path = '"
				+ object.getAttributePathAsJSONObjectString() + "'");

		return object;
	}
}
