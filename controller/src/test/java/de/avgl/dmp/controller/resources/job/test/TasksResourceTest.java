package de.avgl.dmp.controller.resources.job.test;

import static org.junit.Assert.assertEquals;

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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

import de.avgl.dmp.controller.resources.resource.test.utils.ConfigurationsResourceTestUtils;
import de.avgl.dmp.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import de.avgl.dmp.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.AttributePathsResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.AttributesResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.ClaszesResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.SchemasResourceTestUtils;
import de.avgl.dmp.controller.resources.test.ResourceTest;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.internal.test.utils.InternalGDMGraphServiceTestUtils;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class TasksResourceTest extends ResourceTest {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(TasksResourceTest.class);

	private String									taskJSONString	= null;

	private final ResourcesResourceTestUtils		resourcesResourceTestUtils;

	private final ConfigurationsResourceTestUtils	configurationsResourceTestUtils;

	private final DataModelsResourceTestUtils		dataModelsResourceTestUtils;

	private final SchemasResourceTestUtils			schemasResourceTestUtils;

	private final ClaszesResourceTestUtils			classesResourceTestUtils;

	private final AttributesResourceTestUtils		attributesResourceTestUtils;

	private final AttributePathsResourceTestUtils	attributePathsResourceTestUtils;

	private final ObjectMapper						objectMapper	= injector.getInstance(ObjectMapper.class);

	private Configuration							configuration;

	private Resource								resource;

	private DataModel								dataModel;

	private Schema									schema;

	private Clasz									recordClass;

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

		LOG.debug("start task execution test");

		final String resourceFileName = "test-mabxml.xml";

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

		final String dataModelJSONString = objectMapper.writeValueAsString(data1);

		dataModel = dataModelsResourceTestUtils.createObject(dataModelJSONString, data1);

		Assert.assertNotNull("the data model shouldn't be null", dataModel);

		// check processed data
		final String data = dataModelsResourceTestUtils.getData(dataModel.getId(), 1);

		Assert.assertNotNull("the data shouldn't be null", data);

		// manipulate input data model
		final String finalDataModelJSONString = objectMapper.writeValueAsString(dataModel);
		final ObjectNode finalDataModelJSON = objectMapper.readValue(finalDataModelJSONString, ObjectNode.class);

		final ObjectNode taskJSON = objectMapper.readValue(taskJSONString, ObjectNode.class);
		((ObjectNode) taskJSON).put("input_data_model", finalDataModelJSON);

		// manipulate output data model (output data model = input data model (for now))
		((ObjectNode) taskJSON).put("output_data_model", finalDataModelJSON);

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

		LOG.debug("task execution response = '" + responseString + "'");

		final String expectedResultString = DMPPersistenceUtil.getResourceAsString("controller_task-result.json");

		final ArrayNode expectedJSONArray = objectMapper.readValue(expectedResultString, ArrayNode.class);
		final ObjectNode expectedJSON = (ObjectNode) expectedJSONArray.get(0).get("record_data").get(0);
		final String finalExpectedJSONString = objectMapper.writeValueAsString(expectedJSON);

		final ArrayNode actualJSONArray = objectMapper.readValue(responseString, ArrayNode.class);
		final ObjectNode actualJSON = (ObjectNode) actualJSONArray.get(0).get("record_data").get(0);
		final String finalActualJSONString = objectMapper.writeValueAsString(actualJSON);

		assertEquals(finalExpectedJSONString.length(), finalActualJSONString.length());

		dataModel = dataModelsResourceTestUtils.getObject(dataModel.getId());

		Assert.assertNotNull("the data model shouldn't be null", dataModel);
		Assert.assertNotNull("the data model schema shouldn't be null", dataModel.getSchema());

		this.schema = dataModel.getSchema();

		Assert.assertNotNull("the data model schema record class shouldn't be null", this.schema.getRecordClass());

		recordClass = this.schema.getRecordClass();

		LOG.debug("end task execution test");
	}

	@After
	public void cleanUp() {

		final Map<Long, Attribute> attributes = Maps.newHashMap();

		final Map<Long, AttributePath> attributePaths = Maps.newLinkedHashMap();

		if (schema != null) {

			final Set<AttributePath> attributePathsToDelete = schema.getAttributePaths();

			if (attributePaths != null) {

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

		dataModelsResourceTestUtils.deleteObject(dataModel);
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
