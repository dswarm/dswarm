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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import org.dswarm.persistence.util.DMPPersistenceUtil;

public abstract class TasksResourceTestDD538 extends AbstractXMLTasksResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(TasksResourceTestDD538.class);


	protected final String expectedResultXMLFileName;

	public TasksResourceTestDD538(final String taskJSONFileNameArg,
	                              final String inputDataResourceFileNameArg,
	                              final String recordTagArg,
	                              final String storageTypeArg,
	                              final String expectedResultXMLFileNameArg,
	                              final String testPostfixArg,
	                              final boolean utiliseExistingInputSchema) {

		super(taskJSONFileNameArg, inputDataResourceFileNameArg, recordTagArg, storageTypeArg, testPostfixArg, true, utiliseExistingInputSchema);

		expectedResultXMLFileName = expectedResultXMLFileNameArg;
	}

	/**
	 * note: the result XML might not be what one would expect, because it contains for feld->nr multiple values, whereby each value is encapsulated in an own mabxml:nr element (i.e. this isn't conform to the MABXML schema)
	 *
	 * @throws Exception
	 */
	@Override
	public void testTaskExecution() throws Exception {

		TasksResourceTestDD538.LOG.debug("start DD-538 {} task execution test", testPostfix);

		final ObjectNode requestJSON = prepareTask();

		final Response response = target().request(MediaType.APPLICATION_XML_TYPE).post(Entity.json(requestJSON));

		Assert.assertEquals("200 Created was expected", 200, response.getStatus());

		final InputStream actualXMLStream = response.readEntity(InputStream.class);
		Assert.assertNotNull(actualXMLStream);

		final BufferedInputStream bis = new BufferedInputStream(actualXMLStream, 1024);

		final String expectedXML = DMPPersistenceUtil.getResourceAsString(expectedResultXMLFileName);

		// do comparison: check for XML similarity
		final Diff xmlDiff = DiffBuilder
				.compare(Input.fromString(expectedXML).build())
				.withTest(Input.fromStream(bis).build())
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

		TasksResourceTestDD538.LOG.debug("end DD-538 {} task execution test", testPostfix);
	}
}
