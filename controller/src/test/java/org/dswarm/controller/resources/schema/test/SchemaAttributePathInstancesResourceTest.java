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

import org.junit.Assert;

import org.dswarm.controller.resources.schema.test.utils.AttributePathsResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.SchemaAttributePathInstancesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.SchemasResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxySchemaAttributePathInstance;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.dswarm.persistence.service.schema.test.utils.AttributePathServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.SchemaAttributePathInstanceServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.SchemaServiceTestUtils;

public class SchemaAttributePathInstancesResourceTest
		extends
		BasicResourceTest<SchemaAttributePathInstancesResourceTestUtils, SchemaAttributePathInstanceServiceTestUtils, SchemaAttributePathInstanceService, ProxySchemaAttributePathInstance, SchemaAttributePathInstance, Long> {

	private SchemasResourceTestUtils                      schemasResourceTestUtils;
	private AttributePathsResourceTestUtils               attributePathResourceTestUtils;
	private SchemaAttributePathInstancesResourceTestUtils schemaAttributePathInstanceResourceTestUtils;

	public SchemaAttributePathInstancesResourceTest() {

		super(SchemaAttributePathInstance.class, SchemaAttributePathInstanceService.class, "schemaattributepathinstances",
				"schema_attribute_path_instance.json", new SchemaAttributePathInstancesResourceTestUtils());
	}

	@Override
	protected void initObjects() {

		super.initObjects();

		pojoClassResourceTestUtils = new SchemaAttributePathInstancesResourceTestUtils();
		schemasResourceTestUtils = new SchemasResourceTestUtils();
		attributePathResourceTestUtils = new AttributePathsResourceTestUtils();
		schemaAttributePathInstanceResourceTestUtils = new SchemaAttributePathInstancesResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		final SchemaAttributePathInstance schemaAttributePathInstance = new SchemaAttributePathInstance();

		final AttributePathServiceTestUtils attributePathServiceTestUtils = attributePathResourceTestUtils.getPersistenceServiceTestUtils();
		final AttributePath attributePath = attributePathServiceTestUtils.getDctermsTitleDctermHaspartDctermsTitleAP();

		final SchemaServiceTestUtils schemaServiceTestUtils = schemasResourceTestUtils.getPersistenceServiceTestUtils();
		final Schema subSchema = schemaServiceTestUtils.createDefaultObject();

		schemaAttributePathInstance.setAttributePath(attributePath);
		schemaAttributePathInstance.setSubSchema(subSchema);

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(schemaAttributePathInstance);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);
	}

	@Override
	protected SchemaAttributePathInstance updateObject(final SchemaAttributePathInstance persistedMappingAttributePathInstance) throws Exception {

		final String updateSchemaAttributePathInstanceNameString = "new name";
		final AttributePathServiceTestUtils attributePathServiceTestUtils = attributePathResourceTestUtils.getPersistenceServiceTestUtils();
		final AttributePath newAttributePath = attributePathServiceTestUtils.getDctermsCreatedAP();

		final SchemaServiceTestUtils schemaServiceTestUtils = schemasResourceTestUtils.getPersistenceServiceTestUtils();
		final Schema subSchema = schemaServiceTestUtils.createAlternativeSchema();

		persistedMappingAttributePathInstance.setName(updateSchemaAttributePathInstanceNameString);
		persistedMappingAttributePathInstance.setAttributePath(newAttributePath);
		persistedMappingAttributePathInstance.setSubSchema(subSchema);

		final String updatedSchemaAttributePathInstanceJSONString = objectMapper.writeValueAsString(persistedMappingAttributePathInstance);

		final SchemaAttributePathInstance expectedSchemaAttributePathInstance = objectMapper.readValue(
				updatedSchemaAttributePathInstanceJSONString, SchemaAttributePathInstance.class);

		Assert.assertNotNull("the mapping attribute path instance JSON string shouldn't be null", updatedSchemaAttributePathInstanceJSONString);

		final SchemaAttributePathInstance updatedSchemaAttributePathInstance = schemaAttributePathInstanceResourceTestUtils.updateObject(
				updatedSchemaAttributePathInstanceJSONString, expectedSchemaAttributePathInstance);

		Assert.assertEquals("persisted and updated mapping attribute path name should be equal", updatedSchemaAttributePathInstance.getName(),
				updateSchemaAttributePathInstanceNameString);

		return updatedSchemaAttributePathInstance;
	}
}
