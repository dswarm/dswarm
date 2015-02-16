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
package org.dswarm.persistence.service.schema.test.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import org.json.JSONException;
import org.junit.Assert;

import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.service.test.utils.BasicDMPJPAServiceTestUtils;

public class SchemaServiceTestUtils extends BasicDMPJPAServiceTestUtils<SchemaService, ProxySchema, Schema> {

	private final ClaszServiceTestUtils                       claszesServiceTestUtils;
	private final ContentSchemaServiceTestUtils               contentSchemaServiceTestUtils;
	private final SchemaAttributePathInstanceServiceTestUtils schemaAttributePathInstanceResourceTestUtils;

	public SchemaServiceTestUtils() {

		super(Schema.class, SchemaService.class);

		schemaAttributePathInstanceResourceTestUtils = new SchemaAttributePathInstanceServiceTestUtils(this);
		claszesServiceTestUtils = new ClaszServiceTestUtils();
		contentSchemaServiceTestUtils = new ContentSchemaServiceTestUtils(
				schemaAttributePathInstanceResourceTestUtils.getAttributePathServiceTestUtils());
	}

	public SchemaServiceTestUtils(final SchemaAttributePathInstanceServiceTestUtils schemaAttributePathInstanceResourceTestUtils) {
		super(Schema.class, SchemaService.class);
		this.schemaAttributePathInstanceResourceTestUtils = schemaAttributePathInstanceResourceTestUtils;
		claszesServiceTestUtils = new ClaszServiceTestUtils();
		contentSchemaServiceTestUtils = new ContentSchemaServiceTestUtils(
				schemaAttributePathInstanceResourceTestUtils.getAttributePathServiceTestUtils());
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert that both {@link Schema}ta have either no or equal attribute paths,
	 * see {@link AttributePathServiceTestUtils#compareObjects(Set, Map)} for
	 * details.<br />
	 * Assert that both {@link Schema}ta have either no or equal record classes,
	 * see
	 * {@link ClaszServiceTestUtils#compareObjects(org.dswarm.persistence.model.DMPObject, org.dswarm.persistence.model.DMPObject)}
	 * for details.<br />
	 */
	@Override
	public void compareObjects(final Schema expectedSchema, final Schema actualSchema) throws JsonProcessingException, JSONException {

		super.compareObjects(expectedSchema, actualSchema);

		if (expectedSchema.getUniqueAttributePaths() == null || expectedSchema.getUniqueAttributePaths().isEmpty()) {

			final boolean actualSchemaHasNoAttributePaths = (actualSchema.getUniqueAttributePaths() == null || actualSchema
					.getUniqueAttributePaths().isEmpty());
			Assert.assertTrue("the actual schema '" + actualSchema.getUuid() + "' shouldn't have attribute paths", actualSchemaHasNoAttributePaths);

		} else { // !null && !empty

			final Set<SchemaAttributePathInstance> actualAttributePaths = actualSchema.getUniqueAttributePaths();

			Assert.assertNotNull("attribute path instances of actual schema '" + actualSchema.getUuid() + "' shouldn't be null",
					actualAttributePaths);
			Assert.assertFalse("attribute path instances of actual schema '" + actualSchema.getUuid() + "' shouldn't be empty",
					actualAttributePaths.isEmpty());

			final Map<String, SchemaAttributePathInstance> actualAttributePathsMap = Maps.newHashMap();

			for (final SchemaAttributePathInstance actualAttributePath : actualAttributePaths) {

				actualAttributePathsMap.put(actualAttributePath.getUuid(), actualAttributePath);
			}

			schemaAttributePathInstanceResourceTestUtils.compareObjects(expectedSchema.getUniqueAttributePaths(), actualAttributePathsMap);
		}

		if (expectedSchema.getRecordClass() == null) {

			Assert.assertNull("the actual schema '" + actualSchema.getUuid() + "' shouldn't have a record class", actualSchema.getRecordClass());

		} else {

			claszesServiceTestUtils.compareObjects(expectedSchema.getRecordClass(), actualSchema.getRecordClass());
		}

		if (expectedSchema.getContentSchema() != null) {

			contentSchemaServiceTestUtils.compareObjects(expectedSchema.getContentSchema(), actualSchema.getContentSchema());
		}
	}

	public Schema createAndPersistSchema(final String uuid, final String name, final Collection<SchemaAttributePathInstance> attributePaths,
			final Clasz recordClass)
			throws Exception {

		final Schema schema = createSchema(uuid, name, attributePaths, recordClass);

		final Schema updatedSchema = createAndCompareObject(schema, schema);

		Assert.assertNotNull("updated schema shouldn't be null", updatedSchema);
		Assert.assertNotNull("updated schema id shouldn't be null", updatedSchema.getUuid());

		return updatedSchema;
	}

	public Schema createAndPersistSchema(final String name, final Collection<SchemaAttributePathInstance> attributePaths, final Clasz recordClass)
			throws Exception {

		return createAndPersistSchema(null, name, attributePaths, recordClass);
	}

	public Schema createSchema(final String uuid, final String name, final Collection<SchemaAttributePathInstance> attributePaths,
			final Clasz recordClass) {

		final String schemaUUID;

		if (uuid != null && !uuid.trim().isEmpty()) {

			schemaUUID = uuid;
		} else {

			schemaUUID = UUIDService.getUUID(Schema.class.getSimpleName());
		}

		final Schema schema = new Schema(schemaUUID);

		schema.setName(name);
		schema.setAttributePaths(attributePaths);
		schema.setRecordClass(recordClass);

		return schema;
	}

	public Schema createSchema(final String name, final Collection<SchemaAttributePathInstance> attributePaths,
			final Clasz recordClass) {

		return createSchema(null, name, attributePaths, recordClass);
	}

	public Schema createAndPersistSchema(final String uuid, final String name, final SchemaAttributePathInstance[] attributePaths,
			final Clasz recordClass)
			throws Exception {
		return createAndPersistSchema(uuid, name, Arrays.asList(attributePaths), recordClass);
	}

	public Schema createAndPersistSchema(final String name, final SchemaAttributePathInstance[] attributePaths, final Clasz recordClass)
			throws Exception {
		return createAndPersistSchema(name, Arrays.asList(attributePaths), recordClass);
	}

	public Schema createSchema(final String uuid, final String name, final SchemaAttributePathInstance[] attributePaths, final Clasz recordClass)
			throws Exception {
		return createSchema(uuid, name, Arrays.asList(attributePaths), recordClass);
	}

	public Schema createSchema(final String name, final SchemaAttributePathInstance[] attributePaths, final Clasz recordClass) throws Exception {
		return createSchema(name, Arrays.asList(attributePaths), recordClass);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, attribute paths and record class of the schema.
	 */
	@Override
	protected Schema prepareObjectForUpdate(final Schema objectWithUpdates, final Schema object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		final Set<SchemaAttributePathInstance> attributePaths = objectWithUpdates.getUniqueAttributePaths();

		object.setAttributePaths(attributePaths);

		final Clasz recordClass = objectWithUpdates.getRecordClass();

		object.setRecordClass(recordClass);

		return object;
	}

	@Override
	public void reset() {

		claszesServiceTestUtils.reset();
		schemaAttributePathInstanceResourceTestUtils.reset();
		contentSchemaServiceTestUtils.reset();
	}

	@Override
	public Schema createObject(final JsonNode objectDescription) throws Exception {
		// TODO create schema from object description
		return null;
	}

	@Override public Schema createObject(final String identifier) throws Exception {

		return null;
	}

	@Override public Schema createAndPersistDefaultObject() throws Exception {
		return createAndPersistSchema("Default Schema", new SchemaAttributePathInstance[] {
				schemaAttributePathInstanceResourceTestUtils.createAndPersistDefaultObject(),
				schemaAttributePathInstanceResourceTestUtils.getDctermsTitleDctermsHaspartSAPI()
		}, claszesServiceTestUtils.createAndPersistDefaultObject());
	}

	@Override public Schema createDefaultObject() throws Exception {
		return null;
	}

	@Override public Schema createAndPersistDefaultCompleteObject() throws Exception {

		final Schema schema = createAndPersistDefaultObject();
		schema.setContentSchema(contentSchemaServiceTestUtils.createAndPersistDefaultObject());

		return updateAndCompareObject(schema, schema);
	}

	public Schema createAndPersistAlternativeSchema() throws Exception {
		return createAndPersistSchema("my schema", new SchemaAttributePathInstance[] {
				schemaAttributePathInstanceResourceTestUtils.createAndPersistDefaultObject(),
				schemaAttributePathInstanceResourceTestUtils.getDctermsCreatorFOAFNameSAPI(),
				schemaAttributePathInstanceResourceTestUtils.getDctermsCreatedSAPI()
		}, claszesServiceTestUtils.createAndPersistDefaultObject());
	}

	public Schema createAlternativeSchema() throws Exception {
		return createAndPersistSchema("my schema", new SchemaAttributePathInstance[] {
				schemaAttributePathInstanceResourceTestUtils.createAndPersistDefaultObject(),
				schemaAttributePathInstanceResourceTestUtils.getDctermsCreatorFOAFNameSAPI(),
				schemaAttributePathInstanceResourceTestUtils.getDctermsCreatedSAPI()
		}, claszesServiceTestUtils.createAndPersistDefaultObject());
	}
}
