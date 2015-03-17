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
package org.dswarm.converter.flow.test.xml;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.flow.TransformationFlow;
import org.dswarm.converter.flow.TransformationFlowFactory;
import org.dswarm.converter.flow.utils.DMPConverterUtils;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler Created by tgaengler on 16/05/14.
 */
public class XMLTransformationFlowTest extends GuicedTest {

	@Test
	public void testMabxmlComplexMapping() throws Exception {

		testXMLTaskWithTuples("mabxml_dmp.complex-mapping.result.json", "mabxml_dmp.complex-mapping.task.json", "mabxml_dmp.tuples.json");
	}

	@Test
	public void testMabxmlWFilterMapping() throws Exception {

		testXMLTaskWithTuples("mabxml_w_filter.task.result.json", "mabxml_w_filter.task.json", "test-mabxml.tuples.json");
	}

	@Test
	public void testMabxmlWFilterMapping2() throws Exception {

		testXMLTaskWithTuples("mabxml_w_filter_2.task.result.json", "mabxml_w_filter_2.task.json", "mabxml_dmp.tuples.json");
	}

	@Test
	public void testMabxmlWFilterMappings() throws Exception {

		testXMLTaskWithTuples("tgtest_mabxml_mo_proj.task.result.json", "tgtest_mabxml_mo_proj.task.json", "test-mabxml.tuples.json");
	}

	@Test
	public void testMabxmlOneMappingWithFilterAndMultipleFunctions() throws Exception {

		testXMLTaskWithTuples("dd-528.mabxml.task.result.json", "dd-528.mabxml.task.json", "mabxml_dmp.tuples.json");
	}

	@Test
	public void testMabxmlConcatOneMappingOnFeldValueWithTwoFiltersMorph() throws Exception {

		testXMLMorphWithTuples("dd-530.mabxml.morph.result.json", "dd-530.mabxml.morph.xml", "test-mabxml.tuples.json");
	}

	@Test
	public void testMabxmlConcatOneMappingOnFeldValueWithTwoFiltersTask() throws Exception {

		testXMLTaskWithTuples("dd-530.mabxml.task.result.json", "dd-530.mabxml.task.json", "test-mabxml.tuples.json");
	}

	@Test
	public void testMabxmlConcatOneMappingOnFeldValueWithTwoFiltersTask2() throws Exception {

		testXMLTaskWithTuples("dd-530.mabxml.task.result.1.json", "dd-530.mabxml.task.1.json", "test-mabxml.tuples.json");
	}

	@Test
	public void testMabxmlFilterWithRegexMorph() throws Exception {

		testXMLMorphWithTuples("dd-650.mabxml.morph.result.json", "dd-650.mabxml.morph.xml", "test-mabxml.tuples.json");
	}

	@Test
	public void testMabxmlFilterWithRegexTask() throws Exception {

		testXMLTaskWithTuples("dd-650.mabxml.task.result.json", "dd-650.mabxml.task.json", "test-mabxml.tuples.json");
	}

	@Test
	public void testDd727Morph() throws Exception {

		testXMLMorphWithTuples("dd-727.mabxml.morph.result.json", "dd-727.mabxml.morph.xml", "dd-727.mabxml.tuples.json");
	}

	@Test
	public void testDd727Task() throws Exception {

		testXMLTaskWithTuples("dd-727.mabxml.task.result.json", "dd-727.mabxml.task.json", "dd-727.mabxml.tuples.json");
	}

	@Test
	public void testDd734Morph() throws Exception {

		testXMLMorphWithTuples("sub.entity.3level.mabxml.morph.result.json", "sub.entity.3level.mabxml.morph.xml", "mabxml.tuples.json");
	}

	@Test
	public void testDd905Morph() throws Exception {

		testXMLMorphWithTuples("dd-905.morph.result.json", "dd-905.morph.xml", "mabxml.tuples.json");
	}

	@Test
	public void testDd906Task() throws Exception {

		testXMLTaskWithTuples("dd-906.lookups.task.result.json", "dd-906.lookups.task.json", "mabxml.tuples.json");
	}

	@Test
	public void testDd907Morph() throws Exception {

		testXMLMorphWithTuples("dd-907.collectors.morph.result.json", "dd-907.collectors.morph.xml", "mabxml.tuples.json");
	}

