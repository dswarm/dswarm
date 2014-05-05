package de.avgl.dmp.persistence.model.jsonschema;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

public class JSObjectTest extends BaseJSTest<JSObject> {

	public JSObjectTest() {
		super(JSObject.class);
	}

	@Override
	public void setUp() throws Exception {

		final List<JSElement> jsElements = new ArrayList<JSElement>(2);

		// TODO

		obj = new JSObject("foo", jsElements);
	}

	@Test
	public void testAdd() throws Exception {
		// TODO

	}

	@Test
	public void testAddAll() throws Exception {
		// TODO

	}

	@Test
	public void testIterator() throws Exception {
		// TODO

	}

	@Test
	public void testGetType() throws Exception {

		MatcherAssert.assertThat(obj.getType(), Matchers.equalTo("object"));
	}

	@Test
	public void testGetProperties() throws Exception {
		// TODO

	}

	@Test
	public void testRenderInternal() throws Exception {
		// TODO

	}
}
