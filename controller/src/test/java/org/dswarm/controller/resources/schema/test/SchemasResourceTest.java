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
package org.dswarm.controller.resources.schema.test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import org.dswarm.controller.resources.schema.test.utils.ClaszesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.ContentSchemasResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.SchemaAttributePathInstancesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.SchemasResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.service.schema.test.utils.ClaszServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.ContentSchemaServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.SchemaAttributePathInstanceServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.SchemaServiceTestUtils;

public class SchemasResourceTest extends
		BasicResourceTest<SchemasResourceTestUtils, SchemaServiceTestUtils, SchemaService, ProxySchema, Schema, Long> {

	private ClaszesResourceTestUtils claszesResourceTestUtils;

	private SchemaAttributePathInstancesResourceTestUtils schemaAttributePathInstancesResourceTestUtils;

	private ContentSchemasResourceTestUtils contentSchemasResourceTestUtils;

	public SchemasResourceTest() {

		super(Schema.class, SchemaService.class, "schemas", "schema.json", new SchemasResourceTestUtils());
	}

	@Override
	protected void initObjects() {

		super.initObjects();

		pojoClassResourceTestUtils = new SchemasResourceTestUtils();
		schemaAttributePathInstancesResourceTestUtils = new SchemaAttributePathInstancesResourceTestUtils();
		claszesResourceTestUtils = new ClaszesResourceTestUtils();
		contentSchemasResourceTestUtils = new ContentSchemasResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		final ClaszServiceTestUtils claszServiceTestUtils = claszesResourceTestUtils.getPersistenceServiceTestUtils();
		final Clasz recordClass = claszServiceTestUtils.createAndPersistDefaultObject();

		final SchemaAttributePathInstanceServiceTestUtils schemaAttributePathInstanceServiceTestUtils = schemaAttributePathInstancesResourceTestUtils
				.getPersistenceServiceTestUtils();

		final SchemaAttributePathInstance sapi1 = schemaAttributePathInstanceServiceTestUtils.createAndPersistDefaultObject();
		final SchemaAttributePathInstance sapi2 = schemaAttributePathInstanceServiceTestUtils.getDctermsTitleDctermsHaspartSAPI();

		final ContentSchemaServiceTestUtils contentSchemaServiceTestUtils = contentSchemasResourceTestUtils.getPersistenceServiceTestUtils();
		final ContentSchema contentSchema = contentSchemaServiceTestUtils.createAndPersistDefaultObject();

		final Schema schema = new Schema();
		schema.setRecordClass(recordClass);
		schema.addAttributePath(sapi1);
		schema.addAttributePath(sapi2);
		schema.setContentSchema(contentSchema);

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(schema);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);
	}

	@Override
	public void testPUTObject() throws Exception {

		super.testPUTObject();
	}

	@Test
	public void testAddAttributePath() throws Exception {

		final Schema schema = createObjectInternal();

		final String schemaNamespaceURI = SchemaUtils.determineSchemaNamespaceURI(schema.getId());
		final String attributeName1 = "attribute one";
		final String attributeName2 = "attribute2";
		final String attributeName3 = "a3";
		final String attributeUri1 = SchemaUtils.mintAttributeURI(attributeName1, schemaNamespaceURI);
		final String attributeUri2 = SchemaUtils.mintAttributeURI(attributeName2, schemaNamespaceURI);
		final String attributeUri3 = SchemaUtils.mintAttributeURI(attributeName3, schemaNamespaceURI);

		final ArrayNode attributeDescriptionsJSONArray = objectMapper.createArrayNode();
		final ObjectNode attributeDescriptionJSON1 = createAttributeDescription(attributeName1, attributeUri1);
		final ObjectNode attributeDescriptionJSON2 = createAttributeDescription(attributeName2, attributeUri2);
		final ObjectNode attributeDescriptionJSON3 = createAttributeDescription(attributeName3, null);
		attributeDescriptionsJSONArray.add(attributeDescriptionJSON1);
		attributeDescriptionsJSONArray.add(attributeDescriptionJSON2);
		attributeDescriptionsJSONArray.add(attributeDescriptionJSON3);

		final String attributeNamesJSONArrayString = objectMapper.writeValueAsString(attributeDescriptionsJSONArray);

		final Response response = target().path("/" + schema.getId()).request(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(attributeNamesJSONArrayString));

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final Schema updatedSchema = objectMapper.readValue(responseString, Schema.class);

		final List<String> attributeURIs = Lists.newLinkedList();
		attributeURIs.add(attributeUri1);
		attributeURIs.add(attributeUri2);
		attributeURIs.add(attributeUri3);

		Assert.assertNotNull(updatedSchema);

		final Set<SchemaAttributePathInstance> attributePaths = updatedSchema.getUniqueAttributePaths();

		Assert.assertNotNull(attributePaths);

		boolean foundAttributePath = false;

		for (final SchemaAttributePathInstance attributePath : attributePaths) {

			final List<Attribute> attributes = attributePath.getAttributePath().getAttributePath();

			Assert.assertNotNull(attributes);

			final Iterator<String> attributeURIsIter = attributeURIs.iterator();

			boolean match = false;

			for (final Attribute attribute : attributes) {

				Assert.assertNotNull(attribute.getName());

				if (!attributeURIsIter.hasNext()) {

					match = false;

					break;
				}

				final String attributeURI = attributeURIsIter.next();

				if (!attribute.getUri().equals(attributeURI)) {

					match = false;

					break;
				}

				match = true;
			}

			if (match && attributeURIs.size() == attributes.size()) {

				foundAttributePath = true;

				break;
			}
		}

		Assert.assertTrue(foundAttributePath);
	}

	@Test
	public void testAddAttributePath2() throws Exception {

		final Schema schema = createObjectInternal();

		final String schemaNamespaceURI = SchemaUtils.determineSchemaNamespaceURI(schema.getId());
		final String attributeName1 = "attribute one";
		final String attributeUri1 = SchemaUtils.mintAttributeURI(attributeName1, schemaNamespaceURI);

		final SchemaAttributePathInstance baseAttributePath = schema.getUniqueAttributePaths().iterator().next();
		final Long baseAttributePathId = baseAttributePath.getAttributePath().getId();

		final Map<String, String> jsonMap = Maps.newHashMap();
		jsonMap.put("name", attributeName1);
		jsonMap.put("uri", attributeUri1);
		final String payloadJson = objectMapper.writeValueAsString(jsonMap);

		final Response response = target().path("/" + schema.getId() + "/attributepaths/" + baseAttributePathId)
				.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(payloadJson, MediaType.APPLICATION_JSON_TYPE));

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final Schema updatedSchema = objectMapper.readValue(responseString, Schema.class);

		final List<String> attributeURIs = Lists.newLinkedList();

		for (final Attribute attribute : baseAttributePath.getAttributePath().getAttributePath()) {

			attributeURIs.add(attribute.getUri());
		}

		attributeURIs.add(attributeUri1);

		Assert.assertNotNull(updatedSchema);

		final Set<SchemaAttributePathInstance> attributePaths = updatedSchema.getUniqueAttributePaths();

		Assert.assertNotNull(attributePaths);

		boolean foundAttributePath = false;

		for (final SchemaAttributePathInstance attributePath : attributePaths) {

			final List<Attribute> attributes = attributePath.getAttributePath().getAttributePath();

			Assert.assertNotNull(attributes);

			final Iterator<String> attributeURIsIter = attributeURIs.iterator();

			boolean match = false;

			for (final Attribute attribute : attributes) {

				if (!attributeURIsIter.hasNext()) {

					match = false;

					break;
				}

				final String attributeURI = attributeURIsIter.next();

				if (!attribute.getUri().equals(attributeURI)) {

					match = false;

					break;
				}

				match = true;
			}

			if (match && attributeURIs.size() == attributes.size()) {

				foundAttributePath = true;

				break;
			}
		}

		Assert.assertTrue(foundAttributePath);
	}

	@Override
	protected Schema updateObject(final Schema persistedSchema) throws Exception {

		final Set<SchemaAttributePathInstance> persistedAttributePaths = persistedSchema.getUniqueAttributePaths();
		final SchemaAttributePathInstance firstAttributePath = persistedAttributePaths.iterator().next();

		final SchemaAttributePathInstanceServiceTestUtils schemaAttributePathInstanceServiceTestUtils = schemaAttributePathInstancesResourceTestUtils
				.getPersistenceServiceTestUtils();
		final SchemaAttributePathInstance newFirstAttributePath = schemaAttributePathInstanceServiceTestUtils.getDctermsCreatorFOAFNameSAPI();
		Assert.assertNotNull(newFirstAttributePath);
		persistedSchema.removeAttributePath(firstAttributePath);
		persistedSchema.addAttributePath(newFirstAttributePath);

		// clasz update (with a non-persistent class)
		final String biboBookId = "http://purl.org/ontology/bibo/Bookibook";
		final String biboBookName = "bookibook";
		final Clasz biboBook = new Clasz(biboBookId, biboBookName);
		persistedSchema.setRecordClass(biboBook);

		String updateSchemaJSONString = objectMapper.writeValueAsString(persistedSchema);
		final ObjectNode updateSchemaJSON = objectMapper.readValue(updateSchemaJSONString, ObjectNode.class);

		// schema name update
		final String updateSchemaNameString = persistedSchema.getName() + " update";
		updateSchemaJSON.put("name", updateSchemaNameString);

		updateSchemaJSONString = objectMapper.writeValueAsString(updateSchemaJSON);

		final Schema expectedSchema = objectMapper.readValue(updateSchemaJSONString, Schema.class);
		expectedSchema.setContentSchema(null);

		Assert.assertNotNull("the schema JSON string shouldn't be null", updateSchemaJSONString);

		final Schema updateSchema = pojoClassResourceTestUtils.updateObject(updateSchemaJSONString, expectedSchema);

		Assert.assertEquals("persisted and updated clasz uri should be equal", updateSchema.getRecordClass().getUri(), biboBookId);
		Assert.assertEquals("persisted and updated clasz name should be equal", updateSchema.getRecordClass().getName(), biboBookName);
		Assert.assertEquals("persisted and updated schema name should be equal", updateSchema.getName(), updateSchemaNameString);

		return updateSchema;
	}

	private ObjectNode createAttributeDescription(final String name, final String uri) {

		final ObjectNode attributeDescriptionJSON = objectMapper.createObjectNode();
		attributeDescriptionJSON.put("name", name);

		if (uri != null) {

			attributeDescriptionJSON.put("uri", uri);
		}

		return attributeDescriptionJSON;
	}
}
