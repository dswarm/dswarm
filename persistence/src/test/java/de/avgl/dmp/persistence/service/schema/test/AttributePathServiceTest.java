package de.avgl.dmp.persistence.service.schema.test;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttributePath;
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.service.schema.AttributeService;
import de.avgl.dmp.persistence.service.test.IDBasicJPAServiceTest;

public class AttributePathServiceTest extends IDBasicJPAServiceTest<ProxyAttributePath, AttributePath, AttributePathService> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(AttributePathServiceTest.class);

	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	public AttributePathServiceTest() {

		super("attribute path", AttributePathService.class);
	}

	@Test
	public void testSimpleAttributePath2() {

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
		deletedObject(updatedAttributePath.getId());

		// TODO: for some reason the loop on the set won't be executed, when all tests are executed at once and only when this
		// test class is executed alone, i.e., the attributes
		// won't be deleted from the DB
		// => this might be a Hibernate bug, because the set was a specific Hibernate Set implementation here (PersistenceSet);
		// this set wraps for some reason the true set, i.e., at least I saw via debugging a 'set' parameter that was the expected
		// CopyOnWriteSet

		// delete the attributes created for the example paths
		for (final Attribute attribute : attributeSet1) {

			deleteAttribute(attribute);
		}

		AttributePathServiceTest.LOG.debug("end simple attribute path test 2");
	}

	@Test
	public void testUniquenessOfAttributePath() {

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
		deletedObject(attributePath1.getId());

		// delete the attributes created for the example paths
		for (final Attribute attribute : attributeSet1) {

			deleteAttribute(attribute);
		}

		AttributePathServiceTest.LOG.debug("start uniquness of attribute path test");
	}

	@Test
	public void testSimpleAttributePath() {

		AttributePathServiceTest.LOG.debug("start simple attribute path test");

		final Attribute dctermsTitle = createAttribute("http://purl.org/dc/terms/title", "title");

		final Attribute dctermsHasPart = createAttribute("http://purl.org/dc/terms/hasPart", "hasPart");

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
		deletedObject(attributePath.getId());
		deleteAttribute(dctermsHasPart);
		deleteAttribute(dctermsTitle);

		AttributePathServiceTest.LOG.debug("end simple attribute path test");
	}

	private AttributePath configureUniqueExampleAttributePath() {

		final AttributePath tempAttributePath = createAttributePath();

		final AttributePath attributePath = createObject(tempAttributePath.getAttributePath());

		return attributePath;
	}

	private AttributePath createAttributePath() {

		final AttributePath attributePath = new AttributePath();

		final Attribute dctermsTitle = createAttribute("http://purl.org/dc/terms/title", "title");
		final Attribute dctermsHasPart = createAttribute("http://purl.org/dc/terms/hasPart", "hasPart");

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
			object = jpaService.createObject(attributePath);
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

	private AttributePath createObject(final AttributePath attributePath) {

		AttributePath object = null;
		try {
			object = jpaService.createObject(attributePath);
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

	private Attribute createAttribute(final String id, final String name) {

		final AttributeService attributeService = GuicedTest.injector.getInstance(AttributeService.class);

		Assert.assertNotNull("attribute service shouldn't be null", attributeService);

		// create first attribute

		Attribute attribute = null;

		try {
			attribute = attributeService.createObjectTransactional(id);
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while attribute creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("attribute shouldn't be null", attribute);
		Assert.assertNotNull("attribute id shouldn't be null", attribute.getId());

		attribute.setName(name);

		Attribute updatedAttribute = null;

		try {

			updatedAttribute = attributeService.updateObjectTransactional(attribute).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updaging the attribute of id = '" + id + "'", false);
		}

		Assert.assertNotNull("updated attribute shouldn't be null", updatedAttribute);
		Assert.assertNotNull("updated attribute id shouldn't be null", updatedAttribute.getId());
		Assert.assertNotNull("updated attribute name shouldn't be null", updatedAttribute.getName());

		return updatedAttribute;
	}

	private void deleteAttribute(final Attribute attribute) {

		final AttributeService attributeService = GuicedTest.injector.getInstance(AttributeService.class);

		Assert.assertNotNull("attribute service shouldn't be null", attributeService);

		final Long attributeId = attribute.getId();

		attributeService.deleteObject(attributeId);

		final Attribute deletedAttribute = attributeService.getObject(attributeId);

		Assert.assertNull("deleted attribute shouldn't exist any more", deletedAttribute);
	}
}
