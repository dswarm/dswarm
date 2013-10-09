package de.avgl.dmp.persistence.model.jsonschema;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;


public class JSOtherTest extends BaseJSTest<JSOther> {

	private String nameSpace;

	public JSOtherTest() {
		super(JSOther.class);
	}

	@Before
	public void setUp() throws Exception {

		nameSpace = "http://avantgarde-labs.de/";
		obj = new JSOther("foo", nameSpace);
	}


	@Test
	public void testGetType() throws Exception {

		assertThat(obj.getType(), equalTo("other"));
	}

	@Test
	public void testGetProperties() throws Exception {

		assertThat(obj.getProperties(), is(nullValue()));
	}

	@Test
	public void testGetNameSpace() throws Exception {

		assertThat(obj.getNameSpace(), equalTo(nameSpace));
	}

	@Test
	public void testRenderInternal() throws Exception {
		//TODO

	}
}
