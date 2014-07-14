package org.dswarm.controller.resources.job.test;

import org.dswarm.controller.resources.job.test.utils.FunctionsResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.proxy.ProxyFunction;
import org.dswarm.persistence.service.job.FunctionService;
import org.dswarm.persistence.service.job.test.utils.FunctionServiceTestUtils;
import org.junit.Assert;

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
