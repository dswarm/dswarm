package de.avgl.dmp.controller.resources.test;

import org.hibernate.tuple.component.PojoComponentTuplizer;
import org.junit.Assert;

import de.avgl.dmp.controller.resources.test.utils.FunctionsResourceTestUtils;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.service.job.FunctionService;

public class FunctionsResourceTest extends BasicResourceTest<FunctionsResourceTestUtils, FunctionService, Function, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributesResourceTest.class);
	
	private final FunctionsResourceTestUtils		functionsResourceTestUtils;

	public FunctionsResourceTest() {

		super(Function.class, FunctionService.class, "functions", "function.json", new FunctionsResourceTestUtils());
		
		functionsResourceTestUtils = new FunctionsResourceTestUtils();
	}
	
	@Override
	public Function updateObject(final Function actualFunction) throws Exception {
		
		actualFunction.setDescription(actualFunction.getDescription() + " update");
		
		final String updateFunctionJSONString = objectMapper.writeValueAsString(actualFunction);
		
		Assert.assertNotNull("the function JSON string shouldn't be null", updateFunctionJSONString);
		
		final Function updateFunction = functionsResourceTestUtils.updateObject(updateFunctionJSONString, actualFunction);
		
		Assert.assertNotNull("the function JSON string shouldn't be null", updateFunction);
		
		return updateFunction;
	}
}
