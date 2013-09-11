package de.avgl.dmp.converter.morph;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.avgl.dmp.init.util.DMPUtil;
import de.avgl.dmp.persistence.mapping.JsonToPojoMapper;
import de.avgl.dmp.persistence.model.Transformation;

public class MorphScriptBuilderTest {

	@Test
	public void testRequestToMorph() throws Exception {

		final String request = DMPUtil.getResourceAsString("complex-request.json");
		final String expected = DMPUtil.getResourceAsString("complex-metamorph.xml");

		List<Transformation> pojos = new JsonToPojoMapper().apply(request);
		final String actual = new MorphScriptBuilder().apply(pojos).toString();

		assertEquals(expected, actual);
	}
}
