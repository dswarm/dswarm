package org.dswarm.converter.mf.framework;

import org.culturegraph.mf.framework.LifeCycle;
import org.culturegraph.mf.framework.Receiver;

/**
 * Interface for objects which receive JSON events.
 * This is just a combination of the XXX interfaces
 * and the metastream {@link LifeCycle} interface.
 *
 * @see JsonPipe
 *
 * @author tgaengler
 */
public interface JsonReceiver extends Receiver {

	// TODO: add methods that should be provided by such a receiver

	void startObject(final String name/* add parameters as needed */);

	void endObject(final String name/* add parameters as needed */);

	void startArray(final String name/* add parameters as needed */);

	void endArray(final String name/* add parameters as needed */);

	void literal(final String name, final String value);
}
