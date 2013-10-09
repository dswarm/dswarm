package de.avgl.dmp.persistence.model.jsonschema;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;

public class JSElementTest {

	private TestJS js;

	private final String expectedName = "foo";
	private final String expectedType = "test";

	private class TestJS extends JSElement {

		private TestJS() {
			super(expectedName);
		}

		private TestJS(String name) {
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
		public JSElement withName(String newName) {
			return new TestJS(newName);
		}
	}

	@Before
	public void setUp() throws Exception {
		js = new TestJS();

	}

	@Test
	public void testGetName() throws Exception {

		assertThat(js.getName(), equalTo(expectedName));
	}

	@Test
	public void testDescription() throws Exception {

		final String description = "description";
		js.setDescription(description);

		assertThat(js.getDescription(), equalTo(description));
		assertThat(js.getDescription(), is(sameInstance(description)));
	}

	@Test
	public void testGetType() throws Exception {

		assertThat(js.getType(), equalTo(expectedType));
	}

	@Test
	public void testGetProperties() throws Exception {

		final List<JSElement> properties = js.getProperties();

		assertThat(properties, is(instanceOf(List.class)));
		assertThat(properties.size(), equalTo(1));
		assertThat(properties, hasItem(js));
	}

	@Test
	public void testWithName() throws Exception {

		final String name = "bar";

		final JSElement withName = js.withName(name);

		assertThat(withName, is(instanceOf(TestJS.class)));

		final TestJS withNameJS = (TestJS) withName;

		assertThat(withNameJS, is(not(sameInstance(js))));

		assertThat(withNameJS.getName(), equalTo(name));
	}

	@Test
	public void testRender() throws Exception {
		//TODO

	}

	@Test
	public void testRenderDescription() throws Exception {
		//TODO

	}

	@Test
	public void testRenderInternal() throws Exception {
		//TODO

	}
}
