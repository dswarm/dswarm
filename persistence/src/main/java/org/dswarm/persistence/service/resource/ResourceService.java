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

import java.util.Set;

import javax.persistence.EntityManager;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.model.resource.proxy.ProxyResource;
import org.dswarm.persistence.service.ExtendedBasicDMPJPAService;

/**
 * A persistence service for {@link Resource}s.
 *
 * @author tgaengler
 */
public class ResourceService extends ExtendedBasicDMPJPAService<ProxyResource, Resource> {

	/**
	 * Creates a new resource persistence service with the given entity manager provider.
	 *
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public ResourceService(final Provider<EntityManager> entityManagerProvider) {

		super(Resource.class, ProxyResource.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Clears the relationship to configurations.
	 */
	@Override
	protected void prepareObjectForRemoval(final Resource object) {

		object.setConfigurations(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateObjectInternal(final Resource object, final Resource updateObject)
			throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject);

		final Set<Configuration> configurations = object.getConfigurations();

		updateObject.setConfigurations(configurations);

		final ResourceType type = object.getType();

		updateObject.setType(type);

		final ObjectNode attributes = object.getAttributes();

		updateObject.setAttributes(attributes);
	}
}
