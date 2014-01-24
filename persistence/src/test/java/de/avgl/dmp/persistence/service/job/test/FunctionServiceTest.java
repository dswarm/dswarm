package de.avgl.dmp.persistence.service.job.test;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.FunctionType;
import de.avgl.dmp.persistence.model.job.proxy.ProxyFunction;
import de.avgl.dmp.persistence.service.job.FunctionService;
import de.avgl.dmp.persistence.service.test.IDBasicJPAServiceTest;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class FunctionServiceTest extends IDBasicJPAServiceTest<ProxyFunction, Function, FunctionService> {

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

		Assert.assertEquals("the function description JSON strings are not equal", functionFunctionDescriptionJSONString,
				functionDescriptionJSONString);

		Assert.assertNotNull("the function parameters shouldn't be null", updatedFunction.getParameters());
		Assert.assertEquals("the function parameters' size are not equal", 2, updatedFunction.getParameters().size());
		Assert.assertEquals("the function parameter '" + functionParameter1 + "' are not equal", functionParameter1, updatedFunction.getParameters()
				.get(0));
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

	// @Test
	public void loadMetafactureSimpleFunctionSetTest() {

		String metafactureFunctionDescriptionsJSONString = null;

		try {

			metafactureFunctionDescriptionsJSONString = DMPPersistenceUtil.getResourceAsString("functions.json");
		} catch (IOException e) {

			FunctionServiceTest.LOG.debug("something went wrong while reading the metafacture functions JSON file", e);

			Assert.assertTrue("something went wrong while reading the metafacture functions JSON file", false);
		}

		Assert.assertNotNull("the metafacture function descriptions JSON string shouldn't be null", metafactureFunctionDescriptionsJSONString);

		ObjectNode metafactureFunctionDescriptionsJSON = null;

		try {
			metafactureFunctionDescriptionsJSON = objectMapper.readValue(metafactureFunctionDescriptionsJSONString, ObjectNode.class);
		} catch (JsonParseException e) {

			FunctionServiceTest.LOG.debug("something went wrong while deserializing the metafacture functions JSON string", e);

			Assert.assertTrue("something went wrong while deserializing the metafacture functions JSON string", false);
		} catch (JsonMappingException e) {

			FunctionServiceTest.LOG.debug("something went wrong while deserializing the metafacture functions JSON string", e);

			Assert.assertTrue("something went wrong while deserializing the metafacture functions JSON string", false);
		} catch (IOException e) {

			FunctionServiceTest.LOG.debug("something went wrong while deserializing the metafacture functions JSON string", e);

			Assert.assertTrue("something went wrong while deserializing the metafacture functions JSON string", false);
		}

		Assert.assertNotNull("the metafacture function descriptions JSON shouldn't be null", metafactureFunctionDescriptionsJSON);

		JsonNode metafactureFunctionDescriptionsArrayNode = metafactureFunctionDescriptionsJSON.get("functions");

		Assert.assertNotNull("the metafacture function descriptions array node shouldn't be null", metafactureFunctionDescriptionsArrayNode);

		Assert.assertTrue("the metafacture function description array node should be an JSON array",
				metafactureFunctionDescriptionsArrayNode.isArray());

		ArrayNode metafactureFunctionDescriptionsArray = (ArrayNode) metafactureFunctionDescriptionsArrayNode;

		for (JsonNode metafactureFunctionDescriptionNode : metafactureFunctionDescriptionsArray) {

			final ObjectNode metafactureFunctionDescriptionJSON = (ObjectNode) metafactureFunctionDescriptionNode;

			final JsonNode metafactureFunctionName = metafactureFunctionDescriptionNode.get("name");

			String functionName = null;

			if (metafactureFunctionName != null) {

				functionName = metafactureFunctionName.asText();
			}

			final JsonNode metafactureFunctionDescription = metafactureFunctionDescriptionJSON.get("description");

			String functionDescription = null;

			if (metafactureFunctionDescription != null) {

				functionDescription = metafactureFunctionDescription.asText();
			}

			// TODO: optimize parameter extraction (?)

			LinkedList<String> parameters = null;

			final JsonNode metafactureFunctionParametersNode = metafactureFunctionDescriptionJSON.get("parameters");

			if (metafactureFunctionParametersNode != null) {

				final ObjectNode metafactureFunctionParametersJSON = (ObjectNode) metafactureFunctionParametersNode;

				final Iterator<String> fieldNames = metafactureFunctionParametersJSON.fieldNames();

				if (fieldNames != null) {

					parameters = Lists.newLinkedList();

					while (fieldNames.hasNext()) {

						final String fieldName = fieldNames.next();

						if (fieldName != null) {

							parameters.add(fieldName);
						}
					}
				}
			}

			final Function function = createObject();

			if (functionName != null) {

				function.setName(functionName);
			}

			if (functionDescription != null) {

				function.setDescription(functionDescription);
			}

			if (parameters != null && !parameters.isEmpty()) {

				function.setParameters(parameters);
			}

			function.setFunctionDescription(metafactureFunctionDescriptionJSON);

			final Function updatedFunction = updateObjectTransactional(function);

			Assert.assertNotNull("the updated metafacture function shouldn't be null", updatedFunction);
		}
	}
}
