package de.avgl.dmp.controller;


import de.avgl.dmp.controller.mapping.JsonToPojoMapper;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.converter.resources.PojoToXMLBuilder;
import de.avgl.dmp.persistence.model.Transformation;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class EndToEndTest extends AbstractBaseTest {

	@Test
	public void testEndToEnd() throws Exception {

		final String request = getResourceAsString("complex-request.json");
		final String expected = getResourceAsString("complex-result.json");

		List<Transformation> pojos = new JsonToPojoMapper().apply(request);
		final TransformationFlow flow = TransformationFlow.from(pojos);

		final String actual = flow.apply();

		assertEquals(expected, actual);
	}

	@Test
	public void testMorphToEnd() throws Exception {

		final String expected = getResourceAsString("complex-result.json");

		final TransformationFlow flow = TransformationFlow.from("complex-metamorph.xml");

		final String actual = flow.apply();

		assertEquals(expected, actual);
	}

	@Test
	public void testRequestToMorph() throws Exception {

		final String request = getResourceAsString("complex-request.json");
		final String expected = getResourceAsString("complex-metamorph.xml");

		List<Transformation> pojos = new JsonToPojoMapper().apply(request);
		final String actual = new PojoToXMLBuilder().apply(pojos).toString();

		assertEquals(expected, actual);
	}
}
