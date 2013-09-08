package de.avgl.dmp.controller.resources.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.collect.Lists;
import de.avgl.dmp.persistence.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

public class TransformationsResourceTest extends ResourceTest {

	private ObjectNode	transformationJSON	= null;

	public TransformationsResourceTest() {
		super("transformations");
	}

	@Before
	public void prepare() {

		final Transformation transformation = new Transformation();
		transformation.setId("1234bla");
		transformation.setName("Hi my name is ...");

		final Component component1 = new Component();
		component1.setId("bliblablub123");
		component1.setName("Component 1");
		component1.setType(ComponentType.EXTENDED);

		final Component component2 = new Component();
		component2.setId("bliblablub1245");
		component2.setName("Component 2");
		component2.setType(ComponentType.EXTENDED);

		final Connection connection1 = new Connection();
		connection1.setId("345345gfdfg");
		connection1.setName("Connection 1");
		connection1.setType(ConnectionType.DEFAULT);
		connection1.setTarget(component2);
		connection1.setSource(component1);

		final List<Component> components = Lists.newArrayList();
		components.add(component1);
		components.add(component2);

		transformation.setComponents(components);

		final ObjectMapper mapper = new ObjectMapper();

		final JaxbAnnotationModule module = new JaxbAnnotationModule();
		// configure as necessary
		mapper.registerModule(module);

		String str = null;

		try {
			str = mapper.writeValueAsString(transformation);
		} catch (JsonProcessingException e) {

			e.printStackTrace();

			Assert.assertTrue("something went wrong", false);
		}

		try {
			transformationJSON = mapper.readValue(str, ObjectNode.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Assert.assertTrue(true);
	}

	/**
	 * test post of transformations
	 */
	@Test
	public void testEchoJSON() {

		// POST method
		// ClientResponse response = target.path(resourceIdentifier).request(MediaType.APPLICATION_JSON_TYPE)
		// .post(Entity.json(transformationJSON.toString()), ClientResponse.class);

		String responseString = target.path(resourceIdentifier + "/echo").request(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(transformationJSON.toString()), String.class);

		final JsonNodeFactory factory = JsonNodeFactory.instance;

		final ObjectNode referenceResponseJSON = new ObjectNode(factory);

		referenceResponseJSON.put("response_message", "this is your response message");

		referenceResponseJSON.put("request_message", transformationJSON);

		Assert.assertEquals("POST responses are not equal", referenceResponseJSON.toString(), responseString);

		// // check response status code
		// if (response.getStatus() != 200) {
		//
		// throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		// }
		//
		// String result = null;
		//
		// try {
		// result = IOUtils.toString(response.getEntityStream(), "UTF-8");
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// Assert.assertEquals("wrong", "bla", result);
	}
}
