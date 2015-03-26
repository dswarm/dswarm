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

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.name.Named;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.stream.converter.StreamToTriples;
import org.culturegraph.mf.types.Triple;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.mf.stream.reader.CsvReader;
import org.dswarm.converter.pipe.timing.ObjectTimer;
import org.dswarm.converter.pipe.timing.StreamTimer;
import org.dswarm.converter.pipe.timing.TimerBasedFactory;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;

/**
 * @author phorn
 */
public class CSVSourceResourceTriplesFlow extends AbstractCSVResourceFlow<ImmutableList<Triple>> {

	private final MetricRegistry registry;
	private final TimerBasedFactory timerBasedFactory;

	@AssistedInject
	private CSVSourceResourceTriplesFlow(
			@Named("Monitoring") final MetricRegistry registry,
			final TimerBasedFactory timerBasedFactory,
			@Assisted("encoding") final String encoding,
			@Assisted("escapeCharacter") final Character escapeCharacter,
			@Assisted("quoteCharacter") final Character quoteCharacter,
			@Assisted("columnDelimiter") final Character columnDelimiter,
			@Assisted("rowDelimiter") final String rowDelimiter) {
		super(encoding, escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter);
		this.registry = registry;
		this.timerBasedFactory = timerBasedFactory;
	}

	@AssistedInject
	private CSVSourceResourceTriplesFlow(
			@Named("Monitoring") final MetricRegistry registry,
			final TimerBasedFactory timerBasedFactory,
			@Assisted final Configuration configuration) throws DMPConverterException {
		super(configuration);
		this.registry = registry;
		this.timerBasedFactory = timerBasedFactory;
	}

	@AssistedInject
	private CSVSourceResourceTriplesFlow(
			@Named("Monitoring") final MetricRegistry registry,
			final TimerBasedFactory timerBasedFactory,
			@Assisted final DataModel dataModel) throws DMPConverterException {
		super(dataModel);
		this.registry = registry;
		this.timerBasedFactory = timerBasedFactory;
	}

	@Override
	protected ImmutableList<Triple> process(final ObjectPipe<String, ObjectReceiver<Reader>> opener, final String obj, final CsvReader pipe) {

		final ListTripleReceiver tripleReceiver = new ListTripleReceiver();
		final StreamTimer csvInputTimer = timerBasedFactory.forStream("csv-input");
		final ObjectTimer csvTriplesTimer = timerBasedFactory.forObject("csv-triples");
		final ObjectTimer csvReaderTimer = timerBasedFactory.forObject("csv-reader");

		pipe
				.setReceiver(csvInputTimer)
				.setReceiver(new StreamToTriples())
				.setReceiver(csvTriplesTimer)
				.setReceiver(tripleReceiver);

		//noinspection unchecked
		opener.setReceiver(csvReaderTimer).setReceiver(pipe);

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
