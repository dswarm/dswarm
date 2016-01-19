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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

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

public class TasksCsvResourceTest4 extends ResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(TasksCsvResourceTest4.class);

	private String taskJSONString;

	private ResourcesResourceTestUtils resourcesResourceTestUtils;

	private DataModelsResourceTestUtils dataModelsResourceTestUtils;

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	public TasksCsvResourceTest4() {

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

		taskJSONString = DMPPersistenceUtil.getResourceAsString("task.csv.json");
	}

	/**
	 * do ingest on-the-fly + export on-the-fly
	 *
	 * @throws Exception
	 */
	@Test
	public void testTaskExecution() throws Exception {

		TasksCsvResourceTest4.LOG.debug("start task execution test");

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

			TasksCsvResourceTest4.LOG.debug("couldn't determine file type from file '" + resourceFile.getAbsolutePath() + "'");
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

		final String dataModelUuid = "DataModel-642fa5de-9afc-4f91-ad35-d204c40a11f4";

		final DataModel dataModel1 = new DataModel(dataModelUuid);
		dataModel1.setName("'" + res1.getName() + "' + '" + conf1.getName() + "' data model");
		dataModel1.setDescription("data model of resource '" + res1.getName() + "' and configuration '" + conf1.getName() + "'");
		dataModel1.setDataResource(resource);
		dataModel1.setConfiguration(configuration);

		final String schemaUuid = "Schema-b2db413b-9c14-4724-b7e2-7b03186bf6be";

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
		requestJSON.put(TasksResource.PERSIST_IDENTIFIER, Boolean.FALSE);
		requestJSON.put(TasksResource.DO_INGEST_ON_THE_FLY_IDENTIFIER, Boolean.TRUE);
		requestJSON.put(TasksResource.DO_EXPORT_ON_THE_FLY_IDENTIFIER, Boolean.TRUE);
		requestJSON.put(TasksResource.DO_VERSIONING_ON_RESULT_IDENTIFIER, Boolean.FALSE);

		final Response response = target().request(MediaType.APPLICATION_XML_TYPE).post(Entity.json(requestJSON));

		// SR Start checking response

		Assert.assertEquals("200 Created was expected", 200, response.getStatus());

		final InputStream actualXMLStream = response.readEntity(InputStream.class);
		Assert.assertNotNull(actualXMLStream);

		final BufferedInputStream bis = new BufferedInputStream(actualXMLStream, 1024);

		final String expectedXML = DMPPersistenceUtil.getResourceAsString("controller_task-result.csv.xml");

		// do comparison: check for XML similarity
		final Diff xmlDiff = DiffBuilder
				.compare(Input.fromString(expectedXML).build())
				.withTest(Input.fromStream(bis).build())
				.ignoreWhitespace()
				.checkForSimilar()
				.build();

		if (xmlDiff.hasDifferences()) {
			final StringBuilder sb = new StringBuilder("Oi chap, there seem to ba a mishap!");
			for (final Difference difference : xmlDiff.getDifferences()) {
				sb.append('\n').append(difference);
			}
			Assert.fail(sb.toString());
		}

		actualXMLStream.close();
		bis.close();

		TasksCsvResourceTest4.LOG.debug("end task execution test");
	}
}
