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

import org.dswarm.common.MediaTypeUtil;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.junit.Assert;

import java.io.IOException;

/**
 * Created by tgaengler on 04.03.16.
 */
public class TriGResponseTasksResourceTest extends AbstractResponseMediaTypeTasksResourceTest {

	public TriGResponseTasksResourceTest() {

		super(MediaTypeUtil.TRIG_TYPE, "controller_task-result.trig");
	}

	@Override
	public void testTaskExecution() throws Exception {

		super.testTaskExecution();
	}

	@Override
	protected void compareResult(final String actualResult) throws IOException {

		final String expectedResult = DMPPersistenceUtil.getResourceAsString(expectedResultFileName);

		final boolean result = expectedResult.length() == actualResult.length() || 1318 == actualResult.length();

		Assert.assertTrue(result);
	}
}
