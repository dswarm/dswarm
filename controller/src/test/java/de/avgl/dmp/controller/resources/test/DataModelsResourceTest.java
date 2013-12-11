package de.avgl.dmp.controller.resources.test;

import java.util.Map;

import org.junit.After;
import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

import de.avgl.dmp.controller.resources.test.utils.AttributePathsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.AttributesResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.ClaszesResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.ConfigurationsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.DataModelsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.ResourcesResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.SchemasResourceTestUtils;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class DataModelsResourceTest extends BasicResourceTest<DataModelsResourceTestUtils, DataModelService, DataModel, Long> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(DataModelsResourceTest.class);

	private final AttributesResourceTestUtils		attributesResourceTestUtils;

	private final ClaszesResourceTestUtils			claszesResourceTestUtils;

	private final AttributePathsResourceTestUtils	attributePathsResourceTestUtils;

	private final ResourcesResourceTestUtils		resourcesResourceTestUtils;

	private final ConfigurationsResourceTestUtils	configurationsResourceTestUtils;

	private final SchemasResourceTestUtils			schemasResourceTestUtils;

	final Map<String, Attribute>					attributes		= Maps.newHashMap();

	final Map<Long, AttributePath>					attributePaths	= Maps.newLinkedHashMap();

	private Clasz									recordClass;

	private Schema									schema;

	private Configuration							configuration;

	private Resource								resource;

	public DataModelsResourceTest() {

		super(DataModel.class, DataModelService.class, "datamodels", "datamodel.json", new DataModelsResourceTestUtils());

		attributesResourceTestUtils = new AttributesResourceTestUtils();
		claszesResourceTestUtils = new ClaszesResourceTestUtils();
		attributePathsResourceTestUtils = new AttributePathsResourceTestUtils();
		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
		schemasResourceTestUtils = new SchemasResourceTestUtils();
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

			final String attributeJSONFileName = "attribute" + i + ".json";

			final Attribute actualAttribute = attributesResourceTestUtils.createObject(attributeJSONFileName);

			attributes.put(actualAttribute.getId(), actualAttribute);
		}

		recordClass = claszesResourceTestUtils.createObject("clasz.json");

		// prepare schema json for attribute path ids manipulation
		String schemaJSONString = DMPPersistenceUtil.getResourceAsString("schema.json");
		final ObjectNode schemaJSON = objectMapper.readValue(schemaJSONString, ObjectNode.class);

		for (int j = 1; j < 4; j++) {

			final String attributePathJSONFileName = "attribute_path" + j + ".json";

			final AttributePath actualAttributePath = attributePathsResourceTestUtils.createObject(attributePathJSONFileName);

			attributePaths.put(actualAttributePath.getId(), actualAttributePath);

			// manipulate attribute path ids
			ArrayNode attributePathsArray = (ArrayNode) schemaJSON.get("attribute_paths");

			for (final JsonNode attributePathJsonNode : attributePathsArray) {

				if (((ObjectNode) attributePathJsonNode).get("id").asInt() == j) {

					((ObjectNode) attributePathJsonNode).put("id", actualAttributePath.getId());

					break;
				}
			}
		}

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

	@After
	public void tearDown2() throws Exception {

		// resource clean-up

		resourcesResourceTestUtils.deleteObject(resource);

		// configuration clean-up

		configurationsResourceTestUtils.deleteObject(configuration);

		// START schema clean-up
		
		schemasResourceTestUtils.deleteObject(schema);

		for (final AttributePath attributePath : attributePaths.values()) {

			attributePathsResourceTestUtils.deleteObject(attributePath);
		}

		for (final Attribute attribute : attributes.values()) {

			attributesResourceTestUtils.deleteObject(attribute);
		}

		claszesResourceTestUtils.deleteObject(recordClass);

		// END schema clean-up
	}
}
