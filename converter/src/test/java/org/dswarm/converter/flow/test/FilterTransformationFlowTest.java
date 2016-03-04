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
package org.dswarm.converter.flow.test;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dswarm.converter.flow.JSONTransformationFlow;
import org.dswarm.converter.flow.JSONTransformationFlowFactory;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.flow.utils.DMPConverterUtils;
import org.dswarm.converter.morph.MorphScriptBuilder;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class FilterTransformationFlowTest extends GuicedTest {

	@Test
	public void testFilterEndToEndWithOneResult() throws Exception {

		testFilter("test-mabxml.tuples.json", Optional.<String>empty(), "filtermorph.xml", "test-mabxml.filter.result.json");
	}

	/**
	 * multiple records
	 *
	 * @throws Exception
	 */
	@Test
	public void testFilterEndToEndWithOneResult2() throws Exception {

		testFilter("test-mabxml.tuples.3.json", Optional.<String>empty(), "filtermorph.xml", "test-mabxml.filter.result.1.2.json");
	}

	/**
	 * takes only the records where the value from field where feld->nr = 076 + feld->ind = v is 5
	 * in the transformation only the value from field where feld->nr = 076 + feld->ind = k will be selected
	 *
	 * @throws Exception
	 */
	@Test
	public void testFilterEndToEndWithOneResultSF() throws Exception {

		testFilter("test-mabxml.tuples.3.json", Optional.of("skipfiltermorph1.xml"), "transformationmorph1.xml", "skipfilter.morph.result.1.json");
	}

	/**
	 * takes only the records where the value from field where feld->nr = 076 + feld->ind = v is 5
	 * in the transformation only the value from field where feld->nr = 076 + feld->ind = k will be selected
	 * note: source has also a record with field with a value = 5 but where the other conditions don't match
	 *
	 * @throws Exception
	 */
	@Test
	public void testFilterEndToEndWithOneResultSF2() throws Exception {

		testFilter("test-mabxml.tuples.3.2.json", Optional.of("skipfiltermorph1.2.xml"), "transformationmorph1.xml",
				"skipfilter.morph.result.1.json");
	}

	/**
	 * field (with two values) occurs only once
	 *
	 * @throws Exception
	 */
	@Test
	public void testFilterEndToEndWithMultipleResults() throws Exception {

		testFilter("test-mabxml.tuples.2.json", Optional.<String>empty(), "filtermorph2.xml", "test-mabxml.filter.result.2.json");
	}

	/**
	 * field (with two values) occurs twice
	 *
	 * @throws Exception
	 */
	@Test
	public void testFilterEndToEndWithMultipleResults2() throws Exception {

		testFilter("test-mabxml.tuples.json", Optional.<String>empty(), "filtermorph2.xml", "test-mabxml.filter.result.2.1.json");
	}

	@Test
	public void testFilterEndToEndWithMultipleResultsSF() throws Exception {

		testFilter("test-mabxml.tuples.4.json", Optional.of("skipfiltermorph2.xml"), "transformationmorph2.xml",
				"test-mabxml.filter.result.2.2.json");
	}

	/**
	 * selects the 2nd value of the 2nd match
	 *
	 * @throws Exception
	 */
	@Test
	public void testFilterEndToEndWithMultipleResultsAndRepeatableElements() throws Exception {

		testFilter("test-mabxml.tuples.json", Optional.<String>empty(), "filtermorph3.xml", "test-mabxml.filter.result.3.json");
	}

	/**
	 * selects the 2nd value of the 2nd match
	 *
	 * @throws Exception
	 */
	@Test
	public void testFilterEndToEndWithMultipleResultsAndRepeatableElementsSF() throws Exception {

		testFilter("test-mabxml.tuples.5.json", Optional.of("skipfiltermorph2.xml"), "transformationmorph3.xml",
				"test-mabxml.filter.result.3.1.json");
	}

	/**
	 * selects the 2nd value
	 *
	 * @throws Exception
	 */
	@Test
	public void testFilterEndToEndWithMultipleResultsAndRepeatableElementsSF2() throws Exception {

		testFilter("test-mabxml.tuples.5.json", Optional.of("skipfiltermorph2.xml"), "transformationmorph3.1.xml",
				"test-mabxml.filter.result.3.2.json");
	}

	@Test
	public void testFilterEndToEndWithMultipleResultsAndSelectingSpecificIndex() throws Exception {

		testFilter("test-mabxml.tuples.json", Optional.<String>empty(), "filtermorph4.xml", "test-mabxml.filter.result.4.json");
	}

	/**
	 * ... of all occurences, i.e., the values could be part of different fields, e.g., as it is the case in the 4th record (there the first field contains only one value)
	 *
	 * @throws Exception
	 */
	@Test
	public void testFilterEndToEndWithMultipleResultsAndSelectingSpecificIndexSF() throws Exception {

		testFilter("test-mabxml.tuples.5.json", Optional.of("skipfiltermorph2.xml"), "filtermorph4.xml", "test-mabxml.filter.result.4.1.json");
	}

	@Test
	public void testFilterAndSelectingValueIsOnAnotherHierarchy() throws Exception {

		testFilter("ralfs_mabxml.tuples.json", Optional.<String>empty(), "filtermorph5.xml", "test-ralfs_mabxml.filter.result.5.json");
	}

	@Test
	public void testFilterAndSelectingValueIsOnAnotherHierarchySF() throws Exception {

		testFilter("ralfs_mabxml.tuples.json", Optional.of("skipfiltermorph3.xml"), "filtermorph5.xml", "test-ralfs_mabxml.filter.result.5.1.json");
	}

	@Test
	public void testFilterAndSelectingValueIsOnAnotherHierarchy2() throws Exception {

		testFilter("ralfs_mabxml.tuples.json", Optional.<String>empty(), "filtermorph7.xml", "test-ralfs_mabxml.filter.result.7.json");
	}

	@Test
	public void testFilterAndSelectingValueIsOnAnotherHierarchy2SF() throws Exception {

		testFilter("ralfs_mabxml.tuples.json", Optional.of("skipfiltermorph4.xml"), "filtermorph7.xml", "test-ralfs_mabxml.filter.result.7.1.json");
	}

	@Test
	public void testFilterAndSelectingValueIsOnAnotherHierarchyAndSelectingSpecificIndex() throws Exception {

		testFilter("ralfs_mabxml.tuples.json", Optional.<String>empty(), "filtermorph6.xml", "test-ralfs_mabxml.filter.result.6.json");
	}

	@Test
	public void testFilterAndSelectingValueIsOnAnotherHierarchyAndSelectingSpecificIndexSF() throws Exception {

		testFilter("ralfs_mabxml.tuples.json", Optional.of("skipfiltermorph5.xml"), "filtermorph6.xml", "test-ralfs_mabxml.filter.result.6.1.json");
	}

	@Test
	public void testFilterEndToEndWithMorphScriptBuilder() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test-mabxml.filter.morphscript.result.json");

		final JSONTransformationFlowFactory flowFactory = GuicedTest.injector
				.getInstance(JSONTransformationFlowFactory.class);

		final String request = DMPPersistenceUtil.getResourceAsString("task.filter.json");

		final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

		final Task task = objectMapper.readValue(request, Task.class);

		final String morphScriptString = new MorphScriptBuilder().apply(task).toString();

		final JSONTransformationFlow flow = flowFactory.fromString(morphScriptString);

		flow.getScript();

		final String actual = flow.applyResource("test-mabxml.tuples.json").toBlocking().first();
		final ArrayNode actualJSONArray = DMPPersistenceUtil.getJSONObjectMapper().readValue(actual, ArrayNode.class);
		final JsonNode cleanedActualJSONArray = DMPConverterUtils.removeRecordIdFields(actualJSONArray);
		final String finalActual = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(cleanedActualJSONArray);

		final ArrayNode expectedJson = replaceKeyWithActualKey(expected, actual);
		final JsonNode cleanedExpectedJson = DMPConverterUtils.removeRecordIdFields(expectedJson);
		final String finalExpected = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(cleanedExpectedJson);

		Assert.assertEquals(finalExpected, finalActual);
	}

	private void testFilter(final String inputTuplesFileName, final Optional<String> optionalSkipFilterMorphScriptFileName,
			final String transformationMorphScriptFileName, final String resultFileName)
			throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString(resultFileName);

		final JSONTransformationFlowFactory flowFactory = GuicedTest.injector
				.getInstance(JSONTransformationFlowFactory.class);

		final JSONTransformationFlow flow;

		if (optionalSkipFilterMorphScriptFileName.isPresent()) {

			flow = flowFactory.fromFile(transformationMorphScriptFileName, optionalSkipFilterMorphScriptFileName.get());
		} else {

			flow = flowFactory.fromFile(transformationMorphScriptFileName);
		}

		final String actual = flow.applyResource(inputTuplesFileName).toBlocking().first();

		final ArrayNode expectedJson = replaceKeyWithActualKey(expected, actual);
		final String finalExpected = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(expectedJson);

		final ObjectMapper objectMapper2 = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(
				SerializationFeature.INDENT_OUTPUT, true);

		final ArrayNode array = objectMapper2.readValue(actual, ArrayNode.class);
		final String finalActual = objectMapper2.writeValueAsString(array);

		final ArrayNode expectedArray = objectMapper2.readValue(finalExpected, ArrayNode.class);
		final String finalExpected2 = objectMapper2.writeValueAsString(expectedArray);

		JSONAssert.assertEquals(finalExpected2, finalActual, true);
	}

	private ArrayNode replaceKeyWithActualKey(final String expected, final String actual) throws IOException {

		// replace key with actual key
		final ArrayNode expectedJson = DMPPersistenceUtil.getJSONObjectMapper().readValue(expected, ArrayNode.class);

		final Iterator<JsonNode> iter = expectedJson.iterator();
		int iterCount = 0;

		while (iter.hasNext()) {

			final ObjectNode expectedTuple = (ObjectNode) iter.next();
			final String expectedFieldName = expectedTuple.fieldNames().next();
			final ArrayNode expectedContent = (ArrayNode) expectedTuple.get(expectedFieldName);
			final int expectedContentSize = expectedContent.size();
			ObjectNode expectedContentJson = null;
			JsonNode typeNode = null;

			for (final JsonNode expectedContentJsonCandidate : expectedContent) {

				final String expectedContentJsonFieldName = expectedContentJsonCandidate.fieldNames().next();

				if (expectedContentJsonFieldName.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {

					typeNode = expectedContentJsonCandidate;

					continue;
				}

				expectedContentJson = (ObjectNode) expectedContentJsonCandidate;
			}

			if (expectedContentJson == null) {

				iterCount++;

				continue;
			}

			Assert.assertNotNull("expected content JSON shouldn't be null", expectedContentJson);

			final JsonNode expectedContentValue = expectedContentJson.get(expectedContentJson.fieldNames().next());

			Assert.assertNotNull("the actual transformation result shouldn't be null", actual);
			final ArrayNode actualJson = DMPPersistenceUtil.getJSONObjectMapper().readValue(actual, ArrayNode.class);
			Assert.assertNotNull("the deserialised JSON array of the actual transformation result shouldn't be null", actualJson);
			final ObjectNode actualTuple = (ObjectNode) actualJson.get(iterCount);
			Assert.assertNotNull("the tuple of the deserialised JSON array of the actual transformation result shouldn't be null", actualTuple);
			final JsonNode actualContent = actualTuple.get(actualTuple.fieldNames().next());
			Assert.assertNotNull("the content of the tuple of the deserialised JSON array of the actual transformation result shouldn't be null",
					actualContent);
			Assert.assertTrue("the actual content should be a JSON array", actualContent.isArray());
			JsonNode actualContentJson = null;
			Integer typeNodePosition = null;
			int i = 0;

			for (final JsonNode actualContentJsonCandidate : actualContent) {

				i++;

				final String actualContentJsonFieldName = actualContentJsonCandidate.fieldNames().next();

				if (actualContentJsonFieldName.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {

					typeNodePosition = i;

					continue;
				}

				actualContentJson = actualContentJsonCandidate;
			}

			final ObjectNode newExpectedContentJson;

			if ((expectedContentSize > 1 && typeNode != null) || (typeNode == null && expectedContentSize == 1)) {

				Assert.assertNotNull("the actual content JSON shouldn't be null", actualContentJson);
				Assert.assertTrue("the actual content JSON should be a JSON object", actualContentJson.isObject());

				newExpectedContentJson = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();
				newExpectedContentJson.set(actualContentJson.fieldNames().next(), expectedContentValue);
			} else {

				newExpectedContentJson = null;
			}

			final ArrayNode newExpectedContent = DMPPersistenceUtil.getJSONObjectMapper().createArrayNode();

			if (typeNode != null && newExpectedContentJson != null) {

				if (typeNodePosition != null) {

					if (typeNodePosition == 1) {

						newExpectedContent.add(typeNode);
						newExpectedContent.add(newExpectedContentJson);
					} else {

						newExpectedContent.add(newExpectedContentJson);
						newExpectedContent.add(typeNode);
					}
				} else {

					newExpectedContent.add(newExpectedContentJson);
				}
			} else if (newExpectedContentJson != null) {

				newExpectedContent.add(newExpectedContentJson);
			} else {

				newExpectedContent.add(typeNode);
			}

			expectedTuple.set(expectedFieldName, newExpectedContent);

			iterCount++;
		}

		return expectedJson;
	}
}
