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
package org.dswarm.persistence.monitoring;

import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.Marker;


final class ContinuousReporter extends ScheduledReporter {

	private final Logger logger;
	private final Marker marker;

	ContinuousReporter(
			final Logger logger,
			final Marker marker,
			final MetricRegistry registry,
			final TimeUnit rateUnit,
			final TimeUnit durationUnit) {

		super(
				registry,
				"continuous-execution-reporter",
				(name, metric) -> name.endsWith(".cumulative"),
				rateUnit,
				durationUnit);
		this.logger = logger;
		this.marker = marker;
	}

	@Override
	public void report(
			final SortedMap<String, Gauge> gauges,
			final SortedMap<String, Counter> counters,
			final SortedMap<String, Histogram> histograms,
			final SortedMap<String, Meter> meters,
			final SortedMap<String, Timer> timers) {

		final String status = timers.entrySet().stream()
				.filter(entry -> entry.getValue().getCount() > 0)
				.map(entry -> {
					final String name = StringUtils.removeEnd(entry.getKey(), ".cumulative");
					final long count = entry.getValue().getCount();
					return String.format("%s of %s", count, name);
				})
				.collect(Collectors.joining(", "));

		if (!status.isEmpty()) {
			logger.info(marker, "{} in progress - {} so far",
					marker.toString().toLowerCase(), status);
		}
	}
}
