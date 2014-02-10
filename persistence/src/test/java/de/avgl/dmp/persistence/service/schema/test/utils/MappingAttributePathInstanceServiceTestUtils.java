package de.avgl.dmp.persistence.service.schema.test.utils;

import org.junit.Assert;

import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.MappingAttributePathInstance;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyMappingAttributePathInstance;
import de.avgl.dmp.persistence.service.job.test.utils.FilterServiceTestUtils;
import de.avgl.dmp.persistence.service.schema.MappingAttributePathInstanceService;

public class MappingAttributePathInstanceServiceTestUtils extends
		AttributePathInstanceServiceTestUtils<MappingAttributePathInstanceService, ProxyMappingAttributePathInstance, MappingAttributePathInstance> {

	private final FilterServiceTestUtils	filtersResourceTestUtils;

	public MappingAttributePathInstanceServiceTestUtils() {

		super(MappingAttributePathInstance.class, MappingAttributePathInstanceService.class);

		filtersResourceTestUtils = new FilterServiceTestUtils();
	}

	@Override
	public void compareObjects(final MappingAttributePathInstance expectedObject, final MappingAttributePathInstance actualObject) {

		super.compareObjects(expectedObject, actualObject);

		compareMappingAttributePathInstances(expectedObject, actualObject);
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

	private void compareMappingAttributePathInstances(final MappingAttributePathInstance expectedMappingAttributePathInstance,
			final MappingAttributePathInstance actualMappingAttributePathInstance) {

		if (expectedMappingAttributePathInstance.getFilter() != null) {

			filtersResourceTestUtils.compareObjects(expectedMappingAttributePathInstance.getFilter(), actualMappingAttributePathInstance.getFilter());
		}

		if (expectedMappingAttributePathInstance.getOrdinal() != null) {

			Assert.assertEquals("the ordinals of the mapping attribute path should be equal", expectedMappingAttributePathInstance.getOrdinal(),
					actualMappingAttributePathInstance.getOrdinal());
		}
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
