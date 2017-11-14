/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.converter.flow.test.json;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger LOG = LoggerFactory.getLogger(JSONSourceResourceGDMStmtsFlowTest.class);

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

	@Test
	public void testFromConfigurationWithArray() throws Exception {

		final String uuid = UUIDService.getUUID(Configuration.class.getSimpleName());

		final Configuration configuration = new Configuration(uuid);

		configuration.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("record"));

		final String dataModelUUID = "2";

		final DataModel dataModel = new DataModel(dataModelUUID);

		dataModel.setConfiguration(configuration);

		final JSONSourceResourceGDMStmtsFlow flow = injector
				.getInstance(JsonResourceFlowFactory.class)
				.fromDataModel(dataModel, false);

		testFlow(flow, "dd-1426/slub-finc-swb-records.json", "dd-1426/slub-finc-swb-records-gdm.json", null);
	}

	private void testFlow(final JSONSourceResourceGDMStmtsFlow flow, final String fileName, final String expectedResultFileName, final Integer offset)
			throws DMPConverterException, IOException {

		// serialise model as JSON (pretty print)
		final ObjectMapper mapper = DMPPersistenceUtil.getJSONObjectMapper().copy().configure(SerializationFeature.INDENT_OUTPUT, true);

		final List<GDMModel> gdmModels = flow.applyResource(fileName).toList().toBlocking().first();

		LOG.debug("GDM model = '{}'", gdmModels);

		final URL expectedResultFileURL = Resources.getResource(expectedResultFileName);

		Assert.assertNotNull("the expected result file URL shouldn't be null", expectedResultFileURL);

		final String expectedResults = Resources.toString(expectedResultFileURL, StandardCharsets.UTF_8);

		final ArrayNode expectedModelsJSON = mapper.readValue(expectedResults, ArrayNode.class);

		Assert.assertNotNull("the expected result shouldn't be null", expectedResults);

		int i = 0;

		if (gdmModels != null && !gdmModels.isEmpty()) {

			for (final GDMModel gdmModel : gdmModels) {

				final Model model = gdmModel.getModel();

				final JsonNode jsonNode = gdmModel.toGDMCompactJSON();

				Assert.assertNotNull("the GDM model shouldn't be null", model);

				final String modelJSON = mapper.writeValueAsString(model);

				Assert.assertNotNull("the GDM model JSON shouldn't be null", modelJSON);

				final long expectedResultLength;

				final JsonNode expectedModelJSON = expectedModelsJSON.get(i);

				final String expectedResult = mapper.writeValueAsString(expectedModelJSON);

				if (offset != null) {

					// TODO http://data.slub-dresden.de/datamodels/

					final ArrayNode actualModelJSON = mapper.readValue(modelJSON, ArrayNode.class);

					Assert.assertNotNull("the deserialized result shouldn't be null", actualModelJSON);

					final JsonNode firstElementJSON = actualModelJSON.get(0);

					Assert.assertNotNull("the first element of the actual result JSON array shouldn't be null", firstElementJSON);

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

					final int firstSlash = fieldNameWOBaseURI.indexOf("/");

					Assert.assertFalse(
							"couldn't find the first slash in the cut first field name of the first element of the actual result JSON array",
							firstSlash == -1);

					final String dataModelIdString = fieldNameWOBaseURI.substring(0, firstSlash);

					Assert.assertNotNull("the extracted data model id string shouldn't be null", dataModelIdString);

					if (dataModelIdString.length() == 1) {

						expectedResultLength = expectedResult.length();
					} else if (dataModelIdString.length() > 1) {

						final Long dataModelId = Long.valueOf(dataModelIdString);

						Assert.assertNotNull("the converted data model id should be a number", dataModelId);

						expectedResultLength = expectedResult.length() + (offset * (dataModelIdString.length() - 1));
					} else {

						// data model id string is empty - this should never happen

						expectedResultLength = expectedResult.length();
					}
				} else {

					expectedResultLength = expectedResult.length();
				}

				//Assert.assertEquals("the processing result length is not equal to the expected one", expectedResultLength, modelJSON.length());

				i++;
			}
		}
	}
}
