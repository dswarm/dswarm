package de.avgl.dmp.converter.mf.stream.converter;

import java.util.Iterator;

import org.apache.commons.csv.CSVRecord;
import org.culturegraph.mf.exceptions.MetafactureException;
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

	private boolean		hasHeader;
	private boolean		hasHeadersProcessed;
	private String[]	header	= new String[0];
	private int			count;
	
	private String									dataResourceBaseURI;
	private String dataResourceSchemaBaseURI;

	@Override
	public void process(final CSVRecord record) {

		assert !isClosed();

		if (!hasHeadersProcessed) {
			if (hasHeader) {

				// determine schema properties from header

				final Iterator<String> headerIter = record.iterator();

				header = new String[record.size()];

				int i = 0;

				while (headerIter.hasNext()) {

					final String headerColumnName = headerIter.next();
					
					final String headerColumnURI;
					
					if(dataResourceSchemaBaseURI != null) {
						
						headerColumnURI = dataResourceSchemaBaseURI + headerColumnName;
					} else {
						
						headerColumnURI = headerColumnName;
					}
					
					header[i] = headerColumnURI;
					i++;
				}
			} else {

				final int size = record.size();

				header = new String[size];

				for (int i = 0; i < size; i++) {
					header[i] = String.valueOf(i);
				}
			}

			hasHeadersProcessed = true;

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

			throw new MetafactureException(String.format(
					"wrong number of columns in input line %d - expected [%d] but found [%d] instead, line was %s", count, header.length,
					record.size(), record.toString()));
		}
	}

	public void setHeader(final boolean hasHeaderArg) {

		hasHeader = hasHeaderArg;
	}
	
	public void setDataResourceBaseURI(final String dataResourceBaseURIArg) {
		
		dataResourceBaseURI = dataResourceBaseURIArg;
	}
	
	public void setDataResourceSchemaBaseURI(final String dataResourceSchemaBaseURIArg) {
		
		dataResourceSchemaBaseURI = dataResourceSchemaBaseURIArg;
	}
}
