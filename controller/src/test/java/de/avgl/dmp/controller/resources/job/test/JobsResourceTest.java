package de.avgl.dmp.controller.resources.job.test;

import java.io.IOException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.avgl.dmp.controller.resources.test.ResourceTest;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class JobsResourceTest extends ResourceTest {

	private String							jobJSONString	= null;

	public JobsResourceTest() {

		super("jobs");
	}

	@Before
	public void prepare() throws IOException {

		// jobJSONString = DMPPersistenceUtil.getResourceAsString("complex-request.json");
	}

	@Ignore
	@Test
	public void testExecuteJobDemo() throws Exception {

		Response response = target("/demo").request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(jobJSONString));

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseString = response.readEntity(String.class);

		final String expected = DMPPersistenceUtil.getResourceAsString("complex-result.json");

		Assert.assertEquals("POST responses are not equal", expected, responseString);
	}
}
