package de.avgl.dmp.controller.resources.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.io.Resources;

import de.avgl.dmp.controller.resources.test.utils.ClaszesResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.ConfigurationsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.DataModelsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.ResourcesResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.SchemasResourceTestUtils;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;
import de.avgl.dmp.persistence.model.resource.utils.DataModelUtils;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class TasksCsvResourceTest extends ResourceTest {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(TasksCsvResourceTest.class);

	private String									taskJSONString;

	private final ResourcesResourceTestUtils		resourcesResourceTestUtils;

	private final ConfigurationsResourceTestUtils	configurationsResourceTestUtils;

	private final DataModelsResourceTestUtils		dataModelsResourceTestUtils;

	private final SchemasResourceTestUtils			schemasResourceTestUtils;

	private final ClaszesResourceTestUtils			classesResourceTestUtils;

	private final ObjectMapper						objectMapper	= injector.getInstance(ObjectMapper.class);

	private Configuration							configuration;

	private Resource								resource;

	private DataModel								dataModel;

	private Schema									schema;

	private Clasz									recordClass;

	public TasksCsvResourceTest() {

		super("tasks");

		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
		dataModelsResourceTestUtils = new DataModelsResourceTestUtils();
		schemasResourceTestUtils = new SchemasResourceTestUtils();
		classesResourceTestUtils = new ClaszesResourceTestUtils();
	}

	@Before
	public void prepare() throws IOException {

		taskJSONString = DMPPersistenceUtil.getResourceAsString("task.csv.json");
	}

	@Test
	public void testTaskExecution() throws Exception {

		LOG.debug("start task execution test");

		final String resourceFileName = "test_csv.csv";

		final Resource res1 = new Resource();
		res1.setName(resourceFileName);
		res1.setDescription("this is a description");
		res1.setType(ResourceType.FILE);

		final URL fileURL = Resources.getResource(resourceFileName);
		final File resourceFile = FileUtils.toFile(fileURL);

		// upload data resource
		resource = resourcesResourceTestUtils.uploadResource(resourceFile, res1);

		// process input data model
		final Configuration conf1 = new Configuration();

		conf1.setName("config1");
		conf1.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		conf1.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode("csv"));

		final String configurationJSONString = objectMapper.writeValueAsString(conf1);

		// create configuration
		configuration = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);

		final DataModel data1 = new DataModel();
		data1.setName("'" + res1.getName() + "' + '" + conf1.getName() + "' data model");
		data1.setDescription("data model of resource '" + res1.getName() + "' and configuration '" + conf1.getName() + "'");
		data1.setDataResource(resource);
		data1.setConfiguration(configuration);

		final String dataModelJSONString = objectMapper.writeValueAsString(data1);

		dataModel = dataModelsResourceTestUtils.createObject(dataModelJSONString, data1);

		Assert.assertNotNull("the data model shouldn't be null", dataModel);
		Assert.assertNotNull("the data model schema shouldn't be null", dataModel.getSchema());

		schema = dataModel.getSchema();

		Assert.assertNotNull("the data model schema record class shouldn't be null", schema.getRecordClass());

		recordClass = schema.getRecordClass();

		// check processed data
		final String data = dataModelsResourceTestUtils.getData(dataModel.getId(), 1);

		Assert.assertNotNull("the data shouldn't be null", data);

		// manipulate input data model
		final String finalDataModelJSONString = objectMapper.writeValueAsString(dataModel);
		final ObjectNode finalDataModelJSON = objectMapper.readValue(finalDataModelJSONString, ObjectNode.class);

		final ObjectNode taskJSON = objectMapper.readValue(taskJSONString, ObjectNode.class);
		taskJSON.put("input_data_model", finalDataModelJSON);

		// manipulate attributes
		final ObjectNode mappingJSON = (ObjectNode) ((ArrayNode) ((ObjectNode) ((ObjectNode) taskJSON).get("job")).get("mappings")).get(0);

		final String dataResourceSchemaBaseURI = DataModelUtils.determineDataResourceSchemaBaseURI(dataModel);

		final ObjectNode outputAttributePathAttributeJSON = (ObjectNode) ((ArrayNode) ((ObjectNode) mappingJSON.get("output_attribute_path"))
				.get("attributes")).get(0);
		final String outputAttributeName = outputAttributePathAttributeJSON.get("name").asText();
		outputAttributePathAttributeJSON.put("id", dataResourceSchemaBaseURI + outputAttributeName);

		final ArrayNode inputAttributePathsJSON = (ArrayNode) mappingJSON.get("input_attribute_paths");

		for (final JsonNode inputAttributePathsJSONNode : inputAttributePathsJSON) {

			final ObjectNode inputAttributeJSON = (ObjectNode) ((ArrayNode) ((ObjectNode) inputAttributePathsJSONNode).get("attributes")).get(0);
			final String inputAttributeName = inputAttributeJSON.get("name").asText();
			inputAttributeJSON.put("id", dataResourceSchemaBaseURI + inputAttributeName);
		}

		final String finalTaskJSONString = objectMapper.writeValueAsString(taskJSON);

		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(finalTaskJSONString));

		Assert.assertEquals("200 Created was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		LOG.debug("task execution response = '" + responseString + "'");

		final String expectedResultString = DMPPersistenceUtil.getResourceAsString("task-result.csv.json");

		final ArrayNode expectedJSONArray = objectMapper.readValue(expectedResultString, ArrayNode.class);
		final ArrayNode actualJSONArray = objectMapper.readValue(responseString, ArrayNode.class);

		System.out.println("expected=" + objectMapper.writeValueAsString(expectedJSONArray));
		System.out.println("actual=" + objectMapper.writeValueAsString(actualJSONArray));

		assertThat(expectedJSONArray.size(), equalTo(actualJSONArray.size()));

		final Map<String, JsonNode> actualNodes = new HashMap<>(actualJSONArray.size());
		for (final JsonNode node : actualJSONArray) {
			actualNodes.put(node.get("record_id").asText(), node);
		}

		for (final JsonNode expectedNode : expectedJSONArray) {
			final JsonNode actualNode = actualNodes.get(expectedNode.get("record_id").asText());

			assertThat(actualNode, is(notNullValue()));

			assertThat(expectedNode.get("record_id").asText(), equalTo(actualNode.get("record_id").asText()));

			final JsonNode expectedRecordData = expectedNode.get("record_data");
			final String expectedRecordDataString = objectMapper.writeValueAsString(expectedRecordData);

			final JsonNode actualRecordData = actualNode.get("record_data");
			final String actualRecordDataString = objectMapper.writeValueAsString(actualRecordData);

			assertThat(expectedRecordDataString.length(), equalTo(actualRecordDataString.length()));
		}

		LOG.debug("end task execution test");
	}

	@After
	public void cleanUp() {

		dataModelsResourceTestUtils.deleteObject(dataModel);
		schemasResourceTestUtils.deleteObject(schema);
		classesResourceTestUtils.deleteObject(recordClass);
		resourcesResourceTestUtils.deleteObject(resource);
		configurationsResourceTestUtils.deleteObject(configuration);
	}
}
