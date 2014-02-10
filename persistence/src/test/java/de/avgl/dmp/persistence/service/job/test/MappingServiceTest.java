package de.avgl.dmp.persistence.service.job.test;

import java.util.Iterator;
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
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.job.proxy.ProxyMapping;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.MappingAttributePathInstance;
import de.avgl.dmp.persistence.service.job.MappingService;
import de.avgl.dmp.persistence.service.job.TransformationService;
import de.avgl.dmp.persistence.service.job.test.utils.ComponentServiceTestUtils;
import de.avgl.dmp.persistence.service.job.test.utils.FunctionServiceTestUtils;
import de.avgl.dmp.persistence.service.schema.test.utils.AttributePathServiceTestUtils;
import de.avgl.dmp.persistence.service.schema.test.utils.AttributeServiceTestUtils;
import de.avgl.dmp.persistence.service.schema.test.utils.MappingAttributePathInstanceServiceTestUtils;
import de.avgl.dmp.persistence.service.test.IDBasicJPAServiceTest;

public class MappingServiceTest extends IDBasicJPAServiceTest<ProxyMapping, Mapping, MappingService> {

	private static final org.apache.log4j.Logger				LOG								= org.apache.log4j.Logger
																										.getLogger(MappingServiceTest.class);

	private final ObjectMapper									objectMapper					= GuicedTest.injector.getInstance(ObjectMapper.class);

	private final Map<Long, Function>							functions						= Maps.newLinkedHashMap();

	private Map<Long, Attribute>								attributes						= Maps.newLinkedHashMap();

	private Map<Long, AttributePath>							attributePaths					= Maps.newLinkedHashMap();

	private Map<Long, Component>								components						= Maps.newLinkedHashMap();

	private Map<Long, Transformation>							transformations					= Maps.newLinkedHashMap();

	private Map<Long, MappingAttributePathInstance>				mappingAttributePathInstances	= Maps.newLinkedHashMap();

	private final AttributeServiceTestUtils						attributeServiceTestUtils;
	private final AttributePathServiceTestUtils					attributePathServiceTestUtils;
	private final FunctionServiceTestUtils						functionServiceTestUtils;
	private final MappingAttributePathInstanceServiceTestUtils	mappingAttributePathInstanceServiceTestUtils;
	private final ComponentServiceTestUtils						componentServiceTestUtils;

	public MappingServiceTest() {

		super("mapping", MappingService.class);

		attributeServiceTestUtils = new AttributeServiceTestUtils();
		attributePathServiceTestUtils = new AttributePathServiceTestUtils();
		functionServiceTestUtils = new FunctionServiceTestUtils();
		mappingAttributePathInstanceServiceTestUtils = new MappingAttributePathInstanceServiceTestUtils();
		componentServiceTestUtils = new ComponentServiceTestUtils();
	}

