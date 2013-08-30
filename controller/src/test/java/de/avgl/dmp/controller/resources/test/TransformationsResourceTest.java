package de.avgl.dmp.controller.resources.test;

import java.io.IOException;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.client.ClientResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.Component;
import de.avgl.dmp.persistence.model.ComponentType;
import de.avgl.dmp.persistence.model.Connection;
import de.avgl.dmp.persistence.model.ConnectionType;
import de.avgl.dmp.persistence.model.Transformation;

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

		final Set<Component> components = Sets.newLinkedHashSet();
		components.add(component1);
		components.add(component2);

		final Set<Connection> connections = Sets.newLinkedHashSet();
		connections.add(connection1);

		transformation.setComponents(components);
		transformation.setConnections(connections);

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
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	public void testPOSTJSON() {

		// String responseMsg = target.path(resourceIdentifier).request()
		// .accept(MediaType.APPLICATION_JSON).post(Entity.json(transformationJSON.toString()), String.class);
		//
		// Assert.assertEquals("{\"message\":\"Hello World\"}", responseMsg);

		// POST method
		ClientResponse response = target.path(resourceIdentifier).request(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(transformationJSON.toString()), ClientResponse.class);

		// check response status code
		if (response.getStatus() != 200) {

			throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		}

		String result = null;

		try {
			result = IOUtils.toString(response.getEntityStream(), "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Assert.assertEquals("wrong", "bla", result);
	}
}
