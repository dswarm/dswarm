/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.io.Resources;
import com.google.inject.Key;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		final Response response = target(String.valueOf(updateDataModel.getUuid()), "/data").request().post(Entity.text(""));

		Assert.assertEquals("200 Updated was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final Tuple<Optional<Map<String, Model>>, ObjectNode> result = readData(updateDataModel, Optional.<Integer>absent());

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
	public void testXMLData() throws Exception {

		DataModelsResourceTest2.LOG.debug("start get XML data test");

		// START DATA MODEL CREATION

		final String dataResourceResourceFileName = "test-mabxml-resource.json";
		final String dataResourceFileName = "controller_test-mabxml.xml";
		final String configurationFileName = "xml-configuration.json";
		final String dataModelName = "mabxml";

		final DataModel dataModel = createDataModel(dataResourceResourceFileName, dataResourceFileName, configurationFileName, dataModelName);

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

		//		System.out.println("expected JSON = '" + objectMapper.writeValueAsString(expectedJson) + "'");

		Assert.assertThat(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#status", json),
				CoreMatchers.equalTo(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#status", expectedJson)));
		Assert.assertThat(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#mabVersion", json),
				CoreMatchers.equalTo(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#mabVersion", expectedJson)));
		Assert.assertThat(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#typ", json),
				CoreMatchers.equalTo(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#typ", expectedJson)));
		Assert.assertThat(getValueNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld", json).size(),
				CoreMatchers.equalTo(getValueNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld", expectedJson).size()));

		DataModelsResourceTest2.LOG.debug("end get XML data test");
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
		final String dataResourceFileName = "dd-1024.oai-pmh_marcxml.xml";
		final String configurationFileName = "oai-pmh_marcxml.configuration.json";
		final String dataModelName = "oai-pmh+marcxml";

		final DataModel dataModel = createDataModel(dataResourceResourceFileName, dataResourceFileName, configurationFileName, dataModelName);

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

		// prepare resource
		final String resourceJSONString = DMPPersistenceUtil.getResourceAsString("test-mabxml-resource2.json");

		final Resource expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

		final URL fileURL = Resources.getResource("test-mabxml2-controller.xml");
		final File resourceFile = FileUtils.toFile(fileURL);

		final String configurationJSONString = DMPPersistenceUtil.getResourceAsString("xml-configuration.json");

		// add resource and config
		final Resource resource = resourcesResourceTestUtils.uploadResource(resourceFile, expectedResource);

		final Configuration configuration = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);

		final String dataModel1Uuid = UUIDService.getUUID(DataModel.class.getSimpleName());

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
				"{\"status\":\"nok\",\"status_code\":500,\"error\":\" 1; XML document structures must start and end within the same entity.\"}",
				body);

		DataModelsResourceTest2.LOG.debug("end throw Exception at XML data test");
	}

	private DataModel createDataModel(final String dataResourceResourceFileName, final String dataResourceFileName,
			final String configurationFileName, final String dataModelName) throws Exception {

		// prepare resource
		final String resourceJSONString = DMPPersistenceUtil.getResourceAsString(dataResourceResourceFileName);

		final Resource expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

		final URL fileURL = Resources.getResource(dataResourceFileName);
		final File resourceFile = FileUtils.toFile(fileURL);

		// create resource
		final Resource resource = resourcesResourceTestUtils.uploadResource(resourceFile, expectedResource);

		final String configurationJSONString = DMPPersistenceUtil.getResourceAsString(configurationFileName);

		final Configuration configuration = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);

		final String dataModel1Uuid = UUIDService.getUUID(DataModel.class.getSimpleName());

		final DataModel dataModel1 = new DataModel(dataModel1Uuid);
		dataModel1.setName("my " + dataModelName + " data model");
		dataModel1.setDescription("my " + dataModelName + " data model description");
		dataModel1.setDataResource(resource);
		dataModel1.setConfiguration(configuration);

		final String dataModelJSONString = objectMapper.writeValueAsString(dataModel1);

		return pojoClassResourceTestUtils.createObjectWithoutComparison(dataModelJSONString);
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
		final String dataModelName = "mabxml";

		final DataModel dataModel = createDataModel(dataResourceResourceFileName, dataResourceFileName, configurationFileName, dataModelName);

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
		final Optional<Map<String, Model>> data = service.getObjects(dataModel.getUuid(), optionalAtMost);

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
}
