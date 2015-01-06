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
package org.dswarm.persistence.service.schema.test.utils;

import org.junit.Assert;

import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyMappingAttributePathInstance;
import org.dswarm.persistence.service.job.test.utils.FilterServiceTestUtils;
import org.dswarm.persistence.service.schema.MappingAttributePathInstanceService;

public class MappingAttributePathInstanceServiceTestUtils extends
		AttributePathInstanceServiceTestUtils<MappingAttributePathInstanceService, ProxyMappingAttributePathInstance, MappingAttributePathInstance> {

	private final FilterServiceTestUtils	filtersResourceTestUtils;

	public MappingAttributePathInstanceServiceTestUtils() {

		super(MappingAttributePathInstance.class, MappingAttributePathInstanceService.class);

		filtersResourceTestUtils = new FilterServiceTestUtils();
	}

	/**
	 * {@inheritDoc}<br />
	 * Assert ordinals are equal. <br />
	 * Assert either no filters are present or filters are equal, see
	 * {@link FilterServiceTestUtils#compareObjects(Filter, Filter)} for details.
	 * 
	 * @param expectedMappingAttributePathInstance
	 * @param actualMappingAttributePathInstance
	 */
	@Override
	public void compareObjects(final MappingAttributePathInstance expectedMappingAttributePathInstance,
			final MappingAttributePathInstance actualMappingAttributePathInstance) {

		super.compareObjects(expectedMappingAttributePathInstance, actualMappingAttributePathInstance);

		Assert.assertEquals("the ordinals of the mapping attribute path should be equal", expectedMappingAttributePathInstance.getOrdinal(),
				actualMappingAttributePathInstance.getOrdinal());

		if (expectedMappingAttributePathInstance.getFilter() == null) {

			Assert.assertNull("the actual mapping attribute path instance should not have a filter", actualMappingAttributePathInstance.getFilter());

		} else {

			filtersResourceTestUtils.compareObjects(expectedMappingAttributePathInstance.getFilter(), actualMappingAttributePathInstance.getFilter());
		}
	}

	public MappingAttributePathInstance createMappingAttributePathInstance(final String name, final AttributePath attributePath,
			final Integer ordinal, final Filter filter) throws Exception {

		final MappingAttributePathInstance mappingAttributePathInstance = new MappingAttributePathInstance();

		mappingAttributePathInstance.setName(name);
		mappingAttributePathInstance.setAttributePath(attributePath);
		mappingAttributePathInstance.setOrdinal(ordinal);
		mappingAttributePathInstance.setFilter(filter);

		final MappingAttributePathInstance updatedMappingAttributePathInstance = createObject(mappingAttributePathInstance,
				mappingAttributePathInstance);

		Assert.assertNotNull(updatedMappingAttributePathInstance.getId());

		return updatedMappingAttributePathInstance;
	}

	/**
	 * {@inheritDoc}<br/>
	 */
	@Override
	protected MappingAttributePathInstance prepareObjectForUpdate(final MappingAttributePathInstance objectWithUpdates,
			final MappingAttributePathInstance object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		object.setFilter(objectWithUpdates.getFilter());
		object.setOrdinal(objectWithUpdates.getOrdinal());

		return object;
	}

	@Override
	public void reset() {

		super.reset();
		filtersResourceTestUtils.reset();
	}
}
