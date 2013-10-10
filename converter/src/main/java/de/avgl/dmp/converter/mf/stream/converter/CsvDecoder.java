package de.avgl.dmp.converter.mf.stream.converter;

import java.util.Iterator;

import org.apache.commons.csv.CSVRecord;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

/**
 * Decodes lines of CSV files.
 *
 * @author tgaengler
 */
@Description("Decodes lines of CSV files.")
@In(CSVRecord.class)
@Out(StreamReceiver.class)
public final class CsvDecoder extends DefaultObjectPipe<CSVRecord, StreamReceiver> {

	private boolean		hasHeader	= false;
	private String[]	header		= new String[0];
	private int			count;
	private boolean		withLimit	= false;
	private int			limit		= -1;
	private int			limitCount	= 0;

	public CsvDecoder() {
		super();
	}

	public CsvDecoder(final int limit) {
		super();

		this.limit = limit;
		this.withLimit = true;
	}

	@Override
	public void process(final CSVRecord record) {

		assert !isClosed();

		if (withLimit) {

			if (limitCount == limit) {

				return;
			}

			limitCount++;
		}

		if (hasHeader) {

			if (header.length == 0) {

				// determine schema properties from header

				final Iterator<String> headerIter = record.iterator();

				header = new String[record.size()];

				int i = 0;

				while (headerIter.hasNext()) {

					header[i] = headerIter.next();
					i++;
				}
			} else if (record.size() == header.length) {

				// utilise header for schema properties

				getReceiver().startRecord(String.valueOf(++count));

				final Iterator<String> columnsIter = record.iterator();

				int i = 0;

				while (columnsIter.hasNext()) {

					getReceiver().literal(header[i], columnsIter.next());
					i++;
				}

				getReceiver().endRecord();
			} else {

				throw new IllegalArgumentException(String.format(
						"wrong number of columns in input line %d - expected [%d] but found [%d] instead, line was %s",
						count, header.length, record.size(), record.toString()));
			}
		} else {
			// utilise column number as default for schema property

			getReceiver().startRecord(String.valueOf(++count));

			final Iterator<String> columnsIter = record.iterator();

			int i = 0;

			while (columnsIter.hasNext()) {

				getReceiver().literal(String.valueOf(i), columnsIter.next());
				i++;
			}
			getReceiver().endRecord();
		}
	}

	public void setHeader(final boolean hasHeaderArg) {

		if (withLimit) {

			// increase limit for header row

			limit++;
		}

		hasHeader = hasHeaderArg;
	}
}
