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
package org.dswarm.converter.flow.test.xml;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.inject.Provider;
import org.junit.Assert;
import org.junit.Test;

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
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.types.Tuple;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.service.internal.graph.InternalGDMGraphService;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.resource.ResourceService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public abstract class AbstractXMLTransformationFlowTest extends GuicedTest {

	protected final String taskJSONFileName;

	protected final String expectedResultJSONFileName;

	protected final String recordTag;

	protected final String xmlNamespace;

	protected final String exampleDataResourceFileName;

	protected final ObjectMapper objectMapper;

	public AbstractXMLTransformationFlowTest(final String taskJSONFileNameArg, final String expectedResultJSONFileNameArg, final String recordTagArg,
			final String xmlNamespaceArg, final String exampleDataResourceFileNameArg) {

		objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

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

		final Optional<Map<String, Model>> optionalModelMap = gdmService.getObjects(updatedInputDataModel.getId(), Optional.<Integer>absent());

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
		final long internalModelId = 2;
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
		final Iterable<JsonNode> actualKeyArray = actualElementInArray.get(actualKeyInArray);
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
