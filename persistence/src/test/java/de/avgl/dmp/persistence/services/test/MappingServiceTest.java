package de.avgl.dmp.persistence.services.test;

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
import de.avgl.dmp.persistence.model.job.Attribute;
import de.avgl.dmp.persistence.model.job.AttributePath;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.test.IDBasicJPAServiceTest;
import de.avgl.dmp.persistence.services.AttributePathService;
import de.avgl.dmp.persistence.services.AttributeService;
import de.avgl.dmp.persistence.services.ComponentService;
import de.avgl.dmp.persistence.services.FunctionService;
import de.avgl.dmp.persistence.services.MappingService;
import de.avgl.dmp.persistence.services.TransformationService;

public class MappingServiceTest extends IDBasicJPAServiceTest<Mapping, MappingService, Long> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(MappingServiceTest.class);

	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private final Map<Long, Function>				functions		= Maps.newLinkedHashMap();

	private Map<String, Attribute>					attributes		= Maps.newLinkedHashMap();

	public MappingServiceTest() {

		super("mapping", MappingService.class);
	}

	@Test
	public void simpleMappingTest() {

		LOG.debug("start simple mapping test");

		final LinkedList<String> parameters = Lists.newLinkedList();

		parameters.add("inputString");

		final Function function = createFunction("trim", "trims leading and trailing whitespaces from a given string", parameters);

		final String componentName = "my trim component";
		final Map<String, String> parameterMappings = Maps.newLinkedHashMap();

		final String functionParameterName = "inputString";
		final String componentVariableName = "previousComponent.outputString";

		parameterMappings.put(functionParameterName, componentVariableName);

		final Component component = createComponent(componentName, parameterMappings, function, null, null);

		// transformation

		final String transformationName = "my transformation";
		final String transformationDescription = "transformation which just makes use of one function";
		final String transformationParameter = "transformationInputString";

		final LinkedList<String> transformationParameters = Lists.newLinkedList();
		transformationParameters.add(transformationParameter);

		final Set<Component> components = Sets.newLinkedHashSet();

		components.add(component);

		final Transformation transformation = createTransformation(transformationName, transformationDescription, components,
				transformationParameters);

		// attribute paths

		// input attribute path

		final String dctermsTitleId = "http://purl.org/dc/terms/title";
		final String dctermsTitleName = "title";

		final Attribute dctermsTitle = createAttribute(dctermsTitleId, dctermsTitleName);

		final LinkedList<Attribute> dctermsTitleAttributePath = Lists.newLinkedList();
		dctermsTitleAttributePath.add(dctermsTitle);

		final AttributePath inputAttributePath = createAttributePath(dctermsTitleAttributePath);

		// output attribute path

		final String rdfsLabelId = "http://www.w3.org/2000/01/rdf-schema#label";
		final String rdfsLabelName = "label";

		final Attribute rdfsLabel = createAttribute(rdfsLabelId, rdfsLabelName);

		final LinkedList<Attribute> rdfsLabelAttributePath = Lists.newLinkedList();
		rdfsLabelAttributePath.add(rdfsLabel);

		final AttributePath outputAttributePath = createAttributePath(rdfsLabelAttributePath);

		// transformation component

		final Map<String, String> transformationComponentParameterMappings = Maps.newLinkedHashMap();

		transformationComponentParameterMappings.put(transformation.getParameters().get(0), inputAttributePath.toAttributePath());
		transformationComponentParameterMappings.put("transformationOutputVariable", outputAttributePath.toAttributePath());

		final Component transformationComponent = createComponent(transformation.getName() + " (component)",
				transformationComponentParameterMappings, transformation, null, null);

		// mapping

		final String mappingName = "my mapping";

		final Mapping mapping = createObject();
		mapping.setName(mappingName);
		mapping.addInputAttributePath(inputAttributePath);
		mapping.setOutputAttributePath(outputAttributePath);
		mapping.setTransformation(transformationComponent);

		final Mapping updatedMapping = updateObjectTransactional(mapping);

		Assert.assertNotNull("the mapping id shouldn't be null", updatedMapping.getId());
		Assert.assertNotNull("the mapping name shouldn't be null", updatedMapping.getName());
		Assert.assertEquals("the mapping names are not equal", mappingName, updatedMapping.getName());
		Assert.assertNotNull("the transformation component id shouldn't be null", updatedMapping.getTransformation().getId());
		Assert.assertEquals("the transformation component ids are not equal", transformationComponent.getId(), updatedMapping.getTransformation()
				.getId());
		Assert.assertNotNull("the transformation component parameter mappings shouldn't be null", updatedMapping.getTransformation()
				.getParameterMappings());
		Assert.assertEquals("the transformation component parameter mappings' size are not equal", 2, updatedMapping.getTransformation()
				.getParameterMappings().size());
		Assert.assertTrue("the transformation component parameter mappings doesn't contain a mapping for function parameter '"
				+ transformation.getParameters().get(0) + "'",
				updatedMapping.getTransformation().getParameterMappings().containsKey(transformation.getParameters().get(0)));
		Assert.assertEquals("the transformation component parameter mapping for '" + transformation.getParameters().get(0) + "' are not equal",
				inputAttributePath.toAttributePath(),
				updatedMapping.getTransformation().getParameterMappings().get(transformation.getParameters().get(0)));
		Assert.assertNotNull("the transformation shouldn't be null", updatedMapping.getTransformation().getFunction());
		Assert.assertNotNull("the transformation id shouldn't be null", updatedMapping.getTransformation().getFunction().getId());
		Assert.assertEquals("the transformation ids are not equal", transformation.getId(), updatedMapping.getTransformation().getFunction().getId());
		Assert.assertNotNull("the transformation name shouldn't be null", updatedMapping.getTransformation().getFunction().getName());
		Assert.assertEquals("the transformation names are not equal", transformationName, updatedMapping.getTransformation().getFunction().getName());
		Assert.assertNotNull("the transformation description shouldn't be null", updatedMapping.getTransformation().getFunction().getDescription());
		Assert.assertEquals("the transformation descriptions are not equal", transformationDescription, updatedMapping.getTransformation()
				.getFunction().getDescription());
		Assert.assertEquals("the transformation parameters' size are not equal", 1, updatedMapping.getTransformation().getFunction().getParameters()
				.size());
		Assert.assertTrue("the transformation parameters doesn't contain transformation parameter '" + transformationParameter + "'", updatedMapping
				.getTransformation().getFunction().getParameters().contains(transformationParameter));
		Assert.assertEquals("the transformation parameter for '" + transformationParameter + "' are not equal", transformationParameter,
				updatedMapping.getTransformation().getFunction().getParameters().iterator().next());
		Assert.assertNotNull("the transformation components set shouldn't be null", ((Transformation) updatedMapping.getTransformation()
				.getFunction()).getComponents());
		Assert.assertEquals("the transformation component sets are not equal", components, ((Transformation) updatedMapping.getTransformation()
				.getFunction()).getComponents());
		Assert.assertNotNull("the component id shouldn't be null", ((Transformation) updatedMapping.getTransformation().getFunction())
				.getComponents().iterator().next().getId());
		Assert.assertEquals("the component ids are not equal", component.getId(), ((Transformation) updatedMapping.getTransformation().getFunction())
				.getComponents().iterator().next().getId());
		Assert.assertNotNull("the component name shouldn't be null", ((Transformation) updatedMapping.getTransformation().getFunction())
				.getComponents().iterator().next().getName());
		Assert.assertEquals("the component names are not equal", componentName, ((Transformation) updatedMapping.getTransformation().getFunction())
				.getComponents().iterator().next().getName());
		Assert.assertNotNull("the component parameter mappings shouldn't be null",
				((Transformation) updatedMapping.getTransformation().getFunction()).getComponents().iterator().next().getParameterMappings());
		Assert.assertEquals("the component parameter mappings' size are not equal", 1, ((Transformation) updatedMapping.getTransformation()
				.getFunction()).getComponents().iterator().next().getParameterMappings().size());
		Assert.assertTrue("the component parameter mappings doesn't contain a mapping for function parameter '" + functionParameterName + "'",
				((Transformation) updatedMapping.getTransformation().getFunction()).getComponents().iterator().next().getParameterMappings()
						.containsKey(functionParameterName));
		Assert.assertEquals(
				"the component parameter mapping for '" + functionParameterName + "' are not equal",
				componentVariableName,
				((Transformation) updatedMapping.getTransformation().getFunction()).getComponents().iterator().next().getParameterMappings()
						.get(functionParameterName));

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedMapping);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("mapping json: " + json);

		deletedObject(updatedMapping.getId());
		deleteTransformation(transformation);
		checkDeletedComponent(component);
		deleteFunction(function);

		LOG.debug("end simple mappping test");
	}

	// @Test
	// public void complexTransformationTest() {
	//
	// LOG.debug("start complex transformation test");
	//
	// // previous component
	//
	// final String function1Name = "replace";
	// final String function1Description = "replace certain parts of a given string that matches a certain regex";
	// final String function1Parameter = "inputString";
	// final String function2Parameter = "regex";
	// final String function3Parameter = "replaceString";
	//
	// final LinkedList<String> function1Parameters = Lists.newLinkedList();
	// function1Parameters.add(function1Parameter);
	// function1Parameters.add(function2Parameter);
	// function1Parameters.add(function3Parameter);
	//
	// final Function function1 = createFunction(function1Name, function1Description, function1Parameters);
	//
	// final String component1Name = "my replace component";
	// final Map<String, String> parameterMapping1 = Maps.newLinkedHashMap();
	//
	// final String functionParameterName1 = "inputString";
	// final String componentVariableName1 = "previousComponent.outputString";
	// final String functionParameterName2 = "regex";
	// final String componentVariableName2 = "\\.";
	// final String functionParameterName3 = "replaceString";
	// final String componentVariableName3 = ":";
	//
	// parameterMapping1.put(functionParameterName1, componentVariableName1);
	// parameterMapping1.put(functionParameterName2, componentVariableName2);
	// parameterMapping1.put(functionParameterName3, componentVariableName3);
	//
	// final Component component1 = createComponent(component1Name, parameterMapping1, function1, null, null);
	//
	// // next component
	//
	// final String function2Name = "lower_case";
	// final String function2Description = "lower cases all characters of a given string";
	// final String function4Parameter = "inputString";
	//
	// final LinkedList<String> function2Parameters = Lists.newLinkedList();
	// function2Parameters.add(function4Parameter);
	//
	// final Function function2 = createFunction(function2Name, function2Description, function2Parameters);
	//
	// final String component2Name = "my lower case component";
	// final Map<String, String> parameterMapping2 = Maps.newLinkedHashMap();
	//
	// final String functionParameterName4 = "inputString";
	// final String componentVariableName4 = "previousComponent.outputString";
	//
	// parameterMapping2.put(functionParameterName4, componentVariableName4);
	//
	// final Component component2 = createComponent(component2Name, parameterMapping2, function2, null, null);
	//
	// // main component
	//
	// final String functionName = "trim";
	// final String functionDescription = "trims leading and trailing whitespaces from a given string";
	// final String functionParameter = "inputString";
	//
	// final LinkedList<String> functionParameters = Lists.newLinkedList();
	// functionParameters.add(functionParameter);
	//
	// final Function function = createFunction(functionName, functionDescription, functionParameters);
	//
	// // final String componentId = UUID.randomUUID().toString();
	// final String componentName = "my trim component";
	// final Map<String, String> parameterMapping = Maps.newLinkedHashMap();
	//
	// final String functionParameterName = "inputString";
	// final String componentVariableName = "previousComponent.outputString";
	//
	// parameterMapping.put(functionParameterName, componentVariableName);
	//
	// final Set<Component> inputComponents = Sets.newLinkedHashSet();
	//
	// inputComponents.add(component1);
	//
	// final Set<Component> outputComponents = Sets.newLinkedHashSet();
	//
	// outputComponents.add(component2);
	//
	// final Component component = createComponent(componentName, parameterMapping, function, inputComponents, outputComponents);
	//
	// // transformation
	//
	// final String transformationName = "my transformation";
	// final String transformationDescription = "transformation which just makes use of one function";
	// final String transformationParameter = "transformationInputString";
	//
	// final Set<Component> components = Sets.newLinkedHashSet();
	//
	// components.add(component1);
	// components.add(component);
	// components.add(component2);
	//
	// final Transformation transformation = createObject();
	// transformation.setName(transformationName);
	// transformation.setDescription(transformationDescription);
	// transformation.setComponents(components);
	// transformation.addParameter(transformationParameter);
	//
	// Assert.assertNotNull("the transformation components set shouldn't be null", transformation.getComponents());
	// Assert.assertEquals("the transformation components sizes are not equal", 3, transformation.getComponents().size());
	//
	//
	// final Transformation updatedTransformation = updateObjectTransactional(transformation);
	//
	// Assert.assertNotNull("the transformation id shouldn't be null", updatedTransformation.getId());
	// Assert.assertNotNull("the transformation name shouldn't be null", updatedTransformation.getName());
	// Assert.assertEquals("the transformation names are not equal", transformationName, updatedTransformation.getName());
	// Assert.assertNotNull("the transformation description shouldn't be null", updatedTransformation.getDescription());
	// Assert.assertEquals("the transformation descriptions are not equal", transformationDescription,
	// updatedTransformation.getDescription());
	// Assert.assertEquals("the transformation parameters' size are not equal", 1, updatedTransformation.getParameters().size());
	// Assert.assertTrue("the transformation parameters doesn't contain transformation parameter '" + transformationParameter +
	// "'",
	// updatedTransformation.getParameters().contains(transformationParameter));
	// Assert.assertEquals("the transformation parameter for '" + transformationParameter + "' are not equal",
	// transformationParameter,
	// updatedTransformation.getParameters().iterator().next());
	// Assert.assertNotNull("the transformation components set shouldn't be null", updatedTransformation.getComponents());
	// Assert.assertEquals("the transformation components sizes are not equal", 3, updatedTransformation.getComponents().size());
	//
	// final Iterator<Component> iter = updatedTransformation.getComponents().iterator();
	//
	// Component mainComponent = null;
	//
	// while(iter.hasNext()) {
	//
	// mainComponent = iter.next();
	//
	// if(componentName.equals(mainComponent.getName())) {
	//
	// break;
	// }
	// }
	//
	// Assert.assertNotNull("the main component shouldn't be null", mainComponent);
	// Assert.assertNotNull("the component id shouldn't be null", mainComponent.getId());
	// Assert.assertNotNull("the component name shouldn't be null", mainComponent.getName());
	// Assert.assertEquals("the component names are not equal", componentName, mainComponent.getName());
	// Assert.assertNotNull("the component parameter mappings shouldn't be null", mainComponent.getParameterMappings());
	// Assert.assertEquals("the component parameter mappings' size are not equal", 1,
	// mainComponent.getParameterMappings().size());
	// Assert.assertTrue("the component parameter mappings doesn't contain a mapping for function parameter '" +
	// functionParameterName + "'",
	// mainComponent.getParameterMappings().containsKey(functionParameterName));
	// Assert.assertEquals("the component parameter mapping for '" + functionParameterName + "' are not equal",
	// componentVariableName, mainComponent
	// .getParameterMappings().get(functionParameterName));
	// Assert.assertNotNull("the component input components set shouldn't be null", mainComponent.getInputComponents());
	// Assert.assertEquals("the component input components set are not equal", 1, mainComponent.getInputComponents().size());
	// Assert.assertTrue("the component input components set doesn't contain component '" + component1.getId() + "'",
	// mainComponent
	// .getInputComponents().contains(component1));
	// Assert.assertEquals("the component input component '" + component1.getId() + "' are not equal", component1, mainComponent
	// .getInputComponents().iterator().next());
	// Assert.assertNotNull("the component output components set shouldn't be null", mainComponent.getOutputComponents());
	// Assert.assertEquals("the component output components set are not equal", 1, mainComponent.getOutputComponents().size());
	// Assert.assertTrue("the component output components set doesn't contain component '" + component2.getId() + "'",
	// mainComponent
	// .getOutputComponents().contains(component2));
	// Assert.assertEquals("the component output component '" + component2.getId() + "' are not equal", component2, mainComponent
	// .getOutputComponents().iterator().next());
	//
	// String json = null;
	//
	// try {
	//
	// json = objectMapper.writeValueAsString(updatedTransformation);
	// } catch (JsonProcessingException e) {
	//
	// e.printStackTrace();
	// }
	//
	// LOG.debug("transformation json: " + json);
	//
	// try {
	//
	// json = objectMapper.writeValueAsString(component1);
	// } catch (JsonProcessingException e) {
	//
	// e.printStackTrace();
	// }
	//
	// LOG.debug("previous component json: " + json);
	//
	// try {
	//
	// json = objectMapper.writeValueAsString(component);
	// } catch (JsonProcessingException e) {
	//
	// e.printStackTrace();
	// }
	//
	// LOG.debug("main component json: " + json);
	//
	// try {
	//
	// json = objectMapper.writeValueAsString(component2);
	// } catch (JsonProcessingException e) {
	//
	// e.printStackTrace();
	// }
	//
	// LOG.debug("next component json: " + json);
	//
	// // clean-up
	// deletedObject(updatedTransformation.getId());
	//
	// for (final Function functionToDelete : functions.values()) {
	//
	// deleteFunction(functionToDelete);
	// }
	//
	// LOG.debug("end complex transformation test");
	// }

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

		functions.put(updatedFunction.getId(), updatedFunction);

		return updatedFunction;
	}

	private Component createComponent(final String name, final Map<String, String> parameterMappings, final Function function,
			final Set<Component> inputComponents, final Set<Component> outputComponents) {

		final ComponentService componentService = GuicedTest.injector.getInstance(ComponentService.class);

		Component component = null;

		try {

			component = componentService.createObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while component creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("component shouldn't be null", component);
		Assert.assertNotNull("component id shouldn't be null", component.getId());

		component.setName(name);
		component.setFunction(function);
		component.setParameterMapping(parameterMappings);

		if (inputComponents != null) {
			component.setInputComponents(inputComponents);
		}

		if (outputComponents != null) {
			component.setOutputComponents(outputComponents);
		}

		Component updatedComponent = null;

		try {

			updatedComponent = componentService.updateObjectTransactional(component);
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the component of id = '" + component.getId() + "'", false);
		}

		Assert.assertNotNull("the updated component shouldn't be null", updatedComponent);
		Assert.assertNotNull("the component name shouldn't be null", updatedComponent.getId());
		Assert.assertNotNull("the component name shouldn't be null", updatedComponent.getName());
		Assert.assertEquals("the component names are not equal", name, updatedComponent.getName());
		Assert.assertNotNull("the component parameter mappings shouldn't be null", updatedComponent.getParameterMappings());

		return updatedComponent;
	}

	private Transformation createTransformation(final String name, final String description, final Set<Component> components,
			final LinkedList<String> parameters) {

		final TransformationService transformationService = GuicedTest.injector.getInstance(TransformationService.class);

		Transformation transformation = null;

		try {

			transformation = transformationService.createObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while transformation creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("transformation shouldn't be null", transformation);
		Assert.assertNotNull("transformation id shouldn't be null", transformation.getId());

		transformation.setName(name);
		transformation.setDescription(description);
		transformation.setComponents(components);
		transformation.setParameters(parameters);

		Transformation updatedTransformation = null;

		try {

			updatedTransformation = transformationService.updateObjectTransactional(transformation);
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the transformation of id = '" + transformation.getId() + "'", false);
		}

		Assert.assertNotNull("the updated component shouldn't be null", updatedTransformation);
		Assert.assertNotNull("the transformation name shouldn't be null", updatedTransformation.getId());
		Assert.assertNotNull("the transformation name shouldn't be null", updatedTransformation.getName());
		Assert.assertEquals("the transformation names are not equal", name, updatedTransformation.getName());
		Assert.assertNotNull("the transformation parameter mappings shouldn't be null", updatedTransformation.getParameters());

		return updatedTransformation;
	}

	private void deleteFunction(final Function function) {

		final FunctionService functionService = GuicedTest.injector.getInstance(FunctionService.class);

		Assert.assertNotNull("function service shouldn't be null", functionService);

		final Long functionId = function.getId();

		functionService.deleteObject(functionId);

		final Function deletedFunction = functionService.getObject(functionId);

		Assert.assertNull("deleted function shouldn't exist any more", deletedFunction);
	}

	private void deleteTransformation(final Transformation transformation) {

		final TransformationService transformationService = GuicedTest.injector.getInstance(TransformationService.class);

		Assert.assertNotNull("transformation service shouldn't be null", transformationService);

		final Long transformationId = transformation.getId();

		transformationService.deleteObject(transformationId);

		final Transformation deletedTransformation = transformationService.getObject(transformationId);

		Assert.assertNull("deleted transformation shouldn't exist any more", deletedTransformation);
	}

	private void checkDeletedComponent(final Component component) {

		final ComponentService componentService = GuicedTest.injector.getInstance(ComponentService.class);

		Component deletedComponent = null;

		deletedComponent = componentService.getObject(component.getId());

		Assert.assertNull("component should be null", deletedComponent);

	}

	private AttributePath createAttributePath(final LinkedList<Attribute> attributePathArg) {

		final AttributePathService attributePathService = GuicedTest.injector.getInstance(AttributePathService.class);

		Assert.assertNotNull("attribute path service shouldn't be null", attributePathService);

		AttributePath attributePath = null;

		try {

			attributePath = attributePathService.createObject();
		} catch (final DMPPersistenceException e1) {

			Assert.assertTrue("something went wrong while attribute path creation.\n" + e1.getMessage(), false);
		}

		Assert.assertNotNull("attribute path shouldn't be null", attributePath);
		Assert.assertNotNull("attribute path id shouldn't be null", attributePath.getId());

		attributePath.setAttributePath(attributePathArg);

		System.out.println("attribute path = '" + attributePath.toString());

		AttributePath updatedAttributePath = null;
		try {
			updatedAttributePath = attributePathService.updateObjectTransactional(attributePath);
		} catch (final DMPPersistenceException e1) {

			Assert.assertTrue("something went wrong while updating the attribute path of id = '" + attributePath.getId() + "'", false);
		}

		Assert.assertNotNull("updated attribute path shouldn't be null", updatedAttributePath);
		Assert.assertNotNull("updated attribute path id shouldn't be null", updatedAttributePath.getId());
		Assert.assertNotNull("the attribute path's attribute of the updated attribute path shouldn't be null", updatedAttributePath.getAttributes());
		Assert.assertEquals("the attribute path's attributes size are not equal", attributePath.getAttributes(), updatedAttributePath.getAttributes());
		Assert.assertEquals("the first attributes of the attribute path are not equal", attributePath.getAttributePath().get(0), updatedAttributePath
				.getAttributePath().get(0));
		Assert.assertNotNull("the attribute path string of the updated attribute path shouldn't be null", updatedAttributePath.toAttributePath());
		Assert.assertEquals("the attribute path's strings are not equal", attributePath.toAttributePath(), updatedAttributePath.toAttributePath());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedAttributePath);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("attribute path json for attribute path '" + attributePath.getId() + "': " + json);

		return updatedAttributePath;
	}

	private Attribute createAttribute(final String id, final String name) {

		if (attributes.containsKey(id)) {

			return attributes.get(id);
		}

		final AttributeService attributeService = GuicedTest.injector.getInstance(AttributeService.class);

		Assert.assertNotNull("attribute service shouldn't be null", attributeService);

		// create first attribute

		Attribute attribute = null;

		try {
			attribute = attributeService.createObject(id);
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while attribute creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("attribute shouldn't be null", attribute);
		Assert.assertNotNull("attribute id shouldn't be null", attribute.getId());

		attribute.setName(name);

		Attribute updatedAttribute = null;

		try {

			updatedAttribute = attributeService.updateObjectTransactional(attribute);
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the attribute of id = '" + id + "'", false);
		}

		Assert.assertNotNull("updated attribute shouldn't be null", updatedAttribute);
		Assert.assertNotNull("updated attribute id shouldn't be null", updatedAttribute.getId());
		Assert.assertNotNull("updated attribute name shouldn't be null", updatedAttribute.getName());

		attributes.put(updatedAttribute.getId(), updatedAttribute);

		return updatedAttribute;
	}
}
