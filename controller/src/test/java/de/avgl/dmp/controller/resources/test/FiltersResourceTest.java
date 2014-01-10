package de.avgl.dmp.controller.resources.test;

import de.avgl.dmp.controller.resources.test.utils.FiltersResourceTestUtils;
import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.service.job.FilterService;

public class FiltersResourceTest extends BasicResourceTest<FiltersResourceTestUtils, FilterService, Filter, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributesResourceTest.class);

	public FiltersResourceTest() {

		super(Filter.class, FilterService.class, "filters", "filter.json", new FiltersResourceTestUtils());
		
		updateObjectJSONFileName = "filter2.json";
	}
}
