package de.avgl.dmp.converter.flow.test.csv;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Provider;

import de.avgl.dmp.converter.GuicedTest;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.persistence.model.job.Task;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 * Created by tgaengler on 13/05/14.
 */
public class CSVTransformationFlowTest extends GuicedTest {

	@Test
	public void testCSVEndToEndWithEmptyValues() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test_csv_-_manipulated.result.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = GuicedTest.injector
				.getProvider(InternalModelServiceFactory.class);
		
		final String finalTaskJSONString = DMPPersistenceUtil.getResourceAsString("complex-transformation_on_csv.task.json");
		final ObjectMapper objectMapper = DMPPersistenceUtil.getJSONObjectMapper();

		final ObjectMapper objectMapper2 = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.setSerializationInclusion(JsonInclude.Include.NON_EMPTY).configure(SerializationFeature.INDENT_OUTPUT, true);

		final Task task = objectMapper.readValue(finalTaskJSONString, Task.class);

		final TransformationFlow flow = TransformationFlow.fromTask(task, internalModelServiceFactoryProvider);

		flow.getScript();

		final String actual = flow.applyResource("test_csv_-_manipulated.tuples.json");
		final ArrayNode array = objectMapper2.readValue(actual, ArrayNode.class);
		final String finalActual = objectMapper2.writeValueAsString(array);

		final ArrayNode expectedArray = objectMapper2.readValue(expected, ArrayNode.class);
		final String finalExpected = objectMapper2.writeValueAsString(expectedArray);
		Assert.assertEquals(finalExpected.length(), finalActual.length());
	}

}
