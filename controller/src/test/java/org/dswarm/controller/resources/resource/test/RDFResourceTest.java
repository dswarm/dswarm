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

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.common.MediaTypeUtil;
import org.dswarm.common.rdf.utils.RDFUtils;
import org.dswarm.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ExportTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.controller.resources.test.ResourceTest;
import org.dswarm.controller.test.GuicedTest;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
public class RDFResourceTest extends ResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(RDFResourceTest.class);

	private ResourcesResourceTestUtils resourcesResourceTestUtils;

	private DataModelsResourceTestUtils dataModelsResourceTestUtils;

	protected final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	public RDFResourceTest() {

		super("rdf");
	}

	@Override protected void initObjects() {

		super.initObjects();

		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		dataModelsResourceTestUtils = new DataModelsResourceTestUtils();
	}

	@Test
	public void testExportAllToNQuads() throws Exception {

		exportInternal(MediaTypeUtil.N_QUADS, HttpStatus.SC_OK, MediaTypeUtil.N_QUADS_TYPE, ".nq");
	}

	@Test
	public void testExportAllToTriG() throws Exception {

		exportInternal(MediaTypeUtil.TRIG, HttpStatus.SC_OK, MediaTypeUtil.TRIG_TYPE, ".trig");
	}

	@Test
	public void testExportAllToDefaultFormat() throws Exception {

		exportInternal("", HttpStatus.SC_OK, MediaTypeUtil.N_QUADS_TYPE, ".nq");
	}

	@Test
	public void testExportAllNoFormatParameter() throws Exception {

		exportInternal(null, HttpStatus.SC_OK, MediaTypeUtil.N_QUADS_TYPE, ".nq");
	}

	@Test
	public void testExportAllToUnsupportedFormat() throws Exception {

		exportInternal(MediaTypeUtil.RDF_XML, HttpStatus.SC_NOT_ACCEPTABLE, null, null);
	}

	@Test
	public void testExportAllToRandomFormat() throws Exception {

		exportInternal("khlav/kalash", HttpStatus.SC_NOT_ACCEPTABLE, null, null);
	}

	/**
	 * Request the export of all data from BE proxy endpoint in the serialization format. <br />
	 * Count the number of statements in all serialized model file. (These are expected to be exported by db and hence should have
	 * been imported in db in a prepare step). <br />
	 * Assert the numbers of statements in the export is equal to the sum of statements of all serialized models.
	 *
	 * @param requestedExportLanguage  the requested serialization format to be used in export. may be empty to test the default
	 *                                 fallback of the endpoint.
	 * @param expectedHTTPResponseCode the expected HTTP status code of the response, e.g. {@link HttpStatus#SC_OK} or
	 *                                 {@link HttpStatus#SC_NOT_ACCEPTABLE}
	 * @param expectedExportMediaType  the language the exported data is expected to be serialized in. hint: language may differ
	 *                                 from {@code requestedExportLanguage} to test for default values. (ignored if expectedHTTPResponseCode !=
	 *                                 {@link HttpStatus#SC_OK})
	 * @param expectedFileEnding       the expected file ending to be received from neo4j (ignored if expectedHTTPResponseCode !=
	 *                                 {@link HttpStatus#SC_OK})
	 * @throws Exception
	 */
	private void exportInternal(final String requestedExportLanguage, final int expectedHTTPResponseCode, final MediaType expectedExportMediaType,
			final String expectedFileEnding)
			throws Exception {

		RDFResourceTest.LOG.debug("start test export all data to format \"" + requestedExportLanguage + "\"");

		// prepare: load data to mysql and graph db
		// SR hint: the resource's description needs to be "this is a description" since this is hard coded in
		// org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils.uploadResource(File, Resource)
		// should be refactored some day
		loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");
		loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");

		// request the export from BE proxy endpoint

		WebTarget targetBE = target("/getall");
		// be able to simulate absence of query parameter
		if (requestedExportLanguage != null) {
			targetBE = targetBE.queryParam("format", requestedExportLanguage);
		}

		final Response response = targetBE.request().get(Response.class);

		Assert.assertEquals("expected " + expectedHTTPResponseCode, expectedHTTPResponseCode, response.getStatus());

		// in case we requested an unsupported format, stop processing here since there is no exported data to verify
		if (expectedHTTPResponseCode == HttpStatus.SC_NOT_ACCEPTABLE) {
			return;
		}

		// check Content-Type header for correct content type (hint: even though we did not request the content type via an accept
		// header field, we do want to get the content type specified in query parameter format) 
		ExportTestUtils.checkContentTypeHeader(response, expectedExportMediaType.toString());

		// check Content-Disposition header for correct file ending
		ExportTestUtils.checkContentDispositionHeader(response, expectedFileEnding);

		// read data set from response
		final String body = response.readEntity(String.class);
		Assert.assertNotNull("response body  shouldn't be null", body);

		final InputStream stream = new ByteArrayInputStream(body.getBytes("UTF-8"));
		Assert.assertNotNull("input stream (from body) shouldn't be null", stream);

		final Lang expectedExportLanguage = RDFLanguages.contentTypeToLang(expectedExportMediaType.toString());
		final Dataset dataset = DatasetFactory.createMem();
		RDFDataMgr.read(dataset, stream, expectedExportLanguage);
		Assert.assertNotNull("dataset from response shouldn't be null", dataset);

		// count number of statements in exportet data set
		final long statementsInExportedRDFModel = RDFUtils.determineDatasetSize(dataset);

		LOG.debug("exported '" + statementsInExportedRDFModel + "' statements");

		// count number of statements that were loaded to db while prepare
		long expectedStatements = countStatementsInSerializedN3("atMostTwoRows.n3") + countStatementsInSerializedN3("UTF-8.n3");

		// + 2 triples from data model statements (for versioning)
		expectedStatements += 2;

		// compare number of exported statements with expected
		Assert.assertEquals("the number of exported statements should be " + expectedStatements, expectedStatements, statementsInExportedRDFModel);

		RDFResourceTest.LOG.debug("end test export all data as " + expectedExportLanguage);
	}

	// SR TODO refactor to dswarm-common

	/**
	 * Read a file, serialized in N3, and count the number of statements.
	 *
	 * @param resourceNameN3 the /path/to/file.end of a model serialized in N3 format
	 * @return the number of statements the serialized model contains
	 * @throws IOException
	 */
	public long countStatementsInSerializedN3(final String resourceNameN3) throws IOException {
		final URL fileURL = Resources.getResource(resourceNameN3);

		byte[] bodyBytes = Resources.toByteArray(fileURL);
		final InputStream expectedStream = new ByteArrayInputStream(bodyBytes);

		final com.hp.hpl.jena.rdf.model.Model modelFromOriginalRDFile = ModelFactory.createDefaultModel();
		modelFromOriginalRDFile.read(expectedStream, null, "N3");

		return modelFromOriginalRDFile.size();
	}

	private DataModel loadCSVData(final String resourceJsonFilename, final String csvFilename, final String configurationJsonFilename)
			throws Exception {

		RDFResourceTest.LOG.debug("start load CSV data");

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

		return dataModelsResourceTestUtils.createObjectWithoutComparison(dataModelJSONString);
	}

}
