package de.avgl.dmp.controller.resources.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;

import de.avgl.dmp.controller.services.PersistenceServices;
import de.avgl.dmp.init.util.DMPUtil;
import de.avgl.dmp.persistence.model.resource.Resource;
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

		String responseResource = response.readEntity(String.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());
		Assert.assertEquals("resource JSONs are not equal", resourceJSON, responseResource);

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
		form.field("filename", resourceFile.getName());
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
