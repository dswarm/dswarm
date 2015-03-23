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
package org.dswarm.persistence.service.job.test.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONException;
import org.junit.Assert;

import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.job.proxy.ProxyFilter;
import org.dswarm.persistence.service.UUIDService;
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
	public void compareObjects(final Filter expectedFilter, final Filter actualFilter) throws JsonProcessingException, JSONException {

		super.compareObjects(expectedFilter, actualFilter);

		if (expectedFilter.getExpression() != null) {

			Assert.assertNotNull(actualFilter.getExpression());

			Assert.assertEquals("the filter expressions should be equal", expectedFilter.getExpression(), actualFilter.getExpression());
		} else {

			Assert.assertNull(actualFilter.getExpression());
		}
	}

	public Filter createAndPersistFilter(final String name, final String expression) throws Exception {

		final Filter filter = createFilter(name, expression);

		return createAndCompareObject(filter, filter);
	}

	public Filter createFilter(final String name, final String expression) {

		// TODO: think about this?
		final String uuid = UUIDService.getUUID(Filter.class.getSimpleName());

		final Filter filter = new Filter(uuid);

		filter.setName(name);
		filter.setExpression(expression);

		return filter;
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

	@Override
	public Filter createObject(final JsonNode objectDescription) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public Filter createObject(final String identifier) throws Exception {

		return null;
	}

	@Override public Filter createAndPersistDefaultObject() throws Exception {

		final Filter filter = createDefaultObject();

		return createAndCompareObject(filter, filter);
	}

	@Override public Filter createDefaultObject() throws Exception {

		final String filterName = "my filter";

		final String filterExpression = "SELECT ?identifier ?url\n" + "WHERE {\n" + "    ?record custmabxml:metadata ?metadata ;\n"
				+ "            custmabxml:header ?header .\n" + "    ?header custmabxml:identifier ?identifier .\n"
				+ "    ?metadata m:record ?mabrecord .\n" + "    ?mabrecord m:datafield ?dataField .\n" + "    ?dataField m:tag \"088\" ;\n"
				+ "               m:ind1 \"a\" ;\n" + "               m:subfield ?subField .\n" + "    ?subField rdf:value ?url .\n" + "}";

		return createFilter(filterName, filterExpression);
	}
}
