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
package org.dswarm.persistence.service.schema;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxySchemaAttributePathInstance;

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
	protected void updateObjectInternal(final SchemaAttributePathInstance object, final SchemaAttributePathInstance updateObject,
			final EntityManager entityManager) throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject, entityManager);

		final Schema subSchema = object.getSubSchema();

		updateObject.setSubSchema(subSchema);

		//final Integer ordinal = object.getOrdinal();

		//updateObject.setOrdinal(ordinal);
	}

//	@Transactional(rollbackOn = Exception.class)
	public ProxySchemaAttributePathInstance createObjectTransactional( final AttributePath attributePath ) throws DMPPersistenceException {
		SchemaAttributePathInstance sapi = new SchemaAttributePathInstance();
		sapi.setAttributePath( attributePath );
		ProxySchemaAttributePathInstance psapi = createObjectTransactional( sapi );
		return psapi;
		
//		final EntityManager em = acquire();
//		final ProxySchemaAttributePathInstance psapi = createObjectInternal( em );
//		final SchemaAttributePathInstance sapi = psapi.getObject();
//		sapi.setAttributePath(attributePath);
//		return updateObjectInternal(sapi, em, "transactional");
	}
}