package de.avgl.dmp.controller.resources.test;

import de.avgl.dmp.controller.resources.test.utils.FunctionsResourceTestUtils;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.service.job.FunctionService;

public class FunctionsResourceTest extends BasicResourceTest<FunctionsResourceTestUtils, FunctionService, Function, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributesResourceTest.class);

	public FunctionsResourceTest() {

		super(Function.class, FunctionService.class, "functions", "function.json", new FunctionsResourceTestUtils());
	}
}
