package de.avgl.dmp.controller.resources.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.inject.Key;
import com.google.inject.name.Names;

import de.avgl.dmp.controller.resources.test.utils.ConfigurationsResourceTestUtils;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;
import de.avgl.dmp.persistence.service.resource.ResourceService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class ResourcesResourceTest extends ResourceTest {

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(ResourcesResourceTest.class);

	private String									resourceJSONString		= null;
	private File									resourceFile			= null;
	private Resource								expectedResource		= null;
	private Resource								actualResource			= null;
	private Set<Configuration>						exceptedConfigurations	= null;

	private final ConfigurationService				configurationService	= injector.getInstance(ConfigurationService.class);

	private final ResourceService					resourceService			= injector.getInstance(ResourceService.class);

	private final ObjectMapper						objectMapper			= injector.getInstance(ObjectMapper.class);

	private final ConfigurationsResourceTestUtils	configurationsResourceTestUtils;

	public ResourcesResourceTest() {
		super("resources");

		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
	}

	@Before
	public void prepare() throws IOException {
		resourceJSONString = DMPPersistenceUtil.getResourceAsString("resource.json");

		expectedResource = injector.getInstance(ObjectMapper.class).readValue(resourceJSONString, Resource.class);

		final URL fileURL = Resources.getResource("test_csv.csv");
		resourceFile = FileUtils.toFile(fileURL);
	}

	@Test
	public void testResourceUpload() throws Exception {

		LOG.debug("start resource upload test");

		final String resourceJSON = testResourceUploadInteral(resourceFile, expectedResource);

		LOG.debug("created resource = '" + resourceJSON + "'");

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		cleanUpDB(resource);

		LOG.debug("end resource upload test");
	}

	@Test
	public void testResourceUpload2() throws Exception {

		LOG.debug("start resource upload test 2");

		resourceJSONString = DMPPersistenceUtil.getResourceAsString("resource2.json");
		expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

		final URL fileURL = Resources.getResource("utf8dmpf04.n3");
		resourceFile = FileUtils.toFile(fileURL);

		final String resourceJSON = testResourceUploadInteral(resourceFile, expectedResource);

		LOG.debug("created resource = '" + resourceJSON + "'");

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		cleanUpDB(resource);

		LOG.debug("end resource upload test 2");
	}

	@Test
	public void getResource() throws Exception {

		LOG.debug("start get resource test");

		final String resourceJSON = testResourceUploadInteral(resourceFile, expectedResource);

		LOG.debug("created resource = '" + resourceJSON + "'");

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getId());

		LOG.debug("try to retrieve resource '" + resource.getId() + "'");

		final Response response = target(String.valueOf(resource.getId())).request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		final String responseResource = response.readEntity(String.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());
		Assert.assertEquals("resource JSONs are not equal", resourceJSON, responseResource);

		cleanUpDB(resource);

		LOG.debug("end get resource test");
	}

	@Test
	public void testGetResourceLines() throws Exception {

		LOG.debug("start get resource lines test");

		final String resourceJSON = testResourceUploadInteral(resourceFile, expectedResource);

		LOG.debug("created resource = '" + resourceJSON + "'");

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getId());

		LOG.debug("try to retrieve resource '" + resource.getId() + "'");

		final List<String> expectedLines = Files.readLines(resourceFile, Charset.forName("UTF-8"));

		Response response = target(String.valueOf(resource.getId()), "lines").request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Iterator<String> expectedIter = expectedLines.iterator();

		JsonNode responseResource = response.readEntity(JsonNode.class);
		Iterator<JsonNode> actualIter = responseResource.get("lines").elements();

		while (actualIter.hasNext()) {
			String expected = expectedIter.next();
			String actual = actualIter.next().asText();

			assertThat(actual, equalTo(expected));
		}

		assertThat(responseResource.get("name").asText(), equalTo(resource.getName()));
		assertThat(responseResource.get("description").asText(), equalTo(resource.getDescription()));

		response = target(String.valueOf(resource.getId()), "lines").queryParam("atMost", 3).request().accept(MediaType.APPLICATION_JSON_TYPE)
				.get(Response.class);

		expectedIter = Iterables.limit(expectedLines, 3).iterator();

		responseResource = response.readEntity(JsonNode.class);
		actualIter = responseResource.get("lines").elements();

		while (actualIter.hasNext()) {
			String expected = expectedIter.next();
			String actual = actualIter.next().asText();

			assertThat(actual, equalTo(expected));
		}

		assertThat(responseResource.get("name").asText(), equalTo(resource.getName()));
		assertThat(responseResource.get("description").asText(), equalTo(resource.getDescription()));

		cleanUpDB(resource);

		LOG.debug("end resource lines test");
	}

	@Test
	public void getResourceConfigurations() throws Exception {

		LOG.debug("start get resource configurations test");

		prepareGetResourceConfigurations();

		final int numberOfIterations = injector.getInstance(Key.get(Integer.class, Names.named("NumberOfIterations")));
		final int sleepTime = injector.getInstance(Key.get(Integer.class, Names.named("SleepingTime")));

		// check idempotency of GET

		for (int i = 0; i < numberOfIterations; i++) {

			getResourceConfigurationsInternal(actualResource);
		}

		for (int i = 0; i < numberOfIterations; i++) {

			getResourcesInternal(actualResource.getId(), expectedResource);
		}

		for (int i = 0; i < numberOfIterations; i++) {

			getResourceConfigurationsInternal(actualResource);

			Thread.sleep(sleepTime);
		}

		for (int i = 0; i < numberOfIterations; i++) {

			getResourcesInternal(actualResource.getId(), expectedResource);

			Thread.sleep(sleepTime);
		}

		finalizeGetResourceConfigurations();

		LOG.debug("end get resource configurations test");
	}

	@Test
	public void curlGetResourceConfigurations() throws Exception {

		LOG.debug("start curl get resource configurations test");

		prepareGetResourceConfigurations();

		final int numberOfIterations = injector.getInstance(Key.get(Integer.class, Names.named("NumberOfIterations")));
		final int sleepTime = injector.getInstance(Key.get(Integer.class, Names.named("SleepingTime")));

		// check idempotency of GET

		for (int i = 0; i < numberOfIterations; i++) {

			curlGetResourceConfigurationsInternal(actualResource);
		}

		for (int i = 0; i < numberOfIterations; i++) {

			curlGetResourcesInternal(actualResource.getId(), expectedResource);
		}

		for (int i = 0; i < numberOfIterations; i++) {

			curlGetResourceConfigurationsInternal(actualResource);

			Thread.sleep(sleepTime);
		}

		for (int i = 0; i < numberOfIterations; i++) {

			curlGetResourcesInternal(actualResource.getId(), expectedResource);

			Thread.sleep(sleepTime);
		}

		finalizeGetResourceConfigurations();

		LOG.debug("end curl get resource configurations test");
	}

	@Test
	public void getResourceConfigurations2() throws Exception {

		LOG.debug("start get resource configurations test 2");

		final String resourceJSON = testResourceUploadInteral(resourceFile, expectedResource);

		LOG.debug("created resource = '" + resourceJSON + "'");

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getId());

		LOG.debug("try to retrieve resource '" + resource.getId() + "'");

		final Response response = target(String.valueOf(resource.getId()), "configurations").request().accept(MediaType.APPLICATION_JSON_TYPE)
				.get(Response.class);

		Assert.assertEquals("404 NOT FOUND was expected", 404, response.getStatus());

		cleanUpDB(resource);

		LOG.debug("end get resource configurations test 2");
	}

	@Test
	public void addResourceConfiguration() throws Exception {

		LOG.debug("start add resource configuration test");

		final Resource resource = addResourceConfigurationInternal(resourceFile, "configuration.json", expectedResource);

		// clean up

		for (final Configuration configuration : resource.getConfigurations()) {

			configurationService.deleteObject(configuration.getId());
		}

		cleanUpDB(resource);

		LOG.debug("end add resource configuration test");
	}

	@Test
	public void getResourceConfiguration() throws Exception {

		LOG.debug("start get resource configuration test");

		final Resource resource = addResourceConfigurationInternal(resourceFile, "configuration.json", expectedResource);

		final Configuration configuration = resource.getConfigurations().iterator().next();

		LOG.debug("try to retrieve resource configuration '" + configuration.getId() + "'");

		final Response response = target(String.valueOf(resource.getId()), "/configurations/", String.valueOf(configuration.getId())).request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseResourceConfigurationJSON = response.readEntity(String.class);

		Assert.assertNotNull("response resource configuration JSON shouldn't be null", responseResourceConfigurationJSON);

		final Configuration responseResourceConfiguration = objectMapper.readValue(responseResourceConfigurationJSON, Configuration.class);

		Assert.assertNotNull("response resource configuration shoudln't be null", responseResourceConfiguration);

		configurationsResourceTestUtils.compareObjects(configuration, responseResourceConfiguration);

		configurationService.deleteObject(configuration.getId());

		cleanUpDB(resource);

		LOG.debug("end get resource configuration test");
	}

	@Test
	public void getResources() throws Exception {

		LOG.debug("start get resources test");

		final String resourceJSON = testResourceUploadInteral(resourceFile, expectedResource);

		LOG.debug("created resource = '" + resourceJSON + "'");

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getId());

		final String resource2JSON = testResourceUploadInteral(resourceFile, expectedResource);

		LOG.debug("created resource = '" + resource2JSON + "'");

		final Resource resource2 = objectMapper.readValue(resource2JSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource2);
		Assert.assertNotNull("resource id shouldn't be null", resource2.getId());

		final ArrayNode resourcesJSONArray = objectMapper.createArrayNode();

		final ObjectNode resourceJSONObject = objectMapper.readValue(resourceJSON, ObjectNode.class);
		final ObjectNode resource2JSONObject = objectMapper.readValue(resource2JSON, ObjectNode.class);

		resourcesJSONArray.add(resourceJSONObject).add(resource2JSONObject);

		LOG.debug("try to retrieve resources");

		final Response response = target().request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		String responseResources = response.readEntity(String.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());
		// Assert.assertEquals("resources JSONs are not equal", resourcesJSONArray.toString(), responseResources);

		cleanUpDB(resource);
		cleanUpDB(resource2);

		LOG.debug("end get resources test");
	}

	@Test
	public void testPOSTConfigurationCSVPreview() throws Exception {

		LOG.debug("start post configuration CSV preview test");

		final String resourceJSON = testResourceUploadInteral(resourceFile, expectedResource);

		LOG.debug("created resource = '" + resourceJSON + "'");

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);

		final String configurationJSON = DMPPersistenceUtil.getResourceAsString("configuration2.json");

		final Response response = target(String.valueOf(resource.getId()), "/configurationpreview").request(MediaType.TEXT_PLAIN_TYPE)
				.accept(MediaType.TEXT_PLAIN_TYPE).post(Entity.json(configurationJSON));
		final String responseString = response.readEntity(String.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String expected = DMPPersistenceUtil.getResourceAsString("test_csv.csv");

		Assert.assertEquals("POST responses are not equal", expected, responseString);

		cleanUpDB(resource);

		LOG.debug("end post configuration CSV preview test");
	}

	@Test
	public void testPOSTConfigurationCSVJSONPreview() throws Exception {

		LOG.debug("start post configuration CSV JSON preview test");

		final String resourceJSON = testResourceUploadInteral(resourceFile, expectedResource);

		LOG.debug("created resource = '" + resourceJSON + "'");

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);

		final String configurationJSON = DMPPersistenceUtil.getResourceAsString("configuration2.json");

		final Response response = target(String.valueOf(resource.getId()), "/configurationpreview").request(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(configurationJSON));
		final String responseString = response.readEntity(String.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String expected = DMPPersistenceUtil.getResourceAsString("test_csv.json");

		Assert.assertEquals("POST responses are not equal", expected.trim(), responseString.trim());

		cleanUpDB(resource);

		LOG.debug("start post configuration CSV JSON preview test");
	}
	
	@Test
	public void testDELETEResource() throws Exception {
		
		LOG.debug("start DELETE resource test");

		final String resourceJSON = testResourceUploadInteral(resourceFile, expectedResource);

		LOG.debug("created resource = '" + resourceJSON + "'");

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		final Long resourceId = resource.getId();
		
		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resourceId);

		LOG.debug("try to retrieve resource '" + resource.getId() + "'");

		final Response response = target(String.valueOf(resource.getId())).request().delete();

		Assert.assertEquals("204 NO CONTENT was expected", 204, response.getStatus());

		final Resource deletedResource = resourceService.getObject(resourceId);
		
		Assert.assertNull(deletedResource);
		
		LOG.debug("end DELETE resource test");
	}

	private String testResourceUploadInteral(final File resourceFile, final Resource expectedResource) throws Exception {

		final FormDataMultiPart form = new FormDataMultiPart();
		form.field("name", resourceFile.getName());
		form.field("filename", resourceFile.getName());
		form.field("description", "this is a description");
		form.bodyPart(new FileDataBodyPart("file", resourceFile, MediaType.MULTIPART_FORM_DATA_TYPE));

		final Response response = target().request(MediaType.MULTIPART_FORM_DATA_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(form, MediaType.MULTIPART_FORM_DATA));

		Assert.assertEquals("200 OK was expected", 201, response.getStatus());

		String responseResourceString = response.readEntity(String.class);

		Assert.assertNotNull("resource shouldn't be null", responseResourceString);

		final Resource responseResource = objectMapper.readValue(responseResourceString, Resource.class);

		compareResource(expectedResource, responseResource);

		return responseResourceString;
	}

	private Resource addResourceConfigurationInternal(final File resourceFile, final String configurationFileName, final Resource expectedResource)
			throws Exception {

		final String resourceJSON = testResourceUploadInteral(resourceFile, expectedResource);

		LOG.debug("created resource = '" + resourceJSON + "'");

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getId());

		LOG.debug("try to add configuration to resource '" + resource.getId() + "'");

		final String configurationJSON = DMPPersistenceUtil.getResourceAsString(configurationFileName);
		final Configuration configuration = objectMapper.readValue(configurationJSON, Configuration.class);

		final Response response = target(String.valueOf(resource.getId()), "/configurations").request(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(configurationJSON));

		final String responseConfigurationJSON = response.readEntity(String.class);

		Assert.assertEquals("201 Created was expected", 201, response.getStatus());
		Assert.assertNotNull("response configuration JSON shouldn't be null", responseConfigurationJSON);

		final Configuration responseConfiguration = objectMapper.readValue(responseConfigurationJSON, Configuration.class);

		Assert.assertNotNull("response configuration shouldn't be null", responseConfiguration);

		configurationsResourceTestUtils.compareObjects(configuration, responseConfiguration);

		resource.addConfiguration(responseConfiguration);

		return resource;
	}

	private void cleanUpDB(final Resource resource) {

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getId());

		final Long resourceId = resource.getId();

		// clean-up DB

		final ResourceService resourceService = injector.getInstance(ResourceService.class);

		Assert.assertNotNull("resource service shouldn't be null", resourceService);

		resourceService.deleteObject(resourceId);

		final Resource deletedResource = resourceService.getObject(resourceId);

		Assert.assertNull("deleted resource should be null", deletedResource);
	}

	private void getResourceConfigurationsInternal(final Resource resource) throws Exception {

		final Response response = target(String.valueOf(resource.getId()), "/configurations").request().accept(MediaType.APPLICATION_JSON_TYPE)
				.get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());
		final String resourceConfigurationsJSON = response.readEntity(String.class);

		configurationsResourceTestUtils.evaluateObjects(resourceConfigurationsJSON, exceptedConfigurations);
	}

	private void curlGetResourceConfigurationsInternal(final Resource resource) throws Exception {

		final String resourceConfigurationsJSON = executeCommand("curl -G -H \"Content-Type: application/json\" -H \"Accepted: application/json\" "
				+ baseUri() + "/resources/" + resource.getId().toString() + "/configurations");

		configurationsResourceTestUtils.evaluateObjects(resourceConfigurationsJSON, exceptedConfigurations);
	}

	private void getResourcesInternal(final Long resourceId, final Resource expectedResource) throws Exception {

		final Response response = target(String.valueOf(resourceId)).request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseResourceJSON = response.readEntity(String.class);

		evaluateGetResourcesInternal(responseResourceJSON);
	}

	private void curlGetResourcesInternal(final Long resourceId, final Resource expectedResource) throws Exception {

		final String responseResourceJSON = executeCommand("curl -G -H \"Content-Type: application/json\" -H \"Accepted: application/json\" "
				+ baseUri() + "/resources/" + resourceId.toString());

		evaluateGetResourcesInternal(responseResourceJSON);
	}

	private void compareResource(final Resource expectedResource, final Resource responseResource) {

		Assert.assertNotNull("resource shouldn't be null", responseResource);
		Assert.assertNotNull("resource name shouldn't be null", responseResource.getName());
		Assert.assertEquals("the resource names should be equal", expectedResource.getName(), responseResource.getName());
		Assert.assertNotNull("resource description shouldn't be null", responseResource.getDescription());
		Assert.assertEquals("the resource descriptions should be equal", expectedResource.getDescription(), responseResource.getDescription());
		Assert.assertNotNull("resource type shouldn't be null", responseResource.getType());
		Assert.assertEquals("the resource types should be equal", expectedResource.getType(), responseResource.getType());
		Assert.assertNotNull("resource attributes shouldn't be null", responseResource.getAttributes());

		final Iterator<Entry<String, JsonNode>> attributesJSONNodeIter = expectedResource.getAttributes().fields();

		while (attributesJSONNodeIter.hasNext()) {

			final Entry<String, JsonNode> attributeJSONEntry = attributesJSONNodeIter.next();
			final String attributeKey = attributeJSONEntry.getKey();

			Assert.assertNotNull("resource attribute '" + attributeKey + "' shouldn't be null", responseResource.getAttribute(attributeKey));

			if (!"path".equals(attributeKey) && !"filesize".equals(attributeKey)) {

				Assert.assertEquals("the resource " + attributeKey + "s should be equal", expectedResource.getAttribute(attributeKey),
						responseResource.getAttribute(attributeKey));
			}
		}
	}

	private void prepareGetResourceConfigurations() throws Exception {

		final String complexResourceJSONString = DMPPersistenceUtil.getResourceAsString("complex_resource.json");
		final Resource expectedComplexResource = objectMapper.readValue(complexResourceJSONString, Resource.class);

		Assert.assertNotNull("the complex resource shouldn't be null", expectedComplexResource);
		Assert.assertNotNull("the name of the complex resource shouldn't be null", expectedComplexResource.getName());
		Assert.assertNotNull("the description of the complex resource shouldn't be null", expectedComplexResource.getDescription());
		Assert.assertNotNull("the type of the complex resource shouldn't be null", expectedComplexResource.getType());
		Assert.assertNotNull("the attributes of the complex resource shouldn't be null", expectedComplexResource.getAttributes());
		Assert.assertNotNull("the configurations of the complex resource shouldn't be null", expectedComplexResource.getConfigurations());
		Assert.assertFalse("the configurations of the complex resource shouldn't be empty", expectedComplexResource.getConfigurations().isEmpty());

		Resource complexResource = null;

		try {

			complexResource = resourceService.createObject().getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong during object creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("resource shouldn't be null", complexResource);
		Assert.assertNotNull("resource id shouldn't be null", complexResource.getId());

		LOG.debug("create new resource with id = '" + complexResource.getId() + "'");

		complexResource.setName(expectedComplexResource.getName());
		complexResource.setDescription(expectedComplexResource.getDescription());
		complexResource.setType(expectedComplexResource.getType());
		complexResource.setAttributes(expectedComplexResource.getAttributes());

		final Set<Configuration> createdConfigurations = Sets.newLinkedHashSet();

		for (final Configuration expectedConfiguration : expectedComplexResource.getConfigurations()) {

			Configuration configuration = null;

			try {

				configuration = configurationService.createObject().getObject();
			} catch (final DMPPersistenceException e) {

				Assert.assertTrue("something went wrong during object creation.\n" + e.getMessage(), false);
			}

			Assert.assertNotNull("configuration shouldn't be null", configuration);
			Assert.assertNotNull("configuration id shouldn't be null", configuration.getId());

			configuration.setParameters(expectedConfiguration.getParameters());

			complexResource.addConfiguration(configuration);

			createdConfigurations.add(configuration);
		}

		Resource updatedComplexResource = null;

		try {

			updatedComplexResource = resourceService.updateObjectTransactional(complexResource).getObject();
		} catch (DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the resource", false);
		}

		Assert.assertNotNull("updated resource shouldn't be null", updatedComplexResource);
		Assert.assertNotNull("updated resource id shouldn't be null", updatedComplexResource.getId());

		LOG.debug("try to retrieve configurations of resource '" + updatedComplexResource.getId() + "'");

		expectedResource = expectedComplexResource;
		actualResource = updatedComplexResource;
		exceptedConfigurations = createdConfigurations;
	}

	private void finalizeGetResourceConfigurations() {

		// clean up

		for (final Configuration configuration : exceptedConfigurations) {

			configurationService.deleteObject(configuration.getId());
		}

		cleanUpDB(actualResource);
	}

	private void evaluateGetResourcesInternal(final String responseResourceJSON) throws Exception {

		Assert.assertNotNull("response resource JSON shouldn't be null", responseResourceJSON);

		final Resource responseResource = objectMapper.readValue(responseResourceJSON, Resource.class);

		Assert.assertNotNull("the response resource shouldn't be null", responseResource);

		compareResource(expectedResource, responseResource);

		Assert.assertNotNull(responseResource.getConfigurations());

		final Map<Long, Configuration> actualConfigurations = Maps.newHashMap();

		for (final Configuration configuration : responseResource.getConfigurations()) {

			actualConfigurations.put(configuration.getId(), configuration);
		}

		configurationsResourceTestUtils.compareObjects(exceptedConfigurations, actualConfigurations);
	}

	private String executeCommand(final String command) throws Exception {

		final Process process = Runtime.getRuntime().exec(command);
		int exitStatus = process.waitFor();

		Assert.assertEquals("exit status should be 0", 0, exitStatus);

		final StringBuilder sb = new StringBuilder();

		final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = reader.readLine();
		while (line != null) {
			sb.append(line);
			line = reader.readLine();
		}

		LOG.debug("got result from command execution '" + command + "' = '" + sb.toString() + "'");

		return sb.toString();
	}
}
