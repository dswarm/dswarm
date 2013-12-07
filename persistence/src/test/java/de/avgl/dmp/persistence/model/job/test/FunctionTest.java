package de.avgl.dmp.persistence.model.job.test;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.job.Function;

public class FunctionTest extends GuicedTest {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(FunctionTest.class);

	private final ObjectMapper						objectMapper	= injector.getInstance(ObjectMapper.class);

	@Test
	public void simpleFunctionTest() {

		//final String functionId = UUID.randomUUID().toString();
		final String functionName = "trim";
		final String functionDescription = "trims leading and trailing whitespaces from a given string";
		final String functionParameter = "inputString";
		final String functionFunctionDescription = "machine readable description";

		final Function function = new Function();
		//function.setId(functionId);
		function.setName(functionName);
		function.setDescription(functionDescription);
		function.addParameter(functionParameter);
		function.setFunctionDescription(functionFunctionDescription);

		//Assert.assertNotNull("the function id shouldn't be null", function.getId());
		//Assert.assertEquals("the function ids are not equal", functionId, function.getId());
		Assert.assertNotNull("the function name shouldn't be null", function.getName());
		Assert.assertEquals("the function names are not equal", functionName, function.getName());
		Assert.assertNotNull("the function description shouldn't be null", function.getDescription());
		Assert.assertEquals("the function descriptions are not equal", functionDescription, function.getDescription());
		Assert.assertNotNull("the function parameters shouldn't be null", function.getParameters());
		Assert.assertEquals("the function parameters' size are not equal", 1, function.getParameters().size());
		Assert.assertEquals("the function parameter '" + functionParameter + "' are not equal", functionParameter, function.getParameters().get(0));

		String json = null;

		try {

			json = objectMapper.writeValueAsString(function);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("function json: " + json);
	}

}
