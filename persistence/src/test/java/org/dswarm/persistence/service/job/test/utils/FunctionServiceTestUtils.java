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

import java.util.LinkedList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.junit.Assert;

import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.proxy.ProxyFunction;
import org.dswarm.persistence.service.job.FunctionService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class FunctionServiceTestUtils extends BasicFunctionServiceTestUtils<FunctionService, ProxyFunction, Function> {

	public FunctionServiceTestUtils() {

		super(Function.class, FunctionService.class);
	}

	@Override
	public Function createObject(final JsonNode objectDescription) throws Exception {
		return null;
	}

	@Override
	public Function createObject(final String identifier) throws Exception {
		return null;
	}

	@Override
	public Function createDefaultObject() throws Exception {

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

	public Function getSimpleTrimFunction() throws Exception {

		final LinkedList<String> parameters = Lists.newLinkedList();
		parameters.add("inputString");

		return createFunction("trim", "trims leading and trailing whitespaces from a given string",
				parameters);
	}

	public Function getSimpleReplaceFunction() throws Exception {

		final String function1Name = "replace";
		final String function1Description = "replace certain parts of a given string that matches a certain regex";
		final String function1Parameter = "inputString";
		final String function2Parameter = "regex";
		final String function3Parameter = "replaceString";

		final LinkedList<String> function1Parameters = Lists.newLinkedList();
		function1Parameters.add(function1Parameter);
		function1Parameters.add(function2Parameter);
		function1Parameters.add(function3Parameter);

		return createFunction(function1Name, function1Description, function1Parameters);
	}

	public Function getSimpleLowerCaseFunction() throws Exception {

		final String function2Name = "lower_case";
		final String function2Description = "lower cases all characters of a given string";
		final String function4Parameter = "inputString";

		final LinkedList<String> function2Parameters = Lists.newLinkedList();
		function2Parameters.add(function4Parameter);

		return createFunction(function2Name, function2Description, function2Parameters);
	}

	@Override
	public void reset() {

	}
}
