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

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.job.proxy.ProxyFilter;
import org.dswarm.persistence.service.BasicDMPJPAService;

/**
 * A persistence service for {@link Filter}s.
 * 
 * @author tgaengler
 */
public class FilterService extends BasicDMPJPAService<ProxyFilter, Filter> {

	/**
	 * Creates a new filter persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public FilterService(final Provider<EntityManager> entityManagerProvider) {

		super(Filter.class, ProxyFilter.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepareObjectForRemoval(final Filter object) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateObjectInternal(final Filter object, final Filter updateObject)
			throws DMPPersistenceException {

		final String expression = object.getExpression();

		updateObject.setExpression(expression);

		super.updateObjectInternal(object, updateObject);
	}

}
