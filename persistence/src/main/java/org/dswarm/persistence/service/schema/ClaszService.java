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
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.proxy.ProxyClasz;
import org.dswarm.persistence.service.AdvancedDMPJPAService;

/**
 * A persistence service for {@link Clasz}es.
 * 
 * @author tgaengler
 */
public class ClaszService extends AdvancedDMPJPAService<ProxyClasz, Clasz> {

	/**
	 * Creates a new class persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public ClaszService(final Provider<EntityManager> entityManagerProvider) {

		super(Clasz.class, ProxyClasz.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepareObjectForRemoval(final Clasz object) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateObjectInternal(final Clasz object, final Clasz updateObject)
			throws DMPPersistenceException {

		final String name = object.getName();

		updateObject.setName(name);
	}

}
