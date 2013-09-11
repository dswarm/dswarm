package de.avgl.dmp.converter.sink;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ObjectBufferWriterTest {
	private ObjectBufferWriter sink;

	private static final String STRING = "foobar";

	@Before
	public void setUp() throws Exception {
		sink = new ObjectBufferWriter();
	}

	@Test
	public void testProcess() throws Exception {
		sink.process(STRING);

		assertEquals(STRING, sink.toString());
	}

	@Test
	public void testResetStream() throws Exception {
		sink.process(null);
		sink.resetStream();

		assertEquals("", sink.toString());
	}

	@Test
	public void testCloseStream() throws Exception {
		sink.process(null);
		sink.closeStream();

		assertEquals("", sink.toString());
	}

	@Test
	public void testToString() throws Exception {
		sink.process("foo");
		sink.process("bar");
		sink.process("baz");

		assertEquals("foobarbaz", sink.toString());
	}
}
