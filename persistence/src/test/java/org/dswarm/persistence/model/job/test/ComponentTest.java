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
package org.dswarm.persistence.model.job.test;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.service.UUIDService;

public class ComponentTest extends GuicedTest {

	private static final Logger LOG = LoggerFactory.getLogger(ComponentTest.class);

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	@Test
	public void simpleComponentTest() {

		// final String functionId = UUID.randomUUID().toString();
		final String functionName = "trim";
		final String functionDescription = "trims leading and trailing whitespaces from a given string";
		final String functionParameter = "inputString";

		final String uuid = UUIDService.getUUID(Function.class.getSimpleName());

		final Function function = new Function(uuid);
		// function.setId(functionId);
		function.setName(functionName);
		function.setDescription(functionDescription);
		function.addParameter(functionParameter);

		// final String componentId = UUID.randomUUID().toString();
		final String componentName = "my trim component";
		final Map<String, String> parameterMapping = Maps.newLinkedHashMap();

		final String functionParameterName = "inputString";
		final String componentVariableName = "previousComponent.outputString";

		parameterMapping.put(functionParameterName, componentVariableName);

		final String componentUUID = UUIDService.getUUID(Component.class.getSimpleName());

		final Component component = new Component(componentUUID);
		// component.setId(componentId);
		component.setName(componentName);
		component.setFunction(function);
		component.setParameterMappings(parameterMapping);

		// Assert.assertNotNull("the component id shouldn't be null", component.getId());
		// Assert.assertEquals("the component ids are not equal", componentId, component.getId());
		Assert.assertNotNull("the component name shouldn't be null", component.getName());
		Assert.assertEquals("the component names are not equal", componentName, component.getName());
		Assert.assertNotNull("the component parameter mappings shouldn't be null", component.getParameterMappings());
		Assert.assertEquals("the component parameter mappings' size are not equal", 1, component.getParameterMappings().size());
		Assert.assertTrue("the component parameter mappings doesn't contain a mapping for function parameter '" + functionParameterName + "'",
				component.getParameterMappings().containsKey(functionParameterName));
		Assert.assertEquals("the component parameter mapping for '" + functionParameterName + "' are not equal", componentVariableName, component
				.getParameterMappings().get(functionParameterName));

		String json = null;

		try {

			json = objectMapper.writeValueAsString(component);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ComponentTest.LOG.debug("component json: " + json);
	}

	@Test
	public void complexComponentTest() {

		// previous component

		// final String function1Id = UUID.randomUUID().toString();
		final String function1Name = "replace";
		final String function1Description = "replace certain parts of a given string that matches a certain regex";
		final String function1Parameter = "inputString";
		final String function2Parameter = "regex";
		final String function3Parameter = "replaceString";

		final String uuid = UUIDService.getUUID(Function.class.getSimpleName());

		final Function function1 = new Function(uuid);
		// function1.setId(function1Id);
		function1.setName(function1Name);
		function1.setDescription(function1Description);
		function1.addParameter(function1Parameter);
		function1.addParameter(function2Parameter);
		function1.addParameter(function3Parameter);

		// final String component1Id = UUID.randomUUID().toString();
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

		final String componentUUID = UUIDService.getUUID(Component.class.getSimpleName());

		final Component component1 = new Component(componentUUID);
		// component1.setId(component1Id);
		component1.setName(component1Name);
		component1.setFunction(function1);
		component1.setParameterMappings(parameterMapping1);

		// next component

		// final String function2Id = UUID.randomUUID().toString();
		final String function2Name = "lower_case";
		final String function2Description = "lower cases all characters of a given string";
		final String function4Parameter = "inputString";

		final String function2UUID = UUIDService.getUUID(Function.class.getSimpleName());

		final Function function2 = new Function(function2UUID);
		// function2.setId(function2Id);
		function2.setName(function2Name);
		function2.setDescription(function2Description);
		function2.addParameter(function4Parameter);

		// final String component2Id = UUID.randomUUID().toString();
		final String component2Name = "my lower case component";
		final Map<String, String> parameterMapping2 = Maps.newLinkedHashMap();

		final String functionParameterName4 = "inputString";
		final String componentVariableName4 = "previousComponent.outputString";

		parameterMapping1.put(functionParameterName4, componentVariableName4);

		final String component2UUID = UUIDService.getUUID(Component.class.getSimpleName());

		final Component component2 = new Component(component2UUID);
		// component2.setId(component2Id);
		component2.setName(component2Name);
		component2.setFunction(function2);
		component2.setParameterMappings(parameterMapping2);

		// main component

		// final String functionId = UUID.randomUUID().toString();
		final String functionName = "trim";
		final String functionDescription = "trims leading and trailing whitespaces from a given string";
		final String functionParameter = "inputString";

		final String functionUUID = UUIDService.getUUID(Function.class.getSimpleName());

		final Function function = new Function(functionUUID);
		// function.setId(functionId);
		function.setName(functionName);
		function.setDescription(functionDescription);
		function.addParameter(functionParameter);

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

		final String component3UUID = UUIDService.getUUID(Component.class.getSimpleName());

		final Component component = new Component(component3UUID);
		// component.setId(componentId);
		component.setName(componentName);
		component.setFunction(function);
		component.setParameterMappings(parameterMapping);
		component.setInputComponents(inputComponents);
		component.setOutputComponents(outputComponents);

		// Assert.assertNotNull("the component id shouldn't be null", component.getId());
		// Assert.assertEquals("the component ids are not equal", componentId, component.getId());
		Assert.assertNotNull("the component name shouldn't be null", component.getName());
		Assert.assertEquals("the component names are not equal", componentName, component.getName());
		Assert.assertNotNull("the component parameter mappings shouldn't be null", component.getParameterMappings());
		Assert.assertEquals("the component parameter mappings' size are not equal", 1, component.getParameterMappings().size());
		Assert.assertTrue("the component parameter mappings doesn't contain a mapping for function parameter '" + functionParameterName + "'",
				component.getParameterMappings().containsKey(functionParameterName));
		Assert.assertEquals("the component parameter mapping for '" + functionParameterName + "' are not equal", componentVariableName, component
				.getParameterMappings().get(functionParameterName));
		Assert.assertNotNull("the component input components set shouldn't be null", component.getInputComponents());
		Assert.assertEquals("the component input components set are not equal", 1, component.getInputComponents().size());
		Assert.assertTrue("the component input components set doesn't contain component '" + component1.getUuid() + "'",
				component.getInputComponents()
						.contains(component1));
		Assert.assertEquals("the component input component '" + component1.getUuid() + "' are not equal", component1, component.getInputComponents()
				.iterator().next());
		Assert.assertNotNull("the component output components set shouldn't be null", component.getOutputComponents());
		Assert.assertEquals("the component output components set are not equal", 1, component.getOutputComponents().size());
		Assert.assertTrue("the component output components set doesn't contain component '" + component2.getUuid() + "'", component
				.getOutputComponents().contains(component2));
		Assert.assertEquals("the component output component '" + component2.getUuid() + "' are not equal", component2, component.getOutputComponents()
				.iterator().next());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(component);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ComponentTest.LOG.debug("component json: " + json);
	}

}
