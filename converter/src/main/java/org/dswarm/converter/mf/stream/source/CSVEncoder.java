package org.dswarm.converter.mf.stream.source;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

import com.google.common.collect.Lists;

/**
 * Serialises an object as CSV. Records and entities are represented as objects.
 * 
 * @author tgaengler
 */
@Description("Serialises an object as CSV")
@In(StreamReceiver.class)
@Out(String.class)
public final class CSVEncoder extends DefaultStreamPipe<ObjectReceiver<String>> {

	private List<String>		header;
	private List<String>		values;

	private final CSVFormat		csvFormat;
	private CSVPrinter			csvPrinter;
	private final StringWriter	writer	= new StringWriter();

	private boolean				withHeader;
	private boolean				firstLine;
	private boolean				firstLineInitialized;

	public CSVEncoder() {

		final char escapeCharacter = '\\';
		final char quoteCharacter = '"';
		final char columnSeparator = ';';
		final String lineEnding = System.getProperty("line.separator");

		csvFormat = CSVFormat.newFormat(columnSeparator).withCommentMarker('#').withQuoteChar(quoteCharacter).withEscape(escapeCharacter)
				.withRecordSeparator(lineEnding);
	}

	@Override
	public void startRecord(final String id) {

		if (firstLine) {

			firstLine = false;
		}

		if (!firstLineInitialized) {

			firstLine = true;
			firstLineInitialized = true;

			if (withHeader) {

				header = Lists.newLinkedList();
			}
		}

		final StringBuffer buffer = writer.getBuffer();
		buffer.delete(0, buffer.length());

		// create CSV printer

		try {
			csvPrinter = new CSVPrinter(writer, csvFormat);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		// TODO: workaround (?)

		startEntity(id);
	}

	@Override
	public void endRecord() {

		// TODO: workaround (?)

		endEntity();

		// write CSV with writer from CSV printer

		// TODO: workaround to cut row delimiter
		final String line = writer.toString();

		getReceiver().process(line.substring(0, line.length() - 1));
	}

	@Override
	public void startEntity(final String name) {

		values = Lists.newLinkedList();
	}

	@Override
	public void endEntity() {

		if (firstLine) {

			if (withHeader) {

				try {
					csvPrinter.printRecord(header);
				} catch (final IOException e) {

					throw new MetafactureException("couldn't write CSV header line");
				}
			}
		}

		// write record

		try {
			csvPrinter.printRecord(values);
		} catch (final IOException e) {

			throw new MetafactureException("couldn't write CSV line");
		}
	}

	@Override
	public void literal(final String name, final String value) {

		if (firstLine) {

			if (withHeader) {

				if (name != null) {

					header.add(name);
				} else {

					throw new MetafactureException("couldn't write header column, because it is null");
				}
			}
		}

		// collect values
		if (value != null) {

			values.add(value);
		} else {

			throw new MetafactureException("name and value are null");
		}
	}

	public void withHeader() {

		withHeader = true;
	}
}
