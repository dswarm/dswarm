package org.dswarm.controller.resources.job.test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.After;
import org.junit.Assert;

import org.dswarm.controller.resources.job.test.utils.ComponentsResourceTestUtils;
import org.dswarm.controller.resources.job.test.utils.FunctionsResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.proxy.ProxyComponent;
import org.dswarm.persistence.service.job.ComponentService;
import org.dswarm.persistence.service.job.test.utils.ComponentServiceTestUtils;

public class ComponentsResourceTest extends
		BasicResourceTest<ComponentsResourceTestUtils, ComponentServiceTestUtils, ComponentService, ProxyComponent, Component, Long> {

	private FunctionsResourceTestUtils	functionsResourceTestUtils;
	private ComponentsResourceTestUtils	componentsResourceTestUtils;

	private Function					function;

	public ComponentsResourceTest() {

		super(Component.class, ComponentService.class, "components", "component.json", new ComponentsResourceTestUtils());
	}

	@Override
	protected void initObjects() {

		super.initObjects();

		pojoClassResourceTestUtils = new ComponentsResourceTestUtils();
		functionsResourceTestUtils = new FunctionsResourceTestUtils();
		componentsResourceTestUtils = new ComponentsResourceTestUtils();
	}

	private void resetObjectVars() {

		function = null;
	}

	@Override
	public void prepare() throws Exception {

		restartServer();
		initObjects();
		resetObjectVars();

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
	}

	@After
	public void tearDown2() throws Exception {

		functionsResourceTestUtils.deleteObject(function);
	}

	@Override
	protected Component updateObject(final Component actualComponent) throws Exception {

		final Function componentFunction = actualComponent.getFunction();

		final String functionName = componentFunction.getName() + " update function";

		componentFunction.setName(functionName);

		actualComponent.setFunction(componentFunction);

		actualComponent.setDescription(actualComponent.getDescription() + " update component");

		final String updateComponentJSONString = objectMapper.writeValueAsString(actualComponent);

		Assert.assertNotNull("the component JSON string shouldn't be null", updateComponentJSONString);

		final Component updateComponent = componentsResourceTestUtils.updateObject(updateComponentJSONString, actualComponent);

		Assert.assertNotNull("the component JSON string shouldn't be null", updateComponent);
		Assert.assertEquals("function names shoud be equal", updateComponent.getFunction().getName(), functionName);
		Assert.assertEquals("component description shoud be equal", updateComponent.getDescription(), actualComponent.getDescription());

		return updateComponent;
	}
}
