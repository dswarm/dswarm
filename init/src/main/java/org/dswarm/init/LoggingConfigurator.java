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
package org.dswarm.init;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;
import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import org.slf4j.LoggerFactory;


public final class LoggingConfigurator {

	private static final String LOGGING_PATH_CONFIG = "dswarm.paths.logging";
	private static final String DEPRECATED_CONFIG_ROOT = "dswarm.";
	private static final String LOGGING_CONFIG_ROOT = "dswarm.logging.";
	private static final String LOGLEVEL_CONFIG = "loglevel";
	private static final String ROOT_LOGLEVEL_CONFIG = "root-loglevel";
	private static final String TO_CONSOLE_CONFIG = "dswarm.logging.log-to-console";

	private static final AtomicBoolean ALREADY_CONFIGURED = new AtomicBoolean(false);

	private LoggingConfigurator() {
	}

	public static void configureFrom(final Config config) {
		Preconditions.checkNotNull(config);
		final String loggingPath = config.getString(LOGGING_PATH_CONFIG);
		final Optional<String> logLevel = ConfigModule.firstString(config,
				LOGGING_CONFIG_ROOT + LOGLEVEL_CONFIG,
				DEPRECATED_CONFIG_ROOT + LOGLEVEL_CONFIG);
		final Optional<String> rootLogLevel = ConfigModule.firstString(config,
				LOGGING_CONFIG_ROOT + ROOT_LOGLEVEL_CONFIG,
				DEPRECATED_CONFIG_ROOT + ROOT_LOGLEVEL_CONFIG);
		final boolean resetLoggingContext = !config.getBoolean(TO_CONSOLE_CONFIG);
		configure(
				loggingPath,
				logLevel.orElse("default"),
				rootLogLevel.orElse("default"),
				resetLoggingContext);
	}

	private static void configure(final String loggingPath, final String logLevel, final String rootLogLevel, final boolean resetLoggingContext) {
		if (ALREADY_CONFIGURED.getAndSet(true)) {
			return;
		}

		final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		if (lc.isStarted()) {
			return;
		}

		final StatusManager sm = lc.getStatusManager();
		if (sm != null) {
			sm.add(new InfoStatus("Setting up dswarm configuration.", lc));
		}

		if (resetLoggingContext) {
			lc.reset();
		}

		final Level level = Level.toLevel(logLevel, Level.INFO);
		final Level rootLevel = Level.toLevel(rootLogLevel, Level.INFO);
		configureLogback(loggingPath, lc, level, rootLevel);
		lc.start();
	}

	private static void configureLogback(final String loggingPath, final LoggerContext lc, final Level logLevel, final Level rootLogLevel) {
		configureRootLogger(loggingPath, lc, rootLogLevel);
		configureDswarmLogger(loggingPath, lc, logLevel);
		configureMonitoringLogger(loggingPath, lc);
	}

	private static void configureDswarmLogger(final String loggingPath, final LoggerContext lc, final Level logLevel) {
		final Logger logger = lc.getLogger("org.dswarm");
		logger.setLevel(logLevel);

		LoggingAppender.of(lc)
				.withBasePath(loggingPath).addPath("dmp")
				.withName("FAT").withLevel(Level.TRACE).appendTo(logger)
				.withName("FAD").withLevel(Level.DEBUG).appendTo(logger)
				.withName("FAI").withLevel(Level.INFO).appendTo(logger)
				.withName("FAW").withLevel(Level.WARN).appendTo(logger)
				.withName("FAE").withLevel(Level.ERROR).appendTo(logger);
	}

	private static void configureRootLogger(final String loggingPath, final LoggerContext lc, final Level logLevel) {
		final Logger logger = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		// dedicated logger for org, otherwise third-parties with 'org'
		// would use the 'org.dswarm' logger
		final Logger orgLogger = lc.getLogger("org");
		logger.setLevel(logLevel);
		orgLogger.setLevel(logLevel);

		LoggingAppender.of(lc)
				.withBasePath(loggingPath).addPath("default")
				.withName("RootLogger")
				.withLevel(Level.DEBUG).appendTo(logger).appendTo(orgLogger)
				.withLevel(Level.INFO).appendTo(logger).appendTo(orgLogger)
				.withLevel(Level.WARN).appendTo(logger).appendTo(orgLogger)
				.withLevel(Level.ERROR).appendTo(logger).appendTo(orgLogger);
	}

	private static void configureMonitoringLogger(final String loggingPath, final LoggerContext lc) {
		final Logger logger = lc.getLogger("dswarm.monitoring");
		logger.setLevel(Level.INFO);

		LoggingAppender.of(lc)
				.withBasePath(loggingPath).addPath("monitoring")
				.withPattern("%date | %msg%n")
				.withLevel(Level.INFO)
				.withDiscriminationKey("entityIdentifier")
				// metrics log
				.withoutMarkers("EXECUTION", "INGEST")
				.withName("Monitoring")
				.withLogFileBaseName("metrics")
				.appendTo(logger)
				// task executions log
				.withMarker("EXECUTION")
				.withName("Execution")
				.withLogFileBaseName("executions")
				.appendTo(logger)
				// data model ingest log
				.withMarker("INGEST")
				.withName("Ingest")
				.withLogFileBaseName("ingests")
				.appendTo(logger);
	}
}
