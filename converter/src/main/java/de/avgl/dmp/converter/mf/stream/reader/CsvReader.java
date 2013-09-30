package de.avgl.dmp.converter.mf.stream.reader;

import org.apache.commons.csv.CSVRecord;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

import de.avgl.dmp.converter.mf.framework.annotations.Record;
import de.avgl.dmp.converter.mf.stream.converter.CsvDecoder;
import de.avgl.dmp.converter.mf.stream.converter.CsvLineReader;

/**
 * Reads Csv files. First line can be interpreted as header.<br>
 * Inspired by org.culturegraph.mf.stream.reader.CsvReader
 * 
 * @author tgaengler
 */
@Description("reads Csv files. First line can be interpreted as header.")
@In(java.io.Reader.class)
@Record(CSVRecord.class)
@Out(StreamReceiver.class)
public final class CsvReader implements Reader<CSVRecord> {

	private final CsvLineReader	lineReader;
	private final CsvDecoder	decoder;
	private boolean				withLimit	= false;
	private int					limit		= -1;
	private int					count		= 0;

	public CsvReader() {

		super();

		this.decoder = new CsvDecoder();
		lineReader = new CsvLineReader();
		lineReader.setReceiver(this.decoder);
	}

	public CsvReader(final int limit) {

		super();

		this.decoder = new CsvDecoder();
		lineReader = new CsvLineReader();
		lineReader.setReceiver(this.decoder);

		this.limit = limit;
		this.withLimit = true;
	}

	public CsvReader(final char escapeCharacteArg, final char quoteCharacterArg, final char columnSeparatorArg, final String lineEnding) {

		super();

		this.decoder = new CsvDecoder();
		lineReader = new CsvLineReader(escapeCharacteArg, quoteCharacterArg, columnSeparatorArg, lineEnding);
		lineReader.setReceiver(this.decoder);
	}

	public CsvReader(final char escapeCharacteArg, final char quoteCharacterArg, final char columnSeparatorArg, final String lineEnding,
			final int limit) {

		super();

		this.decoder = new CsvDecoder();
		lineReader = new CsvLineReader(escapeCharacteArg, quoteCharacterArg, columnSeparatorArg, lineEnding);
		lineReader.setReceiver(this.decoder);

		this.limit = limit;
		this.withLimit = true;
	}

	public final CsvDecoder getDecoder() {

		return decoder;
	}

	@Override
	public final <R extends StreamReceiver> R setReceiver(final R receiver) {

		decoder.setReceiver(receiver);

		return receiver;
	}

	@Override
	public final void process(final java.io.Reader reader) {

		lineReader.process(reader);
	}

	@Override
	public final void read(final CSVRecord entry) {
		
		if (withLimit) {

			if (count == limit) {

				return;
			}
			
			count++;
		}

		decoder.process(entry);
	}

	@Override
	public final void resetStream() {

		lineReader.resetStream();
	}

	@Override
	public final void closeStream() {

		lineReader.closeStream();
	}

	public void setHeader(final boolean hasHeader) {
		
		if(hasHeader) {
			
			limit++;
		}

		decoder.setHeader(hasHeader);
	}
}
