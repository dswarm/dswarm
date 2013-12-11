package de.avgl.dmp.persistence;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import de.avgl.dmp.persistence.mapping.JsonToPojoMapper;
import de.avgl.dmp.persistence.service.InternalServiceFactory;
import de.avgl.dmp.persistence.service.impl.InternalServiceFactoryImpl;
import de.avgl.dmp.persistence.service.job.ComponentService;
import de.avgl.dmp.persistence.service.job.FunctionService;
import de.avgl.dmp.persistence.service.job.TransformationService;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.resource.ResourceService;
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.service.schema.AttributeService;
import de.avgl.dmp.persistence.service.schema.ClaszService;
import de.avgl.dmp.persistence.service.schema.SchemaService;

public class PersistenceModule extends AbstractModule {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PersistenceModule.class);

	@Override
	protected void configure() {
		final URL resource = Resources.getResource("dmp.properties");
		final Properties properties = new Properties();
		try {
			properties.load(resource.openStream());
		} catch (final IOException e) {
			LOG.error("Could not load dmp.properties", e);
		}

		final String tdbPath = properties.getProperty("tdb_path", "target/h2");

		bind(String.class).annotatedWith(Names.named("TdbPath")).toInstance(tdbPath);

		bind(JsonToPojoMapper.class);

		bind(ResourceService.class); //.in(Scopes.SINGLETON);
		bind(ConfigurationService.class); //.in(Scopes.SINGLETON);
		bind(AttributeService.class).in(Scopes.SINGLETON);
		bind(AttributePathService.class).in(Scopes.SINGLETON);
		bind(ClaszService.class).in(Scopes.SINGLETON);
		bind(SchemaService.class).in(Scopes.SINGLETON);
		bind(FunctionService.class).in(Scopes.SINGLETON);
		bind(ComponentService.class).in(Scopes.SINGLETON);
		bind(TransformationService.class).in(Scopes.SINGLETON);
		bind(DataModelService.class).in(Scopes.SINGLETON);

		bind(InternalServiceFactory.class).to(InternalServiceFactoryImpl.class).in(Scopes.SINGLETON);
	}

	@Provides
	@Singleton
	protected ObjectMapper provideObjectMapper() {
		final ObjectMapper mapper = new ObjectMapper();
		final JaxbAnnotationModule module = new JaxbAnnotationModule();

		mapper.registerModule(module).setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

		return mapper;
	}

	@Provides
	@Singleton
	protected MetricRegistry provideMetricRegistry() {
		return new MetricRegistry();
	}

	@Provides
	@Singleton
	protected EventBus provideEventBus() {
		final ExecutorService executorService = Executors.newCachedThreadPool();

		return new AsyncEventBus(executorService);

		// // synchronous event bus
		// return new EventBus();
	}
}
