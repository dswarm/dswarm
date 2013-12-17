package de.avgl.dmp.converter.flow;

import java.io.Reader;
import java.io.StringWriter;

import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.stream.sink.ObjectJavaIoWriter;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.mf.stream.reader.CsvReader;
import de.avgl.dmp.converter.mf.stream.source.CSVEncoder;
import de.avgl.dmp.persistence.model.resource.Configuration;

public class CSVSourceResourceCSVPreviewFlow extends AbstractCSVResourceFlow<String> {

	public CSVSourceResourceCSVPreviewFlow(final String encoding, final Character escapeCharacter,
										   final Character quoteCharacter, final Character columnDelimiter,
										   final String rowDelimiter) {
		super(encoding, escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter);
	}

	public CSVSourceResourceCSVPreviewFlow(final Configuration configuration) throws DMPConverterException {
		super(configuration);
	}

	@Override
	protected String process(final ObjectPipe<String, ObjectReceiver<Reader>> opener, final String obj, final CsvReader pipe) {
		final CSVEncoder converter = new CSVEncoder();
		converter.withHeader();

		final StringWriter stringWriter = new StringWriter();
		final ObjectJavaIoWriter<String> writer = new ObjectJavaIoWriter<>(stringWriter);

		pipe.setReceiver(converter).setReceiver(writer);

		opener.process(obj);

		return stringWriter.toString();
	}
}
