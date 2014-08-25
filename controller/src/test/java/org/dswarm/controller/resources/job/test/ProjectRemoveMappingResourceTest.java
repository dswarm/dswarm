package org.dswarm.controller.resources.job.test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.resources.job.test.utils.FiltersResourceTestUtils;
import org.dswarm.controller.resources.job.test.utils.FunctionsResourceTestUtils;
import org.dswarm.controller.resources.job.test.utils.MappingsResourceTestUtils;
import org.dswarm.controller.resources.job.test.utils.ProjectsResourceTestUtils;
import org.dswarm.controller.resources.job.test.utils.TransformationsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ConfigurationsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.AttributePathsResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.AttributesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.ClaszesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.MappingAttributePathInstancesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.SchemasResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.job.proxy.ProxyProject;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.job.ProjectService;
import org.dswarm.persistence.service.job.test.utils.ProjectServiceTestUtils;

public class ProjectRemoveMappingResourceTest extends
		BasicResourceTest<ProjectsResourceTestUtils, ProjectServiceTestUtils, ProjectService, ProxyProject, Project, Long> {

	private static final Logger										LOG								= LoggerFactory
																											.getLogger(ProjectRemoveMappingResourceTest.class);

	private final FunctionsResourceTestUtils						functionsResourceTestUtils;

	private final TransformationsResourceTestUtils					transformationsResourceTestUtils;

	private final AttributesResourceTestUtils						attributesResourceTestUtils;

	private final AttributePathsResourceTestUtils					attributePathsResourceTestUtils;

	private final ClaszesResourceTestUtils							claszesResourceTestUtils;

	private final ResourcesResourceTestUtils						resourcesResourceTestUtils;

	private final ConfigurationsResourceTestUtils					configurationsResourceTestUtils;

	private final SchemasResourceTestUtils							schemasResourceTestUtils;

	private final DataModelsResourceTestUtils						dataModelsResourceTestUtils;

	private final MappingsResourceTestUtils							mappingsResourceTestUtils;

	private final ProjectsResourceTestUtils							projectsResourceTestUtils;

	private final MappingAttributePathInstancesResourceTestUtils	mappingAttributePathInstancesResourceTestUtils;

	private final FiltersResourceTestUtils							filterResourceTestUtils;

	final Map<Long, Attribute>										attributes						= Maps.newHashMap();

	final Map<Long, AttributePath>									attributePaths					= Maps.newLinkedHashMap();

	final Map<Long, MappingAttributePathInstance>					mappingAttributePathInstances	= Maps.newLinkedHashMap();

	private Project													initiallyPersistedProject		= null;

	public ProjectRemoveMappingResourceTest() {

		super(Project.class, ProjectService.class, "projects", "project_to_remove_mapping_from_with_dummy_IDs.json", new ProjectsResourceTestUtils());

		functionsResourceTestUtils = new FunctionsResourceTestUtils();
		attributesResourceTestUtils = new AttributesResourceTestUtils();
		attributePathsResourceTestUtils = new AttributePathsResourceTestUtils();
		transformationsResourceTestUtils = new TransformationsResourceTestUtils();
		claszesResourceTestUtils = new ClaszesResourceTestUtils();
		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
		schemasResourceTestUtils = new SchemasResourceTestUtils();
		dataModelsResourceTestUtils = new DataModelsResourceTestUtils();
		mappingsResourceTestUtils = new MappingsResourceTestUtils();
		projectsResourceTestUtils = new ProjectsResourceTestUtils();
		mappingAttributePathInstancesResourceTestUtils = new MappingAttributePathInstancesResourceTestUtils();
		filterResourceTestUtils = new FiltersResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {
		super.prepare();

		// persist project via API since dummy IDs need to be replaced with the ones used in database
		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(objectJSONString));
		Assert.assertEquals("201 Created was expected", 201, response.getStatus());

		final String responseString = response.readEntity(String.class);
		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		initiallyPersistedProject = objectMapper.readValue(responseString, Project.class);
		Assert.assertNotNull("the response project shouldn't be null", initiallyPersistedProject);
	}

	/**
	 * Simulate a user loading an already persisted project in front end, removing a mapping (that contains filters and functions)
	 * and saving the updated project (by putting the whole project JSON).<br />
	 * <br />
	 * It is intended that the mapping is removed from the project only, i.e. the relation between the project and the mapping is
	 * removed but the mapping itself and all of its parts (like functions and filters) are still present in the database (to be
	 * used in other projects).
	 *
	 * @throws Exception
	 */
	@Test
	public void testPUTProjectWithRemovedMapping() throws Exception {

		// Start simulate user removing a mapping from the given project
		final String initiallyPersistedProjectJSONString = objectMapper.writeValueAsString(initiallyPersistedProject);
		final Project modifiedProject = objectMapper.readValue(initiallyPersistedProjectJSONString, Project.class);

		final Set<Mapping> persistedMappings = modifiedProject.getMappings();
		final Set<Mapping> reducedMappings = Sets.newHashSet();
		final String mappingToBeRemovedFromProjectName = "first+last-to-contributor";
		Mapping mappingToBeRemovedFromProject = null;

		for (final Mapping mapping : persistedMappings) {

			// the mapping to be removed (by the user in front end)
			if (mapping.getName().equals(mappingToBeRemovedFromProjectName)) {
				mappingToBeRemovedFromProject = mapping;
				continue;
			}

			reducedMappings.add(mapping);
		}

		Assert.assertNotNull("could not find mapping to be removed \"" + mappingToBeRemovedFromProjectName + "\"", mappingToBeRemovedFromProject);

		// re-inject mappings
		modifiedProject.setMappings(reducedMappings);
		final String modifiedProjectJSONString = objectMapper.writeValueAsString(modifiedProject);

		// End simulate user removing a mapping from the given project
		// Start simulate user pushing button 'save project' in front end

		String idEncoded = null;
		try {

			idEncoded = URLEncoder.encode(initiallyPersistedProject.getId().toString(), "UTF-8");
		} catch (final UnsupportedEncodingException e) {

			ProjectRemoveMappingResourceTest.LOG.debug("couldn't encode id", e);

			Assert.assertTrue(false);
		}

		final Response response = target(idEncoded).request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.put(Entity.json(modifiedProjectJSONString));

		// End simulate user pushing button 'save project' in front end
		// Start check response

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);
		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final Project updatedPersistedProject = objectMapper.readValue(responseString, Project.class);
		Assert.assertNotNull("the response project shouldn't be null", updatedPersistedProject);

		// make sure the project does not contain (the reference to) the mapping anymore
		projectsResourceTestUtils.compareObjects(modifiedProject, updatedPersistedProject);

		// End check response
		// Start check db

		// the mapping itself must still be present in database
		Assert.assertNotNull("mapping to be removed \"" + mappingToBeRemovedFromProjectName + "\" has no ID", mappingToBeRemovedFromProject.getId());
		final Mapping persistedMappingToBeRemovedFromProject = mappingsResourceTestUtils.getObject(mappingToBeRemovedFromProject.getId());
		mappingsResourceTestUtils.compareObjects(mappingToBeRemovedFromProject, persistedMappingToBeRemovedFromProject);

	}

	@After
	public void tearDown2() throws Exception {
		final DataModel inputDataModel = initiallyPersistedProject.getInputDataModel();

		Resource inputDataResource = null;
		Configuration inputConfiguration = null;
		Schema inputSchema = null;

		final Map<Long, MappingAttributePathInstance> mappingAttributePathInstances = Maps.newHashMap();
		final Map<Long, AttributePath> attributePaths = Maps.newHashMap();
		final Map<Long, Attribute> attributes = Maps.newHashMap();
		final Map<Long, Clasz> claszes = Maps.newHashMap();
		final Map<Long, Filter> filter = Maps.newHashMap();

		if (inputDataModel != null) {

			inputDataResource = inputDataModel.getDataResource();
			inputConfiguration = inputDataModel.getConfiguration();
			inputSchema = inputDataModel.getSchema();

			if (inputSchema != null) {

				final Set<AttributePath> inputAttributePaths = inputSchema.getUniqueAttributePaths();

				if (inputAttributePaths != null) {

					for (final AttributePath inputAttributePath : inputAttributePaths) {

						attributePaths.put(inputAttributePath.getId(), inputAttributePath);

						final Set<Attribute> inputAttributePathAttributes = inputAttributePath.getAttributes();

						if (inputAttributePathAttributes != null) {

							for (final Attribute inputAttributePathAttribute : inputAttributePathAttributes) {

								attributes.put(inputAttributePathAttribute.getId(), inputAttributePathAttribute);
							}
						}
					}
				}

				final Clasz recordClass = inputSchema.getRecordClass();

				if (recordClass != null) {

					claszes.put(recordClass.getId(), recordClass);
				}
			}
		}

		final DataModel outputDataModel = initiallyPersistedProject.getOutputDataModel();

		Schema outputSchema = null;

		if (outputDataModel != null) {

			outputSchema = outputDataModel.getSchema();

			if (outputSchema != null) {

				final Set<AttributePath> outputAttributePaths = outputSchema.getUniqueAttributePaths();

				if (outputAttributePaths != null) {

					for (final AttributePath outputAttributePath : outputAttributePaths) {

						attributePaths.put(outputAttributePath.getId(), outputAttributePath);

						final Set<Attribute> outputAttributePathAttributes = outputAttributePath.getAttributes();

						if (outputAttributePathAttributes != null) {

							for (final Attribute outputAttributePathAttribute : outputAttributePathAttributes) {

								attributes.put(outputAttributePathAttribute.getId(), outputAttributePathAttribute);
							}
						}
					}
				}

				final Clasz recordClass = outputSchema.getRecordClass();

				if (recordClass != null) {

					claszes.put(recordClass.getId(), recordClass);
				}
			}
		}

		final Map<Long, Mapping> mappings = Maps.newHashMap();
		final Map<Long, Function> functions = Maps.newHashMap();
		Transformation transformation = null;

		final Set<Mapping> projectMappings = initiallyPersistedProject.getMappings();

		if (projectMappings != null) {

			for (final Mapping projectMapping : projectMappings) {

				mappings.put(projectMapping.getId(), projectMapping);

				final Component transformationComponent = projectMapping.getTransformation();

				if (transformationComponent != null) {

					final Function transformationComponentFunction = transformationComponent.getFunction();

					if (transformationComponentFunction != null) {

						if (Transformation.class.isInstance(transformationComponentFunction)) {

							transformation = (Transformation) transformationComponentFunction;

							final Set<Component> components = transformation.getComponents();

							for (final Component component : components) {

								final Function componentFunction = component.getFunction();

								if (componentFunction != null) {

									functions.put(componentFunction.getId(), componentFunction);
								}
							}
						} else {

							functions.put(transformationComponentFunction.getId(), transformationComponentFunction);
						}
					}
				}

				final Set<MappingAttributePathInstance> projectMappingInputAttributePaths = projectMapping.getInputAttributePaths();

				if (projectMappingInputAttributePaths != null) {

					for (final MappingAttributePathInstance inputMappingAttributePathInstance : projectMappingInputAttributePaths) {

						mappingAttributePathInstances.put(inputMappingAttributePathInstance.getId(), inputMappingAttributePathInstance);

						final AttributePath inputAttributePath = inputMappingAttributePathInstance.getAttributePath();

						if (inputAttributePath != null) {

							attributePaths.put(inputAttributePath.getId(), inputAttributePath);

							final Set<Attribute> inputAttributePathAttributes = inputAttributePath.getAttributes();

							if (inputAttributePathAttributes != null) {

								for (final Attribute inputAttributePathAttribute : inputAttributePathAttributes) {

									attributes.put(inputAttributePathAttribute.getId(), inputAttributePathAttribute);
								}
							}
						}

						final Filter singleFilter = inputMappingAttributePathInstance.getFilter();
						if (singleFilter != null) {
							filter.put(singleFilter.getId(), singleFilter);
						}

					}
				}

				final MappingAttributePathInstance projectMappingOutputMappingAttributePathInstance = projectMapping.getOutputAttributePath();

				if (projectMappingOutputMappingAttributePathInstance != null) {

					mappingAttributePathInstances.put(projectMappingOutputMappingAttributePathInstance.getId(),
							projectMappingOutputMappingAttributePathInstance);

					final AttributePath projectMappingOutputAttributePath = projectMappingOutputMappingAttributePathInstance.getAttributePath();

					if (projectMappingOutputAttributePath != null) {

						attributePaths.put(projectMappingOutputAttributePath.getId(), projectMappingOutputAttributePath);

						final Set<Attribute> inputAttributePathAttributes = projectMappingOutputAttributePath.getAttributes();

						if (inputAttributePathAttributes != null) {

							for (final Attribute inputAttributePathAttribute : inputAttributePathAttributes) {

								attributes.put(inputAttributePathAttribute.getId(), inputAttributePathAttribute);
							}
						}
					}
				}
			}
		}

		final Set<Function> projectFunctions = initiallyPersistedProject.getFunctions();

		if (projectFunctions != null) {

			for (final Function projectFunction : projectFunctions) {

				functions.put(projectFunction.getId(), projectFunction);
			}
		}

		cleanUpDB(initiallyPersistedProject);

		if (inputDataModel != null) {

			dataModelsResourceTestUtils.deleteObject(inputDataModel);
		}

		if (outputDataModel != null) {

			dataModelsResourceTestUtils.deleteObject(outputDataModel);
		}

		if (inputDataResource != null) {

			resourcesResourceTestUtils.deleteObject(inputDataResource);
		}

		if (inputConfiguration != null) {

			configurationsResourceTestUtils.deleteObject(inputConfiguration);
		}

		if (inputSchema != null) {

			schemasResourceTestUtils.deleteObject(inputSchema);
		}

		if (outputSchema != null) {

			schemasResourceTestUtils.deleteObject(outputSchema);
		}

		for (final Mapping mapping : mappings.values()) {

			mappingsResourceTestUtils.deleteObject(mapping);
		}

		for (final MappingAttributePathInstance mappingAttributePathInstance : mappingAttributePathInstances.values()) {

			mappingAttributePathInstancesResourceTestUtils.deleteObject(mappingAttributePathInstance);
		}

		for (final AttributePath attributePath : attributePaths.values()) {

			attributePathsResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attributePath);
		}

		for (final Clasz clasz : claszes.values()) {

			claszesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(clasz);
		}

		for (final Attribute attribute : attributes.values()) {

			attributesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attribute);
		}

		if (transformation != null) {

			transformationsResourceTestUtils.deleteObject(transformation);
		}

		for (final Function function : functions.values()) {

			functionsResourceTestUtils.deleteObject(function);
		}

		for (final Filter singleFilter : filter.values()) {
			filterResourceTestUtils.deleteObject(singleFilter);
		}
	}

	@Ignore
	@Test
	@Override
	public void testPOSTObjects() throws Exception {
	}

	@Ignore
	@Test
	@Override
	public void testGETObjects() throws Exception {
	}

	@Ignore
	@Test
	@Override
	public void testGETObject() throws Exception {
	}

	@Ignore
	@Test
	@Override
	public void testDELETEObject() throws Exception {
	}

	@Ignore
	@Test
	@Override
	public void testPUTObject() throws Exception {
	}

}
