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
import com.google.common.io.Resources;

import de.avgl.dmp.converter.GuicedTest;
import de.avgl.dmp.converter.flow.CSVSourceResourceTriplesFlow;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.converter.mf.stream.reader.CsvReader;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.internal.impl.MemoryDBInputModel;
import de.avgl.dmp.persistence.model.job.Task;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;
import de.avgl.dmp.persistence.model.resource.utils.DataModelUtils;
import de.avgl.dmp.persistence.model.types.Tuple;
import de.avgl.dmp.persistence.service.impl.InternalMemoryDbService;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.resource.ResourceService;
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

		final String taskJSONString = DMPPersistenceUtil.getResourceAsString("task.csv.json");
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
		final DataModel dataModel = dataModelService.createObjectTransactional().getObject();

		dataModel.setDataResource(updatedResource);
		dataModel.setConfiguration(updatedConfiguration);

		DataModel updatedDataModel = dataModelService.updateObjectTransactional(dataModel).getObject();

		final CSVSourceResourceTriplesFlow flow2 = new CSVSourceResourceTriplesFlow(updatedDataModel);

		final List<Triple> csvRecordTriples = flow2.applyResource("test_csv.csv");

		Assert.assertNotNull("CSV record triple list shouldn't be null", csvRecordTriples);
		Assert.assertFalse("CSV record triple list shouldn't be empty", csvRecordTriples.isEmpty());

		final InternalMemoryDbService memoryDbService = injector.getInstance(InternalMemoryDbService.class);

		// write CSV record triples
		for (final Triple triple : csvRecordTriples) {

			final MemoryDBInputModel mdbim = new MemoryDBInputModel(triple);

			memoryDbService.createObject(updatedDataModel.getId(), mdbim);
		}

		final Optional<Map<String, Model>> optionalModelMap = memoryDbService.getObjects(updatedDataModel.getId(), Optional.<Integer> absent());

		Assert.assertNotNull("CSV record model map optional shouldn't be null", optionalModelMap);
		Assert.assertTrue("CSV record model map should be present", optionalModelMap.isPresent());
		Assert.assertFalse("CSV record model map shouldn't be empty", optionalModelMap.get().isEmpty());

		final Iterator<Tuple<String, JsonNode>> tuples = dataIterator(optionalModelMap.get().entrySet().iterator());

		Assert.assertNotNull("CSV record tuples iterator shouldn't be null", tuples);

		final String dataModelJSONString = objectMapper.writeValueAsString(updatedDataModel);
		final ObjectNode dataModelJSON = objectMapper.readValue(dataModelJSONString, ObjectNode.class);

		// manipulate input data model
		final ObjectNode taskJSON = objectMapper.readValue(taskJSONString, ObjectNode.class);
		((ObjectNode) taskJSON).put("input_data_model", dataModelJSON);

		// manipulate attributes
		final ObjectNode mappingJSON = (ObjectNode) ((ArrayNode) ((ObjectNode) ((ObjectNode) taskJSON).get("job")).get("mappings")).get(0);

		final String dataResourceSchemaBaseURI = DataModelUtils.determineDataResourceSchemaBaseURI(updatedDataModel);

		final ObjectNode outputAttributePathAttributeJSON = (ObjectNode) ((ArrayNode) ((ObjectNode) mappingJSON.get("output_attribute_path"))
				.get("attributes")).get(0);
		final String outputAttributeName = outputAttributePathAttributeJSON.get("name").asText();
		outputAttributePathAttributeJSON.put("uri", dataResourceSchemaBaseURI + outputAttributeName);

		final ArrayNode inputAttributePathsJSON = (ArrayNode) mappingJSON.get("input_attribute_paths");

		for (final JsonNode inputAttributePathsJSONNode : inputAttributePathsJSON) {

			final ObjectNode inputAttributeJSON = (ObjectNode) ((ArrayNode) ((ObjectNode) inputAttributePathsJSONNode).get("attributes")).get(0);
			final String inputAttributeName = inputAttributeJSON.get("name").asText();
			inputAttributeJSON.put("uri", dataResourceSchemaBaseURI + inputAttributeName);
		}

		final String finalTaskJSONString = objectMapper.writeValueAsString(taskJSON);

		final Task task = objectMapper.readValue(finalTaskJSONString, Task.class);
		final TransformationFlow flow = TransformationFlow.fromTask(task);

		final String actual = flow.apply(tuples);

		final ArrayNode expectedJSONArray = objectMapper.readValue(expected, ArrayNode.class);
		final ArrayNode actualNodes = objectMapper.readValue(actual, ArrayNode.class);

		final String actualDataResourceSchemaBaseURI = DataModelUtils.determineDataResourceSchemaBaseURI(updatedDataModel);

		final String expectedRecordDataFieldNameExample = expectedJSONArray.get(0).get("record_data").fieldNames().next();
		final String expectedDataResourceSchemaBaseURI = expectedRecordDataFieldNameExample.substring(0,
				expectedRecordDataFieldNameExample.lastIndexOf('#') + 1);

		for (final JsonNode expectedNode : expectedJSONArray) {
			final String recordId = expectedNode.get("record_id").asText();
			final JsonNode actualNode = getRecord(recordId, actualNodes);

			assertThat(actualNode, is(notNullValue()));

			assertThat(expectedNode.get("record_id").asText(), equalTo(actualNode.get("record_id").asText()));

			final ObjectNode expectedRecordData = (ObjectNode) expectedNode.get("record_data");
			final ObjectNode actualRecordData = (ObjectNode) actualNode.get("record_data");

			assertThat(expectedRecordData.get(expectedDataResourceSchemaBaseURI + "description").asText(),
					equalTo(actualRecordData.get(actualDataResourceSchemaBaseURI + "description").asText()));
		}

		// clean-up
		dataModelService.deleteObject(updatedDataModel.getId());
		configurationService.deleteObject(updatedConfiguration.getId());
		resourceService.deleteObject(updatedResource.getId());
	}

	@Test
	public void testMorphToEndDemo() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("complex-result.json");

		final TransformationFlow flow = TransformationFlow.fromFile("complex-metamorph.xml");

		final String actual = flow.applyDemo();

		assertEquals(expected, actual);
	}

	@Test
	public void testEndToEndByRecordStringExampleDemo() throws Exception {

		final String request = DMPPersistenceUtil.getResourceAsString("qucosa_record.xml");
		final String expected = DMPPersistenceUtil.getResourceAsString("complex-result.json");

		final TransformationFlow flow = TransformationFlow.fromFile("complex-metamorph.xml");

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

		Assert.assertNotNull("the result output shoudln't be null", resultOutput);

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
					final JsonNode jsonNode = nextTriple.getValue().toJSON();
					return Tuple.tuple(recordId, jsonNode);
				}
				return endOfData();
			}
		};
	}

	private JsonNode getRecord(final String recordId, final ArrayNode jsonArray) {

		for (final JsonNode jsonEntry : jsonArray) {

			if (recordId.equals(jsonEntry.get("record_id").asText())) {

				return jsonEntry;
			}
		}

		return null;
	}
}
