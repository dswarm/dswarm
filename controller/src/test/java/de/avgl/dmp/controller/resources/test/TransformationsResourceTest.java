package de.avgl.dmp.controller.resources.test;

import java.io.IOException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class TransformationsResourceTest extends ResourceTest {

	private static final JsonNodeFactory factory = JsonNodeFactory.instance;
	private static final ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		final JaxbAnnotationModule module = new JaxbAnnotationModule();
		mapper.registerModule(module);
	}

	private ObjectNode transformationJSON = null;
	private String transformationJSONString = null;

	public TransformationsResourceTest() {
		super("transformations");
	}

	@Before
	public void prepare() throws IOException {
		transformationJSONString = DMPPersistenceUtil.getResourceAsString("transformations-post-request.json");
		transformationJSON = mapper.readValue(transformationJSONString, ObjectNode.class);
	}

	@Ignore
	@Test
	public void testXML() throws Exception {

		final Response response = target().request(MediaType.APPLICATION_XML_TYPE)
				.accept(MediaType.APPLICATION_XML_TYPE)
				.post(Entity.json(transformationJSONString));

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		final String expected = DMPPersistenceUtil.getResourceAsString("transformations-post-metamorph.xml");;

		Assert.assertEquals("POST responses are not equal", expected, responseString);
	}

	@Ignore
	@Test
	public void testTransformationDemo() throws Exception {

		final Response response = target("/demo").request(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(transformationJSONString));

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		final String expected = DMPPersistenceUtil.getResourceAsString("transformations-post-result.json");

		Assert.assertEquals("POST responses are not equal", expected, responseString);
	}
}
