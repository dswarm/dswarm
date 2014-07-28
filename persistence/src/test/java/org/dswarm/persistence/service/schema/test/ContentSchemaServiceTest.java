package org.dswarm.persistence.service.schema.test;

import java.util.LinkedList;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.proxy.ProxyContentSchema;
import org.dswarm.persistence.service.schema.ContentSchemaService;
import org.dswarm.persistence.service.schema.test.utils.AttributePathServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.AttributeServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;

public class ContentSchemaServiceTest extends IDBasicJPAServiceTest<ProxyContentSchema, ContentSchema, ContentSchemaService> {

	private static final Logger					LOG				= LoggerFactory.getLogger(ContentSchemaServiceTest.class);

	private final ObjectMapper					objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private final Map<Long, Attribute>			attributes		= Maps.newLinkedHashMap();

	private final AttributeServiceTestUtils		attributeServiceTestUtils;
	private final AttributePathServiceTestUtils	attributePathServiceTestUtils;

	public ContentSchemaServiceTest() {

		super("content schema", ContentSchemaService.class);

		attributeServiceTestUtils = new AttributeServiceTestUtils();
		attributePathServiceTestUtils = new AttributePathServiceTestUtils();
	}

	@Test
	public void testSimpleSchema() throws Exception {

		// record identifier attribute path

		final String dctermsIdentifierId = "http://purl.org/dc/terms/identifier";
		final String dctermsIdentifierName = "identifier";

		final Attribute dctermsIdentifier = attributeServiceTestUtils.createAttribute(dctermsIdentifierId, dctermsIdentifierName);
		attributes.put(dctermsIdentifier.getId(), dctermsIdentifier);

		final LinkedList<Attribute> dctermsIdentifierAPList = Lists.newLinkedList();

		dctermsIdentifierAPList.add(dctermsIdentifier);

		final AttributePath dctermsIdentifierAP = attributePathServiceTestUtils.createAttributePath(dctermsIdentifierAPList);

		// key first attribute path

		final Attribute dctermsTitle = attributeServiceTestUtils.createAttribute("http://purl.org/dc/terms/title", "title");
		attributes.put(dctermsTitle.getId(), dctermsTitle);

		final Attribute dctermsHasPart = attributeServiceTestUtils.createAttribute("http://purl.org/dc/terms/hasPart", "hasPart");
		attributes.put(dctermsHasPart.getId(), dctermsHasPart);

		final LinkedList<Attribute> attributePath1Arg = Lists.newLinkedList();

		attributePath1Arg.add(dctermsTitle);
		attributePath1Arg.add(dctermsHasPart);
		attributePath1Arg.add(dctermsTitle);

		final AttributePath attributePath1 = attributePathServiceTestUtils.createAttributePath(attributePath1Arg);

		// key second attribute path

		final String dctermsCreatorId = "http://purl.org/dc/terms/creator";
		final String dctermsCreatorName = "creator";

		final Attribute dctermsCreator = attributeServiceTestUtils.createAttribute(dctermsCreatorId, dctermsCreatorName);
		attributes.put(dctermsCreator.getId(), dctermsCreator);

		final String foafNameId = "http://xmlns.com/foaf/0.1/name";
		final String foafNameName = "name";

		final Attribute foafName = attributeServiceTestUtils.createAttribute(foafNameId, foafNameName);
		attributes.put(foafName.getId(), foafName);

		final LinkedList<Attribute> attributePath2Arg = Lists.newLinkedList();

		attributePath2Arg.add(dctermsCreator);
		attributePath2Arg.add(foafName);

		final AttributePath attributePath2 = attributePathServiceTestUtils.createAttributePath(attributePath2Arg);

		// key third attribute path

		final String dctermsCreatedId = "http://purl.org/dc/terms/created";
		final String dctermsCreatedName = "created";

		final Attribute dctermsCreated = attributeServiceTestUtils.createAttribute(dctermsCreatedId, dctermsCreatedName);
		attributes.put(dctermsCreated.getId(), dctermsCreated);

		final LinkedList<Attribute> attributePath3Arg = Lists.newLinkedList();

		attributePath3Arg.add(dctermsCreated);

		final AttributePath attributePath3 = attributePathServiceTestUtils.createAttributePath(attributePath3Arg);

		// value attribute path

		final String rdfValueId = "http://www.w3.org/1999/02/22-rdf-syntax-ns#value";
		final String rdfValueName = "value";

		final Attribute rdfValue = attributeServiceTestUtils.createAttribute(rdfValueId, rdfValueName);
		attributes.put(rdfValue.getId(), rdfValue);

		final LinkedList<Attribute> rdfValueAPList = Lists.newLinkedList();

		rdfValueAPList.add(rdfValue);

		final AttributePath rdfValueAP = attributePathServiceTestUtils.createAttributePath(rdfValueAPList);

		// content schema

		final ContentSchema contentSchema = createObject().getObject();

		contentSchema.setName("my content schema");
		contentSchema.setRecordIdentifierAttributePath(dctermsIdentifierAP);
		contentSchema.addKeyAttributePath(attributePath1);
		contentSchema.addKeyAttributePath(attributePath2);
		contentSchema.addKeyAttributePath(attributePath3);
		contentSchema.setValueAttributePath(rdfValueAP);

		// update content schema

		final ContentSchema updatedContentSchema = updateObjectTransactional(contentSchema).getObject();

		Assert.assertNotNull("the record identifier attribute path of the updated content schema shouldn't be null", updatedContentSchema.getRecordIdentifierAttributePath());
		Assert.assertEquals("the record identifier attribute paths are not equal", contentSchema.getRecordIdentifierAttributePath(),
				updatedContentSchema.getRecordIdentifierAttributePath());
		Assert.assertNotNull("the content schema's key attribute paths of the updated content schema shouldn't be null",
				updatedContentSchema.getKeyAttributePaths());
		Assert.assertEquals("the content schema's key attribute paths size are not equal", contentSchema.getKeyAttributePaths(),
				updatedContentSchema.getKeyAttributePaths());
		Assert.assertEquals("the key attribute path '" + attributePath1.getId() + "' of the content schema are not equal",
				contentSchema.getKeyAttributePath(attributePath1.getId()), updatedContentSchema.getKeyAttributePath(attributePath1.getId()));
		Assert.assertNotNull("the key attribute path's attributes of the key attribute path '" + attributePath1.getId()
				+ "' of the updated content schema shouldn't be null", updatedContentSchema.getKeyAttributePath(attributePath1.getId())
				.getAttributes());
		Assert.assertEquals("the key attribute path's attributes size of key attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.getAttributes(), updatedContentSchema.getKeyAttributePath(attributePath1.getId()).getAttributes());
		Assert.assertEquals("the first attributes of key attribute path '" + attributePath1.getId() + "' are not equal", attributePath1
				.getAttributePath().get(0), updatedContentSchema.getKeyAttributePath(attributePath1.getId()).getAttributePath().get(0));
		Assert.assertNotNull("the key attribute path string of key attribute path '" + attributePath1.getId()
				+ "' of the update content schema shouldn't be null", updatedContentSchema.getKeyAttributePath(attributePath1.getId())
				.toAttributePath());
		Assert.assertEquals("the key attribute path's strings key attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.toAttributePath(), updatedContentSchema.getKeyAttributePath(attributePath1.getId()).toAttributePath());
		Assert.assertNotNull("the value attribute path of the updated content schema shouldn't be null", updatedContentSchema.getValueAttributePath());
		Assert.assertEquals("the value attribute paths are not equal", contentSchema.getValueAttributePath(),
				updatedContentSchema.getValueAttributePath());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(contentSchema);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ContentSchemaServiceTest.LOG.debug("content schema json: " + json);

		// clean up DB
		deleteObject(contentSchema.getId());

		attributePathServiceTestUtils.deleteObject(dctermsIdentifierAP);
		attributePathServiceTestUtils.deleteObject(attributePath1);
		attributePathServiceTestUtils.deleteObject(attributePath2);
		attributePathServiceTestUtils.deleteObject(attributePath3);
		attributePathServiceTestUtils.deleteObject(rdfValueAP);

		for (final Attribute attribute : attributes.values()) {

			attributeServiceTestUtils.deleteObject(attribute);
		}
	}
}
