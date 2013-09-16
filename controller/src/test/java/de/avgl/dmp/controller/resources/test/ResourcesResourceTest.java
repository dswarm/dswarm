package de.avgl.dmp.controller.resources.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map.Entry;

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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;

import de.avgl.dmp.controller.services.PersistenceServices;
import de.avgl.dmp.init.util.DMPUtil;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.services.ConfigurationService;
import de.avgl.dmp.persistence.services.ResourceService;

public class ResourcesResourceTest extends ResourceTest {

	private static final org.apache.log4j.Logger	LOG					= org.apache.log4j.Logger.getLogger(ResourcesResourceTest.class);

	private String									resourceJSONString	= null;
	private File									resourceFile		= null;
	private Resource								expectedResource	= null;

	public ResourcesResourceTest() {
		super("resources");
	}

	@Before
	public void prepare() throws IOException {
		resourceJSONString = DMPUtil.getResourceAsString("resource.json");
		expectedResource = DMPUtil.getJSONObjectMapper().readValue(resourceJSONString, Resource.class);

		final URL fileURL = Resources.getResource("test_csv.csv");
		resourceFile = FileUtils.toFile(fileURL);
	}

	@Test
	public void testResourceUpload() throws Exception {

		final String resourceJSON = testResourceUploadInteral();

		LOG.debug("created resource = '" + resourceJSON + "'");

		final Resource resource = DMPUtil.getJSONObjectMapper().readValue(resourceJSON, Resource.class);

		cleanUpDB(resource);
	}

	@Test
	public void getResource() throws Exception {

		final String resourceJSON = testResourceUploadInteral();

		LOG.debug("created resource = '" + resourceJSON + "'");

		final Resource resource = DMPUtil.getJSONObjectMapper().readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getId());

		LOG.debug("try to retrieve resource '" + resource.getId() + "'");

		final Response response = target.path(resourceIdentifier + "/" + resource.getId()).request().accept(MediaType.APPLICATION_JSON_TYPE)
				.get(Response.class);

		final String responseResource = response.readEntity(String.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());
		Assert.assertEquals("resource JSONs are not equal", resourceJSON, responseResource);

