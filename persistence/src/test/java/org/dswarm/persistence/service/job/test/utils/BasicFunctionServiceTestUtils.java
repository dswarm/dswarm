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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.Assert;

import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.proxy.ProxyBasicFunction;
import org.dswarm.persistence.service.job.BasicFunctionService;
import org.dswarm.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;

public abstract class BasicFunctionServiceTestUtils<POJOCLASSPERSISTENCESERVICE extends BasicFunctionService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyBasicFunction<POJOCLASS>, POJOCLASS extends Function>
		extends ExtendedBasicDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	public BasicFunctionServiceTestUtils(final Class<POJOCLASS> pojoClassArg, final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg) {

		super(pojoClassArg, persistenceServiceClassArg);
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert both expected and actual function have either no or equal function descriptions. <br />
	 * Assert both expected and actual function have either no or equal parameters.
	 */
	@Override
	public void compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) throws JsonProcessingException, JSONException {

		super.compareObjects(expectedObject, actualObject);

		// compare function description
		if (expectedObject.getFunctionDescription() == null) {

			Assert.assertNull("the actual " + pojoClassName + " description JSON should be null", actualObject.getFunctionDescription());

		} else {

			Assert.assertNotNull("the actual " + pojoClassName + " description JSON shouldn't be null", actualObject.getFunctionDescription());

			String actualFunctionDescriptionJSONString = null;

			try {

				actualFunctionDescriptionJSONString = objectMapper.writeValueAsString(actualObject.getFunctionDescription());
			} catch (final JsonProcessingException e) {

				Assert.assertTrue("something went wrong while serializing the actual " + pojoClassName + " description JSON", false);
			}

			Assert.assertNotNull("the actual " + pojoClassName + " description JSON string shouldn't be null", actualFunctionDescriptionJSONString);

			String expectedFunctionDescriptionJSONString = null;

			try {

				expectedFunctionDescriptionJSONString = objectMapper.writeValueAsString(expectedObject.getFunctionDescription());
			} catch (final JsonProcessingException e) {

				Assert.assertTrue("something went wrong while serializing the expected " + pojoClassName + " description JSON", false);
			}

			Assert.assertNotNull("the expected " + pojoClassName + " description JSON string shouldn't be null",
					expectedFunctionDescriptionJSONString);

			Assert.assertEquals("the " + pojoClassName + " description JSON strings are not equal", expectedFunctionDescriptionJSONString,
					actualFunctionDescriptionJSONString);
		}

		// compare parameters
		Assert.assertEquals("the " + pojoClassName + " parameters should be equal", expectedObject.getParameters(), actualObject.getParameters());
	}

	public POJOCLASS createFunction(final String name, final String description, final LinkedList<String> parameters) throws Exception {

		final POJOCLASS function = createNewObject();

		function.setName(name);
		function.setDescription(description);
		function.setParameters(parameters);

		return createAndCompareObject(function, function);
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
