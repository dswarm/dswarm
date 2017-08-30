/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.common.MediaTypeUtil;
import org.dswarm.graph.json.util.Util;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;

public class TasksResourceTestDD1319GDMSimpleJSON extends AbtractExportOnTheFlyTasksResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(TasksResourceTestDD1319GDMSimpleJSON.class);

	private static final ObjectMapper indentObjectMapper = Util.getJSONObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);

	public TasksResourceTestDD1319GDMSimpleJSON() {

		super("dd-1387/task.json",
				"dd-1387/input.xml",
				"Publisher",
				ConfigurationStatics.XML_STORAGE_TYPE,
				"dd-1319/gdm-compact.json.result.json",
				"01",
				true,
				MediaTypeUtil.GDM_SIMPLE_JSON_TYPE);
	}

	@Override
	protected void doComparison(final String expectedResult,
	                            final InputStream actualResult) {

		try {

			final ArrayNode actualResultJSONArray = indentObjectMapper.readValue(actualResult, ArrayNode.class);
			final String actualResultJSON = indentObjectMapper.writeValueAsString(actualResultJSONArray);

			Assert.assertEquals(expectedResult.length(), actualResultJSON.length());
		} catch (final IOException e) {

			LOG.error("some thing went wrong", e);

			Assert.assertTrue(false);
		}
	}
}
