package de.avgl.dmp.persistence.service.schema.test;

import java.util.Iterator;
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
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.service.schema.AttributeService;
import de.avgl.dmp.persistence.service.test.IDBasicJPAServiceTest;

public class AttributePathServiceTest extends IDBasicJPAServiceTest<AttributePath, AttributePathService, Long> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(AttributePathServiceTest.class);

	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	public AttributePathServiceTest() {

		super("attribute path", AttributePathService.class);
	}

	@Test
	public void testUniquenessOfAttributePath() {

		final AttributePath attributePath1 = configureUniqueExampleAttributePath();
		final AttributePath attributePath2 = configureUniqueExampleAttributePath();

		Assert.assertEquals("ids of attribute paths should be equal", attributePath1.getId(), attributePath2.getId());

		// there should not be two or more persistent AttributePath instances now with the same path (attributePath field)

		final String jsonPath = attributePath1.getAttributePathAsJSONObjectString();

		final List<AttributePath> attributePathList = jpaService.getAttributePathsWithPath(jsonPath);
		AttributePathServiceTest.LOG.debug("Number of AttributePath instances with the identical path" + jsonPath + ":" + attributePathList.size());
		Assert.assertTrue("There are more than two AttributePaths instances with an identical path!", attributePathList.size() <= 1);

		// clean up DB (!! this won't happen when the assertion fails! -> create extra cleanup methode for this test)
		deletedObject(attributePath1.getId());
		// TODO: trying to delete the second path creates Nullpointer (since already deleted)

		// delete the attributes created for the example paths
		final Set<Attribute> attributeSet1 = attributePath1.getAttributes();
		for (final Iterator<Attribute> iterator = attributeSet1.iterator(); iterator.hasNext();) {
			final Attribute attribute = iterator.next();
			deleteAttribute(attribute);
		}

	}

	private AttributePath configureUniqueExampleAttributePath() {

		final AttributePath tempAttributePath = new AttributePath();

		final Attribute dctermsTitle = createAttribute("http://purl.org/dc/terms/title", "title");
		final Attribute dctermsHasPart = createAttribute("http://purl.org/dc/terms/hasPart", "hasPart");

		tempAttributePath.addAttribute(dctermsHasPart);
		tempAttributePath.addAttribute(dctermsTitle);

		System.out.println("attribute title = '" + dctermsTitle.toString());
		System.out.println("attribute hasPart = '" + dctermsHasPart.toString());
		System.out.println("temp attribute path = '" + tempAttributePath.toString());

		final AttributePath attributePath = createObject(tempAttributePath.getAttributePath());

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

	@Test
	public void testSimpleAttributePath() {

		final AttributePath attributePath = createObject();

		final Attribute dctermsTitle = createAttribute("http://purl.org/dc/terms/title", "title");

		final Attribute dctermsHasPart = createAttribute("http://purl.org/dc/terms/hasPart", "hasPart");

		attributePath.addAttribute(dctermsHasPart);
		attributePath.addAttribute(dctermsTitle);

		System.out.println("attribute title = '" + dctermsTitle.toString());
		System.out.println("attribute hasPart = '" + dctermsHasPart.toString());
		System.out.println("attribute path = '" + attributePath.toString());

		final AttributePath updatedAttributePath = updateObjectTransactional(attributePath);

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

			updatedAttribute = attributeService.updateObjectTransactional(attribute);
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

		final String attributeId = attribute.getId();

		attributeService.deleteObject(attributeId);

		final Attribute deletedAttribute = attributeService.getObject(attributeId);

		Assert.assertNull("deleted attribute shouldn't exist any more", deletedAttribute);
	}
}
