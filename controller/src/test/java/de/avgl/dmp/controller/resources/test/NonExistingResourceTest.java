package de.avgl.dmp.controller.resources.test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

public class NonExistingResourceTest extends ResourceTest {

	private static final org.apache.log4j.Logger	LOG					= org.apache.log4j.Logger.getLogger(NonExistingResourceTest.class);

	private static final String						resourceDirective	= "blablub";

	public NonExistingResourceTest() {

		super(resourceDirective);
	}

	@Test
	public void testNonExistingResource() throws Exception {

		LOG.debug("expecting NotFoundException near this, because we are testing this exception here");

		final Response response = target.path(resourceDirective).request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("404 NOT FOUND was expected", 404, response.getStatus());

		final String responseString = response.readEntity(String.class);
	}
}
