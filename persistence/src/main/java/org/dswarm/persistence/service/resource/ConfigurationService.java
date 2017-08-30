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
package org.dswarm.persistence.service.resource;

import javax.persistence.EntityManager;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.proxy.ProxyConfiguration;
import org.dswarm.persistence.service.ExtendedBasicDMPJPAService;

/**
 * A persistence service for {@link Configuration}s.
 * 
 * @author tgaengler
 */
public class ConfigurationService extends ExtendedBasicDMPJPAService<ProxyConfiguration, Configuration> {

	/**
	 * Creates a new configuration persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public ConfigurationService(final Provider<EntityManager> entityManagerProvider) {

		super(Configuration.class, ProxyConfiguration.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Clears the relationship to resources.
	 */
	@Override
	protected void prepareObjectForRemoval(final Configuration object) {

		// should clear the relationship to the resources
		object.setResources(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateObjectInternal(final Configuration object, final Configuration updateObject)
			throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject);

		// TODO: disable resource updating for now (until resource id ref resolution is implemented)

		// final Set<Resource> resources = object.getResources();
		final ObjectNode parameters = object.getParameters();

		// updateObject.setResources(resources);
		updateObject.setParameters(parameters);
	}

}
