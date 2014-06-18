package de.avgl.dmp.persistence.service.job.test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.FunctionType;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.job.proxy.ProxyTransformation;
import de.avgl.dmp.persistence.service.job.TransformationService;
import de.avgl.dmp.persistence.service.job.test.utils.ComponentServiceTestUtils;
import de.avgl.dmp.persistence.service.job.test.utils.FunctionServiceTestUtils;
import de.avgl.dmp.persistence.service.test.IDBasicJPAServiceTest;

public class TransformationServiceTest extends IDBasicJPAServiceTest<ProxyTransformation, Transformation, TransformationService> {

	private static final Logger				LOG				= LoggerFactory.getLogger(TransformationServiceTest.class);

	private final ObjectMapper				objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private final Map<Long, Function>		functions		= Maps.newLinkedHashMap();

	private final FunctionServiceTestUtils	functionServiceTestUtils;
	private final ComponentServiceTestUtils	componentServiceTestUtils;

	public TransformationServiceTest() {

		super("transformation", TransformationService.class);

		functionServiceTestUtils = new FunctionServiceTestUtils();
		componentServiceTestUtils = new ComponentServiceTestUtils();
	}

	@Test
	public void simpleTransformationTest() throws Exception {

		TransformationServiceTest.LOG.debug("start simple transformation test");

		final LinkedList<String> parameters = Lists.newLinkedList();

		parameters.add("inputString");

		final Function function = functionServiceTestUtils.createFunction("trim", "trims leading and trailing whitespaces from a given string",
				parameters);

		final String componentName = "my trim component";
		final Map<String, String> parameterMappings = Maps.newLinkedHashMap();

		final String functionParameterName = "inputString";
		final String componentVariableName = "previousComponent.outputString";

		parameterMappings.put(functionParameterName, componentVariableName);

		final Component component = componentServiceTestUtils.createComponent(componentName, parameterMappings, function, null, null);

		// transformation

		final String transformationName = "my transformation";
		final String transformationDescription = "transformation which just makes use of one function";
		final String transformationParameter = "transformationInputString";

		final Set<Component> components = Sets.newLinkedHashSet();

		components.add(component);

		final Transformation transformation = createObject().getObject();
		transformation.setName(transformationName);
		transformation.setDescription(transformationDescription);
		transformation.setComponents(components);
		transformation.addParameter(transformationParameter);

		final Transformation updatedTransformation = updateObjectTransactional(transformation).getObject();

		Assert.assertNotNull("the transformation id shouldn't be null", updatedTransformation.getId());
		Assert.assertNotNull("the transformation name shouldn't be null", updatedTransformation.getName());
		Assert.assertEquals("the transformation names are not equal", transformationName, updatedTransformation.getName());
		Assert.assertNotNull("the transformation description shouldn't be null", updatedTransformation.getDescription());
		Assert.assertEquals("the transformation descriptions are not equal", transformationDescription, updatedTransformation.getDescription());
		Assert.assertEquals("the transformation parameters' size are not equal", 1, updatedTransformation.getParameters().size());
		Assert.assertTrue("the transformation parameters doesn't contain transformation parameter '" + transformationParameter + "'",
				updatedTransformation.getParameters().contains(transformationParameter));
		Assert.assertEquals("the transformation parameter for '" + transformationParameter + "' are not equal", transformationParameter,
				updatedTransformation.getParameters().iterator().next());
		Assert.assertNotNull("the transformation components set shouldn't be null", updatedTransformation.getComponents());
		Assert.assertEquals("the transformation component sets are not equal", components, updatedTransformation.getComponents());
		Assert.assertNotNull("the component id shouldn't be null", updatedTransformation.getComponents().iterator().next().getId());
		Assert.assertNotNull("the component name shouldn't be null", updatedTransformation.getComponents().iterator().next().getName());
		Assert.assertEquals("the component names are not equal", componentName, updatedTransformation.getComponents().iterator().next().getName());
		Assert.assertNotNull("the component parameter mappings shouldn't be null", updatedTransformation.getComponents().iterator().next()
				.getParameterMappings());
		Assert.assertEquals("the component parameter mappings' size are not equal", 1, updatedTransformation.getComponents().iterator().next()
				.getParameterMappings().size());
		Assert.assertTrue("the component parameter mappings doesn't contain a mapping for function parameter '" + functionParameterName + "'",
				updatedTransformation.getComponents().iterator().next().getParameterMappings().containsKey(functionParameterName));
		Assert.assertEquals("the component parameter mapping for '" + functionParameterName + "' are not equal", componentVariableName,
				updatedTransformation.getComponents().iterator().next().getParameterMappings().get(functionParameterName));
		Assert.assertEquals("the function type is not '" + FunctionType.Transformation + "'", FunctionType.Transformation,
				updatedTransformation.getFunctionType());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedTransformation);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		TransformationServiceTest.LOG.debug("transformation json: " + json);

		deleteObject(transformation.getId());
		componentServiceTestUtils.checkDeletedComponent(component);
		functionServiceTestUtils.deleteObject(function);

