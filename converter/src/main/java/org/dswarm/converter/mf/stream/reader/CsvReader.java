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
package org.dswarm.converter.mf.stream.reader;

import com.google.common.base.Optional;
import org.apache.commons.csv.CSVRecord;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

import org.dswarm.converter.mf.framework.annotations.Record;
import org.dswarm.converter.mf.stream.converter.CsvDecoder;
import org.dswarm.converter.mf.stream.converter.CsvLineReader;

/**
 * Reads Csv files. First line can be interpreted as header.<br>
 * Inspired by org.culturegraph.mf.stream.reader.CsvReader
 * 
 * @author tgaengler
 * @author phorn
 */
@Description("reads Csv files. First line can be interpreted as header.")
@In(java.io.Reader.class)
@Record(CSVRecord.class)
@Out(StreamReceiver.class)
public final class CsvReader implements Reader<CSVRecord> {

	private CsvLineReader		lineReader;
	private final CsvDecoder	decoder;

	private void setLineReader(final CsvLineReader reader) {
		lineReader = reader;
		lineReader.setReceiver(decoder);
	}

	private CsvReader(final CsvLineReader lineReaderArg) {
		super();

		decoder = new CsvDecoder();
		setLineReader(lineReaderArg);
	}

	public CsvReader() {

		this(new CsvLineReader());
	}

	public CsvReader(final char escapeCharacter, final char quoteCharacter, final char columnSeparator, final String lineEnding) {

		this(new CsvLineReader(escapeCharacter, quoteCharacter, columnSeparator, lineEnding));
	}

	public CsvReader(final char escapeCharacter, final char quoteCharacter, final char columnDelimiter, final String rowDelimiter,
			final int ignoreLines, final int discardRows, final Optional<Integer> atMost) {

		this(new CsvLineReader(escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter, ignoreLines, discardRows, atMost));
	}

	public CsvDecoder getDecoder() {

		return decoder;
	}

	public CsvReader withLimit(final int limit) {

		setLineReader(lineReader.withLimit(limit));

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

	public CsvReader setHeader(final boolean hasHeader) {

		setLineReader(lineReader.withHeader(hasHeader));
		decoder.setHeader(hasHeader);

		return this;
	}

	public CsvReader setDataResourceSchemaBaseURI(final String dataResourceSchemaBaseURI) {

		decoder.setDataModelSchemaBaseURI(dataResourceSchemaBaseURI);

		return this;
	}
}
