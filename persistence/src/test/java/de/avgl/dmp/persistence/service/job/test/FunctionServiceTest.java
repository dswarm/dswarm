package de.avgl.dmp.persistence.service.job.test;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.FunctionType;
import de.avgl.dmp.persistence.service.job.FunctionService;
import de.avgl.dmp.persistence.service.test.IDBasicJPAServiceTest;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class FunctionServiceTest extends IDBasicJPAServiceTest<Function, FunctionService, Long> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(FunctionServiceTest.class);

	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	public FunctionServiceTest() {

		super("function", FunctionService.class);
	}

	@Test
	public void testSimpleFunction() throws Exception {

		final String functionName = "trim";
		final String functionDescription = "trims leading and trailing whitespaces from a given string";
		final String functionParameter1 = "inputString";
		final String functionParameter2 = "parameter2";
		
		final String functionFunctionDescriptionString = DMPPersistenceUtil.getResourceAsString("function_description.prettyprint.json");

		Assert.assertNotNull("the function description JSON string shouldn't be null", functionFunctionDescriptionString);

		final ObjectNode functionFunctionDescription = objectMapper.readValue(functionFunctionDescriptionString, ObjectNode.class);

		Assert.assertNotNull("the function description JSON shouldn't be null", functionFunctionDescription);

		final Function function = createObject();
		function.setName(functionName);
		function.setDescription(functionDescription);
		function.addParameter(functionParameter1);
		function.addParameter(functionParameter2);
		function.setFunctionDescription(functionFunctionDescription);
		

		final Function updatedFunction = updateObjectTransactional(function);

		Assert.assertNotNull("the function name shouldn't be null", updatedFunction.getName());
		Assert.assertEquals("the function names are not equal", functionName, updatedFunction.getName());
		Assert.assertNotNull("the function description shouldn't be null", updatedFunction.getDescription());
		Assert.assertEquals("the function descriptions are not equal", functionDescription, updatedFunction.getDescription());
		
		Assert.assertNotNull("the function description JSON shouldn't be null", updatedFunction.getFunctionDescription());

		final String functionDescriptionJSONString = objectMapper.writeValueAsString(updatedFunction.getFunctionDescription());
		
		final String functionFunctionDescriptionJSONString = objectMapper.writeValueAsString(functionFunctionDescription);

		Assert.assertEquals("the function description JSON strings are not equal", functionFunctionDescriptionJSONString, functionDescriptionJSONString);
		
		Assert.assertNotNull("the function parameters shouldn't be null", updatedFunction.getParameters());
		Assert.assertEquals("the function parameters' size are not equal", 2, updatedFunction.getParameters().size());
		Assert.assertEquals("the function parameter '" + functionParameter1 + "' are not equal", functionParameter1, updatedFunction.getParameters().get(0));
		Assert.assertEquals("the function type is not '" + FunctionType.Function + "'", FunctionType.Function, updatedFunction.getFunctionType());

		String json = null;

		try {
			json = objectMapper.writeValueAsString(updatedFunction);
		} catch (final JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		FunctionServiceTest.LOG.debug("function json: " + json);

		// clean up DB
		deletedObject(function.getId());
	}
}
