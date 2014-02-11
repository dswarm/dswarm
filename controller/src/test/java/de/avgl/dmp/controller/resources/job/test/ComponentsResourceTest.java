package de.avgl.dmp.controller.resources.job.test;

import org.junit.After;
import org.junit.Assert;

import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.controller.resources.job.test.utils.ComponentsResourceTestUtils;
import de.avgl.dmp.controller.resources.job.test.utils.FunctionsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.BasicResourceTest;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.proxy.ProxyComponent;
import de.avgl.dmp.persistence.service.job.ComponentService;
import de.avgl.dmp.persistence.service.job.test.utils.ComponentServiceTestUtils;

public class ComponentsResourceTest extends
		BasicResourceTest<ComponentsResourceTestUtils, ComponentServiceTestUtils, ComponentService, ProxyComponent, Component, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ComponentsResourceTest.class);

	private final FunctionsResourceTestUtils		functionsResourceTestUtils;
	private final ComponentsResourceTestUtils		componentsResourceTestUtils;

	private Function								function;

	public ComponentsResourceTest() {

		super(Component.class, ComponentService.class, "components", "component.json", new ComponentsResourceTestUtils());

		functionsResourceTestUtils = new FunctionsResourceTestUtils();
		componentsResourceTestUtils = new ComponentsResourceTestUtils();
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
	}

	@After
	public void tearDown2() throws Exception {

		functionsResourceTestUtils.deleteObject(function);
	}

	@Override
	public Component updateObject(final Component actualComponent) throws Exception {

		Function componentFunction = actualComponent.getFunction();

		String functionName = componentFunction.getName() + " update function";

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
