package de.avgl.dmp.controller.resources.resource.test;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

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

import de.avgl.dmp.controller.resources.resource.test.utils.ConfigurationsResourceTestUtils;
import de.avgl.dmp.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import de.avgl.dmp.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.AttributePathsResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.AttributesResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.ClaszesResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.utils.SchemasResourceTestUtils;
import de.avgl.dmp.controller.resources.test.BasicResourceTest;
import de.avgl.dmp.controller.servlet.DMPInjector;
import de.avgl.dmp.controller.test.GuicedTest;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.proxy.ProxyDataModel;
import de.avgl.dmp.persistence.model.resource.utils.DataModelUtils;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.InternalModelService;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;
import de.avgl.dmp.persistence.service.internal.test.utils.InternalGDMGraphServiceTestUtils;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.resource.test.utils.DataModelServiceTestUtils;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataModelsResourceTest extends
		BasicResourceTest<DataModelsResourceTestUtils, DataModelServiceTestUtils, DataModelService, ProxyDataModel, DataModel, Long> {

	private static final Logger LOG = LoggerFactory.getLogger(DataModelsResourceTest.class);

	private final AttributesResourceTestUtils attributesResourceTestUtils;

	private final ClaszesResourceTestUtils claszesResourceTestUtils;

	private final AttributePathsResourceTestUtils attributePathsResourceTestUtils;

	private final ResourcesResourceTestUtils resourcesResourceTestUtils;

	private final ConfigurationsResourceTestUtils configurationsResourceTestUtils;

	private final SchemasResourceTestUtils schemasResourceTestUtils;

	private final DataModelsResourceTestUtils dataModelsResourceTestUtils;

	private final Map<Long, Attribute> attributes = Maps.newHashMap();

	private final Map<Long, AttributePath> attributePaths = Maps.newLinkedHashMap();

	private Clasz recordClass;

	private Clasz updateRecordClass = null;

	private Schema schema;

	private Configuration configuration;

	private Configuration updateConfiguration = null;

	private Resource resource;

	private Resource updateResource = null;

	public DataModelsResourceTest() {

		super(DataModel.class, DataModelService.class, "datamodels", "datamodel.json", new DataModelsResourceTestUtils());

		attributesResourceTestUtils = new AttributesResourceTestUtils();
		claszesResourceTestUtils = new ClaszesResourceTestUtils();
		attributePathsResourceTestUtils = new AttributePathsResourceTestUtils();
		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
		schemasResourceTestUtils = new SchemasResourceTestUtils();
		dataModelsResourceTestUtils = new DataModelsResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

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

			final LinkedList<Attribute> attributes = attributePath.getAttributePath();
			final LinkedList<Attribute> newAttributes = Lists.newLinkedList();

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

		final DataModel dataModel = pojoClassResourceTestUtils.createObject(dataModelJSONString, dataModel1);

		final int atMost = 1;

		final InternalModelServiceFactory serviceFactory = DMPInjector.injector.getInstance(Key.get(InternalModelServiceFactory.class));
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

		final DataModel dataModel = pojoClassResourceTestUtils.createObject(dataModelJSONString, dataModel1);

		final int atMost = 1;

		final InternalModelServiceFactory serviceFactory = DMPInjector.injector.getInstance(Key.get(InternalModelServiceFactory.class));
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

			final Set<AttributePath> attributePaths = schema.getAttributePaths();

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
	public void testDMPPersistenceExceptionAtXMLData() throws Exception {

		DataModelsResourceTest.LOG.debug("start throw DMPPersistenceException at XML data test");

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
		dataModel1.setName("my data model");
		dataModel1.setDescription("my data model description");
		dataModel1.setDataResource(resource);
		dataModel1.setConfiguration(configuration);

		final String dataModelJSONString = objectMapper.writeValueAsString(dataModel1);

		final DataModel dataModel = pojoClassResourceTestUtils.createObject(dataModelJSONString, dataModel1);

		final int atMost = 1;

		final InternalModelServiceFactory serviceFactory = DMPInjector.injector.getInstance(Key.get(InternalModelServiceFactory.class));
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

			final Set<AttributePath> attributePaths = schema.getAttributePaths();

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

		DataModelsResourceTest.LOG.debug("end throw DMPPersistenceException at XML data test");
	}

	@Test
	public void testDataMissing() throws Exception {

		DataModelsResourceTest.LOG.debug("start get data missing test");

		final Response response = target("42", "data").request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertThat("404 Not Found was expected", response.getStatus(), CoreMatchers.equalTo(404));
		Assert.assertThat(response.hasEntity(), CoreMatchers.equalTo(false));

		DataModelsResourceTest.LOG.debug("end get resource configuration data missing test");
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
}
