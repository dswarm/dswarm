/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.service.job.test;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.proxy.ProxyFunction;
import org.dswarm.persistence.service.job.FunctionService;
import org.dswarm.persistence.service.job.test.utils.FunctionServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class FunctionServiceTest extends IDBasicJPAServiceTest<ProxyFunction, Function, FunctionService> {

	private static final Logger LOG = LoggerFactory.getLogger(FunctionServiceTest.class);

	private FunctionServiceTestUtils functionServiceTestUtils;

	public FunctionServiceTest() {

		super("function", FunctionService.class);
	}

	@Override protected void initObjects() {

		super.initObjects();

		functionServiceTestUtils = new FunctionServiceTestUtils();
	}

	@Test
	@Override
	public void testSimpleObject() throws Exception {

		final Function function = functionServiceTestUtils.createDefaultObject();

		final Function updatedFunction = updateObjectTransactional(function).getObject();

		logObjectJSON(updatedFunction);
	}

	// @Test
	public void loadMetafactureSimpleFunctionSetTest() {

		String metafactureFunctionDescriptionsJSONString = null;

		try {

			metafactureFunctionDescriptionsJSONString = DMPPersistenceUtil.getResourceAsString("functions.json");
		} catch (final IOException e) {

			FunctionServiceTest.LOG.debug("something went wrong while reading the metafacture functions JSON file", e);

			Assert.assertTrue("something went wrong while reading the metafacture functions JSON file", false);
		}

		Assert.assertNotNull("the metafacture function descriptions JSON string shouldn't be null", metafactureFunctionDescriptionsJSONString);

		ObjectNode metafactureFunctionDescriptionsJSON = null;

		try {
			metafactureFunctionDescriptionsJSON = objectMapper.readValue(metafactureFunctionDescriptionsJSONString, ObjectNode.class);
		} catch (final JsonParseException e) {

			FunctionServiceTest.LOG.debug("something went wrong while deserializing the metafacture functions JSON string", e);

			Assert.assertTrue("something went wrong while deserializing the metafacture functions JSON string", false);
		} catch (final JsonMappingException e) {

			FunctionServiceTest.LOG.debug("something went wrong while deserializing the metafacture functions JSON string", e);

			Assert.assertTrue("something went wrong while deserializing the metafacture functions JSON string", false);
		} catch (final IOException e) {

			FunctionServiceTest.LOG.debug("something went wrong while deserializing the metafacture functions JSON string", e);

			Assert.assertTrue("something went wrong while deserializing the metafacture functions JSON string", false);
		}

		Assert.assertNotNull("the metafacture function descriptions JSON shouldn't be null", metafactureFunctionDescriptionsJSON);

		final JsonNode metafactureFunctionDescriptionsArrayNode = metafactureFunctionDescriptionsJSON.get("functions");

		Assert.assertNotNull("the metafacture function descriptions array node shouldn't be null", metafactureFunctionDescriptionsArrayNode);

		Assert.assertTrue("the metafacture function description array node should be an JSON array",
				metafactureFunctionDescriptionsArrayNode.isArray());

		final ArrayNode metafactureFunctionDescriptionsArray = (ArrayNode) metafactureFunctionDescriptionsArrayNode;

		for (final JsonNode metafactureFunctionDescriptionNode : metafactureFunctionDescriptionsArray) {

			final ObjectNode metafactureFunctionDescriptionJSON = (ObjectNode) metafactureFunctionDescriptionNode;

			final JsonNode metafactureFunctionName = metafactureFunctionDescriptionNode.get("name");

			String functionName = null;

			if (metafactureFunctionName != null) {

				functionName = metafactureFunctionName.asText();
			}

			final JsonNode metafactureFunctionDescription = metafactureFunctionDescriptionJSON.get("description");

			String functionDescription = null;

			if (metafactureFunctionDescription != null) {

				functionDescription = metafactureFunctionDescription.asText();
			}

			// TODO: optimize parameter extraction (?)

			LinkedList<String> parameters = null;

			final JsonNode metafactureFunctionParametersNode = metafactureFunctionDescriptionJSON.get("parameters");

			if (metafactureFunctionParametersNode != null) {

				final ObjectNode metafactureFunctionParametersJSON = (ObjectNode) metafactureFunctionParametersNode;

				final Iterator<String> fieldNames = metafactureFunctionParametersJSON.fieldNames();

				if (fieldNames != null) {

					parameters = Lists.newLinkedList();

					while (fieldNames.hasNext()) {

						final String fieldName = fieldNames.next();

						if (fieldName != null) {

							parameters.add(fieldName);
						}
					}
				}
			}

			final Function function = createObject().getObject();

			if (functionName != null) {

				function.setName(functionName);
			}

			if (functionDescription != null) {

				function.setDescription(functionDescription);
			}

			if (parameters != null && !parameters.isEmpty()) {

				function.setParameters(parameters);
			}

			function.setFunctionDescription(metafactureFunctionDescriptionJSON);

			final Function updatedFunction = updateObjectTransactional(function).getObject();

			Assert.assertNotNull("the updated metafacture function shouldn't be null", updatedFunction);
		}
	}
}
