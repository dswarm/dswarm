package org.dswarm.persistence.service.job.test;

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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.FunctionType;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.job.proxy.ProxyProject;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.job.MappingService;
import org.dswarm.persistence.service.job.ProjectService;
import org.dswarm.persistence.service.job.test.utils.ComponentServiceTestUtils;
import org.dswarm.persistence.service.job.test.utils.FunctionServiceTestUtils;
import org.dswarm.persistence.service.job.test.utils.MappingServiceTestUtils;
import org.dswarm.persistence.service.job.test.utils.TransformationServiceTestUtils;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.resource.test.utils.ConfigurationServiceTestUtils;
import org.dswarm.persistence.service.resource.test.utils.DataModelServiceTestUtils;
import org.dswarm.persistence.service.resource.test.utils.ResourceServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.AttributePathServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.AttributeServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.ClaszServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.MappingAttributePathInstanceServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.SchemaServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class ProjectServiceTest extends IDBasicJPAServiceTest<ProxyProject, Project, ProjectService> {

	private static final Logger									LOG								= LoggerFactory.getLogger(ProjectServiceTest.class);

	private final ObjectMapper									objectMapper					= GuicedTest.injector.getInstance(ObjectMapper.class);

	private final Map<Long, Function>							functions						= Maps.newLinkedHashMap();

	private final Map<Long, Attribute>							attributes						= Maps.newLinkedHashMap();

	private final Map<Long, Clasz>								classes							= Maps.newLinkedHashMap();

	private final Map<Long, AttributePath>						attributePaths					= Maps.newLinkedHashMap();

	private final Map<Long, Component>							components						= Maps.newLinkedHashMap();

	private final Map<Long, Transformation>						transformations					= Maps.newLinkedHashMap();

	private final Map<Long, Mapping>							mappings						= Maps.newLinkedHashMap();

	private final Map<Long, Schema>								schemas							= Maps.newLinkedHashMap();

	private final Map<Long, Resource>							resources						= Maps.newLinkedHashMap();

	private final Map<Long, Configuration>						configurations					= Maps.newLinkedHashMap();

	private final Map<Long, MappingAttributePathInstance>		mappingAttributePathInstances	= Maps.newLinkedHashMap();

	private final AttributeServiceTestUtils						attributeServiceTestUtils;
	private final AttributePathServiceTestUtils					attributePathServiceTestUtils;
	private final FunctionServiceTestUtils						functionServiceTestUtils;
	private final MappingAttributePathInstanceServiceTestUtils	mappingAttributePathInstanceServiceTestUtils;
	private final ComponentServiceTestUtils						componentServiceTestUtils;
	private final TransformationServiceTestUtils				transformationServiceTestUtils;
	private final SchemaServiceTestUtils						schemaServiceTestUtils;
	private final ConfigurationServiceTestUtils					configurationServiceTestUtils;
	private final ResourceServiceTestUtils						resourceServiceTestUtils;
	private final ClaszServiceTestUtils							claszServiceTestUtils;
	private final MappingServiceTestUtils						mappingServiceTestUtils;
	private final DataModelServiceTestUtils						dataModelServiceTestUtils;

	public ProjectServiceTest() {

		super("project", ProjectService.class);

		attributeServiceTestUtils = new AttributeServiceTestUtils();
		attributePathServiceTestUtils = new AttributePathServiceTestUtils();
		functionServiceTestUtils = new FunctionServiceTestUtils();
		mappingAttributePathInstanceServiceTestUtils = new MappingAttributePathInstanceServiceTestUtils();
		componentServiceTestUtils = new ComponentServiceTestUtils();
		transformationServiceTestUtils = new TransformationServiceTestUtils();
		claszServiceTestUtils = new ClaszServiceTestUtils();
		schemaServiceTestUtils = new SchemaServiceTestUtils();
		configurationServiceTestUtils = new ConfigurationServiceTestUtils();
		resourceServiceTestUtils = new ResourceServiceTestUtils();
		mappingServiceTestUtils = new MappingServiceTestUtils();
		dataModelServiceTestUtils = new DataModelServiceTestUtils();
	}

	@Test
	public void simpleProjectTest() throws Exception {

		ProjectServiceTest.LOG.debug("start simple project test");

		// mappings

		final Mapping simpleMapping = createMapping();
		final Mapping complexMapping = createComplexMapping();

		final Set<Mapping> mappings = Sets.newLinkedHashSet();
		mappings.add(simpleMapping);
		mappings.add(complexMapping);

		// data models

		final DataModel inputDataModel = createInputDataModel();

		final DataModel outputDataModel = createOutputDataModel();

		// functions

		final LinkedList<String> parameters = Lists.newLinkedList();

		parameters.add("inputString");

		final Function function1 = functionServiceTestUtils.createFunction("trim", "trims leading and trailing whitespaces from a given string",
				parameters);
		functions.put(function1.getId(), function1);

		final Set<Function> functions = Sets.newLinkedHashSet();
		functions.add(function1);

		// project

		final String projectName = "my project";
		final String projectDescription = "my project description";

		final Project project = createObject().getObject();

		project.setName(projectName);
		project.setDescription(projectDescription);
		project.setInputDataModel(inputDataModel);
		project.setOutputDataModel(outputDataModel);
		project.setMappings(mappings);
		project.setFunctions(functions);

		final Project updatedProject = updateObjectTransactional(project).getObject();

		Assert.assertNotNull("the update project shouldn't be null", updatedProject);
		Assert.assertNotNull("the id of the updated project shouldn't be null", updatedProject.getId());
		Assert.assertEquals("the id of the updated project isn't the same as of project", project.getId(), updatedProject.getId());
		Assert.assertNotNull("the name of the updated project shouldn't be null", updatedProject.getName());
		Assert.assertEquals("the name of the updated project isn't the same as of project", project.getName(), updatedProject.getName());
		Assert.assertNotNull("the description of the updated project shouldn't be null", updatedProject.getDescription());
		Assert.assertEquals("the description of the updated project isn't the same as of project", project.getDescription(),
				updatedProject.getDescription());

		// input data model

		Assert.assertNotNull("the input data model of the updated project shouldn't be null", updatedProject.getInputDataModel());
		Assert.assertEquals("the input data model of the updated project isn't the same as of project", project.getInputDataModel(),
				updatedProject.getInputDataModel());
		Assert.assertNotNull("the input data model id of the updated project shouldn't be null", updatedProject.getInputDataModel().getId());
		Assert.assertEquals("the input data model id of the updated project isn't the same as of project", project.getInputDataModel().getId(),
				updatedProject.getInputDataModel().getId());
		Assert.assertNotNull("the input data model name of the updated project shouldn't be null", updatedProject.getInputDataModel().getName());
		Assert.assertEquals("the input data model name of the updated project isn't the same as of project", project.getInputDataModel().getName(),
				updatedProject.getInputDataModel().getName());
		Assert.assertNotNull("the input data model data resource of the updated project shouldn't be null", updatedProject.getInputDataModel()
				.getDataResource());
		Assert.assertEquals("the input data model data resource of the updated project isn't the same as of project", project.getInputDataModel()
				.getDataResource(), updatedProject.getInputDataModel().getDataResource());
		Assert.assertNotNull("the input data model data resource id of the updated project shouldn't be null", updatedProject.getInputDataModel()
				.getDataResource().getId());
		Assert.assertEquals("the input data model data resource id of the updated project isn't the same as of project", project.getInputDataModel()
				.getDataResource().getId(), updatedProject.getInputDataModel().getDataResource().getId());
		Assert.assertNotNull("the input data model data resource name of the updated project shouldn't be null", updatedProject.getInputDataModel()
				.getDataResource().getName());
		Assert.assertEquals("the input data model data resource name of the updated project isn't the same as of project", project
				.getInputDataModel().getDataResource().getName(), updatedProject.getInputDataModel().getDataResource().getName());
		Assert.assertNotNull("the input data model configuration of the updated project shouldn't be null", updatedProject.getInputDataModel()
				.getConfiguration());
		Assert.assertEquals("the input data model configuration of the updated project isn't the same as of project", project.getInputDataModel()
				.getConfiguration(), updatedProject.getInputDataModel().getConfiguration());
		Assert.assertNotNull("the input data model configuration id of the updated project shouldn't be null", updatedProject.getInputDataModel()
				.getConfiguration().getId());
		Assert.assertEquals("the input data model configuration id of the updated project isn't the same as of project", project.getInputDataModel()
				.getConfiguration().getId(), updatedProject.getInputDataModel().getConfiguration().getId());
		Assert.assertNotNull("the input data model configuration name of the updated project shouldn't be null", updatedProject.getInputDataModel()
				.getConfiguration().getName());
		Assert.assertEquals("the input data model configuration name of the updated project isn't the same as of project", project
				.getInputDataModel().getConfiguration().getName(), updatedProject.getInputDataModel().getConfiguration().getName());
		Assert.assertNotNull("the input data model schema of the updated project shouldn't be null", updatedProject.getInputDataModel().getSchema());
		Assert.assertEquals("the input data model schema of the updated project isn't the same as of project", project.getInputDataModel()
				.getSchema(), updatedProject.getInputDataModel().getSchema());
		Assert.assertNotNull("the input data model schema id of the updated project shouldn't be null", updatedProject.getInputDataModel()
				.getSchema().getId());
		Assert.assertEquals("the input data model schema id of the updated project isn't the same as of project", project.getInputDataModel()
				.getSchema().getId(), updatedProject.getInputDataModel().getSchema().getId());
		Assert.assertNotNull("the input data model schema name of the updated project shouldn't be null", updatedProject.getInputDataModel()
				.getSchema().getName());
		Assert.assertEquals("the input data model schema name of the updated project isn't the same as of project", project.getInputDataModel()
				.getSchema().getName(), updatedProject.getInputDataModel().getSchema().getName());

		// output data model

		Assert.assertNotNull("the output data model of the updated project shouldn't be null", updatedProject.getOutputDataModel());
		Assert.assertEquals("the output data model of the updated project isn't the same as of project", project.getOutputDataModel(),
				updatedProject.getOutputDataModel());
		Assert.assertNotNull("the output data model id of the updated project shouldn't be null", updatedProject.getOutputDataModel().getId());
		Assert.assertEquals("the output data model id of the updated project isn't the same as of project", project.getOutputDataModel().getId(),
				updatedProject.getOutputDataModel().getId());
		Assert.assertNotNull("the output data model name of the updated project shouldn't be null", updatedProject.getOutputDataModel().getName());
		Assert.assertEquals("the output data model name of the updated project isn't the same as of project", project.getOutputDataModel().getName(),
				updatedProject.getOutputDataModel().getName());
		Assert.assertNotNull("the output data model schema of the updated project shouldn't be null", updatedProject.getOutputDataModel().getSchema());
		Assert.assertEquals("the output data model schema of the updated project isn't the same as of project", project.getOutputDataModel()
				.getSchema(), updatedProject.getOutputDataModel().getSchema());
		Assert.assertNotNull("the output data model schema id of the updated project shouldn't be null", updatedProject.getOutputDataModel()
				.getSchema().getId());
		Assert.assertEquals("the output data model schema id of the updated project isn't the same as of project", project.getOutputDataModel()
				.getSchema().getId(), updatedProject.getOutputDataModel().getSchema().getId());
		Assert.assertNotNull("the output data model schema name of the updated project shouldn't be null", updatedProject.getOutputDataModel()
				.getSchema().getName());
		Assert.assertEquals("the output data model schema name of the updated project isn't the same as of project", project.getOutputDataModel()
				.getSchema().getName(), updatedProject.getOutputDataModel().getSchema().getName());

		// project mappings

		Assert.assertNotNull("the mappings of the updated project shouldn't be null", updatedProject.getMappings());
		Assert.assertEquals("the size of the mappings collection of the updated project isn't the same as of project", project.getMappings().size(),
				updatedProject.getMappings().size());
		Assert.assertNotNull("the mapping of the updated project shouldn't be null", updatedProject.getMappings().iterator().next());

		final Iterator<Mapping> mappingIter = updatedProject.getMappings().iterator();

		Mapping updatedComplexMapping = null;

		while (mappingIter.hasNext()) {

			final Mapping iterMapping = mappingIter.next();

			if (complexMapping.getId().equals(iterMapping.getId())) {

				updatedComplexMapping = iterMapping;

				break;
			}
		}

		Assert.assertNotNull("the updated complex mapping of the updated project shouldn't be null", updatedComplexMapping);
		Assert.assertNotNull("the mapping name shouldn't be null", updatedComplexMapping.getName());
		Assert.assertEquals("the mapping names are not equal", complexMapping.getName(), updatedComplexMapping.getName());
		Assert.assertNotNull("the mapping transformation component shouldn't be null", updatedComplexMapping.getTransformation());
		Assert.assertEquals("the mapping transformation components are not equal", complexMapping.getTransformation(),
				updatedComplexMapping.getTransformation());
		Assert.assertNotNull("the transformation component id shouldn't be null", updatedComplexMapping.getTransformation().getId());
		Assert.assertEquals("the mapping transformation components' ids are not equal", complexMapping.getTransformation().getId(),
				updatedComplexMapping.getTransformation().getId());
		Assert.assertNotNull("the transformation component name shouldn't be null", updatedComplexMapping.getTransformation().getName());
		Assert.assertEquals("the mapping transformation components' names are not equal", complexMapping.getTransformation().getName(),
				updatedComplexMapping.getTransformation().getName());
		Assert.assertNotNull("the transformation component function shouldn't be null", updatedComplexMapping.getTransformation().getFunction());
		Assert.assertEquals("the mapping transformation components' functions are not equal", complexMapping.getTransformation().getFunction(),
				updatedComplexMapping.getTransformation().getFunction());
		Assert.assertNotNull("the transformation component function id shouldn't be null", updatedComplexMapping.getTransformation().getFunction()
				.getId());
		Assert.assertEquals("the mapping transformation components' functions' ids are not equal", complexMapping.getTransformation().getFunction()
				.getId(), updatedComplexMapping.getTransformation().getFunction().getId());
		Assert.assertNotNull("the transformation component function name shouldn't be null", updatedComplexMapping.getTransformation().getFunction()
				.getName());
		Assert.assertEquals("the mapping transformation components' functions' names are not equal", complexMapping.getTransformation().getFunction()
				.getName(), updatedComplexMapping.getTransformation().getFunction().getName());

		// project functions

		Assert.assertNotNull("the functions of the updated project shouldn't be null", updatedProject.getFunctions());
		Assert.assertEquals("the size of the functions collection of the updated project isn't the same as of project",
				project.getFunctions().size(), updatedProject.getFunctions().size());
		Assert.assertNotNull("the function of the updated project shouldn't be null", updatedProject.getFunctions().iterator().next());
		Assert.assertEquals("the function of the updated project isn't the same as of project", function1, updatedProject.getFunctions().iterator()
				.next());
		Assert.assertEquals("the function id of the updated project isn't the same as of project", function1.getId(), updatedProject.getFunctions()
				.iterator().next().getId());
		Assert.assertEquals("the function name of the updated project isn't the same as of project", function1.getName(), updatedProject
				.getFunctions().iterator().next().getName());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedProject);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ProjectServiceTest.LOG.debug("project json: " + json);

		deleteObject(updatedProject.getId());

		final Transformation transformationFromComplexMapping = (Transformation) complexMapping.getTransformation().getFunction();

		for (final Mapping mapping : this.mappings.values()) {

			mappingServiceTestUtils.deleteObject(mapping);
		}

		dataModelServiceTestUtils.deleteObject(inputDataModel);
		dataModelServiceTestUtils.deleteObject(outputDataModel);

		transformationServiceTestUtils.deleteObject(transformationFromComplexMapping);

		for (final Transformation transformation : transformations.values()) {

			transformationServiceTestUtils.deleteObject(transformation);
		}

		for (final Component component : components.values()) {

			componentServiceTestUtils.checkDeletedComponent(component);
		}

		for (final Function function : this.functions.values()) {

			functionServiceTestUtils.deleteObject(function);
		}

		for (final Schema schema : schemas.values()) {

			schemaServiceTestUtils.deleteObject(schema);
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

		for (final Clasz clasz : classes.values()) {

			claszServiceTestUtils.deleteObject(clasz);
		}

		for (final Resource resource : resources.values()) {

			resourceServiceTestUtils.deleteObject(resource);
		}

		for (final Configuration configuration : configurations.values()) {

			configurationServiceTestUtils.deleteObject(configuration);
		}

		ProjectServiceTest.LOG.debug("end simple project test");
	}

	// /*@Test
	// public void complexMappingTest() {
	//
	// LOG.debug("start complex mapping test");
	//
	// // clean-up
	// deletedObject(updatedMapping.getId());
	//
	// // upper transformation needs to be deleted first, so that the functions/transformations of its components will be
	// // released
	// // note: could maybe improved via bidirectional relationship from component to function and vice versa
	// deleteTransformation(transformation2);
	//
	// for (final Transformation transformationToBeDeleted : transformations.values()) {
	//
	// deleteTransformation(transformationToBeDeleted);
	// }
	//
	// for (final Component componentAlreadyDeleted : this.components.values()) {
	//
	// checkDeletedComponent(componentAlreadyDeleted);
	// }
	//
	// for (final Function functionToDelete : functions.values()) {
	//
	// deleteFunction(functionToDelete);
	// }
	//
	// for (final AttributePath attributePath : attributePaths.values()) {
	//
	// deleteAttributePath(attributePath);
	// }
	//
	// for (final Attribute attribute : attributes.values()) {
	//
	// deleteAttribute(attribute);
	// }
	//
	// LOG.debug("end complex mapping test");
	// }*/

	// private Function createFunction(final String name, final String description, final LinkedList<String> parameters) {
	//
	// final FunctionService functionService = GuicedTest.injector.getInstance(FunctionService.class);
	//
	// Assert.assertNotNull("function service shouldn't be null", functionService);
	//
	// final String functionName = name;
	// final String functionDescription = description;
	//
	// Function function = null;
	//
	// try {
	//
	// function = functionService.createObjectTransactional().getObject();
	// } catch (final DMPPersistenceException e) {
	//
	// Assert.assertTrue("something went wrong while function creation.\n" + e.getMessage(), false);
	// }
	//
	// Assert.assertNotNull("function shouldn't be null", function);
	// Assert.assertNotNull("function id shouldn't be null", function.getId());
	//
	// function.setName(functionName);
	// function.setDescription(functionDescription);
	// function.setParameters(parameters);
	//
	// Function updatedFunction = null;
	//
	// try {
	//
	// updatedFunction = functionService.updateObjectTransactional(function).getObject();
	// } catch (final DMPPersistenceException e) {
	//
	// Assert.assertTrue("something went wrong while updating the function of id = '" + function.getId() + "'", false);
	// }
	//
	// Assert.assertNotNull("function shouldn't be null", updatedFunction);
	// Assert.assertNotNull("the function name shouldn't be null", function.getName());
	// Assert.assertEquals("the function names are not equal", functionName, function.getName());
	// Assert.assertNotNull("the function description shouldn't be null", function.getDescription());
	// Assert.assertEquals("the function descriptions are not equal", functionDescription, function.getDescription());
	// Assert.assertNotNull("the function parameters shouldn't be null", function.getParameters());
	// Assert.assertEquals("the function type is not '" + FunctionType.Function + "'", FunctionType.Function,
	// function.getFunctionType());
	//
	// functions.put(updatedFunction.getId(), updatedFunction);
	//
	// return updatedFunction;
	// }
	//
	// private Component createComponent(final String name, final Map<String, String> parameterMappings, final Function function,
	// final Set<Component> inputComponents, final Set<Component> outputComponents) {
	//
	// final ComponentService componentService = GuicedTest.injector.getInstance(ComponentService.class);
	//
	// Component component = null;
	//
	// try {
	//
	// component = componentService.createObjectTransactional().getObject();
	// } catch (final DMPPersistenceException e) {
	//
	// Assert.assertTrue("something went wrong while component creation.\n" + e.getMessage(), false);
	// }
	//
	// Assert.assertNotNull("component shouldn't be null", component);
	// Assert.assertNotNull("component id shouldn't be null", component.getId());
	//
	// component.setName(name);
	// component.setFunction(function);
	// component.setParameterMappings(parameterMappings);
	//
	// if (inputComponents != null) {
	// component.setInputComponents(inputComponents);
	// }
	//
	// if (outputComponents != null) {
	// component.setOutputComponents(outputComponents);
	// }
	//
	// Component updatedComponent = null;
	//
	// try {
	//
	// updatedComponent = componentService.updateObjectTransactional(component).getObject();
	// } catch (final DMPPersistenceException e) {
	//
	// Assert.assertTrue("something went wrong while updating the component of id = '" + component.getId() + "'", false);
	// }
	//
	// Assert.assertNotNull("the updated component shouldn't be null", updatedComponent);
	// Assert.assertNotNull("the component name shouldn't be null", updatedComponent.getId());
	// Assert.assertNotNull("the component name shouldn't be null", updatedComponent.getName());
	// Assert.assertEquals("the component names are not equal", name, updatedComponent.getName());
	// Assert.assertNotNull("the component parameter mappings shouldn't be null", updatedComponent.getParameterMappings());
	// Assert.assertEquals("the function type is not '" + function.getFunctionType() + "'", function.getFunctionType(),
	// updatedComponent
	// .getFunction().getFunctionType());
	//
	// components.put(updatedComponent.getId(), updatedComponent);
	//
	// return updatedComponent;
	// }
	//
	// private Transformation createTransformation(final String name, final String description, final Set<Component> components,
	// final LinkedList<String> parameters) {
	//
	// final TransformationService transformationService = GuicedTest.injector.getInstance(TransformationService.class);
	//
	// Transformation transformation = null;
	//
	// try {
	//
	// transformation = transformationService.createObjectTransactional().getObject();
	// } catch (final DMPPersistenceException e) {
	//
	// Assert.assertTrue("something went wrong while transformation creation.\n" + e.getMessage(), false);
	// }
	//
	// Assert.assertNotNull("transformation shouldn't be null", transformation);
	// Assert.assertNotNull("transformation id shouldn't be null", transformation.getId());
	//
	// transformation.setName(name);
	// transformation.setDescription(description);
	// transformation.setComponents(components);
	// transformation.setParameters(parameters);
	//
	// Transformation updatedTransformation = null;
	//
	// try {
	//
	// updatedTransformation = transformationService.updateObjectTransactional(transformation).getObject();
	// } catch (final DMPPersistenceException e) {
	//
	// Assert.assertTrue("something went wrong while updating the transformation of id = '" + transformation.getId() + "'",
	// false);
	// }
	//
	// Assert.assertNotNull("the updated component shouldn't be null", updatedTransformation);
	// Assert.assertNotNull("the transformation name shouldn't be null", updatedTransformation.getId());
	// Assert.assertNotNull("the transformation name shouldn't be null", updatedTransformation.getName());
	// Assert.assertEquals("the transformation names are not equal", name, updatedTransformation.getName());
	// Assert.assertNotNull("the transformation parameter mappings shouldn't be null", updatedTransformation.getParameters());
	//
	// transformations.put(updatedTransformation.getId(), updatedTransformation);
	//
	// return updatedTransformation;
	// }

	private Mapping createMapping() throws Exception {

		final MappingService mappingService = GuicedTest.injector.getInstance(MappingService.class);

		// function

		final LinkedList<String> parameters = Lists.newLinkedList();

		parameters.add("inputString");

		final Function function = functionServiceTestUtils.createFunction("trim", "trims leading and trailing whitespaces from a given string",
				parameters);
		functions.put(function.getId(), function);

		final String componentName = "my trim component";
		final Map<String, String> parameterMappings = Maps.newLinkedHashMap();

		final String functionParameterName = "inputString";
		final String componentVariableName = "previousComponent.outputString";

		parameterMappings.put(functionParameterName, componentVariableName);

		final Component component = componentServiceTestUtils.createComponent(componentName, parameterMappings, function, null, null);
		components.put(component.getId(), component);

		// transformation

		final String transformationName = "my transformation";
		final String transformationDescription = "transformation which just makes use of one function";
		final String transformationParameter = "transformationInputString";

		final LinkedList<String> transformationParameters = Lists.newLinkedList();
		transformationParameters.add(transformationParameter);

		final Set<Component> components = Sets.newLinkedHashSet();

		components.add(component);

		final Transformation transformation = transformationServiceTestUtils.createTransformation(transformationName, transformationDescription,
				components, transformationParameters);
		transformations.put(transformation.getId(), transformation);

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
		this.components.put(transformationComponent.getId(), transformationComponent);

		// mapping

		final String mappingName = "my mapping";

		Mapping mapping = null;

		try {

			mapping = mappingService.createObjectTransactional().getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while mapping creation.\n" + e.getMessage(), false);
		}

		mapping.setName(mappingName);
		mapping.addInputAttributePath(inputMappingAttributePathInstance);
		mapping.setOutputAttributePath(outputMappingAttributePathInstance);
		mapping.setTransformation(transformationComponent);

		Mapping updatedMapping = null;

		try {

			updatedMapping = mappingService.updateObjectTransactional(mapping).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the mapping of id = '" + mapping.getId() + "'", false);
		}

		Assert.assertNotNull("the mapping shouldn't be null", updatedMapping);
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
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ProjectServiceTest.LOG.debug("mapping json: " + json);

		mappings.put(updatedMapping.getId(), updatedMapping);

		return updatedMapping;
	}

	private Mapping createComplexMapping() throws Exception {

		final MappingService mappingService = GuicedTest.injector.getInstance(MappingService.class);

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
		components.put(component1.getId(), component1);

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
		components.put(component2.getId(), component2);

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
		components.put(component.getId(), component);

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

		final Transformation transformation = transformationServiceTestUtils.createTransformation(transformationName, transformationDescription,
				components, transformationParameters);
		transformations.put(transformation.getId(), transformation);

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

		final Transformation transformation2 = transformationServiceTestUtils.createTransformation(transformation2Name, transformation2Description,
				components2, transformation2Parameters);
		// (???) transformations.put(transformation2.getId(), transformation2);

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

		Mapping mapping = null;

		try {

			mapping = mappingService.createObjectTransactional().getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while mapping creation.\n" + e.getMessage(), false);
		}

		mapping.setName(mappingName);
		mapping.addInputAttributePath(firstNameMappingAttributePathInstance);
		mapping.addInputAttributePath(familyNameMappingAttributePathInstance);
		mapping.setOutputAttributePath(outputMappingAttributePathInstance);
		mapping.setTransformation(transformationComponent3);

		Mapping updatedMapping = null;

		try {

			updatedMapping = mappingService.updateObjectTransactional(mapping).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the mapping of id = '" + mapping.getId() + "'", false);
		}

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
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ProjectServiceTest.LOG.debug("mapping json: " + json);

		try {

			json = objectMapper.writeValueAsString(transformation2);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ProjectServiceTest.LOG.debug("transformation json: " + json);

		try {

			json = objectMapper.writeValueAsString(transformation);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ProjectServiceTest.LOG.debug("clean-up transformation json: " + json);

		try {

			json = objectMapper.writeValueAsString(component1);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ProjectServiceTest.LOG.debug("clean-up previous component json: " + json);

		try {

			json = objectMapper.writeValueAsString(component);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ProjectServiceTest.LOG.debug("clean-up main component json: " + json);

		try {

			json = objectMapper.writeValueAsString(component2);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ProjectServiceTest.LOG.debug("clean-up next component json: " + json);

		mappings.put(updatedMapping.getId(), updatedMapping);

		return updatedMapping;
	}

	private DataModel createInputDataModel() throws Exception {

		// first attribute path

		final Attribute dctermsTitle = attributeServiceTestUtils.createAttribute("http://purl.org/dc/terms/title", "title");
		attributes.put(dctermsTitle.getId(), dctermsTitle);

		final Attribute dctermsHasPart = attributeServiceTestUtils.createAttribute("http://purl.org/dc/terms/hasPart", "hasPart");
		attributes.put(dctermsHasPart.getId(), dctermsHasPart);

		final LinkedList<Attribute> attributePath1Arg = Lists.newLinkedList();

		attributePath1Arg.add(dctermsTitle);
		attributePath1Arg.add(dctermsHasPart);
		attributePath1Arg.add(dctermsTitle);

		System.out.println("attribute title = '" + dctermsTitle.toString());
		System.out.println("attribute hasPart = '" + dctermsHasPart.toString());

		final AttributePath attributePath1 = attributePathServiceTestUtils.createAttributePath(attributePath1Arg);
		attributePaths.put(attributePath1.getId(), attributePath1);

		// second attribute path

		final String dctermsCreatorId = "http://purl.org/dc/terms/creator";
		final String dctermsCreatorName = "creator";

		final Attribute dctermsCreator = attributeServiceTestUtils.createAttribute(dctermsCreatorId, dctermsCreatorName);
		attributes.put(dctermsCreator.getId(), dctermsCreator);

		final String foafNameId = "http://xmlns.com/foaf/0.1/name";
		final String foafNameName = "name";

		final Attribute foafName = attributeServiceTestUtils.createAttribute(foafNameId, foafNameName);
		attributes.put(foafName.getId(), foafName);

		final LinkedList<Attribute> attributePath2Arg = Lists.newLinkedList();

		attributePath2Arg.add(dctermsCreator);
		attributePath2Arg.add(foafName);

		System.out.println("attribute creator = '" + dctermsCreator.toString());
		System.out.println("attribute name = '" + foafName.toString());

		final AttributePath attributePath2 = attributePathServiceTestUtils.createAttributePath(attributePath2Arg);
		attributePaths.put(attributePath2.getId(), attributePath2);

		// third attribute path

		final String dctermsCreatedId = "http://purl.org/dc/terms/created";
		final String dctermsCreatedName = "created";

		final Attribute dctermsCreated = attributeServiceTestUtils.createAttribute(dctermsCreatedId, dctermsCreatedName);
		attributes.put(dctermsCreated.getId(), dctermsCreated);

		final LinkedList<Attribute> attributePath3Arg = Lists.newLinkedList();

		attributePath3Arg.add(dctermsCreated);

		System.out.println("attribute created = '" + dctermsCreated.toString());

		final AttributePath attributePath3 = attributePathServiceTestUtils.createAttributePath(attributePath3Arg);
		attributePaths.put(attributePath3.getId(), attributePath3);

		// record class

		final String biboDocumentId = "http://purl.org/ontology/bibo/Document";
		final String biboDocumentName = "document";

		final Clasz biboDocument = claszServiceTestUtils.createClass(biboDocumentId, biboDocumentName);
		classes.put(biboDocument.getId(), biboDocument);

		// schema

		final Set<AttributePath> attributePaths = Sets.newLinkedHashSet();

		attributePaths.add(attributePath1);
		attributePaths.add(attributePath2);
		attributePaths.add(attributePath3);

		final Schema schema = schemaServiceTestUtils.createSchema("my schema", attributePaths, biboDocument);
		schemas.put(schema.getId(), schema);

		// configuration

		final ObjectNode parameters = new ObjectNode(objectMapper.getNodeFactory());
		final String parameterKey = "fileseparator";
		final String parameterValue = ";";
		parameters.put(parameterKey, parameterValue);

		final Configuration configuration = configurationServiceTestUtils.createConfiguration("my configuration", "configuration description",
				parameters);
		configurations.put(configuration.getId(), configuration);

		// data resource

		final String attributeKey = "path";
		final String attributeValue = "/path/to/file.end";

		final ObjectNode attributes = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
		attributes.put(attributeKey, attributeValue);

		final Set<Configuration> configurations = Sets.newLinkedHashSet();
		configurations.add(configuration);

		final Resource resource = resourceServiceTestUtils.createResource("bla", "blubblub", ResourceType.FILE, attributes, configurations);
		resources.put(resource.getId(), resource);

		// data model

		final DataModelService dataModelService = GuicedTest.injector.getInstance(DataModelService.class);

		DataModel dataModel = null;

		try {

			dataModel = dataModelService.createObjectTransactional().getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while data model creation.\n" + e.getMessage(), false);
		}

		final String dataModelName = "my data model";
		final String dataModelDescription = "my data model description";

		dataModel.setName(dataModelName);
		dataModel.setDescription(dataModelDescription);
		dataModel.setDataResource(resource);
		dataModel.setConfiguration(configuration);
		dataModel.setSchema(schema);

		DataModel updatedDataModel = null;

		try {

			updatedDataModel = dataModelService.updateObjectTransactional(dataModel).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the data model of id = '" + dataModel.getId() + "'", false);
		}

		Assert.assertNotNull("the updated data model shouldn't be null", updatedDataModel);
		Assert.assertNotNull("the update data model id shouldn't be null", updatedDataModel.getId());
		Assert.assertNotNull("the schema of the updated data model shouldn't be null", updatedDataModel.getSchema());
		Assert.assertNotNull("the schema's attribute paths of the updated schema shouldn't be null", updatedDataModel.getSchema().getAttributePaths());
		Assert.assertEquals("the schema's attribute paths size are not equal", schema.getAttributePaths(), updatedDataModel.getSchema()
				.getAttributePaths());
		Assert.assertEquals("the attribute path '" + attributePath1.getId() + "' of the schema are not equal",
				schema.getAttributePath(attributePath1.getId()), updatedDataModel.getSchema().getAttributePath(attributePath1.getId()));
		Assert.assertNotNull("the attribute path's attributes of the attribute path '" + attributePath1.getId()
				+ "' of the updated schema shouldn't be null", updatedDataModel.getSchema().getAttributePath(attributePath1.getId()).getAttributes());
		Assert.assertEquals("the attribute path's attributes size of attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.getAttributes(), updatedDataModel.getSchema().getAttributePath(attributePath1.getId()).getAttributes());
		Assert.assertEquals("the first attributes of attribute path '" + attributePath1.getId() + "' are not equal", attributePath1
				.getAttributePath().get(0), updatedDataModel.getSchema().getAttributePath(attributePath1.getId()).getAttributePath().get(0));
		Assert.assertNotNull("the attribute path string of attribute path '" + attributePath1.getId() + "' of the update schema shouldn't be null",
				updatedDataModel.getSchema().getAttributePath(attributePath1.getId()).toAttributePath());
		Assert.assertEquals("the attribute path's strings attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.toAttributePath(), updatedDataModel.getSchema().getAttributePath(attributePath1.getId()).toAttributePath());
		Assert.assertNotNull("the record class of the updated schema shouldn't be null", updatedDataModel.getSchema().getRecordClass());
		Assert.assertEquals("the recod classes are not equal", schema.getRecordClass(), updatedDataModel.getSchema().getRecordClass());
		Assert.assertNotNull("the resource of the updated data model shouddn't be null", updatedDataModel.getDataResource());

		resourceServiceTestUtils.checkSimpleResource(resource, updatedDataModel.getDataResource(), attributeKey, attributeValue);
		resourceServiceTestUtils.checkComplexResource(resource, updatedDataModel.getDataResource());
		resourceServiceTestUtils.checkComplexResource(resource, updatedDataModel.getDataResource(), parameterKey, parameterValue);

		Assert.assertNotNull("the configuration of the updated data model shouldn't be null", updatedDataModel.getConfiguration());
		Assert.assertNotNull("the configuration name of the updated resource shouldn't be null", updatedDataModel.getConfiguration().getName());
		Assert.assertEquals("the configuration' names of the resource are not equal", configuration.getName(), updatedDataModel.getConfiguration()
				.getName());
		Assert.assertNotNull("the configuration description of the updated resource shouldn't be null", updatedDataModel.getConfiguration()
				.getDescription());
		Assert.assertEquals("the configuration descriptions of the resource are not equal", configuration.getDescription(), updatedDataModel
				.getConfiguration().getDescription());
		Assert.assertNotNull("the configuration parameters of the updated resource shouldn't be null", updatedDataModel.getConfiguration()
				.getParameters());
		Assert.assertEquals("the configurations parameters of the resource are not equal", configuration.getParameters(), updatedDataModel
				.getConfiguration().getParameters());
		Assert.assertNotNull("the parameter value shouldn't be null", configuration.getParameter(parameterKey));
		Assert.assertEquals("the parameter value should be equal", configuration.getParameter(parameterKey).asText(), parameterValue);

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedDataModel);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ProjectServiceTest.LOG.debug("data model json: " + json);

		return updatedDataModel;
	}

	private DataModel createOutputDataModel() throws Exception {

		// first attribute path

		final Attribute dctermsTitle = attributeServiceTestUtils.createAttribute("http://purl.org/dc/terms/title", "title");
		attributes.put(dctermsTitle.getId(), dctermsTitle);

		final Attribute dctermsHasPart = attributeServiceTestUtils.createAttribute("http://purl.org/dc/terms/hasPart", "hasPart");
		attributes.put(dctermsHasPart.getId(), dctermsHasPart);

		final LinkedList<Attribute> attributePath1Arg = Lists.newLinkedList();

		attributePath1Arg.add(dctermsTitle);
		attributePath1Arg.add(dctermsHasPart);
		attributePath1Arg.add(dctermsTitle);

		System.out.println("attribute title = '" + dctermsTitle.toString());
		System.out.println("attribute hasPart = '" + dctermsHasPart.toString());

		final AttributePath attributePath1 = attributePathServiceTestUtils.createAttributePath(attributePath1Arg);
		attributePaths.put(attributePath1.getId(), attributePath1);

		// second attribute path

		final String dctermsCreatorId = "http://purl.org/dc/terms/creator";
		final String dctermsCreatorName = "creator";

		final Attribute dctermsCreator = attributeServiceTestUtils.createAttribute(dctermsCreatorId, dctermsCreatorName);
		attributes.put(dctermsCreator.getId(), dctermsCreator);

		final String foafNameId = "http://xmlns.com/foaf/0.1/name";
		final String foafNameName = "name";

		final Attribute foafName = attributeServiceTestUtils.createAttribute(foafNameId, foafNameName);
		attributes.put(foafName.getId(), foafName);

		final LinkedList<Attribute> attributePath2Arg = Lists.newLinkedList();

		attributePath2Arg.add(dctermsCreator);
		attributePath2Arg.add(foafName);

		System.out.println("attribute creator = '" + dctermsCreator.toString());
		System.out.println("attribute name = '" + foafName.toString());

		final AttributePath attributePath2 = attributePathServiceTestUtils.createAttributePath(attributePath2Arg);
		attributePaths.put(attributePath2.getId(), attributePath2);

		// third attribute path

		final String dctermsCreatedId = "http://purl.org/dc/terms/created";
		final String dctermsCreatedName = "created";

		final Attribute dctermsCreated = attributeServiceTestUtils.createAttribute(dctermsCreatedId, dctermsCreatedName);
		attributes.put(dctermsCreated.getId(), dctermsCreated);

		final LinkedList<Attribute> attributePath3Arg = Lists.newLinkedList();

		attributePath3Arg.add(dctermsCreated);

		System.out.println("attribute created = '" + dctermsCreated.toString());

		final AttributePath attributePath3 = attributePathServiceTestUtils.createAttributePath(attributePath3Arg);
		attributePaths.put(attributePath3.getId(), attributePath3);

		// record class

		final String biboDocumentId = "http://purl.org/ontology/bibo/Document";
		final String biboDocumentName = "document";

		final Clasz biboDocument = claszServiceTestUtils.createClass(biboDocumentId, biboDocumentName);
		classes.put(biboDocument.getId(), biboDocument);

		// schema

		final Set<AttributePath> attributePaths = Sets.newLinkedHashSet();

		attributePaths.add(attributePath1);
		attributePaths.add(attributePath2);
		attributePaths.add(attributePath3);

		final Schema schema = schemaServiceTestUtils.createSchema("my schema", attributePaths, biboDocument);
		schemas.put(schema.getId(), schema);

		// data model

		final DataModelService dataModelService = GuicedTest.injector.getInstance(DataModelService.class);

		DataModel dataModel = null;

		try {

			dataModel = dataModelService.createObjectTransactional().getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while data model creation.\n" + e.getMessage(), false);
		}

		final String dataModelName = "my output data model";
		final String dataModelDescription = "my output data model description";

		dataModel.setName(dataModelName);
		dataModel.setDescription(dataModelDescription);
		dataModel.setSchema(schema);

		DataModel updatedDataModel = null;

		try {

			updatedDataModel = dataModelService.updateObjectTransactional(dataModel).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the data model of id = '" + dataModel.getId() + "'", false);
		}

		Assert.assertNotNull("the updated data model shouldn't be null", updatedDataModel);
		Assert.assertNotNull("the update data model id shouldn't be null", updatedDataModel.getId());
		Assert.assertNotNull("the schema of the updated data model shouldn't be null", updatedDataModel.getSchema());
		Assert.assertNotNull("the schema's attribute paths of the updated schema shouldn't be null", updatedDataModel.getSchema().getAttributePaths());
		Assert.assertEquals("the schema's attribute paths size are not equal", schema.getAttributePaths(), updatedDataModel.getSchema()
				.getAttributePaths());
		Assert.assertEquals("the attribute path '" + attributePath1.getId() + "' of the schema are not equal",
				schema.getAttributePath(attributePath1.getId()), updatedDataModel.getSchema().getAttributePath(attributePath1.getId()));
		Assert.assertNotNull("the attribute path's attributes of the attribute path '" + attributePath1.getId()
				+ "' of the updated schema shouldn't be null", updatedDataModel.getSchema().getAttributePath(attributePath1.getId()).getAttributes());
		Assert.assertEquals("the attribute path's attributes size of attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.getAttributes(), updatedDataModel.getSchema().getAttributePath(attributePath1.getId()).getAttributes());
		Assert.assertEquals("the first attributes of attribute path '" + attributePath1.getId() + "' are not equal", attributePath1
				.getAttributePath().get(0), updatedDataModel.getSchema().getAttributePath(attributePath1.getId()).getAttributePath().get(0));
		Assert.assertNotNull("the attribute path string of attribute path '" + attributePath1.getId() + "' of the update schema shouldn't be null",
				updatedDataModel.getSchema().getAttributePath(attributePath1.getId()).toAttributePath());
		Assert.assertEquals("the attribute path's strings attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.toAttributePath(), updatedDataModel.getSchema().getAttributePath(attributePath1.getId()).toAttributePath());
		Assert.assertNotNull("the record class of the updated schema shouldn't be null", updatedDataModel.getSchema().getRecordClass());
		Assert.assertEquals("the recod classes are not equal", schema.getRecordClass(), updatedDataModel.getSchema().getRecordClass());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedDataModel);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ProjectServiceTest.LOG.debug("data model json: " + json);

		return updatedDataModel;
	}
}
