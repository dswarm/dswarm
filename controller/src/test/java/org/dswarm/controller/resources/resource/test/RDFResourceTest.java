package org.dswarm.controller.resources.resource.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.common.MediaTypeUtil;
import org.dswarm.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ExportTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.controller.resources.test.ResourceTest;
import org.dswarm.controller.test.GuicedTest;
import org.dswarm.graph.rdf.utils.RDFUtils;
import org.dswarm.persistence.DMPPersistenceException;
// import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.service.internal.test.utils.InternalGDMGraphServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * Created by tgaengler on 28/04/14.
 * 
 * @author tgaengler
 */
public class RDFResourceTest extends ResourceTest {

	private static final Logger					LOG				= LoggerFactory.getLogger(RDFResourceTest.class);

	private final ResourcesResourceTestUtils	resourcesResourceTestUtils;

	private final DataModelsResourceTestUtils	dataModelsResourceTestUtils;

	protected final ObjectMapper				objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	public RDFResourceTest() {

		super("rdf");

		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		dataModelsResourceTestUtils = new DataModelsResourceTestUtils();

		initObjects();
	}

	/**
	 * reset mysql and graph db
	 * 
	 * @throws DMPPersistenceException
	 */
	@After
	public void tearDown2() throws DMPPersistenceException {
		maintainDBService.initDB();
		InternalGDMGraphServiceTestUtils.cleanGraphDB();
	}

	@Test
	public void testExportAllNQuads() throws Exception {

		exportInternal(MediaTypeUtil.N_QUADS, HttpStatus.SC_OK, Lang.NQUADS, ".nq");
	}

	@Test
	public void testExportAllTriG() throws Exception {

		exportInternal(MediaTypeUtil.TRIG, HttpStatus.SC_OK, Lang.TRIG, ".trig");
	}

	@Test
	public void testExportAllDefaultFormat() throws Exception {

		exportInternal("", HttpStatus.SC_OK, Lang.NQUADS, ".nq");
	}

	@Test
	public void testExportUnsupportedFormat() throws Exception {

		exportInternal(MediaTypeUtil.RDF_XML, HttpStatus.SC_NOT_ACCEPTABLE, null, null);
	}

	@Test
	public void testExportRandomFormat() throws Exception {

		exportInternal("khlav/kalash", HttpStatus.SC_NOT_ACCEPTABLE, null, null);
	}

	/**
	 * Request the export of all data from BE proxy endpoint in the serialization format. <br />
	 * Count the number of statements in all serialized model file. (These are expected to be exported by db and hence should have
	 * been imported in db in a prepare step). <br />
	 * Assert the numbers of statements in the export is equal to the sum of statements of all serialized models.
	 * 
	 * @param requestedExportLanguage the requested serialization format to be used in export. may be empty to test the default
	 *            fallback of the endpoint.
	 * @param expectedHTTPResponseCode the expected HTTP status code of the response, e.g. {@link HttpStatus.SC_OK} or
	 *            {@link HttpStatus.SC_NOT_ACCEPTABLE}
	 * @param expectedExportLanguage the language the exported data is expected to be serialized in. hint: language may differ
	 *            from {@code requestedExportLanguage} to test for default values. (ignored if expectedHTTPResponseCode !=
	 *            {@link HttpStatus.SC_OK})
	 * @param expectedFileEnding the expected file ending to be received from neo4j (ignored if expectedHTTPResponseCode !=
	 *            {@link HttpStatus.SC_OK})
	 * @throws Exception
	 */
	private void exportInternal(final String requestedExportLanguage, final int expectedHTTPResponseCode, final Lang expectedExportLanguage, final String expectedFileEnding)
			throws Exception {

		RDFResourceTest.LOG.debug("start test export all data to format \"" + requestedExportLanguage + "\"");

		// prepare: load data to mysql and graph db
		// SR hint: the resource's description needs to be "this is a description" since this is hard coded in
		// org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils.uploadResource(File, Resource)
		// should be refactored some day
		loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");
		loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");

		// request the export from BE proxy endpoint
		final Response response = target("/getall").queryParam("format", requestedExportLanguage).request().get(Response.class);

		Assert.assertEquals("expected " + expectedHTTPResponseCode, expectedHTTPResponseCode, response.getStatus());

		// in case we requested an unsupported format, stop processing here since there is no exported data to verify
		if (expectedHTTPResponseCode == HttpStatus.SC_NOT_ACCEPTABLE) {
			return;
		}
		
		// check Content-Disposition header for correct file ending
		ExportTestUtils.checkContentDispositionHeader(response, expectedFileEnding);		

		// read data set from response
		final String body = response.readEntity(String.class);
		Assert.assertNotNull("response body  shouldn't be null", body);

		final InputStream stream = new ByteArrayInputStream(body.getBytes("UTF-8"));
		Assert.assertNotNull("input stream (from body) shouldn't be null", stream);
 
		final Dataset dataset = DatasetFactory.createMem();
		RDFDataMgr.read(dataset, stream, expectedExportLanguage);
		Assert.assertNotNull("dataset from response shouldn't be null", dataset);

		// count number of statements in exportet data set
		final long statementsInExportedRDFModel = RDFUtils.determineDatasetSize(dataset);

		LOG.debug("exported '" + statementsInExportedRDFModel + "' statements");

		// count number of statements that were loaded to db while prepare
		long expectedStatements = countStatementsInSerializedN3("atMostTwoRows.n3") + countStatementsInSerializedN3("UTF-8.n3");

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

		final long statementsInOriginalRDFFile = modelFromOriginalRDFile.size();
		return statementsInOriginalRDFFile;
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

		final DataModel dataModelToCreate = new DataModel();
		dataModelToCreate.setName("my data model");
		dataModelToCreate.setDescription("my data model description");
		dataModelToCreate.setDataResource(resource);
		dataModelToCreate.setConfiguration(config);

		final String dataModelJSONString = objectMapper.writeValueAsString(dataModelToCreate);

		final DataModel dataModelfromDB = dataModelsResourceTestUtils.createObjectWithoutComparison(dataModelJSONString);

		return dataModelfromDB;
	}

}
