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
package org.dswarm.controller.resources.job.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

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
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.resources.resource.test.utils.ConfigurationsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.AttributePathsResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.AttributesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.ClaszesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.SchemasResourceTestUtils;
import org.dswarm.controller.resources.test.ResourceTest;
import org.dswarm.controller.test.GuicedTest;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.internal.test.utils.InternalGDMGraphServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class TasksResourceTest extends ResourceTest {

	private static final Logger						LOG				= LoggerFactory.getLogger(TasksResourceTest.class);

	private String									taskJSONString	= null;

	private final ResourcesResourceTestUtils		resourcesResourceTestUtils;

	private final ConfigurationsResourceTestUtils	configurationsResourceTestUtils;

	private final DataModelsResourceTestUtils		dataModelsResourceTestUtils;

	private final SchemasResourceTestUtils			schemasResourceTestUtils;

	private final ClaszesResourceTestUtils			classesResourceTestUtils;

	private final AttributesResourceTestUtils		attributesResourceTestUtils;

	private final AttributePathsResourceTestUtils	attributePathsResourceTestUtils;

	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private Configuration							configuration;

	private Resource								resource;

	private DataModel inputDataModel;

	private Schema schema;

	private Clasz recordClass;

	public TasksResourceTest() {

		super("tasks");

		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
		dataModelsResourceTestUtils = new DataModelsResourceTestUtils();
		schemasResourceTestUtils = new SchemasResourceTestUtils();
		classesResourceTestUtils = new ClaszesResourceTestUtils();
		attributePathsResourceTestUtils = new AttributePathsResourceTestUtils();
		attributesResourceTestUtils = new AttributesResourceTestUtils();
	}

	@Before
	public void prepare() throws IOException {

		taskJSONString = DMPPersistenceUtil.getResourceAsString("task.json");
	}

	@Test
	public void testTaskExecution() throws Exception {

		TasksResourceTest.LOG.debug("start task execution test");

		final String resourceFileName = "controller_test-mabxml.xml";

		final Resource res1 = new Resource();
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
		resource = resourcesResourceTestUtils.uploadResource(resourceFile, res1);

		// process input data model
		final Configuration conf1 = new Configuration();

		conf1.setName("configuration 1");
		conf1.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("datensatz"));
		conf1.addParameter(ConfigurationStatics.XML_NAMESPACE, new TextNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd"));
		conf1.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode("xml"));

		final String configurationJSONString = objectMapper.writeValueAsString(conf1);

		// create configuration
		configuration = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);

		final DataModel data1 = new DataModel();
		data1.setName("'" + res1.getName() + "' + '" + conf1.getName() + "' data model");
		data1.setDescription("data model of resource '" + res1.getName() + "' and configuration '" + conf1.getName() + "'");
		data1.setDataResource(resource);
		data1.setConfiguration(configuration);

		// TODO: add schema to data1

		final String inputDataModelJSONString = objectMapper.writeValueAsString(data1);

		inputDataModel = dataModelsResourceTestUtils.createObjectWithoutComparison(inputDataModelJSONString);

		Assert.assertNotNull("the data model shouldn't be null", inputDataModel);

		// check processed data
		final String data = dataModelsResourceTestUtils.getData(inputDataModel.getId(), 1);

		Assert.assertNotNull("the data shouldn't be null", data);

		// manipulate input data model
		final String finalInputDataModelJSONString = objectMapper.writeValueAsString(inputDataModel);
		final ObjectNode finalInputDataModelJSON = objectMapper.readValue(finalInputDataModelJSONString, ObjectNode.class);

		final ObjectNode taskJSON = objectMapper.readValue(taskJSONString, ObjectNode.class);
		taskJSON.put("input_data_model", finalInputDataModelJSON);

		// utilise internal model as output data model
		final DataModel outputDataModel = dataModelsResourceTestUtils.getObject((long) 1);
		final String outputDataModelJSONString = objectMapper.writeValueAsString(outputDataModel);
		final ObjectNode outputDataModelJSON = objectMapper.readValue(outputDataModelJSONString, ObjectNode.class);

		taskJSON.put("output_data_model", outputDataModelJSON);

		final String finalTaskJSONString = objectMapper.writeValueAsString(taskJSON);

		final Response response = target().queryParam("persist", Boolean.TRUE).request(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(finalTaskJSONString));

		Assert.assertEquals("200 Created was expected", 200, response.getStatus());

		// DD-277
		final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		final javax.xml.validation.Schema schema = schemaFactory.newSchema();

		System.out.println("DocumentBuilderFactory = " + builderFactory.getClass().getName());
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
		final ObjectNode expectedJSON = (ObjectNode) expectedJSONArray.get(0).get("record_data").get(0);
		final String finalExpectedJSONString = objectMapper.writeValueAsString(expectedJSON);

		final ArrayNode actualJSONArray = objectMapper.readValue(responseString, ArrayNode.class);
		final ArrayNode actualKeyArray = (ArrayNode) actualJSONArray.get(0).get("record_data");
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

		inputDataModel = dataModelsResourceTestUtils.getObject(inputDataModel.getId());

		Assert.assertNotNull("the data model shouldn't be null", inputDataModel);
		Assert.assertNotNull("the data model schema shouldn't be null", inputDataModel.getSchema());

		this.schema = inputDataModel.getSchema();

		Assert.assertNotNull("the data model schema record class shouldn't be null", this.schema.getRecordClass());

		recordClass = this.schema.getRecordClass();

		final DataModel finalOutputDataModel = dataModelsResourceTestUtils.getObject(outputDataModel.getId());
		finalOutputDataModel.setSchema(null);
		dataModelsResourceTestUtils.updateObjectWithoutComparison(finalOutputDataModel);

		TasksResourceTest.LOG.debug("end task execution test");
	}

	@After
	public void cleanUp() {

		final Map<Long, Attribute> attributes = Maps.newHashMap();

		final Map<Long, AttributePath> attributePaths = Maps.newLinkedHashMap();

		if (schema != null) {

			final Set<AttributePath> attributePathsToDelete = schema.getUniqueAttributePaths();

			if (attributePathsToDelete != null) {

				for (final AttributePath attributePath : attributePathsToDelete) {

					attributePaths.put(attributePath.getId(), attributePath);

					final Set<Attribute> attributesToDelete = attributePath.getAttributes();

					if (attributes != null) {

						for (final Attribute attribute : attributesToDelete) {

							attributes.put(attribute.getId(), attribute);
						}
					}
				}
			}
		}

		dataModelsResourceTestUtils.deleteObject(inputDataModel);
		schemasResourceTestUtils.deleteObject(schema);

		for (final AttributePath attributePath : attributePaths.values()) {

			attributePathsResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attributePath);
		}

		for (final Attribute attribute : attributes.values()) {

			attributesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attribute);
		}

		classesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(recordClass);
		resourcesResourceTestUtils.deleteObject(resource);
		configurationsResourceTestUtils.deleteObject(configuration);

		// clean-up graph db
		InternalGDMGraphServiceTestUtils.cleanGraphDB();
	}
}
