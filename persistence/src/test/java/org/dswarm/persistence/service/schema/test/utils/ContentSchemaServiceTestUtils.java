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
package org.dswarm.persistence.service.schema.test.utils;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import org.junit.Assert;

import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.proxy.ProxyContentSchema;
import org.dswarm.persistence.service.schema.ContentSchemaService;
import org.dswarm.persistence.service.test.utils.BasicDMPJPAServiceTestUtils;

public class ContentSchemaServiceTestUtils extends BasicDMPJPAServiceTestUtils<ContentSchemaService, ProxyContentSchema, ContentSchema> {

	private final AttributePathServiceTestUtils	attributePathsResourceTestUtils;

	public ContentSchemaServiceTestUtils() {

		super(ContentSchema.class, ContentSchemaService.class);

		attributePathsResourceTestUtils = new AttributePathServiceTestUtils();
	}

	@Override
	public void compareObjects(final ContentSchema expectedObject, final ContentSchema actualObject) {

		super.compareObjects(expectedObject, actualObject);

		compareContentSchemas(expectedObject, actualObject);
	}

	public ContentSchema createContentSchema(final String name, final AttributePath recordIdentifierAttributePath, final LinkedList<AttributePath> keyAttributePaths,
			final AttributePath valueAttributePath) throws Exception {

		final ContentSchema contentSchema = new ContentSchema();

		contentSchema.setName(name);
		contentSchema.setRecordIdentifierAttributePath(recordIdentifierAttributePath);
		contentSchema.setKeyAttributePaths(keyAttributePaths);
		contentSchema.setValueAttributePath(valueAttributePath);

		return createContentSchemaInternal(contentSchema);
	}

	public ContentSchema createContentSchema(final ContentSchema contentSchema) throws Exception {

		return createContentSchemaInternal(contentSchema);
	}

	private ContentSchema createContentSchemaInternal(final ContentSchema contentSchema) throws Exception {

		// update content schema

		final ContentSchema updatedContentSchema = createObject(contentSchema, contentSchema);

		Assert.assertNotNull("updated content schema shouldn't be null", updatedContentSchema);
		Assert.assertNotNull("updated content schema id shouldn't be null", updatedContentSchema.getId());

		return updatedContentSchema;
	}

	private void compareContentSchemas(final ContentSchema expectedContentSchema, final ContentSchema actualContentSchema) {

		if (expectedContentSchema.getRecordIdentifierAttributePath() != null) {

			attributePathsResourceTestUtils
					.compareObjects(expectedContentSchema.getRecordIdentifierAttributePath(), actualContentSchema.getRecordIdentifierAttributePath());
		}

		if (expectedContentSchema.getKeyAttributePaths() != null && !expectedContentSchema.getKeyAttributePaths().isEmpty()) {

			final Set<AttributePath> actualUtilisedKeyAttributePaths = actualContentSchema.getUtilisedKeyAttributePaths();

			Assert.assertNotNull("key attribute paths of actual content schema '" + actualContentSchema.getId() + "' shouldn't be null",
					actualUtilisedKeyAttributePaths);
			Assert.assertFalse("attribute paths of actual content schema '" + actualContentSchema.getId() + "' shouldn't be empty",
					actualUtilisedKeyAttributePaths.isEmpty());

			final Map<Long, AttributePath> actualKeyAttributePathsMap = Maps.newHashMap();

			for (final AttributePath actualKeyAttributePath : actualUtilisedKeyAttributePaths) {

				actualKeyAttributePathsMap.put(actualKeyAttributePath.getId(), actualKeyAttributePath);
			}

			attributePathsResourceTestUtils.compareObjects(expectedContentSchema.getUtilisedKeyAttributePaths(), actualKeyAttributePathsMap);
		}

		if (expectedContentSchema.getValueAttributePath() != null) {

			attributePathsResourceTestUtils
					.compareObjects(expectedContentSchema.getValueAttributePath(), actualContentSchema.getValueAttributePath());
		}
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, record identifier attribute path, key attribute paths and value attribute path of the content schema.
	 */
	@Override
	protected ContentSchema prepareObjectForUpdate(final ContentSchema objectWithUpdates, final ContentSchema object) {

		final AttributePath recordIdentifierAttributePath = objectWithUpdates.getRecordIdentifierAttributePath();

		object.setRecordIdentifierAttributePath(recordIdentifierAttributePath);

		super.prepareObjectForUpdate(objectWithUpdates, object);

		final LinkedList<AttributePath> keyAttributePaths = objectWithUpdates.getKeyAttributePaths();

		object.setKeyAttributePaths(keyAttributePaths);

		final AttributePath valueAttributePath = objectWithUpdates.getValueAttributePath();

		object.setValueAttributePath(valueAttributePath);

		return object;
	}

	@Override
	public void reset() {

		attributePathsResourceTestUtils.reset();
	}
}
