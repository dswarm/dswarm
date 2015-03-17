/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import org.junit.Assert;

import org.dswarm.controller.resources.job.test.utils.ComponentsResourceTestUtils;
import org.dswarm.controller.resources.job.test.utils.FiltersResourceTestUtils;
import org.dswarm.controller.resources.job.test.utils.MappingsResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.AttributePathsResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.proxy.ProxyMapping;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.service.job.MappingService;
import org.dswarm.persistence.service.job.test.utils.MappingServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.AttributePathServiceTestUtils;

public class MappingsResourceTest extends
		BasicResourceTest<MappingsResourceTestUtils, MappingServiceTestUtils, MappingService, ProxyMapping, Mapping> {

	private ComponentsResourceTestUtils componentsResourceTestUtils;

	private AttributePathsResourceTestUtils attributePathsResourceTestUtils;
	private FiltersResourceTestUtils        filtersResourceTestUtils;

	public MappingsResourceTest() {

		super(Mapping.class, MappingService.class, "mappings", "mapping.json", new MappingsResourceTestUtils());
	}

	@Override
	protected void initObjects() {

		super.initObjects();

		pojoClassResourceTestUtils = new MappingsResourceTestUtils();
		componentsResourceTestUtils = new ComponentsResourceTestUtils();
		attributePathsResourceTestUtils = new AttributePathsResourceTestUtils();
		filtersResourceTestUtils = new FiltersResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		final MappingServiceTestUtils mappingServiceTestUtils = pojoClassResourceTestUtils.getPersistenceServiceTestUtils();
		final Mapping mapping = mappingServiceTestUtils.createDefaultObject();

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(mapping);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);

//		System.out.println("mapping json = '" + objectJSONString + "'");
	}

	@Override
	protected Mapping updateObject(final Mapping persistedMapping) throws Exception {

		// update name
		persistedMapping.setName(persistedMapping.getName() + " update");

		// update filter
		final Filter updateFilter = filtersResourceTestUtils.createObject("filter2.json");

		// update component
		final Component updateTransformationComponent = componentsResourceTestUtils.createObject("component.json");

		persistedMapping.setTransformation(updateTransformationComponent);

		final AttributePathServiceTestUtils attributePathServiceTestUtils = attributePathsResourceTestUtils.getPersistenceServiceTestUtils();

		// update input attribute paths
		final AttributePath updateInputAttributePath = attributePathServiceTestUtils.getDctermsCreatorFoafNameAP();

		persistedMapping.getInputAttributePaths().iterator().next().setAttributePath(updateInputAttributePath);
		persistedMapping.getInputAttributePaths().iterator().next().setFilter(updateFilter);

		final String updateMappingJSONString = objectMapper.writeValueAsString(persistedMapping);
		expectedObject = objectMapper.readValue(updateMappingJSONString, pojoClass);

		final Mapping updateMapping = pojoClassResourceTestUtils.updateObject(updateMappingJSONString, expectedObject);

		Assert.assertNotNull("the mapping JSON string shouldn't be null", updateMapping);
		Assert.assertEquals("mapping name shoud be equal", expectedObject.getName(), updateMapping.getName());
		Assert.assertEquals(updateMapping.getInputAttributePaths().iterator().next().getAttributePath(), updateInputAttributePath);
		Assert.assertEquals(updateMapping.getInputAttributePaths().iterator().next().getFilter(), updateFilter);

		return updateMapping;
	}
}
