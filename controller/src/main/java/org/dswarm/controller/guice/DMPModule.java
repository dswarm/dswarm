package org.dswarm.controller.guice;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import org.dswarm.controller.eventbus.CSVConverterEventRecorder;
import org.dswarm.controller.eventbus.SchemaEventRecorder;
import org.dswarm.controller.eventbus.XMLConverterEventRecorder;
import org.dswarm.controller.eventbus.XMLSchemaEventRecorder;
import org.dswarm.controller.resources.utils.ResourceUtilsFactory;
import org.dswarm.controller.status.DMPStatus;
import org.dswarm.controller.status.DatabaseHealthCheck;
import org.dswarm.controller.status.MetricsReporter;
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

		bind(SchemaEventRecorder.class).asEagerSingleton();
		bind(CSVConverterEventRecorder.class).asEagerSingleton();
		bind(XMLConverterEventRecorder.class).asEagerSingleton();
		bind(XMLSchemaEventRecorder.class).asEagerSingleton();

		bind(DataModelUtil.class);
		bind(ResourceUtilsFactory.class);

		bind(MetricsReporter.class);
		bind(DMPStatus.class);

		// TODO: read from config
		bind(Integer.class).annotatedWith(Names.named("reporting.interval")).toInstance(60);
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