		cleanUpDB(resource);
	}

	@Test
	public void getResourceConfigurations() throws Exception {

		final String complexResourceJSONString = DMPUtil.getResourceAsString("complex_resource.json");
		final Resource expectedComplexResource = DMPUtil.getJSONObjectMapper().readValue(complexResourceJSONString, Resource.class);

		Assert.assertNotNull("the complex resource shouldn't be null", expectedComplexResource);
		Assert.assertNotNull("the name of the complex resource shouldn't be null", expectedComplexResource.getName());
		Assert.assertNotNull("the description of the complex resource shouldn't be null", expectedComplexResource.getDescription());
		Assert.assertNotNull("the type of the complex resource shouldn't be null", expectedComplexResource.getType());
		Assert.assertNotNull("the attributes of the complex resource shouldn't be null", expectedComplexResource.getAttributes());
		Assert.assertNotNull("the configurations of the complex resource shouldn't be null", expectedComplexResource.getConfigurations());
		Assert.assertFalse("the configurations of the complex resource shouldn't be empty", expectedComplexResource.getConfigurations().isEmpty());

		final ResourceService resourceService = PersistenceServices.getInstance().getResourceService();

		Resource complexResource = null;

		try {

			complexResource = resourceService.createObject();
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

		final ConfigurationService configurationService = PersistenceServices.getInstance().getConfigurationService();

		for (final Configuration expectedConfiguration : expectedComplexResource.getConfigurations()) {

			Configuration configuration = null;

			try {

				configuration = configurationService.createObject();
			} catch (final DMPPersistenceException e) {

				Assert.assertTrue("something went wrong during object creation.\n" + e.getMessage(), false);
			}

			Assert.assertNotNull("configuration shouldn't be null", configuration);
			Assert.assertNotNull("configuration id shouldn't be null", configuration.getId());

			configuration.setParameters(expectedConfiguration.getParameters());

			complexResource.addConfiguration(configuration);
		}

		Resource updatedComplexResource = null;

		try {

			updatedComplexResource = resourceService.updateObjectTransactional(complexResource);
		} catch (DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the resource", false);
		}

		Assert.assertNotNull("updated resource shouldn't be null", updatedComplexResource);
		Assert.assertNotNull("updated resource id shouldn't be null", updatedComplexResource.getId());

		LOG.debug("try to retrieve configurations of resource '" + updatedComplexResource.getId() + "'");

		final Response response = target.path(resourceIdentifier + "/" + updatedComplexResource.getId() + "/configurations").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		final String responseResourceConfigurations = response.readEntity(String.class);
		final String resourceConfigurationsJSON = DMPUtil.getResourceAsString("resource_configurations.json");

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());
		Assert.assertEquals("resource JSONs are not equal", resourceConfigurationsJSON, responseResourceConfigurations);

		cleanUpDB(updatedComplexResource);
	}

	@Test
	public void getResourceConfigurations2() throws Exception {

		final String resourceJSON = testResourceUploadInteral();

		LOG.debug("created resource = '" + resourceJSON + "'");

		final Resource resource = DMPUtil.getJSONObjectMapper().readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getId());

		LOG.debug("try to retrieve resource '" + resource.getId() + "'");

		final Response response = target.path(resourceIdentifier + "/" + resource.getId() + "/configurations").request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("404 NOT FOUND was expected", 404, response.getStatus());

		cleanUpDB(resource);
	}

	@Test
	public void addResourceConfiguration() throws Exception {

		final String resourceJSON = testResourceUploadInteral();

		LOG.debug("created resource = '" + resourceJSON + "'");

		final Resource resource = DMPUtil.getJSONObjectMapper().readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getId());

		LOG.debug("try to add configuration to resource '" + resource.getId() + "'");

		final String configurationJSON = DMPUtil.getResourceAsString("configuration.json");
		final Configuration configuration = DMPUtil.getJSONObjectMapper().readValue(configurationJSON, Configuration.class);

		final Response response = target.path(resourceIdentifier + "/" + resource.getId() + "/configurations")
				.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(configurationJSON));

		final String responseConfigurationJSON = response.readEntity(String.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());
		Assert.assertNotNull("response configuration JSON shouldn't be null", responseConfigurationJSON);

		final Configuration responseConfiguration = DMPUtil.getJSONObjectMapper().readValue(responseConfigurationJSON, Configuration.class);

		Assert.assertNotNull("response configuration shouldn't be null", responseConfiguration);

		final ObjectNode parameters = configuration.getParameters();

		final Iterator<Entry<String, JsonNode>> parameterEntriesIter = parameters.fields();

		final ObjectNode responseParameters = responseConfiguration.getParameters();

		Assert.assertNotNull("response parameters shoudln't be null", responseParameters);

		while (parameterEntriesIter.hasNext()) {

			final Entry<String, JsonNode> parameterEntry = parameterEntriesIter.next();

			final String parameterKey = parameterEntry.getKey();

			final JsonNode parameterValueNode = responseParameters.get(parameterKey);

			Assert.assertNotNull("parameter '" + parameterKey + "' is not part of the response configuration parameters", parameterValueNode);

			final String parameterValue = parameterEntry.getValue().asText();

			Assert.assertTrue("the parameter values of '" + parameterKey + "' are not equal. expected = '" + parameterValue + "'; was = '"
					+ parameterValueNode.asText() + "'", parameterValue.equals(parameterValueNode.asText()));
		}

		cleanUpDB(resource);
	}

	@Test
	public void getResources() throws Exception {

		final String resourceJSON = testResourceUploadInteral();

		LOG.debug("created resource = '" + resourceJSON + "'");

		final Resource resource = DMPUtil.getJSONObjectMapper().readValue(resourceJSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getId());

		final String resource2JSON = testResourceUploadInteral();

		LOG.debug("created resource = '" + resource2JSON + "'");

		final Resource resource2 = DMPUtil.getJSONObjectMapper().readValue(resource2JSON, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", resource2);
		Assert.assertNotNull("resource id shouldn't be null", resource2.getId());

		final ArrayNode resourcesJSONArray = DMPUtil.getJSONObjectMapper().createArrayNode();

		final ObjectNode resourceJSONObject = DMPUtil.getJSONObjectMapper().readValue(resourceJSON, ObjectNode.class);
		final ObjectNode resource2JSONObject = DMPUtil.getJSONObjectMapper().readValue(resource2JSON, ObjectNode.class);

		resourcesJSONArray.add(resourceJSONObject).add(resource2JSONObject);

		LOG.debug("try to retrieve resources");

		final Response response = target.path(resourceIdentifier).request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		String responseResources = response.readEntity(String.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());
		Assert.assertEquals("resources JSONs are not equal", resourcesJSONArray.toString(), responseResources);

		cleanUpDB(resource);
		cleanUpDB(resource2);
	}

	private String testResourceUploadInteral() throws Exception {

		final FormDataMultiPart form = new FormDataMultiPart();
		form.field("name", resourceFile.getName());
		form.field("filename", resourceFile.getName());
		form.field("description", "this is a description");
		form.bodyPart(new FileDataBodyPart("file", resourceFile, MediaType.MULTIPART_FORM_DATA_TYPE));

		final Response response = target.path(resourceIdentifier).request(MediaType.MULTIPART_FORM_DATA_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(form, MediaType.MULTIPART_FORM_DATA));

		Assert.assertEquals("200 OK was expected", 201, response.getStatus());

		String responseResourceString = response.readEntity(String.class);

		Assert.assertNotNull("resource shouldn't be null", responseResourceString);

		final Resource responseResource = DMPUtil.getJSONObjectMapper().readValue(responseResourceString, Resource.class);

		Assert.assertNotNull("resource shouldn't be null", responseResource);
		Assert.assertNotNull("resource name shouldn't be null", responseResource.getName());
		Assert.assertEquals("the resource names should be equal", expectedResource.getName(), responseResource.getName());
		Assert.assertNotNull("resource description shouldn't be null", responseResource.getDescription());
		Assert.assertEquals("the resource descriptions should be equal", expectedResource.getDescription(), responseResource.getDescription());
		Assert.assertNotNull("resource type shouldn't be null", responseResource.getType());
		Assert.assertEquals("the resource types should be equal", expectedResource.getType(), responseResource.getType());
		Assert.assertNotNull("resource attributes shouldn't be null", responseResource.getAttributes());
		Assert.assertNotNull("resource attribute 'filetype' shouldn't be null", responseResource.getAttribute("filetype"));
		Assert.assertEquals("the resource file types should be equal", expectedResource.getAttribute("filetype"),
				responseResource.getAttribute("filetype"));
		Assert.assertNotNull("resource attribute 'filesize' shouldn't be null", responseResource.getAttribute("filesize"));
		Assert.assertEquals("the resource file sizes should be equal", expectedResource.getAttribute("filesize"),
				responseResource.getAttribute("filesize"));

		return responseResourceString;
	}

	private void cleanUpDB(final Resource resource) {

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getId());

		final Long resourceId = resource.getId();

		// clean-up DB

		final ResourceService resourceService = PersistenceServices.getInstance().getResourceService();

		Assert.assertNotNull("resource service shouldn't be null", resourceService);

		resourceService.deleteObject(resourceId);

		final Resource deletedResource = resourceService.getObject(resourceId);

		Assert.assertNull("deleted resource should be null", deletedResource);
	}
}
