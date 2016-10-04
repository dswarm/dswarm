/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.converter.flow.test.xml;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
import org.junit.Assert;
import org.junit.Test;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.flow.XMLSourceResourceGDMStmtsFlow;
import org.dswarm.converter.flow.XmlResourceFlowFactory;
import org.dswarm.graph.json.Model;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class XMLSourceResourceGDMStmtsFlowTest extends GuicedTest {

	private void testFlow(final XMLSourceResourceGDMStmtsFlow flow, final String fileName, final String expectedResultFileName, final Integer offset)
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

					expectedResult = Resources.toString(expectedResultFileURL, StandardCharsets.UTF_8);
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

	// @Test
	// public void testEndToEnd() throws Exception {
	//
	// final CSVSourceResourceTriplesFlow flow =
	// CSVResourceFlowFactory.fromConfigurationParameters("UTF-8", '\\', '"', ';', "\n",
	// CSVSourceResourceTriplesFlow.class);
	//
	// testFlow(flow);
	// }

	@Test
	public void testFromConfiguration() throws Exception {

		final String uuid = UUIDService.getUUID(Configuration.class.getSimpleName());

		final Configuration configuration = new Configuration(uuid);

		configuration.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("datensatz"));
		configuration.addParameter(ConfigurationStatics.XML_NAMESPACE, new TextNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd"));

		final String dataModelUUID = "1";

		final DataModel dataModel = new DataModel(dataModelUUID);

		dataModel.setConfiguration(configuration);

		final XMLSourceResourceGDMStmtsFlow flow = injector
				.getInstance(XmlResourceFlowFactory.class)
				.fromDataModel(dataModel, false);

		testFlow(flow, "test-mabxml.xml", "test-mabxml_converter.gson", null);
	}

	@Test
	public void testFromConfiguration2() throws Exception {

		final String uuid = UUIDService.getUUID(Configuration.class.getSimpleName());

		final Configuration configuration = new Configuration(uuid);

		configuration.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("datensatz"));
		configuration.addParameter(ConfigurationStatics.XML_NAMESPACE, new TextNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd"));

		final String dataModelUUID = "2";

		final DataModel dataModel = new DataModel(dataModelUUID);

		dataModel.setConfiguration(configuration);

		final XMLSourceResourceGDMStmtsFlow flow = injector
				.getInstance(XmlResourceFlowFactory.class)
				.fromDataModel(dataModel, false);

		testFlow(flow, "test-complex-xml.xml", "test-complex-xml_converter.gson", null);
	}

	@Test
	public void testFromConfiguration3() throws Exception {

		final DataModelService dataModelService = GuicedTest.injector.getInstance(DataModelService.class);
		final DataModel dataModel = dataModelService.createObjectTransactional("3").getObject();

		final Configuration configuration = new Configuration("4");
		configuration.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("record"));


		dataModel.setConfiguration(configuration);

		final Resource resource = new Resource("5");
		resource.addAttribute("path", "/tmp/file.record");

		dataModel.setDataResource(resource);

		final XMLSourceResourceGDMStmtsFlow flow = injector
				.getInstance(XmlResourceFlowFactory.class)
				.fromDataModel(dataModel, false);

		testFlow(flow, "test-pnx.xml", "test-pnx-converter.gson", 196);

		dataModelService.deleteObject(dataModel.getUuid());
	}

	@Test
	public void testFromConfiguration4() throws Exception {

		final DataModelService dataModelService = GuicedTest.injector.getInstance(DataModelService.class);
		final DataModel dataModel = dataModelService.getObject(DataModelUtils.PNX_DATA_MODEL_UUID);

		final Configuration configuration = new Configuration("4");
		configuration.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("record"));
		configuration.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode("pnx"));

		dataModel.setConfiguration(configuration);

		final Resource resource = new Resource("5");
		resource.addAttribute("path", "/tmp/file.record");

		dataModel.setDataResource(resource);

		final XMLSourceResourceGDMStmtsFlow flow = injector
				.getInstance(XmlResourceFlowFactory.class)
				.fromDataModel(dataModel, false);

		testFlow(flow, "test-pnx2.xml", "test-pnx2.gson", null);

		dataModelService.deleteObject(dataModel.getUuid());
	}

	// @Test(expected = DMPConverterException.class)
	// public void testNullConfiguration() throws Exception {
	// final Configuration configuration = null;
	// @SuppressWarnings("UnusedDeclaration") final CSVSourceResourceTriplesFlow flow = new
	// CSVSourceResourceTriplesFlow(configuration);
	// }
	//
	// @Test(expected = NullPointerException.class)
	// public void testNullConfigurationParameter() throws Exception {
	// final Configuration configuration = new Configuration();
	// @SuppressWarnings("UnusedDeclaration") final CSVSourceResourceTriplesFlow flow = new
	// CSVSourceResourceTriplesFlow(configuration);
	// }

}
