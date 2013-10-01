package de.avgl.dmp.converter.flow;

import java.io.IOException;
import java.io.Reader;

import com.google.common.collect.ImmutableList;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.stream.converter.StreamToTriples;
import org.culturegraph.mf.types.Triple;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.mf.stream.reader.CsvReader;
import de.avgl.dmp.persistence.model.resource.Configuration;

public class CSVSourceResourceTriplesFlow extends CSVResourceFlow<ImmutableList<Triple>> {

	private static class ListTripleReceiver implements ObjectReceiver<Triple> {
		ImmutableList.Builder<Triple> builder = ImmutableList.builder();
		ImmutableList<Triple> collection = null;

		@Override
		public void process(Triple obj) {
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

	public CSVSourceResourceTriplesFlow(String encoding, Character escapeCharacter, Character quoteCharacter, Character columnDelimiter, String rowDelimiter) {
		super(encoding, escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter);
	}

	public CSVSourceResourceTriplesFlow(Configuration configuration) throws DMPConverterException {
		super(configuration);
	}

	@Override
	protected ImmutableList<Triple> process(ObjectPipe<String, ObjectReceiver<Reader>> opener, String obj, CsvReader pipe) {
		ListTripleReceiver tripleReceiver = new ListTripleReceiver();
		pipe.setReceiver(new StreamToTriples()).setReceiver(tripleReceiver);

		opener.process(obj);
		return tripleReceiver.getCollection();
	}

	public static CSVSourceResourceTriplesFlow fromConfiguration(final Configuration configuration) throws IOException, DMPConverterException {
		return new CSVSourceResourceTriplesFlow(configuration);
	}
}
