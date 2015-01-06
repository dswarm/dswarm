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
package org.dswarm.persistence.service.schema.test.internalmodel;

import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.schema.SchemaService;

public abstract class SchemaBuilder extends GuicedTest {

	private final ObjectMapper	objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);
	private static final Logger	LOG				= LoggerFactory.getLogger(SchemaBuilder.class);
	protected String			prefixPaths		= "";

	public SchemaBuilder() {
		super();
	}

	public abstract Schema buildSchema();

	protected Schema createSchema(final String name, final Set<AttributePath> attributePaths, final Clasz recordClass) {

		final SchemaService schemaService = GuicedTest.injector.getInstance(SchemaService.class);

		Assert.assertNotNull("schema service shouldn't be null", schemaService);

		// create schema

		Schema schema = null;

		try {
			schema = schemaService.createObjectTransactional().getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while schema creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("schema shouldn't be null", schema);
		Assert.assertNotNull("schema id shouldn't be null", schema.getId());

		schema.setName(name);
		schema.setAttributePaths(attributePaths);
		schema.setRecordClass(recordClass);

		// update schema

		Schema updatedSchema = null;

		try {

			updatedSchema = schemaService.updateObjectTransactional(schema).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the schema of id = '" + schema.getId() + "'", false);
		}

		Assert.assertNotNull("updated schema shouldn't be null", updatedSchema);
		Assert.assertNotNull("updated schema id shouldn't be null", updatedSchema.getId());

		final AttributePath attributePath1 = attributePaths.iterator().next();

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

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedSchema);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		SchemaBuilder.LOG.debug("schema json: " + json);

		return updatedSchema;
	}

	public String getPrefixPaths() {
		return prefixPaths;
	}

}
