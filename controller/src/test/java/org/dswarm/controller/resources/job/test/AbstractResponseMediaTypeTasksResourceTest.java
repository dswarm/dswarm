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
package org.dswarm.controller.resources.job.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dswarm.controller.resources.job.TasksResource;
import org.dswarm.controller.resources.job.test.utils.TasksResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.controller.resources.test.ResourceTest;
import org.dswarm.controller.test.GuicedTest;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

public abstract class AbstractResponseMediaTypeTasksResourceTest extends ResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractResponseMediaTypeTasksResourceTest.class);

	private String taskJSONString = null;

	private ResourcesResourceTestUtils resourcesResourceTestUtils;

	private DataModelsResourceTestUtils dataModelsResourceTestUtils;

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	private final MediaType responseMediaType;
	protected final String expectedResultFileName;

	public AbstractResponseMediaTypeTasksResourceTest(final MediaType responseMediaTypeArg, final String expectedResultFileNameArg) {

		super("tasks");

		responseMediaType = responseMediaTypeArg;
		expectedResultFileName = expectedResultFileNameArg;
	}

	@Override
	protected void initObjects() {
		super.initObjects();

		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		dataModelsResourceTestUtils = new DataModelsResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		taskJSONString = DMPPersistenceUtil.getResourceAsString("task.json");
	}

	@Test
	public void testTaskExecution() throws Exception {

		AbstractResponseMediaTypeTasksResourceTest.LOG.debug("start {} task execution test", responseMediaType.toString());

		final String resourceFileName = "controller_test-mabxml.xml";

		final DataModel inputDataModel = TasksResourceTestUtils.prepareDataModel(resourceFileName, objectMapper, resourcesResourceTestUtils, dataModelsResourceTestUtils);

		final ObjectNode requestJSON = prepareTask(inputDataModel);

		final Response response = target().request(responseMediaType).post(Entity.json(requestJSON));

		Assert.assertEquals("200 Created was expected", 200, response.getStatus());

		final String actualResult = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", actualResult);

		AbstractResponseMediaTypeTasksResourceTest.LOG.debug("{} task execution response = '{}'", responseMediaType.toString(), actualResult);

		AbstractResponseMediaTypeTasksResourceTest.LOG.debug("end {} task execution test", responseMediaType.toString());

		compareResult(actualResult);
	}

	protected abstract void compareResult(final String actualResult) throws IOException;

	private ObjectNode prepareTask(final DataModel inputDataModel) throws Exception {
		// check processed data
		final String data = dataModelsResourceTestUtils.getData(inputDataModel.getUuid(), 1);

		Assert.assertNotNull("the data shouldn't be null", data);

		// manipulate input data model
		final String finalInputDataModelJSONString = objectMapper.writeValueAsString(inputDataModel);
		final ObjectNode finalInputDataModelJSON = objectMapper.readValue(finalInputDataModelJSONString, ObjectNode.class);

		final ObjectNode taskJSON = objectMapper.readValue(taskJSONString, ObjectNode.class);
		taskJSON.set("input_data_model", finalInputDataModelJSON);

		// utilise internal model as output data model
		final DataModel outputDataModel = dataModelsResourceTestUtils.getObject(DataModelUtils.BIBO_DOCUMENT_DATA_MODEL_UUID);
		final String outputDataModelJSONString = objectMapper.writeValueAsString(outputDataModel);
		final ObjectNode outputDataModelJSON = objectMapper.readValue(outputDataModelJSONString, ObjectNode.class);

		taskJSON.set("output_data_model", outputDataModelJSON);

		final ObjectNode requestJSON = objectMapper.createObjectNode();
		requestJSON.set(TasksResource.TASK_IDENTIFIER, taskJSON);
		requestJSON.put(TasksResource.PERSIST_IDENTIFIER, Boolean.FALSE);
		requestJSON.put(TasksResource.DO_INGEST_ON_THE_FLY_IDENTIFIER, Boolean.TRUE);
		requestJSON.put(TasksResource.DO_EXPORT_ON_THE_FLY_IDENTIFIER, Boolean.TRUE);
		requestJSON.put(TasksResource.DO_VERSIONING_ON_RESULT_IDENTIFIER, Boolean.FALSE);

		return requestJSON;
	}
}
