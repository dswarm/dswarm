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
package org.dswarm.controller.resources.job.test;

import java.util.Set;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.dswarm.controller.resources.POJOFormat;
import org.dswarm.controller.resources.job.test.utils.ComponentsResourceTestUtils;
import org.dswarm.controller.resources.job.test.utils.FunctionsResourceTestUtils;
import org.dswarm.controller.resources.job.test.utils.MappingsResourceTestUtils;
import org.dswarm.controller.resources.job.test.utils.ProjectsResourceTestUtils;
import org.dswarm.controller.resources.job.test.utils.TransformationsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ConfigurationsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.ClaszesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.SchemasResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.dto.ShortExtendendBasicDMPDTO;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.job.proxy.ProxyProject;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.job.ProjectService;
import org.dswarm.persistence.service.job.test.utils.ProjectServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class ProjectsResourceTest extends
		BasicResourceTest<ProjectsResourceTestUtils, ProjectServiceTestUtils, ProjectService, ProxyProject, Project> {

	private FunctionsResourceTestUtils       functionsResourceTestUtils;
	private MappingsResourceTestUtils        mappingsResourceTestUtils;
	private ClaszesResourceTestUtils         claszesResourceTestUtils;
	private ConfigurationsResourceTestUtils  configurationsResourceTestUtils;
	private ResourcesResourceTestUtils       resourcesResourceTestUtils;
	private SchemasResourceTestUtils         schemasResourceTestUtils;
	private ComponentsResourceTestUtils      componentsResourceTestUtils;
	private TransformationsResourceTestUtils transformationsResourceTestUtils;

	public ProjectsResourceTest() {

		super(Project.class, ProjectService.class, "projects", "project.json", new ProjectsResourceTestUtils());
	}

	@Override
	protected void initObjects() {

		super.initObjects();

		pojoClassResourceTestUtils = new ProjectsResourceTestUtils();
		functionsResourceTestUtils = new FunctionsResourceTestUtils();
		mappingsResourceTestUtils = new MappingsResourceTestUtils();
		claszesResourceTestUtils = new ClaszesResourceTestUtils();
		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		schemasResourceTestUtils = new SchemasResourceTestUtils();
		componentsResourceTestUtils = new ComponentsResourceTestUtils();
		transformationsResourceTestUtils = new TransformationsResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		final ProjectServiceTestUtils projectServiceTestUtils = pojoClassResourceTestUtils.getPersistenceServiceTestUtils();
		final Project project = projectServiceTestUtils.createDefaultObject();

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(project);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);
	}

	@Override
	protected Project updateObject(final Project persistedProject) throws Exception {

		persistedProject.setName(persistedProject.getName() + " update");

		persistedProject.setDescription(persistedProject.getDescription() + " update");

		// update data model
		final Configuration updateConfiguration = configurationsResourceTestUtils.createObject("configuration2.json");
		final DataModel inputDataModel = persistedProject.getInputDataModel();
		inputDataModel.setConfiguration(updateConfiguration);

		final Resource updateResource = resourcesResourceTestUtils.createObject("resource2.json");
		inputDataModel.setDataResource(updateResource);

		final Clasz updateRecordClass = claszesResourceTestUtils.createObject("clasz2.json");

		final Schema tmpSchema = inputDataModel.getSchema();
		tmpSchema.setName("our schema: update");
		tmpSchema.setRecordClass(updateRecordClass);

		final String tmpSchemaJSONString = objectMapper.writeValueAsString(tmpSchema);

		final Schema updateSchema = schemasResourceTestUtils.createObject(tmpSchemaJSONString, tmpSchema);

		inputDataModel.setSchema(updateSchema);

		persistedProject.setInputDataModel(inputDataModel);

		// update mapping
		// - function
		final Function updateFunction = functionsResourceTestUtils.createObject("function1.json");
		final String finalUpdateFunctionJSONString = objectMapper.writeValueAsString(updateFunction);
		final ObjectNode finalUpdateFunctionJSON = objectMapper.readValue(finalUpdateFunctionJSONString, ObjectNode.class);

		// - component
		String updateComponentJSONString = DMPPersistenceUtil.getResourceAsString("component1.json");
		final ObjectNode updateComponentJSON = objectMapper.readValue(updateComponentJSONString, ObjectNode.class);
		updateComponentJSON.set("function", finalUpdateFunctionJSON);
		updateComponentJSONString = objectMapper.writeValueAsString(updateComponentJSON);
		final Component expectedComponent = objectMapper.readValue(updateComponentJSONString, Component.class);
		final Component updateComponent = componentsResourceTestUtils.createObject(updateComponentJSONString, expectedComponent);

		// - transformation
		String updateTransformationJSONString = DMPPersistenceUtil.getResourceAsString("transformation1.json");
		final ObjectNode updateTransformationJSON = objectMapper.readValue(updateTransformationJSONString, ObjectNode.class);
		final String finalUpdateComponentJSONString = objectMapper.writeValueAsString(updateComponent);
		final ObjectNode finalUpdateComponentJSON = objectMapper.readValue(finalUpdateComponentJSONString, ObjectNode.class);
		final ArrayNode updateComponentsJSONArray = objectMapper.createArrayNode();
		updateComponentsJSONArray.add(finalUpdateComponentJSON);
		updateTransformationJSON.set("components", updateComponentsJSONArray);
		updateTransformationJSONString = objectMapper.writeValueAsString(updateTransformationJSON);
		final Transformation expectedTransformation = objectMapper.readValue(updateTransformationJSONString, Transformation.class);
		final Transformation updateTransformation = transformationsResourceTestUtils
				.createObject(updateTransformationJSONString, expectedTransformation);

		// - transformation component
		String updateTransformationComponentJSONString = DMPPersistenceUtil.getResourceAsString("transformation_component1.json");
		final ObjectNode updateTransformationComponentJSON = objectMapper.readValue(updateTransformationComponentJSONString, ObjectNode.class);
		final String finalUpdateTransformationJSONString = objectMapper.writeValueAsString(updateTransformation);
		final ObjectNode finalUpdateTransformationJSON = objectMapper.readValue(finalUpdateTransformationJSONString, ObjectNode.class);
		updateTransformationComponentJSON.set("function", finalUpdateTransformationJSON);
		updateTransformationComponentJSONString = objectMapper.writeValueAsString(updateTransformationComponentJSON);
		final Component expectedTransformationComponent = objectMapper.readValue(updateTransformationComponentJSONString, Component.class);
		final Component updateTransformationComponent = componentsResourceTestUtils.createObject(updateTransformationComponentJSONString,
				expectedTransformationComponent);

		// - mapping
		String updateMappingJSONString = DMPPersistenceUtil.getResourceAsString("mapping1.json");
		final ObjectNode updateMappingJSON = objectMapper.readValue(updateMappingJSONString, ObjectNode.class);
		final String finalUpdateTransformationComponentJSONString = objectMapper.writeValueAsString(updateTransformationComponent);
		final ObjectNode finalUpdateTransformationComponentJSON = objectMapper.readValue(finalUpdateTransformationComponentJSONString,
				ObjectNode.class);
		updateMappingJSON.set("transformation", finalUpdateTransformationComponentJSON);

		// - attribute paths
		updateMappingJSONString = objectMapper.writeValueAsString(updateMappingJSON);
		final Mapping expectedMapping = objectMapper.readValue(updateMappingJSONString, Mapping.class);

		expectedMapping.setInputAttributePaths(persistedProject.getMappings().iterator().next().getInputAttributePaths());
		expectedMapping.setOutputAttributePath(persistedProject.getMappings().iterator().next().getOutputAttributePath());

		updateMappingJSONString = objectMapper.writeValueAsString(expectedMapping);

		final Mapping updateMapping = mappingsResourceTestUtils.createObject(updateMappingJSONString, expectedMapping);
		final Set<Mapping> updateMappings = Sets.newLinkedHashSet();
		updateMappings.add(updateMapping);

		persistedProject.setMappings(updateMappings);

		// now we updated all the above things on our persistedProject (also in database)
		final String updateProjectJSONString = objectMapper.writeValueAsString(persistedProject);
		final Project expectedProject = objectMapper.readValue(updateProjectJSONString, Project.class);
		Assert.assertNotNull("the project JSON string shouldn't be null", updateProjectJSONString);

		final Project updateProject = pojoClassResourceTestUtils.updateObject(updateProjectJSONString, expectedProject);

		Assert.assertEquals("projects should be equal", persistedProject, updateProject);

		return updateProject;
	}

	@Test
	public void testShortVariant() throws Exception {

		final Project project = createObjectInternal();

		final String expectedJson =
				objectMapper.writeValueAsString(
						new ShortExtendendBasicDMPDTO(
								project.getUuid(),
								project.getName(),
								project.getDescription(),
								target(project.getUuid()).getUri().toString()
						)
				);

		final Response response = target(project.getUuid())
				.queryParam("format", POJOFormat.SHORT)
				.request().get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String actualJson = response.readEntity(String.class);

		JSONAssert.assertEquals(expectedJson, actualJson, true);
	}
}
