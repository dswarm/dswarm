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
package org.dswarm.controller.resources.resource.test;

import org.dswarm.controller.resources.resource.test.utils.ConfigurationsResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.proxy.ProxyConfiguration;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.resource.test.utils.ConfigurationServiceTestUtils;

public class ConfigurationsResourceTest
		extends
		BasicResourceTest<ConfigurationsResourceTestUtils, ConfigurationServiceTestUtils, ConfigurationService, ProxyConfiguration, Configuration, Long> {

	public ConfigurationsResourceTest() {

		super(Configuration.class, ConfigurationService.class, "configurations", "controller_configuration.json",
				new ConfigurationsResourceTestUtils());

		updateObjectJSONFileName = "configuration2.json";
	}

	@Override protected void initObjects() {
		super.initObjects();

		pojoClassResourceTestUtils = new ConfigurationsResourceTestUtils();
	}
}
