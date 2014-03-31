package de.avgl.dmp.converter.mf.stream;

import org.culturegraph.mf.framework.ObjectReceiver;

import com.google.common.collect.ImmutableList;

import de.avgl.dmp.persistence.model.internal.rdf.RDFModel;

/**
 * 
 * @author tgaengler
 *
 */
public class RDFModelReceiver implements ObjectReceiver<RDFModel> {

	private ImmutableList.Builder<RDFModel>	builder	= ImmutableList.builder();
	private ImmutableList<RDFModel>			collection;

	@Override
	public void process(final RDFModel rdfModel) {

		builder.add(rdfModel);
	}

	@Override
	public void resetStream() {

		builder = ImmutableList.builder();
	}

	@Override
	public void closeStream() {

		buildCollection();
	}

	public ImmutableList<RDFModel> getCollection() {

		if (collection == null) {

			buildCollection();
		}

		return collection;
	}

	private void buildCollection() {

		collection = builder.build();
	}
}
