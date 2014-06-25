package org.dswarm.converter.mf.stream;

import org.culturegraph.mf.framework.ObjectReceiver;

import com.google.common.collect.ImmutableList;

import org.dswarm.persistence.model.internal.gdm.GDMModel;

/**
 * @author tgaengler
 */
public class GDMModelReceiver implements ObjectReceiver<GDMModel> {

	private ImmutableList.Builder<GDMModel>	builder	= ImmutableList.builder();
	private ImmutableList<GDMModel>			collection;

	@Override
	public void process(final GDMModel gdmModel) {

		builder.add(gdmModel);
	}

	@Override
	public void resetStream() {

		builder = ImmutableList.builder();
	}

	@Override
	public void closeStream() {

		buildCollection();
	}

	public ImmutableList<GDMModel> getCollection() {

		if (collection == null) {

			buildCollection();
		}

		return collection;
	}

	private void buildCollection() {

		collection = builder.build();
	}
}
