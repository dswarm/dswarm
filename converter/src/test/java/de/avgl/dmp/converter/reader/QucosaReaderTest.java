package de.avgl.dmp.converter.reader;

import de.avgl.dmp.converter.decoder.QucosaDecoder;
import org.culturegraph.mf.framework.DefaultStreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class QucosaReaderTest {

	private QucosaDecoder mockedDecoder;
	private QucosaReader reader;


	@Before
	public void setUp() throws Exception {
		mockedDecoder = mock(QucosaDecoder.class);

		reader = new QucosaReader(mockedDecoder);
	}


	@After
	public void tearDown() throws Exception {
		verifyNoMoreInteractions(mockedDecoder);
	}

	@Test
	public void testSetReceiver() throws Exception {
		final DefaultStreamReceiver receiver = new DefaultStreamReceiver();

		final DefaultStreamReceiver returnedReceiver = reader.setReceiver(receiver);

		verify(mockedDecoder).setReceiver(receiver);
		assertEquals(receiver, returnedReceiver);
	}

	@Test
	public void testProcess() throws Exception {
		final StringReader stringReader = new StringReader("foobar");
		reader.process(stringReader);

		verify(mockedDecoder).process(stringReader);
	}

	@Test
	public void testRead() throws Exception {
		final String string= "foobar";
		reader.read(string);

		verify(mockedDecoder).process(string);
	}

	@Test
	public void testResetStream() throws Exception {
		reader.resetStream();

		verify(mockedDecoder).resetStream();
	}

	@Test
	public void testCloseStream() throws Exception {
		class Dummy {
			public void onCloseStreamCalled() {}
		}
		final Dummy dummy = mock(Dummy.class);

		class MockDecoder extends QucosaDecoder {
			@Override
			protected void onCloseStream() {
				dummy.onCloseStreamCalled();
			}
		}

		QucosaReader closeReader = new QucosaReader(new MockDecoder());
		closeReader.closeStream();

		verify(dummy).onCloseStreamCalled();
	}
}
