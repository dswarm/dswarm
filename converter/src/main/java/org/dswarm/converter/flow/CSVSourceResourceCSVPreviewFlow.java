package org.dswarm.converter.flow;

import java.io.Reader;
import java.io.StringWriter;

import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.stream.sink.ObjectJavaIoWriter;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.mf.stream.reader.CsvReader;
import org.dswarm.converter.mf.stream.source.CSVEncoder;
import org.dswarm.persistence.model.resource.Configuration;

/**
 * Flow that reads and parses a given CSV document and returns a preview of its content.
 * 
 * @author tgaengler
 * @author phorn
 */
public class CSVSourceResourceCSVPreviewFlow extends AbstractCSVResourceFlow<String> {

	public CSVSourceResourceCSVPreviewFlow(final String encoding, final Character escapeCharacter, final Character quoteCharacter,
			final Character columnDelimiter, final String rowDelimiter) {
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
