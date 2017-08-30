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

import java.util.LinkedList;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.proxy.ProxyContentSchema;
import org.dswarm.persistence.service.BasicDMPJPAService;

/**
 * A persistence service for {@link org.dswarm.persistence.model.schema.ContentSchema}s.
 * 
 * @author tgaengler
 */
public class ContentSchemaService extends BasicDMPJPAService<ProxyContentSchema, ContentSchema> {

	/**
	 * Creates a new content schema persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public ContentSchemaService(final Provider<EntityManager> entityManagerProvider) {

		super(ContentSchema.class, ProxyContentSchema.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br>
	 * Clear the relationship to the record identifier attribute path, the key attribute paths + value attribute path.
	 */
	@Override
	protected void prepareObjectForRemoval(final ContentSchema object) {

		// should clear the relationship to record identifier attribute path, the key attribute paths + value attribute path
		object.setRecordIdentifierAttributePath(null);
		object.setKeyAttributePaths(null);
		object.setValueAttributePath(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateObjectInternal(final ContentSchema object, final ContentSchema updateObject)
			throws DMPPersistenceException {

		final AttributePath recordIdentifierAttributePath = object.getRecordIdentifierAttributePath();
		final LinkedList<AttributePath> keyAttributePaths = object.getKeyAttributePaths();
		final AttributePath valueAttributePath = object.getValueAttributePath();

		updateObject.setRecordIdentifierAttributePath(recordIdentifierAttributePath);
		updateObject.setKeyAttributePaths(keyAttributePaths);
		updateObject.setValueAttributePath(valueAttributePath);

		super.updateObjectInternal(object, updateObject);
	}

}
