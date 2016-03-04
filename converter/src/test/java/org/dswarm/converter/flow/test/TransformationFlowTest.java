/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.converter.flow.test;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Charsets;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.culturegraph.mf.stream.converter.JsonEncoder;
import org.culturegraph.mf.stream.sink.ObjectJavaIoWriter;
import org.culturegraph.mf.stream.source.FileOpener;
import org.culturegraph.mf.types.Triple;
import org.dswarm.converter.flow.JSONTransformationFlow;
import org.dswarm.converter.flow.JSONTransformationFlowFactory;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import rx.Observable;
import rx.schedulers.Schedulers;

import org.dswarm.common.types.Tuple;
import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.flow.CSVResourceFlowFactory;
import org.dswarm.converter.flow.CSVSourceResourceTriplesFlow;
import org.dswarm.converter.mf.stream.reader.CsvReader;
import org.dswarm.graph.json.LiteralNode;
import org.dswarm.graph.json.Predicate;
import org.dswarm.graph.json.ResourceNode;
import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.internal.graph.InternalGDMGraphService;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.resource.ResourceService;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.dswarm.persistence.util.GDMUtil;

public class TransformationFlowTest extends GuicedTest {

	@Test
	public void testCSVDataResourceEndToEnd() throws Exception {

		final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

		final String taskJSONString = DMPPersistenceUtil.getResourceAsString("converter_task.csv.json");
		final String expected = DMPPersistenceUtil.getResourceAsString("task-result.csv.json");

		// process input data model
		final ConfigurationService configurationService = GuicedTest.injector.getInstance(ConfigurationService.class);
		final Configuration configuration = configurationService.createObjectTransactional().getObject();

		configuration.setName("config1");
		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		configuration.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode("csv"));

		final Configuration updatedConfiguration = configurationService.updateObjectTransactional(configuration).getObject();

		final ResourceService resourceService = GuicedTest.injector.getInstance(ResourceService.class);
		final Resource resource = resourceService.createObjectTransactional().getObject();
		resource.setName("test_csv.csv");
		resource.setType(ResourceType.FILE);
		resource.addConfiguration(updatedConfiguration);

		final URL fileURL = Resources.getResource("test_csv.csv");
		final File resourceFile = FileUtils.toFile(fileURL);

		resource.addAttribute("path", resourceFile.getAbsolutePath());

		final Resource updatedResource = resourceService.updateObjectTransactional(resource).getObject();

		final DataModelService dataModelService = GuicedTest.injector.getInstance(DataModelService.class);
		final DataModel inputDataModel = dataModelService.createObjectTransactional().getObject();

		inputDataModel.setDataResource(updatedResource);
		inputDataModel.setConfiguration(updatedConfiguration);

		final DataModel updatedInputDataModel = dataModelService.updateObjectTransactional(inputDataModel).getObject();

		final CSVSourceResourceTriplesFlow flow2 = injector
				.getInstance(CSVResourceFlowFactory.class)
				.fromDataModel(updatedInputDataModel);

		final Collection<Triple> csvRecordTriples = flow2.applyResource("test_csv.csv").flatMap(Observable::<Triple>from).toList().toBlocking()
				.first();

		Assert.assertNotNull("CSV record triple list shouldn't be null", csvRecordTriples);
		Assert.assertFalse("CSV record triple list shouldn't be empty", csvRecordTriples.isEmpty());

		final InternalGDMGraphService gdmService = GuicedTest.injector.getInstance(InternalGDMGraphService.class);

		// convert result to GDM
		final Map<Long, org.dswarm.graph.json.Resource> recordResources = Maps.newLinkedHashMap();

		final org.dswarm.graph.json.Model model = new org.dswarm.graph.json.Model();

		final String dataResourceBaseSchemaURI = DataModelUtils.determineDataModelSchemaBaseURI(inputDataModel);
		final String recordClassURI = dataResourceBaseSchemaURI + "RecordType";
		final ResourceNode recordClasz = new ResourceNode(recordClassURI);

		for (final org.culturegraph.mf.types.Triple triple : csvRecordTriples) {

			final org.dswarm.graph.json.Resource recordResource = mintRecordResource(Long.valueOf(triple.getSubject()),
					inputDataModel, recordResources, model, recordClasz);
			final Predicate property = new Predicate(triple.getPredicate());

			final ResourceNode subject = (ResourceNode) recordResource.getStatements().iterator().next().getSubject();

			recordResource.addStatement(subject, property, new LiteralNode(triple.getObject()));
		}

		final GDMModel gdmModel = new GDMModel(model, null, recordClassURI);

