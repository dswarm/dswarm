/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.persistence.model.internal.test;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.dswarm.persistence.model.types.Triple;

public class TripleTest {

	private final String		v1	= "foo";
	private final Integer		v2	= 42;
	private final TestObject	v3	= new TestObject();

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
		@SuppressWarnings("UnusedDeclaration")
		final Triple<String, Integer, TestObject> fail = new Triple<String, Integer, TestObject>(null, v2, v3);
	}

	@Test(expected = NullPointerException.class)
	public void testConstructor2() throws Exception {
		@SuppressWarnings("UnusedDeclaration")
		final Triple<String, Integer, TestObject> fail = new Triple<String, Integer, TestObject>(v1, null, v3);
	}

	@Test(expected = NullPointerException.class)
	public void testConstructor3() throws Exception {
		@SuppressWarnings("UnusedDeclaration")
		final Triple<String, Integer, TestObject> fail = new Triple<String, Integer, TestObject>(v1, v2, null);
	}

	@Test
	public void testV1() throws Exception {
		final Triple<String, Integer, TestObject> expected = new Triple<String, Integer, TestObject>(v1, v2, v3);

		Assert.assertThat(expected.v1(), CoreMatchers.sameInstance(v1));
	}

	@Test
	public void testV2() throws Exception {
		final Triple<String, Integer, TestObject> expected = new Triple<String, Integer, TestObject>(v1, v2, v3);

		Assert.assertThat(expected.v2(), CoreMatchers.sameInstance(v2));
	}

	@Test
	public void testV3() throws Exception {
		final Triple<String, Integer, TestObject> expected = new Triple<String, Integer, TestObject>(v1, v2, v3);

		Assert.assertThat(expected.v3(), CoreMatchers.sameInstance(v3));
	}

	@Test
	public void testEquals() throws Exception {
		final Triple<String, Integer, TestObject> triple1 = new Triple<String, Integer, TestObject>(v1, v2, v3);
		final Triple<String, Integer, TestObject> triple2 = new Triple<String, Integer, TestObject>(v1, v2, v3);

		final Triple<String, Integer, TestObject> triple3 = new Triple<String, Integer, TestObject>("bar", v2, v3);
		final Triple<String, Integer, TestObject> triple4 = new Triple<String, Integer, TestObject>(v1, 21, v3);
		final Triple<String, Integer, TestObject> triple5 = new Triple<String, Integer, TestObject>(v1, v2, new TestObject());

		Assert.assertTrue(triple1.equals(triple1));
		Assert.assertFalse(triple1.equals(null));
		Assert.assertFalse(triple1.equals(v3));

		Assert.assertTrue(triple1.equals(triple2));

		Assert.assertFalse(triple1.equals(triple3));
		Assert.assertFalse(triple1.equals(triple4));
		Assert.assertFalse(triple1.equals(triple5));
	}

	@Test
	public void testHashCode() throws Exception {
		final int expected = 17 * (31 * v1.hashCode() + v2.hashCode()) + v3.hashCode();
		final Triple<String, Integer, TestObject> triple = new Triple<String, Integer, TestObject>(v1, v2, v3);

		Assert.assertThat(triple.hashCode(), CoreMatchers.equalTo(expected));
	}

	@Test
	public void testTriple() throws Exception {

		final Triple<String, Integer, TestObject> expected = new Triple<String, Integer, TestObject>(v1, v2, v3);
		final Triple<String, Integer, TestObject> actual = Triple.triple(v1, v2, v3);

		Assert.assertThat(expected, CoreMatchers.equalTo(actual));
	}
}
