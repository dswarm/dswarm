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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxySchemaAttributePathInstance;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.json.JSONException;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SchemaAttributePathInstanceServiceTestUtils extends
		AttributePathInstanceServiceTestUtils<SchemaAttributePathInstanceService, ProxySchemaAttributePathInstance, SchemaAttributePathInstance> {

	private SchemaServiceTestUtils sstUtils;

	public SchemaAttributePathInstanceServiceTestUtils() {

		super(SchemaAttributePathInstance.class, SchemaAttributePathInstanceService.class);

		sstUtils = new SchemaServiceTestUtils(this);
	}

	public SchemaAttributePathInstanceServiceTestUtils(final SchemaServiceTestUtils schemaServiceTestUtils) {

		super(SchemaAttributePathInstance.class, SchemaAttributePathInstanceService.class);

		sstUtils = schemaServiceTestUtils;
	}

	@Override
	public SchemaAttributePathInstance createObject(String identifier) throws Exception {

		return null;
	}

	@Override
	public SchemaAttributePathInstance createAndPersistDefaultObject() throws Exception {

		return getDctermsTitleDctermsHaspartDctermsTitleSAPI();
	}

	@Override
	public SchemaAttributePathInstance createDefaultObject() throws Exception {
		return null;
	}

	public SchemaAttributePathInstance getDctermsTitleDctermsHaspartDctermsTitleSAPI() throws Exception {

		return createSchemaAttributePathInstance(attributePathServiceTestUtils.getDctermsTitleDctermHaspartDctermsTitleAP());
	}

	public SchemaAttributePathInstance getDctermsTitleDctermsHaspartSAPI() throws Exception {

		return createSchemaAttributePathInstance(attributePathServiceTestUtils.getDctermsTitleDctermHaspartAP());
	}

	public SchemaAttributePathInstance getDctermsCreatorFOAFNameSAPI() throws Exception {

		return createSchemaAttributePathInstance(attributePathServiceTestUtils.getDctermsCreatorFoafNameAP());
	}

	public SchemaAttributePathInstance getDctermsCreatedSAPI() throws Exception {

		return createSchemaAttributePathInstance(attributePathServiceTestUtils.getDctermsCreatedAP());
	}

	/**
	 * {@inheritDoc}<br />
	 *
	 * @param expectedSchemaAttributePathInstance
	 * @param actualSchemaAttributePathInstance
	 */
	@Override
	public void compareObjects(final SchemaAttributePathInstance expectedSchemaAttributePathInstance,
	                           final SchemaAttributePathInstance actualSchemaAttributePathInstance) throws JsonProcessingException, JSONException {

		super.compareObjects(expectedSchemaAttributePathInstance, actualSchemaAttributePathInstance);

		assertEquals("the subschema should be equal", expectedSchemaAttributePathInstance.getSubSchema(),
				actualSchemaAttributePathInstance.getSubSchema());
	}

	public SchemaAttributePathInstance createOrGetSchemaAttributePathInstance(final String uuid, final String name, final AttributePath attributePath, final Schema subSchema) throws Exception {

		final Optional<SchemaAttributePathInstance> optionalSapi = Optional.ofNullable(this.getJpaService().getObject(uuid));

		if(optionalSapi.isPresent()) {

			return optionalSapi.get();
		}

		return createSchemaAttributePathInstance(name, attributePath, subSchema);
	}

	public SchemaAttributePathInstance createSchemaAttributePathInstance(final String name, final AttributePath attributePath, final Schema subSchema)
			throws Exception {

		final String schemattributePathInstanceUUID = UUIDService.getUUID(SchemaAttributePathInstance.class.getSimpleName());

		final SchemaAttributePathInstance schemaAttributePathInstance = new SchemaAttributePathInstance(schemattributePathInstanceUUID);
		schemaAttributePathInstance.setName(name);
		schemaAttributePathInstance.setAttributePath(attributePath);
		schemaAttributePathInstance.setSubSchema(subSchema);
		final SchemaAttributePathInstance updatedSchemaAttributePathInstance = createAndCompareObject(schemaAttributePathInstance,
				schemaAttributePathInstance);
		assertNotNull(updatedSchemaAttributePathInstance.getUuid());
		return updatedSchemaAttributePathInstance;
	}

	public SchemaAttributePathInstance createSchemaAttributePathInstance(final String name, final AttributePath attributePath) throws Exception {
		return createSchemaAttributePathInstance(name, attributePath, null);
	}

	public SchemaAttributePathInstance createOrGetSchemaAttributePathInstance(final String uuid, final AttributePath attributePath) throws Exception {

		final Optional<SchemaAttributePathInstance> optionalSapi = Optional.ofNullable(this.getJpaService().getObject(uuid));

		if(optionalSapi.isPresent()) {

			return optionalSapi.get();
		}

		return createSchemaAttributePathInstance(attributePath);
	}

	public SchemaAttributePathInstance createSchemaAttributePathInstance(final AttributePath attributePath) throws Exception {
		return createSchemaAttributePathInstance(null, attributePath, null);
	}

	/**
	 * Convenience method for creating simple attribute path instance with an
	 * attribute path of length 1 and no subschema as they are frequently needed
	 * in sub-schema contexts
	 *
	 * @param attribute
	 * @return a simple attribute path instance with no subschema
	 * @throws Exception
	 */
	public SchemaAttributePathInstance createSchemaAttributePathInstance(final Attribute attribute) throws Exception {
		AttributePath attributePath = attributePathServiceTestUtils.createAttributePath(attribute);
		return createSchemaAttributePathInstance(attributePath);
	}

	/**
	 * Convenience method for creating simple attribute path instance with an
	 * attribute path of length 1 and a subschema as they are frequently needed in
	 * sub-schema contexts
	 *
	 * @param attribute
	 * @return a simple attribute path instance with a subschema
	 * @throws Exception
	 */
	public SchemaAttributePathInstance createSchemaAttributePathInstance(final Attribute attribute, final Schema subSchema) throws Exception {
		final AttributePath attributePath = attributePathServiceTestUtils.createAttributePath(attribute);
		return createSchemaAttributePathInstance(null, attributePath, subSchema);
	}

	@Override
	public SchemaAttributePathInstance createAndPersistDefaultCompleteObject() throws Exception {

		final SchemaAttributePathInstance sapi = createAndPersistDefaultObject();
		sapi.setSubSchema(sstUtils.createAndPersistDefaultObject());

		return updateAndCompareObject(sapi, sapi);
	}

	/**
	 * {@inheritDoc}<br/>
	 */
	@Override
	protected SchemaAttributePathInstance prepareObjectForUpdate(final SchemaAttributePathInstance objectWithUpdates,
	                                                             final SchemaAttributePathInstance object) {
		super.prepareObjectForUpdate(objectWithUpdates, object);
		object.setSubSchema(objectWithUpdates.getSubSchema());
		return object;
	}

	@Override
	protected SchemaAttributePathInstance createAttributePathInstance(final String name, final AttributePath attributePath,
	                                                                  final JsonNode objectDescription) throws Exception {
		//TODO externalize keys (sub_schema)
		final JsonNode schemaJson = objectDescription.get("sub_schema") != null ? objectDescription.get("sub_schema") : null;
		final Schema subSchema = sstUtils.createObject(schemaJson);
		return createSchemaAttributePathInstance(name, attributePath, subSchema);
	}

	@Override
	public void reset() {

		super.reset();

		// ???
		//sstUtils.reset();
	}
}
