package de.avgl.dmp.converter.mf.stream.converter;

import java.io.Reader;
import java.util.NoSuchElementException;
import java.util.Scanner;

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
@Description("Emits each line read as a string.")
@In(Reader.class)
@Out(String.class)
public final class LineReader extends DefaultObjectPipe<Reader, ObjectReceiver<String>> {

	private static final int	BUFFER_SIZE	= 1024 * 1024 * 16;

	private static String		lineEnding;

	public LineReader() {

		lineEnding = "\r\n";
	}

	public LineReader(final String lineEndingArg) {

		lineEnding = lineEndingArg;
	}

	@Override
	public void process(final Reader reader) {
		assert !isClosed();
		assert null != reader;
		process(reader, getReceiver());
	}

	public static void process(final Reader reader, final ObjectReceiver<String> receiver) {

		// note: Scanner is slower than BufferedReader and has a smaller buffer size (initially); however, the line ending pattern
		// in BufferedReader can only be one character; so maybe one need to implement a own BufferedReader implementation
		
		final Scanner tmpScanner = new Scanner(reader);
		final Scanner scanner = tmpScanner.useDelimiter(lineEnding);
		
		String line = null;

		try {
			line = scanner.nextLine();
			
			while (line != null) {
				
				receiver.process(line);
				line = scanner.nextLine();
			}
		} catch (NoSuchElementException e) {
			
			line = null;
			
			scanner.close();
			tmpScanner.close();
		} catch (IllegalStateException e) {
			
			scanner.close();
			tmpScanner.close();
			
			throw new MetafactureException(e);
		}
		
		scanner.close();
		tmpScanner.close();
	}

}