	@Test
	public void simpleMappingTest() throws Exception {

		LOG.debug("start simple mapping test");

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

		final Attribute dctermsTitle = attributeServiceTestUtils.createAttribute(dctermsTitleId, dctermsTitleName);
		attributes.put(dctermsTitle.getId(), dctermsTitle);

		final LinkedList<Attribute> dctermsTitleAttributePath = Lists.newLinkedList();
		dctermsTitleAttributePath.add(dctermsTitle);

		final AttributePath inputAttributePath = attributePathServiceTestUtils.createAttributePath(dctermsTitleAttributePath);
		attributePaths.put(inputAttributePath.getId(), inputAttributePath);

		// input mapping attribute path instance

		final MappingAttributePathInstance inputMappingAttributePathInstance = mappingAttributePathInstanceServiceTestUtils
				.createMappingAttributePathInstance("input mapping attribute path instance", inputAttributePath, null, null);
		mappingAttributePathInstances.put(inputMappingAttributePathInstance.getId(), inputMappingAttributePathInstance);

		// output attribute path

		final String rdfsLabelId = "http://www.w3.org/2000/01/rdf-schema#label";
		final String rdfsLabelName = "label";

		final Attribute rdfsLabel = attributeServiceTestUtils.createAttribute(rdfsLabelId, rdfsLabelName);
		attributes.put(rdfsLabel.getId(), rdfsLabel);

		final LinkedList<Attribute> rdfsLabelAttributePath = Lists.newLinkedList();
		rdfsLabelAttributePath.add(rdfsLabel);

		final AttributePath outputAttributePath = attributePathServiceTestUtils.createAttributePath(rdfsLabelAttributePath);
		attributePaths.put(outputAttributePath.getId(), outputAttributePath);

		// output mapping attribute path instance

		final MappingAttributePathInstance outputMappingAttributePathInstance = mappingAttributePathInstanceServiceTestUtils
				.createMappingAttributePathInstance("output mapping attribute path instance", outputAttributePath, null, null);
		mappingAttributePathInstances.put(outputMappingAttributePathInstance.getId(), outputMappingAttributePathInstance);

		// transformation component

		final Map<String, String> transformationComponentParameterMappings = Maps.newLinkedHashMap();

		transformationComponentParameterMappings.put(transformation.getParameters().get(0), inputAttributePath.toAttributePath());
		transformationComponentParameterMappings.put("transformationOutputVariable", outputAttributePath.toAttributePath());

		final Component transformationComponent = componentServiceTestUtils.createComponent(transformation.getName() + " (component)",
				transformationComponentParameterMappings, transformation, null, null);

		// mapping

		final String mappingName = "my mapping";

		final Mapping mapping = createObject().getObject();
		mapping.setName(mappingName);
		mapping.addInputAttributePath(inputMappingAttributePathInstance);
		mapping.setOutputAttributePath(outputMappingAttributePathInstance);
		mapping.setTransformation(transformationComponent);

		final Mapping updatedMapping = updateObjectTransactional(mapping).getObject();

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

		deleteObject(updatedMapping.getId());
		deleteTransformation(transformation);
		componentServiceTestUtils.checkDeletedComponent(component);
		functionServiceTestUtils.deleteObject(function);

		for (final MappingAttributePathInstance mappingAttributePathInstance : mappingAttributePathInstances.values()) {

			mappingAttributePathInstanceServiceTestUtils.deleteObject(mappingAttributePathInstance);
		}

		for (final AttributePath attributePath : attributePaths.values()) {

			attributePathServiceTestUtils.deleteObject(attributePath);
		}

		for (final Attribute attribute : attributes.values()) {

			attributeServiceTestUtils.deleteObject(attribute);
		}

		LOG.debug("end simple mappping test");
	}

