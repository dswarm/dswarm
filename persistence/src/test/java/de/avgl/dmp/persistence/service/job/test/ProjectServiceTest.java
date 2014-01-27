package de.avgl.dmp.persistence.service.job.test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.FunctionType;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.model.job.Project;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.job.proxy.ProxyProject;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.job.ComponentService;
import de.avgl.dmp.persistence.service.job.FunctionService;
import de.avgl.dmp.persistence.service.job.MappingService;
import de.avgl.dmp.persistence.service.job.ProjectService;
import de.avgl.dmp.persistence.service.job.TransformationService;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.resource.ResourceService;
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.service.schema.AttributeService;
import de.avgl.dmp.persistence.service.schema.ClaszService;
import de.avgl.dmp.persistence.service.schema.SchemaService;
import de.avgl.dmp.persistence.service.test.IDBasicJPAServiceTest;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class ProjectServiceTest extends IDBasicJPAServiceTest<ProxyProject, Project, ProjectService> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(ProjectServiceTest.class);

	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private final Map<Long, Function>				functions		= Maps.newLinkedHashMap();

	private Map<Long, Attribute>					attributes		= Maps.newLinkedHashMap();

	private Map<Long, Clasz>						classes			= Maps.newLinkedHashMap();

	private Map<Long, AttributePath>				attributePaths	= Maps.newLinkedHashMap();

	private Map<Long, Component>					components		= Maps.newLinkedHashMap();

	private Map<Long, Transformation>				transformations	= Maps.newLinkedHashMap();

	private Map<Long, Mapping>						mappings		= Maps.newLinkedHashMap();

	private Map<Long, Schema>						schemas			= Maps.newLinkedHashMap();

	private Map<Long, Resource>						resources		= Maps.newLinkedHashMap();

	private Map<Long, Configuration>				configurations	= Maps.newLinkedHashMap();

	public ProjectServiceTest() {

		super("project", ProjectService.class);
	}

	@Test
	public void simpleProjectTest() {

		LOG.debug("start simple project test");

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

		final Function function1 = createFunction("trim", "trims leading and trailing whitespaces from a given string", parameters);

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
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("project json: " + json);

		deletedObject(updatedProject.getId());

		final Transformation transformationFromComplexMapping = (Transformation) complexMapping.getTransformation().getFunction();

		for (final Mapping mapping : this.mappings.values()) {

			deleteMapping(mapping);
		}

		deleteDataModel(inputDataModel);
		deleteDataModel(outputDataModel);

		deleteTransformation(transformationFromComplexMapping);

		for (final Transformation transformation : transformations.values()) {

			deleteTransformation(transformation);
		}

		for (final Component component : components.values()) {

			checkDeletedComponent(component);
		}

		for (final Function function : this.functions.values()) {

			deleteFunction(function);
		}

		for (final Schema schema : schemas.values()) {

			deleteSchema(schema);
		}

		for (final AttributePath attributePath : attributePaths.values()) {

			deleteAttributePath(attributePath);
		}

		for (final Attribute attribute : attributes.values()) {

			deleteAttribute(attribute);
		}

		for (final Clasz clasz : classes.values()) {

			deleteClasz(clasz);
		}

		for (final Resource resource : resources.values()) {

			deleteResource(resource);
		}

		for (final Configuration configuration : configurations.values()) {

			deleteConfiguration(configuration);
		}

		LOG.debug("end simple project test");
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

	private Function createFunction(final String name, final String description, final LinkedList<String> parameters) {

		final FunctionService functionService = GuicedTest.injector.getInstance(FunctionService.class);

		Assert.assertNotNull("function service shouldn't be null", functionService);

		final String functionName = name;
		final String functionDescription = description;

		Function function = null;

		try {

			function = functionService.createObject().getObject();
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

			updatedFunction = functionService.updateObjectTransactional(function).getObject();
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

	private Component createComponent(final String name, final Map<String, String> parameterMappings, final Function function,
			final Set<Component> inputComponents, final Set<Component> outputComponents) {

		final ComponentService componentService = GuicedTest.injector.getInstance(ComponentService.class);

		Component component = null;

		try {

			component = componentService.createObject().getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while component creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("component shouldn't be null", component);
		Assert.assertNotNull("component id shouldn't be null", component.getId());

		component.setName(name);
		component.setFunction(function);
		component.setParameterMappings(parameterMappings);

		if (inputComponents != null) {
			component.setInputComponents(inputComponents);
		}

		if (outputComponents != null) {
			component.setOutputComponents(outputComponents);
		}

		Component updatedComponent = null;

		try {

			updatedComponent = componentService.updateObjectTransactional(component).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the component of id = '" + component.getId() + "'", false);
		}

		Assert.assertNotNull("the updated component shouldn't be null", updatedComponent);
		Assert.assertNotNull("the component name shouldn't be null", updatedComponent.getId());
		Assert.assertNotNull("the component name shouldn't be null", updatedComponent.getName());
		Assert.assertEquals("the component names are not equal", name, updatedComponent.getName());
		Assert.assertNotNull("the component parameter mappings shouldn't be null", updatedComponent.getParameterMappings());
		Assert.assertEquals("the function type is not '" + function.getFunctionType() + "'", function.getFunctionType(), updatedComponent
				.getFunction().getFunctionType());

		components.put(updatedComponent.getId(), updatedComponent);

		return updatedComponent;
	}

	private Transformation createTransformation(final String name, final String description, final Set<Component> components,
			final LinkedList<String> parameters) {

		final TransformationService transformationService = GuicedTest.injector.getInstance(TransformationService.class);

		Transformation transformation = null;

		try {

			transformation = transformationService.createObject().getObject();
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

	private Mapping createMapping() {

		final MappingService mappingService = GuicedTest.injector.getInstance(MappingService.class);

		// function

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

		Mapping mapping = null;

		try {

			mapping = mappingService.createObject().getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while mapping creation.\n" + e.getMessage(), false);
		}

		mapping.setName(mappingName);
		mapping.addInputAttributePath(inputAttributePath);
		mapping.setOutputAttributePath(outputAttributePath);
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
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("mapping json: " + json);

		mappings.put(updatedMapping.getId(), updatedMapping);

		return updatedMapping;
	}

	private Mapping createComplexMapping() {

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

		final Component component1 = createComponent(component1Name, parameterMapping1, function1, null, null);

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

		final Component component2 = createComponent(component2Name, parameterMapping2, function2, null, null);

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

		final Component component = createComponent(componentName, parameterMapping, function, inputComponents, outputComponents);

		// transformation

		final String transformationName = "my transformation";
		final String transformationDescription = "transformation which makes use of three functions";
		final String transformationParameter = "transformationInputString";

		final Set<Component> components = Sets.newLinkedHashSet();

		components.add(component1);
		components.add(component);
		components.add(component2);

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

		final Component transformationComponent = createComponent(transformationComponentName, transformationComponentParameterMappings,
				transformation, null, null);

		// transformation component 2 (in main transformation) -> clean family name

		final Map<String, String> transformationComponentParameterMappings2 = Maps.newLinkedHashMap();

		transformationComponentParameterMappings2.put("transformationInputString", "familyName");

		final Component transformationComponent2 = createComponent("prepare family name", transformationComponentParameterMappings2, transformation,
				null, null);

		// concat component -> full name

		final String function4Name = "concat";
		final String function4Description = "concatenates two given string";
		final String function5Parameter = "firstString";
		final String function6Parameter = "secondString";

		final LinkedList<String> function4Parameters = Lists.newLinkedList();
		function4Parameters.add(function5Parameter);
		function4Parameters.add(function6Parameter);

		final Function function4 = createFunction(function4Name, function4Description, function4Parameters);

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

		final Component component4 = createComponent(component4Name, parameterMapping4, function4, component4InputComponents, null);

		// transformation 2

		final String transformation2Name = "my transformation 2";
		final String transformation2Description = "transformation which makes use of three functions (two transformations and one funcion)";
		final String transformation2Parameter = "firstName";
		final String transformation2Parameter2 = "familyName";

		final Set<Component> components2 = Sets.newLinkedHashSet();

		components2.add(transformationComponent);
		components2.add(transformationComponent2);
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

		final Attribute dctermsCreator = createAttribute(dctermsCreatorId, dctermsCreatorName);

		// first name attribute path

		final String firstNameId = "http://xmlns.com/foaf/0.1/firstName";
		final String firstNameName = "firstName";

		final Attribute firstName = createAttribute(firstNameId, firstNameName);

		final LinkedList<Attribute> firstNameAttributePathList = Lists.newLinkedList();
		firstNameAttributePathList.add(dctermsCreator);
		firstNameAttributePathList.add(firstName);

		final AttributePath firstNameAttributePath = createAttributePath(firstNameAttributePathList);

		// family name attribute path

		final String familyNameId = "http://xmlns.com/foaf/0.1/familyName";
		final String familyNameName = "familyName";

		final Attribute familyName = createAttribute(familyNameId, familyNameName);

		final LinkedList<Attribute> familyNameAttributePathList = Lists.newLinkedList();
		familyNameAttributePathList.add(dctermsCreator);
		familyNameAttributePathList.add(familyName);

		final AttributePath familyNameAttributePath = createAttributePath(familyNameAttributePathList);

		// output attribute path

		final String foafNameId = "http://xmlns.com/foaf/0.1/name";
		final String foafNameName = "name";

		final Attribute foafName = createAttribute(foafNameId, foafNameName);

		final LinkedList<Attribute> nameAttributePathList = Lists.newLinkedList();
		nameAttributePathList.add(dctermsCreator);
		nameAttributePathList.add(foafName);

		final AttributePath nameAttributePath = createAttributePath(nameAttributePathList);

		// transformation component

		final Map<String, String> transformationComponent3ParameterMappings = Maps.newLinkedHashMap();

		transformationComponent3ParameterMappings.put(transformation2Parameter, firstNameAttributePath.toAttributePath());
		transformationComponent3ParameterMappings.put(transformation2Parameter2, familyNameAttributePath.toAttributePath());
		transformationComponent3ParameterMappings.put("transformationOutputVariable", nameAttributePath.toAttributePath());

		final Component transformationComponent3 = createComponent(transformation2.getName() + " (component)",
				transformationComponent3ParameterMappings, transformation2, null, null);

		// mapping

		final String mappingName = "my mapping";

		Mapping mapping = null;

		try {

			mapping = mappingService.createObject().getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while mapping creation.\n" + e.getMessage(), false);
		}

		mapping.setName(mappingName);
		mapping.addInputAttributePath(firstNameAttributePath);
		mapping.addInputAttributePath(familyNameAttributePath);
		mapping.setOutputAttributePath(nameAttributePath);
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

		mappings.put(updatedMapping.getId(), updatedMapping);

		return updatedMapping;
	}

	private DataModel createInputDataModel() {

		// first attribute path

		final Attribute dctermsTitle = createAttribute("http://purl.org/dc/terms/title", "title");

		final Attribute dctermsHasPart = createAttribute("http://purl.org/dc/terms/hasPart", "hasPart");

		final LinkedList<Attribute> attributePath1Arg = Lists.newLinkedList();

		attributePath1Arg.add(dctermsTitle);
		attributePath1Arg.add(dctermsHasPart);
		attributePath1Arg.add(dctermsTitle);

		System.out.println("attribute title = '" + dctermsTitle.toString());
		System.out.println("attribute hasPart = '" + dctermsHasPart.toString());

		final AttributePath attributePath1 = createAttributePath(attributePath1Arg);

		// second attribute path

		final String dctermsCreatorId = "http://purl.org/dc/terms/creator";
		final String dctermsCreatorName = "creator";

		final Attribute dctermsCreator = createAttribute(dctermsCreatorId, dctermsCreatorName);

		final String foafNameId = "http://xmlns.com/foaf/0.1/name";
		final String foafNameName = "name";

		final Attribute foafName = createAttribute(foafNameId, foafNameName);

		final LinkedList<Attribute> attributePath2Arg = Lists.newLinkedList();

		attributePath2Arg.add(dctermsCreator);
		attributePath2Arg.add(foafName);

		System.out.println("attribute creator = '" + dctermsCreator.toString());
		System.out.println("attribute name = '" + foafName.toString());

		final AttributePath attributePath2 = createAttributePath(attributePath2Arg);

		// third attribute path

		final String dctermsCreatedId = "http://purl.org/dc/terms/created";
		final String dctermsCreatedName = "created";

		final Attribute dctermsCreated = createAttribute(dctermsCreatedId, dctermsCreatedName);

		final LinkedList<Attribute> attributePath3Arg = Lists.newLinkedList();

		attributePath3Arg.add(dctermsCreated);

		System.out.println("attribute created = '" + dctermsCreated.toString());

		final AttributePath attributePath3 = createAttributePath(attributePath3Arg);

		// record class

		final String biboDocumentId = "http://purl.org/ontology/bibo/Document";
		final String biboDocumentName = "document";

		final Clasz biboDocument = createClass(biboDocumentId, biboDocumentName);

		// schema

		final Set<AttributePath> attributePaths = Sets.newLinkedHashSet();

		attributePaths.add(attributePath1);
		attributePaths.add(attributePath2);
		attributePaths.add(attributePath3);

		final Schema schema = createSchema("my schema", attributePaths, biboDocument);

		// configuration

		final ObjectNode parameters = new ObjectNode(objectMapper.getNodeFactory());
		final String parameterKey = "fileseparator";
		final String parameterValue = ";";
		parameters.put(parameterKey, parameterValue);

		final Configuration configuration = createConfiguration("my configuration", "configuration description", parameters);

		// data resource

		final String attributeKey = "path";
		final String attributeValue = "/path/to/file.end";

		final ObjectNode attributes = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
		attributes.put(attributeKey, attributeValue);

		final Set<Configuration> configurations = Sets.newLinkedHashSet();
		configurations.add(configuration);

		final Resource resource = createResource("bla", "blubblub", ResourceType.FILE, attributes, configurations);

		// data model

		final DataModelService dataModelService = GuicedTest.injector.getInstance(DataModelService.class);

		DataModel dataModel = null;

		try {

			dataModel = dataModelService.createObject().getObject();
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

		checkSimpleResource(resource, updatedDataModel.getDataResource(), attributeKey, attributeValue);
		checkComplexResource(resource, updatedDataModel.getDataResource());
		checkComplexResource(resource, updatedDataModel.getDataResource(), parameterKey, parameterValue);

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
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("data model json: " + json);

		return updatedDataModel;
	}

	private DataModel createOutputDataModel() {

		// first attribute path

		final Attribute dctermsTitle = createAttribute("http://purl.org/dc/terms/title", "title");

		final Attribute dctermsHasPart = createAttribute("http://purl.org/dc/terms/hasPart", "hasPart");

		final LinkedList<Attribute> attributePath1Arg = Lists.newLinkedList();

		attributePath1Arg.add(dctermsTitle);
		attributePath1Arg.add(dctermsHasPart);
		attributePath1Arg.add(dctermsTitle);

		System.out.println("attribute title = '" + dctermsTitle.toString());
		System.out.println("attribute hasPart = '" + dctermsHasPart.toString());

		final AttributePath attributePath1 = createAttributePath(attributePath1Arg);

		// second attribute path

		final String dctermsCreatorId = "http://purl.org/dc/terms/creator";
		final String dctermsCreatorName = "creator";

		final Attribute dctermsCreator = createAttribute(dctermsCreatorId, dctermsCreatorName);

		final String foafNameId = "http://xmlns.com/foaf/0.1/name";
		final String foafNameName = "name";

		final Attribute foafName = createAttribute(foafNameId, foafNameName);

		final LinkedList<Attribute> attributePath2Arg = Lists.newLinkedList();

		attributePath2Arg.add(dctermsCreator);
		attributePath2Arg.add(foafName);

		System.out.println("attribute creator = '" + dctermsCreator.toString());
		System.out.println("attribute name = '" + foafName.toString());

		final AttributePath attributePath2 = createAttributePath(attributePath2Arg);

		// third attribute path

		final String dctermsCreatedId = "http://purl.org/dc/terms/created";
		final String dctermsCreatedName = "created";

		final Attribute dctermsCreated = createAttribute(dctermsCreatedId, dctermsCreatedName);

		final LinkedList<Attribute> attributePath3Arg = Lists.newLinkedList();

		attributePath3Arg.add(dctermsCreated);

		System.out.println("attribute created = '" + dctermsCreated.toString());

		final AttributePath attributePath3 = createAttributePath(attributePath3Arg);

		// record class

		final String biboDocumentId = "http://purl.org/ontology/bibo/Document";
		final String biboDocumentName = "document";

		final Clasz biboDocument = createClass(biboDocumentId, biboDocumentName);

		// schema

		final Set<AttributePath> attributePaths = Sets.newLinkedHashSet();

		attributePaths.add(attributePath1);
		attributePaths.add(attributePath2);
		attributePaths.add(attributePath3);

		final Schema schema = createSchema("my schema", attributePaths, biboDocument);

		// data model

		final DataModelService dataModelService = GuicedTest.injector.getInstance(DataModelService.class);

		DataModel dataModel = null;

		try {

			dataModel = dataModelService.createObject().getObject();
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
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("data model json: " + json);

		return updatedDataModel;
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

	private void deleteAttributePath(final AttributePath attributePath) {

		final AttributePathService attributePathService = GuicedTest.injector.getInstance(AttributePathService.class);

		Assert.assertNotNull("attribute path service shouldn't be null", attributePathService);

		final Long attributePathId = attributePath.getId();

		attributePathService.deleteObject(attributePathId);

		final AttributePath deletedAttributePath = attributePathService.getObject(attributePathId);

		Assert.assertNull("deleted attribute path shouldn't exist any more", deletedAttributePath);
	}

	private void deleteAttribute(final Attribute attribute) {

		final AttributeService attributeService = GuicedTest.injector.getInstance(AttributeService.class);

		Assert.assertNotNull("attribute service shouldn't be null", attributeService);

		final Long attributeId = attribute.getId();

		attributeService.deleteObject(attributeId);

		final Attribute deletedAttribute = attributeService.getObject(attributeId);

		Assert.assertNull("deleted attribute shouldn't exist any more", deletedAttribute);
	}

	private void deleteMapping(final Mapping mapping) {

		final MappingService mappingService = GuicedTest.injector.getInstance(MappingService.class);

		Assert.assertNotNull("mapping service shouldn't be null", mappingService);

		final Long mappingId = mapping.getId();

		mappingService.deleteObject(mappingId);

		final Mapping deletedMapping = mappingService.getObject(mappingId);

		Assert.assertNull("deleted mapping shouldn't exist any more", deletedMapping);
	}

	private void deleteDataModel(final DataModel dataModel) {

		final DataModelService dataModelService = GuicedTest.injector.getInstance(DataModelService.class);

		Assert.assertNotNull("data model service shouldn't be null", dataModelService);

		final Long dataModelId = dataModel.getId();

		dataModelService.deleteObject(dataModelId);

		final DataModel deletedDataModel = dataModelService.getObject(dataModelId);

		Assert.assertNull("deleted data model shouldn't exist any more", deletedDataModel);
	}

	private void deleteClasz(final Clasz clasz) {

		final ClaszService claszService = GuicedTest.injector.getInstance(ClaszService.class);

		Assert.assertNotNull("class service shouldn't be null", claszService);

		final Long claszId = clasz.getId();

		claszService.deleteObject(claszId);

		final Clasz deletedClass = claszService.getObject(claszId);

		Assert.assertNull("deleted class shouldn't exist any more", deletedClass);
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

		final AttributePath attributePath = new AttributePath(attributePathArg);

		AttributePath updatedAttributePath = null;

		try {

			updatedAttributePath = attributePathService.createObject(attributePathArg);
		} catch (final DMPPersistenceException e1) {

			Assert.assertTrue("something went wrong while attribute path creation.\n" + e1.getMessage(), false);
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

		LOG.debug("attribute path json for attribute path '" + updatedAttributePath.getId() + "': " + json);

		attributePaths.put(updatedAttributePath.getId(), updatedAttributePath);

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
			attribute = attributeService.createObjectTransactional(id);
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while attribute creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("attribute shouldn't be null", attribute);
		Assert.assertNotNull("attribute id shouldn't be null", attribute.getId());

		attribute.setName(name);

		Attribute updatedAttribute = null;

		try {

			updatedAttribute = attributeService.updateObjectTransactional(attribute).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the attribute of id = '" + id + "'", false);
		}

		Assert.assertNotNull("updated attribute shouldn't be null", updatedAttribute);
		Assert.assertNotNull("updated attribute id shouldn't be null", updatedAttribute.getId());
		Assert.assertNotNull("updated attribute name shouldn't be null", updatedAttribute.getName());

		attributes.put(updatedAttribute.getId(), updatedAttribute);

		return updatedAttribute;
	}

	private Clasz createClass(final String id, final String name) {

		if (classes.containsKey(id)) {

			return classes.get(id);
		}

		final ClaszService classService = GuicedTest.injector.getInstance(ClaszService.class);

		Assert.assertNotNull("class service shouldn't be null", classService);

		// create class

		Clasz clasz = null;

		try {
			clasz = classService.createObjectTransactional(id);
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while class creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("class shouldn't be null", clasz);
		Assert.assertNotNull("class id shouldn't be null", clasz.getId());

		clasz.setName(name);

		Clasz updatedClasz = null;

		try {

			updatedClasz = classService.updateObjectTransactional(clasz).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the class of id = '" + id + "'", false);
		}

		Assert.assertNotNull("updated class shouldn't be null", updatedClasz);
		Assert.assertNotNull("updated class id shouldn't be null", updatedClasz.getId());
		Assert.assertNotNull("updated class name shouldn't be null", updatedClasz.getName());

		classes.put(updatedClasz.getId(), updatedClasz);

		return updatedClasz;
	}

	private Schema createSchema(final String name, final Set<AttributePath> attributePaths, final Clasz recordClass) {

		final SchemaService schemaService = GuicedTest.injector.getInstance(SchemaService.class);

		Assert.assertNotNull("schema service shouldn't be null", schemaService);

		// create schema

		Schema schema = null;

		try {
			schema = schemaService.createObject().getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while schema creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("schema shouldn't be null", schema);
		Assert.assertNotNull("schema id shouldn't be null", schema.getId());

		schema.setName(name);
		schema.setAttributePaths(attributePaths);
		schema.setRecordClass(recordClass);

		// update schema

		Schema updatedSchema = null;

		try {

			updatedSchema = schemaService.updateObjectTransactional(schema).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the schema of id = '" + schema.getId() + "'", false);
		}

		Assert.assertNotNull("updated schema shouldn't be null", updatedSchema);
		Assert.assertNotNull("updated schema id shouldn't be null", updatedSchema.getId());

		final AttributePath attributePath1 = attributePaths.iterator().next();

		Assert.assertNotNull("the schema's attribute paths of the updated schema shouldn't be null", updatedSchema.getAttributePaths());
		Assert.assertEquals("the schema's attribute paths size are not equal", schema.getAttributePaths(), updatedSchema.getAttributePaths());
		Assert.assertEquals("the attribute path '" + attributePath1.getId() + "' of the schema are not equal",
				schema.getAttributePath(attributePath1.getId()), updatedSchema.getAttributePath(attributePath1.getId()));
		Assert.assertNotNull("the attribute path's attributes of the attribute path '" + attributePath1.getId()
				+ "' of the updated schema shouldn't be null", updatedSchema.getAttributePath(attributePath1.getId()).getAttributes());
		Assert.assertEquals("the attribute path's attributes size of attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.getAttributes(), updatedSchema.getAttributePath(attributePath1.getId()).getAttributes());
		Assert.assertEquals("the first attributes of attribute path '" + attributePath1.getId() + "' are not equal", attributePath1
				.getAttributePath().get(0), updatedSchema.getAttributePath(attributePath1.getId()).getAttributePath().get(0));
		Assert.assertNotNull("the attribute path string of attribute path '" + attributePath1.getId() + "' of the update schema shouldn't be null",
				updatedSchema.getAttributePath(attributePath1.getId()).toAttributePath());
		Assert.assertEquals("the attribute path's strings attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.toAttributePath(), updatedSchema.getAttributePath(attributePath1.getId()).toAttributePath());
		Assert.assertNotNull("the record class of the updated schema shouldn't be null", updatedSchema.getRecordClass());
		Assert.assertEquals("the recod classes are not equal", schema.getRecordClass(), updatedSchema.getRecordClass());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedSchema);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("schema json: " + json);

		schemas.put(updatedSchema.getId(), updatedSchema);

		return updatedSchema;
	}

	private Resource createResource(final String name, final String description, final ResourceType resourceType, final ObjectNode attributes,
			final Set<Configuration> configurations) {

		final ResourceService resourceService = GuicedTest.injector.getInstance(ResourceService.class);

		Assert.assertNotNull("resource service shouldn't be null", resourceService);

		// create resource

		Resource resource = null;

		try {
			resource = resourceService.createObject().getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while resource creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getId());

		resource.setName(name);
		resource.setDescription(description);
		resource.setType(resourceType);
		resource.setAttributes(attributes);
		resource.setConfigurations(configurations);

		Resource updatedResource = null;

		try {

			updatedResource = resourceService.updateObjectTransactional(resource).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the resource of id = '" + resource.getId() + "'", false);
		}

		Assert.assertNotNull("updated resource shouldn't be null", updatedResource);
		Assert.assertNotNull("updated resource id shouldn't be null", updatedResource.getId());

		resources.put(updatedResource.getId(), updatedResource);

		return updatedResource;
	}

	private Configuration createConfiguration(final String name, final String description, final ObjectNode parameters) {

		final ConfigurationService configurationService = GuicedTest.injector.getInstance(ConfigurationService.class);

		Assert.assertNotNull("configuration service shouldn't be null", configurationService);

		// create configuration

		Configuration configuration = null;

		try {
			configuration = configurationService.createObject().getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while configuration creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("configuration shouldn't be null", configuration);
		Assert.assertNotNull("configuration id shouldn't be null", configuration.getId());

		configuration.setName(name);
		configuration.setDescription(description);
		configuration.setParameters(parameters);

		Configuration updatedConfiguration = null;

		try {

			updatedConfiguration = configurationService.updateObjectTransactional(configuration).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the configuration of id = '" + configuration.getId() + "'", false);
		}

		Assert.assertNotNull("updated configuration shouldn't be null", updatedConfiguration);
		Assert.assertNotNull("updated configuration id shouldn't be null", updatedConfiguration.getId());

		configurations.put(updatedConfiguration.getId(), updatedConfiguration);

		return updatedConfiguration;
	}

	private void deleteSchema(final Schema schema) {

		final SchemaService schemaService = GuicedTest.injector.getInstance(SchemaService.class);

		Assert.assertNotNull("schema service shouldn't be null", schemaService);

		final Long schemaId = schema.getId();

		schemaService.deleteObject(schemaId);

		final Schema deletedSchema = schemaService.getObject(schemaId);

		Assert.assertNull("deleted schema shouldn't exist any more", deletedSchema);
	}

	private void deleteResource(final Resource resource) {

		final ResourceService resourceService = GuicedTest.injector.getInstance(ResourceService.class);

		Assert.assertNotNull("resource service shouldn't be null", resourceService);

		final Long resourceId = resource.getId();

		resourceService.deleteObject(resourceId);

		final Resource deletedResource = resourceService.getObject(resourceId);

		Assert.assertNull("deleted resource shouldn't exist any more", deletedResource);
	}

	private void deleteConfiguration(final Configuration configuration) {

		final ConfigurationService configurationService = GuicedTest.injector.getInstance(ConfigurationService.class);

		Assert.assertNotNull("configuration service shouldn't be null", configurationService);

		final Long configurationId = configuration.getId();

		configurationService.deleteObject(configurationId);

		final Configuration deletedConfiguration = configurationService.getObject(configurationId);

		Assert.assertNull("deleted configuration shouldn't exist any more", deletedConfiguration);
	}

	private void checkSimpleResource(final Resource resource, final Resource updatedResource, final String attributeKey, final String attributeValue) {

		Assert.assertNotNull("the name of the updated resource shouldn't be null", updatedResource.getName());
		Assert.assertEquals("the names of the resource are not equal", resource.getName(), updatedResource.getName());
		Assert.assertNotNull("the description of the updated resource shouldn't be null", updatedResource.getDescription());
		Assert.assertEquals("the descriptions of the resource are not equal", resource.getDescription(), updatedResource.getDescription());
		Assert.assertNotNull("the type of the updated resource shouldn't be null", updatedResource.getType());
		Assert.assertEquals("the types of the resource are not equal", resource.getType(), updatedResource.getType());
		Assert.assertNotNull("the attributes of the updated resource shouldn't be null", updatedResource.getAttributes());
		Assert.assertEquals("the attributes of the resource are not equal", resource.getAttributes(), updatedResource.getAttributes());
		Assert.assertNotNull("the attribute value shouldn't be null", resource.getAttribute(attributeKey));
		Assert.assertEquals("the attribute value should be equal", resource.getAttribute(attributeKey).asText(), attributeValue);
	}

	private void checkComplexResource(final Resource resource, final Resource updatedResource, final String parameterKey, final String parameterValue) {

		checkComplexResource(resource, updatedResource);

		Assert.assertEquals("the configuration of the resource is not equal", resource.getConfigurations().iterator().next(), resource
				.getConfigurations().iterator().next());
		Assert.assertEquals("the configuration parameter '" + parameterKey + "' of the resource is not equal", resource.getConfigurations()
				.iterator().next().getParameter(parameterKey), resource.getConfigurations().iterator().next().getParameter(parameterKey));
		Assert.assertEquals("the configuration parameter value for '" + parameterKey + "' of the resource is not equal", resource.getConfigurations()
				.iterator().next().getParameter(parameterKey).asText(), resource.getConfigurations().iterator().next().getParameter(parameterKey)
				.asText());
	}

	private void checkComplexResource(final Resource resource, final Resource updatedResource) {

		Assert.assertNotNull("the configurations of the updated resource shouldn't be null", updatedResource.getConfigurations());
		Assert.assertEquals("the configurations of the resource are not equal", resource.getConfigurations(), updatedResource.getConfigurations());
		Assert.assertEquals("the configurations' size of the resource are not equal", resource.getConfigurations().size(), updatedResource
				.getConfigurations().size());
	}
}
