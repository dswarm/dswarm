package org.dswarm.controller.resources.resource.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.jena.riot.RDFLanguages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.inject.Key;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.common.MediaTypeUtil;
import org.dswarm.controller.resources.resource.test.utils.ConfigurationsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ExportTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.AttributePathsResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.AttributesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.ClaszesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.SchemasResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.controller.test.GuicedTest;
import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.proxy.ProxyDataModel;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.InternalModelService;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.service.internal.test.utils.InternalGDMGraphServiceTestUtils;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.resource.test.utils.DataModelServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class DataModelsResourceTest extends
		BasicResourceTest<DataModelsResourceTestUtils, DataModelServiceTestUtils, DataModelService, ProxyDataModel, DataModel, Long> {

	private static final Logger				LOG					= LoggerFactory.getLogger(DataModelsResourceTest.class);

	private AttributesResourceTestUtils		attributesResourceTestUtils;

	private ClaszesResourceTestUtils		claszesResourceTestUtils;

	private AttributePathsResourceTestUtils	attributePathsResourceTestUtils;

	private ResourcesResourceTestUtils		resourcesResourceTestUtils;

	private ConfigurationsResourceTestUtils	configurationsResourceTestUtils;

	private SchemasResourceTestUtils		schemasResourceTestUtils;

	private DataModelsResourceTestUtils		dataModelsResourceTestUtils;

	private final Map<Long, Attribute>		attributes			= Maps.newHashMap();

	private final Map<Long, AttributePath>	attributePaths		= Maps.newLinkedHashMap();

	private Clasz							recordClass;

	private Clasz							updateRecordClass	= null;

	private Schema							schema;

	private Configuration					configuration;

	private Configuration					updateConfiguration	= null;

	private Resource						resource;

	private Resource						updateResource		= null;

	public DataModelsResourceTest() {

		super(DataModel.class, DataModelService.class, "datamodels", "datamodel.json", new DataModelsResourceTestUtils());
	}

	@Override
	protected void initObjects() {

		super.initObjects();

		pojoClassResourceTestUtils = new DataModelsResourceTestUtils();
		attributesResourceTestUtils = new AttributesResourceTestUtils();
		claszesResourceTestUtils = new ClaszesResourceTestUtils();
		attributePathsResourceTestUtils = new AttributePathsResourceTestUtils();
		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
		schemasResourceTestUtils = new SchemasResourceTestUtils();
		dataModelsResourceTestUtils = new DataModelsResourceTestUtils();
	}

	private void resetObjectVars() {

		attributes.clear();
		attributePaths.clear();
		recordClass = null;
		updateRecordClass = null;
		schema = null;
		configuration = null;
		updateConfiguration = null;
		resource = null;
		updateResource = null;
	}

	@Override
	public void prepare() throws Exception {

		restartServer();
		initObjects();
		resetObjectVars();

		super.prepare();

		// START configuration preparation

		configuration = configurationsResourceTestUtils.createObject("configuration2.json");

		// END configuration preparation

		// START resource preparation

		// prepare resource json for configuration ids manipulation
		String resourceJSONString = DMPPersistenceUtil.getResourceAsString("resource1.json");
		final ObjectNode resourceJSON = objectMapper.readValue(resourceJSONString, ObjectNode.class);

		final ArrayNode configurationsArray = objectMapper.createArrayNode();

		final String persistedConfigurationJSONString = objectMapper.writeValueAsString(configuration);
		final ObjectNode persistedConfigurationJSON = objectMapper.readValue(persistedConfigurationJSONString, ObjectNode.class);

		configurationsArray.add(persistedConfigurationJSON);

		resourceJSON.put("configurations", configurationsArray);

		// re-init expect resource
		resourceJSONString = objectMapper.writeValueAsString(resourceJSON);
		final Resource expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

		Assert.assertNotNull("expected resource shouldn't be null", expectedResource);

		resource = resourcesResourceTestUtils.createObject(resourceJSONString, expectedResource);

		// END resource preparation

		// START schema preparation

		for (int i = 1; i < 6; i++) {

			if (i == 2 || i == 4) {

				// exclude attributes from internal model schema (because they should already exist)

				continue;
			}

			final String attributeJSONFileName = "attribute" + i + ".json";

			final Attribute actualAttribute = attributesResourceTestUtils.createObject(attributeJSONFileName);

			attributes.put(actualAttribute.getId(), actualAttribute);
		}

		recordClass = claszesResourceTestUtils.createObject("clasz1.json");

		// prepare schema json for attribute path ids manipulation
		String schemaJSONString = DMPPersistenceUtil.getResourceAsString("schema.json");
		final ObjectNode schemaJSON = objectMapper.readValue(schemaJSONString, ObjectNode.class);

		for (int j = 1; j < 4; j++) {

			if (j == 2) {

				// exclude attribute paths from internal model schema (because they should already exist)

				continue;
			}

			final String attributePathJSONFileName = "attribute_path" + j + ".json";

			String attributePathJSONString = DMPPersistenceUtil.getResourceAsString(attributePathJSONFileName);
			final AttributePath attributePath = objectMapper.readValue(attributePathJSONString, AttributePath.class);

			final List<Attribute> attributes = attributePath.getAttributePath();
			final List<Attribute> newAttributes = Lists.newLinkedList();

			for (final Attribute attribute : attributes) {

				for (final Attribute newAttribute : this.attributes.values()) {

					if (attribute.getUri().equals(newAttribute.getUri())) {

						newAttributes.add(newAttribute);

						break;
					}
				}
			}

			attributePath.setAttributePath(newAttributes);

			attributePathJSONString = objectMapper.writeValueAsString(attributePath);
			final AttributePath expectedAttributePath = objectMapper.readValue(attributePathJSONString, AttributePath.class);
			final AttributePath actualAttributePath = attributePathsResourceTestUtils.createObject(attributePathJSONString, expectedAttributePath);

			attributePaths.put(actualAttributePath.getId(), actualAttributePath);
		}

		// manipulate attribute paths (incl. their attributes)
		final ArrayNode attributePathsArray = objectMapper.createArrayNode();

		for (final AttributePath attributePath : attributePaths.values()) {

			final String attributePathJSONString = objectMapper.writeValueAsString(attributePath);
			final ObjectNode attributePathJSON = objectMapper.readValue(attributePathJSONString, ObjectNode.class);

			attributePathsArray.add(attributePathJSON);
		}

		schemaJSON.put("attribute_paths", attributePathsArray);

		// manipulate record class
		final String recordClassJSONString = objectMapper.writeValueAsString(recordClass);
		final ObjectNode recordClassJSON = objectMapper.readValue(recordClassJSONString, ObjectNode.class);

		schemaJSON.put("record_class", recordClassJSON);

		// re-init expect schema
		schemaJSONString = objectMapper.writeValueAsString(schemaJSON);
		final Schema expectedSchema = objectMapper.readValue(schemaJSONString, Schema.class);

		schema = schemasResourceTestUtils.createObject(schemaJSONString, expectedSchema);

		// END schema preparation

		// START data model preparation

		// prepare data model json for resource, configuration and schema manipulation
		final ObjectNode objectJSON = objectMapper.readValue(objectJSONString, ObjectNode.class);

		final String finalResourceJSONString = objectMapper.writeValueAsString(resource);
		final ObjectNode finalResourceJSON = objectMapper.readValue(finalResourceJSONString, ObjectNode.class);

		objectJSON.put("data_resource", finalResourceJSON);

		final String finalConfigurationJSONString = objectMapper.writeValueAsString(resource.getConfigurations().iterator().next());
		final ObjectNode finalConfigurationJSON = objectMapper.readValue(finalConfigurationJSONString, ObjectNode.class);

		objectJSON.put("configuration", finalConfigurationJSON);

		final String finalSchemaJSONString = objectMapper.writeValueAsString(schema);
		final ObjectNode finalSchemaJSON = objectMapper.readValue(finalSchemaJSONString, ObjectNode.class);

		objectJSON.put("schema", finalSchemaJSON);

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(objectJSON);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);

		// END data model preparation
	}

	@Test
	public void testCSVData() throws Exception {

		DataModelsResourceTest.LOG.debug("start get CSV data test");

		final String resourceJSONString = DMPPersistenceUtil.getResourceAsString("resource.json");

		final Resource expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

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

		final DataModel dataModel = pojoClassResourceTestUtils.createObjectWithoutComparison(dataModelJSONString);

		final int atMost = 1;

		final InternalModelServiceFactory serviceFactory = GuicedTest.injector.getInstance(Key.get(InternalModelServiceFactory.class));
		final InternalModelService service = serviceFactory.getInternalGDMGraphService();
		final Optional<Map<String, Model>> data = service.getObjects(dataModel.getId(), Optional.of(atMost));

		Assert.assertTrue(data.isPresent());
		Assert.assertFalse(data.get().isEmpty());
		Assert.assertThat(data.get().size(), CoreMatchers.equalTo(atMost));

		final String recordId = data.get().keySet().iterator().next();

		final Response response = target(String.valueOf(dataModel.getId()), "data").queryParam("atMost", atMost).request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final ObjectNode assoziativeJsonArray = response.readEntity(ObjectNode.class);

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

		// clean up

		final Schema schema = dataModel.getSchema();

		pojoClassResourceTestUtils.deleteObject(dataModel);

		final Set<AttributePath> attributePaths = schema.getUniqueAttributePaths();
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

		resourcesResourceTestUtils.deleteObject(resource);
		configurationsResourceTestUtils.deleteObject(config);

		claszesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(recordClasz);

		DataModelsResourceTest.LOG.debug("end get CSV data test");
	}

	@Test
	public void testXMLData() throws Exception {

		DataModelsResourceTest.LOG.debug("start get XML data test");

		// prepare resource
		final String resourceJSONString = DMPPersistenceUtil.getResourceAsString("test-mabxml-resource.json");

		final Resource expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

		final URL fileURL = Resources.getResource("test-mabxml.xml");
		final File resourceFile = FileUtils.toFile(fileURL);

		final String configurationJSONString = DMPPersistenceUtil.getResourceAsString("xml-configuration.json");

		// add resource and config
		final Resource resource = resourcesResourceTestUtils.uploadResource(resourceFile, expectedResource);

		final Configuration configuration = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);

		final DataModel dataModel1 = new DataModel();
		dataModel1.setName("my data model");
		dataModel1.setDescription("my data model description");
		dataModel1.setDataResource(resource);
		dataModel1.setConfiguration(configuration);

		final String dataModelJSONString = objectMapper.writeValueAsString(dataModel1);

		final DataModel dataModel = pojoClassResourceTestUtils.createObjectWithoutComparison(dataModelJSONString);

		final int atMost = 1;

		final InternalModelServiceFactory serviceFactory = GuicedTest.injector.getInstance(Key.get(InternalModelServiceFactory.class));
		final InternalModelService service = serviceFactory.getInternalGDMGraphService();
		final Optional<Map<String, Model>> data = service.getObjects(dataModel.getId(), Optional.of(atMost));

		Assert.assertTrue(data.isPresent());
		Assert.assertFalse(data.get().isEmpty());
		Assert.assertThat(data.get().size(), CoreMatchers.equalTo(atMost));

		final String recordId = data.get().keySet().iterator().next();

		final Response response = target(String.valueOf(dataModel.getId()), "data").queryParam("atMost", atMost).request()
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

		System.out.println("expected JSON = '" + objectMapper.writeValueAsString(expectedJson) + "'");

		Assert.assertThat(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#status", json),
				CoreMatchers.equalTo(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#status", expectedJson)));
		Assert.assertThat(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#mabVersion", json),
				CoreMatchers.equalTo(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#mabVersion", expectedJson)));
		Assert.assertThat(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#typ", json),
				CoreMatchers.equalTo(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#typ", expectedJson)));
		Assert.assertThat(getValueNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld", json).size(),
				CoreMatchers.equalTo(getValueNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld", expectedJson).size()));

		// clean up

		final Schema schema = dataModel.getSchema();
		final Clasz recordClass = schema.getRecordClass();

		cleanUpDB(dataModel);

		if (schema != null) {

			final Set<AttributePath> attributePaths = schema.getUniqueAttributePaths();

			if (attributePaths != null) {

				for (final AttributePath attributePath : attributePaths) {

					this.attributePaths.put(attributePath.getId(), attributePath);

					final Set<Attribute> attributes = attributePath.getAttributes();

					if (attributes != null) {

						for (final Attribute attribute : attributes) {

							this.attributes.put(attribute.getId(), attribute);
						}
					}
				}
			}
		}

		resourcesResourceTestUtils.deleteObject(resource);
		configurationsResourceTestUtils.deleteObject(configuration);
		schemasResourceTestUtils.deleteObject(schema);
		claszesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(recordClass);

		DataModelsResourceTest.LOG.debug("end get XML data test");
	}

	@Test
	public void testMABXMLData() throws Exception {

		DataModelsResourceTest.LOG.debug("start get MABXML data test");

		// prepare resource
		final String resourceJSONString = DMPPersistenceUtil.getResourceAsString("test-mabxml-resource.json");

		final Resource expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

		final URL fileURL = Resources.getResource("test-mabxml.xml");
		final File resourceFile = FileUtils.toFile(fileURL);

		final String configurationJSONString = DMPPersistenceUtil.getResourceAsString("mabxml-configuration.json");

		// add resource and config
		final Resource resource = resourcesResourceTestUtils.uploadResource(resourceFile, expectedResource);

		final Configuration configuration = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);

		final DataModel dataModel1 = new DataModel();
		dataModel1.setName("my data model");
		dataModel1.setDescription("my data model description");
		dataModel1.setDataResource(resource);
		dataModel1.setConfiguration(configuration);

		final String dataModelJSONString = objectMapper.writeValueAsString(dataModel1);

		final DataModel dataModel = pojoClassResourceTestUtils.createObjectWithoutComparison(dataModelJSONString);

		final int atMost = 1;

		final InternalModelServiceFactory serviceFactory = GuicedTest.injector.getInstance(Key.get(InternalModelServiceFactory.class));
		final InternalModelService service = serviceFactory.getInternalGDMGraphService();
		final Optional<Map<String, Model>> data = service.getObjects(dataModel.getId(), Optional.of(atMost));

		Assert.assertTrue(data.isPresent());
		Assert.assertFalse(data.get().isEmpty());
		Assert.assertThat(data.get().size(), CoreMatchers.equalTo(atMost));

		final String recordId = data.get().keySet().iterator().next();

		final Response response = target(String.valueOf(dataModel.getId()), "data").queryParam("atMost", atMost).request()
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

		System.out.println("expected JSON = '" + objectMapper.writeValueAsString(expectedJson) + "'");

		Assert.assertThat(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#status", json),
				CoreMatchers.equalTo(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#status", expectedJson)));
		Assert.assertThat(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#mabVersion", json),
				CoreMatchers.equalTo(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#mabVersion", expectedJson)));
		Assert.assertThat(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#typ", json),
				CoreMatchers.equalTo(getValue("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#typ", expectedJson)));
		Assert.assertThat(getValueNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld", json).size(),
				CoreMatchers.equalTo(getValueNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld", expectedJson).size()));

		Assert.assertEquals(Long.valueOf(3), dataModel.getSchema().getId());

		// clean up

		final Schema schema = dataModel.getSchema();
		final Clasz recordClass = schema.getRecordClass();

		cleanUpDB(dataModel);

		if (schema != null) {

			final Set<AttributePath> attributePaths = schema.getUniqueAttributePaths();

			if (attributePaths != null) {

				for (final AttributePath attributePath : attributePaths) {

					this.attributePaths.put(attributePath.getId(), attributePath);

					final Set<Attribute> attributes = attributePath.getAttributes();

					if (attributes != null) {

						for (final Attribute attribute : attributes) {

							this.attributes.put(attribute.getId(), attribute);
						}
					}
				}
			}
		}

		resourcesResourceTestUtils.deleteObject(resource);
		configurationsResourceTestUtils.deleteObject(configuration);
		claszesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(recordClass);

		DataModelsResourceTest.LOG.debug("end get MABXML data test");
	}

	@Test
	public void testDataMissing() throws Exception {

		DataModelsResourceTest.LOG.debug("start get data missing test");

		final Response response = target("42", "data").request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertThat("404 Not Found was expected", response.getStatus(), CoreMatchers.equalTo(404));
		Assert.assertThat(response.hasEntity(), CoreMatchers.equalTo(false));

		DataModelsResourceTest.LOG.debug("end get resource configuration data missing test");
	}

	@Test
	public void testExceptionAtXMLData() throws Exception {

		DataModelsResourceTest.LOG.debug("start throw Exception at XML data test");

		// prepare resource
		final String resourceJSONString = DMPPersistenceUtil.getResourceAsString("test-mabxml-resource2.json");

		final Resource expectedResource = objectMapper.readValue(resourceJSONString, Resource.class);

		final URL fileURL = Resources.getResource("test-mabxml2.xml");
		final File resourceFile = FileUtils.toFile(fileURL);

		final String configurationJSONString = DMPPersistenceUtil.getResourceAsString("xml-configuration.json");

		// add resource and config
		final Resource resource = resourcesResourceTestUtils.uploadResource(resourceFile, expectedResource);

		final Configuration configuration = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);

		final DataModel dataModel1 = new DataModel();
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
				"{\"status\":\"nok\",\"status_code\":500,\"error\":\" 1; XML document structures must start and end within the same entity.\"}", body);

		// clean up

		final List<DataModel> dataModels = pojoClassResourceTestUtils.getPersistenceServiceTestUtils().getObjects();

		Assert.assertNotNull(dataModels);

		DataModel dataModel = null;

		for (final DataModel dataModel2 : dataModels) {

			if (dataModel2.getName().equals(dataModelName)) {

				dataModel = dataModel2;

				break;
			}
		}

		Assert.assertNotNull(dataModel);

		final Schema schema = dataModel.getSchema();

		Clasz recordClass = null;

		if (schema != null) {

			recordClass = schema.getRecordClass();
		}

		cleanUpDB(dataModel);

		if (schema != null) {

			final Set<AttributePath> attributePaths = schema.getUniqueAttributePaths();

			if (attributePaths != null) {

				for (final AttributePath attributePath : attributePaths) {

					this.attributePaths.put(attributePath.getId(), attributePath);

					final Set<Attribute> attributes = attributePath.getAttributes();

					if (attributes != null) {

						for (final Attribute attribute : attributes) {

							this.attributes.put(attribute.getId(), attribute);
						}
					}
				}
			}
		}

		resourcesResourceTestUtils.deleteObject(resource);
		configurationsResourceTestUtils.deleteObject(configuration);
		schemasResourceTestUtils.deleteObject(schema);

		if (recordClass != null) {

			claszesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(recordClass);
		}

		DataModelsResourceTest.LOG.debug("end throw Exception at XML data test");
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
		DataModel datamodelUTF8csv = loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		DataModel datamodelAtMostcsv = loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal(MediaTypeUtil.N3, datamodelUTF8csv.getId(), HttpStatus.SC_OK, MediaTypeUtil.N3_TYPE, "UTF-8.n3", ".n3");

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
		DataModel datamodelUTF8csv = loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		DataModel datamodelAtMostcsv = loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal(MediaTypeUtil.RDF_XML, datamodelUTF8csv.getId(), HttpStatus.SC_OK, MediaTypeUtil.RDF_XML_TYPE, "UTF-8.n3", ".rdf");

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
		DataModel datamodelUTF8csv = loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		DataModel datamodelAtMostcsv = loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal(MediaTypeUtil.N_QUADS, datamodelUTF8csv.getId(), HttpStatus.SC_OK, MediaTypeUtil.N_QUADS_TYPE, "UTF-8.n3", ".nq");

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
		DataModel datamodelUTF8csv = loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		DataModel datamodelAtMostcsv = loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal(MediaTypeUtil.TRIG, datamodelUTF8csv.getId(), HttpStatus.SC_OK, MediaTypeUtil.TRIG_TYPE, "UTF-8.n3", ".trig");

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
		DataModel datamodelUTF8csv = loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		DataModel datamodelAtMostcsv = loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal(MediaTypeUtil.TURTLE, datamodelUTF8csv.getId(), HttpStatus.SC_OK, MediaTypeUtil.TURTLE_TYPE, "UTF-8.n3", ".ttl");

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
		DataModel datamodelUTF8csv = loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		DataModel datamodelAtMostcsv = loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal("", datamodelUTF8csv.getId(), HttpStatus.SC_OK, MediaTypeUtil.N_QUADS_TYPE, "UTF-8.n3", ".nq");

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
		DataModel datamodelUTF8csv = loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		DataModel datamodelAtMostcsv = loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal(null, datamodelUTF8csv.getId(), HttpStatus.SC_OK, MediaTypeUtil.N_QUADS_TYPE, "UTF-8.n3", ".nq");

	}

	/**
	 * Test export of a single graph that is not existing in database. a HTTP 404 (not found) response is expected.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExportDataModelFromNotExistingDatamodel() throws Exception {

		// hint: do not load any data

		testExportInternal(MediaTypeUtil.N_QUADS, Long.MAX_VALUE, HttpStatus.SC_NOT_FOUND, null, null, null);

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
		DataModel datamodelUTF8csv = loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		DataModel datamodelAtMostcsv = loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal(MediaType.TEXT_PLAIN, datamodelAtMostcsv.getId(), HttpStatus.SC_NOT_ACCEPTABLE, null, null, null);

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
		DataModel datamodelUTF8csv = loadCSVData("UTF-8Csv_Resource.json", "UTF-8.csv", "UTF-8Csv_Configuration.json");
		DataModel datamodelAtMostcsv = loadCSVData("atMostTwoRowsCsv_Resource.json", "atMostTwoRows.csv", "atMostTwoRowsCsv_Configuration.json");

		testExportInternal("khlav/kalash", datamodelAtMostcsv.getId(), HttpStatus.SC_NOT_ACCEPTABLE, null, null, null);

	}

	/**
	 * prepare: upload data and metadata of two csv files to mysql and graph db <br />
	 * request the export from BE proxy endpoint<br />
	 * assert the number of exported statements is equal to an expected value. the models themselves are not compared because of
	 * UUIDs generated while uploading the data
	 * 
	 * @param requestedExportLanguage the serialization format neo4j should export the data to. (this value is used as accept
	 *            header arg to query neo4j)
	 * @param datamodelID identifier of the datamodel to be exported
	 * @param provenanceURI identifier of the graph to export
	 * @param expectedHTTPResponseCode the expected HTTP status code of the response, e.g. {@link HttpStatus.SC_OK} or
	 *            {@link HttpStatus.SC_NOT_ACCEPTABLE}
	 * @param expectedExportMediaType the language the exported data is expected to be serialized in. hint: language may differ
	 *            from {@code requestedExportLanguage} to test for default values. (ignored if expectedHTTPResponseCode !=
	 *            {@link HttpStatus.SC_OK})
	 * @param expectedModelFile name of file containing a serialized model, this (expected) model is equal to the actual model
	 *            exported by neo4j. (ignored if expectedHTTPResponseCode != {@link HttpStatus.SC_OK})
	 * @param expectedFileEnding the expected file ending to be received from neo4j (ignored if expectedHTTPResponseCode !=
	 *            {@link HttpStatus.SC_OK})
	 * @throws IOException
	 */
	private void testExportInternal(final String requestedExportLanguage, final long datamodelID, final int expectedHTTPResponseCode,
			final MediaType expectedExportMediaType, final String expectedModelFile, final String expectedFileEnding) throws Exception {

		// request export of a data model
		String datamodelId = String.valueOf(datamodelID);
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

		LOG.trace("Response body:\n" + body);

		final InputStream inputStream = new ByteArrayInputStream(body.getBytes("UTF-8"));

		Assert.assertNotNull("input stream (from body) shouldn't be null", inputStream);

		// read actual model from response body
		final Lang expectedExportLanguage = RDFLanguages.contentTypeToLang(expectedExportMediaType.toString());
		final com.hp.hpl.jena.rdf.model.Model actualModel = ModelFactory.createDefaultModel();
		RDFDataMgr.read(actualModel, inputStream, expectedExportLanguage);

		Assert.assertNotNull("actual model shouldn't be null", actualModel);
		LOG.debug("exported '" + actualModel.size() + "' statements");

		// read expected model from file
		final com.hp.hpl.jena.rdf.model.Model expectedModel = RDFDataMgr.loadModel(expectedModelFile);
		Assert.assertNotNull("expected model shouldn't be null", expectedModel);
		
		// compare models
		Assert.assertEquals("models should have same number of statements.", expectedModel.size(), actualModel.size());

		// this check can not be done because of generated UUIDs
		// check if statements are the "same" (isomorphic, i.e. blank nodes may have different IDs)
		// Assert.assertTrue("the RDF from the property graph is not isomorphic to the RDF in the original file ",
		// actualModel.isIsomorphicWith(expectedModel));
		// end check exported data
	}

	@Override
	public void testPUTObject() throws Exception {

		super.testPUTObject();

		resourcesResourceTestUtils.deleteObject(updateResource);
		configurationsResourceTestUtils.deleteObject(updateConfiguration);
	}

	@After
	public void tearDown2() throws Exception {

		// resource clean-up

		resourcesResourceTestUtils.deleteObject(resource);

		// configuration clean-up

		configurationsResourceTestUtils.deleteObject(configuration);

		// START schema clean-up

		schemasResourceTestUtils.deleteObject(schema);

		for (final AttributePath attributePath : attributePaths.values()) {

			attributePathsResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attributePath);
		}

		for (final Attribute attribute : attributes.values()) {

			attributesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attribute);
		}

		claszesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(recordClass);

		claszesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(updateRecordClass);

		// END schema clean-up

		// clean-up graph db
		InternalGDMGraphServiceTestUtils.cleanGraphDB();
	}

	@Override
	public DataModel updateObject(final DataModel persistedDataModel) throws Exception {

		persistedDataModel.setName(persistedDataModel.getName() + " update");

		persistedDataModel.setDescription(persistedDataModel.getDescription() + " update");

		updateConfiguration = configurationsResourceTestUtils.createObject("configuration2.json");
		persistedDataModel.setConfiguration(updateConfiguration);

		updateResource = resourcesResourceTestUtils.createObject("resource2.json");
		persistedDataModel.setDataResource(updateResource);

		updateRecordClass = claszesResourceTestUtils.createObject("clasz2.json");

		final Schema schema = persistedDataModel.getSchema();
		schema.setName(schema.getName() + " update");
		schema.setRecordClass(updateRecordClass);

		persistedDataModel.setSchema(schema);

		final String updateDataModelJSONString = objectMapper.writeValueAsString(persistedDataModel);
		final DataModel expectedDataModel = objectMapper.readValue(updateDataModelJSONString, DataModel.class);
		Assert.assertNotNull("the data model JSON string shouldn't be null", updateDataModelJSONString);

		final DataModel updateDataModel = dataModelsResourceTestUtils.updateObject(updateDataModelJSONString, expectedDataModel);

		Assert.assertNotNull("the data model JSON string shouldn't be null", updateDataModel);
		Assert.assertEquals("data model id shoud be equal", updateDataModel.getId(), persistedDataModel.getId());
		Assert.assertEquals("data model name shoud be equal", updateDataModel.getName(), persistedDataModel.getName());
		Assert.assertEquals("data model description shoud be equal", updateDataModel.getDescription(), persistedDataModel.getDescription());
		Assert.assertEquals("data model schema shoud be equal", updateDataModel.getSchema(), persistedDataModel.getSchema());
		Assert.assertEquals("data model configuration shoud be equal", updateDataModel.getConfiguration(), persistedDataModel.getConfiguration());

		return updateDataModel;
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
