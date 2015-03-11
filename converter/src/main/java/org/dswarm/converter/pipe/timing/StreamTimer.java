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
package org.dswarm.converter.pipe.timing;

import java.util.Deque;
import java.util.LinkedList;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.culturegraph.mf.framework.StreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;

import org.dswarm.init.Monitoring;

public final class StreamTimer extends TimerBased<StreamReceiver> implements StreamPipe<StreamReceiver> {

	private final Deque<TimingContext> recordContexts;
	private final Deque<TimingContext> entityContexts;

	@Inject
	private StreamTimer(
			@Monitoring final MetricRegistry registry,
			@Assisted final String prefix) {
		super(registry, prefix);

		recordContexts = new LinkedList<>();
		entityContexts = new LinkedList<>();
	}

	@Override
	public void startRecord(final String identifier) {
		final TimingContext context = startMeasurement("records");
		recordContexts.offerLast(context);
		getReceiver().startRecord(identifier);
	}

	@Override
	public void endRecord() {
		getReceiver().endRecord();
		final TimingContext context = recordContexts.pollLast();
		if (context != null) {
			context.stop();
		}
	}

	@Override
	public void startEntity(final String name) {
		final TimingContext context = startMeasurement("entities");
		entityContexts.offerLast(context);
		getReceiver().startEntity(name);
	}

	@Override
	public void endEntity() {
		getReceiver().endEntity();
		final TimingContext context = entityContexts.pollLast();
		if (context != null) {
			context.stop();
		}
	}

	@Override
	public void literal(final String name, final String value) {
		getReceiver().literal(name, value);
		context.stop();
		final TimingContext context = startMeasurement("literals");
	}
}
