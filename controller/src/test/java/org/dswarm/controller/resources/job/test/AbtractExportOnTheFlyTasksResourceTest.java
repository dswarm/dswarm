/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.util.DMPPersistenceUtil;

public abstract class AbtractExportOnTheFlyTasksResourceTest extends AbstractXMLTasksResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(AbtractExportOnTheFlyTasksResourceTest.class);


	protected final String expectedResultFileName;

	private MediaType mediaType;

	public AbtractExportOnTheFlyTasksResourceTest(final String taskJSONFileNameArg,
	                                              final String inputDataResourceFileNameArg,
	                                              final String recordTagArg,
	                                              final String storageTypeArg,
	                                              final String expectedResultFileNameArg,
	                                              final String testPostfixArg,
	                                              final boolean utiliseExistingInputSchema,
	                                              final MediaType mediaTypeArg) {

		super(taskJSONFileNameArg, inputDataResourceFileNameArg, recordTagArg, storageTypeArg, testPostfixArg, true, utiliseExistingInputSchema);

		expectedResultFileName = expectedResultFileNameArg;
		mediaType = mediaTypeArg;
	}

	/**
	 * note: the result XML might not be what one would expect, because it contains for feld->nr multiple values, whereby each value is encapsulated in an own mabxml:nr element (i.e. this isn't conform to the MABXML schema)
	 *
	 * @throws Exception
	 */
	@Override
	public void testTaskExecution() throws Exception {

		AbtractExportOnTheFlyTasksResourceTest.LOG.debug("start on-the-fly export {} task execution test", testPostfix);

		final ObjectNode requestJSON = prepareTask();

		final Response response = target().request(mediaType).post(Entity.json(requestJSON));

		Assert.assertEquals("200 Created was expected", 200, response.getStatus());

		final InputStream actualResultStream = response.readEntity(InputStream.class);
		Assert.assertNotNull(actualResultStream);

		final BufferedInputStream bis = new BufferedInputStream(actualResultStream, 1024);

		final String expectedResult = DMPPersistenceUtil.getResourceAsString(expectedResultFileName);

		doComparison(expectedResult, bis);

		actualResultStream.close();
		bis.close();

		AbtractExportOnTheFlyTasksResourceTest.LOG.debug("end on-the-fly-export {} task execution test", testPostfix);
	}

	protected abstract void doComparison(final String expectedResult,
	                                     final InputStream actualResult);
}
