package de.avgl.dmp.converter.mf.stream.converter;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

/**
 * Processes input from a reader line by line. Inspired by org.culturegraph.mf.stream.converter.LineReader
 * 
 * @author tgaengler
 */
@Description("Emits each line read as a CSVRecord.")
@In(Reader.class)
@Out(CSVRecord.class)
public final class CsvLineReader extends DefaultObjectPipe<Reader, ObjectReceiver<CSVRecord>> {

	private static char		escapeCharacter;
	private static char		quoteCharacter;
	private static char		columnSeparator;
	private static String	lineEnding;

	public CsvLineReader() {

		escapeCharacter = '\\';
		quoteCharacter = '"';
		columnSeparator = ';';
		lineEnding = "\n";
	}

	public CsvLineReader(final char escapeCharacteArg, final char quoteCharacterArg, final char columnSeparatorArg, final String lineEndingArg) {

		escapeCharacter = escapeCharacteArg;
		quoteCharacter = quoteCharacterArg;
		columnSeparator = columnSeparatorArg;
		lineEnding = lineEndingArg;
	}

	@Override
	public void process(final Reader reader) {
		assert !isClosed();
		assert null != reader;
		process(reader, getReceiver());
	}

	public static void process(final Reader reader, final ObjectReceiver<CSVRecord> receiver) {

		final CSVFormat csvFormat = CSVFormat.newFormat(columnSeparator).withCommentStart('#').withQuoteChar(quoteCharacter)
				.withEscape(escapeCharacter).withRecordSeparator(lineEnding);

		CSVParser csvParser = null;
		
		try {

			csvParser = new CSVParser(reader, csvFormat);
		} catch (IOException e) {

			throw new MetafactureException(e);
		}

		Iterator<CSVRecord> csvIter = csvParser.iterator();

		while (csvIter.hasNext()) {

			final CSVRecord record = csvIter.next();

			if (record != null) {

				receiver.process(record);
			}
		}

		try {

			csvParser.close();
		} catch (IOException e) {

			throw new MetafactureException(e);
		}
	}

}
