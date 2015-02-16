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
package org.dswarm.persistence.model.job.test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.service.UUIDService;

public class MappingTest extends GuicedTest {

	private static final Logger LOG = LoggerFactory.getLogger(MappingTest.class);

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	@Test
	public void simpleMappingTest() {

		final String functionName = "trim";
		final String functionDescription = "trims leading and trailing whitespaces from a given string";
		final String functionParameter = "inputString";

		final String functionUUID = UUIDService.getUUID(Function.class.getSimpleName());

		final Function function = new Function(functionUUID);
		// function.setId(functionId);
		function.setName(functionName);
		function.setDescription(functionDescription);
		function.addParameter(functionParameter);

		final String componentId = UUID.randomUUID().toString();
		final String componentName = "my trim component";
		final Map<String, String> parameterMappings = Maps.newLinkedHashMap();

		final String functionParameterName = "inputString";
		final String componentVariableName = "transformationInputString";

		parameterMappings.put(functionParameterName, componentVariableName);

		final Component component = new Component(componentId);
		// component.setId(componentId);
		component.setName(componentName);
		component.setFunction(function);
		component.setParameterMappings(parameterMappings);

		// transformation

		final String transformationId = UUID.randomUUID().toString();
		final String transformationName = "my transformation";
		final String transformationDescription = "transformation which just makes use of one function";
		final String transformationParameter = "transformationInputString";

		final Set<Component> components = Sets.newLinkedHashSet();

		components.add(component);

		final Transformation transformation = new Transformation(transformationId);
		// transformation.setId(transformationId);
		transformation.setName(transformationName);
		transformation.setDescription(transformationDescription);
		transformation.setComponents(components);
		transformation.addParameter(transformationParameter);

		// attribute paths

		// input attribute path

		final String dctermsTitleId = "http://purl.org/dc/terms/title";
		final String dctermsTitleName = "title";

		final Attribute dctermsTitle = createAttribute(dctermsTitleId, dctermsTitleName);

		final String inputAttributePathUUID = UUIDService.getUUID(AttributePath.class.getSimpleName());

		final AttributePath inputAttributePath = new AttributePath(inputAttributePathUUID);
		// inputAttributePath.setId(UUID.randomUUID().toString());

		inputAttributePath.addAttribute(dctermsTitle);

		final String inputMappingAttributePathInstanceUUID = UUIDService.getUUID(MappingAttributePathInstance.class.getSimpleName());

		final MappingAttributePathInstance inputMappingAttributePathInstance = new MappingAttributePathInstance(
				inputMappingAttributePathInstanceUUID);
		inputMappingAttributePathInstance.setAttributePath(inputAttributePath);

		// output attribute path

		final String rdfsLabelId = "http://www.w3.org/2000/01/rdf-schema#label";
		final String rdfsLabelName = "label";

		final Attribute rdfsLabel = createAttribute(rdfsLabelId, rdfsLabelName);

		final String outputAttributePathUUID = UUIDService.getUUID(AttributePath.class.getSimpleName());

		final AttributePath outputAttributePath = new AttributePath(outputAttributePathUUID);
		// outputAttributePath.setId(UUID.randomUUID().toString());

		outputAttributePath.addAttribute(rdfsLabel);

		final String outputMappingAttributePathInstanceUUID = UUIDService.getUUID(MappingAttributePathInstance.class.getSimpleName());

		final MappingAttributePathInstance outputMappingAttributePathInstance = new MappingAttributePathInstance(
				outputMappingAttributePathInstanceUUID);
		outputMappingAttributePathInstance.setAttributePath(outputAttributePath);

		// transformation component

		final Map<String, String> transformationComponentParameterMappings = Maps.newLinkedHashMap();

		transformationComponentParameterMappings.put(transformation.getParameters().get(0), inputAttributePath.toAttributePath());
		transformationComponentParameterMappings.put("transformationOutputVariable", outputAttributePath.toAttributePath());

		final String transformationComponentUUID = UUIDService.getUUID(Component.class.getSimpleName());

		final Component transformationComponent = new Component(transformationComponentUUID);
		// transformationComponent.setId(transformationComponentId);
		transformationComponent.setName(transformation.getName() + " (component)");
		transformationComponent.setFunction(transformation);
		transformationComponent.setParameterMappings(transformationComponentParameterMappings);

		// mapping

		final String mappingName = "my mapping";

		final String mappingUUID = UUIDService.getUUID(Mapping.class.getSimpleName());

		final Mapping mapping = new Mapping(mappingUUID);
		mapping.setName(mappingName);
		mapping.addInputAttributePath(inputMappingAttributePathInstance);
		mapping.setOutputAttributePath(outputMappingAttributePathInstance);
		mapping.setTransformation(transformationComponent);

		Assert.assertNotNull("the mapping name shouldn't be null", mapping.getName());
		Assert.assertEquals("the mapping names are not equal", mappingName, mapping.getName());
		Assert.assertNotNull("the transformation component parameter mappings shouldn't be null", mapping.getTransformation().getParameterMappings());
		Assert.assertEquals("the transformation component parameter mappings' size are not equal", 2, mapping.getTransformation()
				.getParameterMappings().size());
		Assert.assertTrue("the transformation component parameter mappings doesn't contain a mapping for function parameter '"
						+ transformation.getParameters().get(0) + "'",
				mapping.getTransformation().getParameterMappings().containsKey(transformation.getParameters().get(0)));
		Assert.assertEquals("the transformation component parameter mapping for '" + transformation.getParameters().get(0) + "' are not equal",
				inputAttributePath.toAttributePath(), mapping.getTransformation().getParameterMappings().get(transformation.getParameters().get(0)));
		Assert.assertNotNull("the transformation shouldn't be null", mapping.getTransformation().getFunction());
		Assert.assertNotNull("the transformation name shouldn't be null", mapping.getTransformation().getFunction().getName());
		Assert.assertEquals("the transformation names are not equal", transformationName, mapping.getTransformation().getFunction().getName());
		Assert.assertNotNull("the transformation description shouldn't be null", mapping.getTransformation().getFunction().getDescription());
		Assert.assertEquals("the transformation descriptions are not equal", transformationDescription, mapping.getTransformation().getFunction()
				.getDescription());
		Assert.assertEquals("the transformation parameters' size are not equal", 1, mapping.getTransformation().getFunction().getParameters().size());
		Assert.assertTrue("the transformation parameters doesn't contain transformation parameter '" + transformationParameter + "'", mapping
				.getTransformation().getFunction().getParameters().contains(transformationParameter));
		Assert.assertEquals("the transformation parameter for '" + transformationParameter + "' are not equal", transformationParameter, mapping
				.getTransformation().getFunction().getParameters().iterator().next());
		Assert.assertNotNull("the transformation components set shouldn't be null",
				((Transformation) mapping.getTransformation().getFunction()).getComponents());
		Assert.assertEquals("the transformation component sets are not equal", components, ((Transformation) mapping.getTransformation()
				.getFunction()).getComponents());
		Assert.assertNotNull("the component name shouldn't be null", ((Transformation) mapping.getTransformation().getFunction()).getComponents()
				.iterator().next().getName());
		Assert.assertEquals("the component names are not equal", componentName, ((Transformation) mapping.getTransformation().getFunction())
				.getComponents().iterator().next().getName());
		Assert.assertNotNull("the component parameter mappings shouldn't be null", ((Transformation) mapping.getTransformation().getFunction())
				.getComponents().iterator().next().getParameterMappings());
		Assert.assertEquals("the component parameter mappings' size are not equal", 1, ((Transformation) mapping.getTransformation().getFunction())
				.getComponents().iterator().next().getParameterMappings().size());
		Assert.assertTrue(
				"the component parameter mappings doesn't contain a mapping for function parameter '" + functionParameterName + "'",
				((Transformation) mapping.getTransformation().getFunction()).getComponents().iterator().next().getParameterMappings()
						.containsKey(functionParameterName));
		Assert.assertEquals(
				"the component parameter mapping for '" + functionParameterName + "' are not equal",
				componentVariableName,
				((Transformation) mapping.getTransformation().getFunction()).getComponents().iterator().next().getParameterMappings()
						.get(functionParameterName));

		String json = null;

		try {

			json = objectMapper.writeValueAsString(mapping);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		MappingTest.LOG.debug("mapping json: " + json);
	}

	@Test
	public void complexComponentTest() {

		// previous component

		final String function1Name = "replace";
		final String function1Description = "replace certain parts of a given string that matches a certain regex";
		final String function1Parameter = "inputString";
		final String function2Parameter = "regex";
		final String function3Parameter = "replaceString";

		final String function1UUID = UUIDService.getUUID(Function.class.getSimpleName());

		final Function function1 = new Function(function1UUID);
		// function1.setId(function1Id);
		function1.setName(function1Name);
		function1.setDescription(function1Description);
		function1.addParameter(function1Parameter);
		function1.addParameter(function2Parameter);
		function1.addParameter(function3Parameter);

		final String component1Name = "my replace component";
		final Map<String, String> parameterMapping1 = Maps.newLinkedHashMap();

		final String functionParameterName1 = "inputString";
		final String componentVariableName1 = "transformationInputString";
		final String functionParameterName2 = "regex";
		final String componentVariableName2 = "\\.";
		final String functionParameterName3 = "replaceString";
		final String componentVariableName3 = ":";

		parameterMapping1.put(functionParameterName1, componentVariableName1);
		parameterMapping1.put(functionParameterName2, componentVariableName2);
		parameterMapping1.put(functionParameterName3, componentVariableName3);

		final String component1UUID = UUIDService.getUUID(Component.class.getSimpleName());

		final Component component1 = new Component(component1UUID);
		// component1.setId(component1Id);
		component1.setName(component1Name);
		component1.setFunction(function1);
		component1.setParameterMappings(parameterMapping1);

		// next component

		final String function2Name = "lower_case";
		final String function2Description = "lower cases all characters of a given string";
		final String function4Parameter = "inputString";

		final String function2UUID = UUIDService.getUUID(Function.class.getSimpleName());

		final Function function2 = new Function(function2UUID);
		// function2.setId(function2Id);
		function2.setName(function2Name);
		function2.setDescription(function2Description);
		function2.addParameter(function4Parameter);

		final String component2Name = "my lower case component";
		final Map<String, String> parameterMapping2 = Maps.newLinkedHashMap();

		final String functionParameterName4 = "inputString";
		final String componentVariableName4 = "previousComponent.outputString";

		parameterMapping2.put(functionParameterName4, componentVariableName4);

		final String component2UUID = UUIDService.getUUID(Component.class.getSimpleName());

		final Component component2 = new Component(component2UUID);
		// component2.setId(component2Id);
		component2.setName(component2Name);
		component2.setFunction(function2);
		component2.setParameterMappings(parameterMapping2);

		// main component

		final String functionName = "trim";
		final String functionDescription = "trims leading and trailing whitespaces from a given string";
		final String functionParameter = "inputString";

		final String functionUUID = UUIDService.getUUID(Function.class.getSimpleName());

		final Function function = new Function(functionUUID);
		// function.setId(functionId);
		function.setName(functionName);
		function.setDescription(functionDescription);
		function.addParameter(functionParameter);

		final String componentName = "my trim component";
		final Map<String, String> parameterMapping = Maps.newLinkedHashMap();

		final String functionParameterName = "inputString";
		final String componentVariableName = "previousComponent.outputString";

		parameterMapping.put(functionParameterName, componentVariableName);

		final Set<Component> inputComponents = Sets.newLinkedHashSet();

		inputComponents.add(component1);

		final Set<Component> outputComponents = Sets.newLinkedHashSet();

		outputComponents.add(component2);

		final String componentUUID = UUIDService.getUUID(Component.class.getSimpleName());

		final Component component = new Component(componentUUID);
		// component.setId(componentId);
		component.setName(componentName);
		component.setFunction(function);
		component.setParameterMappings(parameterMapping);
		component.setInputComponents(inputComponents);
		component.setOutputComponents(outputComponents);

		// transformation

		final String transformationName = "my clean up transformation";
		final String transformationDescription = "transformation which makes use of three functions";
		final String transformationParameter = "transformationInputString";

		final Set<Component> components = Sets.newLinkedHashSet();

		components.add(component1);
		components.add(component);
		components.add(component2);

		final String transformationUUID = UUIDService.getUUID(Transformation.class.getSimpleName());

		final Transformation transformation = new Transformation(transformationUUID);
		// transformation.setId(transformationId);
		transformation.setName(transformationName);
		transformation.setDescription(transformationDescription);
		transformation.setComponents(components);
		transformation.addParameter(transformationParameter);

		// transformation component 1 (in main transformation) -> clean first name

		final String transformationComponentId = UUID.randomUUID().toString();
		final Map<String, String> transformationComponentParameterMappings = Maps.newLinkedHashMap();

		transformationComponentParameterMappings.put("transformationInputString", "firstName");

		final String transformationComponentUUID = UUIDService.getUUID(Component.class.getSimpleName());

		final Component transformationComponent = new Component(transformationComponentUUID);
		// transformationComponent.setId(transformationComponentId);
		transformationComponent.setName("prepare first name");
		transformationComponent.setFunction(transformation);
		transformationComponent.setParameterMappings(transformationComponentParameterMappings);

		// transformation component 1 (in main transformation) -> clean family name

		final String transformationComponentId2 = UUID.randomUUID().toString();
		final Map<String, String> transformationComponentParameterMappings2 = Maps.newLinkedHashMap();

		transformationComponentParameterMappings2.put("transformationInputString", "familyName");

		final String transformationComponent2UUID = UUIDService.getUUID(Component.class.getSimpleName());

		final Component transformationComponent2 = new Component(transformationComponent2UUID);
		// transformationComponent2.setId(transformationComponentId2);
		transformationComponent2.setName("prepare family name");
		transformationComponent2.setFunction(transformation);
		transformationComponent2.setParameterMappings(transformationComponentParameterMappings2);

		// concat component -> full name

		final String function4Name = "concat";
		final String function4Description = "concatenates two given string";
		final String function5Parameter = "firstString";
		final String function6Parameter = "secondString";

		final String function4UUID = UUIDService.getUUID(Function.class.getSimpleName());

		final Function function4 = new Function(function4UUID);
		// function4.setId(function4Id);
		function4.setName(function4Name);
		function4.setDescription(function4Description);
		function4.addParameter(function5Parameter);
		function4.addParameter(function6Parameter);

		final String component4Name = "full name";
		final Map<String, String> parameterMapping4 = Maps.newLinkedHashMap();

		final String functionParameterName5 = "firstString";
		final String componentVariableName5 = transformationComponentId + ".outputVariable";
		final String functionParameterName6 = "secondString";
		final String componentVariableName6 = transformationComponentId2 + ".outputVariable";

		parameterMapping4.put(functionParameterName5, componentVariableName5);
		parameterMapping4.put(functionParameterName6, componentVariableName6);

		final String component4UUID = UUIDService.getUUID(Component.class.getSimpleName());

		final Component component4 = new Component(component4UUID);
		// component4.setId(component4Id);
		component4.setName(component4Name);
		component4.setFunction(function4);
		component4.setParameterMappings(parameterMapping4);

		final Set<Component> transformationComponentOutputComponents = Sets.newLinkedHashSet();

		transformationComponentOutputComponents.add(component4);

		transformationComponent.setOutputComponents(transformationComponentOutputComponents);

		final Set<Component> transformationComponent2OutputComponents = Sets.newLinkedHashSet();

		transformationComponent2OutputComponents.add(component4);

		transformationComponent2.setOutputComponents(transformationComponent2OutputComponents);

		final Set<Component> component4InputComponents = Sets.newLinkedHashSet();

		component4InputComponents.add(transformationComponent);
		component4InputComponents.add(transformationComponent2);

		component4.setInputComponents(component4InputComponents);

		// TODO: name clean up and concatenate transformation

		// transformation

		final String transformation2Name = "my transformation";
		final String transformation2Description = "transformation which makes use of three functions (two transformations and one funcion)";
		final String transformation2Parameter = "firstName";
		final String transformation2Parameter2 = "familyName";

		final Set<Component> components2 = Sets.newLinkedHashSet();

		components2.add(transformationComponent);
		components2.add(transformationComponent2);
		components2.add(component4);

		final String transformation2UUID = UUIDService.getUUID(Transformation.class.getSimpleName());

		final Transformation transformation2 = new Transformation(transformation2UUID);
		// transformation2.setId(transformation2Id);
		transformation2.setName(transformation2Name);
		transformation2.setDescription(transformation2Description);
		transformation2.setComponents(components2);
		transformation2.addParameter(transformation2Parameter);
		transformation2.addParameter(transformation2Parameter2);

		// attribute paths

		// input attribute paths

		final String dctermsCreatorId = "http://purl.org/dc/terms/creator";
		final String dctermsCreatorName = "creator";

		final Attribute dctermsCreator = createAttribute(dctermsCreatorId, dctermsCreatorName);

		// first name attribute path

		final String firstNameId = "http://xmlns.com/foaf/0.1/firstName";
		final String firstNameName = "firstName";

		final Attribute firstName = createAttribute(firstNameId, firstNameName);

		final String firstNameAttributePathUUID = UUIDService.getUUID(AttributePath.class.getSimpleName());

		final AttributePath firstNameAttributePath = new AttributePath(firstNameAttributePathUUID);
		// firstNameAttributePath.setId(UUID.randomUUID().toString());

		firstNameAttributePath.addAttribute(dctermsCreator);
		firstNameAttributePath.addAttribute(firstName);

		final String firstNameMappingAttributePathInstanceUUID = UUIDService.getUUID(MappingAttributePathInstance.class.getSimpleName());

		final MappingAttributePathInstance firstNameMappingAttributePathInstance = new MappingAttributePathInstance(
				firstNameMappingAttributePathInstanceUUID);
		firstNameMappingAttributePathInstance.setAttributePath(firstNameAttributePath);

		// family name attribute path

		final String familyNameId = "http://xmlns.com/foaf/0.1/familyName";
		final String familyNameName = "familyName";

		final Attribute familyName = createAttribute(familyNameId, familyNameName);

		final String familyNameAttributePathUUID = UUIDService.getUUID(AttributePath.class.getSimpleName());

		final AttributePath familyNameAttributePath = new AttributePath(familyNameAttributePathUUID);
		// familyNameAttributePath.setId(UUID.randomUUID().toString());

		familyNameAttributePath.addAttribute(dctermsCreator);
		familyNameAttributePath.addAttribute(familyName);

		final String familyNameMappingAttributePathInstanceUUID = UUIDService.getUUID(MappingAttributePathInstance.class.getSimpleName());

		final MappingAttributePathInstance familyNameMappingAttributePathInstance = new MappingAttributePathInstance(
				familyNameMappingAttributePathInstanceUUID);
		familyNameMappingAttributePathInstance.setAttributePath(familyNameAttributePath);

		// output attribute path

		final String foafNameId = "http://xmlns.com/foaf/0.1/name";
		final String foafNameName = "name";

		final Attribute foafName = createAttribute(foafNameId, foafNameName);

		final String nameAttributePathUUID = UUIDService.getUUID(AttributePath.class.getSimpleName());

		final AttributePath nameAttributePath = new AttributePath(nameAttributePathUUID);
		// nameAttributePath.setId(UUID.randomUUID().toString());

		nameAttributePath.addAttribute(dctermsCreator);
		nameAttributePath.addAttribute(foafName);

		final String outputMappingAttributePathInstanceUUID = UUIDService.getUUID(MappingAttributePathInstance.class.getSimpleName());

		final MappingAttributePathInstance outputMappingAttributePathInstance = new MappingAttributePathInstance(
				outputMappingAttributePathInstanceUUID);
		outputMappingAttributePathInstance.setAttributePath(nameAttributePath);

		// transformation component

		final Map<String, String> transformationComponent3ParameterMappings = Maps.newLinkedHashMap();

		transformationComponent3ParameterMappings.put(transformation2Parameter, firstNameAttributePath.toAttributePath());
		transformationComponent3ParameterMappings.put(transformation2Parameter2, familyNameAttributePath.toAttributePath());
		transformationComponent3ParameterMappings.put("transformationOutputVariable", nameAttributePath.toAttributePath());

		final String transformationComponent3UUID = UUIDService.getUUID(Component.class.getSimpleName());

		final Component transformationComponent3 = new Component(transformationComponent3UUID);
		// transformationComponent3.setId(transformationComponent3Id);
		transformationComponent3.setName(transformation2.getName() + " (component)");
		transformationComponent3.setFunction(transformation2);
		transformationComponent3.setParameterMappings(transformationComponent3ParameterMappings);

		// mapping

		final String mappingName = "my mapping";

		final String mappingUUID = UUIDService.getUUID(Mapping.class.getSimpleName());

		final Mapping mapping = new Mapping(mappingUUID);
		// mapping.setId(mappingId);
		mapping.setName(mappingName);
		mapping.addInputAttributePath(firstNameMappingAttributePathInstance);
		mapping.addInputAttributePath(familyNameMappingAttributePathInstance);
		mapping.setOutputAttributePath(outputMappingAttributePathInstance);
		mapping.setTransformation(transformationComponent3);

		// Assert.assertNotNull("the mapping id shouldn't be null", mapping.getId());
		// Assert.assertEquals("the mapping ids are not equal", mappingId, mapping.getId());
		// Assert.assertNotNull("the mapping name shouldn't be null", mapping.getName());
		// Assert.assertEquals("the mapping names are not equal", mappingName, mapping.getName());
		// Assert.assertNotNull("the transformation component id shouldn't be null", mapping.getTransformation().getId());
		// Assert.assertEquals("the transformation component ids are not equal", transformationComponent.getId(),
		// mapping.getTransformation().getId());
		// Assert.assertNotNull("the transformation component parameter mappings shouldn't be null",
		// mapping.getTransformation().getParameterMappings());
		// Assert.assertEquals("the transformation component parameter mappings' size are not equal", 2,
		// mapping.getTransformation()
		// .getParameterMappings().size());
		// Assert.assertTrue("the transformation component parameter mappings doesn't contain a mapping for function parameter '"
		// + transformation.getParameters().get(0) + "'",
		// mapping.getTransformation().getParameterMappings().containsKey(transformation.getParameters().get(0)));
		// Assert.assertEquals("the transformation component parameter mapping for '" + transformation.getParameters().get(0) +
		// "' are not equal",
		// firstNameAttributePath.toAttributePath(),
		// mapping.getTransformation().getParameterMappings().get(transformation.getParameters().get(0)));
		// Assert.assertNotNull("the transformation shouldn't be null", mapping.getTransformation().getFunction());
		// Assert.assertNotNull("the transformation id shouldn't be null", mapping.getTransformation().getFunction().getId());
		// Assert.assertEquals("the transformation ids are not equal", transformationId,
		// mapping.getTransformation().getFunction().getId());
		// Assert.assertNotNull("the transformation name shouldn't be null", mapping.getTransformation().getFunction().getName());
		// Assert.assertEquals("the transformation names are not equal", transformationName,
		// mapping.getTransformation().getFunction().getName());
		// Assert.assertNotNull("the transformation description shouldn't be null",
		// mapping.getTransformation().getFunction().getDescription());
		// Assert.assertEquals("the transformation descriptions are not equal", transformationDescription,
		// mapping.getTransformation().getFunction()
		// .getDescription());
		// Assert.assertEquals("the transformation parameters' size are not equal", 1,
		// mapping.getTransformation().getFunction().getParameters().size());
		// Assert.assertTrue("the transformation parameters doesn't contain transformation parameter '" + transformationParameter
		// + "'", mapping
		// .getTransformation().getFunction().getParameters().contains(transformationParameter));
		// Assert.assertEquals("the transformation parameter for '" + transformationParameter + "' are not equal",
		// transformationParameter, mapping
		// .getTransformation().getFunction().getParameters().iterator().next());
		// Assert.assertNotNull("the transformation components set shouldn't be null",
		// ((Transformation) mapping.getTransformation().getFunction()).getComponents());
		// Assert.assertEquals("the transformation component sets are not equal", components, ((Transformation)
		// mapping.getTransformation()
		// .getFunction()).getComponents());
		// Assert.assertNotNull("the component id shouldn't be null", ((Transformation)
		// mapping.getTransformation().getFunction()).getComponents()
		// .iterator().next().getId());
		// Assert.assertEquals("the component ids are not equal", componentId, ((Transformation)
		// mapping.getTransformation().getFunction())
		// .getComponents().iterator().next().getId());
		// Assert.assertNotNull("the component name shouldn't be null", ((Transformation)
		// mapping.getTransformation().getFunction()).getComponents()
		// .iterator().next().getName());
		// Assert.assertEquals("the component names are not equal", componentName, ((Transformation)
		// mapping.getTransformation().getFunction())
		// .getComponents().iterator().next().getName());
		// Assert.assertNotNull("the component parameter mappings shouldn't be null", ((Transformation)
		// mapping.getTransformation().getFunction())
		// .getComponents().iterator().next().getParameterMappings());
		// Assert.assertEquals("the component parameter mappings' size are not equal", 1, ((Transformation)
		// mapping.getTransformation().getFunction())
		// .getComponents().iterator().next().getParameterMappings().size());
		// Assert.assertTrue(
		// "the component parameter mappings doesn't contain a mapping for function parameter '" + functionParameterName + "'",
		// ((Transformation) mapping.getTransformation().getFunction()).getComponents().iterator().next().getParameterMappings()
		// .containsKey(functionParameterName));
		// Assert.assertEquals(
		// "the component parameter mapping for '" + functionParameterName + "' are not equal",
		// componentVariableName,
		// ((Transformation) mapping.getTransformation().getFunction()).getComponents().iterator().next().getParameterMappings()
		// .get(functionParameterName));

		String json = null;

		try {

			json = objectMapper.writeValueAsString(mapping);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		MappingTest.LOG.debug("mapping json: " + json);

		try {

			json = objectMapper.writeValueAsString(transformation2);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		MappingTest.LOG.debug("transformation json: " + json);

		try {

			json = objectMapper.writeValueAsString(transformation);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		MappingTest.LOG.debug("clean-up transformation json: " + json);

		try {

			json = objectMapper.writeValueAsString(component1);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		MappingTest.LOG.debug("clean-up previous component json: " + json);

		try {

			json = objectMapper.writeValueAsString(component);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		MappingTest.LOG.debug("clean-up main component json: " + json);

		try {

			json = objectMapper.writeValueAsString(component2);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		MappingTest.LOG.debug("clean-up next component json: " + json);
	}

	private Attribute createAttribute(final String uri, final String name) {

		final String uuid = UUIDService.getUUID(Attribute.class.getSimpleName());

		final Attribute attribute = new Attribute(uuid, uri);
		attribute.setName(name);

		Assert.assertNotNull("the attribute uri shouldn't be null", attribute.getUri());
		Assert.assertEquals("the attribute ids are not equal", uri, attribute.getUri());
		Assert.assertNotNull("the attribute name shouldn't be null", attribute.getName());
		Assert.assertEquals("the attribute names are not equal", name, attribute.getName());

		return attribute;
	}
}
