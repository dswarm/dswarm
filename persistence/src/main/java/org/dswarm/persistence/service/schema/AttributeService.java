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
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.service.AdvancedDMPJPAService;

/**
 * A persistence service for {@link Attribute}s.
 *
 * @author tgaengler
 */
public class AttributeService extends AdvancedDMPJPAService<ProxyAttribute, Attribute> {

	/**
	 * Creates a new attribute persistence service with the given entity manager provider.
	 *
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public AttributeService(final Provider<EntityManager> entityManagerProvider) {

		super(Attribute.class, ProxyAttribute.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepareObjectForRemoval(final Attribute object) {

		// should clear the relationship to the attribute paths
		// object.setAttributePaths(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateObjectInternal(final Attribute object, final Attribute updateObject)
			throws DMPPersistenceException {

		final String name = object.getName();
		// final Set<AttributePath> attributePaths = object.getUniqueAttributePaths();

		updateObject.setName(name);
		// updateObject.setAttributePaths(attributePaths);
	}

}
