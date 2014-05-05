package de.avgl.dmp.persistence.model.jsonschema;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BaseJSTest<T extends JSElement> {

	protected static ObjectMapper	om;

	private final Class<T>			clazz;

	protected T						obj;

	protected BaseJSTest(final Class<T> clazz) {

		this.clazz = clazz;
	}

	@BeforeClass
	public static void startUp() throws Exception {
		BaseJSTest.om = new ObjectMapper();
	}

	@Before
	public void setUp() throws Exception {
		obj = clazz.getConstructor(String.class).newInstance("foo");

	}

	@Test
	public void testWithName() throws Exception {

		MatcherAssert.assertThat(obj.withName("bar").getName(), Matchers.equalTo("bar"));
		MatcherAssert.assertThat(obj.withName("bar"), Matchers.is(Matchers.instanceOf(clazz)));
	}

}
