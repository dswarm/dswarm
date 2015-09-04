package org.dswarm.converter.mf.framework;

import org.culturegraph.mf.framework.DefaultSender;
import org.culturegraph.mf.framework.Receiver;

/**
 * Default implementation for {@link JsonPipe}s which simply
 * does nothing.
 *
 * @param <R> receiver type of the downstream module
 *
 * @author tgaengler
 */
public class DefaultJsonPipe<R extends Receiver> extends DefaultSender<R> implements JsonPipe<R> {

	@Override
	public void startObject(final String name) {

		// Default implementation does nothing
	}

	@Override
	public void endObject(final String name) {

		// Default implementation does nothing
	}

	@Override
	public void startArray(final String name) {

		// Default implementation does nothing
	}

	@Override
	public void endArray(final String name) {

		// Default implementation does nothing
	}

	@Override
	public void literal(final String name, final String value) {

		// Default implementation does nothing
	}
}
