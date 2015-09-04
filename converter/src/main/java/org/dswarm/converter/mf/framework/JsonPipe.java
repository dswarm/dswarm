package org.dswarm.converter.mf.framework;

import org.culturegraph.mf.framework.Receiver;
import org.culturegraph.mf.framework.Sender;

/**
 * A {@link JsonReceiver} that also implements the {@link Sender} interface.
 * This interface should be implemented by all modules which receive JSON
 * events and invoke methods on a downstream receiver.
 *
 * @param <R> receiver type of the downstream module
 *
 * @see DefaultJsonPipe
 *
 * @author tgaengler
 */
public interface JsonPipe<R extends Receiver> extends JsonReceiver, Sender<R> {

	// Just a combination of sender and receiver
}
