package org.dswarm.controller.resources.job.test;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.dswarm.controller.resources.job.test.utils.ComponentsResourceTestUtils;
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
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ProjectsResourceTest extends
		BasicResourceTest<ProjectsResourceTestUtils, ProjectServiceTestUtils, ProjectService, ProxyProject, Project, Long> {

	private FunctionsResourceTestUtils						functionsResourceTestUtils;

	private TransformationsResourceTestUtils					transformationsResourceTestUtils;

	private ComponentsResourceTestUtils						componentsResourceTestUtils;

	private AttributesResourceTestUtils						attributesResourceTestUtils;

	private AttributePathsResourceTestUtils					attributePathsResourceTestUtils;

	private ClaszesResourceTestUtils							claszesResourceTestUtils;

	private ResourcesResourceTestUtils						resourcesResourceTestUtils;

	private ConfigurationsResourceTestUtils					configurationsResourceTestUtils;

	private SchemasResourceTestUtils							schemasResourceTestUtils;

	private DataModelsResourceTestUtils						dataModelsResourceTestUtils;

	private MappingsResourceTestUtils							mappingsResourceTestUtils;

	private ProjectsResourceTestUtils							projectsResourceTestUtils;

	private MappingAttributePathInstancesResourceTestUtils	mappingAttributePathInstancesResourceTestUtils;

	private Function												function;

	private Function												updateFunction;

	private Component												component;

	private Component												updateComponent;

	private Component												updateTransformationComponent;

	private Transformation											transformation;

	private Transformation											updateTransformation;

	private Component												transformationComponent;

	final Map<Long, Attribute>										attributes						= Maps.newHashMap();

	final Map<Long, AttributePath>									attributePaths					= Maps.newLinkedHashMap();

	final Map<Long, MappingAttributePathInstance>					mappingAttributePathInstances	= Maps.newLinkedHashMap();

	private Clasz													recordClass;

	private Clasz													updateRecordClass;

	private Schema													schema;

	private Schema													updateSchema;

	private Configuration											configuration;

	private Configuration											updateConfiguration;

	private Resource												resource;

	private Resource												updateResource;

	private Mapping													updateMapping;

	private final Map<Long, DataModel>								dataModels						= Maps.newHashMap();

	private final Map<Long, Mapping>								mappings						= Maps.newHashMap();

	public ProjectsResourceTest() {

		super(Project.class, ProjectService.class, "projects", "project.json", new ProjectsResourceTestUtils());
	}

	@Override
	protected void initObjects() {

		super.initObjects();

		pojoClassResourceTestUtils = new ProjectsResourceTestUtils();
		functionsResourceTestUtils = new FunctionsResourceTestUtils();
		componentsResourceTestUtils = new ComponentsResourceTestUtils();
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
	}

	private void resetObjectVars() {

		function = null;
		updateFunction = null;
		component = null;
		updateComponent = null;
		updateTransformationComponent = null;
		transformation = null;
		updateTransformation = null;
		transformationComponent = null;
		attributes.clear();
		attributePaths.clear();
		mappingAttributePathInstances.clear();
		recordClass = null;
		updateRecordClass = null;
		schema = null;
		updateSchema = null;
		configuration = null;
		updateConfiguration = null;
		resource = null;
		updateResource = null;
		updateMapping = null;
		dataModels.clear();
		mappings.clear();
	}

	@Override
	public void prepare() throws Exception {

		restartServer();
		initObjects();
		resetObjectVars();

		super.prepare();

		final DataModel inputDataModel = createInputDataModel();
		final DataModel outputDataModel = createOutputDataModel();
		final Mapping simpleMapping = createSimpleMapping();

		// START project preparation

		// prepare project json for input data model, output data model, mappings and functions manipulation
		final ObjectNode objectJSON = objectMapper.readValue(objectJSONString, ObjectNode.class);

		final String finalInputDataModelJSONString = objectMapper.writeValueAsString(inputDataModel);
		final ObjectNode finalInputDataModelJSON = objectMapper.readValue(finalInputDataModelJSONString, ObjectNode.class);

		objectJSON.put("input_data_model", finalInputDataModelJSON);

		final String finalOutputDataModelJSONString = objectMapper.writeValueAsString(outputDataModel);
		final ObjectNode finalOutputDataModelJSON = objectMapper.readValue(finalOutputDataModelJSONString, ObjectNode.class);

		objectJSON.put("output_data_model", finalOutputDataModelJSON);

		final String finalSimpleMappingJSONString = objectMapper.writeValueAsString(simpleMapping);
		final ObjectNode finalSimpleMappingJSON = objectMapper.readValue(finalSimpleMappingJSONString, ObjectNode.class);

		final ArrayNode mappingsArray = objectMapper.createArrayNode();

		mappingsArray.add(finalSimpleMappingJSON);

		objectJSON.put("mappings", mappingsArray);

		final String finalFunctionJSONString = objectMapper.writeValueAsString(function);
		final ObjectNode finalFunctionJSON = objectMapper.readValue(finalFunctionJSONString, ObjectNode.class);

		final ArrayNode functionsArray = objectMapper.createArrayNode();

		functionsArray.add(finalFunctionJSON);

		objectJSON.put("functions", functionsArray);

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(objectJSON);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);

		// END project preparation
	}

	@Test
	@Override
	public void testPUTObject() throws Exception {

		super.testPUTObject();

		mappingsResourceTestUtils.deleteObject(updateMapping);

		transformationsResourceTestUtils.deleteObject(updateTransformation);

		functionsResourceTestUtils.deleteObject(updateFunction);
	}

	@After
	public void tearDown2() throws Exception {

		// START data models tear down

		for (final DataModel dataModel : dataModels.values()) {

			if (dataModel.getSchema() != null) {

				final Set<AttributePath> attributePaths = dataModel.getSchema().getAttributePaths();

				if (attributePaths != null) {

					for (final AttributePath attributePath : attributePaths) {

						this.attributePaths.put(attributePath.getId(), attributePath);
					}
				}
			}

			dataModelsResourceTestUtils.deleteObject(dataModel);
		}

		// resource clean-up

		resourcesResourceTestUtils.deleteObject(resource);

		resourcesResourceTestUtils.deleteObject(updateResource);

		// configuration clean-up

		configurationsResourceTestUtils.deleteObject(configuration);

		configurationsResourceTestUtils.deleteObject(updateConfiguration);

		// START schema clean-up

		schemasResourceTestUtils.deleteObject(schema);

		schemasResourceTestUtils.deleteObject(updateSchema);

		claszesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(recordClass);

		claszesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(updateRecordClass);

		// END schema clean-up

		// END data models tear down

		// START mappings tear down

		for (final Mapping mapping : mappings.values()) {

			mappingsResourceTestUtils.deleteObject(mapping);
		}

		for (final MappingAttributePathInstance mappingAttributePathInstance : mappingAttributePathInstances.values()) {

			mappingAttributePathInstancesResourceTestUtils.deleteObject(mappingAttributePathInstance);
		}

		for (final AttributePath attributePath : attributePaths.values()) {

			attributePathsResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attributePath);
		}

		for (final Attribute attribute : attributes.values()) {

			attributesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attribute);
		}

		transformationsResourceTestUtils.deleteObject(transformation);

		functionsResourceTestUtils.deleteObject(function);

		// END mappings tear down
	}

	@Override
	protected Project updateObject(final Project persistedProject) throws Exception {

		persistedProject.setName(persistedProject.getName() + " update");

		persistedProject.setDescription(persistedProject.getDescription() + " update");

		// update data model
		updateConfiguration = configurationsResourceTestUtils.createObject("configuration2.json");
		final DataModel inputDataModel = persistedProject.getInputDataModel();
		inputDataModel.setConfiguration(updateConfiguration);

		updateResource = resourcesResourceTestUtils.createObject("resource2.json");
		inputDataModel.setDataResource(updateResource);

		updateRecordClass = claszesResourceTestUtils.createObject("clasz2.json");

		final Schema tmpSchema = inputDataModel.getSchema();
		tmpSchema.setName(schema.getName() + " update");
		tmpSchema.setRecordClass(updateRecordClass);

		final String tmpSchemaJSONString = objectMapper.writeValueAsString(tmpSchema);

		updateSchema = schemasResourceTestUtils.createObject(tmpSchemaJSONString, tmpSchema);

		final Set<AttributePath> updateAttributePaths = updateSchema.getAttributePaths();

		if (updateAttributePaths != null) {

			for (final AttributePath updateAttributePath : updateAttributePaths) {

				attributePaths.put(updateAttributePath.getId(), updateAttributePath);

				final Set<Attribute> updateAttributes = updateAttributePath.getAttributes();

				if (updateAttributes != null) {

					for (final Attribute updateAttribute : updateAttributes) {

						attributes.put(updateAttribute.getId(), updateAttribute);
					}
				}
			}
		}

		inputDataModel.setSchema(updateSchema);

		persistedProject.setInputDataModel(inputDataModel);

		// update mapping
		// - function
		updateFunction = functionsResourceTestUtils.createObject("function1.json");
		final String finalUpdateFunctionJSONString = objectMapper.writeValueAsString(updateFunction);
		final ObjectNode finalUpdateFunctionJSON = objectMapper.readValue(finalUpdateFunctionJSONString, ObjectNode.class);

		// - component
		String updateComponentJSONString = DMPPersistenceUtil.getResourceAsString("component1.json");
		final ObjectNode updateComponentJSON = objectMapper.readValue(updateComponentJSONString, ObjectNode.class);
		updateComponentJSON.put("function", finalUpdateFunctionJSON);
		updateComponentJSONString = objectMapper.writeValueAsString(updateComponentJSON);
		final Component expectedComponent = objectMapper.readValue(updateComponentJSONString, Component.class);
		updateComponent = componentsResourceTestUtils.createObject(updateComponentJSONString, expectedComponent);

		// - transformation
		String updateTransformationJSONString = DMPPersistenceUtil.getResourceAsString("transformation1.json");
		final ObjectNode updateTransformationJSON = objectMapper.readValue(updateTransformationJSONString, ObjectNode.class);
		final String finalUpdateComponentJSONString = objectMapper.writeValueAsString(updateComponent);
		final ObjectNode finalUpdateComponentJSON = objectMapper.readValue(finalUpdateComponentJSONString, ObjectNode.class);
		final ArrayNode updateComponentsJSONArray = objectMapper.createArrayNode();
		updateComponentsJSONArray.add(finalUpdateComponentJSON);
		updateTransformationJSON.put("components", updateComponentsJSONArray);
		updateTransformationJSONString = objectMapper.writeValueAsString(updateTransformationJSON);
		final Transformation expectedTransformation = objectMapper.readValue(updateTransformationJSONString, Transformation.class);
		updateTransformation = transformationsResourceTestUtils.createObject(updateTransformationJSONString, expectedTransformation);

		// - transformation component
		String updateTransformationComponentJSONString = DMPPersistenceUtil.getResourceAsString("transformation_component1.json");
		final ObjectNode updateTransformationComponentJSON = objectMapper.readValue(updateTransformationComponentJSONString, ObjectNode.class);
		final String finalUpdateTransformationJSONString = objectMapper.writeValueAsString(updateTransformation);
		final ObjectNode finalUpdateTransformationJSON = objectMapper.readValue(finalUpdateTransformationJSONString, ObjectNode.class);
		updateTransformationComponentJSON.put("function", finalUpdateTransformationJSON);
		updateTransformationComponentJSONString = objectMapper.writeValueAsString(updateTransformationComponentJSON);
		final Component expectedTransformationComponent = objectMapper.readValue(updateTransformationComponentJSONString, Component.class);
		updateTransformationComponent = componentsResourceTestUtils.createObject(updateTransformationComponentJSONString,
				expectedTransformationComponent);

		// - mapping
		String updateMappingJSONString = DMPPersistenceUtil.getResourceAsString("mapping1.json");
		final ObjectNode updateMappingJSON = objectMapper.readValue(updateMappingJSONString, ObjectNode.class);
		final String finalUpdateTransformationComponentJSONString = objectMapper.writeValueAsString(updateTransformationComponent);
		final ObjectNode finalUpdateTransformationComponentJSON = objectMapper.readValue(finalUpdateTransformationComponentJSONString,
				ObjectNode.class);
		updateMappingJSON.put("transformation", finalUpdateTransformationComponentJSON);

		// - attribute paths
		updateMappingJSONString = objectMapper.writeValueAsString(updateMappingJSON);
		final Mapping expectedMapping = objectMapper.readValue(updateMappingJSONString, Mapping.class);

		expectedMapping.setInputAttributePaths(persistedProject.getMappings().iterator().next().getInputAttributePaths());
		expectedMapping.setOutputAttributePath(persistedProject.getMappings().iterator().next().getOutputAttributePath());

		updateMappingJSONString = objectMapper.writeValueAsString(expectedMapping);

		updateMapping = mappingsResourceTestUtils.createObject(updateMappingJSONString, expectedMapping);
		final Set<Mapping> updateMappings = new LinkedHashSet<Mapping>();
		updateMappings.add(updateMapping);

		persistedProject.setMappings(updateMappings);

		// now we updated all the above things on our persistedProject (also in database)
		final String updateProjectJSONString = objectMapper.writeValueAsString(persistedProject);
		final Project expectedProject = objectMapper.readValue(updateProjectJSONString, Project.class);
		Assert.assertNotNull("the project JSON string shouldn't be null", updateProjectJSONString);

		final Project updateProject = projectsResourceTestUtils.updateObject(updateProjectJSONString, expectedProject);

		Assert.assertEquals("projects should be equal", persistedProject, updateProject);

		final MappingAttributePathInstance inputMappingAttributePathInstance = updateMapping.getInputAttributePaths().iterator().next();

		if (inputMappingAttributePathInstance != null) {

			mappingAttributePathInstances.put(inputMappingAttributePathInstance.getId(), inputMappingAttributePathInstance);

			final AttributePath inputAttributePath = inputMappingAttributePathInstance.getAttributePath();

			if (inputAttributePath != null) {

				attributePaths.put(inputAttributePath.getId(), inputAttributePath);

				final Set<Attribute> inputAttributes = inputAttributePath.getAttributes();

				if (inputAttributes != null) {

					for (final Attribute inputAttribute : inputAttributes) {

						attributes.put(inputAttribute.getId(), inputAttribute);
					}
				}
			}
		}

		final MappingAttributePathInstance outputMappingAttributePathInstance = updateMapping.getOutputAttributePath();

		if (outputMappingAttributePathInstance != null) {

			mappingAttributePathInstances.put(outputMappingAttributePathInstance.getId(), outputMappingAttributePathInstance);

			final AttributePath outputAttributePath = outputMappingAttributePathInstance.getAttributePath();

			if (outputAttributePath != null) {

				attributePaths.put(outputAttributePath.getId(), outputAttributePath);

				final Set<Attribute> outputAttributes = outputAttributePath.getAttributes();

				if (outputAttributes != null) {

					for (final Attribute outputAttribute : outputAttributes) {

						attributes.put(outputAttribute.getId(), outputAttribute);
					}
				}
			}
		}

		return updateProject;
	}

	private DataModel createInputDataModel() throws Exception {

		// START configuration preparation

		configuration = configurationsResourceTestUtils.createObject("configuration2.json");

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

		resource = resourcesResourceTestUtils.createObject(resourceJSONString, expectedResource);

		// END resource preparation

		// START schema preparation

		for (int i = 1; i < 6; i++) {

			if (i == 2 || i == 4) {

				// exclude attributes from internal model schema (because they should already exist)

				continue;
			}

			final String attributeJSONFileName = "attribute" + i + ".json";

			final Attribute actualAttribute = attributesResourceTestUtils.createObject(attributeJSONFileName);

			attributes.put(actualAttribute.getId(), actualAttribute);
		}

		recordClass = claszesResourceTestUtils.createObject("clasz1.json");

		// prepare schema json for attribute path ids manipulation
		String schemaJSONString = DMPPersistenceUtil.getResourceAsString("schema.json");
		final ObjectNode schemaJSON = objectMapper.readValue(schemaJSONString, ObjectNode.class);

		for (int j = 1; j < 4; j++) {

			if (j == 2) {

				// exclude attribute paths from internal model schema (because they should already exist)

				continue;
			}

			final String attributePathJSONFileName = "attribute_path" + j + ".json";

			String attributePathJSONString = DMPPersistenceUtil.getResourceAsString(attributePathJSONFileName);
			final AttributePath attributePath = objectMapper.readValue(attributePathJSONString, AttributePath.class);

			final LinkedList<Attribute> attributes = attributePath.getAttributePath();
			final LinkedList<Attribute> newAttributes = Lists.newLinkedList();

			for (final Attribute attribute : attributes) {

				for (final Attribute newAttribute : this.attributes.values()) {

					if (attribute.getUri().equals(newAttribute.getUri())) {

						newAttributes.add(newAttribute);

						break;
					}
				}
			}

			attributePath.setAttributePath(newAttributes);

			attributePathJSONString = objectMapper.writeValueAsString(attributePath);
			final AttributePath expectedAttributePath = objectMapper.readValue(attributePathJSONString, AttributePath.class);
			final AttributePath actualAttributePath = attributePathsResourceTestUtils.createObject(attributePathJSONString, expectedAttributePath);

			attributePaths.put(actualAttributePath.getId(), actualAttributePath);
		}

		// manipulate attribute paths (incl. their attributes)
		final ArrayNode attributePathsArray = objectMapper.createArrayNode();

		for (final AttributePath attributePath : attributePaths.values()) {

			final String attributePathJSONString = objectMapper.writeValueAsString(attributePath);
			final ObjectNode attributePathJSON = objectMapper.readValue(attributePathJSONString, ObjectNode.class);

			attributePathsArray.add(attributePathJSON);
		}

		schemaJSON.put("attribute_paths", attributePathsArray);

		// manipulate record class
		final String recordClassJSONString = objectMapper.writeValueAsString(recordClass);
		final ObjectNode recordClassJSON = objectMapper.readValue(recordClassJSONString, ObjectNode.class);

		schemaJSON.put("record_class", recordClassJSON);

		// re-init expect schema
		schemaJSONString = objectMapper.writeValueAsString(schemaJSON);
		final Schema expectedSchema = objectMapper.readValue(schemaJSONString, Schema.class);

		schema = schemasResourceTestUtils.createObject(schemaJSONString, expectedSchema);

		// END schema preparation

		// START data model preparation

		// prepare data model json for resource, configuration and schema manipulation
		String dataModelJSONString = DMPPersistenceUtil.getResourceAsString("datamodel.json");
		final ObjectNode dataModelJSON = objectMapper.readValue(dataModelJSONString, ObjectNode.class);

		final String finalResourceJSONString = objectMapper.writeValueAsString(resource);
		final ObjectNode finalResourceJSON = objectMapper.readValue(finalResourceJSONString, ObjectNode.class);

		dataModelJSON.put("data_resource", finalResourceJSON);

		final String finalConfigurationJSONString = objectMapper.writeValueAsString(resource.getConfigurations().iterator().next());
		final ObjectNode finalConfigurationJSON = objectMapper.readValue(finalConfigurationJSONString, ObjectNode.class);

		dataModelJSON.put("configuration", finalConfigurationJSON);

		final String finalSchemaJSONString = objectMapper.writeValueAsString(schema);
		final ObjectNode finalSchemaJSON = objectMapper.readValue(finalSchemaJSONString, ObjectNode.class);

		dataModelJSON.put("schema", finalSchemaJSON);

		// re-init expect object
		dataModelJSONString = objectMapper.writeValueAsString(dataModelJSON);
		final DataModel expectedDataModel = objectMapper.readValue(dataModelJSONString, DataModel.class);

		// END data model preparation

		final DataModel dataModel = dataModelsResourceTestUtils.createObject(dataModelJSONString, expectedDataModel);

		dataModels.put(dataModel.getId(), dataModel);

		System.out.println("data model JSON = '" + dataModelJSONString + "'");

		return dataModel;
	}

	private DataModel createOutputDataModel() throws Exception {

		// START data model preparation

		// prepare data model json for resource, configuration and schema manipulation
		String dataModelJSONString = DMPPersistenceUtil.getResourceAsString("datamodel1.json");
		final ObjectNode dataModelJSON = objectMapper.readValue(dataModelJSONString, ObjectNode.class);

		final String finalSchemaJSONString = objectMapper.writeValueAsString(schema);
		final ObjectNode finalSchemaJSON = objectMapper.readValue(finalSchemaJSONString, ObjectNode.class);

		dataModelJSON.put("schema", finalSchemaJSON);

		// re-init expect object
		dataModelJSONString = objectMapper.writeValueAsString(dataModelJSON);
		final DataModel expectedDataModel = objectMapper.readValue(dataModelJSONString, DataModel.class);

		// END data model preparation

		final DataModel dataModel = dataModelsResourceTestUtils.createObject(dataModelJSONString, expectedDataModel);

		dataModels.put(dataModel.getId(), dataModel);

		return dataModel;
	}

	private Mapping createSimpleMapping() throws Exception {

		createAttribute("attribute6.json");

		final AttributePath inputAttributePath = createAttributePath("attribute_path4.json");
		final AttributePath outputAttributePath = createAttributePath("attribute_path5.json");

		final MappingAttributePathInstance inputMappingAttributePathInstance = createMappingAttributePathInstance(
				"input_mapping_attribute_path_instance.json", inputAttributePath);
		final MappingAttributePathInstance outputMappingAttributePathInstance = createMappingAttributePathInstance(
				"output_mapping_attribute_path_instance.json", outputAttributePath);

		function = functionsResourceTestUtils.createObject("function.json");

		// prepare component json for function manipulation
		String componentJSONString = DMPPersistenceUtil.getResourceAsString("component.json");
		final ObjectNode componentJSON = objectMapper.readValue(componentJSONString, ObjectNode.class);

		final String finalFunctionJSONString = objectMapper.writeValueAsString(function);

		Assert.assertNotNull("the function JSON string shouldn't be null", finalFunctionJSONString);

		final ObjectNode finalFunctionJSON = objectMapper.readValue(finalFunctionJSONString, ObjectNode.class);

		Assert.assertNotNull("the function JSON shouldn't be null", finalFunctionJSON);

		componentJSON.put("function", finalFunctionJSON);

		// re-init expect component
		componentJSONString = objectMapper.writeValueAsString(componentJSON);
		final Component expectedComponent = objectMapper.readValue(componentJSONString, Component.class);

		component = componentsResourceTestUtils.createObject(componentJSONString, expectedComponent);

		// prepare transformation json for component manipulation
		String transformationJSONString = DMPPersistenceUtil.getResourceAsString("transformation.json");
		final ObjectNode transformationJSON = objectMapper.readValue(transformationJSONString, ObjectNode.class);

		final String finalComponentJSONString = objectMapper.writeValueAsString(component);

		Assert.assertNotNull("the component JSON string shouldn't be null", finalComponentJSONString);

		final ObjectNode finalComponentJSON = objectMapper.readValue(finalComponentJSONString, ObjectNode.class);

		Assert.assertNotNull("the component JSON shouldn't be null", finalComponentJSON);

		final ArrayNode componentsJSONArray = objectMapper.createArrayNode();

		componentsJSONArray.add(finalComponentJSON);

		transformationJSON.put("components", componentsJSONArray);

		// re-init expect transformation
		transformationJSONString = objectMapper.writeValueAsString(transformationJSON);
		final Transformation expectedTransformation = objectMapper.readValue(transformationJSONString, Transformation.class);

		transformation = transformationsResourceTestUtils.createObject(transformationJSONString, expectedTransformation);

		// prepare transformation component json for function manipulation
		String transformationComponentJSONString = DMPPersistenceUtil.getResourceAsString("transformation_component.json");
		final ObjectNode transformationComponentJSON = objectMapper.readValue(transformationComponentJSONString, ObjectNode.class);

		final String finalTransformationJSONString = objectMapper.writeValueAsString(transformation);

		Assert.assertNotNull("the transformation JSON string shouldn't be null", finalTransformationJSONString);

		final ObjectNode finalTransformationJSON = objectMapper.readValue(finalTransformationJSONString, ObjectNode.class);

		Assert.assertNotNull("the Transformation JSON shouldn't be null", finalTransformationJSON);

		transformationComponentJSON.put("function", finalTransformationJSON);

		// re-init expect transformation component
		transformationComponentJSONString = objectMapper.writeValueAsString(transformationComponentJSON);
		final Component expectedTransformationComponent = objectMapper.readValue(transformationComponentJSONString, Component.class);

		transformationComponent = componentsResourceTestUtils.createObject(transformationComponentJSONString, expectedTransformationComponent);

		// prepare mapping json for transformation, input attribute paths and output attribute path manipulation
		String mappingJSONString = DMPPersistenceUtil.getResourceAsString("mapping.json");
		final ObjectNode mappingJSON = objectMapper.readValue(mappingJSONString, ObjectNode.class);

		final String finalTransformationComponentJSONString = objectMapper.writeValueAsString(transformationComponent);

		Assert.assertNotNull("the transformation component JSON string shouldn't be null", finalTransformationComponentJSONString);

		final ObjectNode finalTransformationComponentJSON = objectMapper.readValue(finalTransformationComponentJSONString, ObjectNode.class);

		Assert.assertNotNull("the transformation component JSON shouldn't be null", finalTransformationComponentJSON);

		mappingJSON.put("transformation", finalTransformationComponentJSON);

		final String finalInputAttributePathJSONString = objectMapper.writeValueAsString(inputMappingAttributePathInstance);

		Assert.assertNotNull("the input attribute path JSON string shouldn't be null", finalInputAttributePathJSONString);

		final ObjectNode finalInputAttributePathJSON = objectMapper.readValue(finalInputAttributePathJSONString, ObjectNode.class);

		Assert.assertNotNull("the input attribute path JSON shouldn't be null", finalInputAttributePathJSON);

		final ArrayNode inputAttributePathsJSON = objectMapper.createArrayNode();

		inputAttributePathsJSON.add(finalInputAttributePathJSON);

		mappingJSON.put("input_attribute_paths", inputAttributePathsJSON);

		final String finalOutputAttributePathJSONString = objectMapper.writeValueAsString(outputMappingAttributePathInstance);

		Assert.assertNotNull("the output attribute path JSON string shouldn't be null", finalOutputAttributePathJSONString);

		final ObjectNode finalOutputAttributePathJSON = objectMapper.readValue(finalOutputAttributePathJSONString, ObjectNode.class);

		Assert.assertNotNull("the output attribute path JSON shouldn't be null", finalOutputAttributePathJSON);

		mappingJSON.put("output_attribute_path", finalOutputAttributePathJSON);

		// re-init expect object
		mappingJSONString = objectMapper.writeValueAsString(mappingJSON);
		final Mapping expectedMapping = objectMapper.readValue(mappingJSONString, Mapping.class);

		System.out.println("mapping json = '" + objectJSONString + "'");

		final Mapping mapping = mappingsResourceTestUtils.createObject(mappingJSONString, expectedMapping);

		mappings.put(mapping.getId(), mapping);

		return mapping;
	}

	private Attribute createAttribute(final String attributeJSONFileName) throws Exception {

		final Attribute actualAttribute = attributesResourceTestUtils.createObject(attributeJSONFileName);

		attributes.put(actualAttribute.getId(), actualAttribute);

		return actualAttribute;
	}

	private AttributePath createAttributePath(final String attributePathJSONFileName) throws Exception {

		String attributePathJSONString = DMPPersistenceUtil.getResourceAsString(attributePathJSONFileName);
		final AttributePath attributePath = objectMapper.readValue(attributePathJSONString, AttributePath.class);

		final LinkedList<Attribute> attributes = attributePath.getAttributePath();
		final LinkedList<Attribute> newAttributes = Lists.newLinkedList();

		for (final Attribute attribute : attributes) {

			for (final Attribute newAttribute : this.attributes.values()) {

				if (attribute.getUri().equals(newAttribute.getUri())) {

					newAttributes.add(newAttribute);

					break;
				}
			}
		}

		attributePath.setAttributePath(newAttributes);

		attributePathJSONString = objectMapper.writeValueAsString(attributePath);
		final AttributePath expectedAttributePath = objectMapper.readValue(attributePathJSONString, AttributePath.class);
		final AttributePath actualAttributePath = attributePathsResourceTestUtils.createObject(attributePathJSONString, expectedAttributePath);

		attributePaths.put(actualAttributePath.getId(), actualAttributePath);

		return actualAttributePath;
	}

	private MappingAttributePathInstance createMappingAttributePathInstance(final String mappingAttributePathInstanceFileName,
			final AttributePath attributePath) throws Exception {

		String mappingAttributePathInstanceJSONString = DMPPersistenceUtil.getResourceAsString(mappingAttributePathInstanceFileName);
		final MappingAttributePathInstance mappingAttributePathInstanceFromJSON = objectMapper.readValue(mappingAttributePathInstanceJSONString,
				MappingAttributePathInstance.class);

		mappingAttributePathInstanceFromJSON.setAttributePath(attributePath);

		mappingAttributePathInstanceJSONString = objectMapper.writeValueAsString(mappingAttributePathInstanceFromJSON);
		final MappingAttributePathInstance expectedMappingAttributePathInstance = objectMapper.readValue(mappingAttributePathInstanceJSONString,
				MappingAttributePathInstance.class);
		final MappingAttributePathInstance actualMappingAttributePathInstance = mappingAttributePathInstancesResourceTestUtils.createObject(
				mappingAttributePathInstanceJSONString, expectedMappingAttributePathInstance);

		mappingAttributePathInstances.put(actualMappingAttributePathInstance.getId(), actualMappingAttributePathInstance);

		return actualMappingAttributePathInstance;
	}
}
