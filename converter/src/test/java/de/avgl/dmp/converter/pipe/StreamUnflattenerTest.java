package de.avgl.dmp.converter.pipe;

import com.google.common.base.Joiner;
import org.culturegraph.mf.framework.DefaultStreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class StreamUnflattenerTest {

	private DefaultStreamReceiver mockedReceiver;
	private StreamUnflattener unflattener;

	@Before
	public void setUp() {
		mockedReceiver = mock(DefaultStreamReceiver.class);

		unflattener = new StreamUnflattener();
		unflattener.setReceiver(mockedReceiver);
	}

	@After
	public void tearDown() {
		verifyNoMoreInteractions(mockedReceiver);
	}

	@Test
	public void testGetEntityMarker() throws Exception {
		char expectedDefault = StreamUnflattener.DEFAULT_ENTITY_MARKER;
		char expected = ';';

		StreamUnflattener unflattenerDefault = new StreamUnflattener();
		assertEquals("Zero-Arg constructor should set the default entity marker", expectedDefault, unflattenerDefault.getEntityMarker());

		StreamUnflattener unflattener = new StreamUnflattener(StreamUnflattener.DEFAULT_INITIAL_DISCARD, expected);
		assertEquals("getEntityMarker should return the entity marker set via constructor", expected, unflattener.getEntityMarker());
	}

	@Test
	public void testGetInitialDiscard() throws Exception {

		String expectedDefault = StreamUnflattener.DEFAULT_INITIAL_DISCARD;
		String expected = "foobar";

		StreamUnflattener unflattenerDefault = new StreamUnflattener();
		assertEquals("Zero-Arg constructor should set the default inital discard", expectedDefault, unflattenerDefault.getInitialDiscard());

		StreamUnflattener unflattener = new StreamUnflattener(expected);
		assertEquals("getInitialDiscard should return the initial discard set via constructor", expected, unflattener.getInitialDiscard());
	}

	@Test
	public void testStartRecord() throws Exception {
		final String expected = "foobar";

		unflattener.startRecord(expected);
		verify(mockedReceiver).startRecord(expected);
	}

	@Test
	public void testEndRecord() throws Exception {
		unflattener.endRecord();
		verify(mockedReceiver).endRecord();
	}

	@Test(expected = IllegalStateException.class)
	public void testStartEntity() throws Exception {
		unflattener.startEntity("foobar");
	}

	@Test(expected = IllegalStateException.class)
	public void testEndEntity() throws Exception {
		unflattener.endEntity();
	}

	@Test
	public void testLiteralNoHierarchy() throws Exception {
		final String expectedName = "foo";
		final String expectedValue = "bar";

		unflattener.literal(expectedName, expectedValue);

		verify(mockedReceiver).literal(expectedName, expectedValue);
	}

	@Test
	public void testLiteralOneHierarchy() throws Exception {
		final String expectedName = "foo";
		final String expectedValue = "bar";
		final String expectedEntity = "baz";

		final String inName = Joiner.on(StreamUnflattener.DEFAULT_ENTITY_MARKER).join(expectedEntity, expectedName);

		unflattener.literal(inName, expectedValue);

		verify(mockedReceiver).startEntity(expectedEntity);
		verify(mockedReceiver).literal(expectedName, expectedValue);
	}

	@Test
	public void testLiteralTwoHierarchy() throws Exception {
		final String expectedName = "foo";
		final String expectedValue = "bar";
		final String expectedEntity1 = "baz";
		final String expectedEntity2 = "qux";

		final String inName = Joiner.on(StreamUnflattener.DEFAULT_ENTITY_MARKER).join(expectedEntity1, expectedEntity2, expectedName);

		unflattener.literal(inName, expectedValue);

		verify(mockedReceiver).startEntity(expectedEntity1);
		verify(mockedReceiver).startEntity(expectedEntity2);
		verify(mockedReceiver).literal(expectedName, expectedValue);
	}

	@Test
	public void testLiteralInitialDiscard() throws Exception {
		final String expectedName = "foo";
		final String expectedValue = "bar";
		final String initialDiscard = "baz";
		final String expectedEntity = "qux";

		StreamUnflattener unflattener = new StreamUnflattener(initialDiscard);
		unflattener.setReceiver(mockedReceiver);

		final String inName = Joiner.on(StreamUnflattener.DEFAULT_ENTITY_MARKER).join(initialDiscard, expectedEntity, expectedName);

		unflattener.literal(inName, expectedValue);

		verify(mockedReceiver).startEntity(expectedEntity);
		verify(mockedReceiver).literal(expectedName, expectedValue);
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

		final String inName1 = Joiner.on(StreamUnflattener.DEFAULT_ENTITY_MARKER).join(expectedEntity1, expectedEntity21, expectedName1);
		final String inName2 = Joiner.on(StreamUnflattener.DEFAULT_ENTITY_MARKER).join(expectedEntity1, expectedEntity22, expectedName2);

		unflattener.literal(inName1, expectedValue1);

		verify(mockedReceiver).startEntity(expectedEntity1);
		verify(mockedReceiver).startEntity(expectedEntity21);
		verify(mockedReceiver).literal(expectedName1, expectedValue1);


		unflattener.literal(inName2, expectedValue2);

		verify(mockedReceiver).endEntity();
		verify(mockedReceiver).startEntity(expectedEntity22);
		verify(mockedReceiver).literal(expectedName2, expectedValue2);
	}

	@Test
	public void testLiteralUnchangingHierarchy() throws Exception {
		final String expectedName1 = "foo";
		final String expectedValue1 = "bar";
		final String expectedName2 = "foo2";
		final String expectedValue2 = "bar2";
		final String expectedEntity1 = "baz";
		final String expectedEntity2 = "qux";

		final String inName1 = Joiner.on(StreamUnflattener.DEFAULT_ENTITY_MARKER).join(expectedEntity1, expectedEntity2, expectedName1);
		final String inName2 = Joiner.on(StreamUnflattener.DEFAULT_ENTITY_MARKER).join(expectedEntity1, expectedEntity2, expectedName2);

		unflattener.literal(inName1, expectedValue1);

		verify(mockedReceiver).startEntity(expectedEntity1);
		verify(mockedReceiver).startEntity(expectedEntity2);
		verify(mockedReceiver).literal(expectedName1, expectedValue1);


		unflattener.literal(inName2, expectedValue2);

		// no startEntity calls
		verify(mockedReceiver).literal(expectedName2, expectedValue2);
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

		final String inName1 = Joiner.on(StreamUnflattener.DEFAULT_ENTITY_MARKER).join(expectedEntity1, expectedEntity21, expectedEntity3, expectedName1);
		final String inName2 = Joiner.on(StreamUnflattener.DEFAULT_ENTITY_MARKER).join(expectedEntity1, expectedEntity22, expectedName2);

		unflattener.literal(inName1, expectedValue1);

		verify(mockedReceiver).startEntity(expectedEntity1);
		verify(mockedReceiver).startEntity(expectedEntity21);
		verify(mockedReceiver).startEntity(expectedEntity3);
		verify(mockedReceiver).literal(expectedName1, expectedValue1);


		unflattener.literal(inName2, expectedValue2);

		verify(mockedReceiver, times(2)).endEntity();
		verify(mockedReceiver).startEntity(expectedEntity22);
		verify(mockedReceiver).literal(expectedName2, expectedValue2);
	}

	@Test
	public void testEndRecordWithUnflattenedData() throws Exception {
		final String expectedName = "foo";
		final String expectedValue = "bar";
		final String expectedEntity = "baz";

		final String inName = Joiner.on(StreamUnflattener.DEFAULT_ENTITY_MARKER).join(expectedEntity, expectedName);

		unflattener.literal(inName, expectedValue);

		verify(mockedReceiver).startEntity(expectedEntity);
		verify(mockedReceiver).literal(expectedName, expectedValue);


		unflattener.endRecord();
		verify(mockedReceiver).endEntity();
		verify(mockedReceiver).endRecord();
	}
}
