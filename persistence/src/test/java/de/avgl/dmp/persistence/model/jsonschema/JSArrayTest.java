package de.avgl.dmp.persistence.model.jsonschema;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;


public class JSArrayTest extends BaseJSTest<JSArray> {

	private JSElement item;

	public JSArrayTest() {
		super(JSArray.class);

	}

	@Before
	public void setUp() throws Exception {

		item = new JSString("bar");
		obj = new JSArray(item);
	}

	@Test
	public void testGetType() throws Exception {

		assertThat(obj.getType(), equalTo("array"));
	}

	@Test
	public void testGetProperties() throws Exception {

		assertThat(obj.getProperties(), is(nullValue()));
	}

	@Test
	public void testGetItem() throws Exception {

		assertThat(obj.getItem(), is(sameInstance(item)));
	}

	@Test
	public void testRenderInternal() throws Exception {
		//TODO

	}
}
