package de.avgl.dmp.converter.flow.test;

import static org.junit.Assert.assertEquals;

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
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.avgl.dmp.converter.GuicedTest;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.converter.flow.XMLSourceResourceTriplesFlow;
import de.avgl.dmp.converter.mf.stream.reader.CsvReader;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.internal.impl.RDFModel;
import de.avgl.dmp.persistence.model.job.Task;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.types.Tuple;
import de.avgl.dmp.persistence.service.impl.InternalTripleService;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.resource.ResourceService;
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
	public void testEndToEnd() throws Exception {

		final ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);

		final String taskJSONString = DMPPersistenceUtil.getResourceAsString("task.json");
		final String expected = DMPPersistenceUtil.getResourceAsString("task-result.json");

		// process input data model
		final ConfigurationService configurationService = injector.getInstance(ConfigurationService.class);
		final Configuration configuration = configurationService.createObject();

		configuration.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("datensatz"));
		configuration.addParameter(ConfigurationStatics.XML_NAMESPACE, new TextNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd"));
		configuration.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode("xml"));

		Configuration updatedConfiguration = configurationService.updateObjectTransactional(configuration);

		final ResourceService resourceService = injector.getInstance(ResourceService.class);
		final Resource resource = resourceService.createObject();
		resource.setName("test-mabxml.xml");
		resource.setType(ResourceType.FILE);
		resource.addConfiguration(updatedConfiguration);

		Resource updatedResource = resourceService.updateObjectTransactional(resource);

		final DataModelService dataModelService = injector.getInstance(DataModelService.class);
		final DataModel dataModel = dataModelService.createObject();

		dataModel.setDataResource(updatedResource);
		dataModel.setConfiguration(updatedConfiguration);

		DataModel updatedDataModel = dataModelService.updateObjectTransactional(dataModel);

		final XMLSourceResourceTriplesFlow flow2 = new XMLSourceResourceTriplesFlow(updatedDataModel);

		final List<RDFModel> rdfModels = flow2.applyResource("test-mabxml.xml");

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

		// write model and retrieve tuples
		final InternalTripleService tripleService = injector.getInstance(InternalTripleService.class);
		tripleService.createObject(updatedDataModel.getId(), rdfModel);

		// retrieve updated fresh data model
		final DataModel freshDataModel = dataModelService.getObject(updatedDataModel.getId());

		Assert.assertNotNull("the fresh data model shouldn't be null", freshDataModel);
		Assert.assertNotNull("the schema of the fresh data model shouldn't be null", freshDataModel.getSchema());

		final Schema schema = freshDataModel.getSchema();

		Assert.assertNotNull("the record class of the schema of the fresh data model shouldn't be null", schema.getRecordClass());

		final Clasz recordClass = schema.getRecordClass();

		final Optional<Map<String, Model>> optionalModelMap = tripleService.getObjects(updatedDataModel.getId(), Optional.of(1));

		final Iterator<Tuple<String, JsonNode>> tuples = dataIterator(optionalModelMap.get().entrySet().iterator());

		final String dataModelJSONString = objectMapper.writeValueAsString(updatedDataModel);
		final ObjectNode dataModelJSON = objectMapper.readValue(dataModelJSONString, ObjectNode.class);

		// manipulate input data model
		final ObjectNode taskJSON = objectMapper.readValue(taskJSONString, ObjectNode.class);
		((ObjectNode) taskJSON).put("input_data_model", dataModelJSON);

		final String finalTaskJSONString = objectMapper.writeValueAsString(taskJSON);

		final Task task = objectMapper.readValue(finalTaskJSONString, Task.class);
		final TransformationFlow flow = TransformationFlow.fromTask(task);

		final String actual = flow.apply(tuples);

		final ArrayNode expectedJSONArray = objectMapper.readValue(expected, ArrayNode.class);
		final ObjectNode expectedJSON = (ObjectNode) expectedJSONArray.get(0).get("record_data");
		final String finalExpectedJSONString = objectMapper.writeValueAsString(expectedJSON);

		final ArrayNode actualJSONArray = objectMapper.readValue(actual, ArrayNode.class);
		final ObjectNode actualJSON = (ObjectNode) actualJSONArray.get(0).get("record_data");
		final String finalActualJSONString = objectMapper.writeValueAsString(actualJSON);

		assertEquals(finalExpectedJSONString.length(), finalActualJSONString.length());

		// clean-up
		dataModelService.deleteObject(updatedDataModel.getId());

		final SchemaService schemaService = injector.getInstance(SchemaService.class);

		schemaService.deleteObject(schema.getId());

		final ClaszService claszService = injector.getInstance(ClaszService.class);

		claszService.deleteObject(recordClass.getId());

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
}