		// System.out.println(objectMapper.writeValueAsString(gdmModel.getSchema()));
		//
		// for (final AttributePathHelper attributePathHelper : gdmModel.getAttributePaths()) {
		//
		// System.out.println(attributePathHelper.toString());
		// }
		//
		// System.out.println(objectMapper.configure(SerializationFeature.INDENT_OUTPUT,
		// true).writeValueAsString(gdmModel.toJSON()));
		// System.out.println(Util.getJSONObjectMapper().configure(SerializationFeature.INDENT_OUTPUT,
		// true).writeValueAsString(gdmModel.getModel()));

		final Observable<Response> responseObservable = gdmService.createObject(inputDataModel.getUuid(), Observable.just(gdmModel));
		final Response response = responseObservable.toBlocking().firstOrDefault(null);
		// finished writing CSV statements to graph

		Assert.assertNotNull(response);

		// retrieve updated fresh data model
		final DataModel freshInputDataModel = dataModelService.getObject(updatedInputDataModel.getUuid());

		Assert.assertNotNull("the fresh data model shouldn't be null", freshInputDataModel);
		Assert.assertNotNull("the schema of the fresh data model shouldn't be null", freshInputDataModel.getSchema());

		final Schema schema = freshInputDataModel.getSchema();

		final Observable<Map<String, Model>> optionalModelMapObservable = gdmService
				.getObjects(updatedInputDataModel.getUuid(), Optional.<Integer>empty())
				.toMap(Tuple::v1, Tuple::v2);
		final Optional<Map<String, Model>> optionalModelMap = optionalModelMapObservable.map(Optional::of).toBlocking()
				.firstOrDefault(Optional.empty());

		Assert.assertNotNull("CSV record model map optional shouldn't be null", optionalModelMap);
		Assert.assertTrue("CSV record model map should be present", optionalModelMap.isPresent());
		Assert.assertFalse("CSV record model map shouldn't be empty", optionalModelMap.get().isEmpty());

		final Observable<Tuple<String, JsonNode>> tuples = Observable.from(dataIterable(optionalModelMap.get().entrySet()));

		// final List<Tuple<String, JsonNode>> tuplesList = Lists.newLinkedList();
		//
		// while (tuples.hasNext()) {
		//
		// tuplesList.add(tuples.next());
		// }
		//
		// final String tuplesJSON = objectMapper.configure(SerializationFeature.INDENT_OUTPUT,
		// true).writeValueAsString(tuplesList);
		//
		// System.out.println(tuplesJSON);

		Assert.assertNotNull("CSV record tuples iterator shouldn't be null", tuples);

		final String inputDataModelJSONString = objectMapper.writeValueAsString(updatedInputDataModel);
		final ObjectNode inputDataModelJSON = objectMapper.readValue(inputDataModelJSONString, ObjectNode.class);

		// manipulate input data model
		final ObjectNode taskJSON = objectMapper.readValue(taskJSONString, ObjectNode.class);
		taskJSON.set("input_data_model", inputDataModelJSON);

		final String internalModelId = DataModelUtils.BIBO_DOCUMENT_DATA_MODEL_UUID;
		final DataModel outputDataModel = dataModelService.getObject(internalModelId);
		final String outputDataModelJSONString = objectMapper.writeValueAsString(outputDataModel);
		final ObjectNode outputDataModelJSON = objectMapper.readValue(outputDataModelJSONString, ObjectNode.class);
		taskJSON.set("output_data_model", outputDataModelJSON);

		// manipulate attributes
		final ObjectNode mappingJSON = (ObjectNode) taskJSON.get("job").get("mappings").get(0);

		final String dataResourceSchemaBaseURI = DataModelUtils.determineDataModelSchemaBaseURI(updatedInputDataModel);

		final ObjectNode outputAttributePathAttributeJSON = (ObjectNode) mappingJSON
				.get("output_attribute_path").get("attribute_path").get("attributes").get(0);
		final String outputAttributeName = outputAttributePathAttributeJSON.get("name").asText();
		outputAttributePathAttributeJSON.put("uri", dataResourceSchemaBaseURI + outputAttributeName);

		final ArrayNode inputAttributePathsJSON = (ArrayNode) mappingJSON.get("input_attribute_paths");

		for (final JsonNode inputAttributePathsJSONNode : inputAttributePathsJSON) {

			final ObjectNode inputAttributeJSON = (ObjectNode) inputAttributePathsJSONNode
					.get("attribute_path").get("attributes").get(0);
			final String inputAttributeName = inputAttributeJSON.get("name").asText();
			inputAttributeJSON.put("uri", dataResourceSchemaBaseURI + inputAttributeName);
		}

