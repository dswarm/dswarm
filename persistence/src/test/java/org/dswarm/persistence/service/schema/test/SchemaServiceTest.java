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

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.service.schema.test.utils.SchemaServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SchemaServiceTest extends IDBasicJPAServiceTest<ProxySchema, Schema, SchemaService> {

	private static final Logger LOG = LoggerFactory.getLogger(SchemaServiceTest.class);

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	private SchemaServiceTestUtils sstUtils;


	public SchemaServiceTest() {
		super("schema", SchemaService.class);
		sstUtils = new SchemaServiceTestUtils();
	}


	@Test
	public void testSimpleSchema() throws Exception {

		final Schema schema = sstUtils.createDefaultSchemaFull();
		final Schema updatedSchema = updateObjectTransactional(schema).getObject();

		sstUtils.compareObjects(schema, updatedSchema);

		Assert.assertNotNull("the schema's attribute paths of the updated schema shouldn't be null", updatedSchema.getUniqueAttributePaths());
		Assert.assertEquals("the schema's attribute paths size are not equal", schema.getUniqueAttributePaths(),
				updatedSchema.getUniqueAttributePaths());
		Assert.assertEquals("the attribute path '" + attributePath1.getId() + "' of the schema are not equal",
				schema.getAttributePath(attributePath1.getId()), updatedSchema.getAttributePath(attributePath1.getId()));
		Assert.assertNotNull("the attribute path's attributes of the attribute path '" + attributePath1.getId()
				+ "' of the updated schema shouldn't be null", updatedSchema.getAttributePath(attributePath1.getId()).getAttributes());
		Assert.assertEquals("the attribute path's attributes size of attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.getAttributes(), updatedSchema.getAttributePath(attributePath1.getId()).getAttributes());
		Assert.assertEquals("the first attributes of attribute path '" + attributePath1.getId() + "' are not equal", attributePath1
				.getAttributePath().get(0), updatedSchema.getAttributePath(attributePath1.getId()).getAttributePath().get(0));
		Assert.assertNotNull("the attribute path string of attribute path '" + attributePath1.getId()
				+ "' of the update schema shouldn't be null", updatedSchema.getAttributePath(attributePath1.getId()).toAttributePath());
		Assert.assertEquals("the attribute path's strings attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.toAttributePath(), updatedSchema.getAttributePath(attributePath1.getId()).toAttributePath());
		Assert.assertNotNull("the record class of the updated schema shouldn't be null", updatedSchema.getRecordClass());
		Assert.assertEquals("the recod classes are not equal", schema.getRecordClass(), updatedSchema.getRecordClass());
		Assert.assertNotNull("the content schema of the updated schema shouldn't be null", updatedSchema.getContentSchema());
		Assert.assertEquals("the content schemata are not equal", schema.getContentSchema(), updatedSchema.getContentSchema());

		String json = null;
		try {
			json = objectMapper.writeValueAsString(schema);
		} catch( final JsonProcessingException e ) {
			LOG.error(e.getMessage(), e);
		}

		SchemaServiceTest.LOG.debug("schema json: " + json);
	}
}
