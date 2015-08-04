package org.dswarm.converter.flow.test.json;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.io.Resources;
import org.apache.commons.io.Charsets;
import org.junit.Assert;
import org.junit.Test;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.flow.JSONSourceResourceGDMStmtsFlow;
import org.dswarm.converter.flow.JsonResourceFlowFactory;
import org.dswarm.graph.json.Model;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
public class JSONSourceResourceGDMStmtsFlowTest extends GuicedTest {

	@Test
	public void testFromConfiguration() throws Exception {

		final String uuid = UUIDService.getUUID(Configuration.class.getSimpleName());

		final Configuration configuration = new Configuration(uuid);

		configuration.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("record"));

		final String dataModelUUID = "1";

		final DataModel dataModel = new DataModel(dataModelUUID);

		dataModel.setConfiguration(configuration);

		final JSONSourceResourceGDMStmtsFlow flow = injector
				.getInstance(JsonResourceFlowFactory.class)
				.fromDataModel(dataModel, false);

		testFlow(flow, "bib-record-marc.json", "test-record-marc-gdm.json", null);
	}

	private void testFlow(final JSONSourceResourceGDMStmtsFlow flow, final String fileName, final String expectedResultFileName, final Integer offset)
			throws DMPConverterException {

		final List<GDMModel> gdmModels = flow.applyResource(fileName).toList().toBlocking().first();

		if (gdmModels != null && !gdmModels.isEmpty()) {

			for (final GDMModel gdmModel : gdmModels) {

				final Model model = gdmModel.getModel();

				Assert.assertNotNull("the GDM model shouldn't be null", model);

				// serialise model as JSON (pretty print)
				final ObjectMapper mapper = DMPPersistenceUtil.getJSONObjectMapper().copy().configure(SerializationFeature.INDENT_OUTPUT, true);

				String modelJSON = null;

				try {

					modelJSON = mapper.writeValueAsString(model);
				} catch (final JsonProcessingException e) {

					Assert.assertTrue("something went wrong while serialising the GDM model to JSON", false);
				}

				Assert.assertNotNull("the GDM model JSON shouldn't be null", modelJSON);

				final URL expectedResultFileURL = Resources.getResource(expectedResultFileName);

				Assert.assertNotNull("the expected result file URL shouldn't be null", expectedResultFileURL);

				String expectedResult = null;

				try {

					expectedResult = Resources.toString(expectedResultFileURL, Charsets.UTF_8);
				} catch (final IOException e) {

					Assert.assertTrue("something went wrong while reading expected result from file", false);
				}

				Assert.assertNotNull("the expected result shouldn't be null", expectedResult);

				final long expectedResultLength;

				if (offset != null) {

					// TODO http://data.slub-dresden.de/datamodels/

					ArrayNode actualModelJSON = null;

					try {

						actualModelJSON = mapper.readValue(modelJSON, ArrayNode.class);
					} catch (final JsonParseException e) {

						Assert.assertTrue("something went wrong while deserializing the actual result", false);
					} catch (final JsonMappingException e) {

						Assert.assertTrue("something went wrong while deserializing the actual result", false);
					} catch (final IOException e) {

						Assert.assertTrue("something went wrong while deserializing the actual result", false);
					}

					Assert.assertNotNull("the deserialized result shouldn't be null", actualModelJSON);

					final JsonNode firstElementJSON = actualModelJSON.get(0);

					Assert.assertNotNull("the first element of the actual result JSON array shouldn't be null", firstElementJSON);
					Assert.assertTrue("the first element of the actual result JSON array should be a JSON object",
							firstElementJSON instanceof ObjectNode);

					final ObjectNode firstElementJSONObject = (ObjectNode) firstElementJSON;

					final Iterator<String> fieldNames = firstElementJSONObject.fieldNames();

					Assert.assertNotNull("the field names of the first element of the actual result JSON array shouldn't be null", fieldNames);
					Assert.assertTrue("the field names of the first element of the actual result JSON array should at least contain one element",
							fieldNames.hasNext());

					final String fieldName = fieldNames.next();

					Assert.assertNotNull("the first field name of the first lemenet of the actual result JSON array shouldn't be null", fieldName);
					Assert.assertTrue(
							"the first field name of the first element of the actual result JSON array should start with 'http://data.slub-dresden.de/datamodels/'",
							fieldName.startsWith("http://data.slub-dresden.de/datamodels/"));

					final String fieldNameWOBaseURI = fieldName.substring(39, fieldName.length());

					Assert.assertNotNull("the cut first field name of the first element of the actual result JSON array shouldn't be null",
							fieldNameWOBaseURI);

					final int firstSlash = fieldNameWOBaseURI.indexOf("/");

					Assert.assertFalse(
							"couldn't find the first slash in the cut first field name of the first element of the actual result JSON array",
							firstSlash == -1);

					final String dataModelIdString = fieldNameWOBaseURI.substring(0, firstSlash);

					Assert.assertNotNull("the extracted data model id string shouldn't be null", dataModelIdString);

					if (dataModelIdString.length() == 1) {

						expectedResultLength = expectedResult.length();
					} else if (dataModelIdString.length() > 1) {

						Long dataModelId = null;

						try {
							dataModelId = Long.valueOf(dataModelIdString);
						} catch (final NumberFormatException e) {

							Assert.assertTrue("something went wrong while converting the data model id string to a number", false);
						}

						Assert.assertNotNull("the converted data model id should be a number", dataModelId);

						expectedResultLength = expectedResult.length() + (offset * (dataModelIdString.length() - 1));
					} else {

						// data model id string is empty - this should never happen

						expectedResultLength = expectedResult.length();
					}
				} else {

					expectedResultLength = expectedResult.length();
				}

				Assert.assertEquals("the processing result length is not equal to the expected one", expectedResultLength, modelJSON.length());

				// System.out.println(modelJSON);
			}
		}
	}
}
