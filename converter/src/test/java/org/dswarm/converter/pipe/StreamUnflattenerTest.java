/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.converter.pipe;

import com.google.common.base.Joiner;
import org.culturegraph.mf.framework.DefaultStreamReceiver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class StreamUnflattenerTest {

	private DefaultStreamReceiver	mockedReceiver;
	private StreamUnflattener		unflattener;

	@Before
	public void setUp() {
		mockedReceiver = Mockito.mock(DefaultStreamReceiver.class);

		unflattener = new StreamUnflattener();
		unflattener.setReceiver(mockedReceiver);
	}

	@After
	public void tearDown() {
		Mockito.verifyNoMoreInteractions(mockedReceiver);
	}

	@Test
	public void testGetEntityMarker() throws Exception {
		final char expectedDefault = StreamUnflattener.DEFAULT_ATTRIBUTE_DELIMITER;
		final char expected = ';';

		final StreamUnflattener unflattenerDefault = new StreamUnflattener();
		Assert.assertEquals("Zero-Arg constructor should set the default entity marker", expectedDefault, unflattenerDefault.getEntityMarker());

		final StreamUnflattener unflattener = new StreamUnflattener(StreamUnflattener.DEFAULT_INITIAL_DISCARD, expected);
		Assert.assertEquals("getEntityMarker should return the entity marker set via constructor", expected, unflattener.getEntityMarker());
	}

	@Test
	public void testGetInitialDiscard() throws Exception {

		final String expectedDefault = StreamUnflattener.DEFAULT_INITIAL_DISCARD;
		final String expected = "foobar";

		final StreamUnflattener unflattenerDefault = new StreamUnflattener();
		Assert.assertEquals("Zero-Arg constructor should set the default inital discard", expectedDefault, unflattenerDefault.getInitialDiscard());

		final StreamUnflattener unflattener = new StreamUnflattener(expected);
		Assert.assertEquals("getInitialDiscard should return the initial discard set via constructor", expected, unflattener.getInitialDiscard());
	}

	@Test
	public void testStartRecord() throws Exception {
		final String expected = "foobar";

		unflattener.startRecord(expected);
		Mockito.verify(mockedReceiver).startRecord(expected);
	}

	@Test
	public void testEndRecord() throws Exception {
		unflattener.endRecord();
		Mockito.verify(mockedReceiver).endRecord();
	}

//	@Test(expected = IllegalStateException.class)
//	public void testStartEntity() throws Exception {
//		unflattener.startEntity("foobar");
//	}

//	@Test(expected = IllegalStateException.class)
//	public void testEndEntity() throws Exception {
//		unflattener.endEntity();
//	}

	@Test
	public void testLiteralNoHierarchy() throws Exception {
		final String expectedName = "foo";
		final String expectedValue = "bar";

		unflattener.literal(expectedName, expectedValue);

		Mockito.verify(mockedReceiver).literal(expectedName, expectedValue);
	}

	@Test
	public void testLiteralOneHierarchy() throws Exception {
		final String expectedName = "foo";
		final String expectedValue = "bar";
		final String expectedEntity = "baz";

		final String inName = Joiner.on(StreamUnflattener.DEFAULT_ATTRIBUTE_DELIMITER).join(expectedEntity, expectedName);

		unflattener.literal(inName, expectedValue);

		Mockito.verify(mockedReceiver).startEntity(expectedEntity);
		Mockito.verify(mockedReceiver).literal(expectedName, expectedValue);
	}

	@Test
	public void testLiteralTwoHierarchy() throws Exception {
		final String expectedName = "foo";
		final String expectedValue = "bar";
		final String expectedEntity1 = "baz";
		final String expectedEntity2 = "qux";

		final String inName = Joiner.on(StreamUnflattener.DEFAULT_ATTRIBUTE_DELIMITER).join(expectedEntity1, expectedEntity2, expectedName);

		unflattener.literal(inName, expectedValue);

		Mockito.verify(mockedReceiver).startEntity(expectedEntity1);
		Mockito.verify(mockedReceiver).startEntity(expectedEntity2);
		Mockito.verify(mockedReceiver).literal(expectedName, expectedValue);
	}

	@Test
	public void testLiteralInitialDiscard() throws Exception {
		final String expectedName = "foo";
		final String expectedValue = "bar";
		final String initialDiscard = "baz";
		final String expectedEntity = "qux";

		final StreamUnflattener unflattener = new StreamUnflattener(initialDiscard);
		unflattener.setReceiver(mockedReceiver);

		final String inName = Joiner.on(StreamUnflattener.DEFAULT_ATTRIBUTE_DELIMITER).join(initialDiscard, expectedEntity, expectedName);

		unflattener.literal(inName, expectedValue);

		Mockito.verify(mockedReceiver).startEntity(expectedEntity);
		Mockito.verify(mockedReceiver).literal(expectedName, expectedValue);
	}

	@Test
	public void testLiteralChangingHierarchy() throws Exception {
		final String expectedName1 = "foo";
		final String expectedValue1 = "bar";
		final String expectedName2 = "foo2";
		final String expectedValue2 = "bar2";
		final String expectedEntity1 = "baz";
		final String expectedEntity21 = "qux";
		final String expectedEntity22 = "quux";

		final String inName1 = Joiner.on(StreamUnflattener.DEFAULT_ATTRIBUTE_DELIMITER).join(expectedEntity1, expectedEntity21, expectedName1);
		final String inName2 = Joiner.on(StreamUnflattener.DEFAULT_ATTRIBUTE_DELIMITER).join(expectedEntity1, expectedEntity22, expectedName2);

		unflattener.literal(inName1, expectedValue1);

		Mockito.verify(mockedReceiver).startEntity(expectedEntity1);
		Mockito.verify(mockedReceiver).startEntity(expectedEntity21);
		Mockito.verify(mockedReceiver).literal(expectedName1, expectedValue1);

		unflattener.literal(inName2, expectedValue2);

		Mockito.verify(mockedReceiver).endEntity();
		Mockito.verify(mockedReceiver).startEntity(expectedEntity22);
		Mockito.verify(mockedReceiver).literal(expectedName2, expectedValue2);
	}

	@Test
	public void testLiteralUnchangingHierarchy() throws Exception {
		final String expectedName1 = "foo";
		final String expectedValue1 = "bar";
		final String expectedName2 = "foo2";
		final String expectedValue2 = "bar2";
		final String expectedEntity1 = "baz";
		final String expectedEntity2 = "qux";

		final String inName1 = Joiner.on(StreamUnflattener.DEFAULT_ATTRIBUTE_DELIMITER).join(expectedEntity1, expectedEntity2, expectedName1);
		final String inName2 = Joiner.on(StreamUnflattener.DEFAULT_ATTRIBUTE_DELIMITER).join(expectedEntity1, expectedEntity2, expectedName2);

		unflattener.literal(inName1, expectedValue1);

		Mockito.verify(mockedReceiver).startEntity(expectedEntity1);
		Mockito.verify(mockedReceiver).startEntity(expectedEntity2);
		Mockito.verify(mockedReceiver).literal(expectedName1, expectedValue1);

		unflattener.literal(inName2, expectedValue2);

		// no startEntity calls
		Mockito.verify(mockedReceiver).literal(expectedName2, expectedValue2);
	}

	@Test
	public void testLiteralDecreasingHierarchy() throws Exception {
		final String expectedName1 = "foo";
		final String expectedValue1 = "bar";
		final String expectedName2 = "foo2";
		final String expectedValue2 = "bar2";
		final String expectedEntity1 = "baz";
		final String expectedEntity21 = "qux";
		final String expectedEntity3 = "corge";
		final String expectedEntity22 = "quux";

		final String inName1 = Joiner.on(StreamUnflattener.DEFAULT_ATTRIBUTE_DELIMITER).join(expectedEntity1, expectedEntity21, expectedEntity3,
				expectedName1);
		final String inName2 = Joiner.on(StreamUnflattener.DEFAULT_ATTRIBUTE_DELIMITER).join(expectedEntity1, expectedEntity22, expectedName2);

		unflattener.literal(inName1, expectedValue1);

		Mockito.verify(mockedReceiver).startEntity(expectedEntity1);
		Mockito.verify(mockedReceiver).startEntity(expectedEntity21);
		Mockito.verify(mockedReceiver).startEntity(expectedEntity3);
		Mockito.verify(mockedReceiver).literal(expectedName1, expectedValue1);

		unflattener.literal(inName2, expectedValue2);

		Mockito.verify(mockedReceiver, Mockito.times(2)).endEntity();
		Mockito.verify(mockedReceiver).startEntity(expectedEntity22);
		Mockito.verify(mockedReceiver).literal(expectedName2, expectedValue2);
	}

	@Test
	public void testEndRecordWithUnflattenedData() throws Exception {
		final String expectedName = "foo";
		final String expectedValue = "bar";
		final String expectedEntity = "baz";

		final String inName = Joiner.on(StreamUnflattener.DEFAULT_ATTRIBUTE_DELIMITER).join(expectedEntity, expectedName);

		unflattener.literal(inName, expectedValue);

		Mockito.verify(mockedReceiver).startEntity(expectedEntity);
		Mockito.verify(mockedReceiver).literal(expectedName, expectedValue);

		unflattener.endRecord();
		Mockito.verify(mockedReceiver).endEntity();
		Mockito.verify(mockedReceiver).endRecord();
	}
}
