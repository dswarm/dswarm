package org.dswarm.converter.morph;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import org.dswarm.converter.GuicedTest;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class MorphScriptBuilderTest extends GuicedTest {

	@Test
	public void testComplexMappingToMorph() throws Exception {

		compareTaskGeneratedMorphscript("complex-transformation.json", "complex-transformation.morph.xml");
	}

	@Test
	public void testSubstringMappingToMorph() throws Exception {

		compareTaskGeneratedMorphscript("substring.task.json", "substring.task.morph.xml");
	}

	@Test
	public void testDublicateDatasMappingToMorph() throws Exception {

		compareTaskGeneratedMorphscript("demo_csv.multiple_mappings.task.json", "demo_csv.multiple_mappings.task.result.xml");
	}

	@Test
	public void testFilterEncodingMappingToMorph() throws Exception {

		compareTaskGeneratedMorphscript("neuer_test.task.json", "neuer_test.task.morph.xml");
	}

	@Test
	public void testCSVOneMappingWithMultipleFunctionsToMorph() throws Exception {

		compareTaskGeneratedMorphscript("dd-528.csv.task.json", "dd-528.csv.morph.xml");
	}

	@Test
	public void testCSVMultipleMappingsWithAlmostAllFunctionsToMorph() throws Exception {

		compareTaskGeneratedMorphscript("almost.all.functions.complex.test.csv.task.json", "almost.all.functions.complex.test.csv.morph.xml");
	}

	@Test
	public void testMetsmodsXmlWithFilterAndMappingToMorph() throws Exception {

		compareTaskGeneratedMorphscript("metsmods_small.xml.task.json", "metsmods_small.xml.morph.xml");
	}

	@Test
	public void testMabxmlOneMappingWithFilterAndMultipleFunctionsToMorph() throws Exception {

		compareTaskGeneratedMorphscript("dd-528.mabxml.task.json", "dd-528.mabxml.morph.xml");
	}

	private void compareTaskGeneratedMorphscript(final String taskJSONFileName, final String morphFileName) throws Exception {

		final ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);

		final String request = DMPPersistenceUtil.getResourceAsString(taskJSONFileName);

		final String result = DMPPersistenceUtil.getResourceAsString(morphFileName);

		final Task task = objectMapper.readValue(request, Task.class);

		final String morphScriptString = new MorphScriptBuilder().apply(task).toString();

		Assert.assertEquals(result, morphScriptString);
	}
}
