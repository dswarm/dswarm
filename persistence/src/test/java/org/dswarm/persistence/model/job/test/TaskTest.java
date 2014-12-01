/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.Job;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class TaskTest extends GuicedTest {

	private static final Logger	LOG				= LoggerFactory.getLogger(TaskTest.class);

	private final ObjectMapper	objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	@Test
	public void simpleTaskTest() {

		final Job job = createJob();

		final DataModel inputDataModel = createDataModel();
		final DataModel outputDataModel = createDataModel();

		final Task task = new Task();
		task.setName("my task");
		task.setDescription("my task description");
		task.setJob(job);
		task.setInputDataModel(inputDataModel);
		task.setOutputDataModel(outputDataModel);

		String json = null;

		try {

			json = objectMapper.writeValueAsString(task);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		TaskTest.LOG.debug("task json: " + json);
	}

	private DataModel createDataModel() {

		// first attribute path

		final String dctermsTitleId = "http://purl.org/dc/terms/title";
		final String dctermsTitleName = "title";

		final Attribute dctermsTitle = createAttribute(dctermsTitleId, dctermsTitleName);

		final String dctermsHasPartId = "http://purl.org/dc/terms/hasPart";
		final String dctermsHasPartName = "hasPart";

		final Attribute dctermsHasPart = createAttribute(dctermsHasPartId, dctermsHasPartName);

		final AttributePath attributePath1 = new AttributePath();
		// attributePath1.setId(UUID.randomUUID().toString());

		attributePath1.addAttribute(dctermsTitle);
		attributePath1.addAttribute(dctermsHasPart);
		attributePath1.addAttribute(dctermsTitle);

		// second attribute path

		final String dctermsCreatorId = "http://purl.org/dc/terms/creator";
		final String dctermsCreatorName = "creator";

		final Attribute dctermsCreator = createAttribute(dctermsCreatorId, dctermsCreatorName);

		final String foafNameId = "http://xmlns.com/foaf/0.1/name";
		final String foafNameName = "name";

		final Attribute foafName = createAttribute(foafNameId, foafNameName);

		final AttributePath attributePath2 = new AttributePath();
		// attributePath2.setId(UUID.randomUUID().toString());

		attributePath2.addAttribute(dctermsCreator);
		attributePath2.addAttribute(foafName);

		// third attribute path

		final String dctermsCreatedId = "http://purl.org/dc/terms/created";
		final String dctermsCreatedName = "created";

		final Attribute dctermsCreated = createAttribute(dctermsCreatedId, dctermsCreatedName);

		final AttributePath attributePath3 = new AttributePath();
		// attributePath3.setId(UUID.randomUUID().toString());

		attributePath3.addAttribute(dctermsCreated);

		// record class

		final String biboDocumentId = "http://purl.org/ontology/bibo/Document";
		final String biboDocumentName = "document";

		final Clasz biboDocument = new Clasz(biboDocumentId, biboDocumentName);

		// schema

		final Schema schema = new Schema();
		// schema.setId(UUID.randomUUID().toString());

		schema.addAttributePath(createAttributePathInstance(attributePath1));
		schema.addAttributePath(createAttributePathInstance(attributePath2));
		schema.addAttributePath(createAttributePathInstance(attributePath3));
		schema.setRecordClass(biboDocument);

		// data resource
		final Resource resource = new Resource();

		resource.setName("bla");
		resource.setDescription("blubblub");
		resource.setType(ResourceType.FILE);

		final String attributeKey = "path";
		final String attributeValue = "/path/to/file.end";

		final ObjectNode attributes = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
		attributes.put(attributeKey, attributeValue);

		resource.setAttributes(attributes);

		// configuration
		final Configuration configuration = new Configuration();

		configuration.setName("my configuration");
		configuration.setDescription("configuration description");

		final ObjectNode parameters = new ObjectNode(objectMapper.getNodeFactory());
		final String parameterKey = "fileseparator";
		final String parameterValue = ";";
		parameters.put(parameterKey, parameterValue);

		configuration.setParameters(parameters);

		resource.addConfiguration(configuration);

		// data model
		final DataModel dataModel = new DataModel();
		dataModel.setName("my data model");
		dataModel.setDescription("my data model description");
		dataModel.setDataResource(resource);
		dataModel.setConfiguration(configuration);
		dataModel.setSchema(schema);

		String json = null;

		try {

			json = objectMapper.writeValueAsString(dataModel);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		TaskTest.LOG.debug("data model json: " + json);

		return dataModel;
	}

	public Job createJob() {

		final Set<Mapping> mappings = Sets.newLinkedHashSet();

		final Mapping simpleMapping = simpleMapping();
		final Mapping complexMapping = complexMapping();

		mappings.add(simpleMapping);
		mappings.add(complexMapping);

		final Job job = new Job();
		job.setName("my job");
		job.setDescription("my job description");
		job.setMappings(mappings);

		String json = null;

		try {

			json = objectMapper.writeValueAsString(job);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		TaskTest.LOG.debug("job json: " + json);

		return job;
	}

	private Mapping simpleMapping() {

		final String functionName = "trim";
		final String functionDescription = "trims leading and trailing whitespaces from a given string";
		final String functionParameter = "inputString";

		final Function function = new Function();
		function.setName(functionName);
		function.setDescription(functionDescription);
		function.addParameter(functionParameter);

		final String componentName = "my trim component";
		final Map<String, String> parameterMappings = Maps.newLinkedHashMap();

		final String functionParameterName = "inputString";
		final String componentVariableName = "transformationInputString";

		parameterMappings.put(functionParameterName, componentVariableName);

		final Component component = new Component();
		component.setName(componentName);
		component.setFunction(function);
		component.setParameterMappings(parameterMappings);

		// transformation

		final String transformationName = "my transformation";
		final String transformationDescription = "transformation which just makes use of one function";
		final String transformationParameter = "transformationInputString";

		final Set<Component> components = Sets.newLinkedHashSet();

		components.add(component);

		final Transformation transformation = new Transformation();
		transformation.setName(transformationName);
		transformation.setDescription(transformationDescription);
		transformation.setComponents(components);
		transformation.addParameter(transformationParameter);

		// attribute paths

		// input attribute path

		final String dctermsTitleId = "http://purl.org/dc/terms/title";
		final String dctermsTitleName = "title";

		final Attribute dctermsTitle = createAttribute(dctermsTitleId, dctermsTitleName);

		final AttributePath inputAttributePath = new AttributePath();

		inputAttributePath.addAttribute(dctermsTitle);

		final MappingAttributePathInstance inputMappingAttributePathInstance = new MappingAttributePathInstance();
		inputMappingAttributePathInstance.setAttributePath(inputAttributePath);

		// output attribute path

		final String rdfsLabelId = "http://www.w3.org/2000/01/rdf-schema#label";
		final String rdfsLabelName = "label";

		final Attribute rdfsLabel = createAttribute(rdfsLabelId, rdfsLabelName);

		final AttributePath outputAttributePath = new AttributePath();

		outputAttributePath.addAttribute(rdfsLabel);

		final MappingAttributePathInstance outputMappingAttributePathInstance = new MappingAttributePathInstance();
		outputMappingAttributePathInstance.setAttributePath(outputAttributePath);

		// transformation component

		final Map<String, String> transformationComponentParameterMappings = Maps.newLinkedHashMap();

		transformationComponentParameterMappings.put(transformation.getParameters().get(0), inputAttributePath.toAttributePath());
		transformationComponentParameterMappings.put("transformationOutputVariable", outputAttributePath.toAttributePath());

		final Component transformationComponent = new Component();
		transformationComponent.setName(transformation.getName() + " (component)");
		transformationComponent.setFunction(transformation);
		transformationComponent.setParameterMappings(transformationComponentParameterMappings);

		// mapping

		final String mappingName = "my simple mapping";

		final Mapping mapping = new Mapping();
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

		TaskTest.LOG.debug("simple mapping json: " + json);

		return mapping;
	}

	private Mapping complexMapping() {

		// previous component

		final String function1Name = "replace";
		final String function1Description = "replace certain parts of a given string that matches a certain regex";
		final String function1Parameter = "inputString";
		final String function2Parameter = "regex";
		final String function3Parameter = "replaceString";

		final Function function1 = new Function();
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

		final Component component1 = new Component();
		component1.setName(component1Name);
		component1.setFunction(function1);
		component1.setParameterMappings(parameterMapping1);

		// next component

		final String function2Name = "lower_case";
		final String function2Description = "lower cases all characters of a given string";
		final String function4Parameter = "inputString";

		final Function function2 = new Function();
		function2.setName(function2Name);
		function2.setDescription(function2Description);
		function2.addParameter(function4Parameter);

		final String component2Name = "my lower case component";
		final Map<String, String> parameterMapping2 = Maps.newLinkedHashMap();

		final String functionParameterName4 = "inputString";
		final String componentVariableName4 = "previousComponent.outputString";

		parameterMapping2.put(functionParameterName4, componentVariableName4);

		final Component component2 = new Component();
		component2.setName(component2Name);
		component2.setFunction(function2);
		component2.setParameterMappings(parameterMapping2);

		// main component

		final String functionName = "trim";
		final String functionDescription = "trims leading and trailing whitespaces from a given string";
		final String functionParameter = "inputString";

		final Function function = new Function();
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

		final Component component = new Component();
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

		final Transformation transformation = new Transformation();
		transformation.setName(transformationName);
		transformation.setDescription(transformationDescription);
		transformation.setComponents(components);
		transformation.addParameter(transformationParameter);

		// transformation component 1 (in main transformation) -> clean first name

		final String transformationComponentId = UUID.randomUUID().toString();
		final Map<String, String> transformationComponentParameterMappings = Maps.newLinkedHashMap();

		transformationComponentParameterMappings.put("transformationInputString", "firstName");

		final Component transformationComponent = new Component();
		transformationComponent.setName("prepare first name");
		transformationComponent.setFunction(transformation);
		transformationComponent.setParameterMappings(transformationComponentParameterMappings);

		// transformation component 1 (in main transformation) -> clean family name

		final String transformationComponentId2 = UUID.randomUUID().toString();
		final Map<String, String> transformationComponentParameterMappings2 = Maps.newLinkedHashMap();

		transformationComponentParameterMappings2.put("transformationInputString", "familyName");

		final Component transformationComponent2 = new Component();
		transformationComponent2.setName("prepare family name");
		transformationComponent2.setFunction(transformation);
		transformationComponent2.setParameterMappings(transformationComponentParameterMappings2);

		// concat component -> full name

		final String function4Name = "concat";
		final String function4Description = "concatenates two given string";
		final String function5Parameter = "firstString";
		final String function6Parameter = "secondString";

		final Function function4 = new Function();
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

		final Component component4 = new Component();
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

		final String transformation2Name = "my complex transformation";
		final String transformation2Description = "transformation which makes use of three functions (two transformations and one funcion)";
		final String transformation2Parameter = "firstName";
		final String transformation2Parameter2 = "familyName";

		final Set<Component> components2 = Sets.newLinkedHashSet();

		components2.add(transformationComponent);
		components2.add(transformationComponent2);
		components2.add(component4);

		final Transformation transformation2 = new Transformation();
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

		final AttributePath firstNameAttributePath = new AttributePath();

		firstNameAttributePath.addAttribute(dctermsCreator);
		firstNameAttributePath.addAttribute(firstName);

		final MappingAttributePathInstance firstNameMappingAttributePathInstance = new MappingAttributePathInstance();
		firstNameMappingAttributePathInstance.setAttributePath(firstNameAttributePath);

		// family name attribute path

		final String familyNameId = "http://xmlns.com/foaf/0.1/familyName";
		final String familyNameName = "familyName";

		final Attribute familyName = createAttribute(familyNameId, familyNameName);

		final AttributePath familyNameAttributePath = new AttributePath();

		familyNameAttributePath.addAttribute(dctermsCreator);
		familyNameAttributePath.addAttribute(familyName);

		final MappingAttributePathInstance familyNameMappingAttributePathInstance = new MappingAttributePathInstance();
		familyNameMappingAttributePathInstance.setAttributePath(familyNameAttributePath);

		// output attribute path

		final String foafNameId = "http://xmlns.com/foaf/0.1/name";
		final String foafNameName = "name";

		final Attribute foafName = createAttribute(foafNameId, foafNameName);

		final AttributePath nameAttributePath = new AttributePath();

		nameAttributePath.addAttribute(dctermsCreator);
		nameAttributePath.addAttribute(foafName);

		final MappingAttributePathInstance outputMappingAttributePathInstance = new MappingAttributePathInstance();
		outputMappingAttributePathInstance.setAttributePath(nameAttributePath);

		// transformation component

		final Map<String, String> transformationComponent3ParameterMappings = Maps.newLinkedHashMap();

		transformationComponent3ParameterMappings.put(transformation2Parameter, firstNameAttributePath.toAttributePath());
		transformationComponent3ParameterMappings.put(transformation2Parameter2, familyNameAttributePath.toAttributePath());
		transformationComponent3ParameterMappings.put("transformationOutputVariable", nameAttributePath.toAttributePath());

		final Component transformationComponent3 = new Component();
		transformationComponent3.setName(transformation2.getName() + " (component)");
		transformationComponent3.setFunction(transformation2);
		transformationComponent3.setParameterMappings(transformationComponent3ParameterMappings);

		// mapping

		final String mappingName = "my complex mapping";

		final Mapping mapping = new Mapping();
		mapping.setName(mappingName);
		mapping.addInputAttributePath(firstNameMappingAttributePathInstance);
		mapping.addInputAttributePath(familyNameMappingAttributePathInstance);
		mapping.setOutputAttributePath(outputMappingAttributePathInstance);
		mapping.setTransformation(transformationComponent3);

		String json = null;

		try {

			json = objectMapper.writeValueAsString(mapping);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		TaskTest.LOG.debug("complex mapping json: " + json);

		try {

			json = objectMapper.writeValueAsString(transformation2);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		TaskTest.LOG.debug("transformation json: " + json);

		try {

			json = objectMapper.writeValueAsString(transformation);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		TaskTest.LOG.debug("clean-up transformation json: " + json);

		try {

			json = objectMapper.writeValueAsString(component1);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		TaskTest.LOG.debug("clean-up previous component json: " + json);

		try {

			json = objectMapper.writeValueAsString(component);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		TaskTest.LOG.debug("clean-up main component json: " + json);

		try {

			json = objectMapper.writeValueAsString(component2);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		TaskTest.LOG.debug("clean-up next component json: " + json);

		return mapping;
	}

	private Attribute createAttribute(final String id, final String name) {

		final Attribute attribute = new Attribute(id);
		attribute.setName(name);

		Assert.assertNotNull("the attribute id shouldn't be null", attribute.getUri());
		Assert.assertEquals("the attribute ids are not equal", id, attribute.getUri());
		Assert.assertNotNull("the attribute name shouldn't be null", attribute.getName());
		Assert.assertEquals("the attribute names are not equal", name, attribute.getName());

		return attribute;
	}
	
	private static SchemaAttributePathInstance createAttributePathInstance(final AttributePath attributePath) {
		final SchemaAttributePathInstance attributePathInstance = new SchemaAttributePathInstance();
		attributePathInstance.setAttributePath(attributePath);

		Assert.assertNotNull("the attribute path should not be null", attributePathInstance.getAttributePath());

		return attributePathInstance;
	}

}
