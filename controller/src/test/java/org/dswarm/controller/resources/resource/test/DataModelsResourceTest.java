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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.io.Resources;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.glassfish.jersey.client.rx.RxWebTarget;
import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import rx.Observable;
import rx.schedulers.Schedulers;

import org.dswarm.common.MediaTypeUtil;
import org.dswarm.common.types.Tuple;
import org.dswarm.controller.eventbus.CSVConverterEvent;
import org.dswarm.controller.eventbus.CSVConverterEventRecorder;
import org.dswarm.controller.resources.POJOFormat;
import org.dswarm.controller.resources.resource.test.utils.ConfigurationsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ExportTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.ClaszesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.SchemasResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.controller.test.GuicedTest;
import org.dswarm.init.ExecutionScope;
import org.dswarm.persistence.dto.ShortExtendendBasicDMPDTO;
import org.dswarm.persistence.dto.resource.MediumDataModelDTO;
import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.UpdateFormat;
import org.dswarm.persistence.model.resource.proxy.ProxyDataModel;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.InternalModelService;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.resource.test.utils.DataModelServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.SchemaServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

import static com.codahale.metrics.MetricRegistry.name;

public class DataModelsResourceTest extends
		BasicResourceTest<DataModelsResourceTestUtils, DataModelServiceTestUtils, DataModelService, ProxyDataModel, DataModel> {

	private static final Logger LOG = LoggerFactory.getLogger(DataModelsResourceTest.class);

	private ResourcesResourceTestUtils resourcesResourceTestUtils;

	private ConfigurationsResourceTestUtils configurationsResourceTestUtils;

	private SchemasResourceTestUtils schemasResourceTestUtils;

	private ClaszesResourceTestUtils claszesResourceTestUtils;

	public DataModelsResourceTest() {

		super(DataModel.class, DataModelService.class, "datamodels", "datamodel.json", new DataModelsResourceTestUtils());
	}

	@Override
	protected void initObjects() {

		super.initObjects();

		pojoClassResourceTestUtils = new DataModelsResourceTestUtils();
		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
		schemasResourceTestUtils = new SchemasResourceTestUtils();
		claszesResourceTestUtils = new ClaszesResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		// START configuration preparation

		final Configuration configuration = configurationsResourceTestUtils.createObject("configuration2.json");

		// END configuration preparation

		// START resource preparation

		// prepare resource json for configuration ids manipulation
		String resourceJSONString = DMPPersistenceUtil.getResourceAsString("resource1.json");
		final ObjectNode resourceJSON = objectMapper.readValue(resourceJSONString, ObjectNode.class);

		final ArrayNode configurationsArray = objectMapper.createArrayNode();

		final String persistedConfigurationJSONString = objectMapper.writeValueAsString(configuration);
		final ObjectNode persistedConfigurationJSON = objectMapper.readValue(persistedConfigurationJSONString, ObjectNode.class);

		configurationsArray.add(persistedConfigurationJSON);

		resourceJSON.set("configurations", configurationsArray);

		// re-init expect resource
		resourceJSONString = objectMapper.writeValueAsString(resourceJSON);
		final Resource expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

		Assert.assertNotNull("expected resource shouldn't be null", expectedResource);

		final Resource resource = resourcesResourceTestUtils.createObject(resourceJSONString, expectedResource);

		// END resource preparation

		// START schema preparation

		final SchemaServiceTestUtils schemaServiceTestUtils = schemasResourceTestUtils.getPersistenceServiceTestUtils();

		final Schema schema = schemaServiceTestUtils.createAndPersistAlternativeSchema();

		// END schema preparation

		// START data model preparation

		// prepare data model json for resource, configuration and schema manipulation
		final ObjectNode objectJSON = objectMapper.readValue(objectJSONString, ObjectNode.class);

		final String finalResourceJSONString = objectMapper.writeValueAsString(resource);
		final ObjectNode finalResourceJSON = objectMapper.readValue(finalResourceJSONString, ObjectNode.class);

		objectJSON.set("data_resource", finalResourceJSON);

		final String finalConfigurationJSONString = objectMapper.writeValueAsString(resource.getConfigurations().iterator().next());
		final ObjectNode finalConfigurationJSON = objectMapper.readValue(finalConfigurationJSONString, ObjectNode.class);

		objectJSON.set("configuration", finalConfigurationJSON);

		final String finalSchemaJSONString = objectMapper.writeValueAsString(schema);
		final ObjectNode finalSchemaJSON = objectMapper.readValue(finalSchemaJSONString, ObjectNode.class);

		objectJSON.set("schema", finalSchemaJSON);

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(objectJSON);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);

		// END data model preparation
	}

	@Test
	public void testMABXMLData() throws Exception {

		DataModelsResourceTest.LOG.debug("start get MABXML data test");

		testMABXMLDataInternal();

		DataModelsResourceTest.LOG.debug("end get MABXML data test");
	}

	@Test
	public void testPNXExport() throws Exception {

		testXMLExport("PNX", "test-pnx-resource.json", "test-pnx2-controller.xml", "pnx-configuration.json", "pnx", "test-pnx2-expected.xml");
	}

	@Test
	public void testCSVXMLExport() throws Exception {

		testXMLExport("CSV XML", "test-csv-resource.json", "test_csv-controller.csv", "test-csv-configuration.json", "csv", "test-csv-expected.xml");
	}

	@Test
	public void testDeprecateDataModel() throws Exception {

		final String dataModelUuid = testMABXMLDataInternal();

		final WebTarget target = target(dataModelUuid, "deprecate");

		final RxWebTarget<RxObservableInvoker> rxWebTarget = RxObservable.from(target);

		// POST the request
		final RxObservableInvoker rx = rxWebTarget.request(MediaType.TEXT_PLAIN).rx();

		final Entity<String> entity = Entity.entity("", MediaType.TEXT_PLAIN);

		final Observable<Response> post = rx.post(entity).subscribeOn(Schedulers.from(EXECUTOR_SERVICE));

		final Response response = post.toBlocking().firstOrDefault(null);

		Assert.assertNotNull(response);

		final int status = response.getStatus();

		Assert.assertEquals(200, status);

		final String body = response.readEntity(String.class);

		final ObjectNode bodyJSON = objectMapper.readValue(body, ObjectNode.class);

		final JsonNode deprecated = bodyJSON.get("deprecated");

		Assert.assertNotNull(deprecated);

		final int deprecatedRelationships = deprecated.asInt();

		Assert.assertEquals(152, deprecatedRelationships);
	}

	@Test
	public void testDeprecateRecords() throws Exception {

		final String dataModelUuid = testMABXMLDataInternal();

		final WebTarget target = target(dataModelUuid, "deprecate", "records");

		final RxWebTarget<RxObservableInvoker> rxWebTarget = RxObservable.from(target);

		// POST the request
		final RxObservableInvoker rx = rxWebTarget.request(MediaType.APPLICATION_JSON).rx();

		final ArrayNode recordURIsArray = objectMapper.createArrayNode();

		final ObjectNode dataArray = getData(dataModelUuid);

		final String recordURI = dataArray.fieldNames().next();

		recordURIsArray.add(recordURI);

		final String recordURIsArrayJSONString = objectMapper.writeValueAsString(recordURIsArray);

		final Entity<String> entity = Entity.entity(recordURIsArrayJSONString, MediaType.APPLICATION_JSON);

		final Observable<Response> post = rx.post(entity).subscribeOn(Schedulers.from(EXECUTOR_SERVICE));

		final Response response = post.toBlocking().firstOrDefault(null);

		Assert.assertNotNull(response);

		final int status = response.getStatus();

		Assert.assertEquals(200, status);

		final String body = response.readEntity(String.class);

		final ObjectNode bodyJSON = objectMapper.readValue(body, ObjectNode.class);

		final JsonNode deprecated = bodyJSON.get("deprecated");

		Assert.assertNotNull(deprecated);

		final int deprecatedRelationships = deprecated.asInt();

		Assert.assertEquals(152, deprecatedRelationships);
	}

	@Test
	public void testDataMissing() throws Exception {

		DataModelsResourceTest.LOG.debug("start get data missing test");

		final Response response = target("42", "data").request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertThat("404 Not Found was expected", response.getStatus(), CoreMatchers.equalTo(404));
		Assert.assertThat(response.hasEntity(), CoreMatchers.equalTo(false));

		DataModelsResourceTest.LOG.debug("end get resource configuration data missing test");
	}

	/**
	 * Test export of a single graph to N3
	 *
	 * @throws Exception
	 */
	@Test
	public void testCsvImportWithMonitoring() throws Exception {

		final DataModel datamodelUTF8csv = loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");

		final ExecutionScope scope = GuicedTest.injector.getInstance(ExecutionScope.class);
		final CSVConverterEventRecorder recorder = GuicedTest.injector.getInstance(CSVConverterEventRecorder.class);

		try (final ExecutionScope ignore = scope.enter()) {
			recorder.convertConfiguration(new CSVConverterEvent(datamodelUTF8csv, UpdateFormat.FULL, false));
			final MetricRegistry registry = GuicedTest.injector.getInstance(Key.get(MetricRegistry.class, Names.named("Monitoring")));

			final String ingestTimerName = name(DataModel.class, "ingest");

			final Timer ingest = registry.timer(ingestTimerName);
			Assert.assertThat(ingest.getCount(), CoreMatchers.equalTo(1L));

			final String resourceMeterName = name(Resource.class, datamodelUTF8csv.getDataResource().getUuid());
			final Meter resource = registry.meter(resourceMeterName);
			Assert.assertThat(resource.getCount(), CoreMatchers.equalTo(1L));

			final String schemaMeterName = name(Schema.class, datamodelUTF8csv.getSchema().getUuid());
			final Meter schema = registry.meter(schemaMeterName);
			Assert.assertThat(schema.getCount(), CoreMatchers.equalTo(1L));
		}
	}

	/**
	 * Test export of a single graph to N3
	 *
	 * @throws Exception
	 */
	@Test
	public void testExportDataModelAsN3() throws Exception {

		// prepare: load data to mysql and graph db
		// SR hint: the resource's description needs to be "this is a description" since this is hard coded in
		// org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils.uploadResource(File, Resource)
		// should be refactored some day

		// FIXME even though the export of data containing UTF-8 characters is requested, the exported data is not checked for
		// encoding issues yet
		final DataModel datamodelUTF8csv = loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal(MediaTypeUtil.N3, datamodelUTF8csv.getUuid(), HttpStatus.SC_OK, MediaTypeUtil.N3_TYPE, "UTF-8.n3", ".n3");

	}

	/**
	 * Test export of a single graph to RDF_XML
	 *
	 * @throws Exception
	 */
	@Test
	public void testExportDataModelAsRDF_XML() throws Exception {

		// prepare: load data to mysql and graph db
		// SR hint: the resource's description needs to be "this is a description" since this is hard coded in
		// org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils.uploadResource(File, Resource)
		// should be refactored some day
		final DataModel datamodelUTF8csv = loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal(MediaTypeUtil.RDF_XML, datamodelUTF8csv.getUuid(), HttpStatus.SC_OK, MediaTypeUtil.RDF_XML_TYPE, "UTF-8.n3", ".rdf");

	}

	/**
	 * Test export of a single graph to N_QUADS
	 *
	 * @throws Exception
	 */
	@Test
	public void testExportDataModelAsN_QUADS() throws Exception {
		// prepare: load data to mysql and graph db
		// SR hint: the resource's description needs to be "this is a description" since this is hard coded in
		// org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils.uploadResource(File, Resource)
		// should be refactored some day
		final DataModel datamodelUTF8csv = loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal(MediaTypeUtil.N_QUADS, datamodelUTF8csv.getUuid(), HttpStatus.SC_OK, MediaTypeUtil.N_QUADS_TYPE, "UTF-8.n3", ".nq");

	}

	/**
	 * Test export of a single graph to TRIG
	 *
	 * @throws Exception
	 */
	@Test
	public void testExportDataModelAsTRIG() throws Exception {
		// prepare: load data to mysql and graph db
		// SR hint: the resource's description needs to be "this is a description" since this is hard coded in
		// org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils.uploadResource(File, Resource)
		// should be refactored some day
		final DataModel datamodelUTF8csv = loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal(MediaTypeUtil.TRIG, datamodelUTF8csv.getUuid(), HttpStatus.SC_OK, MediaTypeUtil.TRIG_TYPE, "UTF-8.n3", ".trig");

	}

	/**
	 * Test export of a single graph to N_QUADS
	 *
	 * @throws Exception
	 */
	@Test
	public void testExportDataModelAsTURTLE() throws Exception {
		// prepare: load data to mysql and graph db
		// SR hint: the resource's description needs to be "this is a description" since this is hard coded in
		// org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils.uploadResource(File, Resource)
		// should be refactored some day
		final DataModel datamodelUTF8csv = loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal(MediaTypeUtil.TURTLE, datamodelUTF8csv.getUuid(), HttpStatus.SC_OK, MediaTypeUtil.TURTLE_TYPE, "UTF-8.n3", ".ttl");

	}

	/**
	 * Test export of a single graph to default format that is chosen by graph db in case no format is requested (i.e. empty
	 * format parameter)
	 *
	 * @throws Exception
	 */
	@Test
	public void testExportDataModelAsEmptyFormatParameter() throws Exception {
		// prepare: load data to mysql and graph db
		// SR hint: the resource's description needs to be "this is a description" since this is hard coded in
		// org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils.uploadResource(File, Resource)
		// should be refactored some day
		final DataModel datamodelUTF8csv = loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal("", datamodelUTF8csv.getUuid(), HttpStatus.SC_OK, MediaTypeUtil.N_QUADS_TYPE, "UTF-8.n3", ".nq");

	}

	/**
	 * Test export of a single graph to default format that is chosen by graph db in case no format is requested (i.e. no format
	 * parameter is provided)
	 *
	 * @throws Exception
	 */
	@Test
	public void testExportDataModelAsMissingFormatParameter() throws Exception {
		// prepare: load data to mysql and graph db
		// SR hint: the resource's description needs to be "this is a description" since this is hard coded in
		// org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils.uploadResource(File, Resource)
		// should be refactored some day
		final DataModel datamodelUTF8csv = loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal(null, datamodelUTF8csv.getUuid(), HttpStatus.SC_OK, MediaTypeUtil.N_QUADS_TYPE, "UTF-8.n3", ".nq");

	}

	/**
	 * Test export of a single graph that is not existing in database. a HTTP 404 (not found) response is expected.
	 *
	 * @throws Exception
	 */
	@Test
	public void testExportDataModelFromNotExistingDatamodel() throws Exception {

		// hint: do not load any data

		testExportInternal(MediaTypeUtil.N_QUADS, "0815", HttpStatus.SC_NOT_FOUND, null, null, null);

	}

	/**
	 * Test export of a single graph to to text/plain format. This format is not supported, a HTTP 406 (not acceptable) response
	 * is expected.
	 *
	 * @throws Exception
	 */
	@Test
	public void testExportDataModelAsUnsupportedFormat() throws Exception {
		// prepare: load data to mysql and graph db
		// SR hint: the resource's description needs to be "this is a description" since this is hard coded in
		// org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils.uploadResource(File, Resource)
		// should be refactored some day
		loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		final DataModel datamodelAtMostcsv = loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal(MediaType.TEXT_PLAIN, datamodelAtMostcsv.getUuid(), HttpStatus.SC_NOT_ACCEPTABLE, null, null, null);

	}

	/**
	 * Test export of a single graph to a not existing format by sending some "random" accept header value. A HTTP 406 (not
	 * acceptable) response is expected.
	 *
	 * @throws Exception
	 */
	@Test
	public void testExportDataModelAsRandomFormat() throws Exception {
		// prepare: load data to mysql and graph db
		// SR hint: the resource's description needs to be "this is a description" since this is hard coded in
		// org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils.uploadResource(File, Resource)
		// should be refactored some day
		loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		final DataModel datamodelAtMostcsv = loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal("khlav/kalash", datamodelAtMostcsv.getUuid(), HttpStatus.SC_NOT_ACCEPTABLE, null, null, null);

	}

	/**
	 * prepare: upload data and metadata of two csv files to mysql and graph db <br />
	 * request the export from BE proxy endpoint<br />
	 * assert the number of exported statements is equal to an expected value. the models themselves are not compared because of
	 * UUIDs generated while uploading the data
	 *
	 * @param requestedExportLanguage  the serialization format neo4j should export the data to. (this value is used as accept
	 *                                 header arg to query neo4j)
	 * @param datamodelUuid            identifier of the datamodel to be exported
	 * @param expectedHTTPResponseCode the expected HTTP status code of the response, e.g. {@link org.apache.http.HttpStatus#SC_OK} or
	 *                                 {@link org.apache.http.HttpStatus#SC_NOT_ACCEPTABLE}
	 * @param expectedExportMediaType  the language the exported data is expected to be serialized in. hint: language may differ
	 *                                 from {@code requestedExportLanguage} to test for default values. (ignored if expectedHTTPResponseCode !=
	 *                                 {@link org.apache.http.HttpStatus#SC_OK})
	 * @param expectedModelFile        name of file containing a serialized model, this (expected) model is equal to the actual model
	 *                                 exported by neo4j. (ignored if expectedHTTPResponseCode != {@link org.apache.http.HttpStatus#SC_OK})
	 * @param expectedFileEnding       the expected file ending to be received from neo4j (ignored if expectedHTTPResponseCode !=
	 *                                 {@link org.apache.http.HttpStatus#SC_OK})
	 * @throws IOException
	 */
	private void testExportInternal(final String requestedExportLanguage, final String datamodelUuid, final int expectedHTTPResponseCode,
			final MediaType expectedExportMediaType, final String expectedModelFile, final String expectedFileEnding) throws Exception {

		// request export of a data model
		final String datamodelId = String.valueOf(datamodelUuid);
		WebTarget targetBE = target(datamodelId, "export");
		// be able to simulate absence of query parameter
		if (requestedExportLanguage != null) {
			targetBE = targetBE.queryParam("format", requestedExportLanguage);
		}

		final Response response = targetBE.request().get(Response.class);

		Assert.assertEquals("expected " + expectedHTTPResponseCode, expectedHTTPResponseCode, response.getStatus());

		// in case we requested an unsupported format or not existing data model, stop processing here since there is no exported
		// data to verify
		if (expectedHTTPResponseCode == HttpStatus.SC_NOT_FOUND || expectedHTTPResponseCode == HttpStatus.SC_NOT_ACCEPTABLE) {
			return;
		}

		// check Content-Type header for correct content type (hint: even though we did not request the content type via an accept
		// header field, we do want to get the content type specified in query parameter format)
		ExportTestUtils.checkContentTypeHeader(response, expectedExportMediaType.toString());

		// check Content-Disposition header for correct file ending
		ExportTestUtils.checkContentDispositionHeader(response, expectedFileEnding);

		// start check exported data
		final String body = response.readEntity(String.class);

		Assert.assertNotNull("response body shouldn't be null", body);

		LOG.trace("Response body:\n{}", body);

		final InputStream inputStream = new ByteArrayInputStream(body.getBytes("UTF-8"));

		Assert.assertNotNull("input stream (from body) shouldn't be null", inputStream);

		// read actual model from response body
		final Lang expectedExportLanguage = RDFLanguages.contentTypeToLang(expectedExportMediaType.toString());
		final com.hp.hpl.jena.rdf.model.Model actualModel = ModelFactory.createDefaultModel();
		RDFDataMgr.read(actualModel, inputStream, expectedExportLanguage);

		Assert.assertNotNull("actual model shouldn't be null", actualModel);
		LOG.debug("exported '{}' statements", actualModel.size());

		// read expected model from file
		final com.hp.hpl.jena.rdf.model.Model expectedModel = RDFDataMgr.loadModel(expectedModelFile);
		Assert.assertNotNull("expected model shouldn't be null", expectedModel);

		// compare models
		// note: rdf:type statement won't be delivered right now
		Assert.assertEquals("models should have same number of statements.", expectedModel.size() -1, actualModel.size());

		// this check can not be done because of generated UUIDs
		// check if statements are the "same" (isomorphic, i.e. blank nodes may have different IDs)
		// Assert.assertTrue("the RDF from the property graph is not isomorphic to the RDF in the original file ",
		// actualModel.isIsomorphicWith(expectedModel));
		// end check exported data
	}

	@Override
	public DataModel updateObject(final DataModel persistedDataModel) throws Exception {

		persistedDataModel.setName(persistedDataModel.getName() + " update");

		persistedDataModel.setDescription(persistedDataModel.getDescription() + " update");

		final Configuration updateConfiguration = configurationsResourceTestUtils.createObject("configuration2.json");
		persistedDataModel.setConfiguration(updateConfiguration);

		final Resource updateResource = resourcesResourceTestUtils.createObject("resource2.json");
		persistedDataModel.setDataResource(updateResource);

		final Clasz updateRecordClass = claszesResourceTestUtils.createObject("clasz2.json");

		final Schema schema = persistedDataModel.getSchema();
		schema.setName(schema.getName() + " update");
		schema.setRecordClass(updateRecordClass);

		persistedDataModel.setSchema(schema);

		final String updateDataModelJSONString = objectMapper.writeValueAsString(persistedDataModel);
		final DataModel expectedDataModel = objectMapper.readValue(updateDataModelJSONString, DataModel.class);
		Assert.assertNotNull("the data model JSON string shouldn't be null", updateDataModelJSONString);

		final DataModel updateDataModel = pojoClassResourceTestUtils.updateObject(updateDataModelJSONString, expectedDataModel);

		Assert.assertNotNull("the data model JSON string shouldn't be null", updateDataModel);
		Assert.assertEquals("data model id shoud be equal", updateDataModel.getUuid(), persistedDataModel.getUuid());
		Assert.assertEquals("data model name shoud be equal", updateDataModel.getName(), persistedDataModel.getName());
		Assert.assertEquals("data model description shoud be equal", updateDataModel.getDescription(), persistedDataModel.getDescription());
		Assert.assertEquals("data model schema shoud be equal", updateDataModel.getSchema(), persistedDataModel.getSchema());
		Assert.assertEquals("data model configuration shoud be equal", updateDataModel.getConfiguration(), persistedDataModel.getConfiguration());

		return updateDataModel;
	}

	private static JsonNode getValueNode(final String key, final JsonNode json) {

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

		Assert.fail("couldn't find element with key '" + key + "' in JSON structure '" + json + "'");

		return null;
	}

	private static String getValue(final String key, final JsonNode json) {

		final JsonNode jsonNode = getValueNode(key, json);

		if (jsonNode == null) {

			Assert.fail("couldn't find element with key '" + key + "' in JSON structure '" + json + "'");

			return null;
		}

		Assert.assertTrue("the value should be a string", jsonNode.isTextual());

		return jsonNode.asText();
	}

	private DataModel loadCSVData(final String resourceJsonFilename, final String csvFilename, final String configurationJsonFilename)
			throws Exception {

		LOG.debug("start load CSV data");

		final String resourceJSONString = DMPPersistenceUtil.getResourceAsString(resourceJsonFilename);

		final Resource expectedResource = GuicedTest.injector.getInstance(ObjectMapper.class).readValue(resourceJSONString, Resource.class);

		final URL fileURL = Resources.getResource(csvFilename);
		final File resourceFile = FileUtils.toFile(fileURL);

		final String configurationJSONString = DMPPersistenceUtil.getResourceAsString(configurationJsonFilename);

		// add resource and config
		final Resource resource = resourcesResourceTestUtils.uploadResource(resourceFile, expectedResource);

		final Configuration config = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);

		final String dataModelToCreateUuid = UUIDService.getUUID(DataModel.class.getSimpleName());

		final DataModel dataModelToCreate = new DataModel(dataModelToCreateUuid);
		dataModelToCreate.setName("my data model");
		dataModelToCreate.setDescription("my data model description");
		dataModelToCreate.setDataResource(resource);
		dataModelToCreate.setConfiguration(config);

		final String dataModelJSONString = objectMapper.writeValueAsString(dataModelToCreate);

		return pojoClassResourceTestUtils.createObjectWithoutComparison(dataModelJSONString);
	}

	private void testXMLExport(final String type, final String resourceJSONFile, final String dataResourceFile, final String configurationJSONFile,
			final String dataModelType, final String expectedXMLFile) throws Exception {

		DataModelsResourceTest.LOG.debug("start export {} export test", type);

		// prepare resource
		final String resourceJSONString = DMPPersistenceUtil.getResourceAsString(resourceJSONFile);

		final Resource expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

		final URL fileURL = Resources.getResource(dataResourceFile);
		final File resourceFile = FileUtils.toFile(fileURL);

		final String configurationJSONString = DMPPersistenceUtil.getResourceAsString(configurationJSONFile);

		// add resource and config
		final Resource resource = resourcesResourceTestUtils.uploadResource(resourceFile, expectedResource);

		final Configuration configuration = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);

		final String dataModel1Uuid = UUIDService.getUUID(DataModel.class.getSimpleName());

		final DataModel dataModel1 = new DataModel(dataModel1Uuid);
		dataModel1.setName("my " + dataModelType + " data model");
		dataModel1.setDescription("my " + dataModelType + " data model description");
		dataModel1.setDataResource(resource);
		dataModel1.setConfiguration(configuration);

		final String dataModelJSONString = objectMapper.writeValueAsString(dataModel1);

		// create (persist) data model (incl. content)
		final DataModel dataModel = pojoClassResourceTestUtils.createObjectWithoutComparison(dataModelJSONString);

		final Response response = target(String.valueOf(dataModel.getUuid()), "export").queryParam("format", MediaType.APPLICATION_XML).request()
				.accept(MediaType.APPLICATION_OCTET_STREAM_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String actualXML = response.readEntity(String.class);

		final String expectedXML = DMPPersistenceUtil.getResourceAsString(expectedXMLFile);

		// do comparison: check for XML similarity
		final Diff xmlDiff = DiffBuilder.compare(Input.fromString(expectedXML))
				.withTest(Input.fromString(actualXML)).checkForSimilar().ignoreWhitespace().build();

		Assert.assertFalse(xmlDiff.hasDifferences());

		DataModelsResourceTest.LOG.debug("end export {} export test", type);
	}

	@Test
	public void testGetShortObject() throws Exception {

		final String dataModelUuid = UUIDService.getUUID(DataModel.class.getSimpleName());
		final String dataModelName = "my pnx data model";
		final String dataModelDescription = "my pnx data model description";

		final String expectedJson =
				objectMapper.writeValueAsString(
						new ShortExtendendBasicDMPDTO(
								dataModelUuid,
								dataModelName,
								dataModelDescription,
								target(dataModelUuid).getUri().toString()
						)
				);

		final DataModel dataModel = new DataModel(dataModelUuid);
		dataModel.setName(dataModelName);
		dataModel.setDescription(dataModelDescription);
		final String dataModelJSONString = objectMapper.writeValueAsString(dataModel);

		// create (persist) data model
		pojoClassResourceTestUtils.createObjectWithoutComparison(dataModelJSONString);

		final Response response = target(dataModelUuid)
				.queryParam("format", POJOFormat.SHORT.toString())
				.request().get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String actualJson = response.readEntity(String.class);
		JSONAssert.assertEquals(expectedJson, actualJson, true);
	}

	@Test
	public void testGetMediumObject() throws Exception {

		final String resourceJSONFile = "test-pnx-resource.json";
		final String dataResourceFile = "test-pnx2-controller.xml";
		final String configurationJSONFile = "pnx-configuration.json";

		final String dataModelUuid = UUIDService.getUUID(DataModel.class.getSimpleName());
		final String dataModelName = "my pnx data model";
		final String dataModelDescription = "my pnx data model description";

		// prepare resource
		final String resourceJSONString = DMPPersistenceUtil.getResourceAsString(resourceJSONFile);
		final Resource expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

		final URL fileURL = Resources.getResource(dataResourceFile);
		final File resourceFile = FileUtils.toFile(fileURL);

		final String configurationJSONString = DMPPersistenceUtil.getResourceAsString(configurationJSONFile);

		// add resource and config
		final Resource resource = resourcesResourceTestUtils.uploadResource(resourceFile, expectedResource);
		final Configuration configuration = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);

		final String expectedJson =
				objectMapper.writeValueAsString(
						new MediumDataModelDTO(
								dataModelUuid,
								dataModelName,
								dataModelDescription,
								target(dataModelUuid).getUri().toString(),
								resource,
								configuration
						)
				);

		final DataModel dataModel = new DataModel(dataModelUuid);
		dataModel.setName(dataModelName);
		dataModel.setDescription(dataModelDescription);
		dataModel.setDataResource(resource);
		dataModel.setConfiguration(configuration);
		final String dataModelJSONString = objectMapper.writeValueAsString(dataModel);

		// create (persist) data model
		pojoClassResourceTestUtils.createObjectWithoutComparison(dataModelJSONString);

		final Response response = target(dataModelUuid)
				.queryParam("format", POJOFormat.MEDIUM.toString())
				.request().get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String actualJson = response.readEntity(String.class);

		JSONAssert.assertEquals(expectedJson, actualJson, true);
	}

	private String testMABXMLDataInternal() throws Exception {

		// prepare resource
		final String resourceJSONString = DMPPersistenceUtil.getResourceAsString("test-mabxml-resource.json");

		final Resource expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

		final URL fileURL = Resources.getResource("controller_test-mabxml.xml");
		final File resourceFile = FileUtils.toFile(fileURL);

		final String configurationJSONString = DMPPersistenceUtil.getResourceAsString("mabxml-configuration.json");

		// add resource and config
		final Resource resource = resourcesResourceTestUtils.uploadResource(resourceFile, expectedResource);

		final Configuration configuration = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);

		final String dataModel1Uuid = UUIDService.getUUID(DataModel.class.getSimpleName());

		final DataModel dataModel1 = new DataModel(dataModel1Uuid);
		dataModel1.setName("my data model");
		dataModel1.setDescription("my data model description");
		dataModel1.setDataResource(resource);
		dataModel1.setConfiguration(configuration);

		final String dataModelJSONString = objectMapper.writeValueAsString(dataModel1);

		final DataModel dataModel = pojoClassResourceTestUtils.createObjectWithoutComparison(dataModelJSONString);

		final int atMost = 1;

		final InternalModelServiceFactory serviceFactory = GuicedTest.injector.getInstance(Key.get(InternalModelServiceFactory.class));
		final InternalModelService service = serviceFactory.getInternalGDMGraphService();
		final Observable<Map<String, Model>> dataObservable = service
				.getObjects(dataModel.getUuid(), Optional.of(atMost))
				.toMap(Tuple::v1, Tuple::v2);
		final Optional<Map<String, Model>> data = dataObservable.map(Optional::of).toBlocking().firstOrDefault(Optional.absent());

		Assert.assertTrue(data.isPresent());
		Assert.assertFalse(data.get().isEmpty());
		Assert.assertThat(data.get().size(), CoreMatchers.equalTo(atMost));

		final String recordId = data.get().keySet().iterator().next();

		final Response response = target(String.valueOf(dataModel.getUuid()), "data").queryParam("atMost", atMost).request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		// final String assoziativeJsonArrayString = response.readEntity(String.class);
		//
		// System.out.println("result = '" + assoziativeJsonArrayString + "'");

		final ObjectNode assoziativeJsonArray = response.readEntity(ObjectNode.class);

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

		Assert.assertEquals(SchemaUtils.MABXML_SCHEMA_UUID, dataModel.getSchema().getUuid());

		return dataModel1Uuid;
	}

	private ObjectNode getData(final String dataModelUuid) throws IOException {

		final String data = pojoClassResourceTestUtils.getData(dataModelUuid, 1);

		return objectMapper.readValue(data, ObjectNode.class);
	}
}
