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

import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.proxy.ProxyComponent;
import org.dswarm.persistence.service.ExtendedBasicDMPJPAService;

/**
 * A persistence service for {@link Component}s.
 * 
 * @author tgaengler
 */
public class ComponentService extends ExtendedBasicDMPJPAService<ProxyComponent, Component> {

	/**
	 * Creates a new component persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public ComponentService(final Provider<EntityManager> entityManagerProvider) {

		super(Component.class, ProxyComponent.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Clears the relationship to the function, input components and output components.
	 */
	@Override
	protected void prepareObjectForRemoval(final Component object) {

		// release connections to other objects
		object.setFunction(null);
		object.setInputComponents(null);
		object.setOutputComponents(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateObjectInternal(final Component object, final Component updateObject)
			throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject);

		final Function function = object.getFunction();
		final Set<Component> inputComponents = object.getInputComponents();
		final Set<Component> outputComponents = object.getOutputComponents();
		final Map<String, String> parameterMappings = object.getParameterMappings();
		// final Transformation transformation = object.getTransformation();

		updateObject.setFunction(function);
		updateObject.setInputComponents(inputComponents);
		updateObject.setOutputComponents(outputComponents);
		updateObject.setParameterMappings(parameterMappings);
		// updateObject.setTransformation(transformation);
	}

}
