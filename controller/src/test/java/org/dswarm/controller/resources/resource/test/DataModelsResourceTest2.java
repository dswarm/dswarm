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
package org.dswarm.controller.resources.resource.test;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;
import com.google.inject.Key;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import org.dswarm.common.DMPStatics;
import org.dswarm.common.types.Tuple;
import org.dswarm.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.controller.test.GuicedTest;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.proxy.ProxyDataModel;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.service.InternalModelService;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.resource.test.utils.DataModelServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class DataModelsResourceTest2 extends
		BasicResourceTest<DataModelsResourceTestUtils, DataModelServiceTestUtils, DataModelService, ProxyDataModel, DataModel> {

	private static final Logger LOG = LoggerFactory.getLogger(DataModelsResourceTest2.class);

	private ResourcesResourceTestUtils resourcesResourceTestUtils;

	public DataModelsResourceTest2() {

		super(DataModel.class, DataModelService.class, "datamodels", "datamodel.json", new DataModelsResourceTestUtils());
	}

	@Override
	protected void initObjects() {

		super.initObjects();

		pojoClassResourceTestUtils = new DataModelsResourceTestUtils();
		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
	}

	@Ignore
	@Test
	@Override public void testPOSTObjects() throws Exception {

		// do nothing
	}

	@Ignore
	@Test
	@Override public void testGETObjects() throws Exception {

		// do nothing
	}

	@Ignore
	@Test
	@Override public void testGETObject() throws Exception {

		// do nothing
	}

	@Ignore
	@Test
	@Override public void testPUTObject() throws Exception {

		// do nothing
	}

	@Ignore
	@Test
	@Override public void testDELETEObject() throws Exception {

		// do nothing
	}

	@Test
	public void testCSVData() throws Exception {

		DataModelsResourceTest2.LOG.debug("start get CSV data test");

		testCSVDataInternal();

		DataModelsResourceTest2.LOG.debug("end get CSV data test");
	}

	//	/**
	//	 * note doesn't work right now
	//	 *
	//	 * @throws Exception
	//	 */
	//	@Test
	//	public void testCSVDataUpdate() throws Exception {
	//
	//		DataModelsResourceTest2.LOG.debug("start CSV data update test");
	//
	//		final DataModel dataModel = testCSVDataInternal();
	//
	//		// prepare resource
	//
	//		final Resource expectedResource = dataModel.getDataResource();
	//
	//		final URL fileURL = Resources.getResource("test_csv-controller2.csv");
	//		final File resourceFile = FileUtils.toFile(fileURL);
	//
	//		// update resource
	//		resourcesResourceTestUtils.updateResource(resourceFile, expectedResource, expectedResource.getUuid());
	//
	//		final DataModel updateDataModel = pojoClassResourceTestUtils.getObject(dataModel.getUuid());
	//
	//		final String updateObjectJSONString = objectMapper.writeValueAsString(updateDataModel);
	//
	//		// update data model
	//		final Response response = target(String.valueOf(updateDataModel.getUuid())).queryParam("updateContent", Boolean.TRUE).request(
	//				MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
	//				.put(Entity.json(updateObjectJSONString));
	//
	//		Assert.assertEquals("200 Updated was expected", 200, response.getStatus());
	//
	//		final String responseString = response.readEntity(String.class);
	//
	//		Assert.assertNotNull("the response JSON shouldn't be null", responseString);
	//
	//		final Tuple<Optional<Map<String, Model>>, ObjectNode> result = readData(updateDataModel, Optional.<Integer>absent());
	//
	//		final Optional<Map<String, Model>> data = result.v1();
	//		final ObjectNode assoziativeJsonArray = result.v2();
	//
	//		Assert.assertTrue(data.isPresent());
	//		Assert.assertNotNull(assoziativeJsonArray);
	//
	//		System.out.println(objectMapper.writeValueAsString(assoziativeJsonArray));
	//
	//		DataModelsResourceTest2.LOG.debug("end CSV data update test");
	//	}

	/**
	 * existing (old) records will be deprecated after data model update
	 * TODO: disbabled right now, it looks like that old records won't be deprecated right now in the versioning algo
	 *
	 * @throws Exception
	 */
	@Ignore
	@Test
	public void testCSVDataUpdate2() throws Exception {

		DataModelsResourceTest2.LOG.debug("start CSV data update test");

		final DataModel dataModel = testCSVDataInternal();

		// prepare resource

		final Resource expectedResource = dataModel.getDataResource();

		final URL fileURL = Resources.getResource("test_csv-controller2.csv");
		final File resourceFile = FileUtils.toFile(fileURL);

		// update resource
		resourcesResourceTestUtils.updateResource(resourceFile, expectedResource, expectedResource.getUuid());

		final DataModel updateDataModel = pojoClassResourceTestUtils.getObject(dataModel.getUuid());

		// update data model
		final Response response = target(String.valueOf(updateDataModel.getUuid()), "/data").request().accept(MediaType.APPLICATION_JSON_TYPE).post(
				Entity.text(""));

		Assert.assertEquals("200 Updated was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final Tuple<Optional<Map<String, Model>>, ObjectNode> result = readData(updateDataModel, Optional.<Integer>empty());

		final Optional<Map<String, Model>> data = result.v1();
		final ObjectNode assoziativeJsonArray = result.v2();

		Assert.assertTrue(data.isPresent());
		Assert.assertNotNull(assoziativeJsonArray);

		final String expectedResult = DMPPersistenceUtil.getResourceAsString("dd-762.expected.delta-result.json");
		final String actualResult = objectMapper.writeValueAsString(assoziativeJsonArray);

		// TODO: do proper comparison
		Assert.assertEquals(expectedResult.length(), actualResult.length());

		DataModelsResourceTest2.LOG.debug("end CSV data update test");
	}

	/**
	 * existing records won't be deprecated; all new records will be added to the data model
	 *
	 * @throws Exception
	 */
	@Test
	public void testCSVDataUpdate3() throws Exception {

		DataModelsResourceTest2.LOG.debug("start CSV data update test");

		final DataModel dataModel = testCSVDataInternal();

		// prepare resource

		final Resource expectedResource = dataModel.getDataResource();

		final URL fileURL = Resources.getResource("test_csv-controller2.csv");
		final File resourceFile = FileUtils.toFile(fileURL);

		// update resource
		resourcesResourceTestUtils.updateResource(resourceFile, expectedResource, expectedResource.getUuid());

		final DataModel updateDataModel = pojoClassResourceTestUtils.getObject(dataModel.getUuid());

		// update data model
		final Response response = target(String.valueOf(updateDataModel.getUuid()), "/data").queryParam("format", "delta").request()
				.post(Entity.text(""));

		Assert.assertEquals("200 Updated was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final Tuple<Optional<Map<String, Model>>, ObjectNode> result = readData(updateDataModel, Optional.<Integer>empty());

		final Optional<Map<String, Model>> data = result.v1();
		final ObjectNode assoziativeJsonArray = result.v2();

		Assert.assertTrue(data.isPresent());
		Assert.assertNotNull(assoziativeJsonArray);

		final String expectedResult = DMPPersistenceUtil.getResourceAsString("dd-762.expected.result.json");
		final String actualResult = objectMapper.writeValueAsString(assoziativeJsonArray);

		// TODO: do proper comparison
		Assert.assertEquals(expectedResult.length(), actualResult.length());

		DataModelsResourceTest2.LOG.debug("end CSV data update test");
	}

	@Test
	public void testMabxmlXMLData() throws Exception {

		DataModelsResourceTest2.LOG.debug("start get mabxml XML data test");

		final String dataResourceResourceFileName = "test-mabxml-resource.json";
		final String dataResourceFileName = "controller_test-mabxml.xml";
		final String configurationFileName = "xml-configuration.json";
		final String dataModelName = "mabxml";

		final Tuple<JsonNode, JsonNode> resultTuple = testXMLDataInternal(dataResourceResourceFileName, dataResourceFileName,
				configurationFileName, dataModelName, true, false);

		final JsonNode json = resultTuple.v1();
		final JsonNode expectedJson = resultTuple.v2();

		Assert.assertThat(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#status", json),
				CoreMatchers.equalTo(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#status", expectedJson)));
		Assert.assertThat(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#mabVersion", json),
				CoreMatchers.equalTo(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#mabVersion", expectedJson)));
		Assert.assertThat(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#typ", json),
				CoreMatchers.equalTo(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#typ", expectedJson)));
		Assert.assertThat(getValueNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld", json).size(),
				CoreMatchers.equalTo(getValueNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld", expectedJson).size()));

		DataModelsResourceTest2.LOG.debug("end get mabxml XML data test");
	}

	@Test
	public void testOaipmhMetsModsXMLData() throws Exception {

		DataModelsResourceTest2.LOG.debug("start get OAI-PMH + Mets + Mods + X XML data test");

		final String dataResourceResourceFileName = "silberman_resource.json";
		final String dataResourceFileName = "silberman_02.xml";
		final String configurationFileName = "oai-pmh_mets_mods_config.json";
		final String dataModelName = "OAI-PMH + Mets + Mods + X";

		final Tuple<JsonNode, JsonNode> resultTuple = testXMLDataInternal(dataResourceResourceFileName, dataResourceFileName,
				configurationFileName, dataModelName, true, false);

		final JsonNode json = resultTuple.v1();
		final JsonNode expectedJson = resultTuple.v2();

		Assert.assertNotNull("the expected data JSON shouldn't be null", expectedJson);

		Assert.assertThat(getValue("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", json),
				CoreMatchers.equalTo(getValue("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", expectedJson)));
		Assert.assertThat(getValueNode("http://www.openarchives.org/OAI/2.0/header", json).size(),
				CoreMatchers.equalTo(getValueNode("http://www.openarchives.org/OAI/2.0/header", expectedJson).size()));
		Assert.assertThat(getValueNode("http://www.openarchives.org/OAI/2.0/metadata", json).size(),
				CoreMatchers.equalTo(getValueNode("http://www.openarchives.org/OAI/2.0/metadata", expectedJson).size()));

		DataModelsResourceTest2.LOG.debug("end get OAI-PMH + Mets + Mods + X XML data test");
	}

	@Test
	public void testXMLWDOCTYPEData() throws Exception {

		DataModelsResourceTest2.LOG.debug("start get XML with DOCTYPE data test");

		final String dataResourceResourceFileName = "xml_w_doctype_resource.json";
		final String dataResourceFileName = "xml_w_doctype.xml";
		final String configurationFileName = "xml_w_doctype_config.json";
		final String dataModelName = "XML with DOCTYPE";

		final Tuple<JsonNode, JsonNode> resultTuple = testXMLDataInternal(dataResourceResourceFileName, dataResourceFileName,
				configurationFileName, dataModelName, true, false);

		final JsonNode json = resultTuple.v1();
		final JsonNode expectedJson = resultTuple.v2();

		Assert.assertNotNull("the expected data JSON shouldn't be null", expectedJson);

		Assert.assertThat(getValue("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", json),
				CoreMatchers.equalTo(getValue("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", expectedJson)));

		DataModelsResourceTest2.LOG.debug("end get ML with DOCTYPE data test");
	}

	/**
	 * fails atm, since jsoup parses DOCTYPE SYSTEM a bit strange, see https://github.com/jhy/jsoup/issues/408
	 *
	 * @throws Exception
	 */
	@Ignore
	@Test
	public void testEnhancedXMLWDOCTYPEData() throws Exception {

		DataModelsResourceTest2.LOG.debug("start get enhanced XML with DOCTYPE data test");

		final String dataResourceResourceFileName = "xml_w_doctype_resource_2.json";
		final String dataResourceFileName = "xml_w_doctype_2.xml";
		final String configurationFileName = "xml_w_doctype_config.json";
		final String dataModelName = "XML with DOCTYPE";

		final Tuple<JsonNode, JsonNode> resultTuple = testXMLDataInternal(dataResourceResourceFileName, dataResourceFileName,
				configurationFileName, dataModelName, true, true);

		final JsonNode json = resultTuple.v1();
		final JsonNode expectedJson = resultTuple.v2();

		Assert.assertNotNull("the expected data JSON shouldn't be null", expectedJson);

		Assert.assertThat(getValue("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", json),
				CoreMatchers.equalTo(getValue("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", expectedJson)));

		DataModelsResourceTest2.LOG.debug("end get enhanced XML with DOCTYPE data test");
	}

	@Test
	public void testJSONData() throws Exception {

		DataModelsResourceTest2.LOG.debug("start get JSON data test");

		// START DATA MODEL CREATION

		final String dataResourceResourceFileName = "test-json-resource.json";
		final String dataResourceFileName = "controller_bib-record-marc.json";
		final String configurationFileName = "json-configuration.json";
		final String dataModelName = "json-example-datamodel";
		final String dataModelUuid = "DataModel-62dbf41c-f4b8-4b67-8995-6314d89c658f";
		final String schemaUuid = "Schema-65f19a84-1431-453e-a574-eabc8f2cc515";

		final DataModel dataModel = createDataModel2(dataResourceResourceFileName, dataResourceFileName, configurationFileName, dataModelName,
				dataModelUuid, schemaUuid, true, false);

		// END DATA MODEL CREATION

		final int atMost = 1;

		final Tuple<Optional<Map<String, Model>>, ObjectNode> result = readData(dataModel, Optional.of(atMost));

		final Optional<Map<String, Model>> data = result.v1();
		final ObjectNode assoziativeJsonArray = result.v2();
		final String recordId = data.get().keySet().iterator().next();

		Assert.assertThat(assoziativeJsonArray.size(), CoreMatchers.equalTo(atMost));

		final JsonNode json = assoziativeJsonArray.get(recordId);

		final JsonNode expectedJson = data.get().get(recordId).toRawJSON();

		Assert.assertNotNull("the expected data JSON shouldn't be null", expectedJson);

		Assert.assertThat(getValue("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", json),
				CoreMatchers.equalTo(getValue("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", expectedJson)));
		Assert.assertThat(
				getValue("http://data.slub-dresden.de/schemas/Schema-65f19a84-1431-453e-a574-eabc8f2cc515#controlNumber", json),
				CoreMatchers.equalTo(
						getValue("http://data.slub-dresden.de/schemas/Schema-65f19a84-1431-453e-a574-eabc8f2cc515#controlNumber",
								expectedJson)));
		Assert.assertThat(getValue("http://data.slub-dresden.de/schemas/Schema-65f19a84-1431-453e-a574-eabc8f2cc515#format", json),
				CoreMatchers.equalTo(getValue("http://data.slub-dresden.de/schemas/Schema-65f19a84-1431-453e-a574-eabc8f2cc515#format",
						expectedJson)));
		Assert.assertThat(
				getValueNode("http://data.slub-dresden.de/schemas/Schema-65f19a84-1431-453e-a574-eabc8f2cc515#fixedFields", json).size(),
				CoreMatchers.equalTo(
						getValueNode("http://data.slub-dresden.de/schemas/Schema-65f19a84-1431-453e-a574-eabc8f2cc515#fixedFields",
								expectedJson).size()));

		DataModelsResourceTest2.LOG.debug("end get JSON data test");
	}

	/**
	 * to ensure that the inbuilt schema won't be corrupted during processing
	 *
	 * @throws Exception
	 */
	@Test
	public void testNonSchemaComformOAIPMHMARCXMLData() throws Exception {

		DataModelsResourceTest2.LOG.debug("start get non-schema comform OAI-PMH+MARCXML XML data test");

		// START DATA MODEL CREATION

		final String dataResourceResourceFileName = "dd-1024.oai-pmh_marcxml.resource.json";
		final String dataResourceFileName = "dd-1024.oai-pmh_marcxml.controller.xml";
		final String configurationFileName = "oai-pmh_marcxml.configuration.json";
		final String dataModelName = "oai-pmh+marcxml";
		final String dataModelUuid = UUIDService.getUUID(DataModel.class.getSimpleName());

		final DataModel dataModel = createDataModel2(dataResourceResourceFileName, dataResourceFileName, configurationFileName, dataModelName,
				dataModelUuid, null, true, false);

		// END DATA MODEL CREATION

		final Response response = target(String.valueOf(dataModel.getUuid())).request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String freshDataModelString = response.readEntity(String.class);
		final DataModel freshDataModel = objectMapper.readValue(freshDataModelString, DataModel.class);

		Assert.assertNotNull(freshDataModel);

		final Schema freshSchema = freshDataModel.getSchema();

		Assert.assertNotNull(freshSchema);

		final String actualAttributePaths = parseAttributePaths(freshSchema);

		Assert.assertNotNull(actualAttributePaths);

		final String expectedAttributePaths = DMPPersistenceUtil.getResourceAsString("dd1024.oai-pmh_plus_marcxml_schema_-_attribute_paths.txt");

		Assert.assertEquals(expectedAttributePaths, actualAttributePaths);

		DataModelsResourceTest2.LOG.debug("end get non-schema comform OAI-PMH+MARCXML XML data test");
	}

	@Test
	public void testExceptionAtXMLData() throws Exception {

		DataModelsResourceTest2.LOG.debug("start throw Exception at XML data test");

		final String dataResourceResourceFileName = "test-mabxml-resource2.json";
		final String dataResourceFileName = "test-mabxml2-controller.xml";
		final String configurationFileName = "xml-configuration.json";
		final String dataModelUuid = UUIDService.getUUID(DataModel.class.getSimpleName());
		final String schemaUuid = UUIDService.getUUID(Schema.class.getSimpleName());
		final String dataModelName = dataModelUuid + " name";
		final String expectedResponse = "{\"status\":\"nok\",\"status_code\":500,\"error\":\"org.xml.sax.SAXParseException; lineNumber: 3; columnNumber: 1; XML document structures must start and end within the same entity.\"}";

		doNegativeDataModelTest(dataResourceResourceFileName, dataResourceFileName, configurationFileName, dataModelUuid, schemaUuid, dataModelName,
				expectedResponse);

		DataModelsResourceTest2.LOG.debug("end throw Exception at XML data test");
	}

	@Test
	public void testExceptionAtXMLData2() throws Exception {

		DataModelsResourceTest2.LOG.debug("start throw Exception at XML data test 2");

		final String dataResourceResourceFileName = "test-dd-1371-resoure.json";
		final String dataResourceFileName = "TEMATEST.xml";
		final String configurationFileName = "dd-1371-xml-configuration.json";
		final String dataModelUuid = UUIDService.getUUID(DataModel.class.getSimpleName());
		final String schemaUuid = UUIDService.getUUID(Schema.class.getSimpleName());
		final String dataModelName = dataModelUuid + " name";
		final String expectedResponse = "{\"status\":\"nok\",\"status_code\":500,\"error\":\"org.xml.sax.SAXParseException; lineNumber: 2; columnNumber: 29; The document type declaration for root element type \\\"documentcontainer\\\" must end with '>'.\"}";

		doNegativeDataModelTest(dataResourceResourceFileName, dataResourceFileName, configurationFileName, dataModelUuid, schemaUuid, dataModelName,
				expectedResponse);

		DataModelsResourceTest2.LOG.debug("end throw Exception at XML data test 2");
	}

	/**
	 * note, data resource consists of two records and the error occurs in the second record
	 *
	 * @throws Exception
	 */
	@Test
	public void testExceptionAtXMLData3() throws Exception {

		DataModelsResourceTest2.LOG.debug("start throw Exception at XML data test 3");

		final String dataResourceResourceFileName = "test-dd-1371-resoure2.json";
		final String dataResourceFileName = "TEMATEST2.xml";
		final String configurationFileName = "dd-1371-xml-configuration.json";
		final String dataModelUuid = UUIDService.getUUID(DataModel.class.getSimpleName());
		final String schemaUuid = UUIDService.getUUID(Schema.class.getSimpleName());
		final String dataModelName = dataModelUuid + " name";
		final String expectedResponse = "{\"status\":\"nok\",\"status_code\":500,\"error\":\"org.xml.sax.SAXParseException; lineNumber: 292; columnNumber: 73; The entity name must immediately follow the '&' in the entity reference.\"}";

		doNegativeDataModelTest(dataResourceResourceFileName, dataResourceFileName, configurationFileName, dataModelUuid, schemaUuid, dataModelName,
				expectedResponse);

		DataModelsResourceTest2.LOG.debug("end throw Exception at XML data test 3");
	}

	@Test
	public void testWrongData() throws Exception {

		DataModelsResourceTest2.LOG.debug("start wrong data test");

		final String dataResourceResourceFileName = "test-mabxml-resource2.csv.json";
		final String dataResourceFileName = "test_csv-controller.csv";
		final String configurationFileName = "xml-configuration.json";
		final String dataModelUuid = UUIDService.getUUID(DataModel.class.getSimpleName());
		final String schemaUuid = UUIDService.getUUID(Schema.class.getSimpleName());
		final String dataModelName = dataModelUuid + " name";
		final String expectedResponse = "{\"status\":\"nok\",\"status_code\":500,\"error\":\"org.xml.sax.SAXParseException; lineNumber: 1; columnNumber: 1; Content is not allowed in prolog.\"}";

		doNegativeDataModelTest(dataResourceResourceFileName, dataResourceFileName, configurationFileName, dataModelUuid, schemaUuid, dataModelName,
				expectedResponse);

		DataModelsResourceTest2.LOG.debug("end wrong data test");
	}

	@Test
	public void testEmptyFile() throws Exception {

		DataModelsResourceTest2.LOG.debug("start empty file test");

		final String dataResourceResourceFileName = "test-mabxml-resource2.csv.empty.json";
		final String dataResourceFileName = "test_csv-controller.empty.csv";
		final String configurationFileName = "xml-configuration.json";
		final String dataModelUuid = UUIDService.getUUID(DataModel.class.getSimpleName());
		final String schemaUuid = UUIDService.getUUID(Schema.class.getSimpleName());
		final String dataModelName = dataModelUuid + " name";
		final String expectedResponse = "{\"status\":\"nok\",\"status_code\":500,\"error\":\"org.xml.sax.SAXParseException; Premature end of file.\"}";

		doNegativeDataModelTest(dataResourceResourceFileName, dataResourceFileName, configurationFileName, dataModelUuid, schemaUuid, dataModelName,
				expectedResponse);

		DataModelsResourceTest2.LOG.debug("end empty file test");
	}

	@Test
	public void testNotExistingDataResource() throws Exception {

		DataModelsResourceTest2.LOG.debug("start not-existing data resource test");

		// prepare resource
		final String resourceJSONString = DMPPersistenceUtil.getResourceAsString("test-mabxml-resource2.json");

		final Resource resource = objectMapper.readValue(resourceJSONString, Resource.class);

		final String configurationJSONString = DMPPersistenceUtil.getResourceAsString("xml-configuration.json");
		final Configuration configuration = objectMapper.readValue(configurationJSONString, Configuration.class);

		final String dataModel1Uuid = "DataModel-c5140a43-cb29-4e57-8ff8-2c590252318d";

		final DataModel dataModel1 = new DataModel(dataModel1Uuid);
		final String dataModelName = UUID.randomUUID().toString();
		dataModel1.setName(dataModelName);
		dataModel1.setDescription("my data model description");
		dataModel1.setDataResource(resource);
		dataModel1.setConfiguration(configuration);

		final String dataModelJSONString = objectMapper.writeValueAsString(dataModel1);

		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(dataModelJSONString));

		Assert.assertEquals("500 was expected", 500, response.getStatus());

		final String body = response.readEntity(String.class);

		Assert.assertEquals(
				"{\"status\":\"nok\",\"status_code\":500,\"error\":\"The data resource '1' at path 'dummy/path/to/test-mabxml2.xml' of data model 'DataModel-c5140a43-cb29-4e57-8ff8-2c590252318d' does not exist. Hence, the data of the data model cannot be processed.\"}",
				body);

		DataModelsResourceTest2.LOG.debug("end not-existing data resource test");
	}

	@Test
	public void testWrongRecordTagAtXMLData() throws Exception {

		DataModelsResourceTest2.LOG.debug("start wrong record tag at XML data test");

		final String dataResourceResourceFileName = "test-mabxml-resource.json";
		final String dataResourceFileName = "controller_test-mabxml.xml";
		final String configurationFileName = "xml-configuration.2.json";
		final String dataModelUuid = "DataModel-e1677fea-9b35-46d0-acb0-8c6c0ff7c0c4";
		final String schemaUuid = "Schema-b4316dbb-3a4c-4653-b58a-24eed7d76b69";
		final String dataModelName = dataModelUuid + " name";
		final String expectedResponse = "{\"status\":\"nok\",\"status_code\":500,\"error\":\"couldn't transform any record from XML data resource at '/home/tgaengler/git/tgaengler/dswarm/tmp/resources/controller_test-mabxml.xml' to GDM for data model 'DataModel-e1677fea-9b35-46d0-acb0-8c6c0ff7c0c4'; maybe you set a wrong record tag (current one = 'record')\"}";

		doNegativeDataModelTest(dataResourceResourceFileName, dataResourceFileName, configurationFileName, dataModelUuid, schemaUuid, dataModelName,
				expectedResponse);

		DataModelsResourceTest2.LOG.debug("end wrong record tag at XML data test");
	}

	@Test
	public void testSearchRecordsCSVData() throws Exception {

		DataModelsResourceTest2.LOG.debug("start search records in CSV data test");

		final DataModel dataModel = testCSVDataInternal();

		final String dataResourceSchemaBaseURI = DataModelUtils.determineDataModelSchemaBaseURI(dataModel);

		Assert.assertNotNull("the data resource schema base uri shouldn't be null", dataResourceSchemaBaseURI);

		final String keyAttributePathString = dataResourceSchemaBaseURI + "name";
		final String searchValue = "foo";

		final ObjectNode requestJSON = objectMapper.createObjectNode();
		requestJSON.put(DMPStatics.KEY_ATTRIBUTE_PATH_IDENTIFIER, keyAttributePathString);
		requestJSON.put(DMPStatics.SEARCH_VALUE_IDENTIFIER, searchValue);

		final Response response = target(String.valueOf(dataModel.getUuid()), "/records/search").request(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(requestJSON));

		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getStatus());

		final String body = response.readEntity(String.class);

		Assert.assertNotNull(body);

		final String expectedResult = DMPPersistenceUtil.getResourceAsString("csv.record.search.result.json");

		Assert.assertNotNull(expectedResult);
		Assert.assertEquals(expectedResult.length(), body.length());

		DataModelsResourceTest2.LOG.debug("end search records in CSV data test");
	}

	private void doNegativeDataModelTest(final String dataResourceResourceFileName, final String dataResourceFileName,
			final String configurationFileName, final String dataModelUuid, final String schemaUuid, final String dataModelName,
			final String expectedResponse)
			throws Exception {
		final Response response = createDataModel(dataResourceResourceFileName, dataResourceFileName, configurationFileName,
				dataModelName, dataModelUuid, schemaUuid);

		Assert.assertEquals("500 was expected", 500, response.getStatus());

		final String body = response.readEntity(String.class);

		Assert.assertEquals(expectedResponse, body);
	}

	private DataModel createDataModel2(final String dataResourceResourceFileName,
	                                   final String dataResourceFileName,
	                                   final String configurationFileName,
	                                   final String dataModelName,
	                                   final String dataModelUuid,
	                                   final String schemaUuid,
	                                   final boolean doIngest,
	                                   final boolean enhanceDataResource) throws Exception {

		final String dataModelJSONString = createDataModelInternal(dataResourceResourceFileName, dataResourceFileName, configurationFileName,
				dataModelName, dataModelUuid, schemaUuid);

		return pojoClassResourceTestUtils.createObjectWithoutComparison(dataModelJSONString, doIngest, enhanceDataResource);
	}

	private Response createDataModel(final String dataResourceResourceFileName, final String dataResourceFileName,
			final String configurationFileName, final String dataModelName, final String dataModelUuid, final String schemaUuid) throws Exception {

		final String dataModelJSONString = createDataModelInternal(dataResourceResourceFileName, dataResourceFileName, configurationFileName,
				dataModelName, dataModelUuid, schemaUuid);

		return target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(dataModelJSONString));
	}

	private String createDataModelInternal(final String dataResourceResourceFileName, final String dataResourceFileName,
			final String configurationFileName, final String dataModelName, final String dataModelUuid, final String schemaUuid) throws Exception {
		// prepare resource
		final String resourceJSONString = DMPPersistenceUtil.getResourceAsString(dataResourceResourceFileName);

		final Resource expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

		final URL fileURL = Resources.getResource(dataResourceFileName);
		final File resourceFile = FileUtils.toFile(fileURL);

		// create resource
		final Resource resource = resourcesResourceTestUtils.uploadResource(resourceFile, expectedResource);

		final String configurationJSONString = DMPPersistenceUtil.getResourceAsString(configurationFileName);

		final Configuration configuration = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);

		final DataModel dataModel1 = new DataModel(dataModelUuid);
		dataModel1.setName("my " + dataModelName + " data model");
		dataModel1.setDescription("my " + dataModelName + " data model description");
		dataModel1.setDataResource(resource);
		dataModel1.setConfiguration(configuration);

		if (schemaUuid != null) {

			final Schema schema1 = new Schema(schemaUuid);

			dataModel1.setSchema(schema1);
		}

		return objectMapper.writeValueAsString(dataModel1);
	}

	private String parseAttributePaths(final Schema schema) {

		final Collection<SchemaAttributePathInstance> sapis = schema.getAttributePaths();

		if (sapis == null) {

			return null;
		}

		final StringBuilder sb = new StringBuilder();

		for (final SchemaAttributePathInstance sapi : sapis) {

			final AttributePath attributePath = sapi.getAttributePath();

			if (attributePath == null) {

				continue;
			}

			sb.append(attributePath.toAttributePath()).append("\n");
		}

		return sb.toString();
	}

	private JsonNode getValueNode(final String key, final JsonNode json) {

		Assert.assertNotNull("the JSON structure shouldn't be null", json);
		Assert.assertTrue("the JSON structure should be an array", json.isArray());
		Assert.assertNotNull("the key shouldn't be null", key);

		for (final JsonNode jsonEntry : json) {

			Assert.assertTrue("the entries of the JSON array should be JSON objects", jsonEntry.isObject());

			final JsonNode jsonNode = jsonEntry.get(key);

			if (jsonNode == null) {

				continue;
			}

			return jsonNode;
		}

		Assert.assertTrue("couldn't find element with key '" + key + "' in JSON structure '" + json + "'", false);

		return null;
	}

	private String getValue(final String key, final JsonNode json) {

		final JsonNode jsonNode = getValueNode(key, json);

		if (jsonNode == null) {

			Assert.assertTrue("couldn't find element with key '" + key + "' in JSON structure '" + json + "'", false);

			return null;
		}

		Assert.assertTrue("the value should be a string", jsonNode.isTextual());

		return jsonNode.asText();
	}

	private DataModel testCSVDataInternal() throws Exception {

		// START DATA MODEL CREATION

		final String dataResourceResourceFileName = "resource.json";
		final String dataResourceFileName = "test_csv-controller.csv";
		final String configurationFileName = "controller_configuration.json";
		final String dataModelName = "csv data model";
		final String dataModelUuid = "DataModel-f8741965-0ba5-4c6b-ac47-ea68b80820cc";
		final String schemaUuid = "Schema-55ab9fe2-5fb0-4cfe-bbe4-70bfbef652d8";

		final DataModel dataModel = createDataModel2(dataResourceResourceFileName, dataResourceFileName, configurationFileName, dataModelName,
				dataModelUuid, schemaUuid, true, false);

		// END DATA MODEL CREATION

		final int atMost = 1;

		final Tuple<Optional<Map<String, Model>>, ObjectNode> result = readData(dataModel, Optional.of(atMost));

		final Optional<Map<String, Model>> data = result.v1();
		final ObjectNode assoziativeJsonArray = result.v2();
		final String recordId = data.get().keySet().iterator().next();

		Assert.assertThat(assoziativeJsonArray.size(), CoreMatchers.equalTo(atMost));

		final JsonNode json = assoziativeJsonArray.get(recordId);

		Assert.assertNotNull("the JSON structure for record '" + recordId + "' shouldn't be null", json);

		final String dataResourceSchemaBaseURI = DataModelUtils.determineDataModelSchemaBaseURI(dataModel);

		Assert.assertNotNull("the data resource schema base uri shouldn't be null", dataResourceSchemaBaseURI);

		Assert.assertThat(getValue(dataResourceSchemaBaseURI + "id", json),
				CoreMatchers.equalTo(getValue(dataResourceSchemaBaseURI + "id", data.get().get(recordId).toRawJSON())));
		Assert.assertThat(getValue(dataResourceSchemaBaseURI + "year", json),
				CoreMatchers.equalTo(getValue(dataResourceSchemaBaseURI + "year", data.get().get(recordId).toRawJSON())));
		Assert.assertThat(getValue(dataResourceSchemaBaseURI + "description", json),
				CoreMatchers.equalTo(getValue(dataResourceSchemaBaseURI + "description", data.get().get(recordId).toRawJSON())));
		Assert.assertThat(getValue(dataResourceSchemaBaseURI + "name", json),
				CoreMatchers.equalTo(getValue(dataResourceSchemaBaseURI + "name", data.get().get(recordId).toRawJSON())));
		Assert.assertThat(getValue(dataResourceSchemaBaseURI + "isbn", json),
				CoreMatchers.equalTo(getValue(dataResourceSchemaBaseURI + "isbn", data.get().get(recordId).toRawJSON())));

		return dataModel;
	}

	private Tuple<Optional<Map<String, Model>>, ObjectNode> readData(final DataModel dataModel, final Optional<Integer> optionalAtMost)
			throws DMPPersistenceException {

		final InternalModelServiceFactory serviceFactory = GuicedTest.injector.getInstance(Key.get(InternalModelServiceFactory.class));
		final InternalModelService service = serviceFactory.getInternalGDMGraphService();
		final Observable<Map<String, Model>> dataObservable = service
				.getObjects(dataModel.getUuid(), optionalAtMost)
				.toMap(Tuple::v1, Tuple::v2);
		final Optional<Map<String, Model>> data = dataObservable.map(Optional::of).toBlocking().firstOrDefault(Optional.empty());

		Assert.assertTrue(data.isPresent());
		Assert.assertFalse(data.get().isEmpty());

		if (optionalAtMost.isPresent()) {

			Assert.assertThat(data.get().size(), CoreMatchers.equalTo(optionalAtMost.get()));
		}

		final WebTarget target = target(String.valueOf(dataModel.getUuid()), "data");

		final WebTarget finalTarget;

		if (optionalAtMost.isPresent()) {

			finalTarget = target.queryParam("atMost", optionalAtMost.get());
		} else {

			finalTarget = target;
		}

		final Response response = finalTarget.request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final ObjectNode assoziativeJsonArray = response.readEntity(ObjectNode.class);

		return Tuple.tuple(data, assoziativeJsonArray);
	}

	private Tuple<JsonNode, JsonNode> testXMLDataInternal(final String dataResourceResourceFileName,
	                                                      final String dataResourceFileName,
	                                                      final String configurationFileName,
	                                                      final String dataModelName,
	                                                      final boolean doIngest,
	                                                      final boolean enhanceDataResource) throws Exception {

		// START DATA MODEL CREATION

		final String dataModelUuid = UUIDService.getUUID(DataModel.class.getSimpleName());
		final String schemaUuid = UUIDService.getUUID(Schema.class.getSimpleName());

		final DataModel dataModel = createDataModel2(dataResourceResourceFileName, dataResourceFileName, configurationFileName, dataModelName,
				dataModelUuid, schemaUuid, doIngest, enhanceDataResource);

		// END DATA MODEL CREATION

		final int atMost = 1;

		final Tuple<Optional<Map<String, Model>>, ObjectNode> result = readData(dataModel, Optional.of(atMost));

		final Optional<Map<String, Model>> data = result.v1();
		final ObjectNode assoziativeJsonArray = result.v2();
		final String recordId = data.get().keySet().iterator().next();

		Assert.assertThat(assoziativeJsonArray.size(), CoreMatchers.equalTo(atMost));

		final JsonNode json = assoziativeJsonArray.get(recordId);

		final JsonNode expectedJson = data.get().get(recordId).toRawJSON();

		Assert.assertNotNull("the expected data JSON shouldn't be null", expectedJson);

		return Tuple.tuple(json, expectedJson);
	}
}
