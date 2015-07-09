package org.dswarm.controller.resources.job.test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tgaengler
 */
public abstract class AbstractNegativeTasksResourceTest extends AbstractTasksResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractNegativeTasksResourceTest.class);

	private final String expectedResponseMessage;

	public AbstractNegativeTasksResourceTest(final String taskJSONFileNameArg, final String inputDataResourceFileNameArg, final String recordTagArg,
			final String storageTypeArg, final String testPostfixArg, final String expectedResponseMessageArg) {

		super(taskJSONFileNameArg, inputDataResourceFileNameArg, recordTagArg, storageTypeArg, testPostfixArg);

		expectedResponseMessage = expectedResponseMessageArg;
	}

	@Override
	public void testTaskExecution() throws Exception {

		AbstractNegativeTasksResourceTest.LOG.debug("start '{}' negative task execution test", testPostfix);

		final ObjectNode requestJSON = prepareTask();

		final Response response = target().request(MediaType.APPLICATION_XML_TYPE).post(Entity.json(requestJSON));

		// note: we cannot change the response code, when an error occurs, because the async response, will be opened only once
		Assert.assertEquals("200 was expected", 200, response.getStatus());

		final String actualResponseMessage = response.readEntity(String.class);

		Assert.assertNotNull(actualResponseMessage);

		Assert.assertEquals(expectedResponseMessage, actualResponseMessage);

		AbstractNegativeTasksResourceTest.LOG.debug("end '{}' negative task execution test", testPostfix);
	}
}
