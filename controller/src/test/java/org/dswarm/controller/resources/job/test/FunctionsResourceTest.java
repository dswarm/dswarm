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
package org.dswarm.controller.resources.job.test;

import org.junit.Assert;

import org.dswarm.controller.resources.job.test.utils.FunctionsResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.proxy.ProxyFunction;
import org.dswarm.persistence.service.job.FunctionService;
import org.dswarm.persistence.service.job.test.utils.FunctionServiceTestUtils;

public class FunctionsResourceTest extends
		BasicResourceTest<FunctionsResourceTestUtils, FunctionServiceTestUtils, FunctionService, ProxyFunction, Function, Long> {

	private final FunctionsResourceTestUtils	functionsResourceTestUtils;

	public FunctionsResourceTest() {

		super(Function.class, FunctionService.class, "functions", "function.json", new FunctionsResourceTestUtils());

		functionsResourceTestUtils = new FunctionsResourceTestUtils();
	}

	@Override
	protected Function updateObject(final Function actualFunction) throws Exception {

		actualFunction.setDescription(actualFunction.getDescription() + " update");

		final String updateFunctionJSONString = objectMapper.writeValueAsString(actualFunction);

		Assert.assertNotNull("the function JSON string shouldn't be null", updateFunctionJSONString);

		final Function updateFunction = functionsResourceTestUtils.updateObject(updateFunctionJSONString, actualFunction);

		Assert.assertNotNull("the function JSON string shouldn't be null", updateFunction);

		return updateFunction;
	}
}
