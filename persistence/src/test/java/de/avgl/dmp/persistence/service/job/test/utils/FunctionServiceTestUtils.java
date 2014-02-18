package de.avgl.dmp.persistence.service.job.test.utils;

import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.proxy.ProxyFunction;
import de.avgl.dmp.persistence.service.job.FunctionService;

public class FunctionServiceTestUtils extends BasicFunctionServiceTestUtils<FunctionService, ProxyFunction, Function> {

	public FunctionServiceTestUtils() {

		super(Function.class, FunctionService.class);
	}

	@Override
	public void reset() {

	}
}
