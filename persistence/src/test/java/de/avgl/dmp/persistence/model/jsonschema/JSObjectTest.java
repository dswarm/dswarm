package de.avgl.dmp.persistence.model.jsonschema;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class JSObjectTest extends BaseJSTest<JSObject> {

	public JSObjectTest() {
		super(JSObject.class);
	}

	@Override
	public void setUp() throws Exception {

		List<JSElement> jsElements = new ArrayList<JSElement>(2);

		// TODO

		obj = new JSObject("foo", jsElements);
	}

	@Test
	public void testAdd() throws Exception {
		//TODO

	}



	@Test
	public void testAddAll() throws Exception {
		//TODO

	}

	@Test
	public void testIterator() throws Exception {
		//TODO

	}

	@Test
	public void testGetType() throws Exception {

		assertThat(obj.getType(), equalTo("object"));
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
