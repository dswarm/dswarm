package org.dswarm.converter.flow;

import com.google.common.collect.ImmutableList;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.types.Triple;

final class ListTripleReceiver implements ObjectReceiver<Triple> {

	private ImmutableList.Builder<Triple>	builder	= ImmutableList.builder();
	private ImmutableList<Triple>			collection;

	@Override
	public void process(final Triple obj) {
		builder.add(obj);
	}

	@Override
	public void resetStream() {
		builder = ImmutableList.builder();
	}

	@Override
	public void closeStream() {
		buildCollection();
	}

	public ImmutableList<Triple> getCollection() {
		if (collection == null) {
			buildCollection();
		}
		return collection;
	}

	private void buildCollection() {
		collection = builder.build();
	}
}
