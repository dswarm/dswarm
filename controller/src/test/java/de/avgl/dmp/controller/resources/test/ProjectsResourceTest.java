package de.avgl.dmp.controller.resources.test;

import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

import de.avgl.dmp.controller.resources.test.utils.AttributePathsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.AttributesResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.ClaszesResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.ComponentsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.ConfigurationsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.DataModelsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.FunctionsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.MappingsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.ProjectsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.ResourcesResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.SchemasResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.TransformationsResourceTestUtils;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.model.job.Project;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.job.ProjectService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class ProjectsResourceTest extends BasicResourceTest<ProjectsResourceTestUtils, ProjectService, Project, Long> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(AttributesResourceTest.class);

	private final FunctionsResourceTestUtils		functionsResourceTestUtils;

	private final TransformationsResourceTestUtils	transformationsResourceTestUtils;

	private final ComponentsResourceTestUtils		componentsResourceTestUtils;

	private final AttributesResourceTestUtils		attributesResourceTestUtils;

	private final AttributePathsResourceTestUtils	attributePathsResourceTestUtils;

	private final ClaszesResourceTestUtils			claszesResourceTestUtils;

	private final ResourcesResourceTestUtils		resourcesResourceTestUtils;

	private final ConfigurationsResourceTestUtils	configurationsResourceTestUtils;

	private final SchemasResourceTestUtils			schemasResourceTestUtils;

	private final DataModelsResourceTestUtils		dataModelsResourceTestUtils;

	private final MappingsResourceTestUtils			mappingsResourceTestUtils;

	private Function								function;

	private Component								component;

	private Transformation							transformation;

	private Component								transformationComponent;

	final Map<String, Attribute>					attributes		= Maps.newHashMap();

	final Map<Long, AttributePath>					attributePaths	= Maps.newLinkedHashMap();

	private Clasz									recordClass;

	private Schema									schema;

	private Configuration							configuration;

	private Resource								resource;

	private Map<Long, DataModel>					dataModels		= Maps.newHashMap();

	private Map<Long, Mapping>						mappings		= Maps.newHashMap();

	public ProjectsResourceTest() {

		super(Project.class, ProjectService.class, "projects", "project.json", new ProjectsResourceTestUtils());

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
	}

	@Override
	public void prepare() throws Exception {

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
	
	@After
	public void tearDown2() throws Exception {

		// START data models tear down

		for (final DataModel dataModel : dataModels.values()) {

			dataModelsResourceTestUtils.deleteObject(dataModel);
		}

		// resource clean-up

		resourcesResourceTestUtils.deleteObject(resource);

		// configuration clean-up

		configurationsResourceTestUtils.deleteObject(configuration);

		// START schema clean-up

		schemasResourceTestUtils.deleteObject(schema);

		claszesResourceTestUtils.deleteObject(recordClass);

		// END schema clean-up

		// END data models tear down

		// START mappings tear down

		for (final Mapping mapping : mappings.values()) {

			mappingsResourceTestUtils.deleteObject(mapping);
		}

		for (final AttributePath attributePath : attributePaths.values()) {

			attributePathsResourceTestUtils.deleteObject(attributePath);
		}

		for (final Attribute attribute : attributes.values()) {

			attributesResourceTestUtils.deleteObject(attribute);
		}

		transformationsResourceTestUtils.deleteObject(transformation);

		functionsResourceTestUtils.deleteObject(function);

		// END mappings tear down
	}
	
	@Ignore
	@Override
	public void testPUTObject() throws Exception {

		//super.testPUTObject();
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

		for (int i = 1; i <= 6; i++) {

			final String attributeJSONFileName = "attribute" + i + ".json";

			final Attribute actualAttribute = attributesResourceTestUtils.createObject(attributeJSONFileName);

			attributes.put(actualAttribute.getId(), actualAttribute);
		}

		recordClass = claszesResourceTestUtils.createObject("clasz.json");

		// prepare schema json for attribute path ids manipulation
		String schemaJSONString = DMPPersistenceUtil.getResourceAsString("schema.json");
		final ObjectNode schemaJSON = objectMapper.readValue(schemaJSONString, ObjectNode.class);

		for (int j = 1; j <= 4; j++) {

			final String attributePathJSONFileName = "attribute_path" + j + ".json";

			final AttributePath actualAttributePath = attributePathsResourceTestUtils.createObject(attributePathJSONFileName);

			attributePaths.put(actualAttributePath.getId(), actualAttributePath);

			// manipulate attribute path ids
			ArrayNode attributePathsArray = (ArrayNode) schemaJSON.get("attribute_paths");

			for (final JsonNode attributePathJsonNode : attributePathsArray) {

				if (((ObjectNode) attributePathJsonNode).get("id").asInt() == j) {

					((ObjectNode) attributePathJsonNode).put("id", actualAttributePath.getId());

					break;
				}
			}
		}

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

		final String finalInputAttributePathJSONString = objectMapper.writeValueAsString(inputAttributePath);

		Assert.assertNotNull("the input attribute path JSON string shouldn't be null", finalInputAttributePathJSONString);

		final ObjectNode finalInputAttributePathJSON = objectMapper.readValue(finalInputAttributePathJSONString, ObjectNode.class);

		Assert.assertNotNull("the input attribute path JSON shouldn't be null", finalInputAttributePathJSON);

		final ArrayNode inputAttributePathsJSON = objectMapper.createArrayNode();

		inputAttributePathsJSON.add(finalInputAttributePathJSON);

		mappingJSON.put("input_attribute_paths", inputAttributePathsJSON);

		final String finalOutputAttributePathJSONString = objectMapper.writeValueAsString(outputAttributePath);

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

		final AttributePath actualAttributePath = attributePathsResourceTestUtils.createObject(attributePathJSONFileName);

		attributePaths.put(actualAttributePath.getId(), actualAttributePath);

		return actualAttributePath;
	}
}
