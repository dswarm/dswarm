package de.avgl.dmp.controller.resources.job.test.utils;

import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import com.google.common.collect.Maps;

import de.avgl.dmp.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.ExtendedBasicDMPResourceTestUtils;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.model.job.Project;
import de.avgl.dmp.persistence.model.job.proxy.ProxyProject;
import de.avgl.dmp.persistence.service.job.ProjectService;

public class ProjectsResourceTestUtils extends ExtendedBasicDMPResourceTestUtils<ProjectService, ProxyProject, Project> {

	private final FunctionsResourceTestUtils	functionsResourceTestUtils;

	private final MappingsResourceTestUtils		mappingsResourceTestUtils;

	private final DataModelsResourceTestUtils	dataModelsResourceTestUtils;

	public ProjectsResourceTestUtils() {

		super("projects", Project.class, ProjectService.class);

		functionsResourceTestUtils = new FunctionsResourceTestUtils();
		mappingsResourceTestUtils = new MappingsResourceTestUtils();
		dataModelsResourceTestUtils = new DataModelsResourceTestUtils();
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

	@Override
	public void reset() {

		dataModelsResourceTestUtils.reset();
		functionsResourceTestUtils.reset();
		mappingsResourceTestUtils.reset();
	}
}
