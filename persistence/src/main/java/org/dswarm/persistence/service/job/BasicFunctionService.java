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
package org.dswarm.persistence.service.job;

import java.util.LinkedList;

import javax.persistence.EntityManager;

import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.proxy.ProxyBasicFunction;
import org.dswarm.persistence.service.ExtendedBasicDMPJPAService;

/**
 * A generic persistence service for {@link Function}s.
 * 
 * @author tgaengler
 * @param <FUNCTIONIMPL> a concrete {@link Function} implementation
 */
public abstract class BasicFunctionService<PROXYFUNCTIONIMPL extends ProxyBasicFunction<FUNCTIONIMPL>, FUNCTIONIMPL extends Function> extends
		ExtendedBasicDMPJPAService<PROXYFUNCTIONIMPL, FUNCTIONIMPL> {

	/**
	 * Creates a new persistence service for the given concrete {@link Function} implementation and the entity manager provider.
	 * 
	 * @param clasz a concrete {@link Function} implementation
	 * @param entityManagerProvider an entity manager provider
	 */
	protected BasicFunctionService(final Class<FUNCTIONIMPL> clasz, final Class<PROXYFUNCTIONIMPL> proxyClasz,
			final Provider<EntityManager> entityManagerProvider) {

		super(clasz, proxyClasz, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepareObjectForRemoval(final FUNCTIONIMPL object) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateObjectInternal(final FUNCTIONIMPL object, final FUNCTIONIMPL updateObject)
			throws DMPPersistenceException {

		final LinkedList<String> parameters = object.getParameters();

		updateObject.setParameters(parameters);

		updateObject.setFunctionDescription(object.getFunctionDescription());

		super.updateObjectInternal(object, updateObject);
	}

}
