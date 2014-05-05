package de.avgl.dmp.controller.resources.job.test;

import java.io.IOException;

import org.junit.Before;

import de.avgl.dmp.controller.resources.test.ResourceTest;

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
