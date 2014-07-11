package org.dswarm.persistence.service.job.test.utils;

import java.util.Map;
import java.util.Set;

import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.proxy.ProxyMapping;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.service.job.MappingService;
import org.dswarm.persistence.service.schema.test.utils.MappingAttributePathInstanceServiceTestUtils;
import org.dswarm.persistence.service.test.utils.BasicDMPJPAServiceTestUtils;
import org.junit.Assert;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MappingServiceTestUtils extends BasicDMPJPAServiceTestUtils<MappingService, ProxyMapping, Mapping> {

	private final ComponentServiceTestUtils						componentServiceTestUtils;

	private final MappingAttributePathInstanceServiceTestUtils	mappingAttributePathInstanceServiceTestUtils;

	private final Set<Long>										checkedExpectedAttributePaths	= Sets.newHashSet();

	private final Set<Long>										checkedActualAttributePaths		= Sets.newHashSet();

	public MappingServiceTestUtils() {

		super(Mapping.class, MappingService.class);

		componentServiceTestUtils = new ComponentServiceTestUtils();
		mappingAttributePathInstanceServiceTestUtils = new MappingAttributePathInstanceServiceTestUtils();
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert that both mappings either have no or equal (transformation) components, see
	 * {@link ComponentServiceTestUtils#compareObjects(Component, Component)}. <br />
	 * Assert that both mappings either have no or equal input attribute paths, see {@link
	 * BasicJPAServiceTestUtils.compareObjects(Set, Map)}. <br />
	 * Assert that both mappings either have no or equal output attribute paths, see {@link
	 * BasicJPAServiceTestUtils.compareObjects(Set, Map)}. <br />
	 */
	@Override
	public void compareObjects(final Mapping expectedMapping, final Mapping actualMapping) {

		super.compareObjects(expectedMapping, actualMapping);

		// transformation (component)
		if (expectedMapping.getTransformation() == null) {

			Assert.assertNull("the actual mapping '" + actualMapping.getId() + "' shouldn't have a transformation", actualMapping.getTransformation());

		} else {
			componentServiceTestUtils.compareObjects(expectedMapping.getTransformation(), actualMapping.getTransformation());
		}

		// input attribute paths
		if (expectedMapping.getInputAttributePaths() == null || expectedMapping.getInputAttributePaths().isEmpty()) {

			final boolean actualMappingHasNoInputAttributePaths = (actualMapping.getInputAttributePaths() == null || actualMapping
					.getInputAttributePaths().isEmpty());
			Assert.assertTrue("actual mapping '" + actualMapping.getId() + "' shouldn't have input attribute paths",
					actualMappingHasNoInputAttributePaths);

		} else { // !null && !empty

			final Set<MappingAttributePathInstance> actualInputAttributePaths = actualMapping.getInputAttributePaths();

			Assert.assertNotNull("input attribute paths of actual mapping '" + actualMapping.getId() + "' shouldn't be null",
					actualInputAttributePaths);
			Assert.assertFalse("input attribute paths of actual mapping '" + actualMapping.getId() + "' shouldn't be empty",
					actualInputAttributePaths.isEmpty());

			final Map<Long, MappingAttributePathInstance> actualInputAttributePathsMap = Maps.newHashMap();

			for (final MappingAttributePathInstance actualInputAttributePath : actualInputAttributePaths) {

				if (checkAttributePath(actualInputAttributePath, checkedActualAttributePaths)) {

					// SR FIXME why can we be sure we dont need to check this actualInputAttributePath? the last reset() may have
					// been a while ago.
					continue;
				}

				actualInputAttributePathsMap.put(actualInputAttributePath.getId(), actualInputAttributePath);
			}

			final Set<MappingAttributePathInstance> uncheckedExpectedInputAttributePaths = Sets.newHashSet();

			for (final MappingAttributePathInstance expectedInputAttributePath : expectedMapping.getInputAttributePaths()) {

				if (checkAttributePath(expectedInputAttributePath, checkedExpectedAttributePaths)) {

					// SR FIXME why can we be sure we dont need to check this expectedInputAttributePath? the last reset() may
					// have been a while ago.
					continue;
				}

				uncheckedExpectedInputAttributePaths.add(expectedInputAttributePath);

			}

			mappingAttributePathInstanceServiceTestUtils.compareObjects(uncheckedExpectedInputAttributePaths, actualInputAttributePathsMap);
		}

		// output attribute paths
		if (expectedMapping.getOutputAttributePath() == null) {

			Assert.assertNull("actual mapping '" + actualMapping.getId() + "' shouldn't have an output attribute path",
					actualMapping.getOutputAttributePath());

			// SR FIXME why can we skip here?
		} else if (!checkAttributePath(expectedMapping.getOutputAttributePath(), checkedExpectedAttributePaths)
				&& !checkAttributePath(actualMapping.getOutputAttributePath(), checkedActualAttributePaths)) {

			mappingAttributePathInstanceServiceTestUtils.compareObjects(expectedMapping.getOutputAttributePath(),
					actualMapping.getOutputAttributePath());

		}
	}

	private boolean checkAttributePath(final MappingAttributePathInstance attributePath, final Set<Long> checkedAttributePaths) {

		if (attributePath != null && attributePath.getId() != null) {

			if (checkedAttributePaths.contains(attributePath.getId())) {

				// attribute path was already checked

				return true;
			}

			checkedAttributePaths.add(attributePath.getId());
		}

		return false;
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, transformation (component), input attribute paths and output attribute path of the mapping.
	 */
	@Override
	protected Mapping prepareObjectForUpdate(final Mapping objectWithUpdates, final Mapping object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		object.setTransformation(objectWithUpdates.getTransformation());
		object.setInputAttributePaths(objectWithUpdates.getInputAttributePaths());
		object.setOutputAttributePath(objectWithUpdates.getOutputAttributePath());

		return object;
	}

	@Override
	public void reset() {

		checkedExpectedAttributePaths.clear();
		checkedActualAttributePaths.clear();
		// checkedExpectedFilters.clear();
		// checkedActualFilters.clear();

		mappingAttributePathInstanceServiceTestUtils.reset();
		// filtersResourceTestUtils.reset();
		componentServiceTestUtils.reset();
	}
}
