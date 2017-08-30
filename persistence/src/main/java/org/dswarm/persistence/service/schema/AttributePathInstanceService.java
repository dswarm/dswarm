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

import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.AttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePathInstance;
import org.dswarm.persistence.service.BasicDMPJPAService;

/**
 * A generic persistence service for {@link AttributePathInstance}s.
 * 
 * @author tgaengler
 * @param <ATTRIBUTEPATHIMPL> a concrete {@link AttributePathInstance} implementation
 */
public abstract class AttributePathInstanceService<PROXYATTRIBUTEPATHIMPL extends ProxyAttributePathInstance<ATTRIBUTEPATHIMPL>, ATTRIBUTEPATHIMPL extends AttributePathInstance>
		extends BasicDMPJPAService<PROXYATTRIBUTEPATHIMPL, ATTRIBUTEPATHIMPL> {

	/**
	 * Creates a new persistence service for the given concrete {@link AttributePathInstance} implementation and the entity
	 * manager provider.
	 * 
	 * @param clasz a concrete {@link AttributePathInstance} implementation
	 * @param entityManagerProvider an entity manager provider
	 */
	protected AttributePathInstanceService(final Class<ATTRIBUTEPATHIMPL> clasz, final Class<PROXYATTRIBUTEPATHIMPL> proxyClasz,
			final Provider<EntityManager> entityManagerProvider) {

		super(clasz, proxyClasz, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepareObjectForRemoval(final ATTRIBUTEPATHIMPL object) {

		object.setAttributePath(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateObjectInternal(final ATTRIBUTEPATHIMPL object, final ATTRIBUTEPATHIMPL updateObject)
			throws DMPPersistenceException {

		final AttributePath attributePath = object.getAttributePath();

		updateObject.setAttributePath(attributePath);

		super.updateObjectInternal(object, updateObject);
	}

}
