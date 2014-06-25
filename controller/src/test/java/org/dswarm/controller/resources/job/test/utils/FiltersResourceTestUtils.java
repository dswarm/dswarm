package org.dswarm.controller.resources.job.test.utils;

import org.dswarm.controller.resources.test.utils.BasicDMPResourceTestUtils;
import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.job.proxy.ProxyFilter;
import org.dswarm.persistence.service.job.FilterService;
import org.dswarm.persistence.service.job.test.utils.FilterServiceTestUtils;

public class FiltersResourceTestUtils extends BasicDMPResourceTestUtils<FilterServiceTestUtils, FilterService, ProxyFilter, Filter> {

	public FiltersResourceTestUtils() {

		super("filters", Filter.class, FilterService.class, FilterServiceTestUtils.class);
	}
}
