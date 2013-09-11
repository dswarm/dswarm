package de.avgl.dmp.converter.decoder;

import org.culturegraph.mf.framework.DefaultStreamReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

import java.io.InputStream;

import static org.mockito.Mockito.*;

public class QucosaDecoderTest {

	private final String recordName = "foobar";

	private InputStream inputStream;
	private InputSource inputSource;
	private DefaultStreamReceiver mockedReceiver;
	private QucosaDecoder qucosaDecoder;

	@Before
	public void setUp() throws Exception {
		inputStream = this.getClass().getClassLoader().getResourceAsStream("qucosa_record.xml");
		inputSource = new InputSource(inputStream);

		mockedReceiver = mock(DefaultStreamReceiver.class);

		qucosaDecoder = new QucosaDecoder(recordName);
		qucosaDecoder.setReceiver(mockedReceiver);
	}

	@After
	public void tearDown() throws Exception {
		inputStream.close();

		verifyNoMoreInteractions(mockedReceiver);
	}

	@Test
	public void testProcess() throws Exception {
		qucosaDecoder.process(inputSource);

		verify(mockedReceiver).startRecord("");
		verify(mockedReceiver).startEntity(recordName);
		verify(mockedReceiver).startEntity(QucosaDecoder.HEADER_TAG);
		verify(mockedReceiver).literal("identifier", "urn:nbn:de:bsz:14-ds-1229427875176-76287\n" +
				"                ");
		verify(mockedReceiver).literal("datestamp", "2010-07-28");
		verify(mockedReceiver).literal("setSpec", "pub-type:article");
		verify(mockedReceiver).literal("setSpec", "has-source-swb:false");
		verify(mockedReceiver).literal("setSpec", "open_access");
		verify(mockedReceiver).literal("setSpec", "ddc:020");
		verify(mockedReceiver).literal("setSpec", "doc-type:article");
//		verify(mockedReceiver).endEntity();
		verify(mockedReceiver).startEntity(QucosaDecoder.METADATA_TAG);
		verify(mockedReceiver).startEntity(QucosaDecoder.OAI_DATA_TAG);
		verify(mockedReceiver).literal("dc:title", "Autoren\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:creator", "\n" +
				"                        Redaktion, BIS\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:subject", "SLUB Dresden\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:subject", "Sachsen\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:subject", "Bibliotheken\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:subject", "Saxony\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:subject", "Libraries\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:subject", "\n" +
				"                        ddc:020\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:subject", "\n" +
				"                        rvk:--\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:description", "Autorenliste des Heftes 4 /\n" +
				"                        2008\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:publisher", "\n" +
				"                        Saechsische Landesbibliothek- Staats- und\n" +
				"                        Universitaetsbibliothek Dresden\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:contributor", "\n" +
				"                        SLUB Dresden, Allgemein\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:date", "\n" +
				"                        2008-12-16\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:type", "\n" +
				"                        doc-type:article\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:format", "\n" +
				"                        application/pdf\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:identifier", "\n" +
				"                        http://nbn-resolving.de/urn:nbn:de:bsz:14-ds-1229427875176-76287\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:identifier", "\n" +
				"                        urn:nbn:de:bsz:14-ds-1229427875176-76287\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:identifier", "\n" +
				"                        http://www.qucosa.de/fileadmin/data/qucosa/documents/26/1229427875176-7628.pdf\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:source", "BIS -\n" +
				"                        Das Magazin der Bibliotheken in Sachsen 1(2008)4, S. 273\n" +
				"                    ");
		verify(mockedReceiver).literal("dc:language", "\n" +
				"                        deu\n" +
				"                    ");
//		verify(mockedReceiver).endEntity();
//		verify(mockedReceiver).endEntity();
		verify(mockedReceiver, times(4)).endEntity();
		verify(mockedReceiver).endRecord();
	}
}
