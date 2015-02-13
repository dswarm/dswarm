/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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


import java.util.concurrent.atomic.AtomicBoolean;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.StatusManager;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import org.slf4j.LoggerFactory;


public final class LoggingConfigurator {

	private static final String FILE_SEPARATOR = System.getProperty("file.separator", "/");
	private static final String LOGGING_PATH_CONFIG = "dswarm.paths.logging";
	private static final String LOGLEVEL_CONFIG = "dswarm.loglevel";
	private static final String ROOT_LOGLEVEL_CONFIG = "dswarm.root-loglevel";

	private static final AtomicBoolean ALREADY_CONFIGURED = new AtomicBoolean(false);

	private LoggingConfigurator() {}

	public static void configureFrom(final Config config) {
		Preconditions.checkNotNull(config);
		configure(
				config.getString(LOGGING_PATH_CONFIG),
				config.getString(ROOT_LOGLEVEL_CONFIG),
				config.getString(LOGLEVEL_CONFIG));
	}

	private static void configure(final String loggingPath, final String logLevel, final String rootLogLevel) {
		if (ALREADY_CONFIGURED.getAndSet(true)) {
			return;
		}

		final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		if (lc.isStarted()) {
			return;
		}

		final StatusManager sm = lc.getStatusManager();
		if (sm != null)  {
			sm.add(new InfoStatus("Setting up dswarm configuration.", lc));
		}

		final Level level = Level.toLevel(logLevel, Level.INFO);
		final Level rootLevel = Level.toLevel(rootLogLevel, Level.INFO);
		configureLogback(loggingPath, lc, level, rootLevel);
	}

	private static String filePath(final String... paths) {
		return Joiner.on(FILE_SEPARATOR).join(paths);
	}

	private static void configureLogback(final String loggingPath, final LoggerContext lc, final Level logLevel, final Level rootLogLevel) {
		configureDswarmLogger(loggingPath, lc, logLevel);
		configureRootLogger(loggingPath, lc, rootLogLevel);
		OnConsoleStatusListener.addNewInstanceToContext(lc);
	}

	private static void configureDswarmLogger(final String loggingPath, final LoggerContext lc, final Level logLevel) {
		final Logger dswarmLogger = lc.getLogger("org.dswarm");
		dswarmLogger.setLevel(logLevel);
		dswarmLogger.addAppender(addFileAppender(lc, loggingPath, Level.TRACE));
		dswarmLogger.addAppender(addFileAppender(lc, loggingPath, Level.DEBUG));
		dswarmLogger.addAppender(addFileAppender(lc, loggingPath, Level.INFO));
		dswarmLogger.addAppender(addFileAppender(lc, loggingPath, Level.WARN));
		dswarmLogger.addAppender(addFileAppender(lc, loggingPath, Level.ERROR));
	}

	private static void configureRootLogger(final String loggingPath, final LoggerContext lc, final Level logLevel) {
		final Logger rootLogger = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		rootLogger.setLevel(logLevel);
		rootLogger.addAppender(addFileAppender(lc, loggingPath, "default", Level.DEBUG));
		rootLogger.addAppender(addFileAppender(lc, loggingPath, "default", Level.INFO));
		rootLogger.addAppender(addFileAppender(lc, loggingPath, "default", Level.WARN));
		rootLogger.addAppender(addFileAppender(lc, loggingPath, "default", Level.ERROR));
	}

	private static Appender<ILoggingEvent> addFileAppender(final LoggerContext lc, final String loggingPath, final Level level) {
		final String basePath = filePath(loggingPath, "dmp", level.levelStr.toLowerCase());
		final String name = String.format("FA%s", level.levelStr.charAt(0));

		return getFileAppender(lc, level, basePath, name);
	}

	private static Appender<ILoggingEvent> addFileAppender(final LoggerContext lc, final String loggingPath, final String alternative, final Level level) {
		final String basePath = filePath(loggingPath, alternative, level.levelStr.toLowerCase());
		final String name = alternative.toUpperCase();

		return getFileAppender(lc, level, basePath, name);
	}

	private static Appender<ILoggingEvent> getFileAppender(final LoggerContext lc, final Level level, final String basePath, final String name) {
		final RollingFileAppender<ILoggingEvent> fileAppender = createRollingFileAppender(lc, name, basePath);

		final RollingPolicy rollingPolicy = getRollingPolicy(lc, basePath, getTriggeringPolicy(lc));
		rollingPolicy.setParent(fileAppender);
		fileAppender.setRollingPolicy(rollingPolicy);
		fileAppender.addFilter(getFilter(lc, level));
		fileAppender.setEncoder(getEncoder(lc));
		rollingPolicy.start();
		fileAppender.start();

		return fileAppender;
	}

	private static RollingFileAppender<ILoggingEvent> createRollingFileAppender(final LoggerContext lc, final String name, final String basePath) {
		final RollingFileAppender<ILoggingEvent> traceFileAppender = new RollingFileAppender<>();
		traceFileAppender.setContext(lc);
		traceFileAppender.setName(name);
		traceFileAppender.setFile(filePath(basePath, "messages.log"));
		return traceFileAppender;
	}

	private static TimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent> getTriggeringPolicy(final LoggerContext lc) {
		final SizeAndTimeBasedFNATP<ILoggingEvent> fnatp = new SizeAndTimeBasedFNATP<>();
		fnatp.setContext(lc);
		fnatp.setMaxFileSize("50MB");
		return fnatp;
	}

	private static RollingPolicy getRollingPolicy(final LoggerContext lc, final String basePath, final TimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent> triggeringPolicy) {
		final TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
		rollingPolicy.setContext(lc);
		rollingPolicy.setFileNamePattern(filePath(basePath, "messages.%d{yyyy-MM-dd}.%i.log"));
		rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(triggeringPolicy);
		rollingPolicy.setMaxHistory(30);
		return rollingPolicy;
	}

	private static Filter<ILoggingEvent> getFilter(final LoggerContext lc, final Level level) {
		final ThresholdFilter filter = new ThresholdFilter();
		filter.setContext(lc);
		filter.setLevel(level.levelStr);
		filter.start();
		return filter;
	}

	private static Encoder<ILoggingEvent> getEncoder(final LoggerContext lc) {
		final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(lc);
		encoder.setPattern("%date %level [%thread] %logger [%file:%line] %msg%n");
		encoder.start();
		return encoder;
	}
}
