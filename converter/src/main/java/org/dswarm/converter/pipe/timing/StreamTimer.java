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

import java.util.Deque;
import java.util.LinkedList;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import org.culturegraph.mf.framework.StreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;


public final class StreamTimer extends TimerBased<StreamReceiver> implements StreamPipe<StreamReceiver> {

	private final Deque<TimingContext> recordContexts;
	private final Deque<TimingContext> entityContexts;

	@Inject
	private StreamTimer(
			@Named("Monitoring") final MetricRegistry registry,
			@Assisted final String prefix) {
		super(registry, prefix);

		recordContexts = new LinkedList<>();
		entityContexts = new LinkedList<>();
	}

	@Override
	public void startRecord(final String identifier) {
		final TimingContext context = startMeasurement(STREAM_RECORDS);
		recordContexts.offerLast(context);
		try {
			getReceiver().startRecord(identifier);
		} catch (final Throwable t) {
			// Remove context for failed downstream calls
			// Not closing/stopping the context does not track the time
			recordContexts.removeLast();
		}
	}

	@Override
	public void endRecord() {
		try {
			getReceiver().endRecord();
		} finally {
			final TimingContext context = recordContexts.pollLast();
			if (context != null) {
				context.stop();
			}
		}
	}

	@Override
	public void startEntity(final String name) {
		final TimingContext context = startMeasurement(STREAM_ENTITIES);
		entityContexts.offerLast(context);
		try {
			getReceiver().startEntity(name);
		} catch (final Throwable t) {
			// Remove context for failed downstream calls
			// Not closing/stopping the context does not track the time
			entityContexts.removeLast();
		}
	}

	@Override
	public void endEntity() {
		try {
			getReceiver().endEntity();
		} finally {
			final TimingContext context = entityContexts.pollLast();
			if (context != null) {
				context.stop();
			}
		}
	}

	@Override
	public void literal(final String name, final String value) {
		final TimingContext context = startMeasurement(STREAM_LITERALS);
		try {
			getReceiver().literal(name, value);
		} finally {
			context.stop();
		}
	}
}
