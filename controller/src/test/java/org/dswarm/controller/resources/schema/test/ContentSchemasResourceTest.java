/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;

import org.dswarm.controller.resources.schema.test.utils.AttributePathsResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.ContentSchemasResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.proxy.ProxyContentSchema;
import org.dswarm.persistence.service.schema.ContentSchemaService;
import org.dswarm.persistence.service.schema.test.utils.AttributePathServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.ContentSchemaServiceTestUtils;

public class ContentSchemasResourceTest
		extends
		BasicResourceTest<ContentSchemasResourceTestUtils, ContentSchemaServiceTestUtils, ContentSchemaService, ProxyContentSchema, ContentSchema> {

	private AttributePathsResourceTestUtils attributePathsResourceTestUtils;

	public ContentSchemasResourceTest() {

		super(ContentSchema.class, ContentSchemaService.class, "contentschemas", "content_schema.json", new ContentSchemasResourceTestUtils());
	}

	@Override
	protected void initObjects() {

		super.initObjects();

		pojoClassResourceTestUtils = new ContentSchemasResourceTestUtils();
		attributePathsResourceTestUtils = new AttributePathsResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		final ContentSchemaServiceTestUtils contentSchemaServiceTestUtils = pojoClassResourceTestUtils.getPersistenceServiceTestUtils();
		final ContentSchema contentSchema = contentSchemaServiceTestUtils.createDefaultObject();

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(contentSchema);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);
	}

	@Override
	protected ContentSchema updateObject(final ContentSchema persistedContentSchema) throws Exception {

		final AttributePathServiceTestUtils attributePathServiceTestUtils = attributePathsResourceTestUtils.getPersistenceServiceTestUtils();

		final AttributePath blaPropertyAP = attributePathServiceTestUtils.getDctermsCreatorFoafNameAP();
		persistedContentSchema.setValueAttributePath(blaPropertyAP);

		String updateContentSchemaJSONString = objectMapper.writeValueAsString(persistedContentSchema);
		final ObjectNode updateSchemaJSON = objectMapper.readValue(updateContentSchemaJSONString, ObjectNode.class);

		// schema name update
		final String updateSchemaNameString = persistedContentSchema.getName() + " update";
		updateSchemaJSON.put("name", updateSchemaNameString);

		updateContentSchemaJSONString = objectMapper.writeValueAsString(updateSchemaJSON);

		final ContentSchema expectedContentSchema = objectMapper.readValue(updateContentSchemaJSONString, ContentSchema.class);

		Assert.assertNotNull("the content schema JSON string shouldn't be null", updateContentSchemaJSONString);

		final ContentSchema updateContentSchema = pojoClassResourceTestUtils.updateObject(updateContentSchemaJSONString, expectedContentSchema);

		final AttributePath updatedValueAttributePath = updateContentSchema.getValueAttributePath();

		Assert.assertEquals("persisted and updated value attribute path string should be equal", updatedValueAttributePath.toAttributePath(),
				blaPropertyAP.toAttributePath());
		Assert.assertEquals("persisted and updated value attribute path attribute name should be equal", updatedValueAttributePath.getAttributePath()
				.iterator().next().getName(), blaPropertyAP.getAttributePath().iterator().next().getName());
		Assert.assertEquals("persisted and updated content schema name should be equal", updateContentSchema.getName(), updateSchemaNameString);

		return updateContentSchema;
	}
}
