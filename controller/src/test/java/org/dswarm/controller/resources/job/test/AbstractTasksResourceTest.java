/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.resources.job.TasksResource;
import org.dswarm.controller.resources.resource.DataModelsResource;
import org.dswarm.controller.resources.resource.test.utils.ConfigurationsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.controller.resources.test.ResourceTest;
import org.dswarm.controller.test.GuicedTest;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public abstract class AbstractTasksResourceTest extends ResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractTasksResourceTest.class);

	protected String taskJSONString = null;

	protected ResourcesResourceTestUtils      resourcesResourceTestUtils;
	protected ConfigurationsResourceTestUtils configurationsResourceTestUtils;
	protected DataModelsResourceTestUtils     dataModelsResourceTestUtils;

	protected final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	protected final String  taskJSONFileName;
	protected final String  inputDataResourceFileName;
	protected final String  testPostfix;
	protected final boolean prepareInputDataResource;

	public AbstractTasksResourceTest(final String taskJSONFileNameArg, final String inputDataResourceFileNameArg, final String testPostfixArg,
			final boolean prepareInputDataResourceArg) {

		super("tasks");

		taskJSONFileName = taskJSONFileNameArg;
		inputDataResourceFileName = inputDataResourceFileNameArg;
		testPostfix = testPostfixArg;
		prepareInputDataResource = prepareInputDataResourceArg;
	}

	@Override protected void initObjects() {
		super.initObjects();

		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
		dataModelsResourceTestUtils = new DataModelsResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		taskJSONString = DMPPersistenceUtil.getResourceAsString(taskJSONFileName);
	}

	@Test
	public abstract void testTaskExecution() throws Exception;

	protected ObjectNode prepareTask() throws Exception {

		final PrepareResource prepareResource;

		if (prepareInputDataResource) {

			prepareResource = new PrepareResource(inputDataResourceFileName).invoke();
		} else {

			final Task task = objectMapper.readValue(taskJSONString, Task.class);
			final DataModel inputDataModel = task.getInputDataModel();
			final Resource inputDataResource = inputDataModel.getDataResource();

			prepareResource = new PrepareResource(inputDataResource);
		}

		final PrepareConfiguration prepareConfiguration = createPrepareConfiguration(prepareResource).invoke();
		DataModel inputDataModel = prepareDataModel(prepareResource, prepareConfiguration);

		final ObjectNode requestJSON = prepareTask(inputDataModel);

		LOG.debug("task request JSON = '{}'", objectMapper.writeValueAsString(requestJSON));

		return requestJSON;
	}

	protected abstract PrepareConfiguration createPrepareConfiguration(final PrepareResource prepareResource);

	private ObjectNode prepareTask(final DataModel inputDataModel) throws Exception {

		// manipulate input data model
		final String finalInputDataModelJSONString = objectMapper.writeValueAsString(inputDataModel);
		final ObjectNode finalInputDataModelJSON = objectMapper.readValue(finalInputDataModelJSONString, ObjectNode.class);

		final ObjectNode taskJSON = objectMapper.readValue(taskJSONString, ObjectNode.class);
		taskJSON.set("input_data_model", finalInputDataModelJSON);

		final ObjectNode requestJSON = objectMapper.createObjectNode();
		requestJSON.set(TasksResource.TASK_IDENTIFIER, taskJSON);
		requestJSON.put(TasksResource.PERSIST_IDENTIFIER, Boolean.FALSE);
		requestJSON.put(TasksResource.DO_INGEST_ON_THE_FLY_IDENTIFIER, Boolean.TRUE);
		requestJSON.put(TasksResource.DO_EXPORT_ON_THE_FLY_IDENTIFIER, Boolean.TRUE);
		requestJSON.put(TasksResource.DO_VERSIONING_ON_RESULT_IDENTIFIER, Boolean.FALSE);
		requestJSON.put(TasksResource.RETURN_IDENTIFIER, Boolean.TRUE);

		return requestJSON;
	}

	private DataModel prepareDataModel(final PrepareResource prepareResource, final PrepareConfiguration prepareConfiguration) throws Exception {

		final Resource resource = prepareResource.getResource();
		final Resource res1 = prepareResource.getRes1();
		final Configuration conf1 = prepareConfiguration.getConf1();
		final Configuration configuration = prepareConfiguration.getConfiguration();

		final String dataModel1Uuid = "DataModel-2e0c9850-6def-4942-abed-b513d3f56eba";

		final DataModel data1 = new DataModel(dataModel1Uuid);
		data1.setName("'" + res1.getName() + "' + '" + conf1.getName() + "' data model");
		data1.setDescription("data model of resource '" + res1.getName() + "' and configuration '" + conf1.getName() + "'");
		data1.setDataResource(resource);
		data1.setConfiguration(configuration);

		final String inputDataModelJSONString = objectMapper.writeValueAsString(data1);

		final WebTarget resourceTarget = dataModelsResourceTestUtils.getResourceTarget();
		final WebTarget webTarget = resourceTarget.queryParam(DataModelsResource.DO_INGEST_QUERY_PARAM_IDENTIFIER, Boolean.FALSE);

		LOG.debug("data model creation request URI = '{}'", webTarget.getUri());

		final Invocation.Builder request = webTarget
				.request(MediaType.APPLICATION_JSON_TYPE);

		final Response response = request.post(
				Entity.json(inputDataModelJSONString));

		Assert.assertNotNull(response);
		Assert.assertEquals(201, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final DataModel inputDataModel = dataModelsResourceTestUtils.readObject(responseString);

		Assert.assertNotNull("the data model shouldn't be null", inputDataModel);

		return inputDataModel;
	}

	protected class PrepareResource {

		private       Resource res1;
		private       Resource resource;
		private final String   resourceFileName;

		public PrepareResource(final Resource resourceArg) {

			resourceFileName = null;
			resource = resourceArg;
			res1 = resource;
		}

		public PrepareResource(final String resourceFileName) {

			this.resourceFileName = resourceFileName;
		}

		public Resource getRes1() {

			return res1;
		}

		public Resource getResource() {

			return resource;
		}

		public PrepareResource invoke() throws Exception {

			final String resource1Uuid = UUIDService.getUUID(Resource.class.getSimpleName());

			final int lastBackslash = resourceFileName.lastIndexOf("/");
			final String relativeResourceFileName = resourceFileName.substring(lastBackslash + 1, resourceFileName.length());

			res1 = new Resource(resource1Uuid);
			res1.setName(relativeResourceFileName);
			res1.setDescription("this is a description");
			res1.setType(ResourceType.FILE);

			final URL fileURL = Resources.getResource(resourceFileName);
			final File resourceFile = FileUtils.toFile(fileURL);

			final ObjectNode attributes1 = new ObjectNode(objectMapper.getNodeFactory());
			attributes1.put("path", resourceFile.getAbsolutePath());

			String fileType = null;
			final Tika tika = new Tika();
			try {
				fileType = tika.detect(resourceFile);
				// fileType = Files.probeContentType(resourceFile.toPath());
			} catch (final IOException e1) {

				AbstractTasksResourceTest.LOG.debug("couldn't determine file type from file '{}'", resourceFile.getAbsolutePath());
			}

			if (fileType != null) {

				attributes1.put("filetype", fileType);
			}

			// hint: size is not important to know since its value is skipped in the comparison of actual and expected resource
			attributes1.put("filesize", -1);

			res1.setAttributes(attributes1);

			// upload data resource
			resource = resourcesResourceTestUtils.uploadResource(resourceFile, res1);

			return this;
		}
	}

	protected abstract class PrepareConfiguration {

		protected       Configuration conf1;
		protected       Configuration configuration;
		protected final Resource      resource;

		public PrepareConfiguration(final PrepareResource prepareResource) {

			this.resource = prepareResource.getResource();
		}

		public Configuration getConf1() {

			return conf1;
		}

		public Configuration getConfiguration() {

			return configuration;
		}

		public abstract PrepareConfiguration invoke() throws Exception;
	}
}
