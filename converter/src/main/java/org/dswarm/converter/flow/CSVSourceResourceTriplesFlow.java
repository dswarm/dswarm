/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.converter.flow;

import java.io.Reader;

import com.google.common.collect.ImmutableList;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.stream.converter.StreamToTriples;
import org.culturegraph.mf.types.Triple;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.mf.stream.reader.CsvReader;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;

/**
 * @author phorn
 */
public class CSVSourceResourceTriplesFlow extends AbstractCSVResourceFlow<ImmutableList<Triple>> {

	public CSVSourceResourceTriplesFlow(final String encoding, final Character escapeCharacter, final Character quoteCharacter,
			final Character columnDelimiter, final String rowDelimiter) {
		super(encoding, escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter);
	}

	public CSVSourceResourceTriplesFlow(final Configuration configuration) throws DMPConverterException {

		super(configuration);
	}

	public CSVSourceResourceTriplesFlow(final DataModel dataModel) throws DMPConverterException {

		super(dataModel);
	}

	@Override
	protected ImmutableList<Triple> process(final ObjectPipe<String, ObjectReceiver<Reader>> opener, final String obj, final CsvReader pipe) {

		final ListTripleReceiver tripleReceiver = new ListTripleReceiver();
		pipe.setReceiver(new StreamToTriples()).setReceiver(tripleReceiver);

		opener.process(obj);
		return tripleReceiver.getCollection();
	}

	private static class ListTripleReceiver implements ObjectReceiver<Triple> {

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
}
