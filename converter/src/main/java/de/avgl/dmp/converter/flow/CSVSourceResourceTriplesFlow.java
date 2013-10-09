package de.avgl.dmp.converter.flow;

import java.io.Reader;

import com.google.common.collect.ImmutableList;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.stream.converter.StreamToTriples;
import org.culturegraph.mf.types.Triple;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.mf.stream.reader.CsvReader;
import de.avgl.dmp.persistence.model.resource.Configuration;

public class CSVSourceResourceTriplesFlow extends AbstractCSVResourceFlow<ImmutableList<Triple>> {

	public CSVSourceResourceTriplesFlow(final String encoding, final Character escapeCharacter,
										final Character quoteCharacter, final Character columnDelimiter,
										final String rowDelimiter) {
		super(encoding, escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter);
	}

	public CSVSourceResourceTriplesFlow(final Configuration configuration) throws DMPConverterException {
		super(configuration);
	}

	@Override
	protected ImmutableList<Triple> process(final ObjectPipe<String, ObjectReceiver<Reader>> opener, final String obj,
											final CsvReader pipe) {
		final ListTripleReceiver tripleReceiver = new ListTripleReceiver();
		pipe.setReceiver(new StreamToTriples()).setReceiver(tripleReceiver);

		opener.process(obj);
		return tripleReceiver.getCollection();
	}

	private static class ListTripleReceiver implements ObjectReceiver<Triple> {
		private ImmutableList.Builder<Triple> builder = ImmutableList.builder();
		private ImmutableList<Triple> collection = null;

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
}
