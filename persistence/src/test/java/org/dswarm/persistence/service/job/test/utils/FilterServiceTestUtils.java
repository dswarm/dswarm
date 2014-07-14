package org.dswarm.persistence.service.job.test.utils;

import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.job.proxy.ProxyFilter;
import org.dswarm.persistence.service.job.FilterService;
import org.dswarm.persistence.service.test.utils.BasicDMPJPAServiceTestUtils;
import org.junit.Assert;

public class FilterServiceTestUtils extends BasicDMPJPAServiceTestUtils<FilterService, ProxyFilter, Filter> {

	public FilterServiceTestUtils() {

		super(Filter.class, FilterService.class);
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert the filter expressions are equal.
	 */
	@Override
	public void compareObjects(final Filter expectedFilter, final Filter actualFilter) {

		super.compareObjects(expectedFilter, actualFilter);

		Assert.assertEquals("the filter expressions should be equal", expectedFilter.getExpression(), actualFilter.getExpression());
	}

	public Filter createFilter(final String name, final String expression) throws Exception {

		final Filter filter = new Filter();

		filter.setName(name);
		filter.setExpression(expression);

		final Filter updatedFilter = createObject(filter, filter);

		return updatedFilter;
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name and expression of the filter.
	 */
	@Override
	protected Filter prepareObjectForUpdate(final Filter objectWithUpdates, final Filter object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		object.setExpression(objectWithUpdates.getExpression());

		return object;
	}

	@Override
	public void reset() {

	}
}
