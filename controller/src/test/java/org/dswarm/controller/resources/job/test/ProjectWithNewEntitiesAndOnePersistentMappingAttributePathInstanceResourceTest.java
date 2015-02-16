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
package org.dswarm.controller.resources.job.test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.resources.job.test.utils.ProjectsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ConfigurationsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.AttributePathsResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.AttributesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.MappingAttributePathInstancesResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.proxy.ProxyProject;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.service.job.ProjectService;
import org.dswarm.persistence.service.job.test.utils.ProjectServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class ProjectWithNewEntitiesAndOnePersistentMappingAttributePathInstanceResourceTest extends
		BasicResourceTest<ProjectsResourceTestUtils, ProjectServiceTestUtils, ProjectService, ProxyProject, Project> {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectWithNewEntitiesAndOnePersistentMappingAttributePathInstanceResourceTest.class);

	private AttributesResourceTestUtils attributesResourceTestUtils;

	private AttributePathsResourceTestUtils attributePathsResourceTestUtils;

	private ResourcesResourceTestUtils resourcesResourceTestUtils;

	private ConfigurationsResourceTestUtils configurationsResourceTestUtils;

	private MappingAttributePathInstancesResourceTestUtils mappingAttributePathInstancesResourceTestUtils;

	public ProjectWithNewEntitiesAndOnePersistentMappingAttributePathInstanceResourceTest() {

		super(Project.class, ProjectService.class, "projects", "project.json", new ProjectsResourceTestUtils());

	}

	@Override protected void initObjects() {
		super.initObjects();

		attributesResourceTestUtils = new AttributesResourceTestUtils();
		attributePathsResourceTestUtils = new AttributePathsResourceTestUtils();
		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
		mappingAttributePathInstancesResourceTestUtils = new MappingAttributePathInstancesResourceTestUtils();
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

	@Test
	public void testPOSTObjectsWithNewEntities() throws Exception {

		ProjectWithNewEntitiesAndOnePersistentMappingAttributePathInstanceResourceTest.LOG.debug("start POST {}s with new entities test", pojoClassName);

		objectJSONString = DMPPersistenceUtil.getResourceAsString("project.w.new.entities.onepersistentmappingattributepathinstance.json");

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

		// START input mapping attribute path instance preparation

		// create attribute
		final Attribute inputMAPIAttribute = attributesResourceTestUtils.createObject("attribute7.json");

		// create attribute path
		String inputMAPIAttributePathJSONString = DMPPersistenceUtil.getResourceAsString("attribute_path7.json");
		final ObjectNode inputMAPIAttributePathJSON = objectMapper.readValue(inputMAPIAttributePathJSONString, ObjectNode.class);

		final ArrayNode inputMAPIAttributePathAttributes = objectMapper.createArrayNode();

		final String persistedInputMAPIAttributeJSONString = objectMapper.writeValueAsString(inputMAPIAttribute);
		final ObjectNode persistedInputMAPIAttributeJSON = objectMapper.readValue(persistedInputMAPIAttributeJSONString, ObjectNode.class);

		inputMAPIAttributePathAttributes.add(persistedInputMAPIAttributeJSON);

		inputMAPIAttributePathJSON.set("attributes", inputMAPIAttributePathAttributes);

		// re-init expected attribute path
		inputMAPIAttributePathJSONString = objectMapper.writeValueAsString(inputMAPIAttributePathJSON);
		final AttributePath expectedInputMAPIAttributePath = objectMapper.readValue(inputMAPIAttributePathJSONString, AttributePath.class);

		Assert.assertNotNull("expected input MAPI attribute path shouldn't be null", expectedInputMAPIAttributePath);

		final AttributePath inputMAPIAttributePath = attributePathsResourceTestUtils.createObject(inputMAPIAttributePathJSONString,
				expectedInputMAPIAttributePath);

		// create mapping attribute path instance
		String inputMAPIJSONString = DMPPersistenceUtil.getResourceAsString("mapping_attribute_path_instance2.json");
		final ObjectNode inputMAPIJSON = objectMapper.readValue(inputMAPIJSONString, ObjectNode.class);

		final String persistedInputMAPIAttributePathJSONString = objectMapper.writeValueAsString(inputMAPIAttributePath);
		final ObjectNode persistedInputMAPIAttributePathJSON = objectMapper.readValue(persistedInputMAPIAttributePathJSONString, ObjectNode.class);

		inputMAPIJSON.set("attribute_path", persistedInputMAPIAttributePathJSON);

		// re-init expected mapping attribute path instance
		inputMAPIJSONString = objectMapper.writeValueAsString(inputMAPIJSON);
		final MappingAttributePathInstance expectedMappingAttributePathInstance = objectMapper.readValue(inputMAPIJSONString,
				MappingAttributePathInstance.class);

		Assert.assertNotNull("expected mapping attribute path instance shouldn't be null", expectedMappingAttributePathInstance);

		final MappingAttributePathInstance inputMappingAttributePathInstance = mappingAttributePathInstancesResourceTestUtils.createObject(
				inputMAPIJSONString, expectedMappingAttributePathInstance);

		// END input mapping attribute path instance preparation

		// prepare project json for input data model resource and configuration and mapping attribute path instance manipulation
		final ObjectNode projectJSON = objectMapper.readValue(objectJSONString, ObjectNode.class);
		final ObjectNode dataModelJSON = (ObjectNode) projectJSON.get("input_data_model");

		// resource
		final String finalResourceJSONString = objectMapper.writeValueAsString(resource);
		final ObjectNode finalResourceJSON = objectMapper.readValue(finalResourceJSONString, ObjectNode.class);

		dataModelJSON.set("data_resource", finalResourceJSON);

		// configuration
		final String finalConfigurationJSONString = objectMapper.writeValueAsString(resource.getConfigurations().iterator().next());
		final ObjectNode finalConfigurationJSON = objectMapper.readValue(finalConfigurationJSONString, ObjectNode.class);

		dataModelJSON.set("configuration", finalConfigurationJSON);

		// mapping attribute path instance
		final ObjectNode mappingJSON = (ObjectNode) projectJSON.get("mappings").get(0);
		final ObjectNode secondIMAPI = (ObjectNode) mappingJSON.get("input_attribute_paths").get(1);

		final ArrayNode newIMAPIs = objectMapper.createArrayNode();

		final String firstIMAPIJSONString = objectMapper.writeValueAsString(inputMappingAttributePathInstance);
		final ObjectNode firstIMAPIJSON = objectMapper.readValue(firstIMAPIJSONString, ObjectNode.class);

		newIMAPIs.add(firstIMAPIJSON);
		newIMAPIs.add(secondIMAPI);

		mappingJSON.set("input_attribute_paths", newIMAPIs);

		objectJSONString = objectMapper.writeValueAsString(projectJSON);

		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(objectJSONString));

		Assert.assertEquals("201 Created was expected", 201, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final Project actualObject = objectMapper.readValue(responseString, pojoClass);

		Assert.assertNotNull("the response project shouldn't be null", actualObject);

		// TODO: do comparison/check somehow

		ProjectWithNewEntitiesAndOnePersistentMappingAttributePathInstanceResourceTest.LOG.debug("end POST {}s with new entities test", pojoClassName);
	}
}
