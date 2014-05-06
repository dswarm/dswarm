package de.avgl.dmp.converter.pipe;

import org.culturegraph.mf.framework.DefaultStreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class StreamJsonCollapserTest {

	private DefaultStreamReceiver	mockedReceiver;
	private StreamJsonCollapser		collapser;

	@Before
	public void setUp() throws Exception {
		mockedReceiver = Mockito.mock(DefaultStreamReceiver.class);

		collapser = new StreamJsonCollapser();
		collapser.setReceiver(mockedReceiver);
	}

	@After
	public void tearDown() throws Exception {
		Mockito.verifyNoMoreInteractions(mockedReceiver);
	}

	@Test
	public void testStartRecord() throws Exception {
		final String expected = "foobar";

		collapser.startRecord(expected);
		Mockito.verify(mockedReceiver).startRecord(expected);
	}

	@Test
	public void testEndRecord() throws Exception {
		collapser.endRecord();
		Mockito.verify(mockedReceiver).endRecord();
	}

	@Test
	public void testStartEntity() throws Exception {
		final String expected = "foobar";

		collapser.startEntity(expected);
		Mockito.verify(mockedReceiver).startEntity(expected);
	}

	@Test
	public void testEndEntity() throws Exception {
		collapser.endRecord();
		Mockito.verify(mockedReceiver).endRecord();
	}

	@Test
	public void testLiteral() throws Exception {
		collapser.literal("foo", "bar");
		// should not invoke stuff
	}

	@Test
	public void testFlushWithNoArray() throws Exception {
		final String recordIdentifier = "test";
		final String expectedName1 = "foo1";
		final String expectedName2 = "foo2";
		final String expectedValue = "bar";

		collapser.startRecord(recordIdentifier);

		collapser.literal(expectedName1, expectedValue);
		collapser.literal(expectedName2, expectedValue);

		collapser.endRecord();

		Mockito.verify(mockedReceiver).startRecord(recordIdentifier);
		Mockito.verify(mockedReceiver).literal(expectedName1, expectedValue);
		Mockito.verify(mockedReceiver).literal(expectedName2, expectedValue);
		Mockito.verify(mockedReceiver).endRecord();
	}

	@Test
	public void testFlushWithAnArray() throws Exception {
		final String recordIdentifier = "test";
		final String expectedName = "foo";
		final String expectedValue1 = "bar";
		final String expectedValue2 = "baz";

		collapser.startRecord(recordIdentifier);

		collapser.literal(expectedName, expectedValue1);
		collapser.literal(expectedName, expectedValue2);

		collapser.endRecord();

		Mockito.verify(mockedReceiver).startRecord(recordIdentifier);
		Mockito.verify(mockedReceiver).startEntity(expectedName + StreamJsonCollapser.ARRAY_MARKER);
		Mockito.verify(mockedReceiver).literal(expectedName, expectedValue1);
		Mockito.verify(mockedReceiver).literal(expectedName, expectedValue2);
		Mockito.verify(mockedReceiver).endEntity();
		Mockito.verify(mockedReceiver).endRecord();
	}

}
