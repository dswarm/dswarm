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
package org.dswarm.persistence.service.schema.test.utils;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import org.json.JSONException;
import org.junit.Assert;

import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.proxy.ProxyContentSchema;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.schema.ContentSchemaService;
import org.dswarm.persistence.service.test.utils.BasicDMPJPAServiceTestUtils;

public class ContentSchemaServiceTestUtils extends BasicDMPJPAServiceTestUtils<ContentSchemaService, ProxyContentSchema, ContentSchema> {

	private AttributePathServiceTestUtils apstUtils;

	public ContentSchemaServiceTestUtils() {
		this(new AttributePathServiceTestUtils());
	}

	public ContentSchemaServiceTestUtils(final AttributePathServiceTestUtils attributePathServiceTestUtils) {
		super(ContentSchema.class, ContentSchemaService.class);
		this.apstUtils = attributePathServiceTestUtils;
	}

	@Override
	public void compareObjects(final ContentSchema expectedObject, final ContentSchema actualObject) throws JsonProcessingException, JSONException {
		super.compareObjects(expectedObject, actualObject);
		compareContentSchemas(expectedObject, actualObject);
	}

	public ContentSchema createContentSchema(final String name, final AttributePath recordIdentifierAttributePath,
			final LinkedList<AttributePath> keyAttributePaths, final AttributePath valueAttributePath) throws Exception {

		// TODO: think about this?
		final String contentSchemaUUID = UUIDService.getUUID(ContentSchema.class.getSimpleName());

		final ContentSchema contentSchema = new ContentSchema(contentSchemaUUID);

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

		final ContentSchema updatedContentSchema = createAndCompareObject(contentSchema, contentSchema);

		Assert.assertNotNull("updated content schema shouldn't be null", updatedContentSchema);
		Assert.assertNotNull("updated content schema id shouldn't be null", updatedContentSchema.getUuid());

		return updatedContentSchema;
	}

	private void compareContentSchemas(final ContentSchema expectedContentSchema, final ContentSchema actualContentSchema)
			throws JsonProcessingException, JSONException {

		if (expectedContentSchema.getRecordIdentifierAttributePath() != null) {

			apstUtils.compareObjects(expectedContentSchema.getRecordIdentifierAttributePath(),
					actualContentSchema.getRecordIdentifierAttributePath());
		}

		if (expectedContentSchema.getKeyAttributePaths() != null && !expectedContentSchema.getKeyAttributePaths().isEmpty()) {

			final Set<AttributePath> actualUtilisedKeyAttributePaths = actualContentSchema.getUtilisedKeyAttributePaths();

			Assert.assertNotNull("key attribute paths of actual content schema '" + actualContentSchema.getUuid() + "' shouldn't be null",
					actualUtilisedKeyAttributePaths);
			Assert.assertFalse("attribute paths of actual content schema '" + actualContentSchema.getUuid() + "' shouldn't be empty",
					actualUtilisedKeyAttributePaths.isEmpty());

			final Map<String, AttributePath> actualKeyAttributePathsMap = Maps.newHashMap();

			for (final AttributePath actualKeyAttributePath : actualUtilisedKeyAttributePaths) {

				actualKeyAttributePathsMap.put(actualKeyAttributePath.getUuid(), actualKeyAttributePath);
			}

			apstUtils.compareObjects(expectedContentSchema.getUtilisedKeyAttributePaths(), actualKeyAttributePathsMap);
		}

		if (expectedContentSchema.getValueAttributePath() != null) {

			apstUtils.compareObjects(expectedContentSchema.getValueAttributePath(),
					actualContentSchema.getValueAttributePath());
		}
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, record identifier attribute path, key attribute paths and
	 * value attribute path of the content schema.
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
		//		apstUtils.reset();
	}

	@Override
	public ContentSchema createObject(final JsonNode objectDescription) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public ContentSchema createObject(final String identifier) throws Exception {
		return null;
	}

	@Override public ContentSchema createAndPersistDefaultObject() throws Exception {

		final ContentSchema contentSchema = createDefaultObject();
		return createContentSchema(contentSchema);
	}

	@Override public ContentSchema createDefaultObject() throws Exception {

		// TODO: think about this?
		final String contentSchemaUUID = UUIDService.getUUID(ContentSchema.class.getSimpleName());

		final ContentSchema contentSchema = new ContentSchema(contentSchemaUUID);
		contentSchema.setName("Default Content Schema");
		contentSchema.addKeyAttributePath(apstUtils.createAndPersistDefaultObject());
		contentSchema.addKeyAttributePath(apstUtils.getDctermsTitleDctermHaspartAP());
		contentSchema.addKeyAttributePath(apstUtils.getDctermsCreatedAP());
		contentSchema.setValueAttributePath(apstUtils.getRDFValueAP());
		contentSchema.setRecordIdentifierAttributePath(apstUtils.getMABXMLIDAP());
		return contentSchema;
	}
}
