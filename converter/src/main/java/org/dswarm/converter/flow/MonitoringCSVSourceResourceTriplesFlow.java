/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
import java.util.Collection;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.types.Triple;
import rx.Emitter;
import rx.Observable;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.mf.stream.converter.StreamToRecordTriples;
import org.dswarm.converter.mf.stream.reader.CsvReader;
import org.dswarm.converter.pipe.timing.ObjectTimer;
import org.dswarm.converter.pipe.timing.StreamTimer;
import org.dswarm.converter.pipe.timing.TimerBasedFactory;
import org.dswarm.persistence.model.resource.DataModel;

/**
 * @author phorn
 */
public class MonitoringCSVSourceResourceTriplesFlow extends CSVSourceResourceTriplesFlow {

	private final TimerBasedFactory timerBasedFactory;

	@Inject
	private MonitoringCSVSourceResourceTriplesFlow(
			final TimerBasedFactory timerBasedFactory,
			@Assisted final DataModel dataModel) throws DMPConverterException {
		super(dataModel);
		this.timerBasedFactory = timerBasedFactory;
	}

	@Override
	protected Observable<Collection<Triple>> process(final ObjectPipe<String, ObjectReceiver<Reader>> opener, final String obj,
			final CsvReader pipe) {

		final ObservableRecordTriplesReceiver tripleReceiver = new ObservableRecordTriplesReceiver();
		final ObjectTimer<Reader> csvReaderTimer = timerBasedFactory.forObject(MonitoringFlowStatics.INPUT_RESOURCE_FILES);
		final StreamTimer csvInputTimer = timerBasedFactory.forStream(MonitoringFlowStatics.CSV_RECORDS);
		final ObjectTimer<Collection<Triple>> csvTriplesTimer = timerBasedFactory.forObject(MonitoringFlowStatics.CSV_TRIPLES);

		pipe
				.setReceiver(csvInputTimer)
				.setReceiver(new StreamToRecordTriples())
				.setReceiver(csvTriplesTimer)
				.setReceiver(tripleReceiver);

		opener.setReceiver(csvReaderTimer).setReceiver(pipe);

		return Observable.create(subscriber -> {

			tripleReceiver.getObservable().subscribe(subscriber);

			opener.process(obj);
			opener.closeStream();
		}, Emitter.BackpressureMode.BUFFER);
	}
}
