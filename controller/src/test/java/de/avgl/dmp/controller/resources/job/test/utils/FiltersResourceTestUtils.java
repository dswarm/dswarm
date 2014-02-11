package de.avgl.dmp.controller.resources.job.test.utils;

import de.avgl.dmp.controller.resources.test.utils.BasicDMPResourceTestUtils;
import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.model.job.proxy.ProxyFilter;
import de.avgl.dmp.persistence.service.job.FilterService;
import de.avgl.dmp.persistence.service.job.test.utils.FilterServiceTestUtils;

public class FiltersResourceTestUtils extends BasicDMPResourceTestUtils<FilterServiceTestUtils, FilterService, ProxyFilter, Filter> {

	public FiltersResourceTestUtils() {

		super("filters", Filter.class, FilterService.class, FilterServiceTestUtils.class);
	}
}