	@Test
	public void complexMappingTest() throws Exception {

		LOG.debug("start complex mapping test");

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
		this.components.put(component1.getId(), component1);

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
		this.components.put(component2.getId(), component2);

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
		this.components.put(component2.getId(), component2);

		// transformation

		final String transformationName = "my transformation";
		final String transformationDescription = "transformation which makes use of three functions";
		final String transformationParameter = "transformationInputString";

		final Set<Component> components = Sets.newLinkedHashSet();

		components.add(component.getInputComponents().iterator().next());
		components.add(component);
		components.add(component.getOutputComponents().iterator().next());

		final LinkedList<String> transformationParameters = Lists.newLinkedList();

		transformationParameters.add(transformationParameter);

		final Transformation transformation = createTransformation(transformationName, transformationDescription, components,
				transformationParameters);

		Assert.assertNotNull("the transformation components set shouldn't be null", transformation.getComponents());
		Assert.assertEquals("the transformation components sizes are not equal", 3, transformation.getComponents().size());

		// transformation component 1 (in main transformation) -> clean first name

		final String transformationComponentFunctionParameterName = "transformationInputString";
		final String transformationComponentVariableName = "firstName";

		final Map<String, String> transformationComponentParameterMappings = Maps.newLinkedHashMap();

		transformationComponentParameterMappings.put(transformationComponentFunctionParameterName, transformationComponentVariableName);

		final String transformationComponentName = "prepare first name";

		final Component transformationComponent = componentServiceTestUtils.createComponent(transformationComponentName,
				transformationComponentParameterMappings, transformation, null, null);
		this.components.put(transformationComponent.getId(), transformationComponent);

		// transformation component 2 (in main transformation) -> clean family name

		final Map<String, String> transformationComponentParameterMappings2 = Maps.newLinkedHashMap();

		transformationComponentParameterMappings2.put("transformationInputString", "familyName");

		final Component transformationComponent2 = componentServiceTestUtils.createComponent("prepare family name",
				transformationComponentParameterMappings2, transformation, null, null);
		this.components.put(transformationComponent2.getId(), transformationComponent2);

		// concat component -> full name

		final String function4Name = "concat";
		final String function4Description = "concatenates two given string";
		final String function5Parameter = "firstString";
		final String function6Parameter = "secondString";

		final LinkedList<String> function4Parameters = Lists.newLinkedList();
		function4Parameters.add(function5Parameter);
		function4Parameters.add(function6Parameter);

		final Function function4 = functionServiceTestUtils.createFunction(function4Name, function4Description, function4Parameters);
		functions.put(function4.getId(), function4);

		final String component4Name = "full name";
		final Map<String, String> parameterMapping4 = Maps.newLinkedHashMap();

		final String functionParameterName5 = "firstString";
		final String componentVariableName5 = transformationComponent.getId() + ".outputVariable";
		final String functionParameterName6 = "secondString";
		final String componentVariableName6 = transformationComponent2.getId() + ".outputVariable";

		parameterMapping4.put(functionParameterName5, componentVariableName5);
		parameterMapping4.put(functionParameterName6, componentVariableName6);

		final Set<Component> component4InputComponents = Sets.newLinkedHashSet();

		component4InputComponents.add(transformationComponent);
		component4InputComponents.add(transformationComponent2);

		final Component component4 = componentServiceTestUtils.createComponent(component4Name, parameterMapping4, function4,
				component4InputComponents, null);
		this.components.put(component4.getId(), component4);

		// final Set<Component> transformationComponentOutputComponents = Sets.newLinkedHashSet();
		//
		// transformationComponentOutputComponents.add(component4);
		//
		// transformationComponent.setOutputComponents(transformationComponentOutputComponents);
		//
		// final Set<Component> transformationComponent2OutputComponents = Sets.newLinkedHashSet();
		//
		// transformationComponent2OutputComponents.add(component4);
		//
		// transformationComponent2.setOutputComponents(transformationComponent2OutputComponents);

		// TODO: update transformation component 1 + 2 (?)

		// transformation 2

		final String transformation2Name = "my transformation 2";
		final String transformation2Description = "transformation which makes use of three functions (two transformations and one funcion)";
		final String transformation2Parameter = "firstName";
		final String transformation2Parameter2 = "familyName";

		final Set<Component> components2 = Sets.newLinkedHashSet();

		final Iterator<Component> iter = component4.getInputComponents().iterator();

		components2.add(iter.next());
		components2.add(iter.next());
		components2.add(component4);

		final LinkedList<String> transformation2Parameters = Lists.newLinkedList();
		transformation2Parameters.add(transformation2Parameter);
		transformation2Parameters.add(transformation2Parameter2);

		final Transformation transformation2 = createTransformation(transformation2Name, transformation2Description, components2,
				transformation2Parameters);

		// attribute paths

		// input attribute paths

		final String dctermsCreatorId = "http://purl.org/dc/terms/creator";
		final String dctermsCreatorName = "creator";

		final Attribute dctermsCreator = attributeServiceTestUtils.createAttribute(dctermsCreatorId, dctermsCreatorName);
		attributes.put(dctermsCreator.getId(), dctermsCreator);

		// first name attribute path

		final String firstNameId = "http://xmlns.com/foaf/0.1/firstName";
		final String firstNameName = "firstName";

		final Attribute firstName = attributeServiceTestUtils.createAttribute(firstNameId, firstNameName);
		attributes.put(firstName.getId(), firstName);

		final LinkedList<Attribute> firstNameAttributePathList = Lists.newLinkedList();
		firstNameAttributePathList.add(dctermsCreator);
		firstNameAttributePathList.add(firstName);

		final AttributePath firstNameAttributePath = attributePathServiceTestUtils.createAttributePath(firstNameAttributePathList);
		attributePaths.put(firstNameAttributePath.getId(), firstNameAttributePath);

		// first name mapping attribute path instance

		final MappingAttributePathInstance firstNameMappingAttributePathInstance = mappingAttributePathInstanceServiceTestUtils
				.createMappingAttributePathInstance("first name mapping attribute path instance", firstNameAttributePath, null, null);
		mappingAttributePathInstances.put(firstNameMappingAttributePathInstance.getId(), firstNameMappingAttributePathInstance);

		// family name attribute path

		final String familyNameId = "http://xmlns.com/foaf/0.1/familyName";
		final String familyNameName = "familyName";

		final Attribute familyName = attributeServiceTestUtils.createAttribute(familyNameId, familyNameName);
		attributes.put(familyName.getId(), familyName);

		final LinkedList<Attribute> familyNameAttributePathList = Lists.newLinkedList();
		familyNameAttributePathList.add(dctermsCreator);
		familyNameAttributePathList.add(familyName);

		final AttributePath familyNameAttributePath = attributePathServiceTestUtils.createAttributePath(familyNameAttributePathList);
		attributePaths.put(familyNameAttributePath.getId(), familyNameAttributePath);

		// family name mapping attribute path instance

		final MappingAttributePathInstance familyNameMappingAttributePathInstance = mappingAttributePathInstanceServiceTestUtils
				.createMappingAttributePathInstance("family name mapping attribute path instance", familyNameAttributePath, null, null);
		mappingAttributePathInstances.put(familyNameMappingAttributePathInstance.getId(), familyNameMappingAttributePathInstance);

		// output attribute path

		final String foafNameId = "http://xmlns.com/foaf/0.1/name";
		final String foafNameName = "name";

		final Attribute foafName = attributeServiceTestUtils.createAttribute(foafNameId, foafNameName);
		attributes.put(foafName.getId(), foafName);

		final LinkedList<Attribute> nameAttributePathList = Lists.newLinkedList();
		nameAttributePathList.add(dctermsCreator);
		nameAttributePathList.add(foafName);

		final AttributePath nameAttributePath = attributePathServiceTestUtils.createAttributePath(nameAttributePathList);
		attributePaths.put(nameAttributePath.getId(), nameAttributePath);

		// output mapping attribute path instance

		final MappingAttributePathInstance outputMappingAttributePathInstance = mappingAttributePathInstanceServiceTestUtils
				.createMappingAttributePathInstance("output mapping attribute path instance", nameAttributePath, null, null);
		mappingAttributePathInstances.put(outputMappingAttributePathInstance.getId(), outputMappingAttributePathInstance);

		// transformation component

		final Map<String, String> transformationComponent3ParameterMappings = Maps.newLinkedHashMap();

		transformationComponent3ParameterMappings.put(transformation2Parameter, firstNameAttributePath.toAttributePath());
		transformationComponent3ParameterMappings.put(transformation2Parameter2, familyNameAttributePath.toAttributePath());
		transformationComponent3ParameterMappings.put("transformationOutputVariable", nameAttributePath.toAttributePath());

		final Component transformationComponent3 = componentServiceTestUtils.createComponent(transformation2.getName() + " (component)",
				transformationComponent3ParameterMappings, transformation2, null, null);
		this.components.put(transformationComponent3.getId(), transformationComponent3);

		// mapping

		final String mappingName = "my mapping";

		final Mapping mapping = createObject().getObject();
		mapping.setName(mappingName);
		mapping.addInputAttributePath(firstNameMappingAttributePathInstance);
		mapping.addInputAttributePath(familyNameMappingAttributePathInstance);
		mapping.setOutputAttributePath(outputMappingAttributePathInstance);
		mapping.setTransformation(transformationComponent3);

		final Mapping updatedMapping = updateObjectTransactional(mapping).getObject();

		Assert.assertNotNull("the mapping shouldn't be null", updatedMapping);
		Assert.assertNotNull("the mapping name shouldn't be null", updatedMapping.getName());
		Assert.assertEquals("the mapping names are not equal", mappingName, updatedMapping.getName());
		Assert.assertNotNull("the transformation component id shouldn't be null", updatedMapping.getTransformation().getId());
		Assert.assertEquals("the transformation component ids are not equal", transformationComponent3.getId(), updatedMapping.getTransformation()
				.getId());
		Assert.assertNotNull("the transformation component parameter mappings shouldn't be null", updatedMapping.getTransformation()
				.getParameterMappings());
		Assert.assertEquals("the transformation component parameter mappings' size are not equal", 3, updatedMapping.getTransformation()
				.getParameterMappings().size());
		Assert.assertTrue("the transformation component parameter mappings doesn't contain a mapping for function parameter '"
				+ transformation2.getParameters().get(0) + "'",
				updatedMapping.getTransformation().getParameterMappings().containsKey(transformation2.getParameters().get(0)));
		Assert.assertEquals("the transformation component parameter mapping for '" + transformation2.getParameters().get(0) + "' are not equal",
				firstNameAttributePath.toAttributePath(),
				updatedMapping.getTransformation().getParameterMappings().get(transformation2.getParameters().get(0)));
		Assert.assertNotNull("the transformation shouldn't be null", updatedMapping.getTransformation().getFunction());
		Assert.assertNotNull("the transformation id shouldn't be null", updatedMapping.getTransformation().getFunction().getId());
		Assert.assertEquals("the transformation ids are not equal", transformation2.getId(), updatedMapping.getTransformation().getFunction().getId());
		Assert.assertNotNull("the transformation name shouldn't be null", updatedMapping.getTransformation().getFunction().getName());
		Assert.assertEquals("the transformation names are not equal", transformation2Name, updatedMapping.getTransformation().getFunction().getName());
		Assert.assertNotNull("the transformation description shouldn't be null", updatedMapping.getTransformation().getFunction().getDescription());
		Assert.assertEquals("the transformation descriptions are not equal", transformation2Description, updatedMapping.getTransformation()
				.getFunction().getDescription());
		Assert.assertEquals("the transformation parameters' size are not equal", 2, updatedMapping.getTransformation().getFunction().getParameters()
				.size());
		Assert.assertTrue("the transformation parameters doesn't contain transformation parameter '" + transformation2Parameter + "'", updatedMapping
				.getTransformation().getFunction().getParameters().contains(transformation2Parameter));
		Assert.assertEquals("the transformation parameter for '" + transformation2Parameter + "' are not equal", transformation2Parameter,
				updatedMapping.getTransformation().getFunction().getParameters().iterator().next());
		Assert.assertEquals("the function type is not '" + FunctionType.Transformation + "'", FunctionType.Transformation, updatedMapping
				.getTransformation().getFunction().getFunctionType());
		Assert.assertTrue("mapping transformation is not a '" + FunctionType.Transformation + "'",
				Transformation.class.isInstance(updatedMapping.getTransformation().getFunction()));
		Assert.assertNotNull("the transformation components set shouldn't be null", ((Transformation) updatedMapping.getTransformation()
				.getFunction()).getComponents());
		Assert.assertEquals("the transformation component sets are not equal", components2, ((Transformation) updatedMapping.getTransformation()
				.getFunction()).getComponents());
		Assert.assertNotNull("the component id shouldn't be null", ((Transformation) updatedMapping.getTransformation().getFunction())
				.getComponents().iterator().next().getId());

		final Iterator<Component> componentIter = ((Transformation) updatedMapping.getTransformation().getFunction()).getComponents().iterator();

		Component transformationComponentFromIter = null;

		while (componentIter.hasNext()) {

			final Component nextComponent = componentIter.next();

			if (nextComponent.getId().equals(transformationComponent.getId())) {

				transformationComponentFromIter = nextComponent;

				break;
			}
		}

		Assert.assertNotNull("the component shouldn't be null", transformationComponentFromIter);
		Assert.assertNotNull("the component name shouldn't be null", ((Transformation) updatedMapping.getTransformation().getFunction())
				.getComponents().iterator().next().getName());
		Assert.assertEquals("the component names are not equal", transformationComponentName, transformationComponentFromIter.getName());
		Assert.assertNotNull("the component parameter mappings shouldn't be null", transformationComponentFromIter.getParameterMappings());
		Assert.assertEquals("the component parameter mappings' size are not equal", 1, transformationComponentFromIter.getParameterMappings().size());
		Assert.assertTrue("the component parameter mappings doesn't contain a mapping for function parameter '"
				+ transformationComponentFunctionParameterName + "'",
				transformationComponentFromIter.getParameterMappings().containsKey(transformationComponentFunctionParameterName));
		Assert.assertEquals("the component parameter mapping for '" + transformationComponentFunctionParameterName + "' are not equal",
				transformationComponentVariableName,
				transformationComponentFromIter.getParameterMappings().get(transformationComponentFunctionParameterName));

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedMapping);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("mapping json: " + json);