	@Test
	public void testDd907Task() throws Exception {

		testXMLTaskWithTuples("dd-907.collectors.task.result.json", "dd-907.collectors.task.json", "test-mabxml.tuples.json");
	}

	@Test
	public void testDd980Task() throws Exception {

		testXMLTaskWithTuples("dd-980.xml.task.result.json", "dd-980.xml.task.json", "rvk_lokal_cdata.tuples.json");
	}

	@Test
	public void testDd980Morph() throws Exception {

		testXMLMorphWithTuples("dd-980.xml.morph.result.json", "dd-980.morph.xml", "rvk_lokal_cdata.tuples.json");
	}

	@Test
	public void testMetsmodsXmlWithFilterAndMapping() throws Exception {

		testXMLTaskWithTuples("metsmods_small.xml.task.result.json", "metsmods_small.xml.task.json", "metsmods_small.xml.tuples.json");
	}

	@Test
	public void testXMLWithFilterCommonAttributePathOnRoot() throws Exception {

		testXMLTaskWithTuples("dd-651.xml.task.result.json", "dd-651.xml.task.json", "testset5.xml.tuples.json");
	}

	private void testXMLTaskWithTuples(final String taskResultJSONFileName, final String taskJSONFileName, final String tuplesJSONFileName)
			throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString(taskResultJSONFileName);

		final TransformationFlowFactory flowFactory = GuicedTest.injector
				.getInstance(TransformationFlowFactory.class);

		final String finalTaskJSONString = DMPPersistenceUtil.getResourceAsString(taskJSONFileName);
		final ObjectMapper objectMapper = DMPPersistenceUtil.getJSONObjectMapper();

		// looks like that the utilised ObjectMappers getting a bit mixed, i.e., actual sometimes delivers a result that is not in
		// pretty print and sometimes it is in pretty print ... (that's why the reformatting of both - expected and actual)
		final ObjectMapper objectMapper2 = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(
				SerializationFeature.INDENT_OUTPUT, true);

		final Task task = objectMapper.readValue(finalTaskJSONString, Task.class);

		final TransformationFlow flow = flowFactory.fromTask(task);

		flow.getScript();

		final String actual = flow.applyResource(tuplesJSONFileName);
		final ArrayNode array = objectMapper2.readValue(actual, ArrayNode.class);

		final ArrayNode expectedArray = objectMapper2.readValue(expected, ArrayNode.class);

		final JsonNode acutalJSONNode = DMPConverterUtils.removeRecordIdFields(array);
		final JsonNode expectedJSONNode = DMPConverterUtils.removeRecordIdFields(expectedArray);

		final String finalActual = objectMapper2.writeValueAsString(acutalJSONNode);
		final String finalExpected = objectMapper2.writeValueAsString(expectedJSONNode);

		JSONAssert.assertEquals(finalExpected, finalActual, true);
	}

	private void testXMLMorphWithTuples(final String resultJSONFileName, final String morphXMLFileName, final String tuplesJSONFileName)
			throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString(resultJSONFileName);

		final TransformationFlowFactory flowFactory = GuicedTest.injector
				.getInstance(TransformationFlowFactory.class);

		final String finalMorphXmlString = DMPPersistenceUtil.getResourceAsString(morphXMLFileName);

		// looks like that the utilised ObjectMappers getting a bit mixed, i.e., actual sometimes delivers a result that is not in
		// pretty print and sometimes it is in pretty print ... (that's why the reformatting of both - expected and actual)
		final ObjectMapper objectMapper2 = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(
				SerializationFeature.INDENT_OUTPUT, true);

		// final Task task = objectMapper.readValue(finalTaskJSONString, Task.class);

		final TransformationFlow flow = flowFactory.fromString(finalMorphXmlString);

		flow.getScript();

		final String actual = flow.applyResource(tuplesJSONFileName);
		final ArrayNode array = objectMapper2.readValue(actual, ArrayNode.class);

		final ArrayNode expectedArray = objectMapper2.readValue(expected, ArrayNode.class);

		final JsonNode acutalJSONNode = DMPConverterUtils.removeRecordIdFields(array);
		final JsonNode expectedJSONNode = DMPConverterUtils.removeRecordIdFields(expectedArray);

		final String finalActual = objectMapper2.writeValueAsString(acutalJSONNode);
		final String finalExpected = objectMapper2.writeValueAsString(expectedJSONNode);

		JSONAssert.assertEquals(finalExpected, finalActual, true);
	}
}
