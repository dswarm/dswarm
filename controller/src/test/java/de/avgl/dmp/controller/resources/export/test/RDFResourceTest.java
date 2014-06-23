package de.avgl.dmp.controller.resources.export.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;

import de.avgl.dmp.controller.resources.resource.test.utils.ConfigurationsResourceTestUtils;
import de.avgl.dmp.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import de.avgl.dmp.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.AttributePathsResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.AttributesResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.ClaszesResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.SchemasResourceTestUtils;
import de.avgl.dmp.controller.resources.test.ResourceTest;
import de.avgl.dmp.controller.servlet.DMPInjector;
import de.avgl.dmp.controller.test.GuicedTest;
import de.avgl.dmp.graph.rdf.utils.RDFUtils;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.utils.DataModelUtils;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.InternalModelService;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;
import de.avgl.dmp.persistence.service.internal.test.utils.InternalGDMGraphServiceTestUtils;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * Created by tgaengler on 28/04/14.
 * 
 * @author tgaengler
 */
public class RDFResourceTest extends ResourceTest {

	private static final Logger						LOG						= LoggerFactory.getLogger(RDFResourceTest.class);

	private final AttributesResourceTestUtils		attributesResourceTestUtils;

	private final ClaszesResourceTestUtils			claszesResourceTestUtils;

	private final AttributePathsResourceTestUtils	attributePathsResourceTestUtils;

	private final ResourcesResourceTestUtils		resourcesResourceTestUtils;

	private final ConfigurationsResourceTestUtils	configurationsResourceTestUtils;

	private final SchemasResourceTestUtils			schemasResourceTestUtils;

	private final DataModelsResourceTestUtils		dataModelsResourceTestUtils;

	protected final ObjectMapper					objectMapper			= GuicedTest.injector.getInstance(ObjectMapper.class);

	private static final String						graphResourceIdentifier	= "rdf";

	private final String							graphEndpoint			= GuicedTest.injector.getInstance(Key.get(String.class,
																					Names.named("dmp_graph_endpoint")));

	public RDFResourceTest() {

		super("rdf");

		attributesResourceTestUtils = new AttributesResourceTestUtils();
		claszesResourceTestUtils = new ClaszesResourceTestUtils();
		attributePathsResourceTestUtils = new AttributePathsResourceTestUtils();
		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
		schemasResourceTestUtils = new SchemasResourceTestUtils();
		dataModelsResourceTestUtils = new DataModelsResourceTestUtils();
	}

	@Test
	public void testExportSingleGraphWCTNQuads() throws Exception {

		RDFResourceTest.LOG.debug("start export CSV data as application/n-quads test");

		final DataModel csvDataModel = loadCSVData();

		// GET the request
		final Response response = graphTarget("/getall").request().accept("application/n-quads").get(Response.class);

		Assert.assertEquals("expected 200", 200, response.getStatus());

		final String body = response.readEntity(String.class);

		Assert.assertNotNull("response body (n-quads) shouldn't be null", body);

		// System.out.println("Response body : " + body);

		final InputStream stream = new ByteArrayInputStream(body.getBytes("UTF-8"));

		Assert.assertNotNull("input stream (from body) shouldn't be null", stream);

		final Dataset dataset = DatasetFactory.createMem();
		RDFDataMgr.read(dataset, stream, Lang.NQUADS);

		Assert.assertNotNull("dataset shouldn't be null", dataset);

		final long statementsInExportedRDFModel = RDFUtils.determineDatasetSize(dataset);

		RDFResourceTest.LOG.debug("exported '" + statementsInExportedRDFModel + "' statements");

		Assert.assertEquals("expected 114 exported statements", 114, statementsInExportedRDFModel);

		tearDownCSVData(csvDataModel);

		RDFResourceTest.LOG.debug("end export CSV data as application/n-quads test");
	}

	@Test
	public void testExportSingleGraphWCTOctetStream() throws Exception {

		RDFResourceTest.LOG.debug("start export CSV data as application/octet-stream test");

		final DataModel csvDataModel = loadCSVData();

		// GET the request
		final Response response = graphTarget("/getall").queryParam("format", "application/n-quads").request()
				.accept(MediaType.APPLICATION_OCTET_STREAM).get(Response.class);

		Assert.assertEquals("expected 200", 200, response.getStatus());

		final String body = response.readEntity(String.class);

		Assert.assertNotNull("response body (n-quads) shouldn't be null", body);

		// System.out.println("Response body : " + body);

		final InputStream stream = new ByteArrayInputStream(body.getBytes("UTF-8"));

		Assert.assertNotNull("input stream (from body) shouldn't be null", stream);

		final Dataset dataset = DatasetFactory.createMem();
		RDFDataMgr.read(dataset, stream, Lang.NQUADS);

		Assert.assertNotNull("dataset shouldn't be null", dataset);

		final long statementsInExportedRDFModel = RDFUtils.determineDatasetSize(dataset);

		RDFResourceTest.LOG.debug("exported '" + statementsInExportedRDFModel + "' statements");

		Assert.assertEquals("expected 114 exported statements", 114, statementsInExportedRDFModel);

		tearDownCSVData(csvDataModel);

		RDFResourceTest.LOG.debug("end export CSV data as application/octet-stream test");
	}

