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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public abstract class AbstractCSVTasksResourceTest extends AbstractTasksResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractCSVTasksResourceTest.class);

	protected final String expectedResultXMLFileName;

	private final String  columnDelimiter;
	private final boolean firstRowIsHeadings;

	public AbstractCSVTasksResourceTest(final String taskJSONFileNameArg, final String inputDataResourceFileNameArg, final String columnDelimiterArg,
			final boolean firstRowIsHeadingsArg, final String expectedResultFileNameArg, final String testPostfixArg, final boolean utiliseExistingInputSchemaArg) {

		super(taskJSONFileNameArg, inputDataResourceFileNameArg, testPostfixArg, true, utiliseExistingInputSchemaArg);

		columnDelimiter = columnDelimiterArg;
		firstRowIsHeadings = firstRowIsHeadingsArg;

		expectedResultXMLFileName = expectedResultFileNameArg;
	}

	/**
	 * note: the result XML might not be what one would expect, because it contains for feld->nr multiple values, whereby each value is encapsulated in an own mabxml:nr element (i.e. this isn't conform to the MABXML schema)
	 *
	 * @throws Exception
	 */
	@Override
	public void testTaskExecution() throws Exception {

		AbstractCSVTasksResourceTest.LOG.debug("start CSV {} task execution test", testPostfix);

		final ObjectNode requestJSON = prepareTask();

		final Response response = target().request(MediaType.APPLICATION_XML_TYPE).post(Entity.json(requestJSON));

		Assert.assertEquals("200 Created was expected", 200, response.getStatus());

		final InputStream actualXMLStream = response.readEntity(InputStream.class);
		Assert.assertNotNull(actualXMLStream);

		final BufferedInputStream bis = new BufferedInputStream(actualXMLStream, 1024);

		final String expectedXML = DMPPersistenceUtil.getResourceAsString(expectedResultXMLFileName);

		// do comparison: check for XML similarity
		final Diff xmlDiff = DiffBuilder
				.compare(Input.fromString(expectedXML))
				.withTest(Input.fromStream(bis))
				.ignoreWhitespace()
				.checkForSimilar()
				.build();

		if (xmlDiff.hasDifferences()) {
			final StringBuilder sb = new StringBuilder("Oi chap, there seem to ba a mishap!");
			for (final Difference difference : xmlDiff.getDifferences()) {
				sb.append('\n').append(difference);
			}
			Assert.fail(sb.toString());
		}

		actualXMLStream.close();
		bis.close();

		AbstractCSVTasksResourceTest.LOG.debug("end CSV {} task execution test", testPostfix);
	}

	@Override protected PrepareConfiguration createPrepareConfiguration(
			PrepareResource prepareResource) {

		return new PrepareXMLConfiguration(prepareResource, columnDelimiter, firstRowIsHeadings, ConfigurationStatics.CSV_STORAGE_TYPE);
	}

	private class PrepareXMLConfiguration extends PrepareConfiguration {

		private final String  columnDelimiter;
		private final boolean firstRowIsHeadings;
		private final String  storageType;

		public PrepareXMLConfiguration(final PrepareResource prepareResource, final String columnDelimiterArg, final boolean firstRowIsHeadingsArg,
				final String storageTypeArg) {

			super(prepareResource);

			columnDelimiter = columnDelimiterArg;
			firstRowIsHeadings = firstRowIsHeadingsArg;
			storageType = storageTypeArg;
		}

		public PrepareConfiguration invoke() throws Exception {

			final String configuration1Uuid = UUIDService.getUUID(Configuration.class.getSimpleName());

			// process input data model
			conf1 = new Configuration(configuration1Uuid);

			conf1.setName("configuration " + testPostfix);
			conf1.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(columnDelimiter));
			conf1.addParameter(ConfigurationStatics.FIRST_ROW_IS_HEADINGS, new TextNode(Boolean.valueOf(firstRowIsHeadings).toString()));
			conf1.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode(storageType));

			final String configurationJSONString = objectMapper.writeValueAsString(conf1);

			if (prepareInputDataResource) {

				// create configuration
				configuration = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);
			} else {

				configuration = configurationsResourceTestUtils.createObjectWithoutComparison(configurationJSONString);
			}

			return this;
		}
	}
}
