/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.converter.mf.stream.converter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.base.Optional;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;

/**
 * Processes input from a reader line by line. Inspired by org.culturegraph.mf.stream.converter.LineReader
 * 
 * @author tgaengler
 * @author phorn
 */
@Description("Emits each line read as a CSVRecord.")
@In(Reader.class)
@Out(CSVRecord.class)
public final class CsvLineReader extends DefaultObjectPipe<Reader, ObjectReceiver<CSVRecord>> {

	private final char				escapeCharacter;
	private final char				quoteCharacter;
	private final char				columnSeparator;
	private final String			lineEnding;
	private final int				ignoreLines;
	private final int				discardRows;
	private final Optional<Integer>	atMost;

	private final boolean			hasHeader;

	public CsvLineReader() {
		// this('\\', '"', ',', "\r\n");
		this(ConfigurationStatics.DEFAULT_ESCAPE_CHARACTER, ConfigurationStatics.DEFAULT_QUOTE_CHARACTER,
				ConfigurationStatics.DEFAULT_COLUMN_DELIMITER, ConfigurationStatics.DEFAULT_ROW_DELIMITER);
	}

	public CsvLineReader(final Character escapeCharacterArg, final Character quoteCharacterArg, final Character columnDelimiter,
			final String rowDelimiter) {
		this(escapeCharacterArg, quoteCharacterArg, columnDelimiter, rowDelimiter, 0, 0, Optional.<Integer> absent());
	}

	public CsvLineReader(final Character escapeCharacterArg, final Character quoteCharacterArg, final Character columnDelimiter,
			final String rowDelimiter, final int ignoreLinesArg, final int discardRowsArg, final Optional<Integer> atMostArg) {

		this(escapeCharacterArg, quoteCharacterArg, columnDelimiter, rowDelimiter, ignoreLinesArg, discardRowsArg, atMostArg, false);
	}

	private CsvLineReader(final Character escapeCharacterArg, final Character quoteCharacterArg, final Character columnDelimiter,
			final String rowDelimiter, final int ignoreLinesArg, final int discardRowsArg, final Optional<Integer> atMostArg,
			final boolean hasHeaderArg) {

		escapeCharacter = escapeCharacterArg;
		quoteCharacter = quoteCharacterArg;
		columnSeparator = columnDelimiter;
		lineEnding = rowDelimiter;
		ignoreLines = ignoreLinesArg;
		discardRows = discardRowsArg;
		atMost = atMostArg;

		hasHeader = hasHeaderArg;
	}

	@Override
	public void process(final Reader reader) {
		assert !isClosed();
		assert null != reader;
		process(reader, getReceiver());
	}

	private Reader getInternalReader(final Reader reader) {

		if (ignoreLines > 0) {

			final BufferedReader bufferedReader = new BufferedReader(reader);
			int i = ignoreLines;
			String exceptionMessage = "Cannot ignore " + ignoreLines + " lines. There is nothing left to ignore [%d] more lines.";

			try {
				for (; i-- > 0;) {
					final String line = bufferedReader.readLine();
					if (line == null) {
						throw new MetafactureException(String.format(exceptionMessage, i + 1));
					}
				}
			} catch (final IOException e) {
				throw new MetafactureException(String.format(exceptionMessage, i + 1), e);
			}

			return bufferedReader;
		}

		return reader;
	}

	private CSVParser getInternalParser(final Reader reader) {
		final CSVFormat csvFormat = CSVFormat.newFormat(columnSeparator).withQuote(quoteCharacter).withEscape(escapeCharacter)
				.withRecordSeparator(lineEnding).withIgnoreEmptyLines(true).withIgnoreSurroundingSpaces(true);

		try {

			return new CSVParser(reader, csvFormat);
		} catch (final IOException e) {

			throw new MetafactureException(e);
		}
	}

	private PeekingIterator<CSVRecord> getInternalCSVIter(final CSVParser parser) {
		final Iterator<CSVRecord> csvIter = parser.iterator();

		if (atMost.isPresent()) {

			final int headerRows = hasHeader ? 1 : 0;
			final Iterator<CSVRecord> limitedIterator = Iterators.limit(csvIter, atMost.get() + headerRows + discardRows);
			return Iterators.peekingIterator(limitedIterator);
		}

		return Iterators.peekingIterator(csvIter);
	}

	private void processHeaders(final PeekingIterator<CSVRecord> iterator, final ObjectReceiver<CSVRecord> receiver) {
		if (!iterator.hasNext()) {
			throw new MetafactureException("Cannot find any row to use as header row.");
		}

		final CSVRecord record;
		if (hasHeader) {
			record = iterator.next();
		} else {
			record = iterator.peek();
		}

		receiver.process(record);
	}

	private void processDiscardRows(final Iterator<CSVRecord> iterator) {
		int i = discardRows;
		try {

			for (; i-- > 0;) {
				iterator.next();
			}

		} catch (final NoSuchElementException e) {

			throw new MetafactureException(String.format("Cannot discard %d rows. There is nothing left to discard [%d] more rows.", discardRows,
					i + 1), e);
		}
	}

	public void process(final Reader reader, final ObjectReceiver<CSVRecord> receiver) {

		final Reader actualReader = getInternalReader(reader);
		final CSVParser csvParser = getInternalParser(actualReader);

		final PeekingIterator<CSVRecord> csvIter = getInternalCSVIter(csvParser);

		processHeaders(csvIter, receiver);
		processDiscardRows(csvIter);

		boolean hasRecord = false;

		while (csvIter.hasNext()) {

			final CSVRecord record = csvIter.next();

			if (record != null) {
				hasRecord = true;

				receiver.process(record);
			}
		}

		if (!hasRecord) {

			throw new MetafactureException(String.format("There are no records available, you need to have at least one row."));
		}

		try {

			csvParser.close();
			receiver.closeStream();
		} catch (final IOException e) {

			throw new MetafactureException(e);
		}
	}

	public CsvLineReader withHeader(final boolean hasHeaderArg) {
		return new CsvLineReader(escapeCharacter, quoteCharacter, columnSeparator, lineEnding, ignoreLines, discardRows, atMost, hasHeaderArg);
	}

	public CsvLineReader withLimit(final int limit) {
		return new CsvLineReader(escapeCharacter, quoteCharacter, columnSeparator, lineEnding, ignoreLines, discardRows, Optional.of(limit),
				hasHeader);
	}
}
