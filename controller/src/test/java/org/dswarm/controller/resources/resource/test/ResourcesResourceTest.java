/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.controller.resources.resource.test;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.resources.POJOFormat;
import org.dswarm.controller.resources.resource.test.utils.ConfigurationsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.controller.resources.test.ResourceTest;
import org.dswarm.controller.test.GuicedTest;
import org.dswarm.init.util.CmdUtil;
import org.dswarm.persistence.dto.ShortExtendendBasicDMPDTO;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.resource.test.utils.ConfigurationServiceTestUtils;
import org.dswarm.persistence.service.resource.test.utils.ResourceServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class ResourcesResourceTest extends ResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(ResourcesResourceTest.class);

	private String             resourceJSONString     = null;
	private File               resourceFile           = null;
	private Resource           expectedResource       = null;
	private Resource           actualResource         = null;
	private Set<Configuration> exceptedConfigurations = null;

	private ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	private ConfigurationsResourceTestUtils configurationsResourceTestUtils;
	private ResourcesResourceTestUtils      resourcesResourceTestUtils;

	public ResourcesResourceTest() {
		super("resources");
	}

	@Override protected void initObjects() {
		super.initObjects();

		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		resourceJSONString = DMPPersistenceUtil.getResourceAsString("resource.json");

		expectedResource = GuicedTest.injector.getInstance(ObjectMapper.class).readValue(resourceJSONString, Resource.class);

		final URL fileURL = Resources.getResource("test_csv-controller.csv");
		resourceFile = FileUtils.toFile(fileURL);
	}

	@Test
	public void testResourceUpload() throws Exception {

		ResourcesResourceTest.LOG.debug("start resource upload test");

		final String resourceJSON = resourceUploadInteral(resourceFile, expectedResource);

		ResourcesResourceTest.LOG.debug("created resource = '{}'", resourceJSON);

		objectMapper.readValue(resourceJSON, Resource.class);

		ResourcesResourceTest.LOG.debug("end resource upload test");
	}

	@Test
	public void testResourceUpload2() throws Exception {

		ResourcesResourceTest.LOG.debug("start resource upload test 2");

		resourceJSONString = DMPPersistenceUtil.getResourceAsString("resource2.json");
		expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

		final URL fileURL = Resources.getResource("utf8dmpf04.n3");
		resourceFile = FileUtils.toFile(fileURL);

		final String resourceJSON = resourceUploadInteral(resourceFile, expectedResource);

		ResourcesResourceTest.LOG.debug("created resource = '{}'", resourceJSON);

		objectMapper.readValue(resourceJSON, Resource.class);

		ResourcesResourceTest.LOG.debug("end resource upload test 2");
	}

	@Test
	public void testGetResource() throws Exception {

		ResourcesResourceTest.LOG.debug("start get resource test");

		final String resourceJSON = resourceUploadInteral(resourceFile, expectedResource);

		ResourcesResourceTest.LOG.debug("created resource = '{}'", resourceJSON);

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getUuid());

		ResourcesResourceTest.LOG.debug("try to retrieve resource '{}'", resource.getUuid());

		final Response response = target(String.valueOf(resource.getUuid())).request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		final String responseResource = response.readEntity(String.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());
		Assert.assertEquals("resource JSONs are not equal", resourceJSON, responseResource);

		ResourcesResourceTest.LOG.debug("end get resource test");
	}

	@Test
	public void testGetResourceLines() throws Exception {

		ResourcesResourceTest.LOG.debug("start get resource lines test");

		final String resourceJSON = resourceUploadInteral(resourceFile, expectedResource);

		ResourcesResourceTest.LOG.debug("created resource = '{}'", resourceJSON);

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getUuid());

		ResourcesResourceTest.LOG.debug("try to retrieve resource '{}'", resource.getUuid());

		final List<String> expectedLines = Files.readLines(resourceFile, Charset.forName("UTF-8"));

		Response response = target(String.valueOf(resource.getUuid()), "lines").request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Iterator<String> expectedIter = expectedLines.iterator();

		JsonNode responseResource = response.readEntity(JsonNode.class);
		Iterator<JsonNode> actualIter = responseResource.get("lines").elements();

		while (actualIter.hasNext()) {
			final String expected = expectedIter.next();
			final String actual = actualIter.next().asText();

			Assert.assertThat(actual, CoreMatchers.equalTo(expected));
		}

		Assert.assertThat(responseResource.get("name").asText(), CoreMatchers.equalTo(resource.getName()));
		Assert.assertThat(responseResource.get("description").asText(), CoreMatchers.equalTo(resource.getDescription()));

		response = target(String.valueOf(resource.getUuid()), "lines").queryParam("atMost", 3).request().accept(MediaType.APPLICATION_JSON_TYPE)
				.get(Response.class);

		expectedIter = Iterables.limit(expectedLines, 3).iterator();

		responseResource = response.readEntity(JsonNode.class);
		actualIter = responseResource.get("lines").elements();

		while (actualIter.hasNext()) {
			final String expected = expectedIter.next();
			final String actual = actualIter.next().asText();

			Assert.assertThat(actual, CoreMatchers.equalTo(expected));
		}

		Assert.assertThat(responseResource.get("name").asText(), CoreMatchers.equalTo(resource.getName()));
		Assert.assertThat(responseResource.get("description").asText(), CoreMatchers.equalTo(resource.getDescription()));

		ResourcesResourceTest.LOG.debug("end resource lines test");
	}

	@Test
	public void testGetXMLResourceLines() throws Exception {

		ResourcesResourceTest.LOG.debug("start get xml resource lines test");

		final String resourceJSONString = DMPPersistenceUtil.getResourceAsString("test-mabxml-resource.json");

		expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

		final URL fileURL = Resources.getResource("controller_test-mabxml.xml");
		resourceFile = FileUtils.toFile(fileURL);

		final String resourceJSON = resourceUploadInteral(resourceFile, expectedResource);

		ResourcesResourceTest.LOG.debug("created resource = '{}'", resourceJSON);

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getUuid());

		ResourcesResourceTest.LOG.debug("try to retrieve resource '{}'", resource.getUuid());

		final List<String> expectedLines = Files.readLines(resourceFile, Charset.forName("UTF-8"));

		Response response = target(String.valueOf(resource.getUuid()), "lines").request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Iterator<String> expectedIter = expectedLines.iterator();

		JsonNode responseResource = response.readEntity(JsonNode.class);
		Iterator<JsonNode> actualIter = responseResource.get("lines").elements();

		while (actualIter.hasNext()) {
			final String expected = expectedIter.next();
			final String actual = actualIter.next().asText();

			Assert.assertThat(actual, CoreMatchers.equalTo(expected));
		}

		Assert.assertThat(responseResource.get("name").asText(), CoreMatchers.equalTo(resource.getName()));
		Assert.assertThat(responseResource.get("description").asText(), CoreMatchers.equalTo(resource.getDescription()));

		response = target(String.valueOf(resource.getUuid()), "lines").queryParam("atMost", 3).request().accept(MediaType.APPLICATION_JSON_TYPE)
				.get(Response.class);

		expectedIter = Iterables.limit(expectedLines, 3).iterator();

		responseResource = response.readEntity(JsonNode.class);
		actualIter = responseResource.get("lines").elements();

		while (actualIter.hasNext()) {
			final String expected = expectedIter.next();
			final String actual = actualIter.next().asText();

			Assert.assertThat(actual, CoreMatchers.equalTo(expected));
		}

		Assert.assertThat(responseResource.get("name").asText(), CoreMatchers.equalTo(resource.getName()));
		Assert.assertThat(responseResource.get("description").asText(), CoreMatchers.equalTo(resource.getDescription()));

		ResourcesResourceTest.LOG.debug("end xml resource lines test");
	}

	@Test
	public void testGetResourceConfigurations() throws Exception {

		ResourcesResourceTest.LOG.debug("start get resource configurations test");

		prepareGetResourceConfigurations();

		final int numberOfIterations = GuicedTest.injector.getInstance(Key.get(Integer.class, Names.named("NumberOfIterations")));
		final int sleepTime = GuicedTest.injector.getInstance(Key.get(Integer.class, Names.named("SleepingTime")));

		// check idempotency of GET

		for (int i = 0; i < numberOfIterations; i++) {

			getResourceConfigurationsInternal(actualResource);
		}

		for (int i = 0; i < numberOfIterations; i++) {

			getResourcesInternal(actualResource.getUuid());
		}

		for (int i = 0; i < numberOfIterations; i++) {

			getResourceConfigurationsInternal(actualResource);

			Thread.sleep(sleepTime);
		}

		for (int i = 0; i < numberOfIterations; i++) {

			getResourcesInternal(actualResource.getUuid());

			Thread.sleep(sleepTime);
		}

		ResourcesResourceTest.LOG.debug("end get resource configurations test");
	}

	@Test
	public void testCurlGetResourceConfigurations() throws Exception {

		ResourcesResourceTest.LOG.debug("start curl get resource configurations test");

		prepareGetResourceConfigurations();

		final int numberOfIterations = GuicedTest.injector.getInstance(Key.get(Integer.class, Names.named("NumberOfIterations")));
		final int sleepTime = GuicedTest.injector.getInstance(Key.get(Integer.class, Names.named("SleepingTime")));

		// check idempotency of GET

		for (int i = 0; i < numberOfIterations; i++) {

			curlGetResourceConfigurationsInternal(actualResource);
		}

		for (int i = 0; i < numberOfIterations; i++) {

			curlGetResourcesInternal(actualResource.getUuid());
		}

		for (int i = 0; i < numberOfIterations; i++) {

			curlGetResourceConfigurationsInternal(actualResource);

			Thread.sleep(sleepTime);
		}

		for (int i = 0; i < numberOfIterations; i++) {

			curlGetResourcesInternal(actualResource.getUuid());

			Thread.sleep(sleepTime);
		}

		ResourcesResourceTest.LOG.debug("end curl get resource configurations test");
	}

	@Test
	public void testGetResourceConfigurations2() throws Exception {

		ResourcesResourceTest.LOG.debug("start get resource configurations test 2");

		final String resourceJSON = resourceUploadInteral(resourceFile, expectedResource);

		ResourcesResourceTest.LOG.debug("created resource = '{}'", resourceJSON);

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getUuid());

		ResourcesResourceTest.LOG.debug("try to retrieve resource '{}'", resource.getUuid());

		final Response response = target(String.valueOf(resource.getUuid()), "configurations").request().accept(MediaType.APPLICATION_JSON_TYPE)
				.get(Response.class);

		Assert.assertEquals("404 NOT FOUND was expected", 404, response.getStatus());

		ResourcesResourceTest.LOG.debug("end get resource configurations test 2");
	}

	@Test
	public void testAddResourceConfiguration() throws Exception {

		ResourcesResourceTest.LOG.debug("start add resource configuration test");

		addResourceConfigurationInternal(resourceFile, "controller_configuration.json", expectedResource);

		ResourcesResourceTest.LOG.debug("end add resource configuration test");
	}

	@Test
	public void testGetResourceConfiguration() throws Exception {

		ResourcesResourceTest.LOG.debug("start get resource configuration test");

		final Resource resource = addResourceConfigurationInternal(resourceFile, "controller_configuration.json", expectedResource);

		final Configuration configuration = resource.getConfigurations().iterator().next();

		ResourcesResourceTest.LOG.debug("try to retrieve resource configuration '{}'", configuration.getUuid());

		final Response response = target(String.valueOf(resource.getUuid()), "/configurations/", String.valueOf(configuration.getUuid())).request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseResourceConfigurationJSON = response.readEntity(String.class);

		Assert.assertNotNull("response resource configuration JSON shouldn't be null", responseResourceConfigurationJSON);

		final Configuration responseResourceConfiguration = objectMapper.readValue(responseResourceConfigurationJSON, Configuration.class);

		Assert.assertNotNull("response resource configuration shouldn't be null", responseResourceConfiguration);

		configurationsResourceTestUtils.compareObjects(configuration, responseResourceConfiguration);

		ResourcesResourceTest.LOG.debug("end get resource configuration test");
	}

	@Test
	public void testGetResources() throws Exception {

		ResourcesResourceTest.LOG.debug("start get resources test");

		final String resourceJSON = resourceUploadInteral(resourceFile, expectedResource);

		ResourcesResourceTest.LOG.debug("created resource = '{}'", resourceJSON);

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getUuid());

		final String resource2JSON = resourceUploadInteral(resourceFile, expectedResource);

		ResourcesResourceTest.LOG.debug("created resource = '{}'", resource2JSON);

		final Resource resource2 = objectMapper.readValue(resource2JSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource2);
		Assert.assertNotNull("resource id shouldn't be null", resource2.getUuid());

		final ArrayNode resourcesJSONArray = objectMapper.createArrayNode();

		final ObjectNode resourceJSONObject = objectMapper.readValue(resourceJSON, ObjectNode.class);
		final ObjectNode resource2JSONObject = objectMapper.readValue(resource2JSON, ObjectNode.class);

		resourcesJSONArray.add(resourceJSONObject).add(resource2JSONObject);

		ResourcesResourceTest.LOG.debug("try to retrieve resources");

		final Response response = target().request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		final String responseResources = response.readEntity(String.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());
		Assert.assertEquals("resources JSONs are not equal", resourcesJSONArray.toString().length(), responseResources.length());

		// note: we cannot guarantee the insert order
		// compare response resources via map

		final Map<String, Resource> responseResourcesMap = Maps.newHashMap();

		final ArrayNode responseResourcesArray = objectMapper.readValue(responseResources, ArrayNode.class);

		for (final JsonNode responseResourceJSON : responseResourcesArray) {

			final String responseResourceString = objectMapper.writeValueAsString(responseResourceJSON);

			final Resource responseResource = objectMapper.readValue(responseResourceString, Resource.class);

			responseResourcesMap.put(responseResource.getUuid(), responseResource);
		}

		final List<Resource> resources = Lists.newArrayList();
		resources.add(resource);
		resources.add(resource2);

		for (final Resource resourceInList : resources) {

			Assert.assertTrue(responseResourcesMap.containsKey(resourceInList.getUuid()));
			final Resource resourceInMap = responseResourcesMap.get(resourceInList.getUuid());

			Assert.assertTrue(resourceInList.completeEquals(resourceInMap));
		}

		ResourcesResourceTest.LOG.debug("end get resources test");
	}

	@Test
	public void testPOSTConfigurationCSVPreview() throws Exception {

		ResourcesResourceTest.LOG.debug("start post configuration CSV preview test");

		final String resourceJSON = resourceUploadInteral(resourceFile, expectedResource);

		ResourcesResourceTest.LOG.debug("created resource = '{}'", resourceJSON);

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);

		final String configurationJSON = DMPPersistenceUtil.getResourceAsString("configuration2.json");

		final Response response = target(String.valueOf(resource.getUuid()), "/configurationpreview").request(MediaType.TEXT_PLAIN_TYPE)
				.accept(MediaType.TEXT_PLAIN_TYPE).post(Entity.json(configurationJSON));
		final String responseString = response.readEntity(String.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String expected = DMPPersistenceUtil.getResourceAsString("test_csv-controller.csv");

		Assert.assertEquals("POST responses are not equal", expected, responseString);

		ResourcesResourceTest.LOG.debug("end post configuration CSV preview test");
	}

	@Test
	public void testPOSTConfigurationCSVJSONPreview() throws Exception {

		ResourcesResourceTest.LOG.debug("start post configuration CSV JSON preview test");

		final String resourceJSON = resourceUploadInteral(resourceFile, expectedResource);

		ResourcesResourceTest.LOG.debug("created resource = '{}'", resourceJSON);

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);

		final String configurationJSON = DMPPersistenceUtil.getResourceAsString("configuration2.json");

		final Response response = target(String.valueOf(resource.getUuid()), "/configurationpreview").request(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(configurationJSON));
		final String responseString = response.readEntity(String.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String expected = DMPPersistenceUtil.getResourceAsString("test_csv.json");

		Assert.assertEquals("POST responses are not equal", expected.trim(), responseString.trim());

		ResourcesResourceTest.LOG.debug("start post configuration CSV JSON preview test");
	}

	/**
	 * Add a resource {@code resourceFile} to db, modify its name and description, update the modified resource via API PUT,
	 * assert the modified resource is updated in db.
	 *
	 * @throws Exception
	 */

	@Test
	public void testPUTResource() throws Exception {

		ResourcesResourceTest.LOG.debug("start put resource test");

		// Start prepare
		// create resource
		final String createResourceJSON = resourceUploadInteral(resourceFile, expectedResource);

		ResourcesResourceTest.LOG.debug("created resource for update = '{}'", createResourceJSON);

		// check created resource
		final Resource createResource = objectMapper.readValue(createResourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", createResource);
		Assert.assertNotNull("resource id shouldn't be null", createResource.getUuid());

		ResourcesResourceTest.LOG.debug("try to retrieve resource '{}'", createResource.getUuid());

		final Response createResponse = target(String.valueOf(createResource.getUuid())).request().accept(MediaType.APPLICATION_JSON_TYPE)
				.get(Response.class);

		final String createResourceString = createResponse.readEntity(String.class);

		Assert.assertEquals("200 OK was expected", 200, createResponse.getStatus());
		Assert.assertEquals("resource JSONs are not equal", createResourceJSON, createResourceString);
		// End prepare

		// modify resource
		expectedResource.setName(expectedResource.getName() + " update");
		expectedResource.setDescription(expectedResource.getDescription() + " update");

		// update resource (test PUT in API)
		final String updateResourceJSON = resourcesResourceTestUtils.updateResource(resourceFile, expectedResource, createResource.getUuid());

		// check response
		ResourcesResourceTest.LOG.debug("update resource = '{}'", updateResourceJSON);

		final Resource updateResource = objectMapper.readValue(updateResourceJSON, Resource.class);

		Assert.assertNotNull("updated resource shouldn't be null", updateResource);
		Assert.assertEquals("updated resource ids should be equals", updateResource.getUuid(), createResource.getUuid());

		ResourcesResourceTest.LOG.debug("try to retrieve updated resource '{}'", updateResource.getUuid());

		// get updated resource via API and compare to result from updating the resource
		final Response updateResponse = target(String.valueOf(updateResource.getUuid())).request().accept(MediaType.APPLICATION_JSON_TYPE)
				.get(Response.class);

		final String updateResourceString = updateResponse.readEntity(String.class);

		Assert.assertEquals("200 OK was expected", 200, updateResponse.getStatus());
		Assert.assertEquals("resource JSONs are not equal", updateResourceJSON, updateResourceString);

		ResourcesResourceTest.LOG.debug("end put resource test");
	}

	@Test
	public void testDELETEResource() throws Exception {

		ResourcesResourceTest.LOG.debug("start DELETE resource test");

		final String resourceJSON = resourceUploadInteral(resourceFile, expectedResource);

		ResourcesResourceTest.LOG.debug("created resource = '{}'", resourceJSON);

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		final String resourceId = resource.getUuid();

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resourceId);

		ResourcesResourceTest.LOG.debug("try to retrieve resource '{}'", resource.getUuid());

		final Response response = target(String.valueOf(resource.getUuid())).request().delete();

		Assert.assertEquals("204 NO CONTENT was expected", 204, response.getStatus());

		final Resource deletedResource = resourcesResourceTestUtils.getPersistenceServiceTestUtils().getJpaService().getObject(resourceId);

		Assert.assertNull(deletedResource);

		ResourcesResourceTest.LOG.debug("end DELETE resource test");
	}

	@Test
	public void testShortVariant() throws Exception {

		final String resourceJSON = resourceUploadInteral(resourceFile, expectedResource);
		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		final String expectedJson =
				objectMapper.writeValueAsString(
						new ShortExtendendBasicDMPDTO(
								resource.getUuid(),
								resourceFile.getName(),
								"this is a description",
								target(resource.getUuid()).getUri().toString()
						)
				);

		final Response response = target(resource.getUuid()).queryParam("format", POJOFormat.SHORT)
				.request().get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String actualJson = response.readEntity(String.class);

		JSONAssert.assertEquals(expectedJson, actualJson, true);
	}

	private String resourceUploadInteral(final File resourceFile, final Resource expectedResource) throws Exception {

		final Response response = resourceUploadInternal(resourceFile);

		Assert.assertEquals("201 CREATED was expected", 201, response.getStatus());

		final String responseResourceString = response.readEntity(String.class);

		Assert.assertNotNull("resource shouldn't be null", responseResourceString);

		final Resource responseResource = objectMapper.readValue(responseResourceString, Resource.class);

		resourcesResourceTestUtils.getPersistenceServiceTestUtils().compareObjects(expectedResource, responseResource);

		return responseResourceString;
	}

	private Response resourceUploadInternal(final File resourceFile) {

		final FormDataMultiPart form = new FormDataMultiPart();
		form.field("name", resourceFile.getName());
		form.field("filename", resourceFile.getName());
		form.field("description", "this is a description");
		form.bodyPart(new FileDataBodyPart("file", resourceFile, MediaType.MULTIPART_FORM_DATA_TYPE));

		return target().request(MediaType.MULTIPART_FORM_DATA_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(form, MediaType.MULTIPART_FORM_DATA));
	}

	private Resource addResourceConfigurationInternal(final File resourceFile, final String configurationFileName, final Resource expectedResource)
			throws Exception {

		final String resourceJSON = resourceUploadInteral(resourceFile, expectedResource);

		ResourcesResourceTest.LOG.debug("created resource = '{}'", resourceJSON);

		final Resource resource = objectMapper.readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getUuid());

		ResourcesResourceTest.LOG.debug("try to add configuration to resource '{}'", resource.getUuid());

		final String configurationJSON = DMPPersistenceUtil.getResourceAsString(configurationFileName);
		final Configuration configuration = objectMapper.readValue(configurationJSON, Configuration.class);

		final Response response = target(String.valueOf(resource.getUuid()), "/configurations").request(MediaType.APPLICATION_JSON_TYPE)
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

	private void getResourceConfigurationsInternal(final Resource resource) throws Exception {

		final Response response = target(String.valueOf(resource.getUuid()), "/configurations").request().accept(MediaType.APPLICATION_JSON_TYPE)
				.get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());
		final String resourceConfigurationsJSON = response.readEntity(String.class);

		configurationsResourceTestUtils.evaluateObjects(resourceConfigurationsJSON, exceptedConfigurations);
	}

	private void curlGetResourceConfigurationsInternal(final Resource resource) throws Exception {

		final String resourceConfigurationsJSON = CmdUtil.executeCommand(
				"curl -G -H \"Content-Type: application/json\" -H \"Accepted: application/json\" "
						+ baseUri() + "/resources/" + resource.getUuid() + "/configurations");

		configurationsResourceTestUtils.evaluateObjects(resourceConfigurationsJSON, exceptedConfigurations);
	}

	private void getResourcesInternal(final String resourceUuid) throws Exception {

		final Response response = target(String.valueOf(resourceUuid)).request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseResourceJSON = response.readEntity(String.class);

		evaluateGetResourcesInternal(responseResourceJSON);
	}

	private void curlGetResourcesInternal(final String resourceUuid) throws Exception {

		final String responseResourceJSON = CmdUtil.executeCommand("curl -G -H \"Content-Type: application/json\" -H \"Accepted: application/json\" "
				+ baseUri() + "/resources/" + resourceUuid);

		evaluateGetResourcesInternal(responseResourceJSON);
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

		final ResourceServiceTestUtils resourceServiceTestUtils = resourcesResourceTestUtils.getPersistenceServiceTestUtils();
		Resource complexResource = resourceServiceTestUtils.getJpaService().createObjectTransactional().getObject();

		final Set<Configuration> createdConfigurations = Sets.newLinkedHashSet();
		final ConfigurationServiceTestUtils configurationServiceTestUtils = resourceServiceTestUtils.getConfigurationsServiceTestUtils();

		for (final Configuration expectedConfiguration : expectedComplexResource.getConfigurations()) {

			final String configurationUuid = UUIDService.getUUID(Configuration.class.getSimpleName());

			final Configuration configuration = new Configuration(configurationUuid);

			configuration.setParameters(expectedConfiguration.getParameters());

			final Configuration createdConfiguration = configurationServiceTestUtils.createAndCompareObject(configuration, configuration);
			complexResource.addConfiguration(createdConfiguration);
			createdConfigurations.add(createdConfiguration);

			complexResource = resourceServiceTestUtils.updateAndCompareObject(complexResource, complexResource);
			// TODO: probably necessary to acquire a fresh entity manager to avoid run into concurrent modification exception => maybe we should also make use of a fresh entity manager for update operations
			complexResource = resourceServiceTestUtils.getObject(complexResource);
		}

		complexResource.setName(expectedComplexResource.getName());
		complexResource.setDescription(expectedComplexResource.getDescription());
		complexResource.setType(expectedComplexResource.getType());
		complexResource.setAttributes(expectedComplexResource.getAttributes());

		final Resource updatedComplexResource = resourceServiceTestUtils.updateAndCompareObject(complexResource, complexResource);

		Assert.assertNotNull("updated resource shouldn't be null", updatedComplexResource);
		Assert.assertNotNull("updated resource id shouldn't be null", updatedComplexResource.getUuid());

		ResourcesResourceTest.LOG.debug("try to retrieve configurations of resource '{}'", updatedComplexResource.getUuid());

		// expected==actual since to use updated ids from db in expected.
		actualResource = updatedComplexResource;
		expectedResource = actualResource;
		exceptedConfigurations = createdConfigurations;
	}

	private void evaluateGetResourcesInternal(final String responseResourceJSON) throws Exception {

		Assert.assertNotNull("response resource JSON shouldn't be null", responseResourceJSON);

		final Resource responseResource = objectMapper.readValue(responseResourceJSON, Resource.class);

		Assert.assertNotNull("the response resource shouldn't be null", responseResource);

		resourcesResourceTestUtils.getPersistenceServiceTestUtils().compareObjects(expectedResource, responseResource);

		Assert.assertNotNull(responseResource.getConfigurations());

		final Map<String, Configuration> actualConfigurations = Maps.newHashMap();

		for (final Configuration configuration : responseResource.getConfigurations()) {

			actualConfigurations.put(configuration.getUuid(), configuration);
		}

		configurationsResourceTestUtils.compareObjects(exceptedConfigurations, actualConfigurations);
	}
}
