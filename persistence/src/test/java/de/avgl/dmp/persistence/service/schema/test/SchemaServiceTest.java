package de.avgl.dmp.persistence.service.schema.test;

import java.util.LinkedList;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.schema.proxy.ProxySchema;
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.service.schema.AttributeService;
import de.avgl.dmp.persistence.service.schema.ClaszService;
import de.avgl.dmp.persistence.service.schema.SchemaService;
import de.avgl.dmp.persistence.service.test.IDBasicJPAServiceTest;

public class SchemaServiceTest extends IDBasicJPAServiceTest<ProxySchema, Schema, SchemaService> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(SchemaServiceTest.class);

	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private Map<Long, Attribute>					attributes		= Maps.newLinkedHashMap();

	public SchemaServiceTest() {

		super("schema", SchemaService.class);
	}

	@Test
	public void testSimpleSchema() {

		// first attribute path

		final Attribute dctermsTitle = createAttribute("http://purl.org/dc/terms/title", "title");

		final Attribute dctermsHasPart = createAttribute("http://purl.org/dc/terms/hasPart", "hasPart");

		final LinkedList<Attribute> attributePath1Arg = Lists.newLinkedList();

		attributePath1Arg.add(dctermsTitle);
		attributePath1Arg.add(dctermsHasPart);
		attributePath1Arg.add(dctermsTitle);

		System.out.println("attribute title = '" + dctermsTitle.toString());
		System.out.println("attribute hasPart = '" + dctermsHasPart.toString());

		final AttributePath attributePath1 = createAttributePath(attributePath1Arg);

		// second attribute path

		final String dctermsCreatorId = "http://purl.org/dc/terms/creator";
		final String dctermsCreatorName = "creator";

		final Attribute dctermsCreator = createAttribute(dctermsCreatorId, dctermsCreatorName);

		final String foafNameId = "http://xmlns.com/foaf/0.1/name";
		final String foafNameName = "name";

		final Attribute foafName = createAttribute(foafNameId, foafNameName);

		final LinkedList<Attribute> attributePath2Arg = Lists.newLinkedList();

		attributePath2Arg.add(dctermsCreator);
		attributePath2Arg.add(foafName);

		System.out.println("attribute creator = '" + dctermsCreator.toString());
		System.out.println("attribute name = '" + foafName.toString());

		final AttributePath attributePath2 = createAttributePath(attributePath2Arg);

		// third attribute path

		final String dctermsCreatedId = "http://purl.org/dc/terms/created";
		final String dctermsCreatedName = "created";

		final Attribute dctermsCreated = createAttribute(dctermsCreatedId, dctermsCreatedName);

		final LinkedList<Attribute> attributePath3Arg = Lists.newLinkedList();

		attributePath3Arg.add(dctermsCreated);

		System.out.println("attribute created = '" + dctermsCreated.toString());

		final AttributePath attributePath3 = createAttributePath(attributePath3Arg);

		// record class

		final String biboDocumentId = "http://purl.org/ontology/bibo/Document";
		final String biboDocumentName = "document";

		final Clasz biboDocument = createClass(biboDocumentId, biboDocumentName);

		// schema

		final Schema schema = createObject().getObject();

		schema.setName("my schema");
		schema.addAttributePath(attributePath1);
		schema.addAttributePath(attributePath2);
		schema.addAttributePath(attributePath3);
		schema.setRecordClass(biboDocument);

		// update schema

		final Schema updatedSchema = updateObjectTransactional(schema).getObject();

		Assert.assertNotNull("the schema's attribute paths of the updated schema shouldn't be null", updatedSchema.getAttributePaths());
		Assert.assertEquals("the schema's attribute paths size are not equal", schema.getAttributePaths(), updatedSchema.getAttributePaths());
		Assert.assertEquals("the attribute path '" + attributePath1.getId() + "' of the schema are not equal",
				schema.getAttributePath(attributePath1.getId()), updatedSchema.getAttributePath(attributePath1.getId()));
		Assert.assertNotNull("the attribute path's attributes of the attribute path '" + attributePath1.getId()
				+ "' of the updated schema shouldn't be null", updatedSchema.getAttributePath(attributePath1.getId()).getAttributes());
		Assert.assertEquals("the attribute path's attributes size of attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.getAttributes(), updatedSchema.getAttributePath(attributePath1.getId()).getAttributes());
		Assert.assertEquals("the first attributes of attribute path '" + attributePath1.getId() + "' are not equal", attributePath1
				.getAttributePath().get(0), updatedSchema.getAttributePath(attributePath1.getId()).getAttributePath().get(0));
		Assert.assertNotNull("the attribute path string of attribute path '" + attributePath1.getId() + "' of the update schema shouldn't be null",
				updatedSchema.getAttributePath(attributePath1.getId()).toAttributePath());
		Assert.assertEquals("the attribute path's strings attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.toAttributePath(), updatedSchema.getAttributePath(attributePath1.getId()).toAttributePath());
		Assert.assertNotNull("the record class of the updated schema shouldn't be null", updatedSchema.getRecordClass());
		Assert.assertEquals("the recod classes are not equal", schema.getRecordClass(), updatedSchema.getRecordClass());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(schema);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("schema json: " + json);

		// clean up DB
		deletedObject(schema.getId());

		deleteClasz(biboDocument);

		deleteAttributePath(attributePath1);
		deleteAttributePath(attributePath2);
		deleteAttributePath(attributePath3);

		for (final Attribute attribute : attributes.values()) {

			deleteAttribute(attribute);
		}
	}

	private AttributePath createAttributePath(final LinkedList<Attribute> attributePathArg) {

		final AttributePathService attributePathService = GuicedTest.injector.getInstance(AttributePathService.class);

		Assert.assertNotNull("attribute path service shouldn't be null", attributePathService);

		final AttributePath attributePath = new AttributePath(attributePathArg);

		AttributePath updatedAttributePath = null;

		try {

			updatedAttributePath = attributePathService.createObject(attributePathArg);
		} catch (final DMPPersistenceException e1) {

			Assert.assertTrue("something went wrong while attribute path creation.\n" + e1.getMessage(), false);
		}

		Assert.assertNotNull("updated attribute path shouldn't be null", updatedAttributePath);
		Assert.assertNotNull("updated attribute path id shouldn't be null", updatedAttributePath.getId());
		Assert.assertNotNull("the attribute path's attribute of the updated attribute path shouldn't be null", updatedAttributePath.getAttributes());
		Assert.assertEquals("the attribute path's attributes size are not equal", attributePath.getAttributes(), updatedAttributePath.getAttributes());
		Assert.assertEquals("the first attributes of the attribute path are not equal", attributePath.getAttributePath().get(0), updatedAttributePath
				.getAttributePath().get(0));
		Assert.assertNotNull("the attribute path string of the updated attribute path shouldn't be null", updatedAttributePath.toAttributePath());
		Assert.assertEquals("the attribute path's strings are not equal", attributePath.toAttributePath(), updatedAttributePath.toAttributePath());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedAttributePath);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("attribute path json for attribute path '" + attributePath.getId() + "': " + json);

		return updatedAttributePath;
	}

	private Attribute createAttribute(final String id, final String name) {

		if (attributes.containsKey(id)) {

			return attributes.get(id);
		}

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

			Assert.assertTrue("something went wrong while updating the attribute of id = '" + id + "'", false);
		}

		Assert.assertNotNull("updated attribute shouldn't be null", updatedAttribute);
		Assert.assertNotNull("updated attribute id shouldn't be null", updatedAttribute.getId());
		Assert.assertNotNull("updated attribute name shouldn't be null", updatedAttribute.getName());

		attributes.put(updatedAttribute.getId(), updatedAttribute);

		return updatedAttribute;
	}

	private Clasz createClass(final String id, final String name) {

		final ClaszService classService = GuicedTest.injector.getInstance(ClaszService.class);

		Assert.assertNotNull("class service shouldn't be null", classService);

		// create class

		Clasz clasz = null;

		try {
			clasz = classService.createObjectTransactional(id);
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while class creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("attribute shouldn't be null", clasz);
		Assert.assertNotNull("attribute id shouldn't be null", clasz.getId());

		clasz.setName(name);

		Clasz updatedClasz = null;

		try {

			updatedClasz = classService.updateObjectTransactional(clasz).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the class of id = '" + id + "'", false);
		}

		Assert.assertNotNull("updated class shouldn't be null", updatedClasz);
		Assert.assertNotNull("updated class id shouldn't be null", updatedClasz.getId());
		Assert.assertNotNull("updated class name shouldn't be null", updatedClasz.getName());

		return updatedClasz;
	}

	private void deleteAttribute(final Attribute attribute) {

		final AttributeService attributeService = GuicedTest.injector.getInstance(AttributeService.class);

		Assert.assertNotNull("attribute service shouldn't be null", attributeService);

		final Long attributeId = attribute.getId();

		attributeService.deleteObject(attributeId);

		final Attribute deletedAttribute = attributeService.getObject(attributeId);

		Assert.assertNull("deleted attribute shouldn't exist any more", deletedAttribute);
	}

	private void deleteAttributePath(final AttributePath attributePath) {

		final AttributePathService attributePathService = GuicedTest.injector.getInstance(AttributePathService.class);

		Assert.assertNotNull("attribute path service shouldn't be null", attributePathService);

		final Long attributePathId = attributePath.getId();

		attributePathService.deleteObject(attributePathId);

		final AttributePath deletedAttributePath = attributePathService.getObject(attributePathId);

		Assert.assertNull("deleted attribute path shouldn't exist any more", deletedAttributePath);
	}

	private void deleteClasz(final Clasz clasz) {

		final ClaszService claszService = GuicedTest.injector.getInstance(ClaszService.class);

		Assert.assertNotNull("class service shouldn't be null", claszService);

		final Long claszId = clasz.getId();

		claszService.deleteObject(claszId);

		final Clasz deletedClass = claszService.getObject(claszId);

		Assert.assertNull("deleted class shouldn't exist any more", deletedClass);
	}
}
