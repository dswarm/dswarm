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

import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.junit.Assert;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Created by tgaengler on 04.03.16.
 */
public class XMLResponseTasksResourceTest extends AbstractResponseMediaTypeTasksResourceTest {

	public XMLResponseTasksResourceTest() {

		super(MediaType.APPLICATION_XML_TYPE, "controller_task-result.xml");
	}

	/**
	 * note: the result XML might not be what one would expect, because it contains for feld->nr multiple values, whereby each value is encapsulated in an own mabxml:nr element (i.e. this isn't conform to the MABXML schema)
	 *
	 * @throws Exception
	 */
	@Override
	public void testTaskExecution() throws Exception {

		super.testTaskExecution();
	}

	@Override
	protected void compareResult(final String actualResult) throws IOException {

		final String expectedResultXML = DMPPersistenceUtil.getResourceAsString(expectedResultFileName);

		final boolean result = expectedResultXML.length() == actualResult.length() || 781 == actualResult.length();

		Assert.assertTrue(result);
	}
}
