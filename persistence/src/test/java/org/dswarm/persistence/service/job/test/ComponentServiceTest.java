/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.service.job.test;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.FunctionType;
import org.dswarm.persistence.model.job.proxy.ProxyComponent;
import org.dswarm.persistence.service.job.ComponentService;
import org.dswarm.persistence.service.job.test.utils.FunctionServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;

public class ComponentServiceTest extends IDBasicJPAServiceTest<ProxyComponent, Component, ComponentService> {

	private static final Logger				LOG				= LoggerFactory.getLogger(ComponentServiceTest.class);

	private final ObjectMapper				objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private final Map<Long, Function>		functions		= Maps.newLinkedHashMap();

	private final FunctionServiceTestUtils	functionServiceTestUtils;

	public ComponentServiceTest() {

		super("component", ComponentService.class);

		functionServiceTestUtils = new FunctionServiceTestUtils();
	}

	@Test
	public void testSimpleComponent() throws Exception {

		final LinkedList<String> parameters = Lists.newLinkedList();

		parameters.add("inputString");

		final Function function = functionServiceTestUtils.createFunction("trim", "trims leading and trailing whitespaces from a given string",
				parameters);

		final String componentName = "my trim component";
		final Map<String, String> parameterMapping = Maps.newLinkedHashMap();

		final String functionParameterName = "inputString";
		final String componentVariableName = "previousComponent.outputString";

		parameterMapping.put(functionParameterName, componentVariableName);

		final Component component = createObject().getObject();
		component.setName(componentName);
		component.setFunction(function);
		component.setParameterMappings(parameterMapping);

		final Component updatedComponent = updateObjectTransactional(component).getObject();

		Assert.assertNotNull("the updated component shouldn't be null", updatedComponent);
		Assert.assertNotNull("the component id shouldn't be null", updatedComponent.getId());
		Assert.assertNotNull("the component name shouldn't be null", updatedComponent.getName());
		Assert.assertEquals("the component names are not equal", componentName, updatedComponent.getName());
		Assert.assertNotNull("the component parameter mappings shouldn't be null", updatedComponent.getParameterMappings());
		Assert.assertEquals("the component parameter mappings' size are not equal", 1, updatedComponent.getParameterMappings().size());
		Assert.assertTrue("the component parameter mappings doesn't contain a mapping for function parameter '" + functionParameterName + "'",
				updatedComponent.getParameterMappings().containsKey(functionParameterName));
		Assert.assertEquals("the component parameter mapping for '" + functionParameterName + "' are not equal", componentVariableName,
				updatedComponent.getParameterMappings().get(functionParameterName));
		Assert.assertEquals("the function type is not '" + FunctionType.Function + "'", FunctionType.Function, updatedComponent.getFunction()
				.getFunctionType());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedComponent);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ComponentServiceTest.LOG.debug("component json: " + json);

