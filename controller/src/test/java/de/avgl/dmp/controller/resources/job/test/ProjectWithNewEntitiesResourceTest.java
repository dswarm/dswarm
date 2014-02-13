package de.avgl.dmp.controller.resources.job.test;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

import de.avgl.dmp.controller.resources.job.test.utils.FunctionsResourceTestUtils;
import de.avgl.dmp.controller.resources.job.test.utils.MappingsResourceTestUtils;
import de.avgl.dmp.controller.resources.job.test.utils.ProjectsResourceTestUtils;
import de.avgl.dmp.controller.resources.job.test.utils.TransformationsResourceTestUtils;
import de.avgl.dmp.controller.resources.resource.test.utils.ConfigurationsResourceTestUtils;
import de.avgl.dmp.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import de.avgl.dmp.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.AttributesResourceTest;
import de.avgl.dmp.controller.resources.schema.test.utils.AttributePathsResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.AttributesResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.ClaszesResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.MappingAttributePathInstancesResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.SchemasResourceTestUtils;
import de.avgl.dmp.controller.resources.test.BasicResourceTest;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.model.job.Project;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.job.proxy.ProxyProject;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.MappingAttributePathInstance;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.job.ProjectService;
import de.avgl.dmp.persistence.service.job.test.utils.ProjectServiceTestUtils;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class ProjectWithNewEntitiesResourceTest extends
		BasicResourceTest<ProjectsResourceTestUtils, ProjectServiceTestUtils, ProjectService, ProxyProject, Project, Long> {

	private static final org.apache.log4j.Logger					LOG	= org.apache.log4j.Logger.getLogger(AttributesResourceTest.class);

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

	private final MappingAttributePathInstancesResourceTestUtils	mappingAttributePathInstancesResourceTestUtils;

	public ProjectWithNewEntitiesResourceTest() {

		super(Project.class, ProjectService.class, "projects", "project.json", new ProjectsResourceTestUtils());

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
		mappingAttributePathInstancesResourceTestUtils = new MappingAttributePathInstancesResourceTestUtils();
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
	public void testPUTObject() throws Exception {

	}

	@Ignore
	@Test
	@Override
	public void testDELETEObject() throws Exception {

	}

	@Test
	public void testPOSTObjectsWithNewEntities() throws Exception {

		LOG.debug("start POST " + pojoClassName + "s with new entities test");

		objectJSONString = DMPPersistenceUtil.getResourceAsString("project.w.new.entities.json");

		// START configuration preparation

		final Configuration configuration = configurationsResourceTestUtils.createObject("configuration2.json");

		// END configuration preparation

		// START resource preparation

		// prepare resource json for configuration ids manipulation
		String resourceJSONString = DMPPersistenceUtil.getResourceAsString("resource1.json");
		final ObjectNode resourceJSON = objectMapper.readValue(resourceJSONString, ObjectNode.class);

		final ArrayNode configurationsArray = objectMapper.createArrayNode();

		final String persistedConfigurationJSONString = objectMapper.writeValueAsString(configuration);
		final ObjectNode persistedConfigurationJSON = objectMapper.readValue(persistedConfigurationJSONString, ObjectNode.class);

		configurationsArray.add(persistedConfigurationJSON);

		resourceJSON.put("configurations", configurationsArray);

		// re-init expect resource
		resourceJSONString = objectMapper.writeValueAsString(resourceJSON);
		final Resource expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

		Assert.assertNotNull("expected resource shouldn't be null", expectedResource);

		final Resource resource = resourcesResourceTestUtils.createObject(resourceJSONString, expectedResource);

		// END resource preparation

		// prepare project json for input data model resource and configuration manipulation
		final ObjectNode projectJSON = objectMapper.readValue(objectJSONString, ObjectNode.class);
		final ObjectNode dataModelJSON = (ObjectNode) projectJSON.get("input_data_model");

		final String finalResourceJSONString = objectMapper.writeValueAsString(resource);
		final ObjectNode finalResourceJSON = objectMapper.readValue(finalResourceJSONString, ObjectNode.class);

		dataModelJSON.put("data_resource", finalResourceJSON);

		final String finalConfigurationJSONString = objectMapper.writeValueAsString(resource.getConfigurations().iterator().next());
		final ObjectNode finalConfigurationJSON = objectMapper.readValue(finalConfigurationJSONString, ObjectNode.class);

		dataModelJSON.put("configuration", finalConfigurationJSON);

		objectJSONString = objectMapper.writeValueAsString(projectJSON);

		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(objectJSONString));

		Assert.assertEquals("201 Created was expected", 201, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final Project actualObject = objectMapper.readValue(responseString, pojoClass);

		Assert.assertNotNull("the response project shouldn't be null", actualObject);

		// TODO: do comparison/check somehow

		final DataModel inputDataModel = actualObject.getInputDataModel();

		Resource inputDataResource = null;
		Configuration inputConfiguration = null;
		Schema inputSchema = null;

		final Map<Long, MappingAttributePathInstance> mappingAttributePathInstances = Maps.newHashMap();
		final Map<Long, AttributePath> attributePaths = Maps.newHashMap();
		final Map<Long, Attribute> attributes = Maps.newHashMap();
		final Map<Long, Clasz> claszes = Maps.newHashMap();

		if (inputDataModel != null) {

			inputDataResource = inputDataModel.getDataResource();
			inputConfiguration = inputDataModel.getConfiguration();
			inputSchema = inputDataModel.getSchema();

			if (inputSchema != null) {

				final Set<AttributePath> inputAttributePaths = inputSchema.getAttributePaths();

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

		final DataModel outputDataModel = actualObject.getOutputDataModel();

		Schema outputSchema = null;

		if (outputDataModel != null) {

			outputSchema = outputDataModel.getSchema();

			if (outputSchema != null) {

				final Set<AttributePath> outputAttributePaths = outputSchema.getAttributePaths();

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

		final Set<Mapping> projectMappings = actualObject.getMappings();

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

		final Set<Function> projectFunctions = actualObject.getFunctions();

		if (projectFunctions != null) {

			for (final Function projectFunction : projectFunctions) {

				functions.put(projectFunction.getId(), projectFunction);
			}
		}

		cleanUpDB(actualObject);

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

			attributePathsResourceTestUtils.deleteObject(attributePath);
		}

		for (final Clasz clasz : claszes.values()) {

			claszesResourceTestUtils.deleteObject(clasz);
		}

		for (final Attribute attribute : attributes.values()) {

			attributesResourceTestUtils.deleteObject(attribute);
		}

		if (transformation != null) {

			transformationsResourceTestUtils.deleteObject(transformation);
		}

		for (final Function function : functions.values()) {

			functionsResourceTestUtils.deleteObject(function);
		}

		LOG.debug("end POST " + pojoClassName + "s with new entities test");
	}
}
