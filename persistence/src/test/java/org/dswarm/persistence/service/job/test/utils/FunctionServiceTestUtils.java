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
package org.dswarm.persistence.service.job.test.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;

import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.proxy.ProxyFunction;
import org.dswarm.persistence.service.job.FunctionService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class FunctionServiceTestUtils extends BasicFunctionServiceTestUtils<FunctionService, ProxyFunction, Function> {

	public FunctionServiceTestUtils() {

		super(Function.class, FunctionService.class);
	}

	@Override public Function createObject(JsonNode objectDescription) throws Exception {
		return null;
	}

	@Override public Function createObject(String identifier) throws Exception {
		return null;
	}

	@Override public Function createDefaultObject() throws Exception {

		final String functionName = "trim";
		final String functionDescription = "trims leading and trailing whitespaces from a given string";
		final String functionParameter1 = "inputString";
		final String functionParameter2 = "parameter2";

		final String functionFunctionDescriptionString = DMPPersistenceUtil.getResourceAsString("function_description.prettyprint.json");

		Assert.assertNotNull("the function description JSON string shouldn't be null", functionFunctionDescriptionString);

		final ObjectNode functionFunctionDescription = objectMapper.readValue(functionFunctionDescriptionString, ObjectNode.class);

		Assert.assertNotNull("the function description JSON shouldn't be null", functionFunctionDescription);

		final Function function = new Function();

		function.setName(functionName);
		function.setDescription(functionDescription);
		function.addParameter(functionParameter1);
		function.addParameter(functionParameter2);
		function.setFunctionDescription(functionFunctionDescription);

		return createAndCompareObject(function, function);
	}

	@Override
	public void reset() {

	}
}
