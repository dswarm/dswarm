package de.avgl.dmp.converter.mf.stream.converter;

import java.util.regex.Pattern;

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
@In(String.class)
@Out(StreamReceiver.class)
public final class CsvDecoder extends DefaultObjectPipe<String, StreamReceiver> {

	private static final String	DEFAULT_SEP		= "[\t,;]";
	private final Pattern		columnSeparator;
	private String[][]			header			= new String[0][0];
	private int					count;
	private int					headerLines;
	private int					headerLinesCount;
	private int					schemaHeaderLine = -1;
	private boolean				readHeaderLines	= false;

	/**
	 * @param columnSeparator regexp to split lines
	 */
	public CsvDecoder(final String columnSeparator) {
		super();
		this.columnSeparator = Pattern.compile("[" + columnSeparator + "]");
	}

	public CsvDecoder() {
		super();
		this.columnSeparator = Pattern.compile(DEFAULT_SEP);
	}

	@Override
	public void process(final String string) {
		
		assert !isClosed();
		
		final String[] parts = columnSeparator.split(string);

		if ((headerLines > 0)) {

			if (readHeaderLines == false) {

				// read header lines

				if (header[headerLinesCount].length == 0) {

					header[headerLinesCount] = parts;
					headerLinesCount++;

					if (headerLinesCount == headerLines) {

						readHeaderLines = true;
					}
				}
			} else {
				
				if(((schemaHeaderLine >= 0) && (schemaHeaderLine < headerLines)) == false) {
					
					throw new IllegalArgumentException("illegal schema header line: " + schemaHeaderLine);
				}

				if (parts.length == header[schemaHeaderLine].length) {

					// process schema header line

					getReceiver().startRecord(String.valueOf(++count));

					for (int i = 0; i < parts.length; ++i) {

						getReceiver().literal(header[schemaHeaderLine][i], parts[i]);
					}

					getReceiver().endRecord();
				} else {

					throw new IllegalArgumentException("wrong number of columns in input line: " + string);
				}
			}
		} else {

			getReceiver().startRecord(String.valueOf(++count));

			for (int i = 0; i < parts.length; ++i) {

				getReceiver().literal(String.valueOf(i), parts[i]);
			}

			getReceiver().endRecord();
		}
	}

	public void setHeaderLines(final int headerLines) {

		if(headerLines < 0) {
			
			throw new IllegalArgumentException("the number of header lines must be greater than 0");
		}
		
		this.headerLines = headerLines;
		this.headerLinesCount = 0;
		this.header = new String[headerLines][0];
		this.header[0] = new String[0];
		
		if(headerLines == 1) {
			
			this.schemaHeaderLine = 0;
		}
	}

	public void setSchemaHeaderLine(final int schemaHeaderLine) {

		this.schemaHeaderLine = schemaHeaderLine - 1;
	}
}
