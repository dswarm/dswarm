package de.avgl.dmp.converter.mf.stream.reader;

import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

import de.avgl.dmp.converter.mf.stream.converter.CsvDecoder;

/**
 * Reads Csv files. First n lines are interpreted as header.<br>
 * Inspired by org.culturegraph.mf.stream.reader.CsvReader
 * 
 * @author tgaengler
 */
@Description("reads Csv files. First n lines are interpreted as header. Provide value separators in brackets as regexp.")
@In(java.io.Reader.class)
@Out(StreamReceiver.class)
public final class CsvReader extends ReaderBase<CsvDecoder> {

	public CsvReader() {

		super(new CsvDecoder());
	}

	public CsvReader(final String columnSeparator) {

		super(new CsvDecoder(columnSeparator));
	}

	public CsvReader(final String columnSeparator, final String rowSeparator) {

		super(new CsvDecoder(columnSeparator), rowSeparator);
	}

	public void setHeaderLines(final int headerLines) {

		getDecoder().setHeaderLines(headerLines);
	}
	
	public void setSchemaHeaderLine(final int schemaHeaderLine) {
		
		getDecoder().setSchemaHeaderLine(schemaHeaderLine);
	}

}
