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

import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.proxy.ProxyResource;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.service.resource.ResourceService;
import org.dswarm.persistence.service.resource.test.utils.ResourceServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;

public class ResourceServiceTest extends IDBasicJPAServiceTest<ProxyResource, Resource, ResourceService> {

	private static final Logger LOG = LoggerFactory.getLogger(ResourceServiceTest.class);

	private ResourceServiceTestUtils resourceServiceTestUtils;

	public ResourceServiceTest() {

		super("resource", ResourceService.class);
	}

	@Override protected void initObjects() {
		super.initObjects();

		resourceServiceTestUtils = new ResourceServiceTestUtils();
	}

	@Test
	public void testSimpleObject() throws Exception {

		final Resource resource = resourceServiceTestUtils.createAndPersistDefaultObject();

		final Resource updatedResource = resourceServiceTestUtils.updateAndCompareObject(resource, resource);

		logObjectJSON(updatedResource);
	}

	@Test
	public void testComplexResource() throws Exception {

		final Resource resource = resourceServiceTestUtils.createAndPeristDefaultCompleteObject();

		// modify first configuration

		final String modifiedParameterValue = "|";

		final Configuration configuration = resource.getConfigurations().iterator().next();

		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(modifiedParameterValue));

		// replace configuration

		resource.replaceConfiguration(configuration);

		// update resource

		// TODO: note, direct update and compare is not working, since equals and hashCode of Configuration (etc.) is not configured properly - so we cannot ensure an up-to-date ObjectNode for the configuration parameters right now ... :\
		resourceServiceTestUtils.updateObject(resource);
		final Resource updatedResource2 = resourceServiceTestUtils.getObject(resource);
		resourceServiceTestUtils.compareObjects(resource, updatedResource2);

		// create second configuration

		Configuration configuration2 = resourceServiceTestUtils.getConfigurationsServiceTestUtils().getAlternativeConfiguration();

		// add configuration to resource

		updatedResource2.addConfiguration(configuration2);

		// update resource

		final Resource updatedResource3 = resourceServiceTestUtils.updateAndCompareObject(updatedResource2, updatedResource2);

		logObjectJSON(updatedResource3);

	}
}
