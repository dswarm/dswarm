package de.avgl.dmp.persistence.service.resource.test;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.model.resource.proxy.ProxyDataModel;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.resource.ResourceService;
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.service.schema.AttributeService;
import de.avgl.dmp.persistence.service.schema.ClaszService;
import de.avgl.dmp.persistence.service.schema.SchemaService;
import de.avgl.dmp.persistence.service.test.IDBasicJPAServiceTest;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class DataModelServiceTest extends IDBasicJPAServiceTest<ProxyDataModel, DataModel, DataModelService> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(DataModelServiceTest.class);

	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private Map<Long, Attribute>					attributes		= Maps.newLinkedHashMap();

	private Map<Long, AttributePath>				attributePaths	= Maps.newLinkedHashMap();

	public DataModelServiceTest() {

		super("data model", DataModelService.class);
	}

	@Test
	public void testSimpleDataModel() {

		// first attribute path

		final Attribute dctermsTitle = createAttribute("http://purl.org/dc/terms/title", "title");

		final Attribute dctermsHasPart = createAttribute("http://purl.org/dc/terms/hasPart", "hasPart");

		final LinkedList<Attribute> attributePath1Arg = Lists.newLinkedList();

		attributePath1Arg.add(dctermsTitle);
		attributePath1Arg.add(dctermsHasPart);
		attributePath1Arg.add(dctermsTitle);

		System.out.println("attribute title = '" + dctermsTitle.toString());
		System.out.println("attribute hasPart = '" + dctermsHasPart.toString());

		final AttributePath attributePath1 = createAttributePath(attributePath1Arg);

		// second attribute path

		final String dctermsCreatorId = "http://purl.org/dc/terms/creator";
		final String dctermsCreatorName = "creator";

		final Attribute dctermsCreator = createAttribute(dctermsCreatorId, dctermsCreatorName);

		final String foafNameId = "http://xmlns.com/foaf/0.1/name";
		final String foafNameName = "name";

		final Attribute foafName = createAttribute(foafNameId, foafNameName);

		final LinkedList<Attribute> attributePath2Arg = Lists.newLinkedList();

		attributePath2Arg.add(dctermsCreator);
		attributePath2Arg.add(foafName);

		System.out.println("attribute creator = '" + dctermsCreator.toString());
		System.out.println("attribute name = '" + foafName.toString());

		final AttributePath attributePath2 = createAttributePath(attributePath2Arg);

		// third attribute path

		final String dctermsCreatedId = "http://purl.org/dc/terms/created";
		final String dctermsCreatedName = "created";

		final Attribute dctermsCreated = createAttribute(dctermsCreatedId, dctermsCreatedName);

		final LinkedList<Attribute> attributePath3Arg = Lists.newLinkedList();

		attributePath3Arg.add(dctermsCreated);

		System.out.println("attribute created = '" + dctermsCreated.toString());

		final AttributePath attributePath3 = createAttributePath(attributePath3Arg);

		// record class

		final String biboDocumentId = "http://purl.org/ontology/bibo/Document";
		final String biboDocumentName = "document";

		final Clasz biboDocument = createClass(biboDocumentId, biboDocumentName);

		// schema

		final Set<AttributePath> attributePaths = Sets.newLinkedHashSet();

		attributePaths.add(attributePath1);
		attributePaths.add(attributePath2);
		attributePaths.add(attributePath3);

		final Schema schema = createSchema("my schema", attributePaths, biboDocument);

		// configuration

		final ObjectNode parameters = new ObjectNode(objectMapper.getNodeFactory());
		final String parameterKey = "fileseparator";
		final String parameterValue = ";";
		parameters.put(parameterKey, parameterValue);

		final Configuration configuration = createConfiguration("my configuration", "configuration description", parameters);

		// data resource

		final String attributeKey = "path";
		final String attributeValue = "/path/to/file.end";

		final ObjectNode attributes = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
		attributes.put(attributeKey, attributeValue);

		final Set<Configuration> configurations = Sets.newLinkedHashSet();
		configurations.add(configuration);

		final Resource resource = createResource("bla", "blubblub", ResourceType.FILE, attributes, configurations);

		// data model

		final DataModel dataModel = createObject().getObject();

		final String dataModelName = "my data model";
		final String dataModelDescription = "my data model description";

		dataModel.setName(dataModelName);
		dataModel.setDescription(dataModelDescription);
		dataModel.setDataResource(resource);
		dataModel.setConfiguration(configuration);
		dataModel.setSchema(schema);

		final DataModel updatedDataModel = updateObjectTransactional(dataModel).getObject();

		Assert.assertNotNull("the updated data model shouldn't be null", updatedDataModel);
		Assert.assertNotNull("the update data model id shouldn't be null", updatedDataModel.getId());
		Assert.assertNotNull("the schema of the updated data model shouldn't be null", updatedDataModel.getSchema());
		Assert.assertNotNull("the schema's attribute paths of the updated schema shouldn't be null", updatedDataModel.getSchema().getAttributePaths());
		Assert.assertEquals("the schema's attribute paths size are not equal", schema.getAttributePaths(), updatedDataModel.getSchema()
				.getAttributePaths());
		Assert.assertEquals("the attribute path '" + attributePath1.getId() + "' of the schema are not equal",
				schema.getAttributePath(attributePath1.getId()), updatedDataModel.getSchema().getAttributePath(attributePath1.getId()));
		Assert.assertNotNull("the attribute path's attributes of the attribute path '" + attributePath1.getId()
				+ "' of the updated schema shouldn't be null", updatedDataModel.getSchema().getAttributePath(attributePath1.getId()).getAttributes());
		Assert.assertEquals("the attribute path's attributes size of attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.getAttributes(), updatedDataModel.getSchema().getAttributePath(attributePath1.getId()).getAttributes());
		Assert.assertEquals("the first attributes of attribute path '" + attributePath1.getId() + "' are not equal", attributePath1
				.getAttributePath().get(0), updatedDataModel.getSchema().getAttributePath(attributePath1.getId()).getAttributePath().get(0));
		Assert.assertNotNull("the attribute path string of attribute path '" + attributePath1.getId() + "' of the update schema shouldn't be null",
				updatedDataModel.getSchema().getAttributePath(attributePath1.getId()).toAttributePath());
		Assert.assertEquals("the attribute path's strings attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.toAttributePath(), updatedDataModel.getSchema().getAttributePath(attributePath1.getId()).toAttributePath());
		Assert.assertNotNull("the record class of the updated schema shouldn't be null", updatedDataModel.getSchema().getRecordClass());
		Assert.assertEquals("the recod classes are not equal", schema.getRecordClass(), updatedDataModel.getSchema().getRecordClass());
		Assert.assertNotNull("the resource of the updated data model shouddn't be null", updatedDataModel.getDataResource());

		checkSimpleResource(resource, updatedDataModel.getDataResource(), attributeKey, attributeValue);
		checkComplexResource(resource, updatedDataModel.getDataResource());
		checkComplexResource(resource, updatedDataModel.getDataResource(), parameterKey, parameterValue);

		Assert.assertNotNull("the configuration of the updated data model shouldn't be null", updatedDataModel.getConfiguration());
		Assert.assertNotNull("the configuration name of the updated resource shouldn't be null", updatedDataModel.getConfiguration().getName());
		Assert.assertEquals("the configuration' names of the resource are not equal", configuration.getName(), updatedDataModel.getConfiguration()
				.getName());
		Assert.assertNotNull("the configuration description of the updated resource shouldn't be null", updatedDataModel.getConfiguration()
				.getDescription());
		Assert.assertEquals("the configuration descriptions of the resource are not equal", configuration.getDescription(), updatedDataModel
				.getConfiguration().getDescription());
		Assert.assertNotNull("the configuration parameters of the updated resource shouldn't be null", updatedDataModel.getConfiguration()
				.getParameters());
		Assert.assertEquals("the configurations parameters of the resource are not equal", configuration.getParameters(), updatedDataModel
				.getConfiguration().getParameters());
		Assert.assertNotNull("the parameter value shouldn't be null", configuration.getParameter(parameterKey));
		Assert.assertEquals("the parameter value should be equal", configuration.getParameter(parameterKey).asText(), parameterValue);

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedDataModel);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("data model json: " + json);

		// clean up DB
		deletedObject(dataModel.getId());

		deleteSchema(schema);
		deleteConfiguration(configuration);
		deleteResource(resource);
		deleteClasz(biboDocument);

		for (final AttributePath attributePath : this.attributePaths.values()) {

			deleteAttributePath(attributePath);
		}

		for (final Attribute attribute : this.attributes.values()) {

			deleteAttribute(attribute);
		}
	}

	private AttributePath createAttributePath(final LinkedList<Attribute> attributePathArg) {

		final AttributePathService attributePathService = GuicedTest.injector.getInstance(AttributePathService.class);

		Assert.assertNotNull("attribute path service shouldn't be null", attributePathService);

		final AttributePath attributePath = new AttributePath(attributePathArg);

		AttributePath updatedAttributePath = null;

		try {

			updatedAttributePath = attributePathService.createObject(attributePathArg);
		} catch (final DMPPersistenceException e1) {

			Assert.assertTrue("something went wrong while attribute path creation.\n" + e1.getMessage(), false);
		}

		Assert.assertNotNull("updated attribute path shouldn't be null", updatedAttributePath);
		Assert.assertNotNull("updated attribute path id shouldn't be null", updatedAttributePath.getId());
		Assert.assertNotNull("the attribute path's attribute of the updated attribute path shouldn't be null", updatedAttributePath.getAttributes());
		Assert.assertEquals("the attribute path's attributes size are not equal", attributePath.getAttributes(), updatedAttributePath.getAttributes());
		Assert.assertEquals("the first attributes of the attribute path are not equal", attributePath.getAttributePath().get(0), updatedAttributePath
				.getAttributePath().get(0));
		Assert.assertNotNull("the attribute path string of the updated attribute path shouldn't be null", updatedAttributePath.toAttributePath());
		Assert.assertEquals("the attribute path's strings are not equal", attributePath.toAttributePath(), updatedAttributePath.toAttributePath());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedAttributePath);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("attribute path json for attribute path '" + updatedAttributePath.getId() + "': " + json);

		attributePaths.put(updatedAttributePath.getId(), updatedAttributePath);

		return updatedAttributePath;
	}

	private Attribute createAttribute(final String id, final String name) {

		if (attributes.containsKey(id)) {

			return attributes.get(id);
		}

		final AttributeService attributeService = GuicedTest.injector.getInstance(AttributeService.class);

		Assert.assertNotNull("attribute service shouldn't be null", attributeService);

		// create first attribute

		Attribute attribute = null;

		try {
			attribute = attributeService.createObjectTransactional(id);
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while attribute creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("attribute shouldn't be null", attribute);
		Assert.assertNotNull("attribute id shouldn't be null", attribute.getId());

		attribute.setName(name);

		Attribute updatedAttribute = null;

		try {

			updatedAttribute = attributeService.updateObjectTransactional(attribute).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the attribute of id = '" + id + "'", false);
		}

		Assert.assertNotNull("updated attribute shouldn't be null", updatedAttribute);
		Assert.assertNotNull("updated attribute id shouldn't be null", updatedAttribute.getId());
		Assert.assertNotNull("updated attribute name shouldn't be null", updatedAttribute.getName());

		attributes.put(updatedAttribute.getId(), updatedAttribute);

		return updatedAttribute;
	}

	private Clasz createClass(final String id, final String name) {

		final ClaszService classService = GuicedTest.injector.getInstance(ClaszService.class);

		Assert.assertNotNull("class service shouldn't be null", classService);

		// create class

		Clasz clasz = null;

		try {
			clasz = classService.createObjectTransactional(id);
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while class creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("class shouldn't be null", clasz);
		Assert.assertNotNull("class id shouldn't be null", clasz.getId());

		clasz.setName(name);

		Clasz updatedClasz = null;

		try {

			updatedClasz = classService.updateObjectTransactional(clasz).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the class of id = '" + id + "'", false);
		}

		Assert.assertNotNull("updated class shouldn't be null", updatedClasz);
		Assert.assertNotNull("updated class id shouldn't be null", updatedClasz.getId());
		Assert.assertNotNull("updated class name shouldn't be null", updatedClasz.getName());

		return updatedClasz;
	}

	private Schema createSchema(final String name, final Set<AttributePath> attributePaths, final Clasz recordClass) {

		final SchemaService schemaService = GuicedTest.injector.getInstance(SchemaService.class);

		Assert.assertNotNull("schema service shouldn't be null", schemaService);

		// create schema

		Schema schema = null;

		try {
			schema = schemaService.createObject().getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while schema creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("schema shouldn't be null", schema);
		Assert.assertNotNull("schema id shouldn't be null", schema.getId());

		schema.setName(name);
		schema.setAttributePaths(attributePaths);
		schema.setRecordClass(recordClass);

		// update schema

		Schema updatedSchema = null;

		try {

			updatedSchema = schemaService.updateObjectTransactional(schema).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the schema of id = '" + schema.getId() + "'", false);
		}

		Assert.assertNotNull("updated schema shouldn't be null", updatedSchema);
		Assert.assertNotNull("updated schema id shouldn't be null", updatedSchema.getId());

		final AttributePath attributePath1 = attributePaths.iterator().next();

		Assert.assertNotNull("the schema's attribute paths of the updated schema shouldn't be null", updatedSchema.getAttributePaths());
		Assert.assertEquals("the schema's attribute paths size are not equal", schema.getAttributePaths(), updatedSchema.getAttributePaths());
		Assert.assertEquals("the attribute path '" + attributePath1.getId() + "' of the schema are not equal",
				schema.getAttributePath(attributePath1.getId()), updatedSchema.getAttributePath(attributePath1.getId()));
		Assert.assertNotNull("the attribute path's attributes of the attribute path '" + attributePath1.getId()
				+ "' of the updated schema shouldn't be null", updatedSchema.getAttributePath(attributePath1.getId()).getAttributes());
		Assert.assertEquals("the attribute path's attributes size of attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.getAttributes(), updatedSchema.getAttributePath(attributePath1.getId()).getAttributes());
		Assert.assertEquals("the first attributes of attribute path '" + attributePath1.getId() + "' are not equal", attributePath1
				.getAttributePath().get(0), updatedSchema.getAttributePath(attributePath1.getId()).getAttributePath().get(0));
		Assert.assertNotNull("the attribute path string of attribute path '" + attributePath1.getId() + "' of the update schema shouldn't be null",
				updatedSchema.getAttributePath(attributePath1.getId()).toAttributePath());
		Assert.assertEquals("the attribute path's strings attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.toAttributePath(), updatedSchema.getAttributePath(attributePath1.getId()).toAttributePath());
		Assert.assertNotNull("the record class of the updated schema shouldn't be null", updatedSchema.getRecordClass());
		Assert.assertEquals("the recod classes are not equal", schema.getRecordClass(), updatedSchema.getRecordClass());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedSchema);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("schema json: " + json);

		return updatedSchema;
	}

	private Resource createResource(final String name, final String description, final ResourceType resourceType, final ObjectNode attributes,
			final Set<Configuration> configurations) {

		final ResourceService resourceService = GuicedTest.injector.getInstance(ResourceService.class);

		Assert.assertNotNull("resource service shouldn't be null", resourceService);

		// create resource

		Resource resource = null;

		try {
			resource = resourceService.createObject().getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while resource creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("resource shouldn't be null", resource);
		Assert.assertNotNull("resource id shouldn't be null", resource.getId());

		resource.setName(name);
		resource.setDescription(description);
		resource.setType(resourceType);
		resource.setAttributes(attributes);
		resource.setConfigurations(configurations);

		Resource updatedResource = null;

		try {

			updatedResource = resourceService.updateObjectTransactional(resource).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the resource of id = '" + resource.getId() + "'", false);
		}

		Assert.assertNotNull("updated resource shouldn't be null", updatedResource);
		Assert.assertNotNull("updated resource id shouldn't be null", updatedResource.getId());

		return updatedResource;
	}

	private Configuration createConfiguration(final String name, final String description, final ObjectNode parameters) {

		final ConfigurationService configurationService = GuicedTest.injector.getInstance(ConfigurationService.class);

		Assert.assertNotNull("configuration service shouldn't be null", configurationService);

		// create configuration

		Configuration configuration = null;

		try {
			configuration = configurationService.createObject().getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while configuration creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("configuration shouldn't be null", configuration);
		Assert.assertNotNull("configuration id shouldn't be null", configuration.getId());

		configuration.setName(name);
		configuration.setDescription(description);
		configuration.setParameters(parameters);

		Configuration updatedConfiguration = null;

		try {

			updatedConfiguration = configurationService.updateObjectTransactional(configuration).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the configuration of id = '" + configuration.getId() + "'", false);
		}

		Assert.assertNotNull("updated configuration shouldn't be null", updatedConfiguration);
		Assert.assertNotNull("updated configuration id shouldn't be null", updatedConfiguration.getId());

		return updatedConfiguration;
	}

	private void deleteAttribute(final Attribute attribute) {

		final AttributeService attributeService = GuicedTest.injector.getInstance(AttributeService.class);

		Assert.assertNotNull("attribute service shouldn't be null", attributeService);

		final Long attributeId = attribute.getId();

		attributeService.deleteObject(attributeId);

		final Attribute deletedAttribute = attributeService.getObject(attributeId);

		Assert.assertNull("deleted attribute shouldn't exist any more", deletedAttribute);
	}

	private void deleteAttributePath(final AttributePath attributePath) {

		final AttributePathService attributePathService = GuicedTest.injector.getInstance(AttributePathService.class);

		Assert.assertNotNull("attribute path service shouldn't be null", attributePathService);

		final Long attributePathId = attributePath.getId();

		attributePathService.deleteObject(attributePathId);

		final AttributePath deletedAttributePath = attributePathService.getObject(attributePathId);

		Assert.assertNull("deleted attribute path shouldn't exist any more", deletedAttributePath);
	}

	private void deleteClasz(final Clasz clasz) {

		final ClaszService claszService = GuicedTest.injector.getInstance(ClaszService.class);

		Assert.assertNotNull("class service shouldn't be null", claszService);

		final Long claszId = clasz.getId();

		claszService.deleteObject(claszId);

		final Clasz deletedClass = claszService.getObject(claszId);

		Assert.assertNull("deleted class shouldn't exist any more", deletedClass);
	}

	private void deleteSchema(final Schema schema) {

		final SchemaService schemaService = GuicedTest.injector.getInstance(SchemaService.class);

		Assert.assertNotNull("schema service shouldn't be null", schemaService);

		final Long schemaId = schema.getId();

		schemaService.deleteObject(schemaId);

		final Schema deletedSchema = schemaService.getObject(schemaId);

		Assert.assertNull("deleted schema shouldn't exist any more", deletedSchema);
	}

	private void deleteResource(final Resource resource) {

		final ResourceService resourceService = GuicedTest.injector.getInstance(ResourceService.class);

		Assert.assertNotNull("resource service shouldn't be null", resourceService);

		final Long resourceId = resource.getId();

		resourceService.deleteObject(resourceId);

		final Resource deletedResource = resourceService.getObject(resourceId);

		Assert.assertNull("deleted resource shouldn't exist any more", deletedResource);
	}

	private void deleteConfiguration(final Configuration configuration) {

		final ConfigurationService configurationService = GuicedTest.injector.getInstance(ConfigurationService.class);

		Assert.assertNotNull("configuration service shouldn't be null", configurationService);

		final Long configurationId = configuration.getId();

		configurationService.deleteObject(configurationId);

		final Configuration deletedConfiguration = configurationService.getObject(configurationId);

		Assert.assertNull("deleted configuration shouldn't exist any more", deletedConfiguration);
	}

	private void checkSimpleResource(final Resource resource, final Resource updatedResource, final String attributeKey, final String attributeValue) {

		Assert.assertNotNull("the name of the updated resource shouldn't be null", updatedResource.getName());
		Assert.assertEquals("the names of the resource are not equal", resource.getName(), updatedResource.getName());
		Assert.assertNotNull("the description of the updated resource shouldn't be null", updatedResource.getDescription());
		Assert.assertEquals("the descriptions of the resource are not equal", resource.getDescription(), updatedResource.getDescription());
		Assert.assertNotNull("the type of the updated resource shouldn't be null", updatedResource.getType());
		Assert.assertEquals("the types of the resource are not equal", resource.getType(), updatedResource.getType());
		Assert.assertNotNull("the attributes of the updated resource shouldn't be null", updatedResource.getAttributes());
		Assert.assertEquals("the attributes of the resource are not equal", resource.getAttributes(), updatedResource.getAttributes());
		Assert.assertNotNull("the attribute value shouldn't be null", resource.getAttribute(attributeKey));
		Assert.assertEquals("the attribute value should be equal", resource.getAttribute(attributeKey).asText(), attributeValue);
	}

	private void checkComplexResource(final Resource resource, final Resource updatedResource, final String parameterKey, final String parameterValue) {

		checkComplexResource(resource, updatedResource);

		Assert.assertEquals("the configuration of the resource is not equal", resource.getConfigurations().iterator().next(), resource
				.getConfigurations().iterator().next());
		Assert.assertEquals("the configuration parameter '" + parameterKey + "' of the resource is not equal", resource.getConfigurations()
				.iterator().next().getParameter(parameterKey), resource.getConfigurations().iterator().next().getParameter(parameterKey));
		Assert.assertEquals("the configuration parameter value for '" + parameterKey + "' of the resource is not equal", resource.getConfigurations()
				.iterator().next().getParameter(parameterKey).asText(), resource.getConfigurations().iterator().next().getParameter(parameterKey)
				.asText());
	}

	private void checkComplexResource(final Resource resource, final Resource updatedResource) {

		Assert.assertNotNull("the configurations of the updated resource shouldn't be null", updatedResource.getConfigurations());
		Assert.assertEquals("the configurations of the resource are not equal", resource.getConfigurations(), updatedResource.getConfigurations());
		Assert.assertEquals("the configurations' size of the resource are not equal", resource.getConfigurations().size(), updatedResource
				.getConfigurations().size());
	}
}
