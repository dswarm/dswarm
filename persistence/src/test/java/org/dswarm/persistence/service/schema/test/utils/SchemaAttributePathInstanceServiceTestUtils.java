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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxySchemaAttributePathInstance;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;

import com.fasterxml.jackson.databind.JsonNode;

public class SchemaAttributePathInstanceServiceTestUtils extends AttributePathInstanceServiceTestUtils<SchemaAttributePathInstanceService, ProxySchemaAttributePathInstance, SchemaAttributePathInstance> {

	private SchemaServiceTestUtils sstUtils;
	private AttributePathServiceTestUtils apstUtils;
	
	
	public SchemaAttributePathInstanceServiceTestUtils() {
		super(SchemaAttributePathInstance.class, SchemaAttributePathInstanceService.class);
	}

	@Override
	protected void initObjects() {
		super.initObjects();
		sstUtils = new SchemaServiceTestUtils();
		apstUtils = new AttributePathServiceTestUtils();
	}
	

	/**
	 * {@inheritDoc}<br />
	 *
	 * @param expectedSchemaAttributePathInstance
	 * @param actualSchemaAttributePathInstance
	 */
	@Override
	public void compareObjects( final SchemaAttributePathInstance expectedSchemaAttributePathInstance,
			final SchemaAttributePathInstance actualSchemaAttributePathInstance ) {

		super.compareObjects(expectedSchemaAttributePathInstance, actualSchemaAttributePathInstance);

		assertEquals("the subschema should be equal", expectedSchemaAttributePathInstance.getSubSchema(),
				actualSchemaAttributePathInstance.getSubSchema());
	}


	public SchemaAttributePathInstance createSchemaAttributePathInstance( final String name, final AttributePath attributePath, final Schema subSchema ) throws Exception {
		final SchemaAttributePathInstance schemaAttributePathInstance = new SchemaAttributePathInstance();
		schemaAttributePathInstance.setName(name);
		schemaAttributePathInstance.setAttributePath(attributePath);
		schemaAttributePathInstance.setSubSchema(subSchema);
		final SchemaAttributePathInstance updatedSchemaAttributePathInstance = createObject(schemaAttributePathInstance,schemaAttributePathInstance);
		assertNotNull(updatedSchemaAttributePathInstance.getId());
		return updatedSchemaAttributePathInstance;
	}


	public SchemaAttributePathInstance createSchemaAttributePathInstance( final String name, final AttributePath attributePath ) throws Exception {
		return createSchemaAttributePathInstance(name, attributePath, null);
	}


	public SchemaAttributePathInstance createSchemaAttributePathInstance( final AttributePath attributePath ) throws Exception {
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
	public SchemaAttributePathInstance createSchemaAttributePathInstance( final Attribute attribute ) throws Exception {
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
	public SchemaAttributePathInstance createSchemaAttributePathInstance( final Attribute attribute, Schema subSchema ) throws Exception {
		AttributePath attributePath = attributePathServiceTestUtils.createAttributePath(attribute);
		return createSchemaAttributePathInstance(null, attributePath, subSchema);
	}

	
	/**
	 * Constructs a sample attribute path instance consisting of the following attributes:<br>
	 * "http://purl.org/dc/terms/title"<br>
	 * "http://purl.org/dc/terms/hasPart"
	 * @return
	 * @throws Exception
	 */
	public SchemaAttributePathInstance createDefaultSchemaAttributePathInstance() throws Exception {
		return createSchemaAttributePathInstance( apstUtils.createDefaultAttributePath() );
	}
	
	/**
	 * Creates a sample attribute path instance based on the default method but with a full set of its properties
	 * @see #createDefaultSchemaAttributePathInstance()
	 * @return
	 * @throws Exception
	 */
	public SchemaAttributePathInstance createDefaultSchemaAttributePathInstanceFull() throws Exception {
		SchemaAttributePathInstance sapi = createSchemaAttributePathInstance( apstUtils.createDefaultAttributePath() );
		sapi.setSubSchema( sstUtils.createDefaultSchema() );
		return sapi;
	}

	/**
	 * {@inheritDoc}<br/>
	 */
	@Override
	protected SchemaAttributePathInstance prepareObjectForUpdate( final SchemaAttributePathInstance objectWithUpdates, final SchemaAttributePathInstance object ) {
		super.prepareObjectForUpdate(objectWithUpdates, object);
		object.setSubSchema(objectWithUpdates.getSubSchema());
		return object;
	}


	@Override
	protected SchemaAttributePathInstance createAttributePathInstance( String name, AttributePath attributePath, JsonNode objectDescription ) throws Exception {
		//TODO externalize keys (sub_schema)
		JsonNode schemaJson = objectDescription.get("sub_schema") != null ? objectDescription.get("sub_schema") : null;
		Schema subSchema = sstUtils.getObject( schemaJson );
		return createSchemaAttributePathInstance(name, attributePath, subSchema);
	}
	
	
}
