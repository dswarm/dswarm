package de.avgl.dmp.converter.flow;

import java.io.Reader;

import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;

import com.google.common.base.Optional;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.mf.stream.reader.CsvReader;
import de.avgl.dmp.converter.mf.stream.source.CSVJSONEncoder;
import de.avgl.dmp.converter.mf.stream.source.CSVJSONWriter;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;

/**
 * Flow that reads and parses a given CSV document and returns a preview of its content as JSON representation.
 * 
 * @author tgaengler
 * @author phorn
 */
public class CSVSourceResourceCSVJSONPreviewFlow extends AbstractCSVResourceFlow<String> {

	public CSVSourceResourceCSVJSONPreviewFlow(final String encoding, final Character escapeCharacter, final Character quoteCharacter,
			final Character columnDelimiter, final String rowDelimiter) {
		super(encoding, escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter);
	}

	public CSVSourceResourceCSVJSONPreviewFlow(final Configuration configuration) throws DMPConverterException {
		super(configuration);
	}

	public CSVSourceResourceCSVJSONPreviewFlow(final DataModel dataModel) throws DMPConverterException {

		super(dataModel);
	}

	@Override
	protected String process(final ObjectPipe<String, ObjectReceiver<Reader>> opener, final String obj, final CsvReader pipe) {

		final CSVJSONEncoder encoder = new CSVJSONEncoder();
		encoder.withHeader();

		final CSVJSONWriter writer = new CSVJSONWriter();

		pipe.setReceiver(encoder).setReceiver(writer);

		opener.process(obj);

		return writer.toString();
	}

	public CSVSourceResourceCSVJSONPreviewFlow withLimit(final int limit) {

		atMost = Optional.of(limit);

		return this;
	}
}
