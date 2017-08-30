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

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.job.proxy.ProxyTransformation;

/**
 * A persistence service for {@link Transformation}s.
 * 
 * @author tgaengler
 */
public class TransformationService extends BasicFunctionService<ProxyTransformation, Transformation> {

	private static final Logger	LOG	= LoggerFactory.getLogger(TransformationService.class);

	/**
	 * Creates a new transformation persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public TransformationService(final Provider<EntityManager> entityManagerProvider) {

		super(Transformation.class, ProxyTransformation.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Clears the relationship to the functions of the components and decouples the components from each other.
	 */
	@Override
	protected void prepareObjectForRemoval(final Transformation object) {

		final Set<Component> components = object.getComponents();

		if (components != null) {

			final Set<Component> componentsToBeDeleted = Sets.newCopyOnWriteArraySet(components);

			for (final Component component : componentsToBeDeleted) {

				// release functions from components of a transformation
				// and disconnect components from each other

				component.setFunction(null);
				component.setInputComponents(null);
				component.setOutputComponents(null);
			}
		}

		TransformationService.LOG.trace("transformation after prepare for removal: " + ToStringBuilder.reflectionToString(object));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateObjectInternal(final Transformation object, final Transformation updateObject)
			throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject);

		final Set<Component> components = object.getComponents();

		updateObject.setComponents(components);
	}

}
