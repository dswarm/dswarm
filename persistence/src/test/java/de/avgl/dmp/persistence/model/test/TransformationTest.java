package de.avgl.dmp.persistence.model.test;

import java.util.List;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import de.avgl.dmp.persistence.model.Component;
import de.avgl.dmp.persistence.model.ComponentType;
import de.avgl.dmp.persistence.model.Connection;
import de.avgl.dmp.persistence.model.ConnectionType;
import de.avgl.dmp.persistence.model.Transformation;

public class TransformationTest {

	@Test
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

			System.out.println("something went wrong");

			Assert.assertTrue("something went wrong", false);
		}

		System.out.println("your json: \n\n" + str);

		Assert.assertTrue(true);
	}

}
