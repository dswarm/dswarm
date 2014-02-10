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
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.resource.test.utils.ConfigurationServiceTestUtils;
import de.avgl.dmp.persistence.service.resource.test.utils.ResourceServiceTestUtils;
import de.avgl.dmp.persistence.service.schema.test.utils.AttributePathServiceTestUtils;
import de.avgl.dmp.persistence.service.schema.test.utils.AttributeServiceTestUtils;
import de.avgl.dmp.persistence.service.schema.test.utils.ClaszServiceTestUtils;
import de.avgl.dmp.persistence.service.schema.test.utils.SchemaServiceTestUtils;
import de.avgl.dmp.persistence.service.test.IDBasicJPAServiceTest;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class DataModelServiceTest extends IDBasicJPAServiceTest<ProxyDataModel, DataModel, DataModelService> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(DataModelServiceTest.class);

	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private Map<Long, Attribute>					attributes		= Maps.newLinkedHashMap();

	private Map<Long, AttributePath>				attributePaths	= Maps.newLinkedHashMap();

	private final AttributeServiceTestUtils			attributeServiceTestUtils;
	private final ClaszServiceTestUtils				claszServiceTestUtils;
	private final AttributePathServiceTestUtils		attributePathServiceTestUtils;
	private final SchemaServiceTestUtils			schemaServiceTestUtils;
	private final ConfigurationServiceTestUtils		configurationServiceTestUtils;
	private final ResourceServiceTestUtils			resourceServiceTestUtils;

	public DataModelServiceTest() {

		super("data model", DataModelService.class);

		attributeServiceTestUtils = new AttributeServiceTestUtils();
		attributePathServiceTestUtils = new AttributePathServiceTestUtils();
		claszServiceTestUtils = new ClaszServiceTestUtils();
		schemaServiceTestUtils = new SchemaServiceTestUtils();
		configurationServiceTestUtils = new ConfigurationServiceTestUtils();
		resourceServiceTestUtils = new ResourceServiceTestUtils();
	}

	@Test
	public void testSimpleDataModel() throws Exception {

		// first attribute path

		final Attribute dctermsTitle = attributeServiceTestUtils.createAttribute("http://purl.org/dc/terms/title", "title");
		attributes.put(dctermsTitle.getId(), dctermsTitle);

		final Attribute dctermsHasPart = attributeServiceTestUtils.createAttribute("http://purl.org/dc/terms/hasPart", "hasPart");
		attributes.put(dctermsHasPart.getId(), dctermsHasPart);

		final LinkedList<Attribute> attributePath1Arg = Lists.newLinkedList();

		attributePath1Arg.add(dctermsTitle);
		attributePath1Arg.add(dctermsHasPart);
		attributePath1Arg.add(dctermsTitle);

		System.out.println("attribute title = '" + dctermsTitle.toString());
		System.out.println("attribute hasPart = '" + dctermsHasPart.toString());

		final AttributePath attributePath1 = attributePathServiceTestUtils.createAttributePath(attributePath1Arg);
		attributePaths.put(attributePath1.getId(), attributePath1);

		// second attribute path

		final String dctermsCreatorId = "http://purl.org/dc/terms/creator";
		final String dctermsCreatorName = "creator";

		final Attribute dctermsCreator = attributeServiceTestUtils.createAttribute(dctermsCreatorId, dctermsCreatorName);
		attributes.put(dctermsCreator.getId(), dctermsCreator);

		final String foafNameId = "http://xmlns.com/foaf/0.1/name";
		final String foafNameName = "name";

		final Attribute foafName = attributeServiceTestUtils.createAttribute(foafNameId, foafNameName);
		attributes.put(foafName.getId(), foafName);

		final LinkedList<Attribute> attributePath2Arg = Lists.newLinkedList();

		attributePath2Arg.add(dctermsCreator);
		attributePath2Arg.add(foafName);

		System.out.println("attribute creator = '" + dctermsCreator.toString());
		System.out.println("attribute name = '" + foafName.toString());

		final AttributePath attributePath2 = attributePathServiceTestUtils.createAttributePath(attributePath2Arg);
		attributePaths.put(attributePath2.getId(), attributePath2);

		// third attribute path

		final String dctermsCreatedId = "http://purl.org/dc/terms/created";
		final String dctermsCreatedName = "created";

		final Attribute dctermsCreated = attributeServiceTestUtils.createAttribute(dctermsCreatedId, dctermsCreatedName);
		attributes.put(dctermsCreated.getId(), dctermsCreated);

		final LinkedList<Attribute> attributePath3Arg = Lists.newLinkedList();

		attributePath3Arg.add(dctermsCreated);

		System.out.println("attribute created = '" + dctermsCreated.toString());

		final AttributePath attributePath3 = attributePathServiceTestUtils.createAttributePath(attributePath3Arg);
		attributePaths.put(attributePath3.getId(), attributePath3);

		// record class

		final String biboDocumentId = "http://purl.org/ontology/bibo/Document";
		final String biboDocumentName = "document";

		final Clasz biboDocument = claszServiceTestUtils.createClass(biboDocumentId, biboDocumentName);

		// schema

		final Set<AttributePath> attributePaths = Sets.newLinkedHashSet();

		attributePaths.add(attributePath1);
		attributePaths.add(attributePath2);
		attributePaths.add(attributePath3);

		final Schema schema = schemaServiceTestUtils.createSchema("my schema", attributePaths, biboDocument);

		// configuration

		final ObjectNode parameters = new ObjectNode(objectMapper.getNodeFactory());
		final String parameterKey = "fileseparator";
		final String parameterValue = ";";
		parameters.put(parameterKey, parameterValue);

		final Configuration configuration = configurationServiceTestUtils.createConfiguration("my configuration", "configuration description",
				parameters);

		// data resource

		final String attributeKey = "path";
		final String attributeValue = "/path/to/file.end";

		final ObjectNode attributes = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
		attributes.put(attributeKey, attributeValue);

		final Set<Configuration> configurations = Sets.newLinkedHashSet();
		configurations.add(configuration);

		final Resource resource = resourceServiceTestUtils.createResource("bla", "blubblub", ResourceType.FILE, attributes, configurations);

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
		deleteObject(dataModel.getId());

		schemaServiceTestUtils.deleteObject(schema);
		configurationServiceTestUtils.deleteObject(configuration);
		resourceServiceTestUtils.deleteObject(resource);
		claszServiceTestUtils.deleteObject(biboDocument);

		for (final AttributePath attributePath : this.attributePaths.values()) {

			attributePathServiceTestUtils.deleteObject(attributePath);
		}

		for (final Attribute attribute : this.attributes.values()) {

			attributeServiceTestUtils.deleteObject(attribute);
		}
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

		Assert.assertEquals("the configuration of the resource is not equal", resource.getConfigurations().iterator().next(), updatedResource
				.getConfigurations().iterator().next());
		Assert.assertEquals("the configuration parameter '" + parameterKey + "' of the resource is not equal", resource.getConfigurations()
				.iterator().next().getParameter(parameterKey), updatedResource.getConfigurations().iterator().next().getParameter(parameterKey));
		Assert.assertEquals("the configuration parameter value for '" + parameterKey + "' of the resource is not equal", resource.getConfigurations()
				.iterator().next().getParameter(parameterKey).asText(),
				updatedResource.getConfigurations().iterator().next().getParameter(parameterKey).asText());
	}

	private void checkComplexResource(final Resource resource, final Resource updatedResource) {

		Assert.assertNotNull("the configurations of the updated resource shouldn't be null", updatedResource.getConfigurations());
		Assert.assertEquals("the configurations of the resource are not equal", resource.getConfigurations(), updatedResource.getConfigurations());
		Assert.assertEquals("the configurations' size of the resource are not equal", resource.getConfigurations().size(), updatedResource
				.getConfigurations().size());
	}
}
