/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.model.schema.test;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.service.UUIDService;

public class SchemaTest extends GuicedTest {

	private static final Logger LOG = LoggerFactory.getLogger(SchemaTest.class);

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	@Test
	public void simpleSchemaTest() throws IOException {

		final Schema schema = makeTestSchema();
		final String json = objectMapper.writeValueAsString(schema);
		final Schema schemaDup = objectMapper.readValue(json, Schema.class);
		final String jsonDup = objectMapper.writeValueAsString(schemaDup);

		SchemaTest.LOG.debug("schema json: {}", json);

		Assert.assertTrue("the two schemas should be identical", json.equals(jsonDup));
	}

	/**
	 * Test building a schema with sub-schemata
	 *
	 * @throws IOException
	 */
	@Test
	public void complexSchemaTest() throws IOException {

		final Schema schema = makeTestSchema();
		final String json = objectMapper.writeValueAsString(schema);
		final Schema schemaDup = objectMapper.readValue(json, Schema.class);
		final String jsonDup = objectMapper.writeValueAsString(schemaDup);

		SchemaTest.LOG.debug("schema json: {}", json);

		Assert.assertTrue("the two schemas should be identical", json.equals(jsonDup));
	}

	private static Schema makeTestSchema() {
		final Attribute dctermsTitle = createAttribute("http://purl.org/dc/terms/title", "title");
		final Attribute dctermsHasPart = createAttribute("http://purl.org/dc/terms/hasPart", "hasPart");
		final Attribute dctermsDescription = createAttribute("http://purl.org/dc/terms/description", "description");
		final Attribute dctermsCreator = createAttribute("http://purl.org/dc/terms/creator", "creator");
		final Attribute foafName = createAttribute("http://xmlns.com/foaf/0.1/name", "name");
		final Attribute dctermsCreated = createAttribute("http://purl.org/dc/terms/created", "created");

		final AttributePath attributePath1 = createAttributePath(dctermsTitle, dctermsHasPart, dctermsDescription);
		final AttributePath attributePath2 = createAttributePath(dctermsCreator, foafName);
		final AttributePath attributePath3 = createAttributePath(dctermsCreated);

		final String uuid = UUIDService.getUUID(Clasz.class.getSimpleName());

		final Clasz biboDocument = new Clasz(uuid, "http://purl.org/ontology/bibo/Document", "document");

		return createSchema(biboDocument, attributePath1, attributePath2, attributePath3);
	}

	private static AttributePath createAttributePath(final Attribute... attributes) {

		final String attributePathUUID = UUIDService.getUUID(AttributePath.class.getSimpleName());

		final AttributePath attributePath = new AttributePath(attributePathUUID);
		for (final Attribute attribute : attributes) {
			attributePath.addAttribute(attribute);
		}

		Assert.assertNotNull("the attributes should not be null", attributePath.getAttributes());
		Assert.assertEquals("the attributes have the wrong size", attributes.length, attributePath.getAttributes().size());

		return attributePath;
	}

	private static SchemaAttributePathInstance createAttributePathInstance(final AttributePath attributePath) {

		final String attributePathInstanceUUID = UUIDService.getUUID(SchemaAttributePathInstance.class.getSimpleName());

		final SchemaAttributePathInstance attributePathInstance = new SchemaAttributePathInstance(attributePathInstanceUUID);
		attributePathInstance.setAttributePath(attributePath);

		Assert.assertNotNull("the attribute path should not be null", attributePathInstance.getAttributePath());

		return attributePathInstance;
	}

	private static Schema createSchema(final Clasz recordClass, final AttributePath... attributePaths) {

		final String schemaUUID = UUIDService.getUUID(Schema.class.getSimpleName());

		final Schema schema = new Schema(schemaUUID);
		schema.setRecordClass(recordClass);
		for (final AttributePath attributePath : attributePaths) {
			SchemaAttributePathInstance pathInstance = createAttributePathInstance(attributePath);
			schema.addAttributePath(pathInstance);
		}

		Assert.assertNotNull("the record class should not be null", schema.getRecordClass());
		Assert.assertEquals("the record class is not the same", recordClass, schema.getRecordClass());
		Assert.assertNotNull("the attribute paths should not be null", schema.getUniqueAttributePaths());
		Assert.assertEquals("the attribute paths have the wrong size", attributePaths.length, schema.getUniqueAttributePaths().size());

		return schema;
	}

	private static Attribute createAttribute(final String uri, final String name) {

		final String uuid = UUIDService.getUUID(Attribute.class.getSimpleName());

		final Attribute attribute = new Attribute(uuid, uri);
		attribute.setName(name);

		Assert.assertNotNull("the attribute uri shouldn't be null", attribute.getUri());
		Assert.assertEquals("the attribute uris are not equal", uri, attribute.getUri());
		Assert.assertNotNull("the attribute name shouldn't be null", attribute.getName());
		Assert.assertEquals("the attribute names are not equal", name, attribute.getName());

		return attribute;
	}
}
