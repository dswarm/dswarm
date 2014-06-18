package de.avgl.dmp.controller.resources.resource.test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.avgl.dmp.controller.resources.test.ResourceTest;

public class NonExistingResourceTest extends ResourceTest {

	private static final Logger	LOG					= LoggerFactory.getLogger(NonExistingResourceTest.class);

	private static final String	resourceDirective	= "blablub";

	public NonExistingResourceTest() {

		super(NonExistingResourceTest.resourceDirective);
	}

	@Test
	public void testNonExistingResource() throws Exception {

		NonExistingResourceTest.LOG.debug("expecting NotFoundException near this, because we are testing this exception here");

		final Response response = target().request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("404 NOT FOUND was expected", 404, response.getStatus());

		final String responseString = response.readEntity(String.class);
	}
}
