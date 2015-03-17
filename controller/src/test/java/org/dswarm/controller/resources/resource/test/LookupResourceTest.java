package org.dswarm.controller.resources.resource.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.dswarm.controller.resources.test.ResourceTest;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
public class LookupResourceTest extends ResourceTest {

	public LookupResourceTest() {

		super("lookup");
	}

	@Test
	public void testLookupTableFileProcessing() throws IOException, JSONException {

		final URL fileURL = Resources.getResource("rvk_slub.csv");
		final File lookupTableFile = FileUtils.toFile(fileURL);

		Assert.assertNotNull(lookupTableFile);

		final FormDataMultiPart form = new FormDataMultiPart();
		form.field("column_delimiter", ",");
		form.bodyPart(new FileDataBodyPart("file", lookupTableFile, MediaType.MULTIPART_FORM_DATA_TYPE));

		final Response response = target().path("/read").request(MediaType.MULTIPART_FORM_DATA_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(form, MediaType.MULTIPART_FORM_DATA));

		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getStatus());

		final String expectedLookupTableJSONString = DMPPersistenceUtil.getResourceAsString("rvk_slub.json");

		Assert.assertNotNull(expectedLookupTableJSONString);

		final String actualLookupTableJSONString = response.readEntity(String.class);

		Assert.assertNotNull(actualLookupTableJSONString);

		final ObjectMapper objectMapper2 = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(
				SerializationFeature.INDENT_OUTPUT, true);

		final ObjectNode actualLookupTableJSON = objectMapper2.readValue(actualLookupTableJSONString, ObjectNode.class);
		final String finalActual = objectMapper2.writeValueAsString(actualLookupTableJSON);

		final ObjectNode expectedLookupTableJSON = objectMapper2.readValue(expectedLookupTableJSONString, ObjectNode.class);
		final String finalExpected = objectMapper2.writeValueAsString(expectedLookupTableJSON);

		JSONAssert.assertEquals(finalExpected, finalActual, true);
	}
}
