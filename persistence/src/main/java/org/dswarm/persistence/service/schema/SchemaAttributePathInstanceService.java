/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.service.schema;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxySchemaAttributePathInstance;
import org.dswarm.persistence.service.UUIDService;

/**
 * A persistence service for {@link SchemaAttributePathInstance}s.
 *
 * @author polowins - adapted from {@link MappingAttributePathInstanceService}
 */
public class SchemaAttributePathInstanceService extends
		AttributePathInstanceService<ProxySchemaAttributePathInstance, SchemaAttributePathInstance> {

	/**
	 * Creates a new schema attribute path instance persistence service with the given entity manager provider.
	 *
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public SchemaAttributePathInstanceService(final Provider<EntityManager> entityManagerProvider) {

		super(SchemaAttributePathInstance.class, ProxySchemaAttributePathInstance.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Clears the relationship to the attribute paths and the (optional) sub-schema.
	 */
	@Override
	protected void prepareObjectForRemoval(final SchemaAttributePathInstance object) {

		super.prepareObjectForRemoval(object);

		// should clear the relationship to the subschema
		object.setSubSchema(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateObjectInternal(final SchemaAttributePathInstance object, final SchemaAttributePathInstance updateObject)
			throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject);

		final Schema subSchema = object.getSubSchema();
		final Boolean required = object.isRequired();
		final Boolean multivalue = object.isMultivalue();

		updateObject.setSubSchema(subSchema);
		updateObject.setRequired(required);
		updateObject.setMultivalue(multivalue);
	}

	public ProxySchemaAttributePathInstance createObjectTransactional(final AttributePath attributePath) throws DMPPersistenceException {

		final String uuid = UUIDService.getUUID(SchemaAttributePathInstance.class.getSimpleName());

		final SchemaAttributePathInstance sapi = new SchemaAttributePathInstance(uuid);

		sapi.setAttributePath(attributePath);

		return createObjectTransactional(sapi);
	}

	public ProxySchemaAttributePathInstance createObjectTransactional(final AttributePath attributePath,
	                                                                  final Boolean required,
	                                                                  final Boolean multivalue) throws DMPPersistenceException {

		final String uuid = UUIDService.getUUID(SchemaAttributePathInstance.class.getSimpleName());

		final SchemaAttributePathInstance sapi = new SchemaAttributePathInstance(uuid);

		sapi.setAttributePath(attributePath);
		sapi.setRequired(required);
		sapi.setMultivalue(multivalue);

		return createObjectTransactional(sapi);
	}
}
