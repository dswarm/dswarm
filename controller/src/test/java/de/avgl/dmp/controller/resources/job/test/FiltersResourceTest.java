package de.avgl.dmp.controller.resources.job.test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

import de.avgl.dmp.controller.resources.job.test.utils.FiltersResourceTestUtils;
import de.avgl.dmp.controller.resources.schema.test.AttributesResourceTest;
import de.avgl.dmp.controller.resources.test.BasicResourceTest;
import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.model.job.proxy.ProxyFilter;
import de.avgl.dmp.persistence.service.job.FilterService;
import de.avgl.dmp.persistence.service.job.test.utils.FilterServiceTestUtils;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

import java.io.IOException;

public class FiltersResourceTest extends
		BasicResourceTest<FiltersResourceTestUtils, FilterServiceTestUtils, FilterService, ProxyFilter, Filter, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributesResourceTest.class);

	public FiltersResourceTest() {

		super(Filter.class, FilterService.class, "filters", "filter.json", new FiltersResourceTestUtils());

		updateObjectJSONFileName = "filter2.json";
	}

	@Test
	public void exceptionTest() throws IOException {

		final String finalJSONString = DMPPersistenceUtil.getResourceAsString("broken_filter.json");

		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(finalJSONString));

		Assert.assertEquals("500", 500, response.getStatus());

		final String entity = response.readEntity(String.class);

		Assert.assertEquals("{\"status\":\"nok\",\"status_code\":500,\"error\":\"something went wrong while deserializing the Filter JSON string\"}", entity);
	}
}
