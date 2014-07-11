package org.dswarm.controller.resources.job.test;

import java.io.IOException;

import org.dswarm.controller.resources.test.ResourceTest;
import org.junit.Before;

public class JobsResourceTest extends ResourceTest {

	private final String	jobJSONString	= null;

	public JobsResourceTest() {

		super("jobs");
	}

	@Before
	public void prepare() throws IOException {

		// jobJSONString = DMPPersistenceUtil.getResourceAsString("complex-request.json");
	}
}
