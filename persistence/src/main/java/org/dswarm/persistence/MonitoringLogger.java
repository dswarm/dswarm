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
package org.dswarm.persistence;

import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;

import org.dswarm.init.Monitoring;


@Singleton
public final class MonitoringLogger implements Reporter {

	private final Provider<MetricRegistry> registryProvider;
	private final Logger logger;
	private final long rateFactor;
	private final double durationFactor;
	private final String durationUnit;
	private final String rateUnit;

	@Inject
	private MonitoringLogger(
			@Monitoring final Provider<MetricRegistry> registryProvider,
			@Monitoring final Logger logger) {
		this.registryProvider = registryProvider;
		this.logger = logger;

		// TODO: assistedInject
		final TimeUnit rateUnit = TimeUnit.SECONDS;
		final TimeUnit durationUnit = TimeUnit.MILLISECONDS;

		this.rateUnit = calculateRateUnit(rateUnit);
		this.durationUnit = durationUnit.toString().toLowerCase(Locale.US);
		rateFactor = rateUnit.toSeconds(1);
		durationFactor = 1.0 / durationUnit.toNanos(1);
	}

	public void report() {
		// TODO: async logging with disruptor
		synchronized (this) {
			final MetricRegistry registry = registryProvider.get();
			final SortedMap<String, Meter> meters = registry.getMeters();
			final SortedMap<String, Timer> timers = registry.getTimers();
			report(meters, timers);
		}
	}

	private void report(final Map<String, Meter> meters, final Map<String, Timer> timers) {
		meters.forEach(this::logMeter);
		timers.forEach(this::logTimer);
	}

	private void logMeter(final String name, final Meter meter) {
		if (logger.isInfoEnabled()) {
			logger.info(
					"type=METER, name={}, count={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}",
					name,
					meter.getCount(),
					convertRate(meter.getMeanRate()),
					convertRate(meter.getOneMinuteRate()),
					convertRate(meter.getFiveMinuteRate()),
					convertRate(meter.getFifteenMinuteRate()),
					rateUnit);
		}
	}

	private void logTimer(final String name, final Timer timer) {
		if (logger.isInfoEnabled()) {
			final Snapshot snapshot = timer.getSnapshot();
			logger.info(
					"type=TIMER, name={}, count={}, min={}, max={}, mean={}, stddev={}, " +
							"median={}, p75={}, p95={}, p98={}, p99={}, p999={}, mean_rate={}, " +
							"m1={}, m5={}, m15={}, rate_unit={}, duration_unit={}",
					name,
					timer.getCount(),
					convertDuration(snapshot.getMin()),
					convertDuration(snapshot.getMax()),
					convertDuration(snapshot.getMean()),
					convertDuration(snapshot.getStdDev()),
					convertDuration(snapshot.getMedian()),
					convertDuration(snapshot.get75thPercentile()),
					convertDuration(snapshot.get95thPercentile()),
					convertDuration(snapshot.get98thPercentile()),
					convertDuration(snapshot.get99thPercentile()),
					convertDuration(snapshot.get999thPercentile()),
					convertRate(timer.getMeanRate()),
					convertRate(timer.getOneMinuteRate()),
					convertRate(timer.getFiveMinuteRate()),
					convertRate(timer.getFifteenMinuteRate()),
					rateUnit,
					durationUnit);
		}
	}

	private static String calculateRateUnit(final TimeUnit unit) {
		final String s = unit.toString().toLowerCase(Locale.US);
		return s.substring(0, s.length() - 1);
	}

	private double convertDuration(final double duration) {
		return duration * durationFactor;
	}

	private double convertRate(final double rate) {
		return rate * rateFactor;
	}
}
