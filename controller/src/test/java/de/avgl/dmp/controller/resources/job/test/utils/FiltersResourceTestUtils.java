package de.avgl.dmp.controller.resources.job.test.utils;

import org.junit.Assert;

import de.avgl.dmp.controller.resources.test.utils.BasicDMPResourceTestUtils;
import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.model.job.proxy.ProxyFilter;
import de.avgl.dmp.persistence.service.job.FilterService;

public class FiltersResourceTestUtils extends BasicDMPResourceTestUtils<FilterService, ProxyFilter, Filter> {

	public FiltersResourceTestUtils() {

		super("filters", Filter.class, FilterService.class);
	}

	@Override
	public void compareObjects(final Filter expectedObject, final Filter actualObject) {

		super.compareObjects(expectedObject, actualObject);

		compareFilters(expectedObject, actualObject);
	}

	private void compareFilters(final Filter expectedFilter, final Filter actualFilter) {

		if (expectedFilter.getExpression() != null) {

			Assert.assertNotNull("the filter expression shouldn't be null", actualFilter.getExpression());
			Assert.assertEquals(expectedFilter.getExpression(), actualFilter.getExpression());
		}
	}

	@Override
	public void reset() {
		
	}
}
