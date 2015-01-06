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
package org.dswarm.converter.flow.test;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Provider;
import org.junit.Assert;
import org.junit.Test;

import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.flow.TransformationFlow;
import org.dswarm.converter.morph.MorphScriptBuilder;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class FilterTransformationFlowTest extends GuicedTest {

	@Test
	public void testFilterEndToEndWithOneResult() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test-mabxml.filter.result.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = GuicedTest.injector
				.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromFile("filtermorph.xml", internalModelServiceFactoryProvider);

		final String actual = flow.applyResource("test-mabxml.tuples.json");

		final ArrayNode expectedJson = replaceKeyWithActualKey(expected, actual);
		final String finalExpected = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(expectedJson);

		Assert.assertEquals(finalExpected, actual);
	}

	@Test
	public void testFilterEndToEndWithMultipleResults() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test-mabxml.filter.result.2.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = GuicedTest.injector
				.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromFile("filtermorph2.xml", internalModelServiceFactoryProvider);

		final String actual = flow.applyResource("test-mabxml.tuples.2.json");

		final ArrayNode expectedJson = replaceKeyWithActualKey(expected, actual);
		final String finalExpected = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(expectedJson);

		Assert.assertEquals(finalExpected, actual);
	}

	/**
	 * selects the 2nd value of the 2nd match
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFilterEndToEndWithMultipleResultsAndRepeatableElements() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test-mabxml.filter.result.3.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = GuicedTest.injector
				.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromFile("filtermorph3.xml", internalModelServiceFactoryProvider);

		final String actual = flow.applyResource("test-mabxml.tuples.json");

		final ArrayNode expectedJson = replaceKeyWithActualKey(expected, actual);
		final String finalExpected = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(expectedJson);

		Assert.assertEquals(finalExpected, actual);
	}

	@Test
	public void testFilterEndToEndWithMultipleResultsAndSelectingSpecificIndex() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test-mabxml.filter.result.4.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = GuicedTest.injector
				.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromFile("filtermorph4.xml", internalModelServiceFactoryProvider);

		final String actual = flow.applyResource("test-mabxml.tuples.json");

		final ArrayNode expectedJson = replaceKeyWithActualKey(expected, actual);
		final String finalExpected = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(expectedJson);

		Assert.assertEquals(finalExpected, actual);
	}

	@Test
	public void testFilterAndSelectingValueIsOnAnotherHierarchy() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test-ralfs_mabxml.filter.result.5.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = GuicedTest.injector
				.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromFile("filtermorph5.xml", internalModelServiceFactoryProvider);

		final String actual = flow.applyResource("ralfs_mabxml.tuples.json");

		final ArrayNode expectedJson = replaceKeyWithActualKey(expected, actual);
		final String finalExpected = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(expectedJson);

		Assert.assertEquals(finalExpected, actual);
	}

	@Test
	public void testFilterAndSelectingValueIsOnAnotherHierarchy2() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test-ralfs_mabxml.filter.result.7.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = GuicedTest.injector
				.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromFile("filtermorph7.xml", internalModelServiceFactoryProvider);

		final String actual = flow.applyResource("ralfs_mabxml.tuples.json");

		final ArrayNode expectedJson = replaceKeyWithActualKey(expected, actual);
		final String finalExpected = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(expectedJson);

		Assert.assertEquals(finalExpected, actual);
	}

	@Test
	public void testFilterAndSelectingValueIsOnAnotherHierarchyAndSelectingSpecificIndex() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test-ralfs_mabxml.filter.result.6.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = GuicedTest.injector
				.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromFile("filtermorph6.xml", internalModelServiceFactoryProvider);

		final String actual = flow.applyResource("ralfs_mabxml.tuples.json");

		final ArrayNode expectedJson = replaceKeyWithActualKey(expected, actual);
		final String finalExpected = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(expectedJson);

		Assert.assertEquals(finalExpected, actual);
	}

	@Test
	public void testFilterEndToEndWithMorphScriptBuilder() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test-mabxml.filter.morphscript.result.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = GuicedTest.injector
				.getProvider(InternalModelServiceFactory.class);

		final String request = DMPPersistenceUtil.getResourceAsString("task.filter.json");

		final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

		final Task task = objectMapper.readValue(request, Task.class);

		final String morphScriptString = new MorphScriptBuilder().apply(task).toString();

		final TransformationFlow flow = TransformationFlow.fromString(morphScriptString, internalModelServiceFactoryProvider);

		flow.getScript();

		final String actual = flow.applyResource("test-mabxml.tuples.json");

		final ArrayNode expectedJson = replaceKeyWithActualKey(expected, actual);
		final String finalExpected = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(expectedJson);

		Assert.assertEquals(finalExpected, actual);
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

			Assert.assertNotNull("the actual content JSON shouldn't be null", actualContentJson);
			Assert.assertTrue("the actual content JSON should be a JSON object", actualContentJson.isObject());

			final ObjectNode newExpectedContentJson = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();
			newExpectedContentJson.put(actualContentJson.fieldNames().next(), expectedContentValue);
			final ArrayNode newExpectedContent = DMPPersistenceUtil.getJSONObjectMapper().createArrayNode();

			if (typeNode != null) {

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
			} else {

				newExpectedContent.add(newExpectedContentJson);
			}
			expectedTuple.put(expectedFieldName, newExpectedContent);

			iterCount++;
		}

		return expectedJson;
	}
}
