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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.dswarm.controller.resources.job.TasksResource;
import org.dswarm.controller.resources.job.test.utils.TasksResourceTestUtils;
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
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class TasksCsvResourceTest3 extends ResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(TasksCsvResourceTest3.class);

	private String taskJSONString;

	private ResourcesResourceTestUtils resourcesResourceTestUtils;

	private DataModelsResourceTestUtils dataModelsResourceTestUtils;

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	public TasksCsvResourceTest3() {

		super("tasks");
	}

	@Override
	protected void initObjects() {
		super.initObjects();

		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		dataModelsResourceTestUtils = new DataModelsResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		taskJSONString = DMPPersistenceUtil.getResourceAsString("task.csv.json");
	}

	/**
	 * do ingest on-the-fly
	 *
	 * @throws Exception
	 */
	@Test
	public void testTaskExecution() throws Exception {

		TasksCsvResourceTest3.LOG.debug("start task execution test");

		final String resourceFileName = "test_csv-controller.csv";

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

		try {
			fileType = Files.probeContentType(resourceFile.toPath());
		} catch (final IOException e1) {

			TasksCsvResourceTest3.LOG.debug("couldn't determine file type from file '" + resourceFile.getAbsolutePath() + "'");
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

		conf1.setName("config1");
		conf1.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		conf1.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode("csv"));

		final String configurationJSONString = objectMapper.writeValueAsString(conf1);

		// create configuration
		Configuration configuration = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);

		final String dataModelUuid = "DataModel-59b8519f-7c17-4a73-80fa-323a8e16617e";

		final DataModel dataModel1 = new DataModel(dataModelUuid);
		dataModel1.setName("'" + res1.getName() + "' + '" + conf1.getName() + "' data model");
		dataModel1.setDescription("data model of resource '" + res1.getName() + "' and configuration '" + conf1.getName() + "'");
		dataModel1.setDataResource(resource);
		dataModel1.setConfiguration(configuration);

		final String schemaUuid = "Schema-f7136c87-34f7-42bd-8e21-7ec605f7fc6c";

		final Schema schema1 = new Schema(schemaUuid);

		dataModel1.setSchema(schema1);

		// manipulate input data model
		final String finalInputDataModelJSONString = objectMapper.writeValueAsString(dataModel1);
		final ObjectNode finalInputDataModelJSON = objectMapper.readValue(finalInputDataModelJSONString, ObjectNode.class);

		final ObjectNode taskJSON = objectMapper.readValue(taskJSONString, ObjectNode.class);
		taskJSON.set("input_data_model", finalInputDataModelJSON);

		// utilise internal model as output data model
		final DataModel outputDataModel = dataModelsResourceTestUtils.getObject(DataModelUtils.BIBO_DOCUMENT_DATA_MODEL_UUID);
		final String outputDataModelJSONString = objectMapper.writeValueAsString(outputDataModel);
		final ObjectNode outputDataModelJSON = objectMapper.readValue(outputDataModelJSONString, ObjectNode.class);

		taskJSON.set("output_data_model", outputDataModelJSON);

		// manipulate attributes
		final ObjectNode mappingJSON = (ObjectNode) taskJSON.get("job").get("mappings").get(0);

		final String dataResourceSchemaBaseURI = DataModelUtils.determineDataModelSchemaBaseURI(dataModel1);

		final ObjectNode outputAttributePathAttributeJSON = (ObjectNode) mappingJSON
				.get("output_attribute_path").get("attribute_path").get("attributes").get(0);
		final String outputAttributeName = outputAttributePathAttributeJSON.get("name").asText();
		outputAttributePathAttributeJSON.put("uri", dataResourceSchemaBaseURI + outputAttributeName);

		final ArrayNode inputAttributePathsJSON = (ArrayNode) mappingJSON.get("input_attribute_paths");

		for (final JsonNode inputAttributePathsJSONNode : inputAttributePathsJSON) {

			final ObjectNode inputAttributeJSON = (ObjectNode) inputAttributePathsJSONNode
					.get("attribute_path").get("attributes").get(0);
			final String inputAttributeName = inputAttributeJSON.get("name").asText();
			inputAttributeJSON.put("uri", dataResourceSchemaBaseURI + inputAttributeName);
		}

		// manipulate parameter mappings in transformation component
		final ObjectNode transformationComponentParameterMappingsJSON = (ObjectNode) mappingJSON.get("transformation")
				.get("parameter_mappings");
		transformationComponentParameterMappingsJSON.put("description", "description");
		transformationComponentParameterMappingsJSON.put("__TRANSFORMATION_OUTPUT_VARIABLE__1", "output mapping attribute path instance");

		final ObjectNode requestJSON = objectMapper.createObjectNode();
		requestJSON.set(TasksResource.TASK_IDENTIFIER, taskJSON);
		requestJSON.put(TasksResource.PERSIST_IDENTIFIER, Boolean.TRUE);
		requestJSON.put(TasksResource.DO_INGEST_ON_THE_FLY_IDENTIFIER, Boolean.TRUE);
		requestJSON.put(TasksResource.DO_VERSIONING_ON_RESULT_IDENTIFIER, Boolean.FALSE);

		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(requestJSON));

		// SR Start checking response

		Assert.assertEquals("200 Created was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		TasksCsvResourceTest3.LOG.debug("task execution response = '" + responseString + "'");

		final String expectedResultString = DMPPersistenceUtil.getResourceAsString("controller_task-result.csv.json");

		final ArrayNode expectedJSONArray = objectMapper.readValue(expectedResultString, ArrayNode.class);
		final ArrayNode actualJSONArray = objectMapper.readValue(responseString, ArrayNode.class);

		Assert.assertThat(expectedJSONArray.size(), Matchers.equalTo(actualJSONArray.size()));

		final Map<String, JsonNode> actualNodes = new HashMap<>(actualJSONArray.size());
		for (final JsonNode node : actualJSONArray) {
			actualNodes.put(node.get(DMPPersistenceUtil.RECORD_ID).asText(), node);
		}

		final String actualDataResourceSchemaBaseURI = DataModelUtils.determineDataModelSchemaBaseURI(dataModel1);

		final String expectedRecordDataFieldNameExample = expectedJSONArray.get(0).get(DMPPersistenceUtil.RECORD_DATA).get(0).fieldNames().next();
		final String expectedDataResourceSchemaBaseURI = expectedRecordDataFieldNameExample.substring(0,
				expectedRecordDataFieldNameExample.lastIndexOf('#') + 1);

		TasksResourceTestUtils.compareCSVTaskResultJSON(expectedJSONArray, expectedDataResourceSchemaBaseURI, actualJSONArray, actualDataResourceSchemaBaseURI);

		final String dataModelJSONString = objectMapper.writeValueAsString(dataModel1);

		// do not compare dataModelJSONString with data1 since the schema is automatically created, comparison would fail.
		DataModel inputDataModel = dataModelsResourceTestUtils.createObjectWithoutComparison(dataModelJSONString);

		Assert.assertNotNull("the data model shouldn't be null", inputDataModel);
		Assert.assertNotNull("the data model schema shouldn't be null", inputDataModel.getSchema());

		Schema schema = inputDataModel.getSchema();

		Assert.assertNotNull("the data model schema record class shouldn't be null", schema.getRecordClass());

		// check processed data
		final String data = dataModelsResourceTestUtils.getData(inputDataModel.getUuid(), 1);

		Assert.assertNotNull("the data shouldn't be null", data);

		inputDataModel = dataModelsResourceTestUtils.getObject(inputDataModel.getUuid());

		Assert.assertNotNull("the data model shouldn't be null", inputDataModel);
		Assert.assertNotNull("the data model schema shouldn't be null", inputDataModel.getSchema());

		schema = inputDataModel.getSchema();

		Assert.assertNotNull("the data model schema record class shouldn't be null", schema.getRecordClass());

		TasksCsvResourceTest3.LOG.debug("end task execution test");
	}
}
