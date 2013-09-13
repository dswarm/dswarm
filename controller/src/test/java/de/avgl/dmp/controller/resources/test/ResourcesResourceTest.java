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

import com.google.common.io.Resources;

import de.avgl.dmp.init.util.DMPUtil;

public class ResourcesResourceTest extends ResourceTest {

	private String		resourceJSONString	= null;
	private File		resourceFile		= null;

	public ResourcesResourceTest() {
		super("resources");
	}

	@Before
	public void prepare() throws IOException {
		resourceJSONString = DMPUtil.getResourceAsString("resource.json");

		final URL fileURL = Resources.getResource("test_csv.csv");
		resourceFile = FileUtils.toFile(fileURL);
	}

	@Test
	public void testResourceUpload() throws Exception {

		final FormDataMultiPart form = new FormDataMultiPart();
		form.field("filename", resourceFile.getName());
		form.bodyPart(new FileDataBodyPart("file", resourceFile, MediaType.MULTIPART_FORM_DATA_TYPE));

		Response response = target.path(resourceIdentifier).request(MediaType.MULTIPART_FORM_DATA_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(form, MediaType.MULTIPART_FORM_DATA));
		String responseResource = response.readEntity(String.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());
		Assert.assertEquals("resource JSONs are not equal", resourceJSONString, responseResource);
	}
}
