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

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.MDC.MDCCloseable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.model.resource.DataModel;

import static com.codahale.metrics.MetricRegistry.name;

public final class MonitoringLogger implements Reporter {

	private static final Marker EXECUTION_MARKER = MarkerFactory.getMarker("EXECUTION");
	private static final Marker INGEST_MARKER = MarkerFactory.getMarker("INGEST");


	private final long continuousInterval;
	private final double durationFactor;
	private final TimeUnit durationUnit;
	private final String durationUnitName;
	private final MarkedTimer executionsTimer;
	private final MarkedTimer ingestTimer;
	private final Logger logger;
	private final ObjectMapper mapper;
	private final MetricFilter noSpecialTimer;
	private final long rateFactor;
	private final TimeUnit rateUnit;
	private final String rateUnitName;
	private final MetricRegistry registry;
	private final List<MarkedTimer> specialTimers;

	@Inject
	private MonitoringLogger(
			@Named("Monitoring") final ObjectMapper mapper,
			@Named("Monitoring") final MetricRegistry registry,
			@Named("Monitoring") final Logger logger,
			@Named("dswarm.monitoring.rate-unit") final String rateUnitFromConfig,
			@Named("dswarm.monitoring.duration-unit") final String durationUnitFromConfig,
			@Named("dswarm.monitoring.continuous-interval") final long continuousIntervalInMillisFromConfig) {
		this.mapper = mapper;
		this.registry = registry;
		this.logger = logger;

		rateUnit = TimeUnit.valueOf(rateUnitFromConfig.toUpperCase());
		durationUnit = TimeUnit.valueOf(durationUnitFromConfig.toUpperCase());
		continuousInterval = continuousIntervalInMillisFromConfig;

		rateUnitName = rateUnitFromConfig.substring(0, rateUnitFromConfig.length() - 1);
		durationUnitName = durationUnitFromConfig;

		rateFactor = rateUnit.toSeconds(1);
		durationFactor = 1.0 / durationUnit.toNanos(1);

		final String executionsTimerName = name(Task.class, "executions");
		executionsTimer = new MarkedTimer(
				executionsTimerName,
				EXECUTION_MARKER,
				registry.timer(executionsTimerName));

		final String ingestTimerName = name(DataModel.class, "ingest");
		ingestTimer = new MarkedTimer(
				ingestTimerName,
				INGEST_MARKER,
				registry.timer(ingestTimerName));

		specialTimers = ImmutableList.of(executionsTimer, ingestTimer);

		noSpecialTimer = (name, metric) ->
				!specialTimers.stream().anyMatch(mt -> mt.matches(name));
	}

	public void report() {
		report(markedTimer -> true);
	}

	public MonitoringHelper startExecution(final Task task) {
		final EntityIdentification identification = EntityIdentification.of(task);
		final MDCCloseable mdc = identification.putMDC();

		task.getJob().getMappings().forEach(this::monitorEntity);
		monitorEntity(task.getInputDataModel(), "source");
		monitorEntity(task.getOutputDataModel(), "target");

		return startMonitoring(executionsTimer, identification, mdc);
	}

	public MonitoringHelper startIngest(final DataModel dataModel) {
		final EntityIdentification identification = EntityIdentification.of(dataModel);
		final MDCCloseable mdc = identification.putMDC();

		monitorEntity(dataModel.getDataResource());
		monitorEntity(dataModel.getSchema());

		return startMonitoring(ingestTimer, identification, mdc);
	}

	void logActionWithMarker(final EntityIdentification entity, final Marker marker, final Operation operation) {
		if (logger.isInfoEnabled()) {
			final Instant now = Instant.now();
			final String entityName = entity.getClass().getSimpleName();
			logger.info(marker, "{} {} {} of [{}] at [{}], unix [{}]",
					operation.toString().toLowerCase(Locale.ENGLISH),
					entityName,
					marker.getName().toLowerCase(),
					entity.getUuid(),
					now,
					now.getEpochSecond());
		}
	}

	void report(final Predicate<MarkedTimer> selectSpecialTimer) {
		final SortedMap<String, Meter> meters = registry.getMeters();
		final SortedMap<String, Timer> timers = registry.getTimers(noSpecialTimer);

		report(selectSpecialTimer, meters, timers);
	}

	private double convertDuration(final double duration) {
		return duration * durationFactor;
	}

	private double convertRate(final double rate) {
		return rate * rateFactor;
	}

