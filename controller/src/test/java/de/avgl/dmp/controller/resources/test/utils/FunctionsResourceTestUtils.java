package de.avgl.dmp.controller.resources.test.utils;

import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.service.job.FunctionService;

public class FunctionsResourceTestUtils extends BasicFunctionsResourceTestUtils<FunctionService, Function> {

	public FunctionsResourceTestUtils() {

		super("functions", Function.class, FunctionService.class);
	}
}
