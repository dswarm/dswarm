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

import java.util.Set;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.proxy.ProxyMapping;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.service.BasicDMPJPAService;

/**
 * A persistence service for {@link Mapping}s.
 * 
 * @author tgaengler
 */
public class MappingService extends BasicDMPJPAService<ProxyMapping, Mapping> {

	/**
	 * Creates a new mapping persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public MappingService(final Provider<EntityManager> entityManagerProvider) {

		super(Mapping.class, ProxyMapping.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Clears the relationship to the input attribute paths, output attribute path, input filter and output filter.
	 */
	@Override
	protected void prepareObjectForRemoval(final Mapping object) {

		// should clear the relationship to the input attribute paths, output attribute path + transformation function
		object.setInputAttributePaths(null);
		object.setOutputAttributePath(null);

		final Component transformation = object.getTransformation();

		if (transformation != null) {

			transformation.setFunction(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateObjectInternal(final Mapping object, final Mapping updateObject)
			throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject);

		final Set<MappingAttributePathInstance> inputAttributePaths = object.getInputAttributePaths();
		final MappingAttributePathInstance outputAttributePath = object.getOutputAttributePath();
		final Component transformation = object.getTransformation();

		updateObject.setInputAttributePaths(inputAttributePaths);
		updateObject.setOutputAttributePath(outputAttributePath);
		updateObject.setTransformation(transformation);
	}

}
