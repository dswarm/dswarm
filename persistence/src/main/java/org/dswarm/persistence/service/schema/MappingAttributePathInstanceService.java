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
import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyMappingAttributePathInstance;

/**
 * A persistence service for {@link MappingAttributePathInstance}s.
 * 
 * @author tgaengler
 */
public class MappingAttributePathInstanceService extends
		AttributePathInstanceService<ProxyMappingAttributePathInstance, MappingAttributePathInstance> {

	/**
	 * Creates a new mapping attribute path instance persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public MappingAttributePathInstanceService(final Provider<EntityManager> entityManagerProvider) {

		super(MappingAttributePathInstance.class, ProxyMappingAttributePathInstance.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Clears the relationship to the input attribute paths, output attribute path, input filter and output filter.
	 */
	@Override
	protected void prepareObjectForRemoval(final MappingAttributePathInstance object) {

		super.prepareObjectForRemoval(object);

		// should clear the relationship to the filter
		object.setFilter(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateObjectInternal(final MappingAttributePathInstance object, final MappingAttributePathInstance updateObject) throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject);

		final Filter filter = object.getFilter();

		updateObject.setFilter(filter);

		final Integer ordinal = object.getOrdinal();

		updateObject.setOrdinal(ordinal);
	}
}