		TransformationServiceTest.LOG.debug("end simple transformation test");
	}

	@Test
	public void complexTransformationTest() throws Exception {

		TransformationServiceTest.LOG.debug("start complex transformation test");

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

		final Component component1 = componentServiceTestUtils.createComponent(component1Name, parameterMapping1, function1, null, null);

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

		final Component component2 = componentServiceTestUtils.createComponent(component2Name, parameterMapping2, function2, null, null);

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

		final Component component = componentServiceTestUtils.createComponent(componentName, parameterMapping, function, inputComponents,
				outputComponents);

		// transformation

		final String transformationName = "my transformation";
		final String transformationDescription = "transformation which just makes use of one function";
		final String transformationParameter = "transformationInputString";

		final Set<Component> components = Sets.newLinkedHashSet();

		components.add(component.getInputComponents().iterator().next());
		components.add(component);
		components.add(component.getOutputComponents().iterator().next());

		final Transformation transformation = createObject().getObject();
		transformation.setName(transformationName);
		transformation.setDescription(transformationDescription);
		transformation.setComponents(components);
		transformation.addParameter(transformationParameter);

		Assert.assertNotNull("the transformation components set shouldn't be null", transformation.getComponents());
		Assert.assertEquals("the transformation components sizes are not equal", 3, transformation.getComponents().size());

		final Transformation updatedTransformation = updateObjectTransactional(transformation).getObject();

		Assert.assertNotNull("the transformation id shouldn't be null", updatedTransformation.getId());
		Assert.assertNotNull("the transformation name shouldn't be null", updatedTransformation.getName());
		Assert.assertEquals("the transformation names are not equal", transformationName, updatedTransformation.getName());
		Assert.assertNotNull("the transformation description shouldn't be null", updatedTransformation.getDescription());
		Assert.assertEquals("the transformation descriptions are not equal", transformationDescription, updatedTransformation.getDescription());
		Assert.assertEquals("the transformation parameters' size are not equal", 1, updatedTransformation.getParameters().size());
		Assert.assertTrue("the transformation parameters doesn't contain transformation parameter '" + transformationParameter + "'",
				updatedTransformation.getParameters().contains(transformationParameter));
		Assert.assertEquals("the transformation parameter for '" + transformationParameter + "' are not equal", transformationParameter,
				updatedTransformation.getParameters().iterator().next());
		Assert.assertNotNull("the transformation components set shouldn't be null", updatedTransformation.getComponents());
		Assert.assertEquals("the transformation components sizes are not equal", 3, updatedTransformation.getComponents().size());
		Assert.assertEquals("the function type is not '" + FunctionType.Transformation + "'", FunctionType.Transformation,
				updatedTransformation.getFunctionType());

		final Iterator<Component> iter = updatedTransformation.getComponents().iterator();

		Component mainComponent = null;

		while (iter.hasNext()) {

			mainComponent = iter.next();

			if (componentName.equals(mainComponent.getName())) {

				break;
			}
		}

		Assert.assertNotNull("the main component shouldn't be null", mainComponent);
		Assert.assertNotNull("the component id shouldn't be null", mainComponent.getId());
		Assert.assertNotNull("the component name shouldn't be null", mainComponent.getName());
		Assert.assertEquals("the component names are not equal", componentName, mainComponent.getName());
		Assert.assertNotNull("the component parameter mappings shouldn't be null", mainComponent.getParameterMappings());
		Assert.assertEquals("the component parameter mappings' size are not equal", 1, mainComponent.getParameterMappings().size());
		Assert.assertTrue("the component parameter mappings doesn't contain a mapping for function parameter '" + functionParameterName + "'",
				mainComponent.getParameterMappings().containsKey(functionParameterName));
		Assert.assertEquals("the component parameter mapping for '" + functionParameterName + "' are not equal", componentVariableName, mainComponent
				.getParameterMappings().get(functionParameterName));
		Assert.assertNotNull("the component input components set shouldn't be null", mainComponent.getInputComponents());
		Assert.assertEquals("the component input components set are not equal", 1, mainComponent.getInputComponents().size());
		Assert.assertTrue("the component input components set doesn't contain component '" + component1.getId() + "'", mainComponent
				.getInputComponents().contains(component1));
		Assert.assertEquals("the component input component '" + component1.getId() + "' are not equal", component1, mainComponent
				.getInputComponents().iterator().next());
		Assert.assertNotNull("the component output components set shouldn't be null", mainComponent.getOutputComponents());
		Assert.assertEquals("the component output components set are not equal", 1, mainComponent.getOutputComponents().size());
		Assert.assertTrue("the component output components set doesn't contain component '" + component2.getId() + "'", mainComponent
				.getOutputComponents().contains(component2));
		Assert.assertEquals("the component output component '" + component2.getId() + "' are not equal", component2, mainComponent
				.getOutputComponents().iterator().next());
		Assert.assertEquals("the function type is not '" + FunctionType.Function + "'", FunctionType.Function, mainComponent.getFunction()
				.getFunctionType());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedTransformation);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		TransformationServiceTest.LOG.debug("transformation json: " + json);

		try {

			json = objectMapper.writeValueAsString(component1);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		TransformationServiceTest.LOG.debug("previous component json: " + json);

		try {

			json = objectMapper.writeValueAsString(component);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		TransformationServiceTest.LOG.debug("main component json: " + json);

		try {

			json = objectMapper.writeValueAsString(component2);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		TransformationServiceTest.LOG.debug("next component json: " + json);

		// clean-up
		deleteObject(updatedTransformation.getId());

		for (final Function functionToDelete : functions.values()) {

			functionServiceTestUtils.deleteObject(functionToDelete);
		}

		TransformationServiceTest.LOG.debug("end complex transformation test");
	}
}