		// clean up DB
		deleteObject(component.getId());
		functionServiceTestUtils.deleteObject(function);
	}

	@Test
	public void complexComponentTest() throws Exception {

		// previous component

		final String function1Name = "replace";
		final String function1Description = "replace certain parts of a given string that matches a certain regex";
		final String function1Parameter = "inputString";
		final String function2Parameter = "regex";
		final String function3Parameter = "replaceString";

		final LinkedList<String> function1Parameters = Lists.newLinkedList();
		function1Parameters.add(function1Parameter);
		function1Parameters.add(function2Parameter);
		function1Parameters.add(function3Parameter);

		final Function function1 = functionServiceTestUtils.createFunction(function1Name, function1Description, function1Parameters);
		functions.put(function1.getId(), function1);

		final String component1Name = "my replace component";
		final Map<String, String> parameterMapping1 = Maps.newLinkedHashMap();

		final String functionParameterName1 = "inputString";
		final String componentVariableName1 = "previousComponent.outputString";
		final String functionParameterName2 = "regex";
		final String componentVariableName2 = "\\.";
		final String functionParameterName3 = "replaceString";
		final String componentVariableName3 = ":";

		parameterMapping1.put(functionParameterName1, componentVariableName1);
		parameterMapping1.put(functionParameterName2, componentVariableName2);
		parameterMapping1.put(functionParameterName3, componentVariableName3);

		final Component component1 = createComponent(component1Name, parameterMapping1, function1);

		// next component

		final String function2Name = "lower_case";
		final String function2Description = "lower cases all characters of a given string";
		final String function4Parameter = "inputString";

		final LinkedList<String> function2Parameters = Lists.newLinkedList();
		function2Parameters.add(function4Parameter);

		final Function function2 = functionServiceTestUtils.createFunction(function2Name, function2Description, function2Parameters);
		functions.put(function2.getId(), function2);

		final String component2Name = "my lower case component";
		final Map<String, String> parameterMapping2 = Maps.newLinkedHashMap();

		final String functionParameterName4 = "inputString";
		final String componentVariableName4 = "previousComponent.outputString";

		parameterMapping2.put(functionParameterName4, componentVariableName4);

		final Component component2 = createComponent(component2Name, parameterMapping2, function2);

		// main component

		final String functionName = "trim";
		final String functionDescription = "trims leading and trailing whitespaces from a given string";
		final String functionParameter = "inputString";

		final LinkedList<String> functionParameters = Lists.newLinkedList();
		functionParameters.add(functionParameter);

		final Function function = functionServiceTestUtils.createFunction(functionName, functionDescription, functionParameters);
		functions.put(function.getId(), function);

		// final String componentId = UUID.randomUUID().toString();
		final String componentName = "my trim component";
		final Map<String, String> parameterMapping = Maps.newLinkedHashMap();

		final String functionParameterName = "inputString";
		final String componentVariableName = "previousComponent.outputString";

		parameterMapping.put(functionParameterName, componentVariableName);

		final Set<Component> inputComponents = Sets.newLinkedHashSet();

		inputComponents.add(component1);

		final Set<Component> outputComponents = Sets.newLinkedHashSet();

		outputComponents.add(component2);

		final Component component = createObject().getObject();
		component.setName(componentName);
		component.setFunction(function);
		component.setParameterMappings(parameterMapping);
		component.setInputComponents(inputComponents);
		component.setOutputComponents(outputComponents);

		final Component updatedComponent = updateObjectTransactional(component).getObject();

		Assert.assertNotNull("the component id shouldn't be null", updatedComponent.getId());
		Assert.assertNotNull("the component name shouldn't be null", updatedComponent.getName());
		Assert.assertEquals("the component names are not equal", componentName, updatedComponent.getName());
		Assert.assertNotNull("the component parameter mappings shouldn't be null", updatedComponent.getParameterMappings());
		Assert.assertEquals("the component parameter mappings' size are not equal", 1, updatedComponent.getParameterMappings().size());
		Assert.assertTrue("the component parameter mappings doesn't contain a mapping for function parameter '" + functionParameterName + "'",
				updatedComponent.getParameterMappings().containsKey(functionParameterName));
		Assert.assertEquals("the component parameter mapping for '" + functionParameterName + "' are not equal", componentVariableName,
				updatedComponent.getParameterMappings().get(functionParameterName));
		Assert.assertNotNull("the component input components set shouldn't be null", updatedComponent.getInputComponents());
		Assert.assertEquals("the component input components set are not equal", 1, updatedComponent.getInputComponents().size());
		Assert.assertTrue("the component input components set doesn't contain component '" + component1.getId() + "'", updatedComponent
				.getInputComponents().contains(component1));
		Assert.assertEquals("the component input component '" + component1.getId() + "' are not equal", component1, updatedComponent
				.getInputComponents().iterator().next());
		Assert.assertNotNull("the component output components set shouldn't be null", updatedComponent.getOutputComponents());
		Assert.assertEquals("the component output components set are not equal", 1, updatedComponent.getOutputComponents().size());
		Assert.assertTrue("the component output components set doesn't contain component '" + component2.getId() + "'", updatedComponent
				.getOutputComponents().contains(component2));
		Assert.assertEquals("the component output component '" + component2.getId() + "' are not equal", component2, updatedComponent
				.getOutputComponents().iterator().next());
		Assert.assertEquals("the function type is not '" + FunctionType.Function + "'", FunctionType.Function, updatedComponent.getFunction()
				.getFunctionType());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedComponent);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ComponentServiceTest.LOG.debug("component json: " + json);

		// clean-up
		deleteObject(updatedComponent.getId());
		deleteObject(component1.getId());
		deleteObject(component2.getId());

		for (final Function functionToDelete : functions.values()) {

			functionServiceTestUtils.deleteObject(functionToDelete);
		}
	}

	private Component createComponent(final String name, final Map<String, String> parameterMappings, final Function function) {

		Component component = null;

		try {

			component = jpaService.createObjectTransactional().getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while component creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("component shouldn't be null", component);
		Assert.assertNotNull("component id shouldn't be null", component.getId());

		component.setName(name);
		component.setFunction(function);
		component.setParameterMappings(parameterMappings);

		Component updatedComponent = null;

		try {

			updatedComponent = jpaService.updateObjectTransactional(component).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the component of id = '" + component.getId() + "'", false);
		}

		Assert.assertNotNull("the updated component shouldn't be null", updatedComponent);
		Assert.assertNotNull("the component name shouldn't be null", updatedComponent.getName());
		Assert.assertEquals("the component names are not equal", name, updatedComponent.getName());
		Assert.assertNotNull("the component parameter mappings shouldn't be null", updatedComponent.getParameterMappings());
		Assert.assertEquals("the function type is not '" + FunctionType.Function + "'", FunctionType.Function, updatedComponent.getFunction()
				.getFunctionType());

		return updatedComponent;
	}
}
