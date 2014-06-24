package de.avgl.dmp.persistence;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.logback.InstrumentedAppender;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.job.utils.TransformationDeserializer;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;
import de.avgl.dmp.persistence.service.internal.InternalServiceFactoryImpl;
import de.avgl.dmp.persistence.service.job.ComponentService;
import de.avgl.dmp.persistence.service.job.FilterService;
import de.avgl.dmp.persistence.service.job.FunctionService;
import de.avgl.dmp.persistence.service.job.MappingService;
import de.avgl.dmp.persistence.service.job.ProjectService;
import de.avgl.dmp.persistence.service.job.TransformationService;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.resource.ResourceService;
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.service.schema.AttributeService;
import de.avgl.dmp.persistence.service.schema.ClaszService;
import de.avgl.dmp.persistence.service.schema.MappingAttributePathInstanceService;
import de.avgl.dmp.persistence.service.schema.SchemaService;

/**
 * The Guice configuration of the persistence module. Interface/classes that are registered here can be utilised for injection.
 *
 * @author phorn
 * @author tgaengler
 */
public class PersistenceModule extends AbstractModule {

	private static final Logger	LOG	= LoggerFactory.getLogger(PersistenceModule.class);

	/**
	 * registers all persistence services and other related properties etc.
	 */
	@Override
	protected void configure() {
		final URL resource = Resources.getResource("dmp.properties");
		final Properties properties = new Properties();
		try {
			properties.load(resource.openStream());
		} catch (final IOException e) {
			PersistenceModule.LOG.error("Could not load dmp.properties", e);
		}

		final String graphEndpoint = properties.getProperty("dmp_graph_endpoint", "http://localhost:7474/graph");
		bind(String.class).annotatedWith(Names.named("dmp_graph_endpoint")).toInstance(graphEndpoint);
		final String reportingEsHost = properties.getProperty("reporting_es_host", "localhost:9200");
		bind(String.class).annotatedWith(Names.named("reporting.es.host")).toInstance(reportingEsHost);

		final GraphDatabaseConfig gdbConfig = new GraphDatabaseConfig(graphEndpoint);
		bind(GraphDatabaseConfig.class).toInstance(gdbConfig);

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

		bind(InternalModelServiceFactory.class).to(InternalServiceFactoryImpl.class).in(Scopes.SINGLETON);
	}

	/**
	 * Provides the metric registry to register objects for metric statistics.
	 *
	 * @return a {@link MetricRegistry} instance as singleton
	 */
	@Provides
	@Singleton
	protected MetricRegistry provideMetricRegistry() {
		final MetricRegistry registry = new MetricRegistry();

		// final InstrumentedAppender appender = new InstrumentedAppender(metricRegistry);
		// appender.activateOptions();
		// LogManager.getRootLogger().addAppender(appender);

		final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
		final ch.qos.logback.classic.Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);

		final InstrumentedAppender metrics = new InstrumentedAppender(registry);
		metrics.setContext(root.getLoggerContext());
		metrics.start();
		root.addAppender(metrics);

		return registry;
	}

	public static class DmpDeserializerModule extends SimpleModule {

		public DmpDeserializerModule() {
			super("DmpDeserializerModule");

			addDeserializer(Transformation.class, new TransformationDeserializer());
		}
	}
}
