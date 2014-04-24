package de.avgl.dmp.converter.flow.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.culturegraph.mf.stream.converter.JsonEncoder;
import org.culturegraph.mf.stream.sink.ObjectJavaIoWriter;
import org.culturegraph.mf.stream.source.FileOpener;
import org.culturegraph.mf.types.Triple;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.google.inject.Provider;

import de.avgl.dmp.converter.GuicedTest;
import de.avgl.dmp.converter.flow.CSVSourceResourceTriplesFlow;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.converter.mf.stream.reader.CsvReader;
import de.avgl.dmp.graph.json.LiteralNode;
import de.avgl.dmp.graph.json.Predicate;
import de.avgl.dmp.graph.json.ResourceNode;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.internal.gdm.GDMModel;
import de.avgl.dmp.persistence.model.job.Task;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;
import de.avgl.dmp.persistence.model.resource.utils.DataModelUtils;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.types.Tuple;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;
import de.avgl.dmp.persistence.service.internal.graph.InternalGDMGraphService;
import de.avgl.dmp.persistence.service.internal.test.utils.InternalGDMGraphServiceTestUtils;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.resource.ResourceService;
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.service.schema.AttributeService;
import de.avgl.dmp.persistence.service.schema.ClaszService;
import de.avgl.dmp.persistence.service.schema.SchemaService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class TransformationFlowTest extends GuicedTest {

	@Ignore
	@Test
	public void testEndToEndDemo() throws Exception {

		final String request = DMPPersistenceUtil.getResourceAsString("complex-request.json");
		final String expected = DMPPersistenceUtil.getResourceAsString("complex-result.json");

		// TODO:

		// final Job job = injector.getInstance(JsonToPojoMapper.class).toJob(request);
		// final TransformationFlow flow = TransformationFlow.fromJob(job);

		final String actual = null;
		// flow.applyDemo();

		assertEquals(expected, actual);
	}

	@Test
	public void testCSVDataResourceEndToEnd() throws Exception {

		final ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);

		final String taskJSONString = DMPPersistenceUtil.getResourceAsString("converter_task.csv.json");
		final String expected = DMPPersistenceUtil.getResourceAsString("task-result.csv.json");

		// process input data model
		final ConfigurationService configurationService = injector.getInstance(ConfigurationService.class);
		final Configuration configuration = configurationService.createObjectTransactional().getObject();

		configuration.setName("config1");
		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		configuration.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode("csv"));

		Configuration updatedConfiguration = configurationService.updateObjectTransactional(configuration).getObject();

		final ResourceService resourceService = injector.getInstance(ResourceService.class);
		final Resource resource = resourceService.createObjectTransactional().getObject();
		resource.setName("test_csv.csv");
		resource.setType(ResourceType.FILE);
		resource.addConfiguration(updatedConfiguration);

		final URL fileURL = Resources.getResource("test_csv.csv");
		final File resourceFile = FileUtils.toFile(fileURL);

		resource.addAttribute("path", resourceFile.getAbsolutePath());

		Resource updatedResource = resourceService.updateObjectTransactional(resource).getObject();

		final DataModelService dataModelService = injector.getInstance(DataModelService.class);
		final DataModel inputDataModel = dataModelService.createObjectTransactional().getObject();

		inputDataModel.setDataResource(updatedResource);
		inputDataModel.setConfiguration(updatedConfiguration);

		DataModel updatedInputDataModel = dataModelService.updateObjectTransactional(inputDataModel).getObject();

		final CSVSourceResourceTriplesFlow flow2 = new CSVSourceResourceTriplesFlow(updatedInputDataModel);

		final List<Triple> csvRecordTriples = flow2.applyResource("test_csv.csv");

		Assert.assertNotNull("CSV record triple list shouldn't be null", csvRecordTriples);
		Assert.assertFalse("CSV record triple list shouldn't be empty", csvRecordTriples.isEmpty());

		final InternalGDMGraphService gdmService = injector.getInstance(InternalGDMGraphService.class);

		// write CSV record triples
		// for (final Triple triple : csvRecordTriples) {
		//
		// final MemoryDBInputModel mdbim = new MemoryDBInputModel(triple);
		//
		// memoryDbService.createObject(updatedInputDataModel.getId(), mdbim);
		// }

		// convert result to GDM
		final Map<Long, de.avgl.dmp.graph.json.Resource> recordResources = Maps.newHashMap();

		final de.avgl.dmp.graph.json.Model model = new de.avgl.dmp.graph.json.Model();

		final String dataResourceBaseSchemaURI = DataModelUtils.determineDataModelSchemaBaseURI(inputDataModel);
		final String recordClassURI = dataResourceBaseSchemaURI + "RecordType";
		final ResourceNode recordClasz = new ResourceNode(recordClassURI);

		for (final org.culturegraph.mf.types.Triple triple : csvRecordTriples) {

			final de.avgl.dmp.graph.json.Resource recordResource = DataModelUtils.mintRecordResource(Long.valueOf(triple.getSubject()), inputDataModel,
					recordResources, model, recordClasz);
			final Predicate property = new Predicate(triple.getPredicate());

			final ResourceNode subject = (ResourceNode) recordResource.getStatements().iterator().next().getSubject();

			recordResource.addStatement(subject, property, new LiteralNode(triple.getObject()));
		}

		final GDMModel gdmModel = new GDMModel(model, null, recordClassURI);

		gdmService.createObject(inputDataModel.getId(), gdmModel);
		// finished writing CSV statements to graph

		// retrieve updated fresh data model
		final DataModel freshInputDataModel = dataModelService.getObject(updatedInputDataModel.getId());

		Assert.assertNotNull("the fresh data model shouldn't be null", freshInputDataModel);
		Assert.assertNotNull("the schema of the fresh data model shouldn't be null", freshInputDataModel.getSchema());

		final Schema schema = freshInputDataModel.getSchema();

		final Optional<Map<String, Model>> optionalModelMap = gdmService.getObjects(updatedInputDataModel.getId(), Optional.<Integer>absent());

		Assert.assertNotNull("CSV record model map optional shouldn't be null", optionalModelMap);
		Assert.assertTrue("CSV record model map should be present", optionalModelMap.isPresent());
		Assert.assertFalse("CSV record model map shouldn't be empty", optionalModelMap.get().isEmpty());

		final Iterator<Tuple<String, JsonNode>> tuples = dataIterator(optionalModelMap.get().entrySet().iterator());

		Assert.assertNotNull("CSV record tuples iterator shouldn't be null", tuples);

		final String inputDataModelJSONString = objectMapper.writeValueAsString(updatedInputDataModel);
		final ObjectNode inputDataModelJSON = objectMapper.readValue(inputDataModelJSONString, ObjectNode.class);

		// manipulate input data model
		final ObjectNode taskJSON = objectMapper.readValue(taskJSONString, ObjectNode.class);
		((ObjectNode) taskJSON).put("input_data_model", inputDataModelJSON);

		// manipulate output data model (output data model = internal model (for now))
		final long internalModelId = 1;
		final DataModel outputDataModel = dataModelService.getObject(internalModelId);
		final String outputDataModelJSONString = objectMapper.writeValueAsString(outputDataModel);
		final ObjectNode outputDataModelJSON = objectMapper.readValue(outputDataModelJSONString, ObjectNode.class);
		((ObjectNode) taskJSON).put("output_data_model", outputDataModelJSON);

		// manipulate attributes
		final ObjectNode mappingJSON = (ObjectNode) ((ArrayNode) ((ObjectNode) ((ObjectNode) taskJSON).get("job")).get("mappings")).get(0);

		final String dataResourceSchemaBaseURI = DataModelUtils.determineDataModelSchemaBaseURI(updatedInputDataModel);

		final ObjectNode outputAttributePathAttributeJSON = (ObjectNode) ((ArrayNode) ((ObjectNode) ((ObjectNode) mappingJSON
				.get("output_attribute_path")).get("attribute_path")).get("attributes")).get(0);
		final String outputAttributeName = outputAttributePathAttributeJSON.get("name").asText();
		outputAttributePathAttributeJSON.put("uri", dataResourceSchemaBaseURI + outputAttributeName);

		final ArrayNode inputAttributePathsJSON = (ArrayNode) mappingJSON.get("input_attribute_paths");

		for (final JsonNode inputAttributePathsJSONNode : inputAttributePathsJSON) {

			final ObjectNode inputAttributeJSON = (ObjectNode) ((ArrayNode) ((ObjectNode) ((ObjectNode) inputAttributePathsJSONNode)
					.get("attribute_path")).get("attributes")).get(0);
			final String inputAttributeName = inputAttributeJSON.get("name").asText();
			inputAttributeJSON.put("uri", dataResourceSchemaBaseURI + inputAttributeName);
		}

		// manipulate parameter mappings in transformation component
		final ObjectNode transformationComponentParameterMappingsJSON = (ObjectNode) ((ObjectNode) mappingJSON.get("transformation"))
				.get("parameter_mappings");
		transformationComponentParameterMappingsJSON.put("description", dataResourceSchemaBaseURI + outputAttributeName);
		transformationComponentParameterMappingsJSON.put("__TRANSFORMATION_OUTPUT_VARIABLE__1", dataResourceSchemaBaseURI + outputAttributeName);

		final String finalTaskJSONString = objectMapper.writeValueAsString(taskJSON);

		final Task task = objectMapper.readValue(finalTaskJSONString, Task.class);

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = injector.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromTask(task, internalModelServiceFactoryProvider);

		flow.getScript();

		final String actual = flow.apply(tuples, true);

		final ArrayNode expectedJSONArray = objectMapper.readValue(expected, ArrayNode.class);
		final ArrayNode actualNodes = objectMapper.readValue(actual, ArrayNode.class);

		objectMapper.writeValueAsString(actualNodes);

		final String actualDataResourceSchemaBaseURI = DataModelUtils.determineDataModelSchemaBaseURI(updatedInputDataModel);

		final ObjectNode firstExpectedElement = (ObjectNode) expectedJSONArray.get(0);
		final String firstExpectedKey = firstExpectedElement.fieldNames().next();
		final String expectedRecordDataFieldNameExample = firstExpectedElement.get(firstExpectedKey).get(0).fieldNames().next();
		final String expectedDataResourceSchemaBaseURI = expectedRecordDataFieldNameExample.substring(0,
				expectedRecordDataFieldNameExample.lastIndexOf('#') + 1);

		for (final JsonNode expectedNode : expectedJSONArray) {

			final ObjectNode expectedElementInArray = (ObjectNode) expectedNode;
			final String expectedKeyInArray = expectedElementInArray.fieldNames().next();
			final String recordData = ((ObjectNode) expectedElementInArray.get(expectedKeyInArray).get(0)).get(
					expectedDataResourceSchemaBaseURI + "description").asText();
			final JsonNode actualNode = getRecordData(recordData, actualNodes, actualDataResourceSchemaBaseURI + "description");

			assertThat(actualNode, is(notNullValue()));

			final ObjectNode expectedRecordData = (ObjectNode) expectedElementInArray.get(expectedKeyInArray).get(0);

			final ObjectNode actualElement = (ObjectNode) actualNode;
			final String actualKey = actualElement.fieldNames().next();
			final ObjectNode actualRecordData = (ObjectNode) actualElement.get(actualKey).get(0);

			assertThat(actualRecordData.get(actualDataResourceSchemaBaseURI + "description").asText(),
					equalTo(expectedRecordData.get(expectedDataResourceSchemaBaseURI + "description").asText()));
		}

		// clean-up
		// TODO: move clean-up to @After

		final Map<Long, Attribute> attributes = Maps.newHashMap();

		final Map<Long, AttributePath> attributePaths = Maps.newLinkedHashMap();

		final Clasz recordClass = schema.getRecordClass();

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

		dataModelService.deleteObject(updatedInputDataModel.getId());
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

		// clean-up
		configurationService.deleteObject(updatedConfiguration.getId());
		resourceService.deleteObject(updatedResource.getId());

		// clean-up graph db
		//InternalGDMGraphServiceTestUtils.cleanGraphDB();
	}

	@Test
	public void testMorphToEndDemo() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("complex-result.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = injector.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromFile("complex-metamorph.xml", internalModelServiceFactoryProvider);

		final String actual = flow.applyDemo();

		assertEquals(expected, actual);
	}

	@Test
	public void testMorphRecursiveToEndDemo() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("complex-result.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = injector.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromFile("complex-metamorph-recursive.xml", internalModelServiceFactoryProvider);

		flow.getScript();

		final String actual = flow.applyDemo();

		assertEquals(expected, actual);
	}

	@Test
	public void testEndToEndByRecordStringExampleDemo() throws Exception {

		final String request = DMPPersistenceUtil.getResourceAsString("qucosa_record.xml");
		final String expected = DMPPersistenceUtil.getResourceAsString("complex-result.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = injector.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromFile("complex-metamorph.xml", internalModelServiceFactoryProvider);

		final String actual = flow.applyRecordDemo(request);

		assertEquals(expected, actual);
	}

	@Test
	public void testEndToEndByRecordStringExampleDemo2() throws Exception {

		final String request = DMPPersistenceUtil.getResourceAsString("qucosa-foaf.xml");
		final String expected = DMPPersistenceUtil.getResourceAsString("complex-multiple-input-result.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = injector.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromFile("complex-metamorph-multiple-input.xml", internalModelServiceFactoryProvider);

		final String actual = flow.applyRecordDemo(request);

		assertEquals(expected, actual);
	}

	public void readCSVTest() throws Exception {

		final FileOpener opener = new FileOpener();

		// set encoding
		opener.setEncoding(Charsets.UTF_8.name());

		final URL url = Resources.getResource("test_csv.csv");
		final File file = FileUtils.toFile(url);

		// set column separator and line separator
		final CsvReader reader = new CsvReader('\\', '"', ';', "\n");

		// set number of header lines (if header lines = 1, then schema header line = 1)
		reader.setHeader(true);
		final JsonEncoder converter = new JsonEncoder();
		final StringWriter stringWriter = new StringWriter();
		final ObjectJavaIoWriter<String> writer = new ObjectJavaIoWriter<String>(stringWriter);

		opener.setReceiver(reader).setReceiver(converter).setReceiver(writer);

		opener.process(file.getAbsolutePath());

		final String resultOutput = stringWriter.toString();

		Assert.assertNotNull("the result output shouldn't be null", resultOutput);

		final String expectedResult = DMPPersistenceUtil.getResourceAsString("csv_json.output");

		Assert.assertEquals("the processing outputs are not equal", expectedResult, resultOutput);
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

	private JsonNode getRecordData(final String recordData, final ArrayNode jsonArray, final String key) {

		for (final JsonNode jsonEntry : jsonArray) {

			final ObjectNode actualElementInArray = (ObjectNode) jsonEntry;
			final String actualKeyInArray = actualElementInArray.fieldNames().next();

			final ObjectNode actualRecordData = (ObjectNode) actualElementInArray.get(actualKeyInArray).get(0);

			if (recordData.equals(actualRecordData.get(key).asText())) {

				return jsonEntry;
			}
		}

		return null;
	}
}
