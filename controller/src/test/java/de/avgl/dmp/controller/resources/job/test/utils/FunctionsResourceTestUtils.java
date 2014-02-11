package de.avgl.dmp.controller.resources.job.test.utils;

import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.proxy.ProxyFunction;
import de.avgl.dmp.persistence.service.job.FunctionService;

public class FunctionsResourceTestUtils extends BasicFunctionsResourceTestUtils<FunctionService, ProxyFunction, Function> {

	public FunctionsResourceTestUtils() {

		super("functions", Function.class, FunctionService.class);
	}

	@Override
	public void reset() {
		
	}
}
