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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.dswarm.converter.flow.JSONTransformationFlowFactory;
import org.junit.Assert;
import org.junit.Test;

import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.flow.JSONTransformationFlow;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author polowins, based on CSVTransformationFlowTest
 */
public class CSVTransformationFlowTestSorted extends GuicedTest {

	@Test
	public void testCSVEndToEndWithJobThatConsistsOfTwoMappingWhereOneMappingIsASimpleMapping() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test_transf_sorted.result.json");

		final JSONTransformationFlowFactory flowFactory = GuicedTest.injector
				.getInstance(JSONTransformationFlowFactory.class);

		final String finalTaskJSONString = DMPPersistenceUtil.getResourceAsString("dd-474.task.json");
		final ObjectMapper objectMapper = DMPPersistenceUtil.getJSONObjectMapper();

		// looks like that the utilised ObjectMappers getting a bit mixed, i.e., actual sometimes delivers a result that is not in
		// pretty print and sometimes it is in pretty print ... (that's why the reformatting of both - expected and actual)
		final ObjectMapper objectMapper2 = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(
				SerializationFeature.INDENT_OUTPUT, true);

		final Task task = objectMapper.readValue(finalTaskJSONString, Task.class);

		final JSONTransformationFlow flow = flowFactory.fromTask(task);

		flow.getScript();

		final String actual = flow.applyResource("test_transf.tuples_sorted.json").toBlocking().first();
		final ArrayNode array = objectMapper2.readValue(actual, ArrayNode.class);
		final String finalActual = objectMapper2.writeValueAsString(array);

		final ArrayNode expectedArray = objectMapper2.readValue(expected, ArrayNode.class);
		final String finalExpected = objectMapper2.writeValueAsString(expectedArray);

		Assert.assertEquals(finalExpected, finalActual);
	}

}
