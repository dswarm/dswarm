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

import java.util.Collection;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.service.BasicDMPJPAService;

/**
 * A persistence service for {@link Schema}s.
 *
 * @author tgaengler
 */
public class SchemaService extends BasicDMPJPAService<ProxySchema, Schema> {

	/**
	 * Creates a new schema persistence service with the given entity manager provider.
	 *
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public SchemaService(final Provider<EntityManager> entityManagerProvider) {

		super(Schema.class, ProxySchema.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br>
	 * Clear the relationship to the attribute paths + record class.
	 */
	@Override
	protected void prepareObjectForRemoval(final Schema object) {

		// should clear the relationship to the attribute paths + record class
		object.setAttributePaths(null);
		object.setRecordClass(null);
		object.setContentSchema(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateObjectInternal(final Schema object, final Schema updateObject)
			throws DMPPersistenceException {

		final String baseURI = object.getBaseURI();
		final Collection<SchemaAttributePathInstance> attributePaths = object.getAttributePaths();
		final Clasz recordClass = object.getRecordClass();
		final ContentSchema contentSchema = object.getContentSchema();

		updateObject.setBaseURI(baseURI);

		// if (attributePaths != null) {
		//
		// for (final AttributePath attributePath : attributePaths) {
		//
		// final AttributePath managedAttributePath = entityManager.merge(attributePath);
		// updateObject.addAttributePath(managedAttributePath);
		// }
		//
		// } else {
		//
		updateObject.setAttributePaths(attributePaths);
		// }

		updateObject.setRecordClass(recordClass);
		updateObject.setContentSchema(contentSchema);

		super.updateObjectInternal(object, updateObject);
	}

}
