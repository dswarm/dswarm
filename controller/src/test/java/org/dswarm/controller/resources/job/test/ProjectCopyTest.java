/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.resources.job.ProjectsResource;
import org.dswarm.controller.resources.job.test.utils.ProjectsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ConfigurationsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.proxy.ProxyProject;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.service.job.ProjectService;
import org.dswarm.persistence.service.job.test.utils.ProjectServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class ProjectCopyTest extends
		BasicResourceTest<ProjectsResourceTestUtils, ProjectServiceTestUtils, ProjectService, ProxyProject, Project> {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectCopyTest.class);

	private static final String CREATEPROJECTWITHHELPOFEXISTINGENTITIES_ENDPOINT = "/createprojectwithhelpofexistingentities";

	private DataModelsResourceTestUtils     dataModelsResourceTestUtils;
	private ConfigurationsResourceTestUtils configurationsResourceTestUtils;
	private ResourcesResourceTestUtils      resourcesResourceTestUtils;

	public ProjectCopyTest() {

		super(Project.class, ProjectService.class, "projects", "project.w.new.entities.json", new ProjectsResourceTestUtils());
	}

	@Override
	protected void initObjects() {

		super.initObjects();

		pojoClassResourceTestUtils = new ProjectsResourceTestUtils();
		dataModelsResourceTestUtils = new DataModelsResourceTestUtils();
		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
	}

	@Ignore
	@Test
	@Override
	public void testPOSTObjects() throws Exception {

	}

	@Ignore
	@Test
	@Override
	public void testGETObjects() throws Exception {

	}

	@Ignore
	@Test
	@Override
	public void testGETObject() throws Exception {

	}

	@Ignore
	@Test
	@Override
	public void testPUTObject() throws Exception {

	}

	@Ignore
	@Test
	@Override
	public void testDELETEObject() throws Exception {

	}

	/**
	 * makes use of the input data model from the reference project
	 *
	 * @throws Exception
	 */
	@Test
	public void createProjectWithHelpOfExistingEntitiesTest() throws Exception {

		ProjectCopyTest.LOG.debug("start create-project-with-help-of-existing-entities test");

		final Project referenceProject = createObjectInternal2();

		final String referenceProjectId = referenceProject.getUuid();

		final ObjectNode requestJSON = objectMapper.createObjectNode();

		requestJSON.put(ProjectsResource.REFERENCE_PROJECT, referenceProjectId);

		final String requestJSONString = objectMapper.writeValueAsString(requestJSON);

		final Project newProject = doCreateProjectWithHelpOfExistingEntitiesRequest(referenceProject, referenceProjectId, requestJSONString);

		Assert.assertEquals(referenceProject.getInputDataModel(), newProject.getInputDataModel());

		ProjectCopyTest.LOG.debug("end create-project-with-help-of-existing-entities test");
	}

	/**
	 * makes use of a new input data model (copy from the input data model of the reference project)
	 *
	 * @throws Exception
	 */
	@Test
	public void createProjectWithHelpOfExistingEntitiesTest2() throws Exception {

		ProjectCopyTest.LOG.debug("start create-project-with-help-of-existing-entities test 2");

		final Project referenceProject = createObjectInternal2();

		final String referenceProjectId = referenceProject.getUuid();

		final DataModel newPersistentInputDataModel = createNewInputDataModel(referenceProject);

		final String newPersistentInputDataModelUuid = newPersistentInputDataModel.getUuid();

		final ObjectNode requestJSON = objectMapper.createObjectNode();

		requestJSON.put(ProjectsResource.INPUT_DATA_MODEL, newPersistentInputDataModelUuid);
		requestJSON.put(ProjectsResource.REFERENCE_PROJECT, referenceProjectId);

		final String requestJSONString = objectMapper.writeValueAsString(requestJSON);

		final Project newProject = doCreateProjectWithHelpOfExistingEntitiesRequest(referenceProject, referenceProjectId, requestJSONString);

		Assert.assertNotEquals(referenceProject.getInputDataModel(), newProject.getInputDataModel());
		Assert.assertEquals(newPersistentInputDataModel, newProject.getInputDataModel());
		Assert.assertTrue(newProject.getInputDataModel().getName().startsWith("copy"));
		Assert.assertTrue(newProject.getInputDataModel().getDescription().startsWith("copy"));

		ProjectCopyTest.LOG.debug("end create-project-with-help-of-existing-entities test 2");
	}

	private DataModel createNewInputDataModel(final Project referenceProject) throws Exception {

		// START CREATE NEW INPUT DATA MODEL

		final DataModel referenceInputDataModel = referenceProject.getInputDataModel();

		final String newInputDataModelUuid = "DataModel-9b1f1249-617e-4369-acd0-81f2c89d2b25";

		final DataModel newInputDataModel = new DataModel(newInputDataModelUuid);
		newInputDataModel.setName("copy of '" + referenceInputDataModel.getName() + "'");
		newInputDataModel.setDescription("copy of '" + referenceInputDataModel.getDescription() + "'");

		final Configuration newConfiguration = createNewConfiguration(referenceInputDataModel);
		final Resource newPersistentDataResource = createNewDataResource(referenceInputDataModel);

		newPersistentDataResource.addConfiguration(newConfiguration);

		newInputDataModel.setConfiguration(newConfiguration);
		newInputDataModel.setDataResource(newPersistentDataResource);

		final String newInputDataModelJSONString = objectMapper.writeValueAsString(newInputDataModel);

		final boolean doIngest = true;
		final boolean enhanceDataResource = false;
		// FINISHED CREATE NEW INPUT DATA MODEL

		return dataModelsResourceTestUtils.createObjectWithoutComparison(newInputDataModelJSONString, doIngest, enhanceDataResource);
	}

	private Resource createNewDataResource(final DataModel referenceInputDataModel) throws Exception {

		// START CREATE NEW DATA RESOURCE (FOR INPUT DATA MODEL)

		final Resource referenceDataResource = referenceInputDataModel.getDataResource();

		final String newDataResourceUuid = "Resource-ec9a823e-f293-4ee7-a772-220f9802897b";
		final String resourceName = "test_csv-controller.csv";

		final Resource newDataResource = new Resource(newDataResourceUuid);
		newDataResource.setName(resourceName);
		newDataResource.setDescription("copy of '" + referenceDataResource.getDescription() + "'");
		newDataResource.setAttributes(referenceDataResource.getAttributes());
		newDataResource.setType(referenceDataResource.getType());

		final URL resourceURL = Resources.getResource(resourceName);
		final File resourceFile = FileUtils.toFile(resourceURL);

		// FINISHED CREATE NEW DATA RESOURCE (FOR INPUT DATA MODEL)

		return resourcesResourceTestUtils.uploadResource(resourceFile, newDataResource);
	}

	private Configuration createNewConfiguration(final DataModel referenceInputDataModel) {

		// START CREATE NEW CONFIGURATION (FOR INPUT DATA MODEL)

		final Configuration referenceConfiguration = referenceInputDataModel.getConfiguration();

		final String newConfigurationUuid = "Configuration-83356b7a-4c0d-48be-8832-6732a120c8b8";

		final Configuration newConfiguration = new Configuration(newConfigurationUuid);
		newConfiguration.setName("copy of '" + referenceConfiguration.getName() + "'");
		newConfiguration.setDescription("copy of '" + referenceConfiguration.getDescription() + "'");
		newConfiguration.setParameters(referenceConfiguration.getParameters());
		newConfiguration.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode(ConfigurationStatics.CSV_STORAGE_TYPE));

		// FINISHED CREATE NEW CONFIGURATION (FOR INPUT DATA MODEL)

		return newConfiguration;
	}

	private Project doCreateProjectWithHelpOfExistingEntitiesRequest(final Project referenceProject, final String referenceProjectId,
			final String requestJSONString)
			throws IOException {

		final Response response = target(CREATEPROJECTWITHHELPOFEXISTINGENTITIES_ENDPOINT).request(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(requestJSONString));

		Assert.assertEquals("201 Created was expected", 201, response.getStatus());

		final String responseBody = response.readEntity(String.class);

		Assert.assertNotNull(responseBody);

		final Project newProject = objectMapper.readValue(responseBody, Project.class);

		Assert.assertNotEquals(referenceProjectId, newProject.getUuid());
		Assert.assertNotNull(referenceProject.getMappings());
		Assert.assertNotNull(newProject.getMappings());
		Assert.assertEquals(referenceProject.getMappings().size(), newProject.getMappings().size());
		Assert.assertTrue(newProject.getName().startsWith("copy"));
		Assert.assertTrue(newProject.getDescription().startsWith("copy"));
		Assert.assertEquals(referenceProject.getOutputDataModel(), newProject.getOutputDataModel());

		return newProject;
	}

	private Project createObjectInternal2() throws Exception {

		objectJSONString = DMPPersistenceUtil.getResourceAsString("project.w.new.entities.json");

		// START configuration preparation

		final Configuration configuration = configurationsResourceTestUtils.createObject("configuration2.json");

		// END configuration preparation

		// START resource preparation

		// prepare resource json for configuration ids manipulation
		String resourceJSONString = DMPPersistenceUtil.getResourceAsString("resource1.json");
		final ObjectNode resourceJSON = objectMapper.readValue(resourceJSONString, ObjectNode.class);

		final ArrayNode configurationsArray = objectMapper.createArrayNode();

		final String persistedConfigurationJSONString = objectMapper.writeValueAsString(configuration);
		final ObjectNode persistedConfigurationJSON = objectMapper.readValue(persistedConfigurationJSONString, ObjectNode.class);

		configurationsArray.add(persistedConfigurationJSON);

		resourceJSON.set("configurations", configurationsArray);

		// re-init expect resource
		resourceJSONString = objectMapper.writeValueAsString(resourceJSON);
		final Resource expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

		Assert.assertNotNull("expected resource shouldn't be null", expectedResource);

		final Resource resource = resourcesResourceTestUtils.createObject(resourceJSONString, expectedResource);

		// END resource preparation

		// prepare project json for input data model resource and configuration manipulation
		final ObjectNode projectJSON = objectMapper.readValue(objectJSONString, ObjectNode.class);
		final ObjectNode dataModelJSON = (ObjectNode) projectJSON.get("input_data_model");

		final String finalResourceJSONString = objectMapper.writeValueAsString(resource);
		final ObjectNode finalResourceJSON = objectMapper.readValue(finalResourceJSONString, ObjectNode.class);

		dataModelJSON.set("data_resource", finalResourceJSON);

		final String finalConfigurationJSONString = objectMapper.writeValueAsString(resource.getConfigurations().iterator().next());
		final ObjectNode finalConfigurationJSON = objectMapper.readValue(finalConfigurationJSONString, ObjectNode.class);

		dataModelJSON.set("configuration", finalConfigurationJSON);

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(projectJSON);

		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(objectJSONString));

		Assert.assertEquals("201 Created was expected", 201, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull(responseString);

		return objectMapper.readValue(responseString, Project.class);
	}
}
