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
package org.dswarm.converter.pipe.timing;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import org.culturegraph.mf.framework.Receiver;
import org.culturegraph.mf.framework.Sender;

import static com.codahale.metrics.MetricRegistry.name;

abstract class TimerBased<R extends Receiver> implements Sender<R> {

	protected static final String OBJECT_PROCESS = "process";
	protected static final String STREAM_RECORDS = "records";
	protected static final String STREAM_ENTITIES = "entities";
	protected static final String STREAM_LITERALS = "literals";
	protected static final String XML_ENTITIES = "entities";
	protected static final String XML_ELEMENTS = "elements";
	protected static final String XML_CHARACTERS = "characters";

	private final MetricRegistry registry;
	private final String prefix;
	private final Timer cumulativeTimer;

	private R receiver;

	@Inject
	protected TimerBased(
			@Named("Monitoring") final MetricRegistry registry,
			@Assisted final String prefix) {
		this.registry = registry;
		this.prefix = prefix;

		cumulativeTimer = registry.timer(name(prefix, "cumulative"));
	}

	@Override
	public final <S extends R> S setReceiver(final S receiver) {
		this.receiver = receiver;
		return receiver;
	}

	public final R getReceiver() {
		return receiver;
	}

	@Override
	public void resetStream() {
		if (receiver != null) {
			receiver.resetStream();
		}
	}

	@Override
	public final void closeStream() {
		if (receiver != null) {
			final Context context = cumulativeTimer.time();
			receiver.closeStream();
			context.stop();
		}
	}

	protected final TimingContext startMeasurement(final String qualifier) {
		final Timer timer = registry.timer(name(prefix, qualifier));
		return new TimingContext(timer.time(), cumulativeTimer.time());
	}
}
