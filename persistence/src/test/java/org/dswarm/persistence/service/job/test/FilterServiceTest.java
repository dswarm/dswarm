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
package org.dswarm.persistence.service.job.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.job.proxy.ProxyFilter;
import org.dswarm.persistence.service.job.FilterService;
import org.dswarm.persistence.service.job.test.utils.FilterServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;

public class FilterServiceTest extends IDBasicJPAServiceTest<ProxyFilter, Filter, FilterService> {

	private static final Logger LOG = LoggerFactory.getLogger(FilterServiceTest.class);

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	private FilterServiceTestUtils filterServiceTestUtils;

	public FilterServiceTest() {

		super("filter", FilterService.class);
	}

	@Override protected void initObjects() {
		super.initObjects();

		filterServiceTestUtils = new FilterServiceTestUtils();
	}

	@Test
	public void testSimpleFilter() throws Exception {

		final Filter filter = filterServiceTestUtils.getDefaultObject();

		final Filter updatedFilter = filterServiceTestUtils.updateObject(filter, filter);

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedFilter);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		FilterServiceTest.LOG.debug("filter json: " + json);
	}
}
