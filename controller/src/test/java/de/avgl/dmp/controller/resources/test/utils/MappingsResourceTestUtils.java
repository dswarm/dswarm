package de.avgl.dmp.controller.resources.test.utils;

import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.model.job.proxy.ProxyMapping;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.service.job.MappingService;

public class MappingsResourceTestUtils extends BasicDMPResourceTestUtils<MappingService, ProxyMapping, Mapping> {

	private final ComponentsResourceTestUtils		componentsResourceTestUtils;

	private final AttributePathsResourceTestUtils	attributePathsResourceTestUtils;

	private final FiltersResourceTestUtils			filtersResourceTestUtils;

	private final Set<Long>							checkedExpectedAttributePaths	= Sets.newHashSet();

	private final Set<Long>							checkedActualAttributePaths		= Sets.newHashSet();

	private final Set<Long>							checkedExpectedFilters			= Sets.newHashSet();

	private final Set<Long>							checkedActualFilters			= Sets.newHashSet();

	public MappingsResourceTestUtils() {

		super("mappings", Mapping.class, MappingService.class);

		componentsResourceTestUtils = new ComponentsResourceTestUtils();
		attributePathsResourceTestUtils = new AttributePathsResourceTestUtils();
		filtersResourceTestUtils = new FiltersResourceTestUtils();
	}

	@Override
	public void compareObjects(final Mapping expectedObject, final Mapping actualObject) {

		super.compareObjects(expectedObject, actualObject);

		compareMappings(expectedObject, actualObject);
	}

	private void compareMappings(final Mapping expectedMapping, final Mapping actualMapping) {

		if (expectedMapping.getTransformation() != null) {

			componentsResourceTestUtils.compareObjects(expectedMapping.getTransformation(), actualMapping.getTransformation());
		}

		if (expectedMapping.getInputAttributePaths() != null && !expectedMapping.getInputAttributePaths().isEmpty()) {

			final Set<AttributePath> actualInputAttributePaths = actualMapping.getInputAttributePaths();

			Assert.assertNotNull("input attribute paths of actual mapping '" + actualMapping.getId() + "' shouldn't be null",
					actualInputAttributePaths);
			Assert.assertFalse("input attribute paths of actual mapping '" + actualMapping.getId() + "' shouldn't be empty",
					actualInputAttributePaths.isEmpty());

			final Map<Long, AttributePath> actualInputAttributePathsMap = Maps.newHashMap();

			for (final AttributePath actualInputAttributePath : actualInputAttributePaths) {

				if (checkAttributePath(actualInputAttributePath, checkedActualAttributePaths)) {

					continue;
				}

				actualInputAttributePathsMap.put(actualInputAttributePath.getId(), actualInputAttributePath);
			}

			final Set<AttributePath> uncheckedExpectedInputAttributePaths = Sets.newHashSet();

			for (final AttributePath expectedInputAttributePath : expectedMapping.getInputAttributePaths()) {

				if (checkAttributePath(expectedInputAttributePath, checkedExpectedAttributePaths)) {

					continue;
				}

				uncheckedExpectedInputAttributePaths.add(expectedInputAttributePath);

			}

			attributePathsResourceTestUtils.compareObjects(uncheckedExpectedInputAttributePaths, actualInputAttributePathsMap);
		}

		if (expectedMapping.getOutputAttributePath() != null) {

			if (!checkAttributePath(expectedMapping.getOutputAttributePath(), checkedExpectedAttributePaths) && !checkAttributePath(actualMapping.getOutputAttributePath(), checkedActualAttributePaths)) {

				attributePathsResourceTestUtils.compareObjects(expectedMapping.getOutputAttributePath(), actualMapping.getOutputAttributePath());
			}

		}

		if (expectedMapping.getInputFilter() != null) {

			if (!checkFilter(expectedMapping.getInputFilter(), checkedExpectedFilters) && !checkFilter(actualMapping.getInputFilter(), checkedActualFilters)) {

				filtersResourceTestUtils.compareObjects(expectedMapping.getInputFilter(), actualMapping.getInputFilter());
			}
		}

		if (expectedMapping.getOutputFilter() != null) {

			if (!checkFilter(expectedMapping.getOutputFilter(), checkedExpectedFilters) && !checkFilter(actualMapping.getOutputFilter(), checkedActualFilters)) {

				filtersResourceTestUtils.compareObjects(expectedMapping.getOutputFilter(), actualMapping.getOutputFilter());
			}
		}
	}

	private boolean checkAttributePath(final AttributePath attributePath, final Set<Long> checkedAttributePaths) {

		if (attributePath != null && attributePath.getId() != null) {

			if (checkedAttributePaths.contains(attributePath.getId())) {

				// attribute path was already checked

				return true;
			}

			checkedAttributePaths.add(attributePath.getId());
		}

		return false;
	}

	private boolean checkFilter(final Filter filter, final Set<Long> checkedFilters) {

		if (filter != null && filter.getId() != null) {

			if (checkedFilters.contains(filter.getId())) {

				// filter was already checked

				return true;
			}

			checkedFilters.add(filter.getId());
		}

		return false;
	}

	@Override
	public void reset() {

		checkedExpectedAttributePaths.clear();
		checkedActualAttributePaths.clear();
		checkedExpectedFilters.clear();
		checkedActualFilters.clear();
		
		attributePathsResourceTestUtils.reset();
		filtersResourceTestUtils.reset();
		componentsResourceTestUtils.reset();
	}
}
