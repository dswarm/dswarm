/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	public void testCSVMultipleMappingsWithAlmostAllFunctionsToMorph2() throws Exception {

		compareTaskGeneratedMorphscript("almost.all.functions.complex.test.csv.task.1.json", "almost.all.functions.complex.test.csv.morph.1.xml");
	}

	@Test
	public void testMetsmodsXmlWithFilterAndMappingToMorph() throws Exception {

		compareTaskGeneratedMorphscript("metsmods_small.xml.task.json", "metsmods_small.xml.morph.xml");
	}

	@Test
	public void testMabxmlOneMappingWithFilterAndMultipleFunctionsToMorph() throws Exception {

		compareTaskGeneratedMorphscript("dd-528.mabxml.task.json", "dd-528.mabxml.morph.xml");
	}

	@Test
	public void testCollectFunctionToMorph() throws Exception {

		compareTaskGeneratedMorphscript("dd-1220.mabxml.task.json", "dd-1220.mabxml.morph.xml");
	}

	@Test
	public void testHTTPAPIFunctionToMorph() throws Exception {

		compareTaskGeneratedMorphscript("dd-72.mabxml.task.json", "dd-72.mabxml.morph.xml");
	}

	@Test
	public void testParseJSONFunctionToMorph() throws Exception {

		compareTaskGeneratedMorphscript("dd-970.mabxml.task.json", "dd-970.mabxml.morph.xml");
	}

	@Test
	public void tesDd906LookupFunction() throws Exception {

		compareTaskGeneratedMorphscript("dd-906.lookups.task.json", "dd-906.lookups.morph.result.xml");
	}

	@Test
	public void tesDd1005Task() throws Exception {

		compareTaskGeneratedMorphscript("dd-1005.csv.task.json", "dd-1005.csv.morph.result.xml");
	}

	@Test
	public void testIfElseTask() throws Exception {

		compareTaskGeneratedMorphscript("if-else.collectors.task.json", "if-else.collectors.morph.xml");
	}

	@Test
	public void testNumFilterTask() throws Exception {

		compareTaskGeneratedMorphscript("numfilter.function.task.json", "numfilter.function.morph.xml");
	}

	@Test
	public void testEqualsFilterTask() throws Exception {

		compareTaskGeneratedMorphscript("equals.filter.function.task.json", "equals.filter.function.morph.xml");
	}

	@Test
	public void testNotEqualsFilterTask() throws Exception {

		compareTaskGeneratedMorphscript("not-equals.filter.function.task.json", "not-equals.filter.function.morph.xml");
	}

	@Test
	public void testSqlMapTask() throws Exception {

		compareTaskGeneratedMorphscript("sqlmap.lookup.task.json", "sqlmap.lookup.morph.xml");
	}

	@Test
	public void testSqlMapTask2() throws Exception {

		compareTaskGeneratedMorphscript("dd-1386.task.json", "dd-1386.task.morph.xml");
	}

	@Test
	public void testSqlMapTask3() throws Exception {

		compareTaskGeneratedMorphscript("dd-1386.task.2.json", "dd-1386.task.2.morph.xml");
	}

	@Test
	public void testConvertValueTask() throws Exception {

		compareTaskGeneratedMorphscript("convertvalue.task.json", "convertvalue.morph.xml");
	}

	@Test
	public void testISSNTask() throws Exception {

		compareTaskGeneratedMorphscript("issn.task.json", "issn.morph.xml");
	}

	@Test
	public void testRegexLookupMap() throws Exception {

		compareTaskGeneratedMorphscript("dd-962.task.json", "dd-962.regexlookup.morph.result.xml");
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
