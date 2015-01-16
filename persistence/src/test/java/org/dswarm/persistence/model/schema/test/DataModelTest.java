/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.model.schema.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class DataModelTest extends GuicedTest {

	private static final Logger LOG = LoggerFactory.getLogger(DataModelTest.class);

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	@Test
	public void simpleDataModelTest() {

		// first attribute path

		final String dctermsTitleId = "http://purl.org/dc/terms/title";
		final String dctermsTitleName = "title";

		final Attribute dctermsTitle = createAttribute(dctermsTitleId, dctermsTitleName);

		final String dctermsHasPartId = "http://purl.org/dc/terms/hasPart";
		final String dctermsHasPartName = "hasPart";

		final Attribute dctermsHasPart = createAttribute(dctermsHasPartId, dctermsHasPartName);

		final String attributePath1UUID = UUIDService.getUUID(AttributePath.class.getSimpleName());

		final AttributePath attributePath1 = new AttributePath(attributePath1UUID);
		// attributePath1.setId(UUID.randomUUID().toString());

		attributePath1.addAttribute(dctermsTitle);
		attributePath1.addAttribute(dctermsHasPart);
		attributePath1.addAttribute(dctermsTitle);

		// second attribute path

		final String dctermsCreatorId = "http://purl.org/dc/terms/creator";
		final String dctermsCreatorName = "creator";

		final Attribute dctermsCreator = createAttribute(dctermsCreatorId, dctermsCreatorName);

		final String foafNameId = "http://xmlns.com/foaf/0.1/name";
		final String foafNameName = "name";

		final Attribute foafName = createAttribute(foafNameId, foafNameName);

		final String attributePath2UUID = UUIDService.getUUID(AttributePath.class.getSimpleName());

		final AttributePath attributePath2 = new AttributePath(attributePath2UUID);
		// attributePath2.setId(UUID.randomUUID().toString());

		attributePath2.addAttribute(dctermsCreator);
		attributePath2.addAttribute(foafName);

		// third attribute path

		final String dctermsCreatedId = "http://purl.org/dc/terms/created";
		final String dctermsCreatedName = "created";

		final Attribute dctermsCreated = createAttribute(dctermsCreatedId, dctermsCreatedName);

		final String attributePath3UUID = UUIDService.getUUID(AttributePath.class.getSimpleName());

		final AttributePath attributePath3 = new AttributePath(attributePath3UUID);
		// attributePath3.setId(UUID.randomUUID().toString());

		attributePath3.addAttribute(dctermsCreated);

		// record class

		final String biboDocumentId = "http://purl.org/ontology/bibo/Document";
		final String biboDocumentName = "document";

		final String uuid = UUIDService.getUUID(Clasz.class.getSimpleName());

		final Clasz biboDocument = new Clasz(uuid, biboDocumentId, biboDocumentName);

		// schema

		final String schemaUUID = UUIDService.getUUID(Schema.class.getSimpleName());

		final Schema schema = new Schema(schemaUUID);
		// schema.setId(UUID.randomUUID().toString());

		schema.addAttributePath(createAttributePathInstance(attributePath1));
		schema.addAttributePath(createAttributePathInstance(attributePath2));
		schema.addAttributePath(createAttributePathInstance(attributePath3));
		schema.setRecordClass(biboDocument);

		final String resourceUUID = UUIDService.getUUID(Resource.class.getSimpleName());

		// data resource
		final Resource resource = new Resource(resourceUUID);

		resource.setName("bla");
		resource.setDescription("blubblub");
		resource.setType(ResourceType.FILE);

		final String attributeKey = "path";
		final String attributeValue = "/path/to/file.end";

		final ObjectNode attributes = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
		attributes.put(attributeKey, attributeValue);

		resource.setAttributes(attributes);

		final String configurationUUID = UUIDService.getUUID(Configuration.class.getSimpleName());

		// configuration
		final Configuration configuration = new Configuration(configurationUUID);

		configuration.setName("my configuration");
		configuration.setDescription("configuration description");

		final ObjectNode parameters = new ObjectNode(objectMapper.getNodeFactory());
		final String parameterKey = "fileseparator";
		final String parameterValue = ";";
		parameters.put(parameterKey, parameterValue);

		configuration.setParameters(parameters);

		resource.addConfiguration(configuration);

		final String dataModelUUID = UUIDService.getUUID(DataModel.class.getSimpleName());

		// data model
		final DataModel dataModel = new DataModel(dataModelUUID);
		dataModel.setName("my data model");
		dataModel.setDescription("my data model description");
		dataModel.setDataResource(resource);
		dataModel.setConfiguration(configuration);
		dataModel.setSchema(schema);

		String json = null;

		try {

			json = objectMapper.writeValueAsString(dataModel);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		DataModelTest.LOG.debug("data model json: " + json);
	}

	private Attribute createAttribute(final String uri, final String name) {

		final String uuid = UUIDService.getUUID(Attribute.class.getSimpleName());

		final Attribute attribute = new Attribute(uuid, uri);
		attribute.setName(name);

		Assert.assertNotNull("the attribute uri shouldn't be null", attribute.getUri());
		Assert.assertEquals("the attribute uris are not equal", uri, attribute.getUri());
		Assert.assertNotNull("the attribute name shouldn't be null", attribute.getName());
		Assert.assertEquals("the attribute names are not equal", name, attribute.getName());

		return attribute;
	}

	private static SchemaAttributePathInstance createAttributePathInstance(final AttributePath attributePath) {

		final String attributePathInstanceUUID = UUIDService.getUUID(SchemaAttributePathInstance.class.getSimpleName());

		final SchemaAttributePathInstance attributePathInstance = new SchemaAttributePathInstance(attributePathInstanceUUID);
		attributePathInstance.setAttributePath(attributePath);

		Assert.assertNotNull("the attribute path should not be null", attributePathInstance.getAttributePath());

		return attributePathInstance;
	}
}
