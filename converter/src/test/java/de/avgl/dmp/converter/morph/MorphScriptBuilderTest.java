package de.avgl.dmp.converter.morph;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.converter.GuicedTest;
import de.avgl.dmp.persistence.model.job.Task;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class MorphScriptBuilderTest extends GuicedTest {

	@Test
	public void testComplexMappingToMorph() throws Exception {

		final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

		final String request = DMPPersistenceUtil.getResourceAsString("complex-transformation.json");

		final Task task = objectMapper.readValue(request, Task.class);

		final String morphScriptString = new MorphScriptBuilder().apply(task).toString();

		// System.out.println(morphScriptString);

	}

}
