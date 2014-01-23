package de.avgl.dmp.controller.resources.test;

import org.junit.After;
import org.junit.Assert;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.controller.resources.test.utils.ComponentsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.FunctionsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.TransformationsResourceTestUtils;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.service.job.TransformationService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class TransformationsResourceTest extends BasicResourceTest<TransformationsResourceTestUtils, TransformationService, Transformation, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributesResourceTest.class);

	private final FunctionsResourceTestUtils		functionsResourceTestUtils;

	private final ComponentsResourceTestUtils		componentsResourceTestUtils;
	
	private final TransformationsResourceTestUtils	transformationsResourceTestUtils;

	private Function								function;

	private Component								component;

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
	public Transformation updateObject(final Transformation actualTransformation) throws Exception {
		
		actualTransformation.setDescription(actualTransformation.getDescription() + " update");
		
		// TODO: [@fniederlein] update some more nested properties
		
		final String updateTransformationJSONString = objectMapper.writeValueAsString(actualTransformation);
		
		Assert.assertNotNull("the transformation JSON string shouldn't be null", updateTransformationJSONString);
		
		final Transformation updateTransformation = transformationsResourceTestUtils.updateObject(updateTransformationJSONString, actualTransformation);
		
		Assert.assertNotNull("the transformation JSON string shouldn't be null", updateTransformation);
		Assert.assertEquals("transformation name shoud be equal", updateTransformation.getName(), actualTransformation.getName());
		
		return updateTransformation;
	}
	
	@After
	public void tearDown2() throws Exception {

		functionsResourceTestUtils.deleteObject(function);
	}
}
