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
package org.dswarm.persistence;

import java.lang.management.ManagementFactory;

import ch.qos.logback.classic.LoggerContext;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.logback.InstrumentedAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.init.ExecutionScope;
import org.dswarm.init.ExecutionScoped;
import org.dswarm.init.util.DMPUtil;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.job.utils.TransformationDeserializer;
import org.dswarm.persistence.monitoring.MonitoringLogger;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.service.MaintainDBService;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.internal.InternalServiceFactoryImpl;
import org.dswarm.persistence.service.job.ComponentService;
import org.dswarm.persistence.service.job.FilterService;
import org.dswarm.persistence.service.job.FunctionService;
import org.dswarm.persistence.service.job.MappingService;
import org.dswarm.persistence.service.job.ProjectService;
import org.dswarm.persistence.service.job.TransformationService;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.resource.ResourceService;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.ContentSchemaService;
import org.dswarm.persistence.service.schema.MappingAttributePathInstanceService;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.dswarm.persistence.service.schema.SchemaService;

/**
 * The Guice configuration of the persistence module. Interface/classes that are registered here can be utilised for injection.
 *
 * @author phorn
 * @author tgaengler
 */
public class PersistenceModule extends AbstractModule {

	private static final ExecutionScope EXECUTION = new ExecutionScope();

	/**
	 * registers all persistence services and other related properties etc.
	 */
	@Override
	protected void configure() {
		bindScope(ExecutionScoped.class, EXECUTION);
		bind(ExecutionScope.class).toInstance(EXECUTION);

		bind(ResourceService.class).in(Scopes.SINGLETON);
		bind(ConfigurationService.class).in(Scopes.SINGLETON);
		bind(AttributeService.class).in(Scopes.SINGLETON);
		bind(AttributePathService.class).in(Scopes.SINGLETON);
		bind(ClaszService.class).in(Scopes.SINGLETON);
		bind(SchemaService.class).in(Scopes.SINGLETON);
		bind(FunctionService.class).in(Scopes.SINGLETON);
		bind(ComponentService.class).in(Scopes.SINGLETON);
		bind(TransformationService.class).in(Scopes.SINGLETON);
		bind(DataModelService.class).in(Scopes.SINGLETON);
		bind(MappingService.class).in(Scopes.SINGLETON);
		bind(FilterService.class).in(Scopes.SINGLETON);
		bind(ProjectService.class).in(Scopes.SINGLETON);
		bind(MappingAttributePathInstanceService.class).in(Scopes.SINGLETON);
		bind(SchemaAttributePathInstanceService.class).in(Scopes.SINGLETON);
		bind(ContentSchemaService.class).in(Scopes.SINGLETON);
		bind(MaintainDBService.class).in(Scopes.SINGLETON);
		bind(UUIDService.class).in(Scopes.SINGLETON);

		bind(InternalModelServiceFactory.class).to(InternalServiceFactoryImpl.class).in(Scopes.SINGLETON);
		bind(DMPUtil.class);

		bind(String.class).annotatedWith(Names.named("Monitoring")).toInstance("dswarm.monitoring");
		bind(MonitoringLogger.class)
				.annotatedWith(Names.named("Monitoring"))
				.to(MonitoringLogger.class)
				.in(EXECUTION);
	}

	/**
	 * Provides the metric registry to register objects for metric statistics.
	 *
	 * @return a {@link MetricRegistry} instance as singleton
	 */
	@Provides
	@Singleton
	protected static MetricRegistry provideMetricRegistry() {
		final MetricRegistry registry = new MetricRegistry();
		instrumentLogback(registry);
		instrumentJvm(registry);

		return registry;
	}

	/**
	 * Provides the metric registry to register objects for metric statistics.
	 * This instance is specific for advanced monitoring only and should be
	 * injected using the {@code @Named("Monitoring")} annotation.
	 *
	 * @return a {@link MetricRegistry} instance as singleton
	 */
	@Provides @Named("Monitoring") @ExecutionScoped
	protected static MetricRegistry provideMonitoringMetricRegistry() {
		return new MetricRegistry();
	}

	@Provides @Named("Monitoring") @Singleton
	protected static Logger provideMonitoringLogger(@Named("Monitoring") final String loggerName) {
		return LoggerFactory.getLogger(loggerName);
	}

	@Provides @Named("Monitoring") @Singleton
	protected static ObjectMapper providerMonitoringMapper() {
		return new ObjectMapper();
	}

	private static void instrumentLogback(final MetricRegistry registry) {
		final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
		final ch.qos.logback.classic.Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);

		final InstrumentedAppender metrics = new InstrumentedAppender(registry);
		metrics.setContext(root.getLoggerContext());
		metrics.start();
		root.addAppender(metrics);
	}

	private static void instrumentJvm(final MetricRegistry registry) {
		registry.register("jvm.file_descriptors", new FileDescriptorRatioGauge());
		registry.register("jvm.buffer_pool", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
		registry.register("jvm.class_loading", new ClassLoadingGaugeSet());
		registry.register("jvm.gc", new GarbageCollectorMetricSet());
		registry.register("jvm.memory", new MemoryUsageGaugeSet());
		registry.register("jvm.threads", new ThreadStatesGaugeSet());
	}

	public static class DmpDeserializerModule extends SimpleModule {

		public DmpDeserializerModule() {
			super("DmpDeserializerModule");

			addDeserializer(Transformation.class, new TransformationDeserializer());
		}
	}
}
