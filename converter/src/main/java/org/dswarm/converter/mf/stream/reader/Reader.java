package org.dswarm.converter.mf.stream.reader;

import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;

/**
 * A generic reader.
 *
 * @author tgaengler
 * @param <RECORDTYPE> the type of the records that should be processed with this reader
 */
public interface Reader<RECORDTYPE> extends ObjectPipe<java.io.Reader, StreamReceiver> {

	/**
	 * Reads a single record
	 *
	 * @param entry one record
	 */
	void read(final RECORDTYPE entry);

}
