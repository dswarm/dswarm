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
package org.dswarm.controller.resources.schema.test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;

import org.dswarm.controller.resources.job.test.utils.FiltersResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.AttributePathsResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.MappingAttributePathInstancesResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyMappingAttributePathInstance;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.job.test.utils.FilterServiceTestUtils;
import org.dswarm.persistence.service.schema.MappingAttributePathInstanceService;
import org.dswarm.persistence.service.schema.test.utils.AttributePathServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.MappingAttributePathInstanceServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class MappingAttributePathInstancesResourceTest
		extends
		BasicResourceTest<MappingAttributePathInstancesResourceTestUtils, MappingAttributePathInstanceServiceTestUtils, MappingAttributePathInstanceService, ProxyMappingAttributePathInstance, MappingAttributePathInstance> {

	private AttributePathsResourceTestUtils attributePathResourceTestUtils;
	private FiltersResourceTestUtils        filterResourceTestUtils;

	public MappingAttributePathInstancesResourceTest() {

		super(MappingAttributePathInstance.class, MappingAttributePathInstanceService.class, "mappingattributepathinstances",
				"mapping_attribute_path_instance.json", new MappingAttributePathInstancesResourceTestUtils());
	}

	@Override
	protected void initObjects() {

		super.initObjects();

		pojoClassResourceTestUtils = new MappingAttributePathInstancesResourceTestUtils();
		attributePathResourceTestUtils = new AttributePathsResourceTestUtils();
		filterResourceTestUtils = new FiltersResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		final AttributePathServiceTestUtils attributePathServiceTestUtils = attributePathResourceTestUtils.getPersistenceServiceTestUtils();
		final AttributePath attributePath = attributePathServiceTestUtils.createAndPersistDefaultObject();

		final FilterServiceTestUtils filterServiceTestUtils = filterResourceTestUtils.getPersistenceServiceTestUtils();
		final Filter filter = filterServiceTestUtils.createAndPersistDefaultObject();

		final String mappingAttributePathInstanceUuid = UUIDService.getUUID(MappingAttributePathInstance.class.getSimpleName());

		final MappingAttributePathInstance mappingAttributePathInstance = new MappingAttributePathInstance(mappingAttributePathInstanceUuid);
		mappingAttributePathInstance.setAttributePath(attributePath);
		mappingAttributePathInstance.setFilter(filter);
		mappingAttributePathInstance.setOrdinal(1);

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(mappingAttributePathInstance);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);
	}

	@Override
	public void testPUTObject() throws Exception {

		super.testPUTObject();
	}

	@Override
	protected MappingAttributePathInstance updateObject(final MappingAttributePathInstance persistedMappingAttributePathInstance) throws Exception {

		final AttributePathServiceTestUtils attributePathServiceTestUtils = attributePathResourceTestUtils.getPersistenceServiceTestUtils();
		final AttributePath newAttributePath = attributePathServiceTestUtils.getDctermsCreatorFoafFamilynameAP();
		final String updatedAttributePathJSONString = objectMapper.writeValueAsString(newAttributePath);
		final ObjectNode updatedAttributePathJSON = objectMapper.readValue(updatedAttributePathJSONString, ObjectNode.class);

		String updatedMappingAttributePathInstanceJSONString = objectMapper.writeValueAsString(persistedMappingAttributePathInstance);
		final ObjectNode updatedMappingAttributePathInstanceJSON = objectMapper.readValue(updatedMappingAttributePathInstanceJSONString,
				ObjectNode.class);

		final Filter oldFilter = persistedMappingAttributePathInstance.getFilter();

		final String filterJSONString = DMPPersistenceUtil.getResourceAsString("filter3.json");
		final ObjectNode updateFilterJSON = objectMapper.readValue(filterJSONString, ObjectNode.class);
		final Filter filter = objectMapper.readValue(filterJSONString, Filter.class);

		// mapping attribute path instance name update
		final String updateMappingAttributePathInstanceNameString = persistedMappingAttributePathInstance.getName() + " update";
		updatedMappingAttributePathInstanceJSON.put("name", updateMappingAttributePathInstanceNameString);
		updatedMappingAttributePathInstanceJSON.set("filter", updateFilterJSON);
		updatedMappingAttributePathInstanceJSON.set("attribute_path", updatedAttributePathJSON);

		updatedMappingAttributePathInstanceJSONString = objectMapper.writeValueAsString(updatedMappingAttributePathInstanceJSON);

		final MappingAttributePathInstance expectedMappingAttributePathInstance = objectMapper.readValue(
				updatedMappingAttributePathInstanceJSONString, MappingAttributePathInstance.class);

		Assert.assertNotNull("the mapping attribute path instance JSON string shouldn't be null", updatedMappingAttributePathInstanceJSONString);

		final MappingAttributePathInstance updatedMappingAttributePathInstance = pojoClassResourceTestUtils.updateObject(
				updatedMappingAttributePathInstanceJSONString, expectedMappingAttributePathInstance);

		Assert.assertEquals("persisted and updated attribute should be equal",
				updatedMappingAttributePathInstance.getAttributePath().getAttributes().iterator().next(),
				updatedMappingAttributePathInstance.getAttributePath().getAttributes().iterator().next());
		Assert.assertNotEquals("old-persisted and updated filter shouldn't be equal", oldFilter, updatedMappingAttributePathInstance.getFilter());
		Assert.assertEquals("persisted and updated mapping attribute path name should be equal", updatedMappingAttributePathInstance.getName(),
				updateMappingAttributePathInstanceNameString);

		return updatedMappingAttributePathInstance;
	}
}
