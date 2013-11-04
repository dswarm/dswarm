package de.avgl.dmp.persistence.mapping.test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.mapping.JsonToPojoMapper;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.ComponentType;
import de.avgl.dmp.persistence.model.job.Job;
import de.avgl.dmp.persistence.model.job.Parameter;
import de.avgl.dmp.persistence.model.job.Payload;
import de.avgl.dmp.persistence.model.job.Transformation;

public class JsonToPojoMapperTest extends GuicedTest {

	private Transformation	transformation	= null;

	private String			jsonInput		= null;

	@Before
	public void setUp() throws Exception {

		InputStream in = this.getClass().getClassLoader().getResourceAsStream("transformation-test-input.json");
		jsonInput = new String(ByteStreams.toByteArray(in), "UTF-8");

		final Parameter parameter1 = new Parameter();
		parameter1.setName("pattern");
		parameter1.setType("regexp");
		parameter1.setData("::");

		final Parameter parameter2 = new Parameter();
		parameter2.setName("with");
		parameter2.setType("text");
		parameter2.setData("?");

		final Parameter parameter3 = new Parameter();
		parameter3.setName("source");
		parameter3.setType("text");
		parameter3.setData("050m");

		final Parameter parameter4 = new Parameter();
		parameter4.setName("name");
		parameter4.setType("text");
		parameter4.setData("entry name");

		final Parameter parameter5 = new Parameter();
		parameter5.setName("value");
		parameter5.setType("text");
		parameter5.setData("entry value");

		final Parameter parameter6 = new Parameter();
		parameter6.setName("entry");
		parameter6.setRepeat(true);
		parameter6.setParameters(new HashMap<String, Parameter>() {

			{
				put("name", parameter4);
				put("value", parameter5);
			}
		});

		Payload payload1 = new Payload();
		payload1.setName("trim");

		Payload payload2 = new Payload();
		payload2.setName("unique");

		Payload payload3 = new Payload();
		payload3.setName("replace");
		payload3.setParameters(new HashMap<String, Parameter>() {

			{
				put("pattern", parameter1);
				put("with", parameter2);
			}
		});

		Payload payload4 = new Payload();
		payload4.setName("lookup");
		payload4.setParameters(new HashMap<String, Parameter>() {

			{
				put("source", parameter3);
				put("entry", parameter6);
			}
		});

		final Component component1 = new Component();
		component1.setId("con_124:fun_1");
		component1.setName("trim");
		component1.setType(ComponentType.FUNCTION);
		component1.setPayload(payload1);

		final Component component2 = new Component();
		component2.setId("con_124:fun_2");
		component2.setName("unique");
		component2.setType(ComponentType.FUNCTION);
		component2.setPayload(payload2);

		final Component component3 = new Component();
		component3.setId("con_124:fun_3");
		component3.setName("replace");
		component3.setType(ComponentType.FUNCTION);
		component3.setPayload(payload3);

		final Component component4 = new Component();
		component4.setId("con_124:fun_4");
		component4.setName("lookup");
		component4.setType(ComponentType.FUNCTION);
		component4.setPayload(payload4);

		transformation = new Transformation();
		transformation.setId("con_124");
		transformation.setName("Publisher Mapping");

		final List<Component> components = Lists.newArrayList();
		components.add(component1);
		components.add(component2);
		components.add(component3);
		components.add(component4);

		transformation.setComponents(components);
	}

	@Test
	public void testApply() throws Exception {

		final Job job = injector.getInstance(JsonToPojoMapper.class).toJob(jsonInput);

		Assert.assertEquals("There should only be one transformation", 1, job.getTransformations().size());

		final Transformation actual = job.getTransformations().get(0);

		Assert.assertEquals("The Transformation was not constructed correctly", transformation, actual);
	}
}
