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
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;

@Description("Benchmarks the execution time of the downstream modules.")
public final class ObjectTimer<T> extends TimerBased<ObjectReceiver<T>>
		implements ObjectPipe<T, ObjectReceiver<T>> {

	@Inject
	private ObjectTimer(
			@Named("Monitoring") final MetricRegistry registry,
			@Assisted final String prefix) {
		super(registry, prefix);
	}

	@Override
	public void process(final T obj) {
		final TimingContext context = startMeasurement(OBJECT_PROCESS);
		try {
			getReceiver().process(obj);
		} finally {
			context.stop();
		}
	}
}
