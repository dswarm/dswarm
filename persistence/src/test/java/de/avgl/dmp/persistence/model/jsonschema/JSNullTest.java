package de.avgl.dmp.persistence.model.jsonschema;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

public class JSNullTest extends BaseJSTest<JSNull> {

	public JSNullTest() {
		super(JSNull.class);
	}

	@Test
	public void testGetType() throws Exception {

		MatcherAssert.assertThat(obj.getType(), Matchers.equalTo("null"));
	}

	@Test
	public void testGetProperties() throws Exception {

		MatcherAssert.assertThat(obj.getProperties(), Matchers.is(Matchers.nullValue()));
	}

	@Test
	public void testRender() throws Exception {
		// TODO

	}
}
