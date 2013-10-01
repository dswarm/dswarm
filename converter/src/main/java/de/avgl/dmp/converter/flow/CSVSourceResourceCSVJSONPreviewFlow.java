package de.avgl.dmp.converter.flow;

import java.io.Reader;

import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.mf.stream.reader.CsvReader;
import de.avgl.dmp.converter.mf.stream.source.CSVJSONEncoder;
import de.avgl.dmp.converter.mf.stream.source.CSVJSONWriter;
import de.avgl.dmp.persistence.model.resource.Configuration;

public class CSVSourceResourceCSVJSONPreviewFlow extends CSVResourceFlow<String> {

	private boolean withLimit = false;
	private int limit = -1;

	public CSVSourceResourceCSVJSONPreviewFlow(String encoding, Character escapeCharacter, Character quoteCharacter, Character columnDelimiter, String rowDelimiter) {
		super(encoding, escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter);
	}

	public CSVSourceResourceCSVJSONPreviewFlow(Configuration configuration) throws DMPConverterException {
		super(configuration);
	}

	@Override
	protected String process(ObjectPipe<String, ObjectReceiver<Reader>> opener, String obj, CsvReader pipe) {


		if(withLimit) {

			pipe.withLimit(limit);
		}

		// TODO: process header from configuration
		final CSVJSONEncoder encoder;

		if(withLimit) {

			encoder = new CSVJSONEncoder(limit);
		} else {
			encoder = new CSVJSONEncoder();
		}
		encoder.withHeader();

		// TODO: process header from configuration
		pipe.getDecoder().setHeader(true);

		final CSVJSONWriter writer = new CSVJSONWriter();

		pipe.setReceiver(encoder).setReceiver(writer);

		opener.process(obj);

		return writer.toString();
	}

	public CSVSourceResourceCSVJSONPreviewFlow withLimit(final int limit) {

		this.limit = limit;
		withLimit = true;

		return this;
	}
}
