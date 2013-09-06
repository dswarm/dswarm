package de.avgl.dmp.converter.sink;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;

/**
 * Buffers all input {@link String}ss into a @{link StringBuffer}.
 * Retrieve the buffered value by calling {@link #toString()}.
 *
 * @author Paul Horn <phorn@avantgarde-labs.de>
 */
@Description("Buffers all input Strings into a StringBuffer")
@In(String.class)
public class ObjectBufferWriter implements ObjectReceiver<String> {

	private final StringBuffer sb = new StringBuffer();

	@Override
	public void process(String obj) {
		sb.append(obj);
	}

	@Override
	public void resetStream() {
		sb.setLength(0);
	}

	@Override
	public void closeStream() {
		resetStream();
	}

	/**
	 * @return the buffered strings
	 */
	@Override
	public String toString() {
		return sb.toString();
	}
}

