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
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.service.schema.SchemaService;

public abstract class SchemaBuilder extends GuicedTest {

	private final ObjectMapper	objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);
	private static final Logger	LOG				= LoggerFactory.getLogger(SchemaBuilder.class);
	protected String			prefixPaths		= "";

	public SchemaBuilder() {
		super();
	}

	public abstract Schema buildSchema();

	protected Schema createSchema(final String name, final Set<SchemaAttributePathInstance> attributePathInstances, final Clasz recordClass) {

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
		schema.setAttributePaths(attributePathInstances);
		schema.setRecordClass(recordClass);

		// update schema

		Schema updatedSchema = null;

		try {

			updatedSchema = schemaService.updateObjectTransactional(schema).getObject();
			
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the schema of id = '" + schema.getId() + "'", false);
		}
		
		// TODO move this to test? check if still correct after switching to schema attribute path instances
		
		Assert.assertNotNull("updated schema shouldn't be null", updatedSchema);
		Assert.assertNotNull("updated schema id shouldn't be null", updatedSchema.getId());

		final SchemaAttributePathInstance attributePathInstance = attributePathInstances.iterator().next();
		final AttributePath attributePath = attributePathInstance.getAttributePath();

		Assert.assertNotNull("the attribute path instances of the updated schema shouldn't be null",
				updatedSchema.getUniqueAttributePaths());
		
		Assert.assertEquals("the attribute path instances of the updated schema are not equal",
				schema.getUniqueAttributePaths(), updatedSchema.getUniqueAttributePaths());
		
		Assert.assertEquals("the attribute path instance '" + attributePathInstance.getId() + "' of the updated schema is not equal",
				schema.getAttributePath(attributePathInstance.getId()), updatedSchema.getAttributePath(attributePathInstance.getId()));
		
		Assert.assertNotNull("the attributes of the attribute path '" + attributePath.getId()
				+ "' of the updated schema shouldn't be null",
				updatedSchema.getAttributePath(attributePathInstance.getId()).getAttributePath().getAttributes());
		
		Assert.assertEquals("the attributes of attribute path '" + attributePath.getId() + "' are not equal",
				attributePath.getAttributes(),
				updatedSchema.getAttributePath(attributePathInstance.getId()).getAttributePath().getAttributes());
		
		Assert.assertEquals("the first attribute of attribute path '" + attributePath.getId() + "' is not equal",
				attributePath.getAttributePath().get(0),
				updatedSchema.getAttributePath(attributePathInstance.getId()).getAttributePath().getAttributePath().get(0));
		
		Assert.assertNotNull("the attribute path string of attribute path '" + attributePathInstance.getId() + "' of the update schema shouldn't be null",
				updatedSchema.getAttributePath(attributePathInstance.getId()).getAttributePath().toAttributePath());
		
		Assert.assertEquals("the attribute path's strings attribute path '" + attributePathInstance.getId() + "' are not equal",
				attributePath.toAttributePath(),
				updatedSchema.getAttributePath(attributePathInstance.getId()).getAttributePath().toAttributePath());
		
		Assert.assertNotNull("the record class of the updated schema shouldn't be null", updatedSchema.getRecordClass());
		
		Assert.assertEquals("the record classes of the updated schema are not equal", schema.getRecordClass(), updatedSchema.getRecordClass());

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
