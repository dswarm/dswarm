package org.dswarm.controller.resources.job.test.utils;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.resources.job.test.AbstractTasksResourceTest;

/**
 * @author tgaengler
 */
public class NegativeTasksResourceTest1 extends AbstractTasksResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(NegativeTasksResourceTest1.class);

	private static final String taskJSONFileName          = "dd-538/oai-pmh_marcxml_controller_task.01.json";
	private static final String inputDataResourceFileName = "test-mabxml2-controller.xml";
	private static final String recordTag                 = "datensatz";
	private static final String storageType               = "xml";
	private static final String testPostfix               = "exceotion at xml data";

	public NegativeTasksResourceTest1() {

		super(taskJSONFileName, inputDataResourceFileName, recordTag, storageType, testPostfix);
	}

	@Override
	public void testTaskExecution() throws Exception {

		NegativeTasksResourceTest1.LOG.debug("start '{}' negative task execution test", testPostfix);

		final ObjectNode requestJSON = prepareTask();

		final Response response = target().request(MediaType.APPLICATION_XML_TYPE).post(Entity.json(requestJSON));

		Assert.assertEquals("500 was expected", 500, response.getStatus());

		final String actualResponseMessage = response.readEntity(String.class);

		Assert.assertNotNull(actualResponseMessage);

		final String expectedResponseMessage = "";

		Assert.assertEquals(expectedResponseMessage, actualResponseMessage);

		NegativeTasksResourceTest1.LOG.debug("end '{}' negative task execution test", testPostfix);
	}
}
