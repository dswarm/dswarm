package org.dswarm.controller.resources.job.test;

import java.io.IOException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dswarm.controller.resources.job.test.utils.FiltersResourceTestUtils;
import org.dswarm.controller.resources.test.BasicResourceTest;
import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.job.proxy.ProxyFilter;
import org.dswarm.persistence.service.job.FilterService;
import org.dswarm.persistence.service.job.test.utils.FilterServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.junit.Assert;
import org.junit.Test;

public class FiltersResourceTest extends
		BasicResourceTest<FiltersResourceTestUtils, FilterServiceTestUtils, FilterService, ProxyFilter, Filter, Long> {

	public FiltersResourceTest() {

		super(Filter.class, FilterService.class, "filters", "filter.json", new FiltersResourceTestUtils());

		updateObjectJSONFileName = "filter2.json";
	}

	@Test
	public void exceptionTest() throws IOException {

		final String finalJSONString = DMPPersistenceUtil.getResourceAsString("broken_filter.json");

		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(finalJSONString));

		Assert.assertEquals("500", 500, response.getStatus());

		final String entity = response.readEntity(String.class);

		Assert.assertEquals("{\"status\":\"nok\",\"status_code\":500,\"error\":\"something went wrong while deserializing the Filter JSON string\"}",
				entity);
	}
}
