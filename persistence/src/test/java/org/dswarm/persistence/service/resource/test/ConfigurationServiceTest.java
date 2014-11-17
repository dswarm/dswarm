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
package org.dswarm.persistence.service.resource.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.proxy.ProxyConfiguration;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.resource.test.utils.ConfigurationServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;

public class ConfigurationServiceTest extends IDBasicJPAServiceTest<ProxyConfiguration, Configuration, ConfigurationService> {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationServiceTest.class);

	private ConfigurationServiceTestUtils configurationServiceTestUtils;

	public ConfigurationServiceTest() {

		super("configuration", ConfigurationService.class);
	}

	@Override protected void initObjects() {
		super.initObjects();

		configurationServiceTestUtils = new ConfigurationServiceTestUtils();
	}

	@Test
	public void testSimpleObject() throws Exception {

		final Configuration configuration = configurationServiceTestUtils.createDefaultObject();

		final Configuration updatedConfiguration = configurationServiceTestUtils.updateAndCompareObject(configuration, configuration);

		logObjectJSON(updatedConfiguration);
	}
}
