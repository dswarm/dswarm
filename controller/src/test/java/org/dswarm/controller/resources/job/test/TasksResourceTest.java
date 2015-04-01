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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.SchemaFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.resources.job.TasksResource;
import org.dswarm.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.controller.resources.test.ResourceTest;
import org.dswarm.controller.test.GuicedTest;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class TasksResourceTest extends ResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(TasksResourceTest.class);

	private String taskJSONString = null;

	private ResourcesResourceTestUtils resourcesResourceTestUtils;

	private DataModelsResourceTestUtils dataModelsResourceTestUtils;

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	public TasksResourceTest() {

		super("tasks");
	}

	@Override protected void initObjects() {
		super.initObjects();

		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		dataModelsResourceTestUtils = new DataModelsResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		taskJSONString = DMPPersistenceUtil.getResourceAsString("task.json");
	}

	@Test
	public void testTaskExecution() throws Exception {

		TasksResourceTest.LOG.debug("start task execution test");

		final String resourceFileName = "controller_test-mabxml.xml";

		final String resource1Uuid = UUIDService.getUUID(Resource.class.getSimpleName());

		final Resource res1 = new Resource(resource1Uuid);
		res1.setName(resourceFileName);
		res1.setDescription("this is a description");
		res1.setType(ResourceType.FILE);

		final URL fileURL = Resources.getResource(resourceFileName);
		final File resourceFile = FileUtils.toFile(fileURL);

		final ObjectNode attributes1 = new ObjectNode(objectMapper.getNodeFactory());
		attributes1.put("path", resourceFile.getAbsolutePath());

		String fileType = null;
		final Tika tika = new Tika();
		try {
			fileType = tika.detect(resourceFile);
			// fileType = Files.probeContentType(resourceFile.toPath());
		} catch (final IOException e1) {

			TasksResourceTest.LOG.debug("couldn't determine file type from file '" + resourceFile.getAbsolutePath() + "'");
		}

		if (fileType != null) {

			attributes1.put("filetype", fileType);
		}

		// hint: size is not important to know since its value is skipped in the comparison of actual and expected resource
		attributes1.put("filesize", -1);

		res1.setAttributes(attributes1);

		// upload data resource
		Resource resource = resourcesResourceTestUtils.uploadResource(resourceFile, res1);

		final String configuration1Uuid = UUIDService.getUUID(Configuration.class.getSimpleName());

		// process input data model
		final Configuration conf1 = new Configuration(configuration1Uuid);

		conf1.setName("configuration 1");
		conf1.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("datensatz"));
		conf1.addParameter(ConfigurationStatics.XML_NAMESPACE, new TextNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd"));
		conf1.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode("xml"));

		final String configurationJSONString = objectMapper.writeValueAsString(conf1);

		// create configuration
		Configuration configuration = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);

		final String dataModel1Uuid = UUIDService.getUUID(DataModel.class.getSimpleName());

		final DataModel data1 = new DataModel(dataModel1Uuid);
		data1.setName("'" + res1.getName() + "' + '" + conf1.getName() + "' data model");
		data1.setDescription("data model of resource '" + res1.getName() + "' and configuration '" + conf1.getName() + "'");
		data1.setDataResource(resource);
		data1.setConfiguration(configuration);

		// TODO: add schema to data1

		final String inputDataModelJSONString = objectMapper.writeValueAsString(data1);

		DataModel inputDataModel = dataModelsResourceTestUtils.createObjectWithoutComparison(inputDataModelJSONString);

		Assert.assertNotNull("the data model shouldn't be null", inputDataModel);

		// check processed data
		final String data = dataModelsResourceTestUtils.getData(inputDataModel.getUuid(), 1);

		Assert.assertNotNull("the data shouldn't be null", data);

		// manipulate input data model
		final String finalInputDataModelJSONString = objectMapper.writeValueAsString(inputDataModel);
		final ObjectNode finalInputDataModelJSON = objectMapper.readValue(finalInputDataModelJSONString, ObjectNode.class);

		final ObjectNode taskJSON = objectMapper.readValue(taskJSONString, ObjectNode.class);
		taskJSON.set("input_data_model", finalInputDataModelJSON);

		// utilise internal model as output data model
		final DataModel outputDataModel = dataModelsResourceTestUtils.getObject(DataModelUtils.BIBO_DOCUMENT_DATA_MODEL_UUID);
		final String outputDataModelJSONString = objectMapper.writeValueAsString(outputDataModel);
		final ObjectNode outputDataModelJSON = objectMapper.readValue(outputDataModelJSONString, ObjectNode.class);

		taskJSON.set("output_data_model", outputDataModelJSON);

		final ObjectNode requestJSON = objectMapper.createObjectNode();
		requestJSON.set(TasksResource.TASK_IDENTIFIER, taskJSON);
		requestJSON.put(TasksResource.PERSIST_IDENTIFIER, Boolean.TRUE);

		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(requestJSON));

		Assert.assertEquals("200 Created was expected", 200, response.getStatus());

		// DD-277
		final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		final javax.xml.validation.Schema schema = schemaFactory.newSchema();

//		System.out.println("DocumentBuilderFactory = " + builderFactory.getClass().getName());
		try {
			builderFactory.setSchema(schema);
		} catch (final UnsupportedOperationException e) {
			Assert.fail();
		}
		Assert.assertNotNull(builderFactory.getSchema());
		Assert.assertEquals(builderFactory.getSchema(), schema);
		// END DD-277

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		TasksResourceTest.LOG.debug("task execution response = '" + responseString + "'");

		final String expectedResultString = DMPPersistenceUtil.getResourceAsString("controller_task-result.json");

		final ArrayNode expectedJSONArray = objectMapper.readValue(expectedResultString, ArrayNode.class);
		final ObjectNode expectedJSON = (ObjectNode) expectedJSONArray.get(0).get(DMPPersistenceUtil.RECORD_DATA).get(0);
		final String finalExpectedJSONString = objectMapper.writeValueAsString(expectedJSON);

		final ArrayNode actualJSONArray = objectMapper.readValue(responseString, ArrayNode.class);
		final ArrayNode actualKeyArray = (ArrayNode) actualJSONArray.get(0).get(DMPPersistenceUtil.RECORD_DATA);
		ObjectNode actualJSON = null;

		for (final JsonNode actualKeyArrayItem : actualKeyArray) {

			if (actualKeyArrayItem.get("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") != null) {

				// don't take the type JSON object for comparison

				continue;
			}

			actualJSON = (ObjectNode) actualKeyArrayItem;
		}

		Assert.assertNotNull("actual selected JSON node shouldn't be null", actualJSON);

		final String finalActualJSONString = objectMapper.writeValueAsString(actualJSON);

		Assert.assertEquals(finalExpectedJSONString.length(), finalActualJSONString.length());

		inputDataModel = dataModelsResourceTestUtils.getObject(inputDataModel.getUuid());

		Assert.assertNotNull("the data model shouldn't be null", inputDataModel);
		Assert.assertNotNull("the data model schema shouldn't be null", inputDataModel.getSchema());

		Schema schema1 = inputDataModel.getSchema();

		Assert.assertNotNull("the data model schema record class shouldn't be null", schema1.getRecordClass());

		final DataModel finalOutputDataModel = dataModelsResourceTestUtils.getObject(outputDataModel.getUuid());
		finalOutputDataModel.setSchema(null);
		dataModelsResourceTestUtils.updateObjectWithoutComparison(finalOutputDataModel);

		TasksResourceTest.LOG.debug("end task execution test");
	}
}
