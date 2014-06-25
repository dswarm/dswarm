package org.dswarm.converter.flow.test.xml;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Maps;
import com.google.inject.Provider;

import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.flow.TransformationFlow;
import org.dswarm.converter.flow.XMLSourceResourceGDMStmtsFlow;
import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.types.Tuple;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.service.internal.graph.InternalGDMGraphService;
import org.dswarm.persistence.service.internal.test.utils.InternalGDMGraphServiceTestUtils;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.resource.ResourceService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.service.schema.test.utils.AttributePathServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.AttributeServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.SchemaServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public abstract class AbstractXMLTransformationFlowTest extends GuicedTest {

	protected final String						taskJSONFileName;

	protected final String						expectedResultJSONFileName;

	protected final String						recordTag;

	protected final String						xmlNamespace;

	protected final String						exampleDataResourceFileName;

	protected final ObjectMapper				objectMapper;

	private final AttributeServiceTestUtils		attributeServiceTestUtils;
	private final AttributePathServiceTestUtils	attributePathServiceTestUtils;
	private final SchemaServiceTestUtils		schemaServiceTestUtils;

	public AbstractXMLTransformationFlowTest(final String taskJSONFileNameArg, final String expectedResultJSONFileNameArg, final String recordTagArg,
			final String xmlNamespaceArg, final String exampleDataResourceFileNameArg) {

		objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

		taskJSONFileName = taskJSONFileNameArg;
		expectedResultJSONFileName = expectedResultJSONFileNameArg;
		recordTag = recordTagArg;
		xmlNamespace = xmlNamespaceArg;
		exampleDataResourceFileName = exampleDataResourceFileNameArg;

		attributeServiceTestUtils = new AttributeServiceTestUtils();
		attributePathServiceTestUtils = new AttributePathServiceTestUtils();
		schemaServiceTestUtils = new SchemaServiceTestUtils();
	}

	@Test
	public void testXMLDataResourceEndToEnd() throws Exception {

		final String taskJSONString = DMPPersistenceUtil.getResourceAsString(taskJSONFileName);
		final String expected = DMPPersistenceUtil.getResourceAsString(expectedResultJSONFileName);

		// process input data model
		final ConfigurationService configurationService = GuicedTest.injector.getInstance(ConfigurationService.class);
		final Configuration configuration = configurationService.createObjectTransactional().getObject();

		configuration.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode(recordTag));

		if (xmlNamespace != null) {

			configuration.addParameter(ConfigurationStatics.XML_NAMESPACE, new TextNode(xmlNamespace));
		}

		configuration.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode("xml"));

		final Configuration updatedConfiguration = configurationService.updateObjectTransactional(configuration).getObject();

		final ResourceService resourceService = GuicedTest.injector.getInstance(ResourceService.class);
		final Resource resource = resourceService.createObjectTransactional().getObject();
		resource.setName(exampleDataResourceFileName);
		resource.setType(ResourceType.FILE);
		resource.addConfiguration(updatedConfiguration);

		final Resource updatedResource = resourceService.updateObjectTransactional(resource).getObject();

		final DataModelService dataModelService = GuicedTest.injector.getInstance(DataModelService.class);
		final DataModel inputDataModel = dataModelService.createObjectTransactional().getObject();

		inputDataModel.setDataResource(updatedResource);
		inputDataModel.setConfiguration(updatedConfiguration);

		final DataModel updatedInputDataModel = dataModelService.updateObjectTransactional(inputDataModel).getObject();

		final XMLSourceResourceGDMStmtsFlow flow2 = new XMLSourceResourceGDMStmtsFlow(updatedInputDataModel);

		final List<GDMModel> gdmModels = flow2.applyResource(exampleDataResourceFileName);

		Assert.assertNotNull("GDM model list shouldn't be null", gdmModels);
		Assert.assertFalse("GDM model list shouldn't be empty", gdmModels.isEmpty());

		// write RDF models at once
		final org.dswarm.graph.json.Model model = new org.dswarm.graph.json.Model();
		String recordClassUri = null;

		for (final GDMModel gdmModel : gdmModels) {

			Assert.assertNotNull("the GDM statements of the GDM model shouldn't be null", gdmModel.getModel());

			final org.dswarm.graph.json.Model aModel = gdmModel.getModel();

			Assert.assertNotNull("the resources of the GDM model shouldn't be null", aModel.getResources());

			final Collection<org.dswarm.graph.json.Resource> resources = aModel.getResources();

			for (final org.dswarm.graph.json.Resource aResource : resources) {

				model.addResource(aResource);

				if (recordClassUri == null) {

					recordClassUri = gdmModel.getRecordClassURI();
				}

			}
		}

		final GDMModel gdmModel = new GDMModel(model, null, recordClassUri);

		// System.out.println(objectMapper.writeValueAsString(gdmModel.getSchema()));
		//
		// for(final AttributePathHelper attributePathHelper : gdmModel.getAttributePaths()) {
		//
		// System.out.println(attributePathHelper.toString());
		// }
		//
		// System.out.println(objectMapper.configure(SerializationFeature.INDENT_OUTPUT,
		// true).writeValueAsString(gdmModel.toJSON()));

		// write model and retrieve tuples
		final InternalGDMGraphService gdmService = GuicedTest.injector.getInstance(InternalGDMGraphService.class);
		gdmService.createObject(updatedInputDataModel.getId(), gdmModel);

		final Optional<Map<String, Model>> optionalModelMap = gdmService.getObjects(updatedInputDataModel.getId(), Optional.<Integer> absent());

		Assert.assertTrue("there is no map of entry models in the database", optionalModelMap.isPresent());

		final Map<String, Model> modelMap = optionalModelMap.get();

		Assert.assertNotNull("the model map shouldn't be null", modelMap);

		final Iterator<Tuple<String, JsonNode>> tuples = dataIterator(modelMap.entrySet().iterator());

		// final List<Tuple<String, JsonNode>> tuplesList = Lists.newLinkedList();
		//
		// while(tuples.hasNext()) {
		//
		// tuplesList.add(tuples.next());
		// }
		//
		// final String tuplesJSON = objectMapper.configure(SerializationFeature.INDENT_OUTPUT,
		// true).writeValueAsString(tuplesList);
		//
		// System.out.println(tuplesJSON);

		final String inputDataModelJSONString = objectMapper.writeValueAsString(updatedInputDataModel);
		final ObjectNode inputDataModelJSON = objectMapper.readValue(inputDataModelJSONString, ObjectNode.class);

		// manipulate input data model
		final ObjectNode taskJSON = objectMapper.readValue(taskJSONString, ObjectNode.class);
		taskJSON.put("input_data_model", inputDataModelJSON);

		// manipulate output data model (output data model = internal model (for now))
		final long internalModelId = 1;
		final DataModel outputDataModel = dataModelService.getObject(internalModelId);
		final String outputDataModelJSONString = objectMapper.writeValueAsString(outputDataModel);
		final ObjectNode outputDataModelJSON = objectMapper.readValue(outputDataModelJSONString, ObjectNode.class);
		taskJSON.put("output_data_model", outputDataModelJSON);

		final String finalTaskJSONString = objectMapper.writeValueAsString(taskJSON);

		final Task task = objectMapper.readValue(finalTaskJSONString, Task.class);

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = GuicedTest.injector
				.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromTask(task, internalModelServiceFactoryProvider);

		flow.getScript();

		final String actual = flow.apply(tuples, true);

		compareResults(expected, actual);

		// retrieve updated fresh data model
		final DataModel freshInputDataModel = dataModelService.getObject(updatedInputDataModel.getId());

		Assert.assertNotNull("the fresh data model shouldn't be null", freshInputDataModel);
		Assert.assertNotNull("the schema of the fresh data model shouldn't be null", freshInputDataModel.getSchema());

		final Schema schema = freshInputDataModel.getSchema();

		// System.out.println(objectMapper.writeValueAsString(schema));

		Assert.assertNotNull("the record class of the schema of the fresh data model shouldn't be null", schema.getRecordClass());

		final Clasz recordClass = schema.getRecordClass();

		// clean-up
		// TODO: move clean-up to @After

		final DataModel freshOutputDataModel = dataModelService.getObject(internalModelId);

		final Schema outputDataModelSchema = freshOutputDataModel.getSchema();

		final Map<Long, Attribute> attributes = Maps.newHashMap();

		final Map<Long, AttributePath> attributePaths = Maps.newLinkedHashMap();

		if (schema != null) {

			final Set<AttributePath> attributePathsToDelete = schema.getAttributePaths();

			if (attributePathsToDelete != null) {

				for (final AttributePath attributePath : attributePathsToDelete) {

					attributePaths.put(attributePath.getId(), attributePath);

					final Set<Attribute> attributesToDelete = attributePath.getAttributes();

					if (attributesToDelete != null) {

						for (final Attribute attribute : attributesToDelete) {

							attributes.put(attribute.getId(), attribute);
						}
					}
				}
			}
		}

		final SchemaService schemaService = GuicedTest.injector.getInstance(SchemaService.class);

		schemaServiceTestUtils.removeAddedAttributePathsFromOutputModelSchema(outputDataModelSchema, attributes, attributePaths);

		dataModelService.deleteObject(updatedInputDataModel.getId());

		schemaService.deleteObject(schema.getId());

		for (final AttributePath attributePath : attributePaths.values()) {

			attributePathServiceTestUtils.deleteObject(attributePath);
		}

		for (final Attribute attribute : attributes.values()) {

			attributeServiceTestUtils.deleteObject(attribute);
		}

		final ClaszService claszService = GuicedTest.injector.getInstance(ClaszService.class);

		claszService.deleteObject(recordClass.getId());

		configurationService.deleteObject(updatedConfiguration.getId());
		resourceService.deleteObject(updatedResource.getId());

		// clean-up graph db
		InternalGDMGraphServiceTestUtils.cleanGraphDB();
	}

	protected void compareResults(final String expectedResultJSONString, final String actualResultJSONString) throws Exception {

		final ArrayNode expectedJSONArray = objectMapper.readValue(expectedResultJSONString, ArrayNode.class);
		final ObjectNode expectedElementInArray = (ObjectNode) expectedJSONArray.get(0);
		final String expectedKeyInArray = expectedElementInArray.fieldNames().next();
		final ObjectNode expectedJSON = (ObjectNode) expectedElementInArray.get(expectedKeyInArray).get(0);
		final String finalExpectedJSONString = objectMapper.writeValueAsString(expectedJSON);

		final ArrayNode actualJSONArray = objectMapper.readValue(actualResultJSONString, ArrayNode.class);
		final ObjectNode actualElementInArray = (ObjectNode) actualJSONArray.get(0);
		final String actualKeyInArray = actualElementInArray.fieldNames().next();
		final ArrayNode actualKeyArray = (ArrayNode) actualElementInArray.get(actualKeyInArray);
		ObjectNode actualJSON = null;

		for (final JsonNode actualKeyArrayItem : actualKeyArray) {

			if (actualKeyArrayItem.get("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") != null) {

				// don't take the type JSON object for comparison

				continue;
			}

			actualJSON = (ObjectNode) actualKeyArrayItem;
		}

		final String finalActualJSONString = objectMapper.writeValueAsString(actualJSON);

		Assert.assertEquals(finalExpectedJSONString.length(), finalActualJSONString.length());
	}

	private Iterator<Tuple<String, JsonNode>> dataIterator(final Iterator<Map.Entry<String, Model>> triples) {
		return new AbstractIterator<Tuple<String, JsonNode>>() {

			@Override
			protected Tuple<String, JsonNode> computeNext() {
				if (triples.hasNext()) {
					final Map.Entry<String, Model> nextTriple = triples.next();
					final String recordId = nextTriple.getKey();
					final JsonNode jsonNode = nextTriple.getValue().toRawJSON();
					return Tuple.tuple(recordId, jsonNode);
				}
				return endOfData();
			}
		};
	}
}
