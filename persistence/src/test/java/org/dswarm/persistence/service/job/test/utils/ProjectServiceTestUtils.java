package org.dswarm.persistence.service.job.test.utils;

import java.util.Map;
import java.util.Set;

import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.proxy.ProxyProject;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.service.job.ProjectService;
import org.dswarm.persistence.service.resource.test.utils.DataModelServiceTestUtils;
import org.dswarm.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;
import org.junit.Assert;

import com.google.common.collect.Maps;

public class ProjectServiceTestUtils extends ExtendedBasicDMPJPAServiceTestUtils<ProjectService, ProxyProject, Project> {

	private final FunctionServiceTestUtils	functionServiceTestUtils;

	private final MappingServiceTestUtils	mappingServiceTestUtils;

	private final DataModelServiceTestUtils	dataModelServiceTestUtils;

	public ProjectServiceTestUtils() {

		super(Project.class, ProjectService.class);

		functionServiceTestUtils = new FunctionServiceTestUtils();
		mappingServiceTestUtils = new MappingServiceTestUtils();
		dataModelServiceTestUtils = new DataModelServiceTestUtils();
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
	public void compareObjects(final Project expectedProject, final Project actualProject) {

		super.compareObjects(expectedProject, actualProject);

		// compare input data models
		if (expectedProject.getInputDataModel() == null) {

			Assert.assertNull("the actual project '" + actualProject.getId() + "' should not have an input data model",
					actualProject.getInputDataModel());

		} else {
			dataModelServiceTestUtils.compareObjects(expectedProject.getInputDataModel(), actualProject.getInputDataModel());
		}

		// compare output data models
		if (expectedProject.getOutputDataModel() == null) {

			Assert.assertNull("the actual project '" + actualProject.getId() + "' should not have an output data model",
					actualProject.getOutputDataModel());

		} else {
			dataModelServiceTestUtils.compareObjects(expectedProject.getOutputDataModel(), actualProject.getOutputDataModel());
		}

		// compare mappings
		if (expectedProject.getMappings() == null || expectedProject.getMappings().isEmpty()) {

			boolean actualProjectHasNoMappings = (actualProject.getMappings() == null || actualProject.getMappings().isEmpty());
			Assert.assertTrue("the actual project '" + actualProject.getId() + "' shouldn't have any mappings", actualProjectHasNoMappings);

		} else { // !null && !empty

			final Set<Mapping> actualMappings = actualProject.getMappings();

			Assert.assertNotNull("mappings of project '" + actualProject.getId() + "' shouldn't be null", actualMappings);
			Assert.assertFalse("mappings of project '" + actualProject.getId() + "' shouldn't be empty", actualMappings.isEmpty());

			final Map<Long, Mapping> actualMappingsMap = Maps.newHashMap();

			for (final Mapping actualMapping : actualMappings) {

				actualMappingsMap.put(actualMapping.getId(), actualMapping);
			}

			mappingServiceTestUtils.compareObjects(expectedProject.getMappings(), actualMappingsMap);
		}

		// compare functions
		if (expectedProject.getFunctions() == null || expectedProject.getFunctions().isEmpty()) {

			boolean actualProjectHasNoFunctions = (actualProject.getFunctions() == null || actualProject.getFunctions().isEmpty());
			Assert.assertTrue("the actual project '" + actualProject.getId() + "' shouldn't have any functions", actualProjectHasNoFunctions);

		} else { // !null && !empty

			final Set<Function> actualFunctions = actualProject.getFunctions();

			Assert.assertNotNull("functions of project '" + actualProject.getId() + "' shouldn't be null", actualFunctions);
			Assert.assertFalse("functions of project '" + actualProject.getId() + "' shouldn't be empty", actualFunctions.isEmpty());

			final Map<Long, Function> actualFunctionsMap = Maps.newHashMap();

			for (final Function actualFunction : actualFunctions) {

				actualFunctionsMap.put(actualFunction.getId(), actualFunction);
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
		object.setMappings(objectWithUpdates.getMappings());

		return object;
	}

	@Override
	public void reset() {

		dataModelServiceTestUtils.reset();
		functionServiceTestUtils.reset();
		mappingServiceTestUtils.reset();
	}
}
