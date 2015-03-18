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

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Locale;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.ExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.model.resource.DataModel;

import static com.codahale.metrics.MetricRegistry.name;

public final class MonitoringLogger implements Reporter {

	private static final Marker EXECUTION_MARKER = MarkerFactory.getMarker("EXECUTION");
	private static final Marker INGEST_MARKER = MarkerFactory.getMarker("INGEST");

	private final ObjectMapper mapper;
	private final MetricRegistry registry;
	private final Logger logger;
	private final long rateFactor;
	private final double durationFactor;
	private final String durationUnit;
	private final String rateUnit;
	private final Timer executionsTimer;
	private final Timer ingestTimer;
	private final String executionsTimerName;
	private final String ingestTimerName;
	private final MetricFilter noSpecialTimer;
	private static final CharMatcher MATCHER =
			CharMatcher.ASCII
					.and(CharMatcher.JAVA_LETTER_OR_DIGIT)
					.negate()
					.or(CharMatcher.WHITESPACE);

	@Inject
	private MonitoringLogger(
			@Named("Monitoring") final ObjectMapper mapper,
			@Named("Monitoring") final MetricRegistry registry,
			@Named("Monitoring") final Logger logger) {
		this.mapper = mapper;
		this.registry = registry;
		this.logger = logger;

		// TODO: config value
		final TimeUnit rateUnit = TimeUnit.SECONDS;
		final TimeUnit durationUnit = TimeUnit.MILLISECONDS;

		this.rateUnit = calculateRateUnit(rateUnit);
		this.durationUnit = durationUnit.toString().toLowerCase(Locale.US);
		rateFactor = rateUnit.toSeconds(1);
		durationFactor = 1.0 / durationUnit.toNanos(1);

		executionsTimerName = name(Task.class, "executions");
		executionsTimer = registry.timer(executionsTimerName);

		ingestTimerName = name(DataModel.class, "ingest");
		ingestTimer = registry.timer(ingestTimerName);

		noSpecialTimer = (name, metric) ->
				!(executionsTimerName.equals(name) || ingestTimerName.equals(name));
	}

	public void report() {
		final SortedMap<String, Meter> meters = registry.getMeters();
		final SortedMap<String, Timer> timers = registry.getTimers(noSpecialTimer);

		meters.forEach(this::logMeter);
		timers.forEach(this::logTimer);
		logTimer(executionsTimerName, executionsTimer, EXECUTION_MARKER);
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

	private void logTimer(final String name, final Timer timer) {
		if (logger.isInfoEnabled()) {
			try {
				logger.info(serialiseTimer(name, timer));
			} catch (final IOException | MonitoringException e) {
				logger.warn("Could not log timer", e);
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

	private String serialiseMeter(final String name, final Metered meter) throws MonitoringException, IOException {
		return serialiseMetric(generator -> writeMetered(name, meter, generator));
	}

	private String serialiseTimer(final String name, final Timer timer) throws MonitoringException, IOException {
		return serialiseMetric(generator -> {
			writeMetered(name, timer, generator);
			writeSnapshot(generator, timer.getSnapshot());
		});
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

	private void writeMetered(final String name, final Metered meter, final JsonGenerator generator) {
		try {
			generator.writeStringField("name", name);
			generator.writeNumberField("count", meter.getCount());
			generator.writeNumberField("mean_rate", convertRate(meter.getMeanRate()));
			generator.writeNumberField("m1", convertRate(meter.getOneMinuteRate()));
			generator.writeNumberField("m5", convertRate(meter.getFiveMinuteRate()));
			generator.writeNumberField("m15", convertRate(meter.getFifteenMinuteRate()));
			generator.writeStringField("rate_unit", rateUnit);
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
			generator.writeStringField("duration_unit", durationUnit);
		} catch (final IOException e) {
			throw new MonitoringException(e);
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

	private void markExecution(final DMPObject entity) {
		logActionWithMarker(entity, EXECUTION_MARKER);
	}

	private void markIngest(final DMPObject entity) {
		logActionWithMarker(entity, INGEST_MARKER);
	}

	private void logActionWithMarker(final DMPObject entity, final Marker marker) {
		if (logger.isInfoEnabled()) {
			final Instant now = Instant.now();
			final String entityName = entity.getClass().getSimpleName();
			logger.info(marker, "{} {} of [{}] at [{}], unix [{}]",
					entityName, marker.getName().toLowerCase(), entity.getUuid(), now, now.getEpochSecond());
		}
	}

	public MonitoringHelper startIngest(final DataModel dataModel) {
		markIngest(dataModel);
		final MDCCloseable mdc = setEntityIdentifier(dataModel);

		monitorEntity(dataModel.getDataResource());
		monitorEntity(dataModel.getSchema());

		return new MonitoringHelper(ingestTimer, mdc, this);
	}

	public MonitoringHelper startExecution(final Task task) {
		markExecution(task);
		final MDCCloseable mdc = setEntityIdentifier(task);

		task.getJob().getMappings().forEach(this::monitorEntity);
		monitorEntity(task.getInputDataModel(), "source");
		monitorEntity(task.getOutputDataModel(), "target");

		return new MonitoringHelper(executionsTimer, mdc, this);
	}

	private static MDCCloseable setEntityIdentifier(final ExtendedBasicDMPJPAObject entity) {
		final String identifier = getEntityIdentifier(entity);
		return MDC.putCloseable("entityIdentifier", identifier);
	}

	private static String getEntityIdentifier(final ExtendedBasicDMPJPAObject entity) {
		final String entityClass =
				entity.getClass().getSimpleName();

		final String entityName =
				StringUtils.defaultIfEmpty(entity.getName(), "Unknown " + entityClass);

		final String entityDescription =
				StringUtils.defaultString(entity.getDescription());

		final String baseIdentifier =
				String.format("%s-%s-%s", entityClass, entityName, entityDescription);

		final String normalizedIdentifier =
				StringUtils.stripAccents(baseIdentifier);

		final Iterable<String> asciiParts =
				Splitter.on(MATCHER).omitEmptyStrings().split(normalizedIdentifier);

		final String identifier =
				Joiner.on('-').join(asciiParts);

		return StringUtils.abbreviate(identifier, 65);
	}

	public void monitorEntity(final DMPObject mapping) {
		monitorEntity(mapping, null);
	}

	public void monitorEntity(final DMPObject entity, final String suffix) {
		if (entity != null && entity.getUuid() != null) {
			registry.meter(name(entity.getClass(), entity.getUuid(), suffix)).mark();
		}
	}

	public static final class MonitoringHelper implements AutoCloseable {

		private final Context context;
		private final MDCCloseable taskIdentifier;
		private final MonitoringLogger monitoringLogger;

		private MonitoringHelper(final Timer executionsTimer, final MDCCloseable taskIdentifier, final MonitoringLogger monitoringLogger) {
			this.taskIdentifier = taskIdentifier;
			this.monitoringLogger = monitoringLogger;
			context = executionsTimer.time();
		}

		@Override
		public void close() {
			context.close();
			monitoringLogger.report();
			taskIdentifier.close();
		}
	}

	private static final class MonitoringException extends RuntimeException {
		public MonitoringException(final Throwable cause) {
			super(cause);
		}
	}
}
