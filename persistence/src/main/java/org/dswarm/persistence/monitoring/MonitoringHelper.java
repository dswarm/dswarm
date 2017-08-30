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

import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import org.slf4j.MDC;


public final class MonitoringHelper implements AutoCloseable {

	private final Timer.Context context;
	private final EntityIdentification entity;
	private final MDC.MDCCloseable identifier;
	private final MonitoringLogger logger;
	private final ScheduledReporter reporter;
	private final MarkedTimer timer;

	MonitoringHelper(
			final MarkedTimer timer,
			final EntityIdentification entity,
			final MDC.MDCCloseable identifier,
			final ScheduledReporter reporter,
			final MonitoringLogger logger) {
		this.timer = timer;
		this.entity = entity;
		this.identifier = identifier;
		this.reporter = reporter;
		this.logger = logger;

		logger.logActionWithMarker(entity, timer.getMarker(), Operation.START);
		context = timer.getTimer().time();
	}

	@Override
	public void close() {
		context.close();
		reporter.close();
		logger.logActionWithMarker(entity, timer.getMarker(), Operation.FINISHED);
		logger.report(mt -> mt.equals(timer));
		identifier.close();
	}
}