		try {

			json = objectMapper.writeValueAsString(transformation2);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("transformation json: " + json);

		try {

			json = objectMapper.writeValueAsString(transformation);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("clean-up transformation json: " + json);

		try {

			json = objectMapper.writeValueAsString(component1);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("clean-up previous component json: " + json);

		try {

			json = objectMapper.writeValueAsString(component);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("clean-up main component json: " + json);

		try {

			json = objectMapper.writeValueAsString(component2);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("clean-up next component json: " + json);

		// clean-up
		deleteObject(updatedMapping.getId());

		// upper transformation needs to be deleted first, so that the functions/transformations of its components will be
		// released
		// note: could maybe improved via bidirectional relationship from component to function and vice versa
		deleteTransformation(transformation2);

		for (final Transformation transformationToBeDeleted : transformations.values()) {

			deleteTransformation(transformationToBeDeleted);
		}

		for (final Component componentAlreadyDeleted : this.components.values()) {

			componentServiceTestUtils.checkDeletedComponent(componentAlreadyDeleted);
		}

		for (final Function functionToDelete : functions.values()) {

			functionServiceTestUtils.deleteObject(functionToDelete);
		}

		for (final MappingAttributePathInstance mappingAttributePathInstance : mappingAttributePathInstances.values()) {

			mappingAttributePathInstanceServiceTestUtils.deleteObject(mappingAttributePathInstance);
		}

		for (final AttributePath attributePath : attributePaths.values()) {

			attributePathServiceTestUtils.deleteObject(attributePath);
		}

		for (final Attribute attribute : attributes.values()) {

			attributeServiceTestUtils.deleteObject(attribute);
		}

		LOG.debug("end complex mapping test");
	}

	private Transformation createTransformation(final String name, final String description, final Set<Component> components,
			final LinkedList<String> parameters) {

		final TransformationService transformationService = GuicedTest.injector.getInstance(TransformationService.class);

		Transformation transformation = null;

		try {

			transformation = transformationService.createObjectTransactional().getObject();
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

			updatedTransformation = transformationService.updateObjectTransactional(transformation).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the transformation of id = '" + transformation.getId() + "'", false);
		}

		Assert.assertNotNull("the updated component shouldn't be null", updatedTransformation);
		Assert.assertNotNull("the transformation name shouldn't be null", updatedTransformation.getId());
		Assert.assertNotNull("the transformation name shouldn't be null", updatedTransformation.getName());
		Assert.assertEquals("the transformation names are not equal", name, updatedTransformation.getName());
		Assert.assertNotNull("the transformation parameter mappings shouldn't be null", updatedTransformation.getParameters());

		transformations.put(updatedTransformation.getId(), updatedTransformation);

		return updatedTransformation;
	}

	private void deleteTransformation(final Transformation transformation) {

		LOG.debug("try to delete transformation '" + transformation.getId() + "'");

		final TransformationService transformationService = GuicedTest.injector.getInstance(TransformationService.class);

		Assert.assertNotNull("transformation service shouldn't be null", transformationService);

		final Long transformationId = transformation.getId();

		final Transformation notDeletedTransformation = transformationService.getObject(transformationId);

		if (notDeletedTransformation != null) {

			transformationService.deleteObject(transformationId);

			final Transformation deletedTransformation = transformationService.getObject(transformationId);

			Assert.assertNull("deleted transformation shouldn't exist any more", deletedTransformation);

			LOG.debug("deleted transformation '" + transformation.getId() + "'");
		}
	}
}
