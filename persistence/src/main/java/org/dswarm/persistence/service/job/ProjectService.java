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
import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.proxy.ProxyProject;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.service.ExtendedBasicDMPJPAService;

/**
 * A persistence service for {@link Project}s.
 * 
 * @author tgaengler
 */
public class ProjectService extends ExtendedBasicDMPJPAService<ProxyProject, Project> {

	/**
	 * Creates a new project persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public ProjectService(final Provider<EntityManager> entityManagerProvider) {

		super(Project.class, ProxyProject.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Clears the relationship to the input data model, output data model, mappings and functions.
	 */
	@Override
	protected void prepareObjectForRemoval(final Project object) {

		// should clear the relationship to the input data model, output data model, mappings and functions
		object.setInputDataModel(null);
		object.setOutputDataModel(null);
		object.setMappings(null);
		object.setSkipFilter(null);
		object.setFunctions(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateObjectInternal(final Project object, final Project updateObject)
			throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject);

		final DataModel inputDataModel = object.getInputDataModel();
		final DataModel outputDataModel = object.getOutputDataModel();
		final Set<Mapping> mappings = object.getMappings();
		final Filter skipFilter = object.getSkipFilter();
		final Set<Function> functions = object.getFunctions();
		final Set<String> selectedRecords = object.getSelectedRecords();

		updateObject.setInputDataModel(inputDataModel);
		updateObject.setOutputDataModel(outputDataModel);
		updateObject.setMappings(mappings);
		updateObject.setSkipFilter(skipFilter);
		updateObject.setFunctions(functions);
		updateObject.setSelectedRecords(selectedRecords);
	}

}
