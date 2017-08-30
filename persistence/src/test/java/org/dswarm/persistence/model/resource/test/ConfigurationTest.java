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
package org.dswarm.persistence.model.resource.test;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * Created by tgaengler on 21/05/14.
 */
public class ConfigurationTest extends GuicedTest {

	private final ObjectMapper	objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	@Test
	public void simpleConfigurationTest() throws IOException {

		final String configurationJSONString = DMPPersistenceUtil.getResourceAsString("configuration.json");

		final Configuration configuration = objectMapper.readValue(configurationJSONString, Configuration.class);

		final ObjectNode configurationJSON = objectMapper.readValue(configurationJSONString, ObjectNode.class);

		final String finalExpectedConfigurationJSONString = objectMapper.writeValueAsString(configurationJSON);
		final String finalActualConfigurationJSONString = objectMapper.writeValueAsString(configuration);

		Assert.assertEquals(finalExpectedConfigurationJSONString.length(), finalActualConfigurationJSONString.length());
	}
}
