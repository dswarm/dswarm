package org.dswarm.persistence.service.job.test.utils;

import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.proxy.ProxyFunction;
import org.dswarm.persistence.service.job.FunctionService;

public class FunctionServiceTestUtils extends BasicFunctionServiceTestUtils<FunctionService, ProxyFunction, Function> {

	public FunctionServiceTestUtils() {

		super(Function.class, FunctionService.class);
	}

	@Override
	public void reset() {

	}
}
