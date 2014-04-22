package de.avgl.dmp.converter.flow.test;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Provider;

import de.avgl.dmp.converter.GuicedTest;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class FilterTransformationFlowTest extends GuicedTest {

	@Test
	public void testFilterEndToEndWithOneResult() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test-mabxml.filter.result.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = injector.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromFile("filtermorph.xml", internalModelServiceFactoryProvider);

		final String actual = flow.applyResource("test-mabxml.tuples.json");

		// replace key with actual key
		final ArrayNode expectedJson = DMPPersistenceUtil.getJSONObjectMapper().readValue(expected, ArrayNode.class);
		final ObjectNode expectedTuple = (ObjectNode) expectedJson.get(0);
		final String expectedFieldName = expectedTuple.fieldNames().next();
		final ArrayNode expectedContent = (ArrayNode) expectedTuple.get(expectedFieldName);
		final ObjectNode expectedContentJson = (ObjectNode) expectedContent.get(0);
		final String expectedContentValue = expectedContentJson.get(expectedContentJson.fieldNames().next()).asText();

		Assert.assertNotNull("the actual transformation result shouldn't be null", actual);
		final ArrayNode actualJson = DMPPersistenceUtil.getJSONObjectMapper().readValue(actual, ArrayNode.class);
		Assert.assertNotNull("the deserialised JSON array of the actual transformation result shouldn't be null", actualJson);
		final ObjectNode actualTuple = (ObjectNode) actualJson.get(0);
		Assert.assertNotNull("the tuple of the deserialised JSON array of the actual transformation result shouldn't be null", actualTuple);
		final JsonNode actualContent = actualTuple.get(actualTuple.fieldNames().next());
		Assert.assertNotNull("the content of the tuple of the deserialised JSON array of the actual transformation result shouldn't be null",
				actualContent);
		Assert.assertTrue("the actual content should be a JSON array", actualContent.isArray());
		final JsonNode actualContentJson = actualContent.get(0);
		Assert.assertNotNull("the acutal content JSON shouldn't be null", actualContentJson);
		Assert.assertTrue("the actual content JSON should be a JSON object", actualContentJson.isObject());

		final ObjectNode newExpectedContentJson = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();
		newExpectedContentJson.put(actualContentJson.fieldNames().next(), expectedContentValue);
		final ArrayNode newExpectedContent = DMPPersistenceUtil.getJSONObjectMapper().createArrayNode();
		newExpectedContent.add(newExpectedContentJson);
		expectedTuple.put(expectedFieldName, newExpectedContent);

		final String finalExpected = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(expectedJson);

		assertEquals(finalExpected, actual);
	}
}
