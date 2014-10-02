/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.service.job.test.utils;

import org.junit.Assert;

import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.job.proxy.ProxyFilter;
import org.dswarm.persistence.service.job.FilterService;
import org.dswarm.persistence.service.test.utils.BasicDMPJPAServiceTestUtils;

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
