package de.avgl.dmp.converter.flow.test;

import static org.junit.Assert.assertEquals;

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
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.avgl.dmp.converter.GuicedTest;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.converter.flow.XMLSourceResourceTriplesFlow;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.internal.rdf.RDFModel;
import de.avgl.dmp.persistence.model.job.Task;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.types.Tuple;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;
import de.avgl.dmp.persistence.service.internal.graph.InternalRDFGraphService;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.resource.ResourceService;
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.service.schema.AttributeService;
import de.avgl.dmp.persistence.service.schema.ClaszService;
import de.avgl.dmp.persistence.service.schema.SchemaService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public abstract class AbstractXMLTransformationFlowTest extends GuicedTest {

	protected final String			taskJSONFileName;

	protected final String			expectedResultJSONFileName;

	protected final String			recordTag;

	protected final String			xmlNamespace;

	protected final String			exampleDataResourceFileName;

	protected final ObjectMapper	objectMapper;

	public AbstractXMLTransformationFlowTest(final String taskJSONFileNameArg, final String expectedResultJSONFileNameArg, final String recordTagArg,
			final String xmlNamespaceArg, final String exampleDataResourceFileNameArg) {

		objectMapper = injector.getInstance(ObjectMapper.class);

		taskJSONFileName = taskJSONFileNameArg;
		expectedResultJSONFileName = expectedResultJSONFileNameArg;
		recordTag = recordTagArg;
		xmlNamespace = xmlNamespaceArg;
		exampleDataResourceFileName = exampleDataResourceFileNameArg;
	}

	@Test
	public void testXMLDataResourceEndToEnd() throws Exception {

		final String taskJSONString = DMPPersistenceUtil.getResourceAsString(taskJSONFileName);
		final String expected = DMPPersistenceUtil.getResourceAsString(expectedResultJSONFileName);

		// process input data model
		final ConfigurationService configurationService = injector.getInstance(ConfigurationService.class);
		final Configuration configuration = configurationService.createObjectTransactional().getObject();

		configuration.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode(recordTag));

		if (xmlNamespace != null) {

			configuration.addParameter(ConfigurationStatics.XML_NAMESPACE, new TextNode(xmlNamespace));
		}

		configuration.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode("xml"));

		Configuration updatedConfiguration = configurationService.updateObjectTransactional(configuration).getObject();

		final ResourceService resourceService = injector.getInstance(ResourceService.class);
		final Resource resource = resourceService.createObjectTransactional().getObject();
		resource.setName(exampleDataResourceFileName);
		resource.setType(ResourceType.FILE);
		resource.addConfiguration(updatedConfiguration);

		Resource updatedResource = resourceService.updateObjectTransactional(resource).getObject();

		final DataModelService dataModelService = injector.getInstance(DataModelService.class);
		final DataModel dataModel = dataModelService.createObjectTransactional().getObject();

		dataModel.setDataResource(updatedResource);
		dataModel.setConfiguration(updatedConfiguration);

		DataModel updatedDataModel = dataModelService.updateObjectTransactional(dataModel).getObject();

		final XMLSourceResourceTriplesFlow flow2 = new XMLSourceResourceTriplesFlow(updatedDataModel);

		final List<RDFModel> rdfModels = flow2.applyResource(exampleDataResourceFileName);

		Assert.assertNotNull("RDF model list shouldn't be null", rdfModels);
		Assert.assertFalse("RDF model list shouldn't be empty", rdfModels.isEmpty());

		// write RDF models at once
		final com.hp.hpl.jena.rdf.model.Model model = ModelFactory.createDefaultModel();
		String recordClassUri = null;

		for (final RDFModel rdfModel : rdfModels) {

			Assert.assertNotNull("the RDF triples of the RDF model shouldn't be null", rdfModel.getModel());

			model.add(rdfModel.getModel());

			if (recordClassUri == null) {

				recordClassUri = rdfModel.getRecordClassURI();
			}
		}

		final RDFModel rdfModel = new RDFModel(model, null, recordClassUri);

		// System.out.println(objectMapper.writeValueAsString(rdfModel.getSchema()));
		//
		// for(final AttributePathHelper attributePathHelper : rdfModel.getAttributePaths()) {
		//
		// System.out.println(attributePathHelper.toString());
		// }
		//
		// System.out.println(objectMapper.writeValueAsString(rdfModel.toJSON()));

		// write model and retrieve tuples
		final InternalRDFGraphService tripleService = injector.getInstance(InternalRDFGraphService.class);
		tripleService.createObject(updatedDataModel.getId(), rdfModel);

		// retrieve updated fresh data model
		final DataModel freshDataModel = dataModelService.getObject(updatedDataModel.getId());

		Assert.assertNotNull("the fresh data model shouldn't be null", freshDataModel);
		Assert.assertNotNull("the schema of the fresh data model shouldn't be null", freshDataModel.getSchema());

		final Schema schema = freshDataModel.getSchema();

		// System.out.println(objectMapper.writeValueAsString(schema));

		Assert.assertNotNull("the record class of the schema of the fresh data model shouldn't be null", schema.getRecordClass());

		final Clasz recordClass = schema.getRecordClass();

		final Optional<Map<String, Model>> optionalModelMap = tripleService.getObjects(updatedDataModel.getId(), Optional.of(1));

		final Iterator<Tuple<String, JsonNode>> tuples = dataIterator(optionalModelMap.get().entrySet().iterator());

		final String dataModelJSONString = objectMapper.writeValueAsString(updatedDataModel);
		final ObjectNode dataModelJSON = objectMapper.readValue(dataModelJSONString, ObjectNode.class);

		// manipulate input data model
		final ObjectNode taskJSON = objectMapper.readValue(taskJSONString, ObjectNode.class);
		((ObjectNode) taskJSON).put("input_data_model", dataModelJSON);
		
		// manipulate output data model (output data model = input data model (for now))
		((ObjectNode) taskJSON).put("output_data_model", dataModelJSON);

		final String finalTaskJSONString = objectMapper.writeValueAsString(taskJSON);

		final Task task = objectMapper.readValue(finalTaskJSONString, Task.class);
		
		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = injector.getProvider(InternalModelServiceFactory.class);
		
		final TransformationFlow flow = TransformationFlow.fromTask(task, internalModelServiceFactoryProvider);
		
		flow.getScript();

		final String actual = flow.apply(tuples);

		compareResults(expected, actual);

		// clean-up
		// TODO: move clean-up to @After

		final Map<Long, Attribute> attributes = Maps.newHashMap();

		final Map<Long, AttributePath> attributePaths = Maps.newLinkedHashMap();

		if (schema != null) {

			final Set<AttributePath> attributePathsToDelete = schema.getAttributePaths();

			if (attributePaths != null) {

				for (final AttributePath attributePath : attributePathsToDelete) {

					attributePaths.put(attributePath.getId(), attributePath);

					final Set<Attribute> attributesToDelete = attributePath.getAttributes();

					if (attributePathsToDelete != null) {

						for (final Attribute attribute : attributesToDelete) {

							attributes.put(attribute.getId(), attribute);
						}
					}
				}
			}
		}

		dataModelService.deleteObject(updatedDataModel.getId());
		final SchemaService schemaService = injector.getInstance(SchemaService.class);

		schemaService.deleteObject(schema.getId());

		final AttributePathService attributePathService = injector.getInstance(AttributePathService.class);

		for (final AttributePath attributePath : attributePaths.values()) {

			attributePathService.deleteObject(attributePath.getId());
		}

		final AttributeService attributeService = injector.getInstance(AttributeService.class);

		for (final Attribute attribute : attributes.values()) {

			attributeService.deleteObject(attribute.getId());
		}

		final ClaszService claszService = injector.getInstance(ClaszService.class);

		claszService.deleteObject(recordClass.getId());

		configurationService.deleteObject(updatedConfiguration.getId());
		resourceService.deleteObject(updatedResource.getId());
	}

	protected void compareResults(final String expectedResultJSONString, final String actualResultJSONString) throws Exception {

		final ArrayNode expectedJSONArray = objectMapper.readValue(expectedResultJSONString, ArrayNode.class);
		final ObjectNode expectedJSON = (ObjectNode) expectedJSONArray.get(0).get("record_data");
		final String finalExpectedJSONString = objectMapper.writeValueAsString(expectedJSON);

		final ArrayNode actualJSONArray = objectMapper.readValue(actualResultJSONString, ArrayNode.class);
		final ObjectNode actualJSON = (ObjectNode) actualJSONArray.get(0).get("record_data");
		final String finalActualJSONString = objectMapper.writeValueAsString(actualJSON);

		assertEquals(finalExpectedJSONString.length(), finalActualJSONString.length());
	}

	private Iterator<Tuple<String, JsonNode>> dataIterator(final Iterator<Map.Entry<String, Model>> triples) {
		return new AbstractIterator<Tuple<String, JsonNode>>() {

			@Override
			protected Tuple<String, JsonNode> computeNext() {
				if (triples.hasNext()) {
					final Map.Entry<String, Model> nextTriple = triples.next();
					final String recordId = nextTriple.getKey();
					final JsonNode jsonNode = nextTriple.getValue().toJSON();
					return Tuple.tuple(recordId, jsonNode);
				}
				return endOfData();
			}
		};
	}
}
