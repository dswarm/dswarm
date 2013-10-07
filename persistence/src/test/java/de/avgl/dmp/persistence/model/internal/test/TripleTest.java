package de.avgl.dmp.persistence.model.internal.test;

import org.junit.Before;
import org.junit.Test;

import de.avgl.dmp.persistence.model.internal.Triple;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class TripleTest {
	private String v1 = "foo";
	private Integer v2 = 42;
	private TestObject v3 = new TestObject();

	private class TestObject {
		@Override
		public int hashCode() {
			return 9;
		}
	}

	@Before
	public void setUp() throws Exception {

	}

	@Test(expected = NullPointerException.class)
	public void testConstructor1() throws Exception {
		@SuppressWarnings("UnusedDeclaration") Triple<String, Integer, TestObject> fail = new Triple<String, Integer, TestObject>(null, v2, v3);
	}

	@Test(expected = NullPointerException.class)
	public void testConstructor2() throws Exception {
		@SuppressWarnings("UnusedDeclaration") Triple<String, Integer, TestObject> fail = new Triple<String, Integer, TestObject>(v1, null, v3);
	}

	@Test(expected = NullPointerException.class)
	public void testConstructor3() throws Exception {
		@SuppressWarnings("UnusedDeclaration") Triple<String, Integer, TestObject> fail = new Triple<String, Integer, TestObject>(v1, v2, null);
	}

	@Test
	public void testV1() throws Exception {
		Triple<String, Integer, TestObject> expected = new Triple<String, Integer, TestObject>(v1, v2, v3);

		assertThat(expected.v1(), sameInstance(v1));
	}

	@Test
	public void testV2() throws Exception {
		Triple<String, Integer, TestObject> expected = new Triple<String, Integer, TestObject>(v1, v2, v3);

		assertThat(expected.v2(), sameInstance(v2));
	}

	@Test
	public void testV3() throws Exception {
		Triple<String, Integer, TestObject> expected = new Triple<String, Integer, TestObject>(v1, v2, v3);

		assertThat(expected.v3(), sameInstance(v3));
	}

	@Test
	public void testEquals() throws Exception {
		Triple<String, Integer, TestObject> triple1 = new Triple<String, Integer, TestObject>(v1, v2, v3);
		Triple<String, Integer, TestObject> triple2 = new Triple<String, Integer, TestObject>(v1, v2, v3);

		Triple<String, Integer, TestObject> triple3 = new Triple<String, Integer, TestObject>("bar", v2, v3);
		Triple<String, Integer, TestObject> triple4 = new Triple<String, Integer, TestObject>(v1, 21, v3);
		Triple<String, Integer, TestObject> triple5 = new Triple<String, Integer, TestObject>(v1, v2, new TestObject());


		assertTrue(triple1.equals(triple1));
		assertFalse(triple1.equals(null));
		assertFalse(triple1.equals(v3));

		assertTrue(triple1.equals(triple2));

		assertFalse(triple1.equals(triple3));
		assertFalse(triple1.equals(triple4));
		assertFalse(triple1.equals(triple5));
	}

	@Test
	public void testHashCode() throws Exception {
		int expected = 17 * (31 * v1.hashCode() + v2.hashCode()) + v3.hashCode();
		Triple<String, Integer, TestObject> triple = new Triple<String, Integer, TestObject>(v1, v2, v3);

		assertThat(triple.hashCode(), equalTo(expected));
	}

	@Test
	public void testTriple() throws Exception {

		Triple<String, Integer, TestObject> expected = new Triple<String, Integer, TestObject>(v1, v2, v3);
		Triple<String, Integer, TestObject> actual = Triple.triple(v1, v2, v3);

		assertThat(expected, equalTo(actual));
	}
}
