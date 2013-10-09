package de.avgl.dmp.persistence.model.jsonschema;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;


public class JSStringTest extends BaseJSTest<JSString> {

	public JSStringTest() {
		super(JSString.class);
	}

	@Test
	public void testGetType() throws Exception {

		assertThat(obj.getType(), equalTo("string"));
	}

	@Test
	public void testGetProperties() throws Exception {

		assertThat(obj.getProperties(), is(nullValue()));
	}
}
