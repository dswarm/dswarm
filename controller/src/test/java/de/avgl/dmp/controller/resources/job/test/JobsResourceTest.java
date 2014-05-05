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
}
