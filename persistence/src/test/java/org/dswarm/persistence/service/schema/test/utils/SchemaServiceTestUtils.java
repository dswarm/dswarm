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

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Assert;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.service.test.utils.BasicDMPJPAServiceTestUtils;

public class SchemaServiceTestUtils extends BasicDMPJPAServiceTestUtils<SchemaService, ProxySchema, Schema> {

	private final AttributePathServiceTestUtils	attributePathsResourceTestUtils;

	private final ClaszServiceTestUtils			claszesResourceTestUtils;

	private final ContentSchemaServiceTestUtils	contentSchemaServiceTestUtils;

	public SchemaServiceTestUtils() {

		super(Schema.class, SchemaService.class);

		attributePathsResourceTestUtils = new AttributePathServiceTestUtils();
		claszesResourceTestUtils = new ClaszServiceTestUtils();
		contentSchemaServiceTestUtils = new ContentSchemaServiceTestUtils();
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert that both {@link Schema}ta have either no or equal attribute paths, see
	 * {@link AttributePathServiceTestUtils#compareObjects(Set, Map)} for details.<br />
	 * Assert that both {@link Schema}ta have either no or equal record classes, see
	 * {@link ClaszServiceTestUtils#compareObjects(org.dswarm.persistence.model.DMPObject, org.dswarm.persistence.model.DMPObject)} for details.<br />
	 */
	@Override
	public void compareObjects(final Schema expectedSchema, final Schema actualSchema) {

		super.compareObjects(expectedSchema, actualSchema);

		if (expectedSchema.getUniqueAttributePaths() == null || expectedSchema.getUniqueAttributePaths().isEmpty()) {

			final boolean actualSchemaHasNoAttributePaths = (actualSchema.getUniqueAttributePaths() == null || actualSchema.getUniqueAttributePaths().isEmpty());
			Assert.assertTrue("the actual schema '" + actualSchema.getId() + "' shouldn't have attribute paths", actualSchemaHasNoAttributePaths);

		} else { // !null && !empty

			final Set<AttributePath> actualAttributePaths = actualSchema.getUniqueAttributePaths();

			Assert.assertNotNull("attribute paths of actual schema '" + actualSchema.getId() + "' shouldn't be null", actualAttributePaths);
			Assert.assertFalse("attribute paths of actual schema '" + actualSchema.getId() + "' shouldn't be empty", actualAttributePaths.isEmpty());

			final Map<Long, AttributePath> actualAttributePathsMap = Maps.newHashMap();

			for (final AttributePath actualAttributePath : actualAttributePaths) {

				actualAttributePathsMap.put(actualAttributePath.getId(), actualAttributePath);
			}

			attributePathsResourceTestUtils.compareObjects(expectedSchema.getUniqueAttributePaths(), actualAttributePathsMap);
		}

		if (expectedSchema.getRecordClass() == null) {

			Assert.assertNull("the actual schema '" + actualSchema.getId() + "' shouldn't have a record class", actualSchema.getRecordClass());

		} else {

			claszesResourceTestUtils.compareObjects(expectedSchema.getRecordClass(), actualSchema.getRecordClass());
		}

		if (expectedSchema.getContentSchema() != null) {

			contentSchemaServiceTestUtils.compareObjects(expectedSchema.getContentSchema(), actualSchema.getContentSchema());
		}
	}

	public Schema createSchema(final String name, final Set<AttributePath> attributePaths, final Clasz recordClass) throws Exception {

		final Schema schema = new Schema();

		schema.setName(name);
		schema.setAttributePaths(attributePaths);
		schema.setRecordClass(recordClass);

		// update schema

		final Schema updatedSchema = createObject(schema, schema);

		Assert.assertNotNull("updated schema shouldn't be null", updatedSchema);
		Assert.assertNotNull("updated schema id shouldn't be null", updatedSchema.getId());

		return updatedSchema;
	}

	public void removeAddedAttributePathsFromOutputModelSchema(final Schema outputDataModelSchema, final Map<Long, Attribute> attributes,
			final Map<Long, AttributePath> attributePaths) throws DMPPersistenceException {

		final Set<AttributePath> outputDataModelSchemaAttributePathRemovalCandidates = Sets.newHashSet();

		// collect attribute paths of attributes that were created via processing the transformation result
		if (outputDataModelSchema != null) {

			final Set<AttributePath> outputDataModelSchemaAttributePaths = outputDataModelSchema.getUniqueAttributePaths();

			if (outputDataModelSchemaAttributePaths != null) {

				for (final AttributePath outputDataModelSchemaAttributePath : outputDataModelSchemaAttributePaths) {

					final Set<Attribute> outputDataModelSchemaAttributePathAttributes = outputDataModelSchemaAttributePath.getAttributes();

					for (final Attribute outputDataModelSchemaAttribute : outputDataModelSchemaAttributePathAttributes) {

						if (attributes.containsKey(outputDataModelSchemaAttribute.getId())) {

							// found candidate for removal

							attributePaths.put(outputDataModelSchemaAttributePath.getId(), outputDataModelSchemaAttributePath);

							// remove candidate from output data model schema
							outputDataModelSchemaAttributePathRemovalCandidates.add(outputDataModelSchemaAttributePath);
						}
					}
				}
			}
		}

		for (final AttributePath outputDataModelSchemaAttributePath : outputDataModelSchemaAttributePathRemovalCandidates) {

			assert outputDataModelSchema != null;
			outputDataModelSchema.removeAttributePath(outputDataModelSchemaAttributePath);
		}

		// update output data model schema to persist possible changes
		jpaService.updateObjectTransactional(outputDataModelSchema);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, attribute paths and record class of the schema.
	 */
	@Override
	protected Schema prepareObjectForUpdate(final Schema objectWithUpdates, final Schema object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		final Set<AttributePath> attributePaths = objectWithUpdates.getUniqueAttributePaths();

		object.setAttributePaths(attributePaths);

		final Clasz recordClass = objectWithUpdates.getRecordClass();

		object.setRecordClass(recordClass);

		return object;
	}

	@Override
	public void reset() {

		attributePathsResourceTestUtils.reset();
		claszesResourceTestUtils.reset();
	}
}
