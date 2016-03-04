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
package org.dswarm.controller.resources.job.test.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dswarm.controller.resources.job.test.model.PrepareConfiguration;
import org.dswarm.controller.resources.job.test.model.PrepareResource;
import org.dswarm.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;

/**
 * Created by tgaengler on 04.03.16.
 */
public class TasksResourceTestUtils {

	public static DataModel prepareDataModel(final String resourceFileName,
	                                         final ObjectMapper objectMapper,
	                                         final ResourcesResourceTestUtils resourcesResourceTestUtils,
	                                         final DataModelsResourceTestUtils dataModelsResourceTestUtils) throws Exception {

		final PrepareResource prepareResource = new PrepareResource(resourceFileName, objectMapper, resourcesResourceTestUtils).invoke();
		final PrepareConfiguration prepareConfiguration = new PrepareConfiguration(prepareResource, objectMapper, resourcesResourceTestUtils).invoke();
		return prepareDataModel(prepareResource, prepareConfiguration, objectMapper, dataModelsResourceTestUtils);
	}

	private static DataModel prepareDataModel(final PrepareResource prepareResource,
	                                          final PrepareConfiguration prepareConfiguration,
	                                          final ObjectMapper objectMapper,
	                                          final DataModelsResourceTestUtils dataModelsResourceTestUtils) throws Exception {

		final Resource resource = prepareResource.getResource();
		final Resource res1 = prepareResource.getRes1();
		final Configuration conf1 = prepareConfiguration.getConf1();
		final Configuration configuration = prepareConfiguration.getConfiguration();

		final String dataModel1Uuid = UUIDService.getUUID(DataModel.class.getSimpleName());

		final DataModel data1 = new DataModel(dataModel1Uuid);
		data1.setName("'" + res1.getName() + "' + '" + conf1.getName() + "' data model");
		data1.setDescription("data model of resource '" + res1.getName() + "' and configuration '" + conf1.getName() + "'");
		data1.setDataResource(resource);
		data1.setConfiguration(configuration);

		// TODO: add schema to data1

		final String inputDataModelJSONString = objectMapper.writeValueAsString(data1);

		final DataModel inputDataModel = dataModelsResourceTestUtils.createObjectWithoutComparison(inputDataModelJSONString);

		Assert.assertNotNull("the data model shouldn't be null", inputDataModel);

		return inputDataModel;
	}

	public static void compareCSVTaskResultJSON(final ArrayNode expectedJSONArray,
	                                            final String expectedDataResourceSchemaBaseURI,
	                                            final ArrayNode actualJSONArray,
	                                            final String actualDataResourceSchemaBaseURI) {

		for (final JsonNode expectedNode : expectedJSONArray) {

			final String recordData = expectedNode.get(DMPPersistenceUtil.RECORD_DATA).get(0).get(expectedDataResourceSchemaBaseURI + "description")
					.asText();
			final JsonNode actualNode = TasksResourceTestUtils.getRecordData(recordData, actualJSONArray, actualDataResourceSchemaBaseURI + "description");

			// SR TODO use Assert.assertNotNull here?
			Assert.assertThat(actualNode, CoreMatchers.is(Matchers.notNullValue()));

			final ObjectNode expectedRecordData = (ObjectNode) expectedNode.get(DMPPersistenceUtil.RECORD_DATA).get(0);

			final ObjectNode actualElement = (ObjectNode) actualNode;
			ObjectNode actualRecordData = null;

			for (final JsonNode actualRecordDataCandidate : actualElement.get(DMPPersistenceUtil.RECORD_DATA)) {

				if (actualRecordDataCandidate.get(actualDataResourceSchemaBaseURI + "description") != null) {

					actualRecordData = (ObjectNode) actualRecordDataCandidate;

					break;
				}
			}

			Assert.assertThat(actualRecordData, CoreMatchers.is(Matchers.notNullValue()));

			Assert.assertThat(actualRecordData.get(actualDataResourceSchemaBaseURI + "description").asText(),
					Matchers.equalTo(expectedRecordData.get(expectedDataResourceSchemaBaseURI + "description").asText()));
		}
	}

	public static JsonNode getRecordData(final String recordData, final ArrayNode jsonArray, final String key) {

		for (final JsonNode jsonEntry : jsonArray) {

			final ArrayNode actualRecordDataArray = (ArrayNode) jsonEntry.get(DMPPersistenceUtil.RECORD_DATA);

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
}
