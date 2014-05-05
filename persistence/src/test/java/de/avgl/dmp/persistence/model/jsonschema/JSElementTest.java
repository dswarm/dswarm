package de.avgl.dmp.persistence.model.jsonschema;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

public class JSElementTest {

	private TestJS			js;

	private final String	expectedName	= "foo";
	private final String	expectedType	= "test";

	private class TestJS extends JSElement {

		private TestJS() {
			super(expectedName);
		}

		private TestJS(final String name) {
			super(name);
		}

		@Override
		public String getType() {
			return expectedType;
		}

		@Override
		public List<JSElement> getProperties() {
			final ArrayList<JSElement> jsElements = new ArrayList<JSElement>(1);
			jsElements.add(this);

			return jsElements;
		}

		@Override
		public JSElement withName(final String newName) {
			return new TestJS(newName);
		}
	}

	@Before
	public void setUp() throws Exception {
		js = new TestJS();

	}

	@Test
	public void testGetName() throws Exception {

		MatcherAssert.assertThat(js.getName(), Matchers.equalTo(expectedName));
	}

	@Test
	public void testDescription() throws Exception {

		final String description = "description";
		js.setDescription(description);

		MatcherAssert.assertThat(js.getDescription(), Matchers.equalTo(description));
		MatcherAssert.assertThat(js.getDescription(), Is.is(Matchers.sameInstance(description)));
	}

	@Test
	public void testGetType() throws Exception {

		MatcherAssert.assertThat(js.getType(), Matchers.equalTo(expectedType));
	}

	@Test
	public void testGetProperties() throws Exception {

		final List<JSElement> properties = js.getProperties();

		MatcherAssert.assertThat(properties, Is.is(Matchers.instanceOf(List.class)));
		MatcherAssert.assertThat(properties.size(), Matchers.equalTo(1));
		MatcherAssert.assertThat(properties, Matchers.hasItem(js));
	}

	@Test
	public void testWithName() throws Exception {

		final String name = "bar";

		final JSElement withName = js.withName(name);

		MatcherAssert.assertThat(withName, Is.is(Matchers.instanceOf(TestJS.class)));

		final TestJS withNameJS = (TestJS) withName;

		MatcherAssert.assertThat(withNameJS, Is.is(Matchers.not(Matchers.sameInstance(js))));

		MatcherAssert.assertThat(withNameJS.getName(), Matchers.equalTo(name));
	}

	@Test
	public void testRender() throws Exception {
		// TODO

	}

	@Test
	public void testRenderDescription() throws Exception {
		// TODO

	}

	@Test
	public void testRenderInternal() throws Exception {
		// TODO

	}
}
