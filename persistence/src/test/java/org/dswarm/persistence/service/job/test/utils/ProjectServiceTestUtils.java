package org.dswarm.persistence.service.job.test.utils;

import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import com.google.common.collect.Maps;

import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.proxy.ProxyProject;
import org.dswarm.persistence.service.job.ProjectService;
import org.dswarm.persistence.service.resource.test.utils.DataModelServiceTestUtils;
import org.dswarm.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;

public class ProjectServiceTestUtils extends ExtendedBasicDMPJPAServiceTestUtils<ProjectService, ProxyProject, Project> {

	private final FunctionServiceTestUtils	functionsResourceTestUtils;

	private final MappingServiceTestUtils	mappingsResourceTestUtils;

	private final DataModelServiceTestUtils	dataModelsResourceTestUtils;

	public ProjectServiceTestUtils() {

		super(Project.class, ProjectService.class);

		functionsResourceTestUtils = new FunctionServiceTestUtils();
		mappingsResourceTestUtils = new MappingServiceTestUtils();
		dataModelsResourceTestUtils = new DataModelServiceTestUtils();
	}

	@Override
	public void compareObjects(final Project expectedObject, final Project actualObject) {

		super.compareObjects(expectedObject, actualObject);

		compareProjects(expectedObject, actualObject);
	}

	private void compareProjects(final Project expectedProject, final Project actualProject) {

		if (expectedProject.getInputDataModel() != null) {

			dataModelsResourceTestUtils.compareObjects(expectedProject.getInputDataModel(), actualProject.getInputDataModel());
		}

		if (expectedProject.getOutputDataModel() != null) {

			dataModelsResourceTestUtils.compareObjects(expectedProject.getOutputDataModel(), actualProject.getOutputDataModel());
		}

		if (expectedProject.getMappings() != null && !expectedProject.getMappings().isEmpty()) {

			final Set<Mapping> actualMappings = actualProject.getMappings();

			Assert.assertNotNull("mappings of project '" + actualProject.getId() + "' shouldn't be null", actualMappings);
			Assert.assertFalse("mappings of project '" + actualProject.getId() + "' shouldn't be empty", actualMappings.isEmpty());

			final Map<Long, Mapping> actualMappingsMap = Maps.newHashMap();

			for (final Mapping actualMapping : actualMappings) {

				actualMappingsMap.put(actualMapping.getId(), actualMapping);
			}

			mappingsResourceTestUtils.compareObjects(expectedProject.getMappings(), actualMappingsMap);
		}

		if (expectedProject.getFunctions() != null && !expectedProject.getFunctions().isEmpty()) {

			final Set<Function> actualFunctions = actualProject.getFunctions();

			Assert.assertNotNull("functions of project '" + actualProject.getId() + "' shouldn't be null", actualFunctions);
			Assert.assertFalse("functions of project '" + actualProject.getId() + "' shouldn't be empty", actualFunctions.isEmpty());

			final Map<Long, Function> actualFunctionsMap = Maps.newHashMap();

			for (final Function actualFunction : actualFunctions) {

				actualFunctionsMap.put(actualFunction.getId(), actualFunction);
			}

			functionsResourceTestUtils.compareObjects(expectedProject.getFunctions(), actualFunctionsMap);
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
		object.setMappings(objectWithUpdates.getMappings());

		return object;
	}

	@Override
	public void reset() {

		dataModelsResourceTestUtils.reset();
		functionsResourceTestUtils.reset();
		mappingsResourceTestUtils.reset();
	}
}
