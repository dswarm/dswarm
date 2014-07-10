package org.dswarm.controller.resources.schema.test;

import java.util.LinkedList;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.dswarm.controller.resources.schema.test.utils.AttributePathsResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.AttributesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.ContentSchemasResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.proxy.ProxyContentSchema;
import org.dswarm.persistence.service.schema.ContentSchemaService;
import org.dswarm.persistence.service.schema.test.utils.ContentSchemaServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.junit.After;
import org.junit.Assert;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ContentSchemasResourceTest
		extends
		BasicResourceTest<ContentSchemasResourceTestUtils, ContentSchemaServiceTestUtils, ContentSchemaService, ProxyContentSchema, ContentSchema, Long> {

	private final AttributesResourceTestUtils		attributesResourceTestUtils;

	private final AttributePathsResourceTestUtils	attributePathsResourceTestUtils;

	private final ContentSchemasResourceTestUtils	contentSchemasResourceTestUtils;

	final Map<Long, Attribute>						attributes		= Maps.newHashMap();

	final Map<Long, AttributePath>					attributePaths	= Maps.newLinkedHashMap();

	public ContentSchemasResourceTest() {

		super(ContentSchema.class, ContentSchemaService.class, "contentschemas", "content_schema.json", new ContentSchemasResourceTestUtils());

		attributesResourceTestUtils = new AttributesResourceTestUtils();
		attributePathsResourceTestUtils = new AttributePathsResourceTestUtils();
		contentSchemasResourceTestUtils = new ContentSchemasResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		// note: due to the exclusion of various attribute and attribute path (that already exist in the database) - the resulted
		// content schema doesn't fully reflect the content schema as it is present in the schema.json example

		super.prepare();

		for (int i = 1; i < 6; i++) {

			if (i == 2 || i == 4) {

				// exclude attributes from internal model schema (because they should already exist)

				continue;
			}

			final String attributeJSONFileName = "attribute" + i + ".json";

			prepareAttribute(attributeJSONFileName);
		}

		// prepare content schema json for key attribute path ids manipulation
		final ObjectNode objectJSON = objectMapper.readValue(objectJSONString, ObjectNode.class);

		for (int j = 1; j < 4; j++) {

			if (j == 2) {

				// exclude attribute paths from internal model schema (because they should already exist)

				continue;
			}

			final String attributePathJSONFileName = "attribute_path" + j + ".json";

			prepareAttributePath(attributePathJSONFileName);
		}

		// manipulate key attribute paths (incl. their attributes)
		final ArrayNode attributePathsArray = objectMapper.createArrayNode();

		for (final AttributePath attributePath : attributePaths.values()) {

			final String attributePathJSONString = objectMapper.writeValueAsString(attributePath);
			final ObjectNode attributePathJSON = objectMapper.readValue(attributePathJSONString, ObjectNode.class);

			attributePathsArray.add(attributePathJSON);
		}

		objectJSON.put("key_attribute_paths", attributePathsArray);

		prepareAttribute("attribute8.json");
		AttributePath valueAttributePath = prepareAttributePath("attribute_path8.json");

		// manipulate value attribute path
		final String valueAttributePathJSONString = objectMapper.writeValueAsString(valueAttributePath);
		final ObjectNode valueAttributePathJSON = objectMapper.readValue(valueAttributePathJSONString, ObjectNode.class);

		objectJSON.put("value_attribute_path", valueAttributePathJSON);

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(objectJSON);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);
	}

	@After
	public void tearDown2() throws Exception {

		for (final AttributePath attributePath : attributePaths.values()) {

			attributePathsResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attributePath);
		}

		for (final Attribute attribute : attributes.values()) {

			attributesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attribute);
		}
	}

	@Override
	protected ContentSchema updateObject(final ContentSchema persistedContentSchema) throws Exception {

		final LinkedList<AttributePath> persistedKeyAttributePaths = persistedContentSchema.getKeyAttributePaths();
		final AttributePath firstAttributePath = persistedKeyAttributePaths.iterator().next();

		final String attributeJSONString = DMPPersistenceUtil.getResourceAsString("attribute3.json");
		final Attribute expectedAttribute = objectMapper.readValue(attributeJSONString, Attribute.class);

		final Response response = attributesResourceTestUtils.executeCreateObject(attributeJSONString);

		// attribute4 already exists in the DB at this moment, hence 200 (instead of 201) will be returned
		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final Attribute attribute = objectMapper.readValue(responseString, Attribute.class);

		attributesResourceTestUtils.compareObjects(expectedAttribute, attribute);

		firstAttributePath.addAttribute(attribute);

		// value attribute path update (with a non-persistent attribute)
		final String blaPropertyId = "http://purl.org/ontology/bibo/blaproperty";
		final String blaPropertyName = "blaproperty";
		prepareAttribute("attribute9.json");

		final AttributePath blaPropertyAP = prepareAttributePath("attribute_path9.json");
		persistedContentSchema.setValueAttributePath(blaPropertyAP);

		String updateContentSchemaJSONString = objectMapper.writeValueAsString(persistedContentSchema);
		final ObjectNode updateSchemaJSON = objectMapper.readValue(updateContentSchemaJSONString, ObjectNode.class);

		// schema name update
		final String updateSchemaNameString = persistedContentSchema.getName() + " update";
		updateSchemaJSON.put("name", updateSchemaNameString);

		updateContentSchemaJSONString = objectMapper.writeValueAsString(updateSchemaJSON);

		final ContentSchema expectedContentSchema = objectMapper.readValue(updateContentSchemaJSONString, ContentSchema.class);

		Assert.assertNotNull("the content schema JSON string shouldn't be null", updateContentSchemaJSONString);

		final ContentSchema updateContentSchema = contentSchemasResourceTestUtils.updateObject(updateContentSchemaJSONString, expectedContentSchema);

		final AttributePath updatedValueAttributePath = updateContentSchema.getValueAttributePath();

		Assert.assertEquals("persisted and updated value attribute path string should be equal", updatedValueAttributePath.toAttributePath(),
				blaPropertyId);
		Assert.assertEquals("persisted and updated value attribute path attribute name should be equal", updatedValueAttributePath.getAttributes()
				.iterator().next().getName(), blaPropertyName);
		Assert.assertEquals("persisted and updated content schema name should be equal", updateContentSchema.getName(), updateSchemaNameString);

		return updateContentSchema;
	}

	private void prepareAttribute(final String attributeJSONFileName) throws Exception {

		final Attribute actualAttribute = attributesResourceTestUtils.createObject(attributeJSONFileName);

		attributes.put(actualAttribute.getId(), actualAttribute);
	}

	private AttributePath prepareAttributePath(final String attributePathJSONFileName) throws Exception {

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

		return actualAttributePath;
	}
}
