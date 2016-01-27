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
package org.dswarm.persistence.model.job.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class FunctionTest extends GuicedTest {

	private static final Logger	LOG				= LoggerFactory.getLogger(FunctionTest.class);

	private final ObjectMapper	objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	@Test
	public void simpleFunctionTest() throws Exception {

		final String functionName = "trim";
		final String functionDescription = "trims leading and trailing whitespaces from a given string";
		final String functionParameter = "inputString";
		final String functionFunctionDescriptionString = DMPPersistenceUtil.getResourceAsString("function_description.prettyprint.json");

		Assert.assertNotNull("the function description JSON string shouldn't be null", functionFunctionDescriptionString);

		final ObjectNode functionFunctionDescription = objectMapper.readValue(functionFunctionDescriptionString, ObjectNode.class);

		Assert.assertNotNull("the function description JSON shouldn't be null", functionFunctionDescription);

		final String uuid = UUIDService.getUUID(Function.class.getSimpleName());

		final Function function = new Function(uuid);
		function.setName(functionName);
		function.setDescription(functionDescription);
		function.addParameter(functionParameter);
		function.setFunctionDescription(functionFunctionDescription);

		Assert.assertNotNull("the function name shouldn't be null", function.getName());
		Assert.assertEquals("the function names are not equal", functionName, function.getName());
		Assert.assertNotNull("the function description shouldn't be null", function.getDescription());
		Assert.assertEquals("the function descriptions are not equal", functionDescription, function.getDescription());
		Assert.assertNotNull("the function description JSON shouldn't be null", function.getFunctionDescription());

		final String functionDescriptionJSONString = objectMapper.writeValueAsString(function.getFunctionDescription());

		final String functionFunctionDescriptionJSONString = objectMapper.writeValueAsString(functionFunctionDescription);

		Assert.assertEquals("the function description JSON strings are not equal", functionFunctionDescriptionJSONString,
				functionDescriptionJSONString);
		Assert.assertNotNull("the function parameters shouldn't be null", function.getParameters());
		Assert.assertEquals("the function parameters' size are not equal", 1, function.getParameters().size());
		Assert.assertEquals("the function parameter '" + functionParameter + "' are not equal", functionParameter, function.getParameters().get(0));

		String json = null;

		try {

			json = objectMapper.writeValueAsString(function);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		FunctionTest.LOG.debug("function json: " + json);
	}

}
