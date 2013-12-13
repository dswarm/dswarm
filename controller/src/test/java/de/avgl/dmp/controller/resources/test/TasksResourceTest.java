package de.avgl.dmp.controller.resources.test;

import java.io.IOException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.model.job.Task;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class TasksResourceTest extends ResourceTest {

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(TasksResourceTest.class);

	private String									taskJSONString		= null;
	private Task								expectedTask		= null;

	private final ObjectMapper						objectMapper			= injector.getInstance(ObjectMapper.class);

	public TasksResourceTest() {
		
		super("tasks");
	}

	@Before
	public void prepare() throws IOException {
		
		taskJSONString = DMPPersistenceUtil.getResourceAsString("task.json");

		expectedTask = objectMapper.readValue(taskJSONString, Task.class);
	}

	@Test
	public void testTaskExecution() throws Exception {

		LOG.debug("start task execution test");
		
		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(taskJSONString));

		Assert.assertEquals("201 Created was expected", 201, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		System.out.println("response = '" + responseString + "'");

		LOG.debug("end task execution test");
	}
}
