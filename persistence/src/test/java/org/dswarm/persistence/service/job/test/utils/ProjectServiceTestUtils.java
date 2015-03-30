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
package org.dswarm.persistence.service.job.test.utils;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.json.JSONException;
import org.junit.Assert;

import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.proxy.ProxyProject;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.job.ProjectService;
import org.dswarm.persistence.service.resource.test.utils.DataModelServiceTestUtils;
import org.dswarm.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;

public class ProjectServiceTestUtils extends ExtendedBasicDMPJPAServiceTestUtils<ProjectService, ProxyProject, Project> {

	private final FunctionServiceTestUtils functionServiceTestUtils;

	private final MappingServiceTestUtils mappingServiceTestUtils;

	private final DataModelServiceTestUtils dataModelServiceTestUtils;

	private final FilterServiceTestUtils filterServiceTestUtils;

	public ProjectServiceTestUtils() {

		super(Project.class, ProjectService.class);

		functionServiceTestUtils = new FunctionServiceTestUtils();
		mappingServiceTestUtils = new MappingServiceTestUtils();
		dataModelServiceTestUtils = new DataModelServiceTestUtils();
		filterServiceTestUtils = new FilterServiceTestUtils();
	}

	@Override public Project createObject(JsonNode objectDescription) throws Exception {
		return null;
	}

	@Override public Project createObject(String identifier) throws Exception {
		return null;
	}

	@Override public Project createAndPersistDefaultObject() throws Exception {

		final Mapping simpleMapping = mappingServiceTestUtils.createAndPersistDefaultObject();
		final Mapping complexMapping = mappingServiceTestUtils.createAndPersistDefaultCompleteObject();

		final Set<Mapping> mappings = Sets.newLinkedHashSet();
		mappings.add(simpleMapping);
		mappings.add(complexMapping);

		final DataModel inputDataModel = dataModelServiceTestUtils.createAndPersistDefaultObject();
		final DataModel outputDataModel = dataModelServiceTestUtils.createAndPersistDefaultObject();

		final Function function1 = simpleMapping.getTransformation().getFunction();

		final Set<Function> functions = Sets.newLinkedHashSet();
		functions.add(function1);

		final String projectName = "my project";
		final String projectDescription = "my project description";

		return createAndPersistProject(projectName, projectDescription, mappings, inputDataModel, outputDataModel, functions);
	}

	@Override public Project createDefaultObject() throws Exception {

		final Mapping simpleMapping = mappingServiceTestUtils.createAndPersistDefaultObject();
		final Mapping complexMapping = mappingServiceTestUtils.createAndPersistDefaultCompleteObject();

		final Set<Mapping> mappings = Sets.newLinkedHashSet();
		mappings.add(simpleMapping);
		mappings.add(complexMapping);

		final DataModel inputDataModel = dataModelServiceTestUtils.createAndPersistDefaultObject();
		final DataModel outputDataModel = dataModelServiceTestUtils.createAndPersistDefaultObject();

		final Function function1 = simpleMapping.getTransformation().getFunction();

		final Set<Function> functions = Sets.newLinkedHashSet();
		functions.add(function1);

		final String projectName = "my project";
		final String projectDescription = "my project description";

		return createProject(projectName, projectDescription, mappings, inputDataModel, outputDataModel, functions);
	}

	@Override public Project createAndPersistDefaultCompleteObject() throws Exception {

		final Project project = createDefaultObject();
		final Filter skipFilter = filterServiceTestUtils.createDefaultObject();

		project.setSkipFilter(skipFilter);

		return createAndCompareObject(project, project);
	}

	public Project createAndPersistProject(final String name, final String description, final Set<Mapping> mappings, final DataModel inputDataModel,
			final DataModel outputDataModel, final Set<Function> functions)
			throws Exception {

		final Project project = createProject(name, description, mappings, inputDataModel, outputDataModel, functions);

		return createAndCompareObject(project, project);
	}

