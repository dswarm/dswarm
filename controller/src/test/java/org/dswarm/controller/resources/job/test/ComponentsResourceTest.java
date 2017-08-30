/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.controller.resources.job.test;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
		BasicResourceTest<ComponentsResourceTestUtils, ComponentServiceTestUtils, ComponentService, ProxyComponent, Component> {

	private FunctionsResourceTestUtils functionsResourceTestUtils;

	public ComponentsResourceTest() {

		super(Component.class, ComponentService.class, "components", "component.json", new ComponentsResourceTestUtils());
	}

	@Override
	protected void initObjects() {

		super.initObjects();

		pojoClassResourceTestUtils = new ComponentsResourceTestUtils();
		functionsResourceTestUtils = new FunctionsResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		final Function function = functionsResourceTestUtils.createObject("function.json");

		// prepare component json for function manipulation
		final ObjectNode objectJSON = objectMapper.readValue(objectJSONString, ObjectNode.class);

		final String finalFunctionJSONString = objectMapper.writeValueAsString(function);

		Assert.assertNotNull("the function JSON string shouldn't be null", finalFunctionJSONString);

		final ObjectNode finalFunctionJSON = objectMapper.readValue(finalFunctionJSONString, ObjectNode.class);

		Assert.assertNotNull("the function JSON shouldn't be null", finalFunctionJSON);

		objectJSON.set("function", finalFunctionJSON);

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(objectJSON);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);
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

		final Component updateComponent = pojoClassResourceTestUtils.updateObject(updateComponentJSONString, actualComponent);

		Assert.assertNotNull("the component JSON string shouldn't be null", updateComponent);
		Assert.assertEquals("function names shoud be equal", updateComponent.getFunction().getName(), functionName);
		Assert.assertEquals("component description shoud be equal", updateComponent.getDescription(), actualComponent.getDescription());

		return updateComponent;
	}
}
