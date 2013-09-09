package de.avgl.dmp.controller.resources.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

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
		transformationJSONString = getResourceAsString("complex-request.json");
		transformationJSON = mapper.readValue(transformationJSONString, ObjectNode.class);
	}

	/**
	 * test post of transformations
	 */
	@Test
	public void testEchoJSON() {
		Response response = target.path(resourceIdentifier + "/echo").request(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(transformationJSONString));
		String responseString = response.readEntity(String.class);

		final ObjectNode expected = new ObjectNode(factory);

		expected.put("response_message", "this is your response message");
		expected.put("request_message", transformationJSON);

		Assert.assertEquals("POST responses are not equal", expected.toString(), responseString);
		Assert.assertEquals("200 OK was expected", 200, response.getStatus());
	}

	@Test
	public void testXML() throws Exception {
		Response response = target.path(resourceIdentifier).request(MediaType.APPLICATION_XML_TYPE)
				.accept(MediaType.APPLICATION_XML_TYPE)
				.post(Entity.json(transformationJSONString));
		String responseString = response.readEntity(String.class);

		final String expected = getResourceAsString("complex-metamorph.xml");

		Assert.assertEquals("POST responses are not equal", expected, responseString);
		Assert.assertEquals("200 OK was expected", 200, response.getStatus());
	}

	@Test
	public void testTransformation() throws Exception {
		Response response = target.path(resourceIdentifier).request(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(transformationJSONString));
		String responseString = response.readEntity(String.class);

		final String expected = getResourceAsString("complex-result.json");

		Assert.assertEquals("POST responses are not equal", expected, responseString);
		Assert.assertEquals("200 OK was expected", 200, response.getStatus());
	}
}