	public Project createProject(final String name, final String description, final Set<Mapping> mappings, final DataModel inputDataModel,
			final DataModel outputDataModel,
			final Set<Function> functions) {

		final String uuid = UUIDService.getUUID(Project.class.getSimpleName());

		final Project project = new Project(uuid);
		project.setName(name);
		project.setDescription(description);
		project.setMappings(mappings);
		project.setInputDataModel(inputDataModel);
		project.setOutputDataModel(outputDataModel);
		project.setFunctions(functions);

		return project;
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert that both {@link Project}s have either no input {@link DataModel}s or their input {@link DataModel}s are equal, see
	 * {@link DataModelServiceTestUtils#compareObjects(DataModel, DataModel)}. <br />
	 * Assert that both {@link Project}s have either no output {@link DataModel}s or their output {@link DataModel}s are equal,
	 * see {@link DataModelServiceTestUtils#compareObjects(DataModel, DataModel)}. <br />
	 * Assert that both {@link Project}s have either no {@link Mapping}s or their {@link Mapping}s are equal, see
	 * {@link MappingServiceTestUtils#compareObjects(Mapping, Mapping)}. <br />
	 * Assert that both {@link Project}s have either no {@link Function}s or their {@link Function}s are equal, see
	 * {@link FunctionServiceTestUtils#compareObjects(Function, Function)}. <br />
	 */
	@Override
	public void compareObjects(final Project expectedProject, final Project actualProject) throws JsonProcessingException, JSONException {

		super.compareObjects(expectedProject, actualProject);

		// compare input data models
		if (expectedProject.getInputDataModel() == null) {

			Assert.assertNull("the actual project '" + actualProject.getUuid() + "' should not have an input data model",
					actualProject.getInputDataModel());

		} else {

			dataModelServiceTestUtils.compareObjects(expectedProject.getInputDataModel(), actualProject.getInputDataModel());
		}

		// compare output data models
		if (expectedProject.getOutputDataModel() == null) {

			Assert.assertNull("the actual project '" + actualProject.getUuid() + "' should not have an output data model",
					actualProject.getOutputDataModel());

		} else {

			dataModelServiceTestUtils.compareObjects(expectedProject.getOutputDataModel(), actualProject.getOutputDataModel());
		}

		// compare output skip filters
		if (expectedProject.getSkipFilter() == null) {

			Assert.assertNull("the actual project '" + actualProject.getUuid() + "' should not have a skip filter",
					actualProject.getSkipFilter());

		} else {

			filterServiceTestUtils.compareObjects(expectedProject.getSkipFilter(), actualProject.getSkipFilter());
		}

		// compare mappings
		if (expectedProject.getMappings() == null || expectedProject.getMappings().isEmpty()) {

			final boolean actualProjectHasNoMappings = (actualProject.getMappings() == null || actualProject.getMappings().isEmpty());
			Assert.assertTrue("the actual project '" + actualProject.getUuid() + "' shouldn't have any mappings", actualProjectHasNoMappings);

		} else { // !null && !empty

			final Set<Mapping> actualMappings = actualProject.getMappings();

			Assert.assertNotNull("mappings of project '" + actualProject.getUuid() + "' shouldn't be null", actualMappings);
			Assert.assertFalse("mappings of project '" + actualProject.getUuid() + "' shouldn't be empty", actualMappings.isEmpty());

			final Map<String, Mapping> actualMappingsMap = Maps.newHashMap();

			for (final Mapping actualMapping : actualMappings) {

				actualMappingsMap.put(actualMapping.getUuid(), actualMapping);
			}

			mappingServiceTestUtils.compareObjects(expectedProject.getMappings(), actualMappingsMap);
		}

		// compare functions
		if (expectedProject.getFunctions() == null || expectedProject.getFunctions().isEmpty()) {

			final boolean actualProjectHasNoFunctions = (actualProject.getFunctions() == null || actualProject.getFunctions().isEmpty());
			Assert.assertTrue("the actual project '" + actualProject.getUuid() + "' shouldn't have any functions", actualProjectHasNoFunctions);

		} else { // !null && !empty

			final Set<Function> actualFunctions = actualProject.getFunctions();

			Assert.assertNotNull("functions of project '" + actualProject.getUuid() + "' shouldn't be null", actualFunctions);
			Assert.assertFalse("functions of project '" + actualProject.getUuid() + "' shouldn't be empty", actualFunctions.isEmpty());

			final Map<String, Function> actualFunctionsMap = Maps.newHashMap();

			for (final Function actualFunction : actualFunctions) {

				actualFunctionsMap.put(actualFunction.getUuid(), actualFunction);
			}

			functionServiceTestUtils.compareObjects(expectedProject.getFunctions(), actualFunctionsMap);
		}
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, description, (sample) input data model, output data model, mappings and functions of the project.
	 */
	@Override
	protected Project prepareObjectForUpdate(final Project objectWithUpdates, final Project object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		object.setFunctions(objectWithUpdates.getFunctions());
		object.setInputDataModel(objectWithUpdates.getInputDataModel());
		object.setOutputDataModel(objectWithUpdates.getOutputDataModel());
		object.setSkipFilter(objectWithUpdates.getSkipFilter());
		object.setMappings(objectWithUpdates.getMappings());

		return object;
	}

	@Override
	public void reset() {

		dataModelServiceTestUtils.reset();
		functionServiceTestUtils.reset();
		mappingServiceTestUtils.reset();
		filterServiceTestUtils.reset();
	}
}
