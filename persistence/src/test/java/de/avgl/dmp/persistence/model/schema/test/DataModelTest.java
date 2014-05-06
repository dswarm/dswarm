package de.avgl.dmp.persistence.model.schema.test;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class DataModelTest extends GuicedTest {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(DataModelTest.class);

	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	@Test
	public void simpleDataModelTest() {

		// first attribute path

		final String dctermsTitleId = "http://purl.org/dc/terms/title";
		final String dctermsTitleName = "title";

		final Attribute dctermsTitle = createAttribute(dctermsTitleId, dctermsTitleName);

		final String dctermsHasPartId = "http://purl.org/dc/terms/hasPart";
		final String dctermsHasPartName = "hasPart";

		final Attribute dctermsHasPart = createAttribute(dctermsHasPartId, dctermsHasPartName);

		final AttributePath attributePath1 = new AttributePath();
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

		final AttributePath attributePath2 = new AttributePath();
		// attributePath2.setId(UUID.randomUUID().toString());

		attributePath2.addAttribute(dctermsCreator);
		attributePath2.addAttribute(foafName);

		// third attribute path

		final String dctermsCreatedId = "http://purl.org/dc/terms/created";
		final String dctermsCreatedName = "created";

		final Attribute dctermsCreated = createAttribute(dctermsCreatedId, dctermsCreatedName);

		final AttributePath attributePath3 = new AttributePath();
		// attributePath3.setId(UUID.randomUUID().toString());

		attributePath3.addAttribute(dctermsCreated);

		// record class

		final String biboDocumentId = "http://purl.org/ontology/bibo/Document";
		final String biboDocumentName = "document";

		final Clasz biboDocument = new Clasz(biboDocumentId, biboDocumentName);

		// schema

		final Schema schema = new Schema();
		// schema.setId(UUID.randomUUID().toString());

		schema.addAttributePath(attributePath1);
		schema.addAttributePath(attributePath2);
		schema.addAttributePath(attributePath3);
		schema.setRecordClass(biboDocument);

		// data resource
		final Resource resource = new Resource();

		resource.setName("bla");
		resource.setDescription("blubblub");
		resource.setType(ResourceType.FILE);

		final String attributeKey = "path";
		final String attributeValue = "/path/to/file.end";

		final ObjectNode attributes = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
		attributes.put(attributeKey, attributeValue);

		resource.setAttributes(attributes);

		// configuration
		final Configuration configuration = new Configuration();

		configuration.setName("my configuration");
		configuration.setDescription("configuration description");

		final ObjectNode parameters = new ObjectNode(objectMapper.getNodeFactory());
		final String parameterKey = "fileseparator";
		final String parameterValue = ";";
		parameters.put(parameterKey, parameterValue);

		configuration.setParameters(parameters);

		resource.addConfiguration(configuration);

		// data model
		final DataModel dataModel = new DataModel();
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

	private Attribute createAttribute(final String id, final String name) {

		final Attribute attribute = new Attribute(id);
		attribute.setName(name);

		Assert.assertNotNull("the attribute id shouldn't be null", attribute.getUri());
		Assert.assertEquals("the attribute ids are not equal", id, attribute.getUri());
		Assert.assertNotNull("the attribute name shouldn't be null", attribute.getName());
		Assert.assertEquals("the attribute names are not equal", name, attribute.getName());

		return attribute;
	}

}
