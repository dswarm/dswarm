package de.avgl.dmp.converter.sink;

import org.culturegraph.mf.framework.ObjectReceiver;


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
		// close StringBuilder
	}

	@Override
	public String toString() {
		return sb.toString();
	}
}

