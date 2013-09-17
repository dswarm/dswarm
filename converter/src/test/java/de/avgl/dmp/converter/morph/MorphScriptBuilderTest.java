package de.avgl.dmp.converter.morph;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.avgl.dmp.init.util.DMPUtil;
import de.avgl.dmp.persistence.mapping.JsonToPojoMapper;
import de.avgl.dmp.persistence.model.job.Job;

public class MorphScriptBuilderTest {

	@Test
	public void testRequestToMorph() throws Exception {

		final String request = DMPUtil.getResourceAsString("complex-request.json");
		final String expected = DMPUtil.getResourceAsString("complex-metamorph.xml");

		final Job job = new JsonToPojoMapper().toJob(request);
		final String actual = new MorphScriptBuilder().apply(job.getTransformations()).toString();

		assertEquals(expected, actual);
	}
}