	private DataModel loadCSVData() throws Exception {

		RDFResourceTest.LOG.debug("start load CSV data");

		final String resourceJSONString = DMPPersistenceUtil.getResourceAsString("resource.json");

		final Resource expectedResource = GuicedTest.injector.getInstance(ObjectMapper.class).readValue(resourceJSONString, Resource.class);

		final URL fileURL = Resources.getResource("test_csv.csv");
		final File resourceFile = FileUtils.toFile(fileURL);

		final String configurationJSONString = DMPPersistenceUtil.getResourceAsString("controller_configuration.json");

		// add resource and config
		final Resource resource = resourcesResourceTestUtils.uploadResource(resourceFile, expectedResource);

		final Configuration config = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);

		final DataModel dataModel1 = new DataModel();
		dataModel1.setName("my data model");
		dataModel1.setDescription("my data model description");
		dataModel1.setDataResource(resource);
		dataModel1.setConfiguration(config);

		final String dataModelJSONString = objectMapper.writeValueAsString(dataModel1);

		final DataModel dataModel = dataModelsResourceTestUtils.createObject(dataModelJSONString, dataModel1);

		final int atMost = 1;

		final InternalModelServiceFactory serviceFactory = DMPInjector.injector.getInstance(Key.get(InternalModelServiceFactory.class));
		final InternalModelService service = serviceFactory.getInternalGDMGraphService();
		final Optional<Map<String, Model>> data = service.getObjects(dataModel.getId(), Optional.of(atMost));

		Assert.assertTrue(data.isPresent());
		Assert.assertFalse(data.get().isEmpty());
		Assert.assertThat(data.get().size(), CoreMatchers.equalTo(atMost));

		final String recordId = data.get().keySet().iterator().next();

		final String associativeJsonArrayString = dataModelsResourceTestUtils.getData(dataModel.getId(), atMost);

		final ObjectNode associativeJsonArray = objectMapper.readValue(associativeJsonArrayString, ObjectNode.class);

		Assert.assertThat(associativeJsonArray.size(), CoreMatchers.equalTo(atMost));

		final JsonNode json = associativeJsonArray.get(recordId);

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

		RDFResourceTest.LOG.debug("end load CSV data");

		return dataModel;
	}

	private void tearDownCSVData(final DataModel dataModel) {

		// clean up

		final Schema schema = dataModel.getSchema();

		dataModelsResourceTestUtils.deleteObject(dataModel);

		final Set<AttributePath> attributePaths = schema.getAttributePaths();
		final Clasz recordClasz = schema.getRecordClass();

		schemasResourceTestUtils.deleteObject(schema);

		final Set<Attribute> attributes = Sets.newHashSet();

		if (attributePaths != null && !attributePaths.isEmpty()) {

			for (final AttributePath attributePath : attributePaths) {

				attributes.addAll(attributePath.getAttributePath());
			}
		}

		for (final AttributePath attributePath : attributePaths) {

			attributePathsResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attributePath);
		}

		for (final Attribute attribute : attributes) {

			attributesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attribute);
		}

		final Resource resource = dataModel.getDataResource();
		final Configuration config = dataModel.getConfiguration();

		resourcesResourceTestUtils.deleteObject(resource);
		configurationsResourceTestUtils.deleteObject(config);

		claszesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(recordClasz);

		// clean-up graph db
		InternalGDMGraphServiceTestUtils.cleanGraphDB();
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

	private Client graphClient() {

		final ClientBuilder builder = ClientBuilder.newBuilder();

		return builder.register(MultiPartFeature.class).build();
	}

	private WebTarget graphTarget() {

		WebTarget target = graphClient().target(graphEndpoint);

		if (RDFResourceTest.graphResourceIdentifier != null) {

			target = target.path(RDFResourceTest.graphResourceIdentifier);
		}

		return target;
	}

	private WebTarget graphTarget(final String... path) {

		WebTarget target = graphTarget();

		for (final String p : path) {

			target = target.path(p);
		}

		return target;
	}
}
