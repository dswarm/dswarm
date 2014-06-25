package org.dswarm.persistence.service.job.test.utils;

import java.util.LinkedList;

import org.junit.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.proxy.ProxyBasicFunction;
import org.dswarm.persistence.service.job.BasicFunctionService;
import org.dswarm.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;

public abstract class BasicFunctionServiceTestUtils<POJOCLASSPERSISTENCESERVICE extends BasicFunctionService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyBasicFunction<POJOCLASS>, POJOCLASS extends Function>
		extends ExtendedBasicDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	public BasicFunctionServiceTestUtils(final Class<POJOCLASS> pojoClassArg, final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg) {

		super(pojoClassArg, persistenceServiceClassArg);
	}

	@Override
	public void compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) {

		super.compareObjects(expectedObject, actualObject);

		compareFunctions(expectedObject, actualObject);
	}

	public POJOCLASS createFunction(final String name, final String description, final LinkedList<String> parameters) throws Exception {

		final String functionName = name;
		final String functionDescription = description;

		final POJOCLASS function = createNewObject();

		function.setName(functionName);
		function.setDescription(functionDescription);
		function.setParameters(parameters);

		final POJOCLASS updatedFunction = createObject(function, function);

		return updatedFunction;
	}

	private void compareFunctions(final POJOCLASS expectedFunction, final POJOCLASS actualFunction) {

		if (expectedFunction.getFunctionDescription() != null) {

			Assert.assertNotNull("the " + pojoClassName + " description JSON shouldn't be null", actualFunction.getFunctionDescription());

			String actualFunctionDescriptionJSONString = null;

			try {

				actualFunctionDescriptionJSONString = objectMapper.writeValueAsString(actualFunction.getFunctionDescription());
			} catch (final JsonProcessingException e) {

				Assert.assertTrue("something went wrong while serializing the actual " + pojoClassName + " description JSON", false);
			}

			Assert.assertNotNull("the actual " + pojoClassName + " description JSON string shouldn't be null", actualFunctionDescriptionJSONString);

			String expectedFunctionDescriptionJSONString = null;

			try {

				expectedFunctionDescriptionJSONString = objectMapper.writeValueAsString(expectedFunction.getFunctionDescription());
			} catch (final JsonProcessingException e) {

				Assert.assertTrue("something went wrong while serializing the expected " + pojoClassName + " description JSON", false);
			}

			Assert.assertNotNull("the expected " + pojoClassName + " description JSON string shouldn't be null",
					expectedFunctionDescriptionJSONString);

			Assert.assertEquals("the " + pojoClassName + " description JSON strings are not equal", expectedFunctionDescriptionJSONString,
					actualFunctionDescriptionJSONString);
		}

		if (expectedFunction.getParameters() != null && !expectedFunction.getParameters().isEmpty()) {

			final LinkedList<String> actualFunctionParameters = actualFunction.getParameters();

			Assert.assertNotNull("the actual " + pojoClassName + " parameters shouldn't be null", actualFunctionParameters);
			Assert.assertFalse("the actual " + pojoClassName + " parameters shouldn't be empty", actualFunctionParameters.isEmpty());

			int i = 0;

			for (final String expectedFunctionParameter : expectedFunction.getParameters()) {

				final String actualFunctionParameter = actualFunctionParameters.get(i);

				Assert.assertEquals("the " + pojoClassName + " parameters are not equal", expectedFunctionParameter, actualFunctionParameter);

				i++;
			}
		}
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, description, parameters and machine processable function description of the function.
	 */
	@Override
	protected POJOCLASS prepareObjectForUpdate(final POJOCLASS objectWithUpdates, final POJOCLASS object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		object.setFunctionDescription(objectWithUpdates.getFunctionDescription());
		object.setParameters(objectWithUpdates.getParameters());

		return object;
	}
}
