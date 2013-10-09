package de.avgl.dmp.persistence.model.jsonschema;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class JSNullTest extends BaseJSTest<JSNull> {

	public JSNullTest() {
		super(JSNull.class);
	}

	@Test
	public void testGetType() throws Exception {

		assertThat(obj.getType(), equalTo("null"));
	}

	@Test
	public void testGetProperties() throws Exception {

		assertThat(obj.getProperties(), is(nullValue()));
	}


	@Test
	public void testRender() throws Exception {
		//TODO

	}
}
