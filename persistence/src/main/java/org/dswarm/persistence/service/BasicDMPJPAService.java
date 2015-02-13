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
package org.dswarm.persistence.service;

import javax.persistence.EntityManager;

import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.BasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.ProxyBasicDMPJPAObject;

/**
 * A generic persistence service implementation for {@link BasicDMPJPAObject}s, i.e., objects where the identifier will be
 * generated by the database and that can have a name.
 *
 * @param <POJOCLASS> the concrete POJO class
 * @author tgaengler
 */
public abstract class BasicDMPJPAService<PROXYPOJOCLASS extends ProxyBasicDMPJPAObject<POJOCLASS>, POJOCLASS extends BasicDMPJPAObject> extends
		BasicJPAService<PROXYPOJOCLASS, POJOCLASS> {

	/**
	 * Creates a new persistence service for the given concrete POJO class and the entity manager provider.
	 *
	 * @param clasz                 a concrete POJO class
	 * @param entityManagerProvider an entity manager provider
	 */
	protected BasicDMPJPAService(final Class<POJOCLASS> clasz, final Class<PROXYPOJOCLASS> proxyClasz,
			final Provider<EntityManager> entityManagerProvider) {

		super(clasz, proxyClasz, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateObjectInternal(final POJOCLASS object, final POJOCLASS updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final String name = object.getName();

		updateObject.setName(name);
	}
}
