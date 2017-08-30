/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.resources.schema.test.utils.AttributesResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.test.utils.AttributeServiceTestUtils;

public class AttributesResourceTest extends
		BasicResourceTest<AttributesResourceTestUtils, AttributeServiceTestUtils, AttributeService, ProxyAttribute, Attribute> {

	private static final Logger LOG = LoggerFactory.getLogger(AttributesResourceTest.class);

	public AttributesResourceTest() {

		super(Attribute.class, AttributeService.class, "attributes", "attribute1.json", new AttributesResourceTestUtils());
	}

	@Override protected void initObjects() {
		super.initObjects();

		pojoClassResourceTestUtils = new AttributesResourceTestUtils();
	}

	@Test
	public void testUniquenessOfAttributes() {

		AttributesResourceTest.LOG.debug("start attribute uniqueness test");

		Attribute attribute1 = null;

		try {

			attribute1 = pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
		} catch (final Exception e) {

			AttributesResourceTest.LOG.error("couldn't create attribute 1 for uniqueness test");

			Assert.assertTrue(false);
		}

		Assert.assertNotNull("attribute 1 shouldn't be null in uniqueness test", attribute1);

		Attribute attribute2 = null;

		try {

			final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
					.post(Entity.json(objectJSONString));

			Assert.assertEquals("200 OK was expected", 200, response.getStatus());

			final String responseString = response.readEntity(String.class);

			Assert.assertNotNull("the response JSON shouldn't be null", responseString);

			attribute2 = objectMapper.readValue(responseString, pojoClass);

			compareObjects(expectedObject, attribute2);

		} catch (final Exception e) {

			AttributesResourceTest.LOG.error("couldn't create attribute 2 for uniqueness test");

			Assert.assertTrue(false);
		}

		Assert.assertNotNull("attribute 2 shouldn't be null in uniqueness test", attribute2);

		Assert.assertEquals("the attributes should be equal", attribute1, attribute2);

		AttributesResourceTest.LOG.debug("end attribute uniqueness test");
	}

	@Override
	public void testPUTObject() throws Exception {

		AttributesResourceTest.LOG.debug("start attribute update test with uri manipulation");

		Attribute attribute = null;

		try {

			attribute = pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
		} catch (final Exception e) {

			AttributesResourceTest.LOG.error("couldn't create attribute for update test");

			Assert.assertTrue(false);
		}

		Assert.assertNotNull("attribute shouldn't be null in update test", attribute);

		// modify attribute for update
		attribute.setName(attribute.getName() + " update");

		String attributeJSONString = objectMapper.writeValueAsString(attribute);

		Assert.assertNotNull("attribute shouldn't be null in update test", attribute);

		Attribute updateAttribute = pojoClassResourceTestUtils.updateObject(attributeJSONString, attribute);

		Assert.assertEquals("the persisted attribute should be equal to the modified attribute for update", updateAttribute, attribute);

		final ObjectNode attributeJSON = objectMapper.readValue(attributeJSONString, ObjectNode.class);

		Assert.assertNotNull("the attribute JSON shouldn't be null", attributeJSON);

		// uniqueness doesn't allow that
		attributeJSON.put("uri", attribute.getUri().replaceAll("http", "https"));

		attributeJSONString = objectMapper.writeValueAsString(attributeJSON);

		final Attribute modifiedAttributeFromJSON = objectMapper.readValue(attributeJSONString, Attribute.class);

		Attribute modifiedAttribute = null;

		try {

			modifiedAttribute = pojoClassResourceTestUtils.createObject(attributeJSONString, modifiedAttributeFromJSON);
		} catch (final Exception e) {

			AttributesResourceTest.LOG.error("couldn't create modified attribute for update test");

			Assert.assertTrue(false);
		}

		updateAttribute = pojoClassResourceTestUtils.updateObject(attributeJSONString, modifiedAttributeFromJSON);

		Assert.assertNotNull("attribute shouldn't be null", attribute);
		Assert.assertNotNull("updated attribute shouldn't be null", updateAttribute);
		Assert.assertEquals("ids of the modified attribute should be equal", modifiedAttribute.getUuid(), updateAttribute.getUuid());
		Assert.assertNotEquals("id should be different, when uri was \"updated\" (uniqueness constraint doesn't allow update of uri)", updateAttribute.getUuid(),
				attribute.getUuid());

		Assert.assertEquals("uri's should be equal", updateAttribute.getUri(), modifiedAttribute.getUri());
		Assert.assertNotEquals("uniqueness dosn't allow update of uri", updateAttribute.getUri(), attribute.getUri());

		AttributesResourceTest.LOG.debug("end attribute update test with uri manipulation");
	}

	@Test
	public void testPUTObjectWNonExistingURI() throws Exception {

		AttributesResourceTest.LOG.debug("start attribute update test with non-existing uri (manipulation)");

		Attribute attribute = null;

		try {

			attribute = pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
		} catch (final Exception e) {

			AttributesResourceTest.LOG.error("coudln't create attribute for update test");

			Assert.assertTrue(false);
		}

		Assert.assertNotNull("attribute shouldn't be null in update test", attribute);

		// modify attribute for update
		attribute.setName(attribute.getName() + " update");

		String attributeJSONString = objectMapper.writeValueAsString(attribute);

		final ObjectNode attributeJSON = objectMapper.readValue(attributeJSONString, ObjectNode.class);

		Assert.assertNotNull("the attribut JSON shouldn't be null", attributeJSON);

		// uniqueness dosn't allow that
		attributeJSON.put("uri", attribute.getUri().replaceAll("http", "https"));

		attributeJSONString = objectMapper.writeValueAsString(attributeJSON);

		final String objectUuid = objectMapper.readValue(attributeJSONString, pojoClass).getUuid();

		Assert.assertEquals("the id of the updated object should be equal", objectUuid, attribute.getUuid());

		final Response response = target(String.valueOf(objectUuid)).request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.put(Entity.json(attributeJSONString));

		Assert.assertEquals("404 NOT FOUND was expected, i.e., no attribute with the given URI exists in the DB", 404, response.getStatus());

		AttributesResourceTest.LOG.debug("end attribute update test with non-existing uri (manipulation)");
	}
}
