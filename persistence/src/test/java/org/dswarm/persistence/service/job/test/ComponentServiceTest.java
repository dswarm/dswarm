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
package org.dswarm.persistence.service.job.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.proxy.ProxyComponent;
import org.dswarm.persistence.service.job.ComponentService;
import org.dswarm.persistence.service.job.test.utils.ComponentServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;

public class ComponentServiceTest extends IDBasicJPAServiceTest<ProxyComponent, Component, ComponentService> {

	private static final Logger LOG = LoggerFactory.getLogger(ComponentServiceTest.class);

	private ComponentServiceTestUtils componentServiceTestUtils;

	public ComponentServiceTest() {

		super("component", ComponentService.class);
	}

	@Override
	protected void initObjects() {
		super.initObjects();

		componentServiceTestUtils = new ComponentServiceTestUtils();
	}

	@Test
	@Override
	public void testSimpleObject() throws Exception {

		final Component component = componentServiceTestUtils.createAndPersistDefaultObject();

		final Component updatedComponent = componentServiceTestUtils.updateAndCompareObject(component, component);

		logObjectJSON(updatedComponent);
	}

	@Test
	public void complexComponentTest() throws Exception {

		final Component component = componentServiceTestUtils.createAndPersistDefaultCompleteObject();

		final Component updatedComponent = componentServiceTestUtils.updateAndCompareObject(component, component);

		logObjectJSON(updatedComponent);
	}
}
