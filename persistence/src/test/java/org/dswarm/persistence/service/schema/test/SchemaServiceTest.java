/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.service.schema.test.utils.AttributePathServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.AttributeServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.ClaszServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.ContentSchemaServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;

public class SchemaServiceTest extends IDBasicJPAServiceTest<ProxySchema, Schema, SchemaService> {

	private static final Logger					LOG				= LoggerFactory.getLogger(SchemaServiceTest.class);

	private final ObjectMapper					objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private final Map<Long, Attribute>			attributes		= Maps.newLinkedHashMap();

	private final AttributeServiceTestUtils		attributeServiceTestUtils;
	private final ClaszServiceTestUtils			claszServiceTestUtils;
	private final ContentSchemaServiceTestUtils	contentSchemaServiceTestUtils;
	private final AttributePathServiceTestUtils	attributePathServiceTestUtils;

	public SchemaServiceTest() {

		super("schema", SchemaService.class);

		attributeServiceTestUtils = new AttributeServiceTestUtils();
		attributePathServiceTestUtils = new AttributePathServiceTestUtils();
		claszServiceTestUtils = new ClaszServiceTestUtils();
		contentSchemaServiceTestUtils = new ContentSchemaServiceTestUtils();
	}

	@Test
	public void testSimpleSchema() throws Exception {

		// first attribute path

		final Attribute dctermsTitle = attributeServiceTestUtils.createAttribute("http://purl.org/dc/terms/title", "title");
		attributes.put(dctermsTitle.getId(), dctermsTitle);

		final Attribute dctermsHasPart = attributeServiceTestUtils.createAttribute("http://purl.org/dc/terms/hasPart", "hasPart");
		attributes.put(dctermsHasPart.getId(), dctermsHasPart);

		final LinkedList<Attribute> attributePath1Arg = Lists.newLinkedList();

		attributePath1Arg.add(dctermsTitle);
		attributePath1Arg.add(dctermsHasPart);
		attributePath1Arg.add(dctermsTitle);

		System.out.println("attribute title = '" + dctermsTitle.toString());
		System.out.println("attribute hasPart = '" + dctermsHasPart.toString());

		final AttributePath attributePath1 = attributePathServiceTestUtils.createAttributePath(attributePath1Arg);

		// second attribute path

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

		System.out.println("attribute creator = '" + dctermsCreator.toString());
		System.out.println("attribute name = '" + foafName.toString());

		final AttributePath attributePath2 = attributePathServiceTestUtils.createAttributePath(attributePath2Arg);

		// third attribute path

		final String dctermsCreatedId = "http://purl.org/dc/terms/created";
		final String dctermsCreatedName = "created";

		final Attribute dctermsCreated = attributeServiceTestUtils.createAttribute(dctermsCreatedId, dctermsCreatedName);
		attributes.put(dctermsCreated.getId(), dctermsCreated);

		final LinkedList<Attribute> attributePath3Arg = Lists.newLinkedList();

		attributePath3Arg.add(dctermsCreated);

		System.out.println("attribute created = '" + dctermsCreated.toString());

		final AttributePath attributePath3 = attributePathServiceTestUtils.createAttributePath(attributePath3Arg);

		// record class

		final String biboDocumentId = "http://purl.org/ontology/bibo/Document";
		final String biboDocumentName = "document";

		final Clasz biboDocument = claszServiceTestUtils.createClass(biboDocumentId, biboDocumentName);

		// START content schema

		// value attribute path

		final String rdfValueId = "http://www.w3.org/1999/02/22-rdf-syntax-ns#value";
		final String rdfValueName = "value";

		final Attribute rdfValue = attributeServiceTestUtils.createAttribute(rdfValueId, rdfValueName);
		attributes.put(rdfValue.getId(), rdfValue);

		final LinkedList<Attribute> rdfValueAPList = Lists.newLinkedList();
		rdfValueAPList.add(rdfValue);

		final AttributePath rdfValueAP = attributePathServiceTestUtils.createAttributePath(rdfValueAPList);

		// content schema

		final ContentSchema dummyContentSchema = new ContentSchema();

		dummyContentSchema.setName("my content schema");
		dummyContentSchema.addKeyAttributePath(attributePath1);
		dummyContentSchema.addKeyAttributePath(attributePath2);
		dummyContentSchema.addKeyAttributePath(attributePath3);
		dummyContentSchema.setValueAttributePath(rdfValueAP);

		final ContentSchema contentSchema = contentSchemaServiceTestUtils.createContentSchema(dummyContentSchema);

		// END content schema

		// schema

		final Schema schema = createObject().getObject();

		schema.setName("my schema");
		schema.addAttributePath(attributePath1);
		schema.addAttributePath(attributePath2);
		schema.addAttributePath(attributePath3);
		schema.setRecordClass(biboDocument);
		schema.setContentSchema(contentSchema);

		// update schema

		final Schema updatedSchema = updateObjectTransactional(schema).getObject();

		Assert.assertNotNull("the schema's attribute paths of the updated schema shouldn't be null", updatedSchema.getUniqueAttributePaths());
		Assert.assertEquals("the schema's attribute paths size are not equal", schema.getUniqueAttributePaths(), updatedSchema.getUniqueAttributePaths());
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
		Assert.assertNotNull("the content schema of the updated schema shouldn't be null", updatedSchema.getContentSchema());
		Assert.assertEquals("the content schemata are not equal", schema.getContentSchema(), updatedSchema.getContentSchema());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(schema);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		SchemaServiceTest.LOG.debug("schema json: " + json);

		// clean up DB
		deleteObject(schema.getId());

		claszServiceTestUtils.deleteObject(biboDocument);
		contentSchemaServiceTestUtils.deleteObject(contentSchema);

		attributePathServiceTestUtils.deleteObject(attributePath1);
		attributePathServiceTestUtils.deleteObject(attributePath2);
		attributePathServiceTestUtils.deleteObject(attributePath3);
		attributePathServiceTestUtils.deleteObject(rdfValueAP);

		for (final Attribute attribute : attributes.values()) {

			attributeServiceTestUtils.deleteObject(attribute);
		}
	}
}
