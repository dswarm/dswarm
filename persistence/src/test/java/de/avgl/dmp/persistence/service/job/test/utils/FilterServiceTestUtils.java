package de.avgl.dmp.persistence.service.job.test.utils;

import org.junit.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.model.job.proxy.ProxyFilter;
import de.avgl.dmp.persistence.service.job.FilterService;
import de.avgl.dmp.persistence.service.job.test.FilterServiceTest;
import de.avgl.dmp.persistence.service.test.utils.BasicDMPJPAServiceTestUtils;

public class FilterServiceTestUtils extends BasicDMPJPAServiceTestUtils<FilterService, ProxyFilter, Filter> {

	public FilterServiceTestUtils() {

		super(Filter.class, FilterService.class);
	}

	@Override
	public void compareObjects(final Filter expectedObject, final Filter actualObject) {

		super.compareObjects(expectedObject, actualObject);

		compareFilters(expectedObject, actualObject);
	}
	
	public Filter createFilter(final String name, final String expression) throws Exception {

		final Filter filter = new Filter();

		filter.setName(name);
		filter.setExpression(expression);

		final Filter updatedFilter = createObject(filter, filter);
		
		return updatedFilter;
	}

	private void compareFilters(final Filter expectedFilter, final Filter actualFilter) {

		if (expectedFilter.getExpression() != null) {

			Assert.assertNotNull("the filter expression shouldn't be null", actualFilter.getExpression());
			Assert.assertEquals(expectedFilter.getExpression(), actualFilter.getExpression());
		}
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
