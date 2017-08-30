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
package org.dswarm.controller.guice;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import org.dswarm.controller.eventbus.CSVConverterEventRecorder;
import org.dswarm.controller.eventbus.JSONConverterEventRecorder;
import org.dswarm.controller.eventbus.SchemaEventRecorder;
import org.dswarm.controller.eventbus.XMLConverterEventRecorder;
import org.dswarm.controller.eventbus.XMLSchemaEventRecorder;
import org.dswarm.controller.status.DatabaseHealthCheck;
import org.dswarm.controller.status.MetricsReporter;
import org.dswarm.controller.utils.DMPControllerUtils;
import org.dswarm.controller.utils.DataModelUtil;

/**
 * The Guice configuration of the controller module. Interface/classes that are registered here can be utilised for injection.
 * Mainly event recorders, e.g., {@link XMLConverterEventRecorder}, are registered here.
 *
 * @author phorn
 */
public class DMPModule extends AbstractModule {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configure() {

		bind(SchemaEventRecorder.class);
		bind(CSVConverterEventRecorder.class);
		bind(XMLConverterEventRecorder.class);
		bind(JSONConverterEventRecorder.class);
		bind(XMLSchemaEventRecorder.class);

		bind(DataModelUtil.class);
		// TODO bind persistence services here ???

		bind(MetricsReporter.class);
		bind(DMPControllerUtils.class);
	}

	@Provides
	@Singleton
	protected HealthCheckRegistry provideHealthCheckRegistry(final DatabaseHealthCheck databaseHealthCheck) {
		final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
		healthCheckRegistry.register("database", databaseHealthCheck);
		healthCheckRegistry.register("deadlocks", new ThreadDeadlockHealthCheck());

		return healthCheckRegistry;
	}
}