	private void logMeter(final String name, final Metered meter) {
		if (logger.isInfoEnabled()) {
			try {
				logger.info(serialiseMeter(name, meter));
			} catch (final IOException | MonitoringException e) {
				logger.warn("Could not log meter", e);
			}
		}
	}

	private void logTimer(final String name, final Timer timer, final Marker marker) {
		if (logger.isInfoEnabled()) {
			try {
				logger.info(marker, serialiseTimer(name, timer));
			} catch (final IOException | MonitoringException e) {
				logger.warn("Could not log timer", e);
			}
		}
	}

	private void logTimer(final String name, final Timer timer) {
		if (logger.isInfoEnabled()) {
			try {
				logger.info(serialiseTimer(name, timer));
			} catch (final IOException | MonitoringException e) {
				logger.warn("Could not log timer", e);
			}
		}
	}

	private void monitorEntity(final DMPObject entity, final String suffix) {
		if (entity != null && entity.getUuid() != null) {
			registry.meter(name(entity.getClass(), entity.getUuid(), suffix)).mark();
		}
	}

	private void monitorEntity(final DMPObject mapping) {
		monitorEntity(mapping, null);
	}

	private void report(
			final Predicate<MarkedTimer> selectSpecialTimer,
			final Map<String, Meter> meters,
			final Map<String, Timer> timers) {
		meters.forEach(this::logMeter);
		timers.forEach(this::logTimer);

		specialTimers.stream()
				.filter(selectSpecialTimer)
				.forEach(mt -> logTimer(mt.getName(), mt.getTimer(), mt.getMarker()));
	}

	private String serialiseMeter(final String name, final Metered meter) throws MonitoringException, IOException {
		return serialiseMetric(generator -> writeMetered(name, meter, generator));
	}

	private String serialiseMetric(final Consumer<JsonGenerator> body) throws MonitoringException, IOException {
		final StringWriter writer = new StringWriter();
		final JsonGenerator generator = mapper.getFactory().createGenerator(writer);

		generator.writeStartObject();
		body.accept(generator);
		generator.writeEndObject();

		generator.flush();
		generator.close();

		return writer.toString();
	}

	private String serialiseTimer(final String name, final Timer timer) throws MonitoringException, IOException {
		return serialiseMetric(generator -> {
			writeMetered(name, timer, generator);
			writeSnapshot(generator, timer.getSnapshot());
		});
	}

	private MonitoringHelper startMonitoring(final MarkedTimer timer, final EntityIdentification entity, final MDCCloseable mdc) {
		final ContinuousReporter reporter = new ContinuousReporter(
				logger, timer.getMarker(), registry, rateUnit, durationUnit);
		if (logger.isInfoEnabled()) {
			reporter.start(continuousInterval, TimeUnit.MILLISECONDS);
		} else {
			reporter.stop();
		}

		return new MonitoringHelper(timer, entity, mdc, reporter, this);
	}

	private void writeMetered(final String name, final Metered meter, final JsonGenerator generator) {
		try {
			generator.writeStringField("name", name);
			generator.writeNumberField("count", meter.getCount());
			generator.writeNumberField("mean_rate", convertRate(meter.getMeanRate()));
			generator.writeNumberField("m1", convertRate(meter.getOneMinuteRate()));
			generator.writeNumberField("m5", convertRate(meter.getFiveMinuteRate()));
			generator.writeNumberField("m15", convertRate(meter.getFifteenMinuteRate()));
			generator.writeStringField("rate_unit", rateUnitName);
		} catch (final IOException e) {
			throw new MonitoringException(e);
		}
	}

	private void writeSnapshot(final JsonGenerator generator, final Snapshot snapshot) {
		try {
			generator.writeNumberField("min", convertDuration(snapshot.getMin()));
			generator.writeNumberField("max", convertDuration(snapshot.getMax()));
			generator.writeNumberField("mean", convertDuration(snapshot.getMean()));
			generator.writeNumberField("stddev", convertDuration(snapshot.getStdDev()));
			generator.writeNumberField("median", convertDuration(snapshot.getMedian()));
			generator.writeNumberField("p75", convertDuration(snapshot.get75thPercentile()));
			generator.writeNumberField("p95", convertDuration(snapshot.get95thPercentile()));
			generator.writeNumberField("p98", convertDuration(snapshot.get98thPercentile()));
			generator.writeNumberField("p99", convertDuration(snapshot.get99thPercentile()));
			generator.writeNumberField("p999", convertDuration(snapshot.get999thPercentile()));
			generator.writeStringField("duration_unit", durationUnitName);
		} catch (final IOException e) {
			throw new MonitoringException(e);
		}
	}
}
