package de.avgl.dmp.persistence.model.jsonschema;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class JSOtherTest extends BaseJSTest<JSOther> {

	private String	nameSpace;

	public JSOtherTest() {
		super(JSOther.class);
	}

	@Override
	@Before
	public void setUp() throws Exception {

		nameSpace = "http://avantgarde-labs.de/";
		obj = new JSOther("foo", nameSpace);
	}

	@Test
	public void testGetType() throws Exception {

		MatcherAssert.assertThat(obj.getType(), Matchers.equalTo("other"));
	}

	@Test
	public void testGetProperties() throws Exception {

		MatcherAssert.assertThat(obj.getProperties(), Matchers.is(Matchers.nullValue()));
	}

	@Test
	public void testGetNameSpace() throws Exception {

		MatcherAssert.assertThat(obj.getNameSpace(), Matchers.equalTo(nameSpace));
	}

	@Test
	public void testRenderInternal() throws Exception {
		// TODO

	}
}
