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
package org.dswarm.persistence.service.schema.test;

import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxySchemaAttributePathInstance;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.dswarm.persistence.service.schema.test.utils.AttributeServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.SchemaAttributePathInstanceServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;

public class SchemaAttributePathInstanceServiceTest extends
		IDBasicJPAServiceTest<ProxySchemaAttributePathInstance, SchemaAttributePathInstance, SchemaAttributePathInstanceService> {

	private static final Logger LOG = LoggerFactory.getLogger(SchemaServiceTest.class);

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	// TODO: generalize
	private SchemaAttributePathInstanceServiceTestUtils sapisUtils;

	public SchemaAttributePathInstanceServiceTest() {
		super("schema attribute path instance", SchemaAttributePathInstanceService.class);
	}

	@Override
	protected void initObjects() {
		super.initObjects();

		// TODO: generalize
		sapisUtils = new SchemaAttributePathInstanceServiceTestUtils();
	}

	@Test
	public void idGenerationTest2() throws Exception {
		LOG.debug("start id generation test for " + type);

		final Set<SchemaAttributePathInstance> objects = Sets.newLinkedHashSet();
		for (int i = 0; i < 10; i++) {
			objects.add(sapisUtils.createDefaultObject());
		}
		Assert.assertEquals(type + "s set size should be 10", 10, objects.size());
		LOG.debug("end id generation test for " + type);
	}

	@Test
	public void testSimpleSchemaAttributePathInstance() throws Exception {

		final ObjectNode objectDescription = objectMapper.createObjectNode();

		final ArrayNode attributeIds = objectMapper.createArrayNode();
		attributeIds.add(AttributeServiceTestUtils.DCTERMS_TITLE);
		attributeIds.add(AttributeServiceTestUtils.DCTERMS_HASPART);
		attributeIds.add(AttributeServiceTestUtils.DCTERMS_TITLE);

		objectDescription.set("attribute_ids", attributeIds);

		final SchemaAttributePathInstance sapi = sapisUtils.createObject(objectDescription);

		final SchemaAttributePathInstance updatedMappingAttributePathInstance = sapisUtils.updateAndCompareObject(sapi, sapi);

		String json = null;
		try {
			json = objectMapper.writeValueAsString(updatedMappingAttributePathInstance);
		} catch (final JsonProcessingException e) {
			LOG.error(e.getMessage(), e);
		}
		SchemaAttributePathInstanceServiceTest.LOG.debug("schema attribute path instance json: " + json);
	}

	@Test
	public void testCompleteSchemaAttributePathInstance() throws Exception {
		SchemaAttributePathInstanceServiceTest.LOG.debug("start complete schema attribute path instance test");

		final SchemaAttributePathInstance sapi = sapisUtils.createDefaultCompleteObject();
		final SchemaAttributePathInstance updatedAttributePath = sapisUtils.updateAndCompareObject(sapi, sapi);

		String json = null;

		try {
			json = objectMapper.writeValueAsString(updatedAttributePath);
		} catch( final JsonProcessingException e ) {
			LOG.error(e.getMessage(), e);
		}

		SchemaAttributePathInstanceServiceTest.LOG.debug("attribute path json: " + json);
		SchemaAttributePathInstanceServiceTest.LOG.debug("end complete schema attribute path instance test");
	}
}
