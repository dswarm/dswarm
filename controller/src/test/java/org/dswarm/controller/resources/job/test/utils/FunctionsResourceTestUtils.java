package org.dswarm.controller.resources.job.test.utils;

import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.proxy.ProxyFunction;
import org.dswarm.persistence.service.job.FunctionService;
import org.dswarm.persistence.service.job.test.utils.FunctionServiceTestUtils;

public class FunctionsResourceTestUtils extends BasicFunctionsResourceTestUtils<FunctionServiceTestUtils, FunctionService, ProxyFunction, Function> {

	public FunctionsResourceTestUtils() {

		super("functions", Function.class, FunctionService.class, FunctionServiceTestUtils.class);
	}
}
