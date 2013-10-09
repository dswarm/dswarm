package de.avgl.dmp.persistence.model.jsonschema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public abstract class BaseJSTest<T extends JSElement> {

	protected static ObjectMapper om;

	private final Class<T> clazz;

	protected T obj;

	protected BaseJSTest(Class<T> clazz) {

		this.clazz = clazz;
	}

	@BeforeClass
	public static void	startUp() throws Exception {
		om = new ObjectMapper();
	}

	@Before
	public void setUp() throws Exception {
		obj = clazz.getConstructor(String.class).newInstance("foo");

	}

	@Test
	public void testWithName() throws Exception {

		assertThat(obj.withName("bar").getName(), equalTo("bar"));
		assertThat(obj.withName("bar"), is(instanceOf(clazz)));
	}

}
