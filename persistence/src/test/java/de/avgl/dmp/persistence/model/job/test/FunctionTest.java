package de.avgl.dmp.persistence.model.job.test;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class FunctionTest extends GuicedTest {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(FunctionTest.class);

	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	@Test
	public void simpleFunctionTest() throws Exception {

		final String functionName = "trim";
		final String functionDescription = "trims leading and trailing whitespaces from a given string";
		final String functionParameter = "inputString";
		final String functionFunctionDescriptionString = DMPPersistenceUtil.getResourceAsString("function_description.prettyprint.json");

		Assert.assertNotNull("the function description JSON string shouldn't be null", functionFunctionDescriptionString);

		final ObjectNode functionFunctionDescription = objectMapper.readValue(functionFunctionDescriptionString, ObjectNode.class);

		Assert.assertNotNull("the function description JSON shouldn't be null", functionFunctionDescription);

		final Function function = new Function();
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
