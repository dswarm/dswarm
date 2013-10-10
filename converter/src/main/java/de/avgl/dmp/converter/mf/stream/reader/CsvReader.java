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

	public CsvReader(final Character escapeCharacter, final Character quoteCharacter, final Character columnDelimiter,
					 final String rowDelimiter, final int ignoreLines, final int discardRows) {

		this.decoder = new CsvDecoder();
		lineReader = new CsvLineReader(escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter, ignoreLines, discardRows);
		lineReader.setReceiver(this.decoder);
	}

	public CsvDecoder getDecoder() {

		return decoder;
	}

	public CsvReader withLimit(final int limit) {
		this.limit = limit;
		this.withLimit = true;

		return this;
	}

	@Override
	public <R extends StreamReceiver> R setReceiver(final R receiver) {

		decoder.setReceiver(receiver);

		return receiver;
	}

	@Override
	public void process(final java.io.Reader reader) {

		lineReader.process(reader);
	}

	@Override
	public void read(final CSVRecord entry) {

		if (withLimit) {

			if (count == limit) {

				return;
			}

			count++;
		}

		decoder.process(entry);
	}

	@Override
	public void resetStream() {

		lineReader.resetStream();
	}

	@Override
	public void closeStream() {

		lineReader.closeStream();
	}

	public void setHeader(final boolean hasHeader) {

		if(hasHeader) {

			limit++;
		}

		lineReader.setHeader(hasHeader);
		decoder.setHeader(hasHeader);
	}
}
