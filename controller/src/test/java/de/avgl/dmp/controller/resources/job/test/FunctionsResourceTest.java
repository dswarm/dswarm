package de.avgl.dmp.controller.resources.job.test;

import org.junit.Assert;

import de.avgl.dmp.controller.resources.job.test.utils.FunctionsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.BasicResourceTest;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.proxy.ProxyFunction;
import de.avgl.dmp.persistence.service.job.FunctionService;
import de.avgl.dmp.persistence.service.job.test.utils.FunctionServiceTestUtils;

public class FunctionsResourceTest extends
		BasicResourceTest<FunctionsResourceTestUtils, FunctionServiceTestUtils, FunctionService, ProxyFunction, Function, Long> {

	private final FunctionsResourceTestUtils functionsResourceTestUtils;

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
