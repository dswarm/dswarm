package de.avgl.dmp.controller.resources.test;

import java.util.LinkedList;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.avgl.dmp.controller.resources.test.utils.AttributePathsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.AttributesResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.ComponentsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.FunctionsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.MappingsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.TransformationsResourceTestUtils;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.service.job.MappingService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class MappingsResourceTest extends BasicResourceTest<MappingsResourceTestUtils, MappingService, Mapping, Long> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(AttributesResourceTest.class);

	private final FunctionsResourceTestUtils		functionsResourceTestUtils;

	private final TransformationsResourceTestUtils	transformationsResourceTestUtils;

	private final ComponentsResourceTestUtils		componentsResourceTestUtils;

	private final AttributesResourceTestUtils		attributesResourceTestUtils;

	private final AttributePathsResourceTestUtils	attributePathsResourceTestUtils;

	private Function								function;

	private Component								component;

	private Transformation							transformation;

	private Component								transformationComponent;

	final Map<Long, Attribute>					attributes		= Maps.newHashMap();

	final Map<Long, AttributePath>					attributePaths	= Maps.newLinkedHashMap();

	public MappingsResourceTest() {

		super(Mapping.class, MappingService.class, "mappings", "mapping.json", new MappingsResourceTestUtils());

		functionsResourceTestUtils = new FunctionsResourceTestUtils();
		componentsResourceTestUtils = new ComponentsResourceTestUtils();
		attributesResourceTestUtils = new AttributesResourceTestUtils();
		attributePathsResourceTestUtils = new AttributePathsResourceTestUtils();
		transformationsResourceTestUtils = new TransformationsResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		createAttribute("attribute1.json");
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
		final ObjectNode objectJSON = objectMapper.readValue(objectJSONString, ObjectNode.class);

		final String finalTransformationComponentJSONString = objectMapper.writeValueAsString(transformationComponent);

		Assert.assertNotNull("the transformation component JSON string shouldn't be null", finalTransformationComponentJSONString);

		final ObjectNode finalTransformationComponentJSON = objectMapper.readValue(finalTransformationComponentJSONString, ObjectNode.class);

		Assert.assertNotNull("the transformation component JSON shouldn't be null", finalTransformationComponentJSON);

		objectJSON.put("transformation", finalTransformationComponentJSON);

		final String finalInputAttributePathJSONString = objectMapper.writeValueAsString(inputAttributePath);

		Assert.assertNotNull("the input attribute path JSON string shouldn't be null", finalInputAttributePathJSONString);

		final ObjectNode finalInputAttributePathJSON = objectMapper.readValue(finalInputAttributePathJSONString, ObjectNode.class);

		Assert.assertNotNull("the input attribute path JSON shouldn't be null", finalInputAttributePathJSON);

		final ArrayNode inputAttributePathsJSON = objectMapper.createArrayNode();

		inputAttributePathsJSON.add(finalInputAttributePathJSON);

		objectJSON.put("input_attribute_paths", inputAttributePathsJSON);

		final String finalOutputAttributePathJSONString = objectMapper.writeValueAsString(outputAttributePath);

		Assert.assertNotNull("the output attribute path JSON string shouldn't be null", finalOutputAttributePathJSONString);

		final ObjectNode finalOutputAttributePathJSON = objectMapper.readValue(finalOutputAttributePathJSONString, ObjectNode.class);

		Assert.assertNotNull("the output attribute path JSON shouldn't be null", finalOutputAttributePathJSON);

		objectJSON.put("output_attribute_path", finalOutputAttributePathJSON);

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(objectJSON);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);

		System.out.println("mapping json = '" + objectJSONString + "'");
	}

	@After
	public void tearDown2() throws Exception {

		for (final AttributePath attributePath : attributePaths.values()) {

			attributePathsResourceTestUtils.deleteObject(attributePath);
		}
		
		for (final Attribute attribute : attributes.values()) {

			attributesResourceTestUtils.deleteObject(attribute);
		}

		transformationsResourceTestUtils.deleteObject(transformation);

		functionsResourceTestUtils.deleteObject(function);
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
}
