package de.avgl.dmp.controller.resources.job.test;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.controller.resources.job.test.utils.ComponentsResourceTestUtils;
import de.avgl.dmp.controller.resources.job.test.utils.FunctionsResourceTestUtils;
import de.avgl.dmp.controller.resources.job.test.utils.TransformationsResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.AttributesResourceTest;
import de.avgl.dmp.controller.resources.test.BasicResourceTest;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.job.proxy.ProxyTransformation;
import de.avgl.dmp.persistence.service.job.TransformationService;
import de.avgl.dmp.persistence.service.job.test.utils.TransformationServiceTestUtils;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class TransformationsResourceTest
		extends
		BasicResourceTest<TransformationsResourceTestUtils, TransformationServiceTestUtils, TransformationService, ProxyTransformation, Transformation, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributesResourceTest.class);

	private final FunctionsResourceTestUtils		functionsResourceTestUtils;

	private final ComponentsResourceTestUtils		componentsResourceTestUtils;

	private final TransformationsResourceTestUtils	transformationsResourceTestUtils;

	private Function								function;

	private Component								component;

	private Function								updateFunction;

	private Component								updateComponent;

	public TransformationsResourceTest() {

		super(Transformation.class, TransformationService.class, "transformations", "transformation.json", new TransformationsResourceTestUtils());

		functionsResourceTestUtils = new FunctionsResourceTestUtils();
		componentsResourceTestUtils = new ComponentsResourceTestUtils();
		transformationsResourceTestUtils = new TransformationsResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

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
		final ObjectNode objectJSON = objectMapper.readValue(objectJSONString, ObjectNode.class);

		final String finalComponentJSONString = objectMapper.writeValueAsString(component);

		Assert.assertNotNull("the component JSON string shouldn't be null", finalComponentJSONString);

		final ObjectNode finalComponentJSON = objectMapper.readValue(finalComponentJSONString, ObjectNode.class);

		Assert.assertNotNull("the component JSON shouldn't be null", finalComponentJSON);

		final ArrayNode componentsJSONArray = objectMapper.createArrayNode();

		componentsJSONArray.add(finalComponentJSON);

		objectJSON.put("components", componentsJSONArray);

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(objectJSON);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);
	}

	@Override
	public void testPUTObject() throws Exception {

		super.testPUTObject();

		functionsResourceTestUtils.deleteObject(updateFunction);
	}

	@Override
	protected Transformation updateObject(final Transformation persistedTransformation) throws Exception {

		String functionJSONString = DMPPersistenceUtil.getResourceAsString("function.json");
		final ObjectNode functionJSON = objectMapper.readValue(functionJSONString, ObjectNode.class);

		String componentJSONString = DMPPersistenceUtil.getResourceAsString("component.json");
		final ObjectNode componentJSON = objectMapper.readValue(componentJSONString, ObjectNode.class);

		// update function in component object
		componentJSON.put("function", functionJSON);
		componentJSONString = objectMapper.writeValueAsString(componentJSON);
		final Component expectedComponent = objectMapper.readValue(componentJSONString, Component.class);
		updateComponent = componentsResourceTestUtils.createObject(componentJSONString, expectedComponent);
		updateFunction = updateComponent.getFunction();

		Set<Component> components = new LinkedHashSet<Component>();
		components.add(updateComponent);
		persistedTransformation.setComponents(components);

		String updateTransformationJSONString = objectMapper.writeValueAsString(persistedTransformation);
		final ObjectNode updateTransformationJSON = objectMapper.readValue(updateTransformationJSONString, ObjectNode.class);

		// update name
		final String updateTransformationNameString = persistedTransformation.getName() + " update";
		updateTransformationJSON.put("name", updateTransformationNameString);

		// update description
		final String updateTransformationDescriptionString = persistedTransformation.getDescription() + " update";
		updateTransformationJSON.put("description", updateTransformationDescriptionString);

		updateTransformationJSONString = objectMapper.writeValueAsString(updateTransformationJSON);
		final Transformation expectedTransformation = objectMapper.readValue(updateTransformationJSONString, Transformation.class);
		Assert.assertNotNull("the transformation JSON string shouldn't be null", updateTransformationJSONString);

		final Transformation updateTransformation = transformationsResourceTestUtils.updateObject(updateTransformationJSONString,
				expectedTransformation);

		Assert.assertNotNull("the transformation JSON string shouldn't be null", updateTransformation);
		Assert.assertEquals("transformation id shoud be equal", updateTransformation.getId(), persistedTransformation.getId());
		Assert.assertEquals("transformation name shoud be equal", updateTransformation.getName(), updateTransformationNameString);
		Assert.assertEquals("transformation description shoud be equal", updateTransformation.getDescription(), updateTransformationDescriptionString);

		Set<Component> components1 = expectedTransformation.getComponents();
		Set<Component> components2 = updateTransformation.getComponents();
		Assert.assertEquals("number of components should be equal", components1.size(), components2.size());
		Assert.assertTrue("components of the transformation should be equal", components1.equals(components2));

		Assert.assertTrue("transformation function should be equal", updateTransformation.getComponents().contains(updateComponent));

		return updateTransformation;
	}

	@After
	public void tearDown2() throws Exception {

		functionsResourceTestUtils.deleteObject(function);
	}
}
