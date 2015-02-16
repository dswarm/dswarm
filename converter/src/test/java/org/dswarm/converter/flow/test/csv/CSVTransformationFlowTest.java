/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Provider;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.flow.TransformationFlow;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.service.InternalModelServiceFactory;
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
	public void testCSVFilterOutputBeforeFilterCondition() throws Exception {

		testCSVMorphWithTuples("dd-747.csv.morph.result.json", "dd-747.csv.morph.xml", "dd-747.csv.tuples.json");
	}

	@Test
	public void testCSVMappingInputAndMappingOutputAreTheSame() throws Exception {

		testCSVTaskWithTuples("dd-767.csv.task.result.json", "dd-767.csv.task.json", "dd-747.csv.tuples.json");
	}

	private void testCSVTaskWithTuples(final String taskResultJSONFileName, final String taskJSONFileName, final String tuplesJSONFileName)
			throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString(taskResultJSONFileName);

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = GuicedTest.injector
				.getProvider(InternalModelServiceFactory.class);

		final String finalTaskJSONString = DMPPersistenceUtil.getResourceAsString(taskJSONFileName);
		final ObjectMapper objectMapper = DMPPersistenceUtil.getJSONObjectMapper();

		// looks like that the utilised ObjectMappers getting a bit mixed, i.e., actual sometimes delivers a result that is not in
		// pretty print and sometimes it is in pretty print ... (that's why the reformatting of both - expected and actual)
		final ObjectMapper objectMapper2 = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(
				SerializationFeature.INDENT_OUTPUT, true);

		final Task task = objectMapper.readValue(finalTaskJSONString, Task.class);

		final TransformationFlow flow = TransformationFlow.fromTask(task, internalModelServiceFactoryProvider);

		flow.getScript();

		final String actual = flow.applyResource(tuplesJSONFileName);
		final ArrayNode array = objectMapper2.readValue(actual, ArrayNode.class);
		final String finalActual = objectMapper2.writeValueAsString(array);

		final ArrayNode expectedArray = objectMapper2.readValue(expected, ArrayNode.class);
		final String finalExpected = objectMapper2.writeValueAsString(expectedArray);

		JSONAssert.assertEquals(finalExpected, finalActual, true);
	}

	private void testCSVMorphWithTuples(final String resultJSONFileName, final String morphXMLFileName, final String tuplesJSONFileName)
			throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString(resultJSONFileName);

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = GuicedTest.injector
				.getProvider(InternalModelServiceFactory.class);

		final String finalMorphXmlString = DMPPersistenceUtil.getResourceAsString(morphXMLFileName);

		// looks like that the utilised ObjectMappers getting a bit mixed, i.e., actual sometimes delivers a result that is not in
		// pretty print and sometimes it is in pretty print ... (that's why the reformatting of both - expected and actual)
		final ObjectMapper objectMapper2 = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(
				SerializationFeature.INDENT_OUTPUT, true);

		// final Task task = objectMapper.readValue(finalTaskJSONString, Task.class);

		final TransformationFlow flow = TransformationFlow.fromString(finalMorphXmlString, internalModelServiceFactoryProvider);

		flow.getScript();

		final String actual = flow.applyResource(tuplesJSONFileName);
		final ArrayNode array = objectMapper2.readValue(actual, ArrayNode.class);
		final String finalActual = objectMapper2.writeValueAsString(array);

		final ArrayNode expectedArray = objectMapper2.readValue(expected, ArrayNode.class);
		final String finalExpected = objectMapper2.writeValueAsString(expectedArray);

		JSONAssert.assertEquals(finalExpected, finalActual, true);
	}
}
