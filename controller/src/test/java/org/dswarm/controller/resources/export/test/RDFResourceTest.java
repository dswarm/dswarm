package org.dswarm.controller.resources.export.test;

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
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.common.MediaTypeUtil;
import org.dswarm.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
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

		exportInternal(MediaTypeUtil.N_QUADS, Lang.NQUADS, 200);
	}

	@Test
	public void testExportAllTriG() throws Exception {

		exportInternal(MediaTypeUtil.TRIG, Lang.TRIG, 200);
	}

	@Ignore("requires BE endpoint to forward HTTP responses from GE")
	@Test
	public void testExportUnsupportedFormat() throws Exception {

		exportInternal(MediaTypeUtil.RDF_XML, Lang.TRIG, 406);
	}

	/**
	 * Request the export of all data from BE proxy endpoint in the serialization format. <br />
	 * Count the number of statements in all serialized model file. (These are expected to be exported by db and hence should have
	 * been imported in db in a prepare step). <br />
	 * Assert the numbers of statements in the export is equal to the sum of statements of all serialized models.
	 * 
	 * @param requestFormatParam the requested serialization format to be used in export. may be empty to test the default
	 *            fallback of the endpoint.
	 * @param expectedResponseFormat the serialization format we expect in a response to the requested format
	 * @throws Exception
	 */
	private void exportInternal(final String requestFormatParam, final Lang expectedResponseFormat, final int expectedHTTPResponseCode)
			throws Exception {

		RDFResourceTest.LOG.debug("start test export all data to format \"" + requestFormatParam + "\"");

		// prepare: load data to mysql and graph db

		// SR hint: the resource's description needs to be "this is a description" since this is hard coded in
		// org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils.uploadResource(File, Resource)
		// should be refactored some day
		loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");
		loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");

		// request the export from BE proxy endpoint
		final Response response = target("/getall").queryParam("format", requestFormatParam).request().get(Response.class);

		Assert.assertEquals("expected " + expectedHTTPResponseCode, expectedHTTPResponseCode, response.getStatus());

		// in case we requested an unsupported format, stop processing here since there is no exported data to verify
		if (expectedHTTPResponseCode == 406) {
			return;
		}

		// read response
		final Object responseEntity = response.getEntity();
		Assert.assertNotNull("response body shouldn't be null", responseEntity);

		// SR TODO: is it okay to do the check like this?
		if (!(responseEntity instanceof InputStream)) {

			Assert.assertTrue("Can not read response body", false);
		}

		final InputStream stream = (InputStream) responseEntity;

		Assert.assertNotNull("input stream (from body) shouldn't be null", stream);

		final Dataset dataset = DatasetFactory.createMem();
		RDFDataMgr.read(dataset, stream, expectedResponseFormat);

		Assert.assertNotNull("dataset from response shouldn't be null", dataset);

		final long statementsInExportedRDFModel = RDFUtils.determineDatasetSize(dataset);

		LOG.info("exported '" + statementsInExportedRDFModel + "' statements");

		// count number of statements that were loaded to db while prepare
		long expectedStatements = countStatementsInSerializedN3("atMostTwoRows.n3") + countStatementsInSerializedN3("UTF-8.n3");

		// compare number of exported statements with expected
		Assert.assertEquals("the number of exported statements should be " + expectedStatements, expectedStatements, statementsInExportedRDFModel);

		RDFResourceTest.LOG.debug("end test export all data as " + expectedResponseFormat);
		RDFResourceTest.LOG.info("end test export all data as " + expectedResponseFormat);
	}

	// SR TODO refactor to dswarm-common
	/**
	 * Read a file, serialized in N3, and count the number of statements.
	 * 
	 * @param resourceName the /path/to/file.end of a serialized N3
	 * @return the number of statements the serialized model contains
	 * @throws IOException
	 */
	public long countStatementsInSerializedN3(final String resourceName) throws IOException {
		final URL fileURL = Resources.getResource(resourceName);

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
