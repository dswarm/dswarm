package org.dswarm.controller.resources.job.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
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
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.internal.test.utils.InternalGDMGraphServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class TasksCsvResourceTest extends ResourceTest {

	private static final Logger						LOG				= LoggerFactory.getLogger(TasksCsvResourceTest.class);

	private String									taskJSONString;

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

	private DataModel outputDataModel;

	private Schema schema;

	private Clasz recordClass;

	public TasksCsvResourceTest() {

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

		taskJSONString = DMPPersistenceUtil.getResourceAsString("task.csv.json");
	}

	@Test
	public void testTaskExecution() throws Exception {

		TasksCsvResourceTest.LOG.debug("start task execution test");

		final String resourceFileName = "test_csv.csv";

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

			TasksCsvResourceTest.LOG.debug("couldn't determine file type from file '" + resourceFile.getAbsolutePath() + "'");
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

		// do not compare dataModelJSONString with data1 since the schema is automatically created, comparison would fail.
		inputDataModel = dataModelsResourceTestUtils.createObjectWithoutComparison(dataModelJSONString);

		Assert.assertNotNull("the data model shouldn't be null", inputDataModel);
		Assert.assertNotNull("the data model schema shouldn't be null", inputDataModel.getSchema());

		schema = inputDataModel.getSchema();

		Assert.assertNotNull("the data model schema record class shouldn't be null", schema.getRecordClass());

		recordClass = schema.getRecordClass();

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

		// manipulate attributes
		final ObjectNode mappingJSON = (ObjectNode) taskJSON.get("job").get("mappings").get(0);

		final String dataResourceSchemaBaseURI = DataModelUtils.determineDataModelSchemaBaseURI(inputDataModel);

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

		final String finalTaskJSONString = objectMapper.writeValueAsString(taskJSON);

		final Response response = target().queryParam("persist", Boolean.TRUE).request(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(finalTaskJSONString));

		// SR Start checking response

		Assert.assertEquals("200 Created was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		TasksCsvResourceTest.LOG.debug("task execution response = '" + responseString + "'");

		final String expectedResultString = DMPPersistenceUtil.getResourceAsString("controller_task-result.csv.json");

		final ArrayNode expectedJSONArray = objectMapper.readValue(expectedResultString, ArrayNode.class);
		final ArrayNode actualJSONArray = objectMapper.readValue(responseString, ArrayNode.class);

		System.out.println("expected=" + objectMapper.writeValueAsString(expectedJSONArray));
		System.out.println("actual=" + objectMapper.writeValueAsString(actualJSONArray));

		Assert.assertThat(expectedJSONArray.size(), Matchers.equalTo(actualJSONArray.size()));

		final Map<String, JsonNode> actualNodes = new HashMap<>(actualJSONArray.size());
		for (final JsonNode node : actualJSONArray) {
			actualNodes.put(node.get("record_id").asText(), node);
		}

		this.outputDataModel = dataModelsResourceTestUtils.getObject(outputDataModel.getId());

		final String actualDataResourceSchemaBaseURI = DataModelUtils.determineDataModelSchemaBaseURI(inputDataModel);

		final String expectedRecordDataFieldNameExample = expectedJSONArray.get(0).get("record_data").get(0).fieldNames().next();
		final String expectedDataResourceSchemaBaseURI = expectedRecordDataFieldNameExample.substring(0,
				expectedRecordDataFieldNameExample.lastIndexOf('#') + 1);

		for (final JsonNode expectedNode : expectedJSONArray) {

			final String recordData = expectedNode.get("record_data").get(0).get(expectedDataResourceSchemaBaseURI + "description")
					.asText();
			final JsonNode actualNode = getRecordData(recordData, actualJSONArray, actualDataResourceSchemaBaseURI + "description");

			// SR TODO use Assert.assertNotNull here?
			Assert.assertThat(actualNode, CoreMatchers.is(Matchers.notNullValue()));

			final ObjectNode expectedRecordData = (ObjectNode) expectedNode.get("record_data").get(0);

			final ObjectNode actualElement = (ObjectNode) actualNode;
			ObjectNode actualRecordData = null;

			for (final JsonNode actualRecordDataCandidate : actualElement.get("record_data")) {

				if (actualRecordDataCandidate.get(actualDataResourceSchemaBaseURI + "description") != null) {

					actualRecordData = (ObjectNode) actualRecordDataCandidate;

					break;
				}
			}

			Assert.assertThat(actualRecordData, CoreMatchers.is(Matchers.notNullValue()));

			Assert.assertThat(actualRecordData.get(actualDataResourceSchemaBaseURI + "description").asText(),
					Matchers.equalTo(expectedRecordData.get(expectedDataResourceSchemaBaseURI + "description").asText()));
		}

		inputDataModel = dataModelsResourceTestUtils.getObject(inputDataModel.getId());

		Assert.assertNotNull("the data model shouldn't be null", inputDataModel);
		Assert.assertNotNull("the data model schema shouldn't be null", inputDataModel.getSchema());

		this.schema = inputDataModel.getSchema();

		Assert.assertNotNull("the data model schema record class shouldn't be null", this.schema.getRecordClass());

		recordClass = this.schema.getRecordClass();

		TasksCsvResourceTest.LOG.debug("end task execution test");
	}

	@After
	public void cleanUp() throws Exception {

		final Map<Long, Attribute> attributes = Maps.newHashMap();

		final Map<Long, AttributePath> attributePaths = Maps.newLinkedHashMap();

		if (schema != null) {

			final Set<AttributePath> attributePathsToDelete = schema.getUniqueAttributePaths();

			if (attributePathsToDelete != null) {

				for (final AttributePath attributePath : attributePathsToDelete) {

					attributePaths.put(attributePath.getId(), attributePath);

					final Set<Attribute> attributesToDelete = attributePath.getAttributes();

					if (attributesToDelete != null) {

						for (final Attribute attribute : attributesToDelete) {

							attributes.put(attribute.getId(), attribute);
						}
					}
				}
			}
		}

		if(outputDataModel.getSchema() != null) {

			final Set<AttributePath> attributePathsToDelete = outputDataModel.getSchema().getUniqueAttributePaths();

			if (attributePathsToDelete != null) {

				for (final AttributePath attributePath : attributePathsToDelete) {

					attributePaths.put(attributePath.getId(), attributePath);

					final Set<Attribute> attributesToDelete = attributePath.getAttributes();

					if (attributesToDelete != null) {

						for (final Attribute attribute : attributesToDelete) {

							attributes.put(attribute.getId(), attribute);
						}
					}
				}
			}
		}

		final Schema outputSchema = outputDataModel.getSchema();
		final Clasz outputRecordClass = outputSchema.getRecordClass();

		outputDataModel.setSchema(null);

		dataModelsResourceTestUtils.updateObjectWithoutComparison(outputDataModel);

		dataModelsResourceTestUtils.deleteObject(inputDataModel);
		schemasResourceTestUtils.deleteObject(schema);
		schemasResourceTestUtils.deleteObject(outputSchema);

		for (final AttributePath attributePath : attributePaths.values()) {

			attributePathsResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attributePath);
		}

		for (final Attribute attribute : attributes.values()) {

			attributesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attribute);
		}

		classesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(recordClass);
		classesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(outputRecordClass);
		resourcesResourceTestUtils.deleteObject(resource);
		configurationsResourceTestUtils.deleteObject(configuration);

		// clean-up graph db
		InternalGDMGraphServiceTestUtils.cleanGraphDB();
	}

	private JsonNode getRecordData(final String recordData, final ArrayNode jsonArray, final String key) {

		for (final JsonNode jsonEntry : jsonArray) {

			final ArrayNode actualRecordDataArray = (ArrayNode) jsonEntry.get("record_data");

			for (final JsonNode actualRecordData : actualRecordDataArray) {

				if (actualRecordData.get(key) == null) {

					continue;
				}

				if (recordData.equals(actualRecordData.get(key).asText())) {

					return jsonEntry;
				}
			}
		}

		return null;
	}
}
