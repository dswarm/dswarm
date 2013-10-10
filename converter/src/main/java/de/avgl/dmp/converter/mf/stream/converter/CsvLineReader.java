package de.avgl.dmp.converter.mf.stream.converter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

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
	private static int		ignoreLines;
	private static int		discardRows;

	private boolean			hasHeader = false;

	public CsvLineReader() {

		escapeCharacter = '\\';
		quoteCharacter = '"';
		columnSeparator = ';';
		lineEnding = "\n";
		ignoreLines = 0;
		discardRows = 0;
	}

	public CsvLineReader(final char escapeCharacteArg, final char quoteCharacterArg, final char columnSeparatorArg, final String lineEndingArg) {

		escapeCharacter = escapeCharacteArg;
		quoteCharacter = quoteCharacterArg;
		columnSeparator = columnSeparatorArg;
		lineEnding = lineEndingArg;
		ignoreLines = 0;
		discardRows = 0;
	}

	public CsvLineReader(final Character escapeCharacterArg, final Character quoteCharacterArg, final Character columnDelimiter, final String rowDelimiter, final int ignoreLinesArg, final int discardRowsArg) {

		escapeCharacter = escapeCharacterArg;
		quoteCharacter = quoteCharacterArg;
		columnSeparator = columnDelimiter;
		lineEnding = rowDelimiter;
		ignoreLines = ignoreLinesArg;
		discardRows = discardRowsArg;
	}

	@Override
	public void process(final Reader reader) {
		assert !isClosed();
		assert null != reader;
		process(reader, getReceiver());
	}

	public void process(final Reader reader, final ObjectReceiver<CSVRecord> receiver) {

		final CSVFormat csvFormat = CSVFormat.newFormat(columnSeparator).withCommentStart('#').withQuoteChar(quoteCharacter)
				.withEscape(escapeCharacter).withRecordSeparator(lineEnding);

		CSVParser csvParser;

		Reader actualReader;
		if (ignoreLines > 0) {
			final BufferedReader bufferedReader = new BufferedReader(reader);
			int i = ignoreLines;
			try {
				for (; i --> 0 ;) {
					final String line = bufferedReader.readLine();
					if (line == null) {
						throw new MetafactureException(String.format("cannot ignore [%d] lines, file is probably empty", i = 1));
					}
				}
			} catch (IOException e) {
				throw new MetafactureException(String.format("cannot ignore [%d] lines, file is probably empty", i + 1), e);
			}
			actualReader = bufferedReader;
		} else {
			actualReader = reader;
		}

		try {

			csvParser = new CSVParser(actualReader, csvFormat);
		} catch (IOException e) {

			throw new MetafactureException(e);
		}

		final Iterator<CSVRecord> csvIter = csvParser.iterator();

		boolean managedDiscards = discardRows <= 0;
		int headersRemaining = hasHeader ? 1 : 0;

		boolean hasRecord = false;

		while (csvIter.hasNext()) {

			if (!managedDiscards && headersRemaining <= 0) {

				int i = discardRows;
				try {

					for (; i --> 0 ;) {
						csvIter.next();
					}
				} catch (NoSuchElementException e) {

					throw new MetafactureException(String.format("there is nothing left to discard [%d] more rows", i + 1), e);
				}

				managedDiscards = true;
				continue;
			}

			final CSVRecord record = csvIter.next();

			if (record != null) {
				if (headersRemaining > 0) {
					headersRemaining -= 1;
				} else {
					hasRecord = true;
				}

				receiver.process(record);
			}
		}

		if (!hasRecord) {

			throw new MetafactureException(String.format("there are no records available, you have to have at least on row"));
		}

		try {

			csvParser.close();
		} catch (IOException e) {

			throw new MetafactureException(e);
		}
	}

	public void setHeader(boolean hasHeader) {
		this.hasHeader = hasHeader;
	}
}
