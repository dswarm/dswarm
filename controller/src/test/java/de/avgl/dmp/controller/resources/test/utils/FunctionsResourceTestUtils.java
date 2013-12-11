package de.avgl.dmp.controller.resources.test.utils;

import java.util.LinkedList;

import org.junit.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.service.job.FunctionService;

public class FunctionsResourceTestUtils extends ExtendedBasicDMPResourceTestUtils<FunctionService, Function> {

	public FunctionsResourceTestUtils() {

		super("functions", Function.class, FunctionService.class);
	}

	@Override
	public void compareObjects(final Function expectedObject, final Function actualObject) {

		super.compareObjects(expectedObject, actualObject);

		compareFunctions(expectedObject, actualObject);
	}

	private void compareFunctions(final Function expectedFunction, final Function actualFunction) {

		if (expectedFunction.getFunctionDescription() != null) {

			Assert.assertNotNull("the function description JSON shouldn't be null", actualFunction.getFunctionDescription());

			String actualFunctionDescriptionJSONString = null;

			try {

				actualFunctionDescriptionJSONString = objectMapper.writeValueAsString(actualFunction.getFunctionDescription());
			} catch (JsonProcessingException e) {

				Assert.assertTrue("something went wrong while serializing the actual function description JSON", false);
			}

			Assert.assertNotNull("the actual function description JSON string shouldn't be null", actualFunctionDescriptionJSONString);

			String expectedFunctionDescriptionJSONString = null;

			try {

				expectedFunctionDescriptionJSONString = objectMapper.writeValueAsString(expectedFunction.getFunctionDescription());
			} catch (JsonProcessingException e) {

				Assert.assertTrue("something went wrong while serializing the expected function description JSON", false);
			}

			Assert.assertNotNull("the expected function description JSON string shouldn't be null", expectedFunctionDescriptionJSONString);

			Assert.assertEquals("the function description JSON strings are not equal", expectedFunctionDescriptionJSONString,
					actualFunctionDescriptionJSONString);
		}

		if (expectedFunction.getParameters() != null && !expectedFunction.getParameters().isEmpty()) {

			final LinkedList<String> actualFunctionParameters = actualFunction.getParameters();

			Assert.assertNotNull("the actual function parameters shouldn't be null", actualFunctionParameters);
			Assert.assertFalse("the actual function parameters shouldn't be empty", actualFunctionParameters.isEmpty());

			int i = 0;

			for (final String expectedFunctionParameter : expectedFunction.getParameters()) {

				final String actualFunctionParameter = actualFunctionParameters.get(i);

				Assert.assertEquals("the function parameters are not equal", expectedFunctionParameter, actualFunctionParameter);

				i++;
			}
		}
	}
}
