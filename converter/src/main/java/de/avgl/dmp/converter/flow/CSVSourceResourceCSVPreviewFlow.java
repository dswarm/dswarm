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

public class CSVSourceResourceCSVPreviewFlow extends CSVResourceFlow<String> {

	public CSVSourceResourceCSVPreviewFlow(String encoding, Character escapeCharacter, Character quoteCharacter, Character columnDelimiter, String rowDelimiter) {
		super(encoding, escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter);
	}

	public CSVSourceResourceCSVPreviewFlow(Configuration configuration) throws DMPConverterException {
		super(configuration);
	}

	@Override
	protected String process(ObjectPipe<String, ObjectReceiver<Reader>> opener, String obj, CsvReader pipe) {
		final CSVEncoder converter = new CSVEncoder();
		converter.withHeader();

		StringWriter stringWriter = new StringWriter();
		final ObjectJavaIoWriter<String> writer = new ObjectJavaIoWriter<String>(stringWriter);

		pipe.setReceiver(converter).setReceiver(writer);

		opener.process(obj);

		return stringWriter.toString();
	}
}