		// manipulate parameter mappings in transformation component
		final ObjectNode transformationComponentParameterMappingsJSON = (ObjectNode) mappingJSON.get("transformation")
				.get("parameter_mappings");
		transformationComponentParameterMappingsJSON.put("description", "description");
		transformationComponentParameterMappingsJSON.put("__TRANSFORMATION_OUTPUT_VARIABLE__1", "output mapping attribute path instance");

		final String finalTaskJSONString = objectMapper.writeValueAsString(taskJSON);

		final Task task = objectMapper.readValue(finalTaskJSONString, Task.class);

		final JSONTransformationFlowFactory flowFactory = GuicedTest.injector
				.getInstance(JSONTransformationFlowFactory.class);

		final JSONTransformationFlow flow = flowFactory.fromTask(task);

		flow.getScript();

		final ArrayNode actualNodes = flow.apply(tuples, true, false, true, Schedulers.newThread()).reduce(
				DMPPersistenceUtil.getJSONObjectMapper().createArrayNode(),
				ArrayNode::add
		).toBlocking().first();

		final ArrayNode expectedJSONArray = objectMapper.readValue(expected, ArrayNode.class);

		Assert.assertThat(actualNodes, CoreMatchers.is(Matchers.notNullValue()));
		Assert.assertThat(expectedJSONArray, CoreMatchers.is(Matchers.notNullValue()));
		Assert.assertThat(actualNodes.size(), Matchers.equalTo(expectedJSONArray.size()));

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
			final String recordData = expectedElementInArray.get(expectedKeyInArray).get(0).get(
					expectedDataResourceSchemaBaseURI + "description").asText();
			final JsonNode actualNode = getRecordData(recordData, actualNodes, actualDataResourceSchemaBaseURI + "description");

			Assert.assertThat(actualNode, CoreMatchers.is(Matchers.notNullValue()));

			final ObjectNode expectedRecordData = (ObjectNode) expectedElementInArray.get(expectedKeyInArray).get(0);

			final ObjectNode actualElement = (ObjectNode) actualNode;
			final String actualKey = actualElement.fieldNames().next();
			ObjectNode actualRecordData = null;

			for (final JsonNode actualRecordDataCandidate : actualElement.get(actualKey)) {

				if (actualRecordDataCandidate.get(actualDataResourceSchemaBaseURI + "description") != null) {

					actualRecordData = (ObjectNode) actualRecordDataCandidate;

					break;
				}
			}

			Assert.assertThat(actualRecordData.get(actualDataResourceSchemaBaseURI + "description").asText(),
					Matchers.equalTo(expectedRecordData.get(expectedDataResourceSchemaBaseURI + "description").asText()));
		}

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

	private static Iterable<Tuple<String, JsonNode>> dataIterable(final Iterable<Map.Entry<String, Model>> triples) {
		return () -> dataIterator(triples.iterator());
	}

	private static Iterator<Tuple<String, JsonNode>> dataIterator(final Iterator<Map.Entry<String, Model>> triples) {
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

			final ArrayNode actualRecordDataArray = (ArrayNode) actualElementInArray.get(actualKeyInArray);

			for (final JsonNode actualRecordData : actualRecordDataArray) {

				if (actualRecordData.get(key) == null) {

					continue;
				}

				if (recordData.equals(actualRecordData.get(key).asText())) {

					return jsonEntry;
				}
			}
		}

		return null;
	}

	private static org.dswarm.graph.json.Resource mintRecordResource(final Long identifier, final DataModel dataModel,
			final Map<Long, org.dswarm.graph.json.Resource> recordResources, final org.dswarm.graph.json.Model model,
			final ResourceNode recordClassNode) {

		if (identifier != null) {

			if (recordResources.containsKey(identifier)) {

				return recordResources.get(identifier);
			}
		}

		// mint completely new uri

		final StringBuilder sb = new StringBuilder();

		if (dataModel != null) {

			// create uri from resource id and configuration id and random uuid

			sb.append("http://data.slub-dresden.de/datamodels/").append(dataModel.getUuid()).append("/records/");
		} else {

			// create uri from random uuid

			sb.append("http://data.slub-dresden.de/records/");
		}

		final String recordURI = sb.append(UUID.randomUUID()).toString();

		final org.dswarm.graph.json.Resource recordResource = new org.dswarm.graph.json.Resource(recordURI);

		if (identifier != null) {

			recordResources.put(identifier, recordResource);
		}

		// add resource type statement to model
		recordResource.addStatement(new ResourceNode(recordResource.getUri()), new Predicate(GDMUtil.RDF_type), recordClassNode);
		model.addResource(recordResource);

		return recordResource;
	}
}
