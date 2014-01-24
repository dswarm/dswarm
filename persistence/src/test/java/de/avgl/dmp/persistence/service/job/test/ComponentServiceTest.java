package de.avgl.dmp.persistence.service.job.test;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.FunctionType;
import de.avgl.dmp.persistence.model.job.proxy.ProxyComponent;
import de.avgl.dmp.persistence.service.job.ComponentService;
import de.avgl.dmp.persistence.service.job.FunctionService;
import de.avgl.dmp.persistence.service.test.IDBasicJPAServiceTest;

public class ComponentServiceTest extends IDBasicJPAServiceTest<ProxyComponent, Component, ComponentService> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(ComponentServiceTest.class);

	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private final Map<Long, Function>				functions		= Maps.newLinkedHashMap();

	public ComponentServiceTest() {

		super("component", ComponentService.class);
	}

	@Test
	public void testSimpleComponent() {

		final LinkedList<String> parameters = Lists.newLinkedList();

		parameters.add("inputString");

		final Function function = createFunction("trim", "trims leading and trailing whitespaces from a given string", parameters);

		final String componentName = "my trim component";
		final Map<String, String> parameterMapping = Maps.newLinkedHashMap();

		final String functionParameterName = "inputString";
		final String componentVariableName = "previousComponent.outputString";

		parameterMapping.put(functionParameterName, componentVariableName);

		final Component component = createObject();
		component.setName(componentName);
		component.setFunction(function);
		component.setParameterMappings(parameterMapping);

		final Component updatedComponent = updateObjectTransactional(component);

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
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("component json: " + json);

		// clean up DB
		deletedObject(component.getId());
		deleteFunction(function);
	}

	@Test
	public void complexComponentTest() {

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

		final Function function1 = createFunction(function1Name, function1Description, function1Parameters);

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

		final Function function2 = createFunction(function2Name, function2Description, function2Parameters);

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

		final Function function = createFunction(functionName, functionDescription, functionParameters);

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

		final Component component = createObject();
		component.setName(componentName);
		component.setFunction(function);
		component.setParameterMappings(parameterMapping);
		component.setInputComponents(inputComponents);
		component.setOutputComponents(outputComponents);

		final Component updatedComponent = updateObjectTransactional(component);

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
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("component json: " + json);

		// clean-up
		deletedObject(updatedComponent.getId());
		deletedObject(component1.getId());
		deletedObject(component2.getId());

		for (final Function functionToDelete : functions.values()) {

			deleteFunction(functionToDelete);
		}
	}

	private Function createFunction(final String name, final String description, final LinkedList<String> parameters) {

		final FunctionService functionService = GuicedTest.injector.getInstance(FunctionService.class);

		Assert.assertNotNull("function service shouldn't be null", functionService);

		final String functionName = name;
		final String functionDescription = description;

		Function function = null;

		try {

			function = functionService.createObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while function creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("function shouldn't be null", function);
		Assert.assertNotNull("function id shouldn't be null", function.getId());

		// function.setId(functionId);
		function.setName(functionName);
		function.setDescription(functionDescription);
		function.setParameters(parameters);

		Function updatedFunction = null;

		try {

			updatedFunction = functionService.updateObjectTransactional(function);
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the function of id = '" + function.getId() + "'", false);
		}

		Assert.assertNotNull("function shouldn't be null", updatedFunction);
		Assert.assertNotNull("the function name shouldn't be null", function.getName());
		Assert.assertEquals("the function names are not equal", functionName, function.getName());
		Assert.assertNotNull("the function description shouldn't be null", function.getDescription());
		Assert.assertEquals("the function descriptions are not equal", functionDescription, function.getDescription());
		Assert.assertNotNull("the function parameters shouldn't be null", function.getParameters());
		Assert.assertEquals("the function type is not '" + FunctionType.Function + "'", FunctionType.Function, function.getFunctionType());

		functions.put(updatedFunction.getId(), updatedFunction);

		return updatedFunction;
	}

	private Component createComponent(final String name, final Map<String, String> parameterMappings, final Function function) {

		Component component = null;

		try {

			component = jpaService.createObject();
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

			updatedComponent = jpaService.updateObjectTransactional(component);
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

	private void deleteFunction(final Function function) {

		final FunctionService functionService = GuicedTest.injector.getInstance(FunctionService.class);

		Assert.assertNotNull("function service shouldn't be null", functionService);

		final Long functionId = function.getId();

		functionService.deleteObject(functionId);

		final Function deletedFunction = functionService.getObject(functionId);

		Assert.assertNull("deleted function shouldn't exist any more", deletedFunction);
	}
}
