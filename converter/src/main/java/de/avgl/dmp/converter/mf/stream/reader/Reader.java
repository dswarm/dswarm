package de.avgl.dmp.converter.mf.stream.reader;

import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;


public interface Reader<RECORDTYPE> extends ObjectPipe<java.io.Reader, StreamReceiver>  {
	
	/**
	 * Reads a single record
	 * @param entry one record
	 */
	void read(final RECORDTYPE entry);

}
