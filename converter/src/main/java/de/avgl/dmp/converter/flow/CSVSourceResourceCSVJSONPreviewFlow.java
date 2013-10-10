package de.avgl.dmp.converter.flow;

import java.io.Reader;

import com.google.common.base.Optional;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.mf.stream.reader.CsvReader;
import de.avgl.dmp.converter.mf.stream.source.CSVJSONEncoder;
import de.avgl.dmp.converter.mf.stream.source.CSVJSONWriter;
import de.avgl.dmp.persistence.model.resource.Configuration;

public class CSVSourceResourceCSVJSONPreviewFlow extends AbstractCSVResourceFlow<String> {

	private boolean withLimit = false;
	private int limit = -1;

	public CSVSourceResourceCSVJSONPreviewFlow(final String encoding, final Character escapeCharacter,
											   final Character quoteCharacter, final Character columnDelimiter,
											   final String rowDelimiter) {
		super(encoding, escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter);
	}

	public CSVSourceResourceCSVJSONPreviewFlow(final Configuration configuration) throws DMPConverterException {
		super(configuration);
	}

	@Override
	protected String process(final ObjectPipe<String, ObjectReceiver<Reader>> opener, final String obj, final CsvReader pipe) {

		final Optional<Integer> newLimit;

		if (atMost.isPresent()) {
			if (withLimit && atMost.get() > limit) {
				newLimit = Optional.of(limit);
			} else {
				newLimit = atMost;
			}

		} else if (withLimit) {
			newLimit = Optional.of(limit);
		} else {
			newLimit = Optional.absent();
		}

		if (newLimit.isPresent()) {

			pipe.withLimit(newLimit.get());
		}

		// TODO: process header from configuration
		final CSVJSONEncoder encoder;

		if(newLimit.isPresent()) {

			encoder = new CSVJSONEncoder(newLimit.get());
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
