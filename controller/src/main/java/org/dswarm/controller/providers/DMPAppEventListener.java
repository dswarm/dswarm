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
package org.dswarm.controller.providers;

import java.io.IOException;

import javax.inject.Inject;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.status.MetricsReporter;

public class DMPAppEventListener implements ApplicationEventListener {

	private static final Logger		LOG	= LoggerFactory.getLogger(DMPAppEventListener.class);

	private final MetricsReporter	metricsReporter;

	@Inject
	public DMPAppEventListener(final MetricsReporter metricsReporter) {
		this.metricsReporter = metricsReporter;
	}

	@Override
	public void onEvent(final ApplicationEvent event) {
		if (event.getType() == ApplicationEvent.Type.INITIALIZATION_FINISHED) {
			onStart();
		}
	}

	@Override
	public RequestEventListener onRequest(final RequestEvent requestEvent) {
		return null;
	}

	private void onStart() {
		try {
			DMPAppEventListener.LOG.trace("starting metrics reporting");
			metricsReporter.start();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
