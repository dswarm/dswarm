package de.avgl.dmp.controller.resources.job.test.utils;

import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.proxy.ProxyFunction;
import de.avgl.dmp.persistence.service.job.FunctionService;
import de.avgl.dmp.persistence.service.job.test.utils.FunctionServiceTestUtils;

public class FunctionsResourceTestUtils extends BasicFunctionsResourceTestUtils<FunctionServiceTestUtils, FunctionService, ProxyFunction, Function> {

	public FunctionsResourceTestUtils() {

		super("functions", Function.class, FunctionService.class, FunctionServiceTestUtils.class);
	}
}
