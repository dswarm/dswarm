package de.avgl.dmp.persistence;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

import de.avgl.dmp.persistence.services.ConfigurationService;
import de.avgl.dmp.persistence.services.InternalService;
import de.avgl.dmp.persistence.services.ResourceService;
import de.avgl.dmp.persistence.services.SchemaService;
import de.avgl.dmp.persistence.services.impl.InternalServiceImpl;
import de.avgl.dmp.persistence.services.impl.SchemaServiceImpl;

public class PersistenceModule extends AbstractModule {

	@Override
	protected void configure() {

		bind(ResourceService.class).in(Scopes.SINGLETON);
		bind(ConfigurationService.class).in(Scopes.SINGLETON);

		bind(InternalService.class).to(InternalServiceImpl.class);
		bind(SchemaService.class).to(SchemaServiceImpl.class);
	}

	@Provides @Singleton
	protected ObjectMapper provideObjectMapper() {
		final ObjectMapper mapper = new ObjectMapper();
		final JaxbAnnotationModule module = new JaxbAnnotationModule();

		mapper.registerModule(module)
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

		return mapper;
	}

	@Provides @Singleton
	protected MetricRegistry provideMetricRegistry() {
		return new MetricRegistry();
	}

	@Provides @Singleton
	protected EventBus provideEventBus() {
		final ExecutorService executorService = Executors.newCachedThreadPool();

		return new AsyncEventBus(executorService);

//		//	synchronous event bus
//		return new EventBus();
	}
}
