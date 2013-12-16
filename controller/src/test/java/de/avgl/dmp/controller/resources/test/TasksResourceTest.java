package de.avgl.dmp.controller.resources.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.io.Resources;

import de.avgl.dmp.controller.resources.test.utils.ConfigurationsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.ResourcesResourceTestUtils;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class TasksResourceTest extends ResourceTest {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(TasksResourceTest.class);

	private String									taskJSONString	= null;

	private final ResourcesResourceTestUtils		resourcesResourceTestUtils;

	private final ConfigurationsResourceTestUtils	configurationsResourceTestUtils;

	private final ObjectMapper						objectMapper	= injector.getInstance(ObjectMapper.class);

	private Configuration							configuration;

	private Resource								resource;

	public TasksResourceTest() {

		super("tasks");

		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
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

		conf1.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("datensatz"));
		conf1.addParameter(ConfigurationStatics.XML_NAMESPACE, new TextNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd"));
		conf1.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode("xml"));

		final String configurationJSONString = objectMapper.writeValueAsString(conf1);

		// create configuration
		configuration = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);

		// process data resource to data model
		final String data = resourcesResourceTestUtils.processResource(resource.getId(), configuration.getId(), 1);

		Assert.assertNotNull("the data shouldn't be null", data);

		final String resourceJSONString = objectMapper.writeValueAsString(resource);
		final ObjectNode resourceJSON = objectMapper.readValue(resourceJSONString, ObjectNode.class);

		final String finalConfigurationJSONString = objectMapper.writeValueAsString(configuration);
		final ObjectNode configurationJSON = objectMapper.readValue(finalConfigurationJSONString, ObjectNode.class);

		// manipulate data resource + configuration id
		final ObjectNode taskJSON = objectMapper.readValue(taskJSONString, ObjectNode.class);
		((ObjectNode) taskJSON.get("input_data_model")).put("data_resource", resourceJSON);
		((ObjectNode) taskJSON.get("input_data_model")).put("configuration", configurationJSON);

		final String finalTaskJSONString = objectMapper.writeValueAsString(taskJSON);

		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(finalTaskJSONString));

		Assert.assertEquals("200 Created was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);
		
		LOG.debug("task execution response = '" + responseString + "'");

		final String expectedResultString = DMPPersistenceUtil.getResourceAsString("task-result.json");

		final ArrayNode expectedJSONArray = objectMapper.readValue(expectedResultString, ArrayNode.class);
		final ObjectNode expectedJSON = (ObjectNode) expectedJSONArray.get(0).get("record_data");
		final String finalExpectedJSONString = objectMapper.writeValueAsString(expectedJSON);

		final ArrayNode actualJSONArray = objectMapper.readValue(responseString, ArrayNode.class);
		final ObjectNode actualJSON = (ObjectNode) actualJSONArray.get(0).get("record_data");
		final String finalActualJSONString = objectMapper.writeValueAsString(actualJSON);

		assertEquals(finalExpectedJSONString.length(), finalActualJSONString.length());

		LOG.debug("end task execution test");
	}

	@After
	public void cleanUp() {

		resourcesResourceTestUtils.deleteObject(resource);
		configurationsResourceTestUtils.deleteObject(configuration);
	}
}
