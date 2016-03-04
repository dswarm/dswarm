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
package org.dswarm.converter.flow.test.csv;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.dswarm.converter.flow.JSONTransformationFlow;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.flow.JSONTransformationFlowFactory;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler Created by tgaengler on 13/05/14.
 */
public class CSVTransformationFlowTest extends GuicedTest {

	@Test
	public void testCSVEndToEndWithEmptyValues() throws Exception {

		testCSVTaskWithTuples("test_csv_-_manipulated.result.json", "complex-transformation_on_csv.task.json", "test_csv_-_manipulated.tuples.json");
	}

	@Test
	public void testCSVEndToEndWithJobThatConsistsOfTwoMappingWhereOneMappingIsASimpleMapping() throws Exception {

		testCSVTaskWithTuples("test_transf.result.json", "dd-474.task.json", "test_transf.tuples.json");
	}

	@Test
	public void testCSVSubstringMapping() throws Exception {

		testCSVTaskWithTuples("test_transf2.result.json", "substring.task.json", "test_transf2.tuples.json");
	}

	@Test
	public void testCSVMultipleMappings() throws Exception {

		testCSVTaskWithTuples("demo_csv.multiple_mappings.result.json", "demo_csv.multiple_mappings.task.json", "demo_csv.tuples.json");
	}

	/**
	 * with skip filter
	 *
	 * @throws Exception
	 */
	@Test
	public void testCSVMultipleMappings2() throws Exception {

		testCSVTaskWithTuples("demo_csv.multiple_mappings.result.2.json", "demo_csv.multiple_mappings.task.2.json", "demo_csv.tuples.json");
	}

	/**
	 * with skip filter
	 *
	 * @throws Exception
	 */
	@Test
	public void testCSVMultipleMappings2Morph() throws Exception {

		testCSVMorphWithTuples("demo_csv.multiple_mappings.result.2.json", "transformationmorph4.xml", "demo_csv.tuples.json",
				Optional.of("skipfiltermorph6.xml"));
	}

	@Test
	public void testCSVOneMappingWithMultipleFunctions() throws Exception {

		testCSVTaskWithTuples("dd-528.csv.task.result.json", "dd-528.csv.task.json", "test_transf.tuples.json");
	}

	@Test
	public void testCSVMultipleMappingsWithAlmostAllFunctions() throws Exception {

		testCSVTaskWithTuples("almost.all.functions.complex.test.csv.result.json", "almost.all.functions.complex.test.csv.task.json",
				"almost.all.functions.complex.test.csv.tuples.json");
	}

	@Test
	public void testCSVMultipleMappingsWithAlmostAllFunctions2() throws Exception {

		testCSVTaskWithTuples("almost.all.functions.complex.test.csv.result.1.json", "almost.all.functions.complex.test.csv.task.1.json",
				"almost.all.functions.complex.test.csv.tuples.json");
	}

	@Test
	public void testCSVFilterOutputBeforeFilterCondition() throws Exception {

		testCSVMorphWithTuples("dd-747.csv.morph.result.json", "dd-747.csv.morph.xml", "dd-747.csv.tuples.json", Optional.<String>empty());
	}

	@Test
	public void testCSVWSkipFilterFilterCondition() throws Exception {

		testCSVMorphWithTuples("skipfilter.morph.result.json", "transformationmorph.xml", "dd-747.csv.tuples.json",
				Optional.of("skipfiltermorph.xml"));
	}

	@Test
	public void testCSVMappingInputAndMappingOutputAreTheSame() throws Exception {

		testCSVTaskWithTuples("dd-767.csv.task.result.json", "dd-767.csv.task.json", "dd-747.csv.tuples.json");
	}

	private void testCSVTaskWithTuples(final String taskResultJSONFileName, final String taskJSONFileName, final String tuplesJSONFileName)
			throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString(taskResultJSONFileName);

		final JSONTransformationFlowFactory flowFactory = GuicedTest.injector
				.getInstance(JSONTransformationFlowFactory.class);

		final String finalTaskJSONString = DMPPersistenceUtil.getResourceAsString(taskJSONFileName);
		final ObjectMapper objectMapper = DMPPersistenceUtil.getJSONObjectMapper();

		// looks like that the utilised ObjectMappers getting a bit mixed, i.e., actual sometimes delivers a result that is not in
		// pretty print and sometimes it is in pretty print ... (that's why the reformatting of both - expected and actual)
		final ObjectMapper objectMapper2 = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(
				SerializationFeature.INDENT_OUTPUT, true);

		final Task task = objectMapper.readValue(finalTaskJSONString, Task.class);

		final JSONTransformationFlow flow = flowFactory.fromTask(task);

		flow.getScript();

		final String actual = flow.applyResource(tuplesJSONFileName).toBlocking().first();
		final ArrayNode array = objectMapper2.readValue(actual, ArrayNode.class);
		final String finalActual = objectMapper2.writeValueAsString(array);

		final ArrayNode expectedArray = objectMapper2.readValue(expected, ArrayNode.class);
		final String finalExpected = objectMapper2.writeValueAsString(expectedArray);

		JSONAssert.assertEquals(finalExpected, finalActual, true);
	}

	private void testCSVMorphWithTuples(final String resultJSONFileName, final String morphXMLFileName, final String tuplesJSONFileName,
			final Optional<String> optionalSkipFilterMorphXMLFileName)
			throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString(resultJSONFileName);

		final JSONTransformationFlowFactory flowFactory = GuicedTest.injector
				.getInstance(JSONTransformationFlowFactory.class);

		// looks like that the utilised ObjectMappers getting a bit mixed, i.e., actual sometimes delivers a result that is not in
		// pretty print and sometimes it is in pretty print ... (that's why the reformatting of both - expected and actual)
		final ObjectMapper objectMapper2 = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(
				SerializationFeature.INDENT_OUTPUT, true);

		// final Task task = objectMapper.readValue(finalTaskJSONString, Task.class);

		final JSONTransformationFlow flow;

		if (optionalSkipFilterMorphXMLFileName.isPresent()) {

			flow = flowFactory.fromFile(morphXMLFileName, optionalSkipFilterMorphXMLFileName.get());
		} else {

			flow = flowFactory.fromFile(morphXMLFileName);
		}

		flow.getScript();

		final String actual = flow.applyResource(tuplesJSONFileName).toBlocking().first();
		final ArrayNode array = objectMapper2.readValue(actual, ArrayNode.class);
		final String finalActual = objectMapper2.writeValueAsString(array);

		final ArrayNode expectedArray = objectMapper2.readValue(expected, ArrayNode.class);
		final String finalExpected = objectMapper2.writeValueAsString(expectedArray);

		JSONAssert.assertEquals(finalExpected, finalActual, true);
	}
}
