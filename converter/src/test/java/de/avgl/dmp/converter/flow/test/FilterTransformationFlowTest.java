package de.avgl.dmp.converter.flow.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Provider;

import de.avgl.dmp.converter.GuicedTest;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.converter.morph.MorphScriptBuilder;
import de.avgl.dmp.persistence.model.job.Task;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class FilterTransformationFlowTest extends GuicedTest {

	@Test
	public void testFilterEndToEndWithOneResult() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test-mabxml.filter.result.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = injector.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromFile("filtermorph.xml", internalModelServiceFactoryProvider);

		final String actual = flow.applyResource("test-mabxml.tuples.json");

		final ArrayNode expectedJson = replaceKeyWithActualKey(expected, actual);
		final String finalExpected = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(expectedJson);

		assertEquals(finalExpected, actual);
	}

	@Test
	public void testFilterEndToEndWithMultipleResults() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test-mabxml.filter.result.2.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = injector.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromFile("filtermorph2.xml", internalModelServiceFactoryProvider);

		final String actual = flow.applyResource("test-mabxml.tuples.2.json");

		final ArrayNode expectedJson = replaceKeyWithActualKey(expected, actual);
		final String finalExpected = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(expectedJson);

		assertEquals(finalExpected, actual);
	}

	/**
	 * TODO: fix this - this test doesn't work yet as expected - or? - i.e. we need to find the entity boarder to be able to
	 * combine it with an index (occurrence)
	 * 
	 * @throws Exception
	 */
	@Ignore
	@Test
	public void testFilterEndToEndWithMultipleResultsAndRepeatableElements() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test-mabxml.filter.result.3.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = injector.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromFile("filtermorph3.xml", internalModelServiceFactoryProvider);

		final String actual = flow.applyResource("test-mabxml.tuples.json");

		final ArrayNode expectedJson = replaceKeyWithActualKey(expected, actual);
		final String finalExpected = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(expectedJson);

		assertEquals(finalExpected, actual);
	}

	@Test
	public void testFilterEndToEndWithMultipleResultsAndSelectingSpecificIndex() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test-mabxml.filter.result.4.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = injector.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromFile("filtermorph4.xml", internalModelServiceFactoryProvider);

		final String actual = flow.applyResource("test-mabxml.tuples.json");

		final ArrayNode expectedJson = replaceKeyWithActualKey(expected, actual);
		final String finalExpected = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(expectedJson);

		assertEquals(finalExpected, actual);
	}
	
	@Test
	public void testFilterEndToEndWithMorphScriptBuilder() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test-mabxml.filter.morphscript.result.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = injector.getProvider(InternalModelServiceFactory.class);

		final String request = DMPPersistenceUtil.getResourceAsString("task.filter.json");
		
		final ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
		
		final Task task = objectMapper.readValue(request, Task.class);
		
		final String morphScriptString = new MorphScriptBuilder().apply(task).toString();
		
		final TransformationFlow flow = TransformationFlow.fromString(morphScriptString, internalModelServiceFactoryProvider);

		final String actual = flow.applyResource("test-mabxml.tuples.json");

		final ArrayNode expectedJson = replaceKeyWithActualKey(expected, actual);
		final String finalExpected = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(expectedJson);

		assertEquals(finalExpected, actual);
	}

	private ArrayNode replaceKeyWithActualKey(final String expected, final String actual) throws JsonParseException, JsonMappingException,
			IOException {

		// replace key with actual key
		final ArrayNode expectedJson = DMPPersistenceUtil.getJSONObjectMapper().readValue(expected, ArrayNode.class);
		final ObjectNode expectedTuple = (ObjectNode) expectedJson.get(0);
		final String expectedFieldName = expectedTuple.fieldNames().next();
		final ArrayNode expectedContent = (ArrayNode) expectedTuple.get(expectedFieldName);
		final ObjectNode expectedContentJson = (ObjectNode) expectedContent.get(0);
		final JsonNode expectedContentValue = expectedContentJson.get(expectedContentJson.fieldNames().next());

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

		return expectedJson;
	}
}
