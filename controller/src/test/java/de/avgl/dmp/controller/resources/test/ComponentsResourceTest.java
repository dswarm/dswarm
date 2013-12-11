package de.avgl.dmp.controller.resources.test;

import org.junit.After;
import org.junit.Assert;

import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.controller.resources.test.utils.ComponentsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.FunctionsResourceTestUtils;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.service.job.ComponentService;

public class ComponentsResourceTest extends BasicResourceTest<ComponentsResourceTestUtils, ComponentService, Component, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ComponentsResourceTest.class);

	private final FunctionsResourceTestUtils		functionsResourceTestUtils;

	private Function								function;

	public ComponentsResourceTest() {

		super(Component.class, ComponentService.class, "components", "component.json", new ComponentsResourceTestUtils());

		functionsResourceTestUtils = new FunctionsResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		function = functionsResourceTestUtils.createObject("function.json");

		// prepare component json for function manipulation
		final ObjectNode objectJSON = objectMapper.readValue(objectJSONString, ObjectNode.class);

		final String finalFunctionJSONString = objectMapper.writeValueAsString(function);

		Assert.assertNotNull("the function JSON string shouldn't be null", finalFunctionJSONString);

		final ObjectNode finalFunctionJSON = objectMapper.readValue(finalFunctionJSONString, ObjectNode.class);

		Assert.assertNotNull("the function JSON shouldn't be null", finalFunctionJSON);

		objectJSON.put("function", finalFunctionJSON);

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(objectJSON);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);

		System.out.println("component json = '" + objectJSONString + "'");
	}

	@After
	public void tearDown2() throws Exception {

		functionsResourceTestUtils.deleteObject(function);
	}
}
