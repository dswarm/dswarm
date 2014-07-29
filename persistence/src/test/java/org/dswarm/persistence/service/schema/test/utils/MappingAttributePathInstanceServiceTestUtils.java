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
